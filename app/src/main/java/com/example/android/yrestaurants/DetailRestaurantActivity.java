package com.example.android.yrestaurants;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.utils.L;

import java.util.HashMap;
import java.util.HashSet;

public class DetailRestaurantActivity extends AppCompatActivity {

    private final String TAG = "DetailRestaurantActivit"; // maximum 23 characters allowed for TAG
    private DatabaseReference userDBRef;
    private String uid;
    private Restaurant currRest;

    //     <restaurant ID , key which is a reference for Firebase database>
    private HashMap<String, String> ufavourRestId = new HashMap<String, String>();
    private CheckBox favouriteCB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_restaurant);

        // Get the current restaurant object
        currRest = getIntent().getExtras().getParcelable("restaurant");

        // Get the current user's uid
        uid = getIntent().getExtras().getString("uid");

        // Get the Firebase database reference of the current user
        userDBRef = FirebaseDatabase.getInstance()
                .getReference(uid);

        favouriteCB = (CheckBox) findViewById(R.id.favourtie_detail);
        favouriteCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Add this restaurant as a favourite in Firebase database
                if(((CheckBox) view).isChecked()){
                    userDBRef.push().setValue(currRest);
                }
                // Remove the favourite from the database
                else{
                    // ufavourRestId is already set by userDBRef.addValueEventListener
                    String currRestKey = ufavourRestId.get(currRest.getId());
                    userDBRef.child(currRestKey).removeValue();
                }
            }
        });

        TextView nameTV = (TextView) findViewById(R.id.name_detail);
        nameTV.setText(currRest.getNameOfRestaurant());

        TextView uidTV = (TextView) findViewById(R.id.uid_detail);
        uidTV.setText(uid);

        // Attaching a ValueEventListener to a list of data will return
        // the entire list of data as a single DataSnapshot
        userDBRef.addValueEventListener(new ValueEventListener() {
            // Going to update the HashMap(ufavourRestId) every time the Firebase database changes
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the hashMap first
                ufavourRestId.clear();

                // Loop over to individual children and store restaurant ID and Key pairs in the HashMap.
                for (DataSnapshot eachDataSnapShot: dataSnapshot.getChildren()) {
                    Restaurant restaurant = eachDataSnapShot.getValue(Restaurant.class);
                    ufavourRestId.put(restaurant.getId(), eachDataSnapShot.getKey());
                }

                // Set the state of the CheckBox based on the current user's favourites stored in Firebase
                favouriteCB.setChecked(ufavourRestId.containsKey(currRest.getId()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "userDBRef.addValueEventListener failed. The detail is "+ databaseError.getDetails());
            }
        });

    }
}
