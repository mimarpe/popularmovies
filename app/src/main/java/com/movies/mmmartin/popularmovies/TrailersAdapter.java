package com.movies.mmmartin.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by mmmartin on 2/7/16.
 */
public class TrailersAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 1;

    public static class ViewHolder {
        public final TextView nameView;
        public final ImageView playView;

        public ViewHolder(View view) {
            nameView = (TextView) view.findViewById(R.id.trailer_text);
            playView = (ImageView) view.findViewById(R.id.trailer_play);
        }
    }

    public TrailersAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_trailer_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.nameView.setText(cursor.getString(DetailActivityFragment.INDEX_TRAILER_NAME));
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
