package com.movies.mmmartin.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.movies.mmmartin.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMoviesApp";

    private ShareActionProvider mShareActionProvider;
    private String mMovie = null;
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] MOVIE_PROJECTION = {
            MoviesContract.MovieEntry._ID,
            MoviesContract.MovieEntry.COLUMN_MOVIE_ID,
            MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MoviesContract.MovieEntry.COLUMN_OVERVIEW,
            MoviesContract.MovieEntry.COLUMN_RELEASE_DATE,
            MoviesContract.MovieEntry.COLUMN_POSTER_PATH,
            MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE,
            MoviesContract.MovieEntry.COLUMN_POPULARITY,
            MoviesContract.MovieEntry.COLUMN_FAVORITE
    };

    // these indices must match the projection
    static final int INDEX_ID               = 0;
    static final int INDEX_MOVIE_ID         = 1;
    static final int INDEX_ORIGINAL_TITLE   = 2;
    static final int INDEX_OVERVIEW         = 3;
    static final int INDEX_RELEASE_DATE     = 4;
    static final int INDEX_POSTER_PATH      = 5;
    static final int INDEX_VOTE_AVERAGE     = 6;
    static final int INDEX_POPULARITY       = 7;
    static final int INDEX_FAVORITE         = 8;

    private TextView mTitleView;
    private ImageView mImageView;
    private TextView mDateView;
    private TextView mVoteView;
    private Button mButtonView;
    private TextView mOverviewView;
    private ListView mTrailersView;
    private ListView mReviewsView;

    private int isFavorite = 0;
    private Integer mMovieId;

    public DetailActivityFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mImageView = (ImageView) rootView.findViewById(R.id.detail_thumbnail);
        mDateView = (TextView) rootView.findViewById(R.id.detail_date);
        mTitleView = (TextView) rootView.findViewById(R.id.detail_title);
        mVoteView = (TextView) rootView.findViewById(R.id.detail_vote);
        mButtonView = (Button) rootView.findViewById(R.id.detail_favorite);
        mOverviewView = (TextView) rootView.findViewById(R.id.detail_overview);
        mTrailersView = (ListView) rootView.findViewById(R.id.detail_trailers);
        mReviewsView = (ListView) rootView.findViewById(R.id.detail_reviews);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // If onLoadFinished happens before this, we can go ahead and set the share intent now.
        if (mMovie != null) {
            mShareActionProvider.setShareIntent(createShareMovieIntent());
        }
    }

    private Intent createShareMovieIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mMovie + MOVIE_SHARE_HASHTAG);
        return shareIntent;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    MOVIE_PROJECTION,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            // Read movie ID from cursor
            mMovieId = data.getInt(INDEX_MOVIE_ID);

            // Image
            String url = "http://image.tmdb.org/t/p/w185/"+data.getString(DetailActivityFragment.INDEX_POSTER_PATH);
            Picasso.with(getContext()).load(url).into(mImageView);
            String title = data.getString(INDEX_ORIGINAL_TITLE);
            mTitleView.setText(title);
            mDateView.setText(data.getString(INDEX_RELEASE_DATE));
            mVoteView.setText(data.getString(INDEX_VOTE_AVERAGE) + "/10");
            String overview = data.getString(INDEX_OVERVIEW);
            mOverviewView.setText(overview);

            isFavorite = data.getInt(INDEX_FAVORITE);

            switchFavButton();

            mButtonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    favoriteSwitch();
                }
            });

            // We still need this for the share intent
            mMovie = String.format("%s - %s %s", title, overview, MOVIE_SHARE_HASHTAG);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareMovieIntent());
            }
        }
    }

    private void favoriteSwitch() {
        if(mMovie!=null) {
            isFavorite = 1 - isFavorite;

            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    ContentValues values = new ContentValues();
                    values.put("favorite",isFavorite);

                    int update = getActivity().getContentResolver().update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            values,
                            MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[] {Integer.toString(mMovieId)} );

                    return update;
                }

                @Override
                protected void onPostExecute(Integer update) {
                    if(update==1) {
                        // Update the button
                        switchFavButton();

                        Toast.makeText(getActivity(), isFavorite == 0 ? getString(R.string.removefavorite) : getString(R.string.addfavorite), Toast.LENGTH_SHORT).show();
                    }else{
                        isFavorite = 1 - isFavorite;
                        Toast.makeText(getActivity(), getString(R.string.problemfavorite), Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
        return;
    }

    private void switchFavButton() {
        if(isFavorite==0){
            mButtonView.setText(getString(R.string.mark_favorite));
            mButtonView.setTextColor(Color.YELLOW);
        }
        else{
            mButtonView.setText(getString(R.string.unmark_favorite));
            mButtonView.setTextColor(Color.RED);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }

}
