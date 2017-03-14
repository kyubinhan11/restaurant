package com.example.android.yrestaurants;

/**
 * Created by Kevin on 3/13/2017.
 */

public class Restaurant {

    /** Image resource ID for the restaurant */
    private String mImageUrl;

    /** Name of the restaurant */
    private String mNameOfRestaurant;

    /** Rating of the restaurant */
    private int mRating;

    /**
     *
     * @param imageUrl is the url for the image
     * @param nameofRestaurant is the name of the restaurant
     */
    public Restaurant(String imageUrl, String nameofRestaurant) {
        mImageUrl = imageUrl;
        mNameOfRestaurant = nameofRestaurant;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getNameOfRestaurant(){
        return mNameOfRestaurant;
    }

}
