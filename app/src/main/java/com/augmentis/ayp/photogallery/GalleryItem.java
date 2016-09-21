package com.augmentis.ayp.photogallery;

import android.net.Uri;

/**
 * Created by Apinya on 8/16/2016.
 */
public class GalleryItem {

    private String mId;
    private String mTitle;
    private String mUrl;
    private String mOwner;
    private String mBigSizeUrl;
    private String mLat;
    private String mLon;


    public String getmLat() {
        return mLat;
    }

    public void setmLat(String mLat) {
        this.mLat = mLat;
    }

    public String getmLon() {
        return mLon;
    }

    public void setmLon(String mLon) {
        this.mLon = mLon;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getName() {
        return getTitle();
    }

    public void setName(String name) {
        setTitle(name);
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof GalleryItem) {
            //is GalleryItem too!
            GalleryItem that = (GalleryItem) o;

            return that.mId != null && mId != null && that.mId.equals(mId);
        }
        return false;
    }

    public void setOwner(String owner) {
        this.mOwner = owner;
    }

    public String getmOwner() {
        return mOwner;
    }

    public void setmBigSizeUrl(String mBigSizeUrl) {
        this.mBigSizeUrl = mBigSizeUrl;
    }

    public String getmBigSizeUrl() {
        return mBigSizeUrl;
    }


    private static final String PHOTO_URL_PREFIX = "https://www.flickr.com/photos/";

    public Uri getPhotoUri(){
        return Uri.parse(PHOTO_URL_PREFIX).buildUpon() // Return builder
                .appendPath(mOwner)
                .appendPath(mId)
                .build(); // Return Uri
    }

    public boolean isGeoCorrect(){
        return ! ("0".equals(mLat) && !"0".equals(mLon) );
    }

}
