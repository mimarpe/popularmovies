package com.movies.mmmartin.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by mmmartin on 2/7/16.
 */
public class ReviewsAdapter extends CursorAdapter {
    private static final int VIEW_TYPE_COUNT = 1;

    public static class ViewHolder {
        public final TextView contentView;
        public final TextView authorView;

        public ViewHolder(View view) {
            contentView = (TextView) view.findViewById(R.id.review_text);
            authorView = (TextView) view.findViewById(R.id.review_author);
        }
    }

    public ReviewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = R.layout.list_review_item;

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.contentView.setText(cursor.getString(DetailActivityFragment.INDEX_REVIEW_CONTENT));
        viewHolder.authorView.setText(cursor.getString(DetailActivityFragment.INDEX_REVIEW_AUTHOR));
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
