package com.example.android.yrestaurants;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestaurantsFragment extends Fragment {


    public RestaurantsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.restaurant_list, container, false);

        ArrayList<Restaurant> restaurants = new ArrayList<Restaurant>();
        restaurants.add(new Restaurant("http://s3-media2.fl.yelpcdn.com/bphoto/MmgtASP3l_t4tPCL1iAsCg/o.jpg"
                , "Four Barrel Coffee"));

        // Create an RestaurantAdapter, whose data source is a list of restaurants. The
        // adapter knows how to create list items for each item in the list.
        RestaurantAdapter adapter = new RestaurantAdapter(getActivity(), restaurants);

        // Find the ListView object in the view hierarchy of the link Activity.
        // the Listview is declared in the restaurant_list.xml layout file.
        ListView listView = (ListView) rootView.findViewById(R.id.list);

        listView.setAdapter(adapter);

        return rootView;
    }

}
