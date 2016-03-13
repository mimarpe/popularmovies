package com.movies.mmmartin.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.movies.mmmartin.popularmovies.R;
import com.movies.mmmartin.popularmovies.Utility;
import com.movies.mmmartin.popularmovies.data.MoviesContract;

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

public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the movies, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final int MOVIES_NOTIFICATION_ID = 3004;

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {
            final String MOVIES_BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_BY_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, Utility.getOrderAPI(getContext()))
                    .appendQueryParameter(API_KEY_PARAM, getContext().getString(R.string.api_key))
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return;
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
                return;
            }
            moviesJsonStr = buffer.toString();
            getMoviesDataFromJson(moviesJsonStr);
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

        // This will only happen if there was an error getting or parsing the movies.
        return;
    }

    /**
     * Take the String representing the complete movies in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private void getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        Log.d(LOG_TAG, moviesJsonStr);

        final String OWM_RESULTS = "results";
        // Movies information
        final String OWM_POSTER_PATH    = "poster_path";
        final String OWM_OVERVIEW       = "overview";
        final String OWM_RELEASE_DATE   = "release_date";
        final String OWM_ID             = "id";
        final String OWM_ORIGINAL_TITLE = "original_title";
        final String OWM_POPULARITY     = "popularity";
        final String OWM_VOTE_AVERAGE   = "vote_average";

        try {
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(OWM_RESULTS);

            // Insert the new movie information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());


            for(int i = 0; i < moviesArray.length(); i++) {
                // These are the values that will be collected.
                String poster_path;
                String overview;
                String date;
                Integer id;
                String title;
                Double popularity;
                Double vote_average;

                // Get the JSON object representing the movie
                JSONObject movie = moviesArray.getJSONObject(i);

                poster_path     = movie.getString(OWM_POSTER_PATH);
                overview        = movie.getString(OWM_OVERVIEW);
                date            = movie.getString(OWM_RELEASE_DATE);
                id              = movie.getInt(OWM_ID);
                title           = movie.getString(OWM_ORIGINAL_TITLE);
                popularity      = movie.getDouble(OWM_POPULARITY);
                vote_average    = movie.getDouble(OWM_VOTE_AVERAGE);

                if(poster_path==null || overview==null || id==null
                        || title==null || popularity==null || vote_average==null) {
                    continue;
                }

                ContentValues movieValues = new ContentValues();

                movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, id);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_FAVORITE, 0);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, title);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, overview);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, popularity);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, date);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, vote_average);
                movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER_PATH, poster_path);

                cVVector.add(movieValues);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContext().getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, cvArray);

                String id_pk = MoviesContract.MovieEntry._ID;
                // delete old data so we don't build up an endless history
                getContext().getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI,
                        id_pk + " not in (select " + id_pk +
                                " from " + MoviesContract.MovieEntry.TABLE_NAME + " order by " +
                                id_pk + " desc limit 100)",
                        null);
                // Optimisations: not delete favorites first, keep inserting until we have more than N new movies and stopping at N limit

            }

            Log.d(LOG_TAG, "Sync Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }


    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}