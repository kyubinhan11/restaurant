package com.example.android.yrestaurants;


import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantsFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private final static String TAG = "RestaurantFragment";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    public RestaurantsFragment() {
        // Required empty public constructor
    }

    private RestaurantAdapter adapter;
    private Location location;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.restaurant_list, container, false);

        // initialize ImageLoader to use Universal Image Loader in RestaurantAdapter
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Create an RestaurantAdapter, whose data source is a list of restaurants. The
        // adapter knows how to create list items for each item in the list.
        // the list will be filled after the user's location is obtained by Google Play Services (look at onConnected)
        adapter = new RestaurantAdapter(getActivity(), new ArrayList<Restaurant>());

        // Find the ListView object in the view hierarchy of the link Activity.
        // the Listview is declared in the restaurant_list.xml layout file.
        ListView RestaurantsListView = (ListView) rootView.findViewById(R.id.list);

        RestaurantsListView.setAdapter(adapter);

        Log.v(TAG, "*** onCreateView() ***");

        return rootView;
    }

    /* the required methods for GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        // Beginning in Android 6.0 (API level 23), users grant permissions to apps
        // while the app is running, not when they install the app.
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (location != null) {
            // update restauraut feeds every time the application resumes
            RestaurantAsyncTask task = new RestaurantAsyncTask();
            // Start an AsyncTask to fetch restaurant feeds
            task.execute("https://api.yelp.com/v3/businesses/search?latitude="+ location.getLatitude()+
                    "&longitude=" + location.getLongitude() + "&radius=10000");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }
    /* end of required methods for GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/

    @Override
    public void onResume() {
        Log.v(TAG, "*** onResume() ***");
        mGoogleApiClient.connect();
        super.onResume();
    }

    @Override
    public void onStop() {
        Log.v(TAG, "*** onStop() ***");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    private class RestaurantAsyncTask extends AsyncTask<String, Void, List<Restaurant>> {
        @Override                                  // Params, Progress, Result
        protected List<Restaurant> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            // get the restaurant feeds from Yelp API
            List<Restaurant> result = QueryUtils.fetchRestaurantData(urls[0]);
            return result;
        }

        @Override
        protected void onPostExecute(List<Restaurant> restaurants) {
            // Clear the adapter of previous restaurant data
            adapter.clear();

            // If there is a valid list of {@link restaurant}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (restaurants != null && !restaurants.isEmpty()) {
                adapter.addAll(restaurants);
            }

        }
    }


}
