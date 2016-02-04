package com.movies.mmmartin.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIES         = 100;
    static final int MOVIE_DETAIL   = 101;
    static final int TRAILER        = 200;
    static final int REVIEW         = 300;

    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    private static final SQLiteQueryBuilder sReviewQueryBuilder;
    private static final SQLiteQueryBuilder sTrailerQueryBuilder;

    static{
        sMovieQueryBuilder      = new SQLiteQueryBuilder();
        sTrailerQueryBuilder    = new SQLiteQueryBuilder();
        sReviewQueryBuilder     = new SQLiteQueryBuilder();
        sMovieQueryBuilder.setTables    ( MoviesContract.MovieEntry.TABLE_NAME );
        sTrailerQueryBuilder.setTables  ( MoviesContract.TrailerEntry.TABLE_NAME );
        sReviewQueryBuilder.setTables   ( MoviesContract.ReviewEntry.TABLE_NAME );
    }

    private static final String sMovieSelection =
            MoviesContract.MovieEntry.TABLE_NAME+
                    "." + MoviesContract.MovieEntry._ID + " = ? ";

    private static final String sTrailersSelection =
            MoviesContract.TrailerEntry.TABLE_NAME+
                    "." + MoviesContract.TrailerEntry.COLUMN_MOVIE_ID + " = ? ";

    private static final String sReviewsSelection =
            MoviesContract.ReviewEntry.TABLE_NAME+
                    "." + MoviesContract.ReviewEntry.COLUMN_MOVIE_ID + " = ? ";


    private Cursor getMovies(Uri uri, String[] projection, String sortOrder) {
        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieById( Uri uri, String[] projection, String sortOrder) {
        Long idSetting = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieSelection,
                new String[]{Long.toString(idSetting)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrailersById( Uri uri, String[] projection, String sortOrder) {
        Long idSetting = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        return sTrailerQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sTrailersSelection,
                new String[]{Long.toString(idSetting)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getReviewsById( Uri uri, String[] projection, String sortOrder) {
        Long idSetting = MoviesContract.MovieEntry.getMovieIdFromUri(uri);

        return sReviewQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sReviewsSelection,
                new String[]{Long.toString(idSetting)},
                null,
                null,
                sortOrder
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MoviesContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.PATH_MOVIE, MOVIES);
        matcher.addURI(authority, MoviesContract.PATH_MOVIE + "/#", MOVIE_DETAIL);
        matcher.addURI(authority, MoviesContract.PATH_TRAILER + "/#", TRAILER);
        matcher.addURI(authority, MoviesContract.PATH_REVIEW + "/#", REVIEW);

        return matcher;
    }


    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }


    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_DETAIL:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIES:
                return MoviesContract.MovieEntry.CONTENT_TYPE;
            case TRAILER:
                return MoviesContract.TrailerEntry.CONTENT_TYPE;
            case REVIEW:
                return MoviesContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movie/#"
            case MOVIE_DETAIL:
            {
                retCursor = getMovieById(uri, projection, sortOrder);
                break;
            }
            // "movie"
            case MOVIES: {
                retCursor = getMovies(uri, projection, sortOrder);
                break;
            }
            // "review"
            case REVIEW: {
                retCursor = getReviewsById(uri, projection, sortOrder);
                break;
            }
            // "trailer"
            case TRAILER: {
                retCursor = getTrailersById(uri, projection, sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIES: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case REVIEW: {
                long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MoviesContract.ReviewEntry.buildReviewUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case MOVIES:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_DETAIL:
                rowsDeleted = db.delete(
                        MoviesContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(
                        MoviesContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW:
                rowsDeleted = db.delete(
                        MoviesContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIES:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MOVIE_DETAIL:
                rowsUpdated = db.update(MoviesContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRAILER:
                rowsUpdated = db.update(MoviesContract.TrailerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case REVIEW:
                rowsUpdated = db.update(MoviesContract.ReviewEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case REVIEW:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.ReviewEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TRAILER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }


    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}