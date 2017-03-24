package com.example.android.yrestaurants;

import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;
import android.Manifest;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private static final int SIGN_IN_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST = 2;
    private DatabaseReference userDBRef;
    private String uid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content of the activity to use the activity_main.xml layout file
        setContentView(R.layout.activity_main);

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
            displayFragments();
        }

        // Request the location services permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);

        }

        Log.v(TAG, "*** onCreate() ***");
    }

    // public getter methods for the fragments
    public String getUid(){
        return uid;
    }

    private void displayFragments(){
        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        SimpleFPAdapter adapter = new SimpleFPAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Find the tab layout that shows the tabs
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        // Connect the tab layout with the view pager. This will
        //   1. Update the tab layout when the view pager is swiped
        //   2. Update the view pager when a tab is selected
        //   3. Set the tab layout's tab names with the view pager's adapter's titles
        //      by calling onPageTitle()
        tabLayout.setupWithViewPager(viewPager);
    }

    private void startSignInOrUpActivity(){
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setProviders(Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build()))
                .setIsSmartLockEnabled(false)
                .build(),
            SIGN_IN_REQUEST_CODE
        );

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

                displayFragments();
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
                    // permission was granted,

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
