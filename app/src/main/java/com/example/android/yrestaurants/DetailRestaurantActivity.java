package com.example.android.yrestaurants;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.Manifest;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.HashMap;

public class DetailRestaurantActivity extends AppCompatActivity {

    private final String TAG = "DetailRestaurantActivit"; // maximum 23 characters allowed for TAG
    private final static int PHONE_PERMISSION_REQUEST = 1;
    private DatabaseReference userDBRef;
    //     <restaurant ID , key which is a reference for Firebase database>
    private HashMap<String, String> ufavourRestIdAndKey = new HashMap<String, String>();
    private CheckBox favouriteCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_restaurant);

        // Request the location services permission if it hasn't been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PHONE_PERMISSION_REQUEST);

        } else { // if the permission is granted already

        }

        ImageLoader imageLoader = ImageLoader.getInstance();

        // Set options in Universal Image Loader for loaded images to be cached in memory and/or on disk
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        // Get the current restaurant object
        final Restaurant currRest = getIntent().getExtras().getParcelable("restaurant");

        // Initialize user interfaces
        TextView nameTV = (TextView) findViewById(R.id.name_detail);
        nameTV.setText(currRest.getNameOfRestaurant());

        TextView distanceTV = (TextView) findViewById(R.id.distance_detail);
        String distanceStr = Double.toString(currRest.getDistance()/1000.0) + "km away";
        distanceTV.setText(distanceStr);

        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_detail);
        ratingBar.setRating(currRest.getRating());

        TextView reviewCountTV = (TextView) findViewById(R.id.review_count_detail);
        String reviewCountStr = " " + currRest.getReviewCount()+ " reviews";
        reviewCountTV.setText(reviewCountStr);

        TextView categoryTV = (TextView) findViewById(R.id.category_detail);
        categoryTV.setText(currRest.getCategory());

        ImageView imageView = (ImageView) findViewById(R.id.image_detail);
        // Load image, decode it to Bitmap and display Bitmap in ImageView
        imageLoader.displayImage(currRest.getImageUrl(), imageView, options);

        TextView phoneNumberTV = (TextView) findViewById(R.id.phone_number_detail);
        phoneNumberTV.setText(currRest.getPhone());

        LinearLayout callLayout = (LinearLayout) findViewById(R.id.call_detail);
        callLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + currRest.getPhone()));

                if (ContextCompat.checkSelfPermission(DetailRestaurantActivity.this, Manifest.permission.CALL_PHONE)
                        == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                }


            }
        });

        // Get the current user's uid
        String uid = getIntent().getExtras().getString("uid");

        // Get the Firebase database reference of the current user
        userDBRef = FirebaseDatabase.getInstance()
                .getReference(uid);

        // Initialize the star shape checkbox and attach a listener
        favouriteCB = (CheckBox) findViewById(R.id.favourtie_detail);
        favouriteCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add this restaurant as a favourite in Firebase database
                if(((CheckBox) view).isChecked()){
                    userDBRef.push().setValue(currRest);
                }
                // Remove this restaurant from the database by getting the key reference from the HashMap
                else{
                    // ufavourRestIdAndKey is already set by userDBRef.addValueEventListener
                    String currRestKey = ufavourRestIdAndKey.get(currRest.getYId());
                    userDBRef.child(currRestKey).removeValue();
                }
            }
        });






        // Attaching a ValueEventListener to a list of data will return
        // the entire list of data as a single DataSnapshot
        userDBRef.addValueEventListener(new ValueEventListener() {
            // Going to update the HashMap(ufavourRestIdAndKey) every time the Firebase database changes
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the hashMap first
                ufavourRestIdAndKey.clear();

                // Loop over to individual children and store restaurant ID and Key pairs in the HashMap.
                for (DataSnapshot eachDataSnapShot: dataSnapshot.getChildren()) {
                    Restaurant restaurant = eachDataSnapShot.getValue(Restaurant.class);
                    ufavourRestIdAndKey.put(restaurant.getYId(), eachDataSnapShot.getKey());
                }

                // Set the state of the CheckBox based on the current user's favourites stored in Firebase
                favouriteCB.setChecked(ufavourRestIdAndKey.containsKey(currRest.getYId()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "userDBRef.addValueEventListener failed. The detail is "+ databaseError.getDetails());
            }
        });

    }
}
