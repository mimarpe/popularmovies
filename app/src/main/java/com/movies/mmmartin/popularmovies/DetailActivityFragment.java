package com.movies.mmmartin.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.movies.mmmartin.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = DetailActivityFragment.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    static final String DETAIL_MID = "MOVIEID";

    private static final String MOVIE_SHARE_HASHTAG = " #PopularMoviesApp";

    private ShareActionProvider mShareActionProvider;
    private String mMovie = null;
    private Uri mUri;

    final String LOG_TAG = DetailActivityFragment.class.getSimpleName();

    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter mReviewsAdapter;

    private static final int DETAIL_LOADER  = 0;
    private static final int TRAILER_LOADER = 1;
    private static final int REVIEWS_LOADER = 2;

    private static final String TRAILER_SUFFIX = "videos";
    private static final String REVIEW_SUFFIX  = "reviews";

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


    private static final String[] TRAILER_PROJECTION = {
            MoviesContract.TrailerEntry._ID,
            MoviesContract.TrailerEntry.COLUMN_MOVIE_ID,
            MoviesContract.TrailerEntry.COLUMN_KEY,
            MoviesContract.TrailerEntry.COLUMN_NAME,
            MoviesContract.TrailerEntry.COLUMN_SITE
    };

    // these indices must match the projection
    static final int INDEX_TRAILER_ID     = 0;
    static final int INDEX_TRAILER_MID    = 1;
    static final int INDEX_TRAILER_KEY    = 2;
    static final int INDEX_TRAILER_NAME   = 3;
    static final int INDEX_TRAILER_SITE   = 4;


    private static final String[] REVIEW_PROJECTION = {
            MoviesContract.ReviewEntry._ID,
            MoviesContract.ReviewEntry.COLUMN_MOVIE_ID,
            MoviesContract.ReviewEntry.COLUMN_AUTHOR,
            MoviesContract.ReviewEntry.COLUMN_CONTENT,
            MoviesContract.ReviewEntry.COLUMN_URL
    };

    // these indices must match the projection
    static final int INDEX_REVIEW_ID      = 0;
    static final int INDEX_REVIEW_MID     = 1;
    static final int INDEX_REVIEW_AUTHOR  = 2;
    static final int INDEX_REVIEW_CONTENT = 3;
    static final int INDEX_REVIEW_URL     = 4;

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

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(String key);

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
            mMovieId = arguments.getInt(DETAIL_MID);
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

        mTrailersAdapter = new TrailersAdapter(getActivity(), null, 0);
        mTrailersView.setAdapter(mTrailersAdapter);

        mTrailersView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(INDEX_TRAILER_KEY));
                }
            }
        });

        mReviewsAdapter = new ReviewsAdapter(getActivity(), null, 0);
        mReviewsView.setAdapter(mReviewsAdapter);

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
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    private class SyncEntriesTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String entriesJsonStr;

            try {
                final String ENTRIES_BASE_URL = "http://api.themoviedb.org/3/movie/" + params[0] + "/" + params[1];
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(ENTRIES_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, getContext().getString(R.string.api_key))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                entriesJsonStr = buffer.toString();

                if( params[1].contains(TRAILER_SUFFIX) ){
                    getTrailersDataFromJson(entriesJsonStr);
                } else if( params[1].contains(REVIEW_SUFFIX) ) {
                    getReviewsDataFromJson(entriesJsonStr);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attempting
                // to parse it.
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }
    }


    private void getTrailersDataFromJson(String trailersJsonStr)
            throws JSONException {

        final String OWM_RESULTS = "results";
        // Movies information
        final String OWM_ID         = "id";
        final String OWM_SITE       = "site";
        final String OWM_KEY        = "key";
        final String OWM_NAME       = "name";
        // the movie id is on the same level as results

        try {
            JSONObject trailersJson = new JSONObject(trailersJsonStr);

            int movieId = trailersJson.getInt(OWM_ID);

            JSONArray trailersArray = trailersJson.getJSONArray(OWM_RESULTS);

            // Insert the new trailers information into the database
            Vector<ContentValues> cVVector = new Vector<>(trailersArray.length());

            for(int i = 0; i < trailersArray.length(); i++) {
                // These are the values that will be collected.
                String site;
                String key;
                String name;

                // Get the JSON object representing the trailer
                JSONObject trailer = trailersArray.getJSONObject(i);

                site     = trailer.getString(OWM_SITE);

                // only interested to play youtube
                if(!site.contentEquals("YouTube"))
                    continue;

                key   = trailer.getString(OWM_KEY);
                name  = trailer.getString(OWM_NAME);

                ContentValues trailerValues = new ContentValues();

                trailerValues.put(MoviesContract.TrailerEntry.COLUMN_MOVIE_ID, movieId);
                trailerValues.put(MoviesContract.TrailerEntry.COLUMN_SITE, site);
                trailerValues.put(MoviesContract.TrailerEntry.COLUMN_KEY, key);
                trailerValues.put(MoviesContract.TrailerEntry.COLUMN_NAME, name);

                cVVector.add(trailerValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.TrailerEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "Sync Trailer Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }


    private void getReviewsDataFromJson(String reviewsJsonStr)
            throws JSONException {

        final String OWM_RESULTS = "results";
        // Movies information
        final String OWM_ID        = "id";
        final String OWM_CONTENT   = "content";
        final String OWM_AUTHOR    = "author";
        final String OWM_URL       = "url";
        // the movie id is on the same level as results

        try {
            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);

            int movieId = reviewsJson.getInt(OWM_ID);

            JSONArray reviewsArray = reviewsJson.getJSONArray(OWM_RESULTS);

            // Insert the new trailers information into the database
            Vector<ContentValues> cVVector = new Vector<>(reviewsArray.length());

            for(int i = 0; i < reviewsArray.length(); i++) {
                // These are the values that will be collected.
                String content;
                String author;
                String url;

                // Get the JSON object representing the trailer
                JSONObject review = reviewsArray.getJSONObject(i);

                content = review.getString(OWM_CONTENT);
                author  = review.getString(OWM_AUTHOR);
                url     = review.getString(OWM_URL);

                ContentValues reviewsValues = new ContentValues();

                reviewsValues.put(MoviesContract.ReviewEntry.COLUMN_MOVIE_ID, movieId);
                reviewsValues.put(MoviesContract.ReviewEntry.COLUMN_CONTENT, content);
                reviewsValues.put(MoviesContract.ReviewEntry.COLUMN_AUTHOR, author);
                reviewsValues.put(MoviesContract.ReviewEntry.COLUMN_URL, url);

                cVVector.add(reviewsValues);
            }

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.ReviewEntry.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "Sync Reviews Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case DETAIL_LOADER:
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

            case TRAILER_LOADER:
                if (mMovieId > 0){
                    Uri tUri = MoviesContract.TrailerEntry.buildTrailerUri(mMovieId);

                    if(Utility.getNumberEntriesByMovieId(getContext(), tUri)==0){
                        new SyncEntriesTask().execute(mMovieId.toString(), TRAILER_SUFFIX);
                    }
                    return new CursorLoader(
                            getActivity(),
                            tUri,
                            TRAILER_PROJECTION,
                            null,
                            null,
                            MoviesContract.TrailerEntry.COLUMN_MOVIE_ID+" ASC limit 5"
                    );
                }
                return null;

            case REVIEWS_LOADER:
                if (mMovieId > 0){
                    Uri rUri = MoviesContract.ReviewEntry.buildReviewUri(mMovieId);

                    if(Utility.getNumberEntriesByMovieId(getContext(),rUri)==0){
                        new SyncEntriesTask().execute(mMovieId.toString(), REVIEW_SUFFIX);
                    }
                    return new CursorLoader(
                            getActivity(),
                            rUri,
                            REVIEW_PROJECTION,
                            null,
                            null,
                            MoviesContract.ReviewEntry.COLUMN_MOVIE_ID+" ASC limit 5"
                    );
                }
                return null;

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            switch (loader.getId()){
                case DETAIL_LOADER:
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
                    break;
                case TRAILER_LOADER:
                    mTrailersAdapter.swapCursor(data);
                    setListViewHeightBasedOnChildren(mTrailersView);
                    break;
                case REVIEWS_LOADER:
                    mReviewsAdapter.swapCursor(data);
                    setListViewHeightBasedOnChildren(mReviewsView);
                    break;
                default:
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case DETAIL_LOADER:
                break;
            case TRAILER_LOADER:
                mTrailersAdapter.swapCursor(null);
                break;
            case REVIEWS_LOADER:
                mTrailersAdapter.swapCursor(null);
                break;
            default:
        }
    }

    private void favoriteSwitch() {
        if(mMovie!=null) {
            isFavorite = 1 - isFavorite;

            new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... params) {
                    ContentValues values = new ContentValues();
                    values.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, isFavorite);

                    return getActivity().getContentResolver().update(
                            MoviesContract.MovieEntry.CONTENT_URI,
                            values,
                            MoviesContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                            new String[] {Integer.toString(mMovieId)} );
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


    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null || listAdapter.getCount() < 2) {
            // pre-condition
            return;
        }

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
//        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        int totalHeight = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            if (listItem instanceof ViewGroup) listItem.setLayoutParams(lp);
            listItem.measure(widthMeasureSpec, heightMeasureSpec);
//            Log.d(TAG, "Item height: " + listItem.getMeasuredHeight());
            totalHeight += listItem.getMeasuredHeight();
        }

        totalHeight += listView.getPaddingTop() + listView.getPaddingBottom();
        totalHeight += (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);
        listView.requestLayout();
//        Log.d(TAG, "Params height: " + params.height);
    }



}
