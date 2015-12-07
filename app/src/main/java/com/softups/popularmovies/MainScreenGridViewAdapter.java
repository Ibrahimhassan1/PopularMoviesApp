package com.softups.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by ibrahimhassan on 8/24/15.
 */
public class MainScreenGridViewAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> urls;

    static class ViewHolder {

        public ImageView posterIV;
    }

    public MainScreenGridViewAdapter(Activity context, ArrayList<String> urls) {
        super(context, R.layout.main_screen_grid_list_item, urls);
        this.context = context;
        this.urls = urls;
    }


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.main_screen_grid_list_item, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.posterIV = (ImageView) rowView
                    .findViewById(R.id.main_screen_image_view);
            rowView.setTag(viewHolder);

        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Get the image URL for the current position.
        String url = getItem(position);

        // Trigger the download of the URL asynchronously into the image view.
        Picasso.with(context) //
                .load(url) //
                .placeholder(R.drawable.placeholder) // resource from Picasso sample
                .error(R.drawable.error) // resource from Picasso sample
                .tag(context) //
                .fit() //
                //.centerInside()
                .into(holder.posterIV);

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
