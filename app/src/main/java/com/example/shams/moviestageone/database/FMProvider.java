package com.example.shams.moviestageone.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.shams.moviestageone.R;
import com.example.shams.moviestageone.database.FMContract.MoviesEntry;

public class FMProvider extends ContentProvider {

    private static final String LOG_TAG = FMProvider.class.getSimpleName();

    private static final int MOVIES_ITEMS = 10;

    private static final int MOVIES_ITEM_ID = 20;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(FMContract.CONTENT_AUTHORITY, FMContract.MOVIE_PATH, MOVIES_ITEMS);

        uriMatcher.addURI(FMContract.CONTENT_AUTHORITY, FMContract.MOVIE_PATH + "/#", MOVIES_ITEM_ID);
    }

    private FMDbHelper fmDbHelper;

    @Override
    public boolean onCreate() {
        fmDbHelper = new FMDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sort) {
        SQLiteDatabase sqLiteDatabase = fmDbHelper.getReadableDatabase();

        Cursor cursor;

        int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case MOVIES_ITEMS:
                cursor = sqLiteDatabase.query(MoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sort);
                break;
            case MOVIES_ITEM_ID:
                selection = MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = sqLiteDatabase.query(MoviesEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sort);

                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.can_not_query_for_invalid_uri) + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case MOVIES_ITEMS:
                return insertValues(uri, contentValues);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.can_not_insert_values_for_invalid_uri) + uri);
        }
    }

    private Uri insertValues(Uri uri, ContentValues contentValues) {

        validateInsertValues(contentValues);

        SQLiteDatabase sqLiteDatabase = fmDbHelper.getWritableDatabase();

        long id = sqLiteDatabase.insert(MoviesEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.failed_to_insert_row) + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        SQLiteDatabase sqLiteDatabase = fmDbHelper.getWritableDatabase();
        int deletedRowId;
        int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case MOVIES_ITEMS:
                deletedRowId = sqLiteDatabase.delete(MoviesEntry.TABLE_NAME, selection, selectionArgs);

                if (deletedRowId != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return deletedRowId;
            case MOVIES_ITEM_ID:
                selection = MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                deletedRowId = sqLiteDatabase.delete(MoviesEntry.TABLE_NAME, selection, selectionArgs);

                if (deletedRowId != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return deletedRowId;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.can_not_delete_values_of_invalid_uri) + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case MOVIES_ITEMS:
                return updateValues(uri, contentValues, selection, selectionArgs);
            case MOVIES_ITEM_ID:
                selection = MoviesEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateValues(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.can_not_update_values_for_invalid_uri) + uri);
        }
    }

    private int updateValues(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase sqLiteDatabase = fmDbHelper.getWritableDatabase();

        int rowId = sqLiteDatabase.update(MoviesEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowId != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowId;

    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int matchUri = uriMatcher.match(uri);
        switch (matchUri) {
            case MOVIES_ITEMS:
                return MoviesEntry.CONTENT_LIST_TYPE;
            case MOVIES_ITEM_ID:
                return MoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.un_known_uri) + uri + getContext().getString(R.string.that_match) + matchUri);
        }
    }

    private void validateInsertValues(ContentValues values) {

        Integer movieId = values.getAsInteger(MoviesEntry.COLUMN_MOVIE_ID);
        validateIntegerValues(movieId, getContext().getString(R.string.movie_id_has_invalid_or_null_value) + movieId);

        String movieName = values.getAsString(MoviesEntry.COLUMN_MOVIE_NAME);
        validateStringValues(movieName, getContext().getString(R.string.movie_name_has_invalid_or_null_value) + movieName);

        String movieVoteAverage = values.getAsString(MoviesEntry.COLUMN_MOVIE_VOTE_AVERAGE);
        validateStringValues(movieVoteAverage, getContext().getString(R.string.movie_vote_average_has_invalid_or_null_value) + movieVoteAverage);

        String movieReleaseDate = values.getAsString(MoviesEntry.COLUMN_MOVIE_RELEASE_DATE);
        validateStringValues(movieReleaseDate, getContext().getString(R.string.movie_release_date_has_invalid_or_null_value) + movieReleaseDate);

        String movieOverview = values.getAsString(MoviesEntry.COLUMN_MOVIE_OVERVIEW);
        validateStringValues(movieOverview, getContext().getString(R.string.movie_overview_has_invalid_or_null_value) + movieOverview);

        String moviePoster = values.getAsString(MoviesEntry.COLUMN_MOVIE_POSTER);
        validateStringValues(moviePoster, getContext().getString(R.string.movie_poster_has_invalid_or_null_value) + moviePoster);

        Integer favouriteMovie = values.getAsInteger(MoviesEntry.COLUMN_IS_FAVOURITE);
        validateIntegerValues(favouriteMovie, getContext().getString(R.string.movie_favourite_has_invalid_or_null_value) + favouriteMovie);
    }

    //Helper Methods

    private void validateStringValues(String stringValue, String errorMessage) {
        if (TextUtils.isEmpty(stringValue)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void validateIntegerValues(Integer integerValue, String errorMessage) {
        if (integerValue == null || integerValue < 0) {
            throw new IllegalArgumentException(errorMessage);
        }
    }


}
