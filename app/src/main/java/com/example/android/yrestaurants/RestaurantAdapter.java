package com.example.android.yrestaurants;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.util.ArrayList;

/**
 *  ArrayAdapter for RestaurantsFragment.java
 */

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private Context context;
    private String uid;

    /**
     *
     * @param context is the current context that the adapter is being created in.
     * @param uid is the current user's id for a reference in Firebase database
     * @param restaurants is the list of restaurants to be displayed
     */

     public RestaurantAdapter(Context context, String uid, ArrayList<Restaurant> restaurants) {
        super(context, 0, restaurants);
        this.context = context;
        this.uid = uid;
        this.imageLoader = ImageLoader.getInstance();

        // Set options in Universal Image Loader for loaded images to be cached in memory and/or on disk
        this.options = new DisplayImageOptions.Builder()
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

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the Restaurant object located at this position in the list
        final Restaurant currRest = getItem(position);

        // Find the 'name' TextView in the list_item.xml layout.
        TextView nameTV = (TextView) listItemView.findViewById(R.id.name_list_item);

        // Get the name of restaurant from the currentRest object
        nameTV.setText(currRest.getNameOfRestaurant());

        // Find the ImageView in the list_item.xml layout with the ID image.
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.image_list_item);

        // Load image, decode it to Bitmap and display Bitmap in ImageView
        imageLoader.displayImage(currRest.getImageUrl(), imageView, options);

        // Find the 'rating' RatingBar in the list_item.xml layout and set the rating
        RatingBar ratingBar = (RatingBar) listItemView.findViewById(R.id.rating_list_item);
        ratingBar.setRating(currRest.getRating());

        // Find the 'reviewCount' TextView in the list_item.xml layout and set the review counts
        TextView reviewCountTV = (TextView) listItemView.findViewById(R.id.review_count_list_item);
        String reviewCountStr = " " + currRest.getReviewCount()+ " reviews";
        reviewCountTV.setText(reviewCountStr);

        // Find the 'category' TextView in the list_item.xml layout and set the category
        TextView categoryTV = (TextView) listItemView.findViewById(R.id.category_list_item);
        categoryTV.setText(currRest.getCategory());

        TextView distanceTV = (TextView) listItemView.findViewById(R.id.distance_list_item);
        String distanceStr = Double.toString(currRest.getDistance()/1000.0) + "km away";
        distanceTV.setText(distanceStr);

        // Return a list item layout
        return listItemView;
    }



}
