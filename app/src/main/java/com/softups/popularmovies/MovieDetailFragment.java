package com.softups.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    View rootView;
    private ProgressDialog dialog;


    TrailersListViewAdapter trailersListViewAdapter;

    ListView trailersListView;
    ArrayList<String> trailerList;
    ArrayList<String> savedMovietrailers;

    ArrayList<String> savedMovieReviews;

    String selectedMovieIndex;
    String selectedMovieID;
    Movie selectedMovie;
    ImageView add_to_favorite;

    String delimiter = ";;";


    static final String DETAIL_MOVIE_KEY = "DETAIL_MOVIE_KEY";



    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
                if (arguments != null) {
                        selectedMovie = arguments.getParcelable(DETAIL_MOVIE_KEY);
                    }

        rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        TextView original_title_tv = (TextView) rootView.findViewById(R.id.original_title_textView);
        TextView release_date_tv = (TextView) rootView.findViewById(R.id.release_date_textView);
        TextView rate_tv = (TextView) rootView.findViewById(R.id.rate_textView);
        TextView plot_tv = (TextView) rootView.findViewById(R.id.plot_textView);

        ImageView poster_iv = (ImageView) rootView.findViewById(R.id.poster_imageView);

        add_to_favorite = (ImageView) rootView.findViewById(R.id.add_to_favorite_button);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        add_to_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String favouriteSavedList = prefs.getString(getString(R.string.pref_favorite_key), "");
                SharedPreferences.Editor editor = prefs.edit();
                // if movie id exist in the list, then remove it, otherwise add it
                if(favouriteSavedList.contains(selectedMovieID)){
                    editor.putString(getString(R.string.pref_favorite_key), favouriteSavedList.replace(selectedMovieID +";",""));
                    editor.remove(selectedMovieID);
                    editor.commit();
                }else {
                    editor.putString(getString(R.string.pref_favorite_key), favouriteSavedList + selectedMovieID + ";");
                    // saving movie details in a string set
                    String movieDetails = selectedMovie.getPoster_path() + delimiter + selectedMovie.getOriginalTitle() + delimiter +
                            selectedMovie.getPlotSynopsis() + delimiter + selectedMovie.getUserRating() + delimiter + selectedMovie.getReleaseDate();

                    editor.putString(selectedMovieID, movieDetails);
                    editor.commit();
                    String savedMovieDetail = prefs.getString(selectedMovieID, null);
                    Log.d("pref", ""+savedMovieDetail.toString());
                }
                Log.d("favouriteSavedList",favouriteSavedList);

                UpdateFavoriteButton();

            }
        });

        // Filling trailers list view
        trailersListView = (ListView) rootView.findViewById(R.id.trailers_list_view);
        trailerList = new ArrayList<>();
        trailersListViewAdapter = new TrailersListViewAdapter(getActivity(), trailerList);

        trailersListView.setAdapter(trailersListViewAdapter);



//        Intent intent = getActivity().getIntent();
        if (arguments != null && selectedMovie != null) {
//            selectedMovie = (Movie) intent.getParcelableExtra("moviedetail");


            // Trigger the download of the URL asynchronously into the image view.
            Picasso.with(getActivity()) //
                    .load(selectedMovie.getPosterURL()) //
                    .placeholder(R.drawable.placeholder) //
                    .error(R.drawable.error) //
                    .tag(getActivity()) //
                    .fit() //
                    .into(poster_iv);

            original_title_tv.setText(selectedMovie.getOriginalTitle());
            release_date_tv.setText(selectedMovie.getReleaseDate().split("-")[0]);
            rate_tv.setText(selectedMovie.getUserRating() + "/10");
            plot_tv.setText(selectedMovie.getPlotSynopsis());
            selectedMovieID = selectedMovie.getId();
            if(savedInstanceState == null) {
                getTrailers(selectedMovie.getId());
            }else {
                savedMovietrailers = new ArrayList<>();
                savedMovietrailers = savedInstanceState.getStringArrayList("savedMovietrailers");
                if (savedMovietrailers != null) {
                    trailersListViewAdapter.addAll(savedMovietrailers);
                    setListViewHeightBasedOnChildren(trailersListView);
                } else {
                    getTrailers(selectedMovie.getId());
                }
            }

        }

        UpdateFavoriteButton();

        // Showing Reviews
        Button reviewButton = (Button) rootView.findViewById(R.id.reviews_button);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getReviews(selectedMovieID);
            }
        });

        return rootView;
    }

   void UpdateFavoriteButton(){
       final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

       String favouriteSavedList = prefs.getString(getString(R.string.pref_favorite_key), "");
       Log.d("UpdateFavoriteButton",favouriteSavedList);

       if(selectedMovieID != null && !favouriteSavedList.equals("") && favouriteSavedList.contains(selectedMovieID)){
           add_to_favorite.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(),R.drawable.favorite_on));
       }else {
           add_to_favorite.setImageDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.favorite_off));
       }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("savedMovietrailers", savedMovietrailers);
//        outState.putParcelableArrayList("movies", movies);
//        outState.putString("sortingOrder", sortingOrder);
    }

    private void getTrailers(String id) {

        GetTrailersTask getTrailersTask = new GetTrailersTask();

        getTrailersTask.execute(id);


    }

    public class GetTrailersTask extends AsyncTask<String, Void, ArrayList<String>> {

        private final String LOG_TAG = GetTrailersTask.class.getSimpleName();

//        private ProgressDialog dialog;

        public GetTrailersTask() {
            if (dialog == null)
                dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            if (!dialog.isShowing()) {
                dialog.setMessage(getString(R.string.trailer_loading_message));
                dialog.show();
            }
        }

        /**
         * Take the String representing the complete popular movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<String> getTrailerFromJson(String movieJsonString)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String results = "results";

            // poster url pieces
            final String baseURL = "https://www.youtube.com/watch?v=";


            savedMovietrailers = new ArrayList<>();

            JSONObject resultJson = new JSONObject(movieJsonString);

            JSONArray itemsArray = resultJson.getJSONArray(results);

            for (int i = 0; i < itemsArray.length(); i++) {
                // add thumbnails to arraylist
                JSONObject trailerJson = (JSONObject) itemsArray.get(i);
                String youtubeID = trailerJson.get("key").toString();

                String poster_path = baseURL + youtubeID;

                String trailerTitle = trailerJson.get("name").toString();

                savedMovietrailers.add(poster_path);
//                Movie movie = new Movie(poster_path, youtubeID, plotSynopsis, userRating, releaseDate);
//                movies.add(movie);
            }


            return savedMovietrailers;

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                // Construct the URL for the themoviedb query
                //http://api.themoviedb.org/3/movie/116741/videos?api_key=368e45a4b9a22e5c649429b70feadc0c
                final String movieID = params[0]; //vote_average.desc or popularity.desc
                final String SEARCH_BASE_URL =
                        "http://api.themoviedb.org/3/movie/"+movieID+"/videos?";

                final String api_key = Global.themoviedbAPIKEY;


                Uri builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                        .appendQueryParameter("api_key", api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
//                Log.d(LOG_TAG, moviesJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
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

            try {

                return getTrailerFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {

//                Log.d(LOG_TAG, "" + result);

                trailersListViewAdapter.clear();
                trailersListViewAdapter.addAll(result);

                trailersListViewAdapter = new TrailersListViewAdapter(getActivity(), trailerList);

                trailersListView.setAdapter(trailersListViewAdapter);
                // saving fetched poster urls for when orientation change ...
                savedMovietrailers = new ArrayList<>();
                savedMovietrailers.addAll(result);
                setListViewHeightBasedOnChildren(trailersListView);



//                Log.d(LOG_TAG, "" + trailersListViewAdapter.getCount());

            }


            if (dialog.isShowing() && trailersListViewAdapter.getCount() > 0) {
                dialog.dismiss();
            }
        }


    }

    private void getReviews(String id) {

        GetReviewsTask getReviewsTask = new GetReviewsTask();

        getReviewsTask.execute(id);


    }

    public class GetReviewsTask extends AsyncTask<String, Void, ArrayList<String>> {

        private final String LOG_TAG = GetReviewsTask.class.getSimpleName();

//        private ProgressDialog dialog;

        public GetReviewsTask() {
            if (dialog == null)
                dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            if (!dialog.isShowing()) {
                dialog.setMessage(getString(R.string.trailer_loading_message));
                dialog.show();
            }
        }

        /**
         * Take the String representing the complete popular movies in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         * <p/>
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private ArrayList<String> getReviewFromJson(String movieJsonString)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String results = "results";

            // poster url pieces
            final String baseURL = "https://www.youtube.com/watch?v=";


            savedMovieReviews = new ArrayList<>();

            JSONObject resultJson = new JSONObject(movieJsonString);

            JSONArray itemsArray = resultJson.getJSONArray(results);

            for (int i = 0; i < itemsArray.length(); i++) {
                // add thumbnails to arraylist
                JSONObject trailerJson = (JSONObject) itemsArray.get(i);
                String auther = trailerJson.get("author").toString();


                String content = trailerJson.get("content").toString();

                savedMovieReviews.add(auther+";;"+content);

            }


            return savedMovieReviews;

        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                // Construct the URL for the themoviedb query
                //http://api.themoviedb.org/3/movie/116741/videos?api_key=368e45a4b9a22e5c649429b70feadc0c
                final String movieID = params[0]; //vote_average.desc or popularity.desc
                final String SEARCH_BASE_URL =
                        "http://api.themoviedb.org/3/movie/"+movieID+"/reviews?";

                final String api_key = Global.themoviedbAPIKEY;


                Uri builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                        .appendQueryParameter("api_key", api_key)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to themoviedb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
//                Log.d(LOG_TAG, moviesJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
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

            try {

                return getReviewFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            if (result != null) {

//                Log.d(LOG_TAG, "" + result);

                Intent intent = new Intent(getActivity(), ReviewsActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, result);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);


                startActivity(intent);
            }


            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }


    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();

        ((ScrollView) rootView.findViewById(R.id.scrollView)).post(new Runnable() {
            public void run() {
                ((ScrollView) rootView.findViewById(R.id.scrollView)).fullScroll(View.FOCUS_UP);
            }
        });
    }
}
