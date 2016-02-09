package com.movies.mmmartin.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by mmmartin on 2/7/16.
 */
public class PostersAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 1;

    public static class ViewHolder {
        public final ImageView posterView;
//        public final TextView posterTextView;

        public ViewHolder(View view) {
            posterView = (ImageView) view.findViewById(R.id.grid_item_poster);
        }
    }

    public PostersAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.grid_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Set poster
        String url = "http://image.tmdb.org/t/p/w185/"+cursor.getString(MainActivityFragment.INDEX_POSTER_PATH);
        Picasso.with(context).load(url).into(viewHolder.posterView);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
