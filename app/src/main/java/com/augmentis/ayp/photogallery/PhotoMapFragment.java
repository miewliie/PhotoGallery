package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

/**
 * Created by Apinya on 9/5/2016.
 */
public class PhotoMapFragment extends SupportMapFragment {

    private static final String TAG = "PhotoMapFragment";
    private static final String KEY_LOCATION = "GA1";
    private static final String KEY_GALLERY_ITEM = "GA2";
    private static final String KEY_URL = "GA3";

        public static PhotoMapFragment newInstance(Location location,
                                                   Location galleryItemLoc,
                                                   String url){

            Bundle args = new Bundle();
            args.putParcelable(KEY_LOCATION, location);
            args.putParcelable(KEY_GALLERY_ITEM, galleryItemLoc);
            args.putString(KEY_URL, url);
            PhotoMapFragment pf = new PhotoMapFragment();
            pf.setArguments(args);
            return pf;
        }

    private GoogleMap mGoogleMap;
    private Location mLocation;
    private Location mLocationGallery;
    private String mUrl;
    private Bitmap mBitmap;
    private MakerFetchTask mFetcherTask;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setHasOptionsMenu(true);

        if(getArguments() != null){
            mLocation = getArguments().getParcelable(KEY_LOCATION);
            mLocationGallery = getArguments().getParcelable(KEY_GALLERY_ITEM);
            mUrl = getArguments().getString(KEY_URL);
        }

        if(mUrl != null){
            Log.d(TAG, "Get url : " + mUrl);
        }

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;

                if(mUrl == null){
                    updateMapUI();

                }else {
//                    mFetcherTask = new MakerFetchTask();
//                    mFetcherTask.execute(mUrl);

                    Glide.with(getActivity() ).load(mUrl)
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    mBitmap = resource;
                                    updateMapUI();
                                }
                            });
                }
            }
        });

    }

    private void updateMapUI(){
        mGoogleMap.clear();

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if(mLocation != null) {
            Log.d(TAG, "Found location for my location " + mLocation);
            plotMarker(mLocation, builder);

        }
        if(mLocationGallery != null) {
            Log.d(TAG, "Found location for gallery item " );

                    if (mBitmap == null) {
                        plotMarker(mLocationGallery, builder);
                    } else {
                        plotMarker(mLocationGallery, builder, mBitmap);
                    }
         }

        int margin = getResources().getDimensionPixelSize(R.dimen.map_insert_margin);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds
                (builder.build(), margin);

        mGoogleMap.animateCamera(cameraUpdate);

    }
    
    private void plotMarker(Location location, final LatLngBounds.Builder builder){
        Log.d(TAG, "Plot Location =" + location);

        LatLng itemPoint = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions itemMarkerOptions = new MarkerOptions()
                .position(itemPoint);

        mGoogleMap.addMarker(itemMarkerOptions);
        builder.include(itemPoint);

//        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemPoint, 15));
    }

    private void plotMarker(final Location location, final LatLngBounds.Builder builder,
                            final Bitmap bitmap){

        Log.d(TAG, "Plot Location w/ bitmap =" + location);

        LatLng itemPoint = new LatLng(location.getLatitude(), location.getLongitude());

        //Use bitmap as marker
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
        MarkerOptions itemMarkerOptions = new MarkerOptions()
                .position(itemPoint)
                .icon(itemBitmap);

        mGoogleMap.addMarker(itemMarkerOptions);
        builder.include(itemPoint);
    }

    private class MakerFetchTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            //Fetch Photo
            String url = params[0];

            FlickrFetcher flickrFetcher = new FlickrFetcher();
            try {
                byte[] imageBytes = flickrFetcher.getUrlBytes(url);
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                return bitmap;
            }catch (IOException ioe){
                Log.e(TAG, "Error in IO ", ioe);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mBitmap = bitmap;
            updateMapUI();
            mFetcherTask = null;
        }
    }
}
