package com.augmentis.ayp.photogallery;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;

/**
 * Created by Apinya on 9/5/2016.
 */
public class PhotoMapActivity extends SingleFragmentActivity {

    private static final String KEY_LOCATION = "GA1";
    private static final String KEY_GALLERY_ITEM = "GA2";
    private static final String KEY_URL = "GA3";

    protected static Intent newIntent(Context context, Location location,
                                      Location galleryItemLoc, String url){

        Intent i = new Intent(context, PhotoMapActivity.class);
        i.putExtra(KEY_LOCATION, location);
        i.putExtra(KEY_GALLERY_ITEM, galleryItemLoc);
        i.putExtra(KEY_URL, url);

        return i;
    }

    @Override
    protected Fragment onCreateFragment() {

        if(getIntent() != null){
            Location galleryLoc = getIntent().getParcelableExtra(KEY_GALLERY_ITEM);
            Location location = getIntent().getParcelableExtra(KEY_LOCATION);
            String url = getIntent().getStringExtra(KEY_URL);

            return PhotoMapFragment.newInstance(location, galleryLoc, url);
        }

        return PhotoMapFragment.newInstance();
    }
}
