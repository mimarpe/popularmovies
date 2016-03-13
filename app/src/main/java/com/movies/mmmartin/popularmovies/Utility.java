package com.movies.mmmartin.popularmovies;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;


public class Utility {

    public static String getOrderPreference(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String order = prefs.getString(context.getString(R.string.pref_order_label_key),
                context.getString(R.string.pref_order_popularity));

        return order;
    }

    public static String getOrderAPI(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String order = prefs.getString(context.getString(R.string.pref_order_label_key),
                context.getString(R.string.pref_order_popularity));

        if(order.equals(R.string.pref_order_favorite))
            order=context.getString(R.string.pref_order_popularity);

        return order+".desc";
    }

    public static String getOrderLoader(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String order = prefs.getString(context.getString(R.string.pref_order_label_key),
                context.getString(R.string.pref_order_popularity));

        String ret = order + " DESC";

        return ret;
    }

    public static int getNumberEntriesByMovieId(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, new String[]{"count(*)"},
                null, null, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
    }

}
