package com.augmentis.ayp.photogallery;

import java.sql.SQLOutput;
import java.util.Objects;

/**
 * Created by Apinya on 8/16/2016.
 */
public class GalleryItem {
    private String mId;
    private String mTitle;
    private String mUrl;
    private String bigSizeUrl;


    public void setId(String id) {
        this.mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getName(){
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof GalleryItem){
            //is GalleryItem too!!
            GalleryItem that = (GalleryItem) obj;

            return that.mId != null && mId != null && that.mId.equals(mId);

        }
        return false;
    }

    public void setBigSizeUrl(String bigSizeUrl) {
        this.bigSizeUrl = bigSizeUrl;
    }


    public String getBigSizeUrl() {
        return bigSizeUrl;
    }
}
