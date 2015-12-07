package com.softups.popularmovies;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ibrahimhassan on 8/24/15.
 */
public class FavoriteListViewAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> urls;

    static class ViewHolder {
        public ImageView playButtonIV;
        public TextView trailerTitleTV;
    }

    public FavoriteListViewAdapter(Activity context, ArrayList<String> urls) {
        super(context, R.layout.trailer_list_item, urls);
        this.context = context;
        this.urls = urls;
    }


    @Override public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.trailer_list_item, null);

            // configure view holder
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.playButtonIV = (ImageView) rowView
                    .findViewById(R.id.trailer_play_button_image_view);
            viewHolder.trailerTitleTV = (TextView) rowView
                    .findViewById(R.id.trailer_title_text_view);
            rowView.setTag(viewHolder);

        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Get the image URL for the current position.
        final String url = getItem(position);

        // Setting trailer title
        holder.trailerTitleTV.setText("Trailer " + (position + 1));
        holder.playButtonIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(context,url,Toast.LENGTH_SHORT).show();
                PlayYouTubeVideo(url);
            }
        });


        // measure ListView item (to solve 'ListView inside ScrollView' problem)
        rowView.measure(View.MeasureSpec.makeMeasureSpec(
                        View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        return rowView;
    }

    void PlayYouTubeVideo(String videoURl){
        Intent videoClient = new Intent(Intent.ACTION_VIEW);
        videoClient.setData(Uri.parse(videoURl));
        context.startActivity(videoClient);

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
