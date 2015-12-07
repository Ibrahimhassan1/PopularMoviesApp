package com.softups.popularmovies;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by ibrahimhassan on 8/26/15.
 */
public class Global extends Application {
    // update this before you run the app
    public static String themoviedbAPIKEY = "yourkey";

    @Override
    public void onCreate() {
        super.onCreate();

        // This is added to enable Picasso caching
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(false);
        built.setLoggingEnabled(false);
        Picasso.setSingletonInstance(built);

    }

    /**
     * Checks if the device has Internet connection.
     *
     * @return <code>true</code> if the phone is connected to the Internet.
     */

    public static boolean getNetworkState(Context pContext) {
        ConnectivityManager connect = null;
        connect = (ConnectivityManager) pContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connect != null) {
            NetworkInfo resultM = connect
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo resultW = connect
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (resultM != null && resultM.isConnectedOrConnecting()) {
                return true;
            } else if (resultW != null && resultW.isConnectedOrConnecting()) {
                return true;
            } else {
                return false;
            }
        } else
            return false;
    }
}
