package com.example.shams.moviestageone.movie.trailers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.shams.moviestageone.Constants;
import com.example.shams.moviestageone.R;
import com.example.shams.moviestageone.movie.MovieDetailsActivity;
import com.example.shams.moviestageone.movie.main.Movies;
import com.example.shams.moviestageone.network.connection.utils.NetworkStatues;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.ContentValues.TAG;


public class MovieTrailerFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<MovieTrailer>>
        , MovieTrailerAdapter.TrailerListClickListener {

    private final int MOVIE_LOADER_ID = 3;
    private Uri movieTrailersUri;
    private final String SAVE_INSTANCE_STATE_KEY = "trailers_saved_instance_key";

    @BindView(R.id.rv_movie_trailers_id)
    RecyclerView recyclerView;

    @BindView(R.id.progress_bar_trailer_activity)
    ProgressBar progressBar;

    @BindView(R.id.sr_movie_trailer_fragment_id)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.tv_empty_text_view_trailer_fragment_id)
    TextView emptyTextView;

    MovieTrailerAdapter movieTrailerAdapter;

    LoaderManager loaderManager;

    private ArrayList<MovieTrailer> movieTrailerArrayList;


    public MovieTrailerFragment() {
        // Required empty public constructor
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final LoaderManager.LoaderCallbacks loaderCallbacks = this;
        loaderManager = getLoaderManager();
        movieTrailerArrayList = new ArrayList<MovieTrailer>();

        if (savedInstanceState == null){

            movieTrailerAdapter =
                    new MovieTrailerAdapter(movieTrailerArrayList,this);

            if (NetworkStatues.isConnected(getActivity())) {
                hideConnectionErrorDisplayData();
                loaderManager.initLoader(MOVIE_LOADER_ID, null, loaderCallbacks);
            } else {
                emptyTextView.setText(getString(R.string.no_internet_connection_connect_and_try_again));
                displayConnectionErrorHideData();
            }

        }else {
            movieTrailerArrayList = savedInstanceState.getParcelableArrayList(SAVE_INSTANCE_STATE_KEY);
            Log.e(TAG, "onActivityCreated: move List equal : " + movieTrailerArrayList );
            movieTrailerAdapter =
                    new MovieTrailerAdapter(movieTrailerArrayList,this);
        }

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL ,false);

        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setAdapter(movieTrailerAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (NetworkStatues.isConnected(getActivity())) {
                    hideConnectionErrorDisplayData();
                    loaderManager.restartLoader(MOVIE_LOADER_ID, null, loaderCallbacks);
                } else {
                    emptyTextView.setText(getString(R.string.no_internet_connection_connect_and_try_again));
                    displayConnectionErrorHideData();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public void hideConnectionErrorDisplayData() {
        emptyTextView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void displayConnectionErrorHideData() {
        emptyTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_trailer, container, false);
        ButterKnife.bind(this , view);

        return view;
    }


    @NonNull
    @Override
    public Loader<List<MovieTrailer>> onCreateLoader(int id, @Nullable Bundle args) {

        Movies currentMovie;
        if (MovieDetailsActivity.currentFavouriteMovie != null) {
            currentMovie = MovieDetailsActivity.currentFavouriteMovie;
        } else {
            currentMovie = getActivity().
                    getIntent().
                    getParcelableExtra(Constants.MOVIE_OBJECT_KEY);
        }

        int movieId = currentMovie.getMovieId() ;

        movieTrailersUri = Uri.parse(Constants.BASE_MOVIE_URL).buildUpon()
                .appendEncodedPath(String.valueOf(movieId))
                .appendEncodedPath("videos")
                .appendQueryParameter(Constants.API_KEY,Constants.API_KEY_VALUE)
                .build();

        progressBar.setVisibility(View.VISIBLE);

        return new MovieTrailerAsyncTask(getActivity(),movieTrailersUri.toString());

    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<MovieTrailer>> loader, List<MovieTrailer> data) {
        progressBar.setVisibility(View.GONE);

        movieTrailerAdapter.clearAdapter();

        if (data != null){
            hideConnectionErrorDisplayData();
            movieTrailerArrayList = (ArrayList<MovieTrailer>) data;
            movieTrailerAdapter.setmMovieReviewsList(data);
        } else {
            emptyTextView.setText(getString(R.string.no_trailers));
            displayConnectionErrorHideData();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<MovieTrailer>> loader) {
        movieTrailerAdapter.clearAdapter();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_INSTANCE_STATE_KEY,movieTrailerArrayList);
    }

    @Override
    public void onTrailerListClickListener(int position) {
        MovieTrailer movieTrailer = movieTrailerAdapter.getItem(position);
        String trailerKey = movieTrailer.getTrailerKey();

        Intent youtubeAppIntent;
        Intent youtubeWebIntent;

        youtubeAppIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKey));

        youtubeWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailerKey));


        if (youtubeAppIntent.resolveActivity(getActivity().getPackageManager()) != null){
            getActivity().startActivity(youtubeAppIntent);
        }else {

            if (youtubeWebIntent.resolveActivity(getActivity().getPackageManager()) != null){
                getActivity().startActivity(youtubeWebIntent);
            }
        }

    }
}
