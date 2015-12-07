package com.softups.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewsActivityFragment extends Fragment {

    View rootView;

    ArrayList<String> savedMovieReviews;
    ReviewsListViewAdapter reviewsListViewAdapter;

    ListView reviewsListView;

    public ReviewsActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_reviews, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            savedMovieReviews = new ArrayList<String>();
            savedMovieReviews = intent.getStringArrayListExtra(Intent.EXTRA_TEXT);

            reviewsListViewAdapter = new ReviewsListViewAdapter(getActivity(),savedMovieReviews);
            reviewsListView = (ListView) rootView.findViewById(R.id.reviews_list_view);

            reviewsListView.setAdapter(reviewsListViewAdapter);

            if(savedInstanceState == null) {
//                getTrailers(selectedMovie.getId());
            }else {
//                savedMovietrailers = new ArrayList<>();
//                savedMovietrailers = savedInstanceState.getStringArrayList("savedMovietrailers");
//                if (savedMovietrailers != null) {
//                    trailersListViewAdapter.addAll(savedMovietrailers);
//                    setListViewHeightBasedOnChildren(trailersListView);
//                } else {
//                    getTrailers(selectedMovie.getId());
//                }
            }

        }

        return rootView;
    }
}
