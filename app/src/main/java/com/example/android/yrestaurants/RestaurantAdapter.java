package com.example.android.yrestaurants;

import android.content.Context;
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

import java.util.ArrayList;

/**
 * Created by Kevin on 3/13/2017.
 */

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    private ImageLoader imageLoader;
    private DisplayImageOptions options;

    /**
     *
     * @param context is the current context that the adapter is being created in.
     * @param restaurants is the list of restaurants to be displayed
     */
    public RestaurantAdapter(Context context, ArrayList<Restaurant> restaurants) {
        super(context, 0, restaurants);
        imageLoader = ImageLoader.getInstance(); // Get singleton instance
        // set options in Universal Image Loader for loaded images to be cached in memory and/or on disk
        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
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
        Restaurant currentRest = getItem(position);

        // Find the 'name' TextView in the list_item.xml layout.
        TextView nameTV = (TextView) listItemView.findViewById(R.id.name_list_item);

        // Get the name of restaurant from the currentRest object
        nameTV.setText(currentRest.getNameOfRestaurant());

        // Find the ImageView in the list_item.xml layout with the ID image.
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.image_list_item);

        // Load image, decode it to Bitmap and display Bitmap in ImageView
        imageLoader.displayImage(currentRest.getImageUrl(), imageView, options);

        // Find the 'rating' RatingBar in the list_item.xml layout and set the rating
        RatingBar ratingBar = (RatingBar) listItemView.findViewById(R.id.rating_list_item);
        ratingBar.setRating(currentRest.getRating());

        // Find the 'reviewCount' TextView in the list_item.xml layout and set the review counts
        TextView reviewCountTV = (TextView) listItemView.findViewById(R.id.review_count_list_item);
        String reviewCountStr = " " + currentRest.getReviewCount()+ " reviews";
        reviewCountTV.setText(reviewCountStr);

        // Find the 'price' TextView in the list_item.xml layout and set the price
        TextView priceTV = (TextView) listItemView.findViewById(R.id.price_list_item);
        String priceStr = "Price: " + currentRest.getPrice();
        priceTV.setText(priceStr);

        // Find the 'isOpen' TextView in the list_item.xml layout.
        TextView openTV = (TextView) listItemView.findViewById(R.id.is_open_list_item);
        if(currentRest.isClosed()) openTV.setVisibility(View.VISIBLE);

        // Find the 'isClosed' TextView in the list_item.xml layout.
        TextView closedTV = (TextView) listItemView.findViewById(R.id.is_closed_list_item);
        if(!currentRest.isClosed()) closedTV.setVisibility(View.VISIBLE);


        // Return the whole list item layout
        return listItemView;
    }



}
