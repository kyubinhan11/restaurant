package com.example.android.yrestaurants;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

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
    private DatabaseReference userDBRef;
    //     <restaurant ID , key which is a reference for Firebase database>
    private HashMap<String, String> ufavourRestIdAndKey = new HashMap<String, String>();
    private CheckBox favouriteCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_restaurant);

        ImageLoader imageLoader = ImageLoader.getInstance();

        // Set options in Universal Image Loader for loaded images to be cached in memory and/or on disk
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .postProcessor(new BitmapProcessor() {
                    // set the size of images to make them fit nicely in the list view
                    @Override
                    public Bitmap process(Bitmap bmp) {
                        return Bitmap.createScaledBitmap(bmp, 500, 500, false);
                    }
                })
                .build();

        // Get the current restaurant object
        final Restaurant currRest = getIntent().getExtras().getParcelable("restaurant");

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

        // Placeholders
        TextView nameTV = (TextView) findViewById(R.id.name_detail);
        nameTV.setText(currRest.getNameOfRestaurant());

        TextView uidTV = (TextView) findViewById(R.id.uid_detail);
        uidTV.setText(currRest.getPhone());


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
