package com.example.android.yrestaurants;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantsFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static String TAG = "RestaurantFragment";
    private final static String SEARCH_PATH = "https://api.yelp.com/v3/businesses/search";
    private final static String TOKEN_PATH = "https://api.yelp.com/oauth2/token";
    private final static String RADIUS = "10000";
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private int offset = 0;
    private String bearerToken;
    private RestaurantAdapter adapter;
    private Location location;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;


    public RestaurantsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.restaurant_list, container, false);


        // Initialize ImageLoader to use Universal Image Loader in RestaurantAdapter
        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(getActivity()));

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = LocationRequest.create()
                .setInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Create an RestaurantAdapter, whose data source is a list of restaurants. The
        // adapter knows how to create list items for each item in the list.
        // the list will be filled after the user's location is obtained by Google Play Services (look at onConnected)
        adapter = new RestaurantAdapter(getActivity(), ((MainActivity) getActivity()).getUid() ,new ArrayList<Restaurant>());

        // Find the ListView object in the view hierarchy of the link Activity.
        // the Listview is declared in the restaurant_list.xml layout file.
        ListView RestaurantsListView = (ListView) rootView.findViewById(R.id.list);

        // Set a scroll listener for an infinite scrolling list
        RestaurantsListView.setOnScrollListener(new InfiniteScrollListener(5) {
            @Override
            public void loadMore(int page, int totalItemsCount) {
                offset += 20;

                // Fetch another batch of restaurant feeds by adding 'offset' parameter
                RestaurantAsyncTask task = new RestaurantAsyncTask();
                task.execute(SEARCH_PATH + "?latitude=" + location.getLatitude() +
                                "&longitude=" + location.getLongitude() + "&radius=" + RADIUS +
                                "&offset=" + Integer.toString(offset),
                        bearerToken);
                // append those new feeds in the restaurantAdapter
                adapter.notifyDataSetChanged();
            }
        });
        RestaurantsListView.setAdapter(adapter);

        Log.v(TAG, "*** onCreateView() ***");

        return rootView;
    }

    /* The required methods for GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        // Checking permissions at runtime
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Get the location using Google Play Services
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if(location != null) {
            if (adapter.isEmpty()) {
                // First, send a POST request to get BearerToken from Yelp
                // then get restaurant feeds using their Search API with the token
                BearerTokenAsyncTask task = new BearerTokenAsyncTask();
                task.execute(TOKEN_PATH);
            }
        } else {
            // So location found in the history which is pretty rare (according to the documentation)
            // so send a location update request
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    // This is callback method from LocationServices.FusedLocationApi.requestLocationUpdates
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location has changed");
        this.location = location;

        // Run AsyncTask one more time to get restaurant feeds after receiving the location
        BearerTokenAsyncTask task = new BearerTokenAsyncTask();
        task.execute(TOKEN_PATH);
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
    /* End of required methods for GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/

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


    private abstract class InfiniteScrollListener implements AbsListView.OnScrollListener {
        private int bufferItemCount = 10;
        private int currentPage = 0;
        private int itemCount = 0;
        private boolean isLoading = true;

        public InfiniteScrollListener(int bufferItemCount) {
            this.bufferItemCount = bufferItemCount;
        }

        public abstract void loadMore(int page, int totalItemsCount);

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            // Do Nothing
        }

        // The onScroll() method is automatically called by the Android runtime every time a user scrolls.
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
            if (totalItemCount < itemCount) {
                this.itemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.isLoading = true; }
            }

            if (isLoading && (totalItemCount > itemCount)) {
                isLoading = false;
                itemCount = totalItemCount;
                currentPage++;
            }

            if (!isLoading && (totalItemCount - visibleItemCount)<=(firstVisibleItem + bufferItemCount)) {
                loadMore(currentPage + 1, totalItemCount);
                isLoading = true;
            }
        }
    }

    // A AsyncTask responsible for receiving the Bearer token from Yelp API (POST Request)
    // This is 2 layers of AsyncTask: 1. Get the token 2. Get the restaurant feeds
    private class BearerTokenAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),
                    "", "Loading...");
        }

        @Override
        protected String doInBackground(String... strings) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (strings.length < 1 || strings[0] == null) {
                return null;
            }
            bearerToken = QueryUtils.getBearerToken(strings[0]);
            // return the token to onPostExecute
            return bearerToken;

        }

        // After the token is received send a GET request to Yelp to get restaurant feeds
        @Override
        protected void onPostExecute(String bearerToken) {
            progressDialog.dismiss();
            RestaurantAsyncTask task = new RestaurantAsyncTask();
            // Start an AsyncTask to fetch restaurant feeds
            task.execute(SEARCH_PATH + "?latitude=" + location.getLatitude() +
                    "&longitude=" + location.getLongitude() + "&radius=" + RADIUS,
                    bearerToken);
        }
    }

    // A AsyncTask responsible for receiving restaurant feeds from Yelp API (GET Request)
    private class RestaurantAsyncTask extends AsyncTask<String, Void, List<Restaurant>> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(),
                    "", "Loading...");
        }

        @Override                                  // Params, Progress, Result
        protected List<Restaurant> doInBackground(String... strings) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (strings.length < 1 || strings[0] == null) {
                return null;
            }

            // Get the restaurant feeds from Yelp API
            List<Restaurant> result = QueryUtils.fetchRestaurantData(strings[0], strings[1]);

            return result;
        }

        @Override
        protected void onPostExecute(List<Restaurant> restaurants) {
            progressDialog.dismiss();

            // If there is a valid list of {@link restaurant}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (restaurants != null && !restaurants.isEmpty()) {
                adapter.addAll(restaurants);
            }

        }
    }



}
