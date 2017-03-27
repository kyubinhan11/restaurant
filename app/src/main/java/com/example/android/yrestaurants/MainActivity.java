package com.example.android.yrestaurants;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int SIGN_IN_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST = 2;
    private DatabaseReference userDBRef;
    private String uid;
    private ViewPager restaurantViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                                getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection
        if(networkInfo != null && networkInfo.isConnected()){

            // Request the location services permission if it hasn't been granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);

            } else { // if the permission is granted already
                startAuthentication();
            }

        } else {
            // Otherwise, display error
            Log.v(TAG, "no internet connection!");
            TextView noInternetTV = (TextView) findViewById(R.id.no_internet);
            String str = "No internet connection.";
            noInternetTV.setText(str);
        }



        Log.v(TAG, "*** onCreate() ***");
    }




    /* public getter methods for the fragments */
    public String getUid(){
        return uid;
    }



    /* private methods */
    private void startAuthentication(){
        // If the user hasn't signed in
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startSignInOrUpActivity();
        } else {
            // User is already signed in. Therefore, display a welcome Toast
            Toast.makeText(this,
                    "Welcome " + FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getDisplayName() +"!",
                    Toast.LENGTH_LONG)
                    .show();

            uid = FirebaseAuth.getInstance()
                    .getCurrentUser()
                    .getUid();

            // Create or refer to each user's firebase database reference
            userDBRef = FirebaseDatabase.getInstance()
                    .getReference(uid);

            // Load restaurants contents
            initializeFragments();
        }


    }

    private void startSignInOrUpActivity(){
//        startActivityForResult(
//                AuthUI.getInstance()
//                        .createSignInIntentBuilder()
//                        .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
//                                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
//                                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
//                        .setIsSmartLockEnabled(false)
//                        .build(),
//                SIGN_IN_REQUEST_CODE
//        );

        Intent intent = new Intent(this, SignInActivity.class);
        startActivityForResult(intent, SIGN_IN_REQUEST_CODE);

    }

    private void initializeFragments(){
        // Find the view pager that will allow the user to swipe between fragments
        restaurantViewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        SimpleFPAdapter adapter = new SimpleFPAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        restaurantViewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(restaurantViewPager);
    }

    private void isCheckLocationServiceIsOn (final Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        }
        catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            Log.e(TAG, "no location services");
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage("Location Services Disabled. \n Please enable location services.");

            dialog.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });

            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {


                }
            });
            dialog.show();

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SIGN_IN_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG).show();

                // Update user's reference to Firebase
                uid = FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

                // Create or refer to each user's firebase database reference
                userDBRef = FirebaseDatabase.getInstance()
                        .getReference(uid);

                initializeFragments();
            } else {
                Toast.makeText(this, "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG).show();

                // Close the app
                finish();
            }
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    startAuthentication();

                } else {
                    // permission denied. We can't get any restaurats feed back without the location value
                    // so let's just finish the app.
                    Toast.makeText(MainActivity.this,
                            "Sorry, the app requires the location permission", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "*** onStop() ***");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "*** onDestroy() ***");
    }

    // Instantiate the menu resource
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Handle click events on the menu item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this,
                                "You have been signed out.",
                                Toast.LENGTH_LONG)
                                .show();

                        // Start sign in/sign up activity again
                        startSignInOrUpActivity();
                    }
                });
        }
        return true;
    }
}
