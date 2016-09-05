package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by Apinya on 9/5/2016.
 */
public class PhotoMapFragment extends SupportMapFragment {

    private static final String KEY_LOCATION = "GA1";
    private static final String KEY_GALLERY_ITEM = "GA2";
    private static final String KEY_BITMAP = "GA3";
    private GoogleMap mGoogleMap;

        public static PhotoMapFragment newInstance(Location location,
                                                   Location galleryItemLoc,
                                                   Bitmap bitmap){

            Bundle args = new Bundle();
            args.putParcelable(KEY_LOCATION, location);
            args.putParcelable(KEY_GALLERY_ITEM, galleryItemLoc);
            args.putParcelable(KEY_BITMAP, bitmap);
            PhotoMapFragment pf = new PhotoMapFragment();
            pf.setArguments(args);
            return pf;
        }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setHasOptionsMenu(true);

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

            }
        });

    }
}
