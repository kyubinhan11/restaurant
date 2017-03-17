package com.example.android.yrestaurants;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Kevin on 3/13/2017.
 */

public class RestaurantAdapter extends ArrayAdapter<Restaurant> {

    /**
     *
     * @param context is the current context that the adapter is being created in.
     * @param restaurants is the list of restaurants to be displayed
     */
    public RestaurantAdapter(Context context, ArrayList<Restaurant> restaurants) {
        super(context, 0, restaurants);
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


        // Find the TextView in the list_item.xml layout with the ID name_of_restaurant.
        TextView nameTextView = (TextView) listItemView.findViewById(R.id.name_of_restaurant);
        // Get the name of restaurant from the currentRest object
        nameTextView.setText(currentRest.getNameOfRestaurant());

        // Find the ImageView in the list_item.xml layout with the ID image.
        ImageView imageView = (ImageView) listItemView.findViewById(R.id.image_of_restaurant);

        // load the image of restaurant asynchronously
        new ImageDownloader(imageView).execute(currentRest.getImageUrl());

        // Return the whole list item layout
        return listItemView;
    }

    // a class for loading an image asynchronously
    class ImageDownloader extends AsyncTask<String, Void, Bitmap> {
        ImageView imageView;

        public ImageDownloader(ImageView imageView) {
            this.imageView = imageView;
        }

        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            Bitmap mIcon = null;
            try {
                InputStream in = new java.net.URL(url).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
            return mIcon;
        }
        // after loading is done set the image in the ImageView
        protected void onPostExecute(Bitmap result) {
            imageView.setImageBitmap(result);
        }
    }

}
