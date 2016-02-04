package com.movies.mmmartin.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.movies.mmmartin.popularmovies.data.MoviesContract;
import com.movies.mmmartin.popularmovies.sync.PopularMoviesSyncAdapter;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private static final String[] MOVIE_COLUMNS_POSTERS = {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
    };
    static final int COL_MOVIE_ID       = 0;
    static final int COL_POSTER_PATH    = 1;

    private static final int POSTERS_LOADER = 0;

    public MainActivityFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieUri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        PopularMoviesSyncAdapter.syncImmediately(getActivity());
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Sort order:  Ascending, according to user setting.
        String sortOrder = Utility.getOrderLoader(getActivity());

        Uri moviePostersUri = MoviesContract.MovieEntry.buildMoviePosterUri();

        return new CursorLoader(getActivity(),
                moviePostersUri,
                MOVIE_COLUMNS_POSTERS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//        mMoviesAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        mMoviesAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(POSTERS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }
    

}
