package com.augmentis.ayp.photogallery;

import java.sql.SQLOutput;
import java.util.Objects;

/**
 * Created by Apinya on 8/16/2016.
 */
public class GalleryItem {
    private String mId;
    private String title;
    private String url;


    public void setId(String id) {
        this.mId = id;
    }

    public String getId() {
        return mId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
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
}
