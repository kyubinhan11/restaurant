package com.example.android.yrestaurants;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantsFragment extends Fragment{

    private static String TAG = "RestaurantFragment";

    public RestaurantsFragment() {
        // Required empty public constructor
    }

    private ListView listView;
    private RestaurantAdapter adapter;
    private RestaurantAsyncTask task;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.restaurant_list, container, false);

        // initialize ImageLoader to use Universal Image Loader in RestaurantAdapter
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));

        // Create an RestaurantAdapter, whose data source is a list of restaurants. The
        // adapter knows how to create list items for each item in the list.
        adapter = new RestaurantAdapter(getActivity(), new ArrayList<Restaurant>());

        // Find the ListView object in the view hierarchy of the link Activity.
        // the Listview is declared in the restaurant_list.xml layout file.
        listView = (ListView) rootView.findViewById(R.id.list);

        listView.setAdapter(adapter);

        // Start the AsyncTask to fetch the restaurant data
        task = new RestaurantAsyncTask();
//        String stringUrl = searchUrl + "?latitude="
//                + Double.toString(lat) + "&longitude=" + Double.toString(lon) + "&radius=" + Integer.toString(radius);

        task.execute("https://api.yelp.com/v3/businesses/search?latitude=49.24&longitude=-122.3&radius=10000");

        Log.v(TAG, "*** onCreateView() ***");


        return rootView;
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "*** onStop() ***");
    }
                                                    // Params, Progress, Result
    private class RestaurantAsyncTask extends AsyncTask<String, Void, List<Restaurant>> {
        @Override
        protected List<Restaurant> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            List<Restaurant> result = QueryUtils.fetchRestaurantData(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<Restaurant> data) {
            // Clear the adapter of previous restaurant data
            adapter.clear();

            // If there is a valid list of {@link restaurant}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (data != null && !data.isEmpty()) {
                adapter.addAll(data);
            }

        }
    }


}
