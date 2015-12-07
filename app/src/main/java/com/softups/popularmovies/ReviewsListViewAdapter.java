package com.softups.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ibrahimhassan on 8/24/15.
 */

public class ReviewsListViewAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> urls;

    static class ViewHolder {
        public TextView authorTV;
        public TextView reviewTV;
    }

    public ReviewsListViewAdapter(Activity context, ArrayList<String> urls) {
        super(context, R.layout.review_list_item, urls);
        this.context = context;
        this.urls = urls;
    }


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.review_list_item, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.authorTV = (TextView) rowView
                    .findViewById(R.id.review_author_text_view);
            viewHolder.reviewTV = (TextView) rowView
                    .findViewById(R.id.review_content_text_view);
            rowView.setTag(viewHolder);

        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Get the image URL for the current position.
        final String url = getItem(position);

        // Setting trailer title
        holder.authorTV.setText(url.split(";;")[0]);
        holder.reviewTV.setText(url.split(";;")[1]);


        return rowView;
    }




    @Override public int getCount() {
        return urls.size();
    }

    @Override public String getItem(int position) {
        return urls.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }
}
