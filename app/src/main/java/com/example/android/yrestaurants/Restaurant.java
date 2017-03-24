package com.example.android.yrestaurants;

import android.os.Parcel;
import android.os.Parcelable;

public class Restaurant implements Parcelable {

    /** Restaurant's info */
    private String mImageUrl;
    private String mNameOfRestaurant;
    private String mPrice;
    private String mYId;
    private String mPhone;
    private String mUrl;
    private int mReviewCount;
    private int mDistance;
    private int mRating;
    private boolean mIsClosed;

    public Restaurant(){
        // Default constructor required for calls to DataSnapshot.getValue(Restaurant.class)
    }

    public Restaurant(String mNameOfRestaurant, String mUrl, String mImageUrl,
                      String mPhone, boolean mIsClosed, int mRating, int mReviewCount, int mDistance, String mPrice, String mYId) {
        this.mImageUrl = mImageUrl;
        this.mNameOfRestaurant = mNameOfRestaurant;
        this.mRating = mRating;
        this.mIsClosed = mIsClosed;
        this.mPhone = mPhone;
        this.mUrl = mUrl;
        this.mReviewCount = mReviewCount;
        this.mDistance = mDistance;
        this.mPrice = mPrice;
        this.mYId = mYId;

    }

    /* Getters methods*/
    public String getYId() {
        return mYId;
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
    /* End of getters methods*/

    /* Setters are needed for DataSnapshot.getValue(Restaurant.class) */
    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public void setNameOfRestaurant(String mNameOfRestaurant) {
        this.mNameOfRestaurant = mNameOfRestaurant;
    }

    public void setRating(int mRating) {
        this.mRating = mRating;
    }

    public void setClosed(boolean mIsClosed) {
        this.mIsClosed = mIsClosed;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public void setReviewCount(int mReviewCount) {
        this.mReviewCount = mReviewCount;
    }

    public void setDistance(int mDistance) {
        this.mDistance = mDistance;
    }

    public void setPrice(String mPrice) {
        this.mPrice = mPrice;
    }

    public void setYId(String mYId) {
        this.mYId = mYId;
    }
    /* End of setters methods*/


    /* Following methods were created by http://www.parcelabler.com/
    *  to make this class parcelable
    */
    protected Restaurant(Parcel in) {
        mImageUrl = in.readString();
        mNameOfRestaurant = in.readString();
        mRating = in.readInt();
        mIsClosed = in.readByte() != 0x00;
        mPhone = in.readString();
        mUrl = in.readString();
        mReviewCount = in.readInt();
        mDistance = in.readInt();
        mPrice = in.readString();
        mYId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageUrl);
        dest.writeString(mNameOfRestaurant);
        dest.writeInt(mRating);
        dest.writeByte((byte) (mIsClosed ? 0x01 : 0x00));
        dest.writeString(mPhone);
        dest.writeString(mUrl);
        dest.writeInt(mReviewCount);
        dest.writeInt(mDistance);
        dest.writeString(mPrice);
        dest.writeString(mYId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Restaurant> CREATOR = new Parcelable.Creator<Restaurant>() {
        @Override
        public Restaurant createFromParcel(Parcel in) {
            return new Restaurant(in);
        }

        @Override
        public Restaurant[] newArray(int size) {
            return new Restaurant[size];
        }
    };
}