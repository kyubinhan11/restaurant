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

    private boolean mIsClosed;

    private String mPhone;

    private String mUrl;

    private int mReviewCount;

    private int mDistance;

    private String mPrice;

    public Restaurant(String mNameOfRestaurant, String mUrl, String mImageUrl,
                      String mPhone, boolean mIsClosed, int mRating, int mReviewCount, int mDistance, String mPrice) {
        this.mImageUrl = mImageUrl;
        this.mNameOfRestaurant = mNameOfRestaurant;
        this.mRating = mRating;
        this.mIsClosed = mIsClosed;
        this.mPhone = mPhone;
        this.mUrl = mUrl;
        this.mReviewCount = mReviewCount;
        this.mDistance = mDistance;
        this.mPrice = mPrice;
    }

    public int getReviewCount() {
        return mReviewCount;
    }

    public int getDistance() {
        return mDistance;
    }

    public String getPrice() {
        return mPrice;
    }

    public int getRating() {
        return mRating;
    }

    public boolean isClosed() {
        return mIsClosed;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getNameOfRestaurant(){
        return mNameOfRestaurant;
    }

    @Override
    public String toString() {
        return "Restaurant{" +
                "mImageUrl='" + mImageUrl + '\'' +
                ", mNameOfRestaurant='" + mNameOfRestaurant + '\'' +
                ", mRating=" + mRating +
                ", mIsClosed=" + mIsClosed +
                ", mPhone='" + mPhone + '\'' +
                ", mUrl='" + mUrl + '\'' +
                '}';
    }
}
