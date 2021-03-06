package com.movies.mmmartin.popularmovies;

import android.app.ProgressDialog;
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
import android.widget.AdapterView;
import android.widget.GridView;

import com.movies.mmmartin.popularmovies.data.MoviesContract;
import com.movies.mmmartin.popularmovies.sync.PopularMoviesSyncAdapter;


public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private GridView mGridView;
    private PostersAdapter mGridViewAdapter;
    private int mPosition = GridView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private static final String SORT = "SORT";
    private String mOrder;

    private static final String[] MOVIE_PROJECTION = {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
//            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
//            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH
//            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
//            MoviesContract.MovieEntry.COLUMN_POPULARITY,
//            MoviesContract.MovieEntry.COLUMN_FAVORITE
    };

    // these indices must match the projection
    static final int INDEX_ID               = 0;
    static final int INDEX_MOVIE_ID         = 1;
    static final int INDEX_ORIGINAL_TITLE   = 2;
//    static final int INDEX_OVERVIEW         = 3;
//    static final int INDEX_RELEASE_DATE     = 4;
    static final int INDEX_POSTER_PATH      = 3;
//    static final int INDEX_VOTE_AVERAGE     = 6;
//    static final int INDEX_POPULARITY       = 7;
//    static final int INDEX_FAVORITE         = 8;

    private static final int POSTERS_LOADER = 0;

    public MainActivityFragment() {
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections and other stuff.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri movieUri);

        // Passes the movieId to the activity
        void setMovieId(int movieId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mGridViewAdapter = new PostersAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mGridView = (GridView) rootView.findViewById(R.id.gridView);
        mGridView.setAdapter(mGridViewAdapter);
        // We'll call our MainActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .setMovieId(cursor.getInt(INDEX_MOVIE_ID));
                    ((Callback) getActivity())
                            .onItemSelected(MoviesContract.MovieEntry.buildMovieUri(cursor.getLong(INDEX_ID)));
                }
                mPosition = position;
            }
        });

//        // We want to load more data on scroll end
//        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (firstVisibleItem + visibleItemCount >= totalItemCount) {
//                    // End has been reached
//                }
//            }
//
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
//            }
//        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The gridview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SORT)) {
            mOrder = savedInstanceState.getString(SORT);
        }else {
            mOrder = Utility.getOrderPreference(getContext());
            PopularMoviesSyncAdapter.initializeSyncAdapter(getContext());
        }

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Sort order:  Ascending, according to user setting.
        String sortOrder = Utility.getOrderLoader(getContext());

        CursorLoader cl =  new CursorLoader(getActivity());
        cl.setUri(MoviesContract.MovieEntry.buildMoviePosterUri());
        cl.setProjection(MOVIE_PROJECTION);

        if(sortOrder.contains(getString(R.string.pref_order_favorite))){
            sortOrder=getString(R.string.pref_order_popularity)+" DESC";
            cl.setSelection(MoviesContract.MovieEntry.COLUMN_FAVORITE + " = ?");
            cl.setSelectionArgs(new String[]{Integer.toString(1)});
        }

        sortOrder +=" limit 50";
        cl.setSortOrder(sortOrder);

        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mGridViewAdapter.swapCursor(data);
        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mGridViewAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(POSTERS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onUserOrderChanged( ) {
        ProgressDialog progress = new ProgressDialog(getContext());
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading posters...");
        progress.show();

        updatePosters();
        getLoaderManager().restartLoader(POSTERS_LOADER, null, this);

        progress.dismiss();
    }

    private void updatePosters() {
        PopularMoviesSyncAdapter.syncImmediately(getActivity());
    }


    @Override
    public void onResume() {
        super.onResume();
        String order = Utility.getOrderPreference( getContext() );

        if (order != null && !order.equals(mOrder)) {
            onUserOrderChanged();
            mOrder = order;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        outState.putString(SORT, mOrder);
        super.onSaveInstanceState(outState);
    }

}
