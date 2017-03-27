package com.example.android.yrestaurants;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class FavouritesFragment extends Fragment {

    private static final String TAG = "FavouritesFragment";
    private String uId;
    private FirebaseListAdapter<Restaurant> adapter;
    private DatabaseReference userDBRef;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    //     <restaurant ID , key which is a reference for Firebase database>
    private HashMap<String, String> ufavourRestIdAndKey = new HashMap<String, String>();

    public FavouritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.restaurant_list, container, false);

        // Get an instance of ImageLoader
        imageLoader = ImageLoader.getInstance();

        // Set options in Universal Image Loader for loaded images to be cached in memory and/or on disk
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .postProcessor(new BitmapProcessor() {
                    // set the size of images to make them fit nicely in the list view
                    @Override
                    public Bitmap process(Bitmap bmp) {
                        return Bitmap.createScaledBitmap(bmp, 350, 300, false);
                    }
                })
                .build();

        // Get the current user id from the mainActivity
        uId = ((MainActivity) getActivity()).getUid();

        // Get a database reference based on the user id
        userDBRef = FirebaseDatabase.getInstance()
                .getReference(uId);

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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "userDBRef.addValueEventListener failed. The detail is " + databaseError.getDetails());
            }
        });

        // use the same ListView as the RestaurantsFragment
        ListView RestaurantsListView = (ListView) rootView.findViewById(R.id.list);
        adapter = new FirebaseListAdapter<Restaurant>(getActivity(), Restaurant.class,
                R.layout.list_item, userDBRef) {
            @Override
            protected void populateView(View v, Restaurant model, int position) {
                // Find the 'name' TextView in the list_item.xml layout.
                final Restaurant currRest = model;

                TextView nameTV = (TextView) v.findViewById(R.id.name_list_item);

                // Get the name of restaurant from the currentRest object
                nameTV.setText(currRest.getNameOfRestaurant());

                // Find the ImageView in the list_item.xml layout with the ID image.
                ImageView imageView = (ImageView) v.findViewById(R.id.image_list_item);

                // Load image, decode it to Bitmap and display Bitmap in ImageView
                imageLoader.displayImage(currRest.getImageUrl(), imageView, options);

                // Find the 'rating' RatingBar in the list_item.xml layout and set the rating
                RatingBar ratingBar = (RatingBar) v.findViewById(R.id.rating_list_item);
                ratingBar.setRating(currRest.getRating());

                // Find the 'reviewCount' TextView in the list_item.xml layout and set the review counts
                TextView reviewCountTV = (TextView) v.findViewById(R.id.review_count_list_item);
                String reviewCountStr = " " + currRest.getReviewCount()+ " reviews";
                reviewCountTV.setText(reviewCountStr);

                // Find the 'category' TextView in the list_item.xml layout and set the category
                TextView categoryTV = (TextView) v.findViewById(R.id.category_list_item);
                categoryTV.setText(currRest.getCategory());

                TextView distanceTV = (TextView) v.findViewById(R.id.distance_list_item);
                String distanceStr = Double.toString(currRest.getDistance()/1000.0) + "km away";
                distanceTV.setText(distanceStr);

                // Find the 'favourite' CheckBox in the list_item.xml layout
                final CheckBox favouriteCB = (CheckBox) v.findViewById(R.id.favourtie_list_item);

                // Make sure the checkbox is visible in this Fragment
                favouriteCB.setVisibility(View.VISIBLE);

                // Attach a listener for users to remove this restaurant from the database
                favouriteCB.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    // ufavourRestIdAndKey is already set by userDBRef.addValueEventListener
                    String currRestKey = ufavourRestIdAndKey.get(currRest.getYId());
                    userDBRef.child(currRestKey).removeValue();
                    // Always set it as checked so that the new list item will have a coloured checkBox.
                    favouriteCB.setChecked(true);
                    }
                });
            }
        };
        RestaurantsListView.setAdapter(adapter);

        return rootView;
    }

}
