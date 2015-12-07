package com.softups.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{

    public static boolean mTwoPane;

    String DETAILFRAGMENT_TAG = "DETAILFRAGMENT_TAG";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.movie_detail_container) != null){
            // we are in the two pane mode (tablet)
            mTwoPane = true;


//            if(savedInstanceState == null){
//
//                    getSupportFragmentManager().beginTransaction().add(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
//                            .commit();
//
//            }
        } else {
            mTwoPane = false;
        }
    }

    public boolean getTwoPane(){
        return mTwoPane;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        //show favorite list
//        if (id == R.id.action_favorite) {
//            //filter for favorite movies only
//
//            Intent intent = new Intent(this, FavoriteActivity.class);
//            startActivity(intent);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onItemSelected(Movie selectedMovieObject) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(MovieDetailFragment.DETAIL_MOVIE_KEY, selectedMovieObject);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {


            // parceable movie will be sent to the detail activity


            Intent intent = new Intent(this, MovieDetail.class)
                    .putExtra("moviedetail", selectedMovieObject);


            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Log.d("selectedmovie", selectedMovieObject.getOriginalTitle());

            startActivity(intent);

        }
    }
}
