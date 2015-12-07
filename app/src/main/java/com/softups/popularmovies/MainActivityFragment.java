package com.softups.popularmovies;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
public class MainActivityFragment extends Fragment {

    GridView mPostersGridView;

    private ProgressDialog dialog;

    MainScreenGridViewAdapter mainScreenGridViewAdapter;

    View rootView;

    ArrayList<String> moviesPosters;

    public static ArrayList<Movie> movies;

    private String sortingOrder = "1";

    public static String LOG_TAG = "MainActivityFragment";

    ArrayList<String> favoriteMovieIDs;
    String delimiter = ";;";
    Boolean favoriteActive = false;
    String favouriteSavedList;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Movie selectedMovieObject);
    }


    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), PreferencesActivity.class);
            startActivity(intent);
            return true;
        }

        //show favorite list
        if (id == R.id.action_favorite) {
            //toggle favorite movies mode
            if (favoriteActive) {
                favoriteActive = false;
                UpdatePosters();
            } else {
                ShowFavoriteMovieList();
            }
//            Intent intent = new Intent(getActivity(), FavoriteActivity.class);
//            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mPostersGridView = (GridView) rootView.findViewById(R.id.grid_view);
        moviesPosters = new ArrayList<>();
        mainScreenGridViewAdapter = new MainScreenGridViewAdapter(getActivity(), moviesPosters);

        mPostersGridView.setAdapter(mainScreenGridViewAdapter);
        movies = new ArrayList<>();

//        Log.d("prefChangedOn Create", "" + prefChanged);

        if (savedInstanceState != null) {
            favoriteActive = savedInstanceState.getBoolean("favoriteActive");
            moviesPosters = new ArrayList<>();
            moviesPosters = savedInstanceState.getStringArrayList("moviesPosters");
            favouriteSavedList = savedInstanceState.getString("favouriteSavedList");

            if (moviesPosters != null) {
                mainScreenGridViewAdapter.addAll(moviesPosters);
                movies = new ArrayList<>();
                movies = savedInstanceState.getParcelableArrayList("movies");
                sortingOrder = savedInstanceState.getString("sortingOrder");
                Log.d("retrievingsavedstate", movies.get(0).getOriginalTitle());

                mPostersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        // parceable movie will be sent to the detail activity
                        Movie movie = movies.get(position);
                        ((Callback) getActivity())
                                .onItemSelected(movie);

                    }
                });
            } else {
                //filter for favorite movies if favorite mode was active before device rotation
                if (favoriteActive) {
                    ShowFavoriteMovieList();
                } else {
                    UpdatePosters();
                }
            }

        } else {
            // Get posters to use when orientation changes
            //filter for favorite movies if favorite mode was active before device rotation
            if (favoriteActive) {
                ShowFavoriteMovieList();
            } else {
                UpdatePosters();
            }


        }


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Test Network
        if (!Global.getNetworkState(getActivity().getApplicationContext())) {
            Log.e(LOG_TAG, "Network is not available");

            CharSequence text = getString(R.string.network_not_available_message);
            Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, Toast.LENGTH_LONG);
            toast.show();
        } else if (!favoriteActive) {
            String newSortingOrder = getPreferredSortingOrder();

            if (newSortingOrder != null && !newSortingOrder.equals(sortingOrder)) {
                Log.d(LOG_TAG, "updating movies via API call");
                sortingOrder = newSortingOrder;

                UpdatePosters();

            }

        } else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


            String favouriteUpdatedList = prefs.getString(getString(R.string.pref_favorite_key), "");
            if (!favouriteUpdatedList.equals(favouriteSavedList)) {
                Log.d("favouriteUpdatedList", favouriteUpdatedList + " " + favouriteSavedList);

                ShowFavoriteMovieList();
//                favouriteSavedList = favouriteUpdatedList;

            }

        }
    }


    public String getPreferredSortingOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_default));
    }


    private void UpdatePosters() {

        movies = new ArrayList<>();

        // Get saved sort pref
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_pref = prefs.getString("sort_pref_list", "1");
        String sort_query; // default sort order
        if (sort_pref.equals("0")) {
            sort_query = "vote_average.desc";
        } else {
            sort_query = "popularity.desc";
        }
        sortingOrder = sort_pref;
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();

        fetchMoviesTask.execute(sort_query);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("moviesPosters", moviesPosters);
        outState.putParcelableArrayList("movies", movies);
        outState.putString("sortingOrder", sortingOrder);
        outState.putBoolean("favoriteActive", favoriteActive);
        outState.putString("favouriteSavedList", favouriteSavedList);

    }

    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<String>> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

//        private ProgressDialog dialog;

        public FetchMoviesTask() {
            if (dialog == null)
                dialog = new ProgressDialog(getActivity());
        }

        @Override
        protected void onPreExecute() {
            if (!dialog.isShowing()) {
                dialog.setMessage("Retrieving movies, please wait.");
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
        private ArrayList<String> getMovieDataFromJson(String playlistCountJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String results = "results";

            // poster url pieces
            final String baseURL = "http://image.tmdb.org/t/p/w185";


            moviesPosters = new ArrayList<>();

            JSONObject resultJson = new JSONObject(playlistCountJsonStr);

            JSONArray itemsArray = resultJson.getJSONArray(results);

            for (int i = 0; i < itemsArray.length(); i++) {
                // add thumbnails to arraylist
                JSONObject movieJson = (JSONObject) itemsArray.get(i);
                String poster_path = baseURL + movieJson.get("poster_path").toString();

                String originalTitle = movieJson.get("original_title").toString();
                String plotSynopsis = movieJson.get("overview").toString();
                String userRating = movieJson.get("vote_average").toString();
                String releaseDate = movieJson.get("release_date").toString();
                String id = movieJson.get("id").toString();

                moviesPosters.add(poster_path);
                Movie movie = new Movie(poster_path, originalTitle, plotSynopsis, userRating, releaseDate, id);
                movies.add(movie);
            }


            return moviesPosters;

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
                final String SEARCH_BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";

                final String sort_by = params[0]; //vote_average.desc or popularity.desc
                final String api_key = Global.themoviedbAPIKEY;


                Uri builtUri = Uri.parse(SEARCH_BASE_URL).buildUpon()
                        .appendQueryParameter("sort_by", sort_by)
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
                Log.d(LOG_TAG, moviesJsonStr);

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

                return getMovieDataFromJson(moviesJsonStr);
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

                Log.d(LOG_TAG, "" + result);

                mainScreenGridViewAdapter.clear();

                mainScreenGridViewAdapter.addAll(result);

                mainScreenGridViewAdapter = new MainScreenGridViewAdapter(getActivity(), moviesPosters);

                mPostersGridView.setAdapter(mainScreenGridViewAdapter);
                // saving fetched poster urls for when orientation change ...
                moviesPosters = new ArrayList<>();
                moviesPosters.addAll(result);

                // parceable movie will be sent to the detail activity
                Movie movie = movies.get(0);
                if(MainActivity.mTwoPane)
                    ((Callback) getActivity()).onItemSelected(movie);

                // Clicking the poster will show detail view of the movie

                mPostersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        // parceable movie will be sent to the detail activity
                        Movie movie = movies.get(position);
                        ((Callback) getActivity())
                                .onItemSelected(movie);

                    }
                });

                Log.d(LOG_TAG, "" + mainScreenGridViewAdapter.getCount());

            }

            if (dialog.isShowing() && mainScreenGridViewAdapter.getCount() > 0) {
                dialog.dismiss();
            }
        }
    }


    void ShowFavoriteMovieList() {
        movies = new ArrayList<>();

        Log.d("GetFavoriteMovieList", "mm");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());


        favouriteSavedList = prefs.getString(getString(R.string.pref_favorite_key), "");
        Log.d(LOG_TAG, "" + favouriteSavedList);

        if (!favouriteSavedList.equals("")) {
            favoriteMovieIDs = new ArrayList<>();
            moviesPosters = new ArrayList<>();

            for (int i = 0; i < favouriteSavedList.split(";").length; i++) {
                String movieID = favouriteSavedList.split(";")[i];
                favoriteMovieIDs.add(movieID);
                // compose the movie objects
                String movieDetail = prefs.getString(movieID, "");
                moviesPosters.add(movieDetail.split(delimiter)[0]);
                Movie movie = new Movie(movieDetail.split(delimiter)[0], movieDetail.split(delimiter)[1], movieDetail.split(delimiter)[2]
                        , movieDetail.split(delimiter)[3], movieDetail.split(delimiter)[4], movieID);
                movies.add(movie);
            }

            mainScreenGridViewAdapter = new MainScreenGridViewAdapter(getActivity(), moviesPosters);

            mPostersGridView.setAdapter(mainScreenGridViewAdapter);

            Movie movie = movies.get(0);

            if(MainActivity.mTwoPane)
            ((Callback) getActivity()).onItemSelected(movie);

            Log.d(LOG_TAG, "" + mainScreenGridViewAdapter.getCount());

            mPostersGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // parceable movie will be sent to the detail activity
                    Movie movie = movies.get(position);
                    ((Callback) getActivity())
                            .onItemSelected(movie);

                }
            });

        }
        favoriteActive = true;

    }
}
