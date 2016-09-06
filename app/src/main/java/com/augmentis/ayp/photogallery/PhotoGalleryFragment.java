package com.augmentis.ayp.photogallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
//import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apinya on 8/16/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";
    private static final int REQUEST_SHOW_PHOTO_DETAIL = 123;
    private static final String DIALOG_SHOW_PHOTO_DETAIL = "SHOW_PHOTO_DETAIL";
    private static final int REQUEST_PERMISSION_LOCATION = 231;

    public static PhotoGalleryFragment newInstance() {
        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private FlickrFetcher mFlickrFetcher;
    private PhotoGalleryAdapter mAdapter;
    //    private List<GalleryItem> mItems;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private String mSearchKey;
    private Boolean mUseGps;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;


    // Cache
    private LruCache<String, Bitmap> mMemoryCache;
    // Memory
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;

    @SuppressWarnings("all")
    private GoogleApiClient.ConnectionCallbacks mCCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Log.i(TAG, "Google API connected");

            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i(TAG, "Last location : " + mLocation);

            if(mUseGps){
                findLocation();
                loadPhoto();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.i(TAG, "Google API suspended");
        }
    };

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Got Location: " + location.getLatitude()
            + ", " + location.getLongitude());

            mLocation = location;

            if(mUseGps){
                loadPhoto();
            }

            Toast.makeText(getActivity(), location.getLatitude() + ", "
                    + location.getLongitude(), Toast.LENGTH_LONG).show();

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);

        mUseGps = PhotoGalleryPreference.getUseGPS(getActivity());
        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity() );

        Intent i = PollService.newIntent(getActivity());
        getActivity().startService(i);
//
//        PollJobService.start(getActivity());
        PollService.setServiceAlarm(getActivity(), true);

        Log.d(TAG, "Memory sixe = " + maxMemory + " K ");

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

//        // Move from onCreateView
//        mFlickrFetcher = new FlickrFetcher();
//        mFetcherTask = new FetcherTask();
//        new FetcherTask().execute(); //run another thread.

        Handler responseUIHandler = new Handler();
        ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder> listener =
                new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail, String url) {

                        if (null == mMemoryCache.get(url)) {
                            mMemoryCache.put(url, thumbnail);
                        }
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(mCCallbacks)
                .build();

        Log.i(TAG, "Start background thread");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.list_menu_refresh, menu);

        MenuItem menuItem = menu.findItem(R.id.menu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQuery(mSearchKey, false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submitted: " + query);
                mSearchKey = query;
                loadPhoto();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changed: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(mSearchKey, false);
            }
        });

        //render polling
        MenuItem menuPolling = menu.findItem(R.id.menu_toggle_polling);

        if(PollService.isServiceAlarmOn(getActivity())){
            menuPolling.setTitle(R.string.stop_polling);
        }else {
            menuPolling.setTitle(R.string.start_polling);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reload:
                loadPhoto();
                return true;

            case R.id.menu_toggle_polling:

                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());

                Log.d(TAG, ((shouldStartAlarm) ? "Start" : "Stop") + " Intent service" );

                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu(); //refresh menu
                return true;

            case R.id.menu_search: mSearchKey = null;
                loadPhoto();
                return true;

            case R.id.menu_manual_check:
                Intent pollIntent = PollService.newIntent(getActivity());
                getActivity().startService(pollIntent);
                return true;

            case R.id.menu_alarm_clock:
                return true;

            case R.id.menu_setting:

                Intent intent = SettingActivity.newIntent(getActivity());
                startActivity(intent);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadPhoto() {
        if (mFetcherTask == null) {
            mFetcherTask = new FetcherTask();

            if (mSearchKey != null) {
                mFetcherTask.execute(mSearchKey);
            } else {
                mFetcherTask.execute();
            }
        }else {
            Log.d(TAG, "Fetch task is running now" );
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG, "Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mThumbnailDownloaderThread.clearQueue();
    }

    @Override
    public void onPause() {
        super.onPause();
        PhotoGalleryPreference.setStoredSearchKey(getActivity(), mSearchKey);
        unFindLocation();
    }

    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());

        if (searchKey != null) {
            mSearchKey = searchKey;
        }

        mUseGps = PhotoGalleryPreference.getUseGPS(getActivity());

        if(!mUseGps){
            loadPhoto();
        }

        Log.d(TAG, "on resume completed, mSearchKey = " + mSearchKey
         + ", mUseGPS = " + mUseGps);
    }

    private void findLocation(){
        if(hasPermission()){
            requestLocation();
        }
    }

    private boolean hasPermission(){
        int permissionStatus =
                ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if(permissionStatus == PackageManager.PERMISSION_GRANTED){
            return true;
        }

        requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        },
                REQUEST_PERMISSION_LOCATION);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_PERMISSION_LOCATION){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                requestLocation();
            }
        }
    }

    @SuppressWarnings("all")
    private void requestLocation() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())
                == ConnectionResult.SUCCESS) {

            LocationRequest request = LocationRequest.create();

            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            request.setNumUpdates(50);
            request.setInterval(1000);

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    request, mLocationListener);

        }
    }

    private void unFindLocation() {
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity())
                == ConnectionResult.SUCCESS) {

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
                    mLocationListener);
        }
    }

    private List<GalleryItem> mItems;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mItems = new ArrayList<>();
        mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

        mSearchKey = PhotoGalleryPreference.getStoredSearchKey(getActivity());
//        loadPhoto();
        Log.d(TAG, "On create complete : ----- Loaded key -----" + mSearchKey);
//        mRecyclerView.setAdapter(new PhotoGalleryAdapter(itemList));
        return v;
    }

    class PhotoHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        ImageView mPhoto;
//        String mBigUrl;
        GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
            mPhoto.setOnClickListener(this);

            itemView.setOnCreateContextMenuListener(this); // itemView is which holder are holding
        }

        public void bindDrawable(@NonNull Drawable drawable) {
            mPhoto.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            FragmentManager fm = getFragmentManager();
            PhotoDialog dD = PhotoDialog.newInstance(mGalleryItem.getmBigSizeUrl());
            dD.setTargetFragment(PhotoGalleryFragment.this, REQUEST_SHOW_PHOTO_DETAIL);
            dD.show(fm, DIALOG_SHOW_PHOTO_DETAIL);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(mGalleryItem.getPhotoUri().toString());

            MenuItem menuItem = menu.add(0, 1, 0, R.string.open_with_external_browser);
            menuItem.setOnMenuItemClickListener(this);
            //
            MenuItem menuItem2 = menu.add(0, 2, 0, R.string.open_in_app_browser);
            menuItem2.setOnMenuItemClickListener(this);

            MenuItem menuItem3 = menu.add(0, 3, 0, R.string.open_in_map);
            menuItem3.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            switch (item.getItemId()) {
                case 1:
                Intent i1 = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoUri());
                startActivity(i1); // call external browser by implicit intent
//            Toast.makeText(getActivity(), mGalleryItem.getUrl(), Toast.LENGTH_LONG).show();
                return true;

                case 2:
                    Intent i2 = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoUri());
                    startActivity(i2); // call internal activity by explicit intent
                    return true;
                case 3:
                    Location itemLoc = null;
                    if(mGalleryItem.isGeoCorrect() ) {
                            itemLoc = new Location("");
                            itemLoc.setLatitude(Double.valueOf(mGalleryItem.getmLat()));
                            itemLoc.setLongitude(Double.valueOf(mGalleryItem.getmLon()));
                        }

                    Intent i3 = PhotoMapActivity.newIntent(getActivity(), mLocation, itemLoc, mGalleryItem.getUrl());
                    startActivity(i3);

                default:
            }
            return false;
        }
    }

    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder> {

        List<GalleryItem> mGalleryItemList;

        PhotoGalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItemList = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(
                    R.layout.item_photo, parent, false);

            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
//            holder.bindGalleryItem(mGalleryItemList.get(position));
            Drawable smileyDrawable = ResourcesCompat
                    .getDrawable(getResources(), R.drawable.aa, null);

            GalleryItem galleryItem = mGalleryItemList.get(position);
            Log.d(TAG, "bind position #" + position + ", url: " + galleryItem.getUrl());

            holder.bindGalleryItem(galleryItem);
            holder.bindDrawable(smileyDrawable);

            if(mMemoryCache.get(galleryItem.getUrl()) != null) {
                Bitmap bitmap = mMemoryCache.get(galleryItem.getUrl());
                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
            } else {
                mThumbnailDownloaderThread.queueThumbnailDownload(holder, galleryItem.getUrl());
            }
        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();
        }
    }

    class FetcherTask extends AsyncTask<String, Void, List<GalleryItem>> {

        @Override
        protected List<GalleryItem> doInBackground(String... params) {

                Log.d(TAG, "Fetcher task finish");
                List<GalleryItem> itemList = new ArrayList<>();
                FlickrFetcher flickrFetcher = new FlickrFetcher();

                if (params.length > 0) {

                            if(mUseGps && mLocation != null){
                                flickrFetcher.searchPhotos(itemList, params[0],
                                        String.valueOf(mLocation.getLatitude() ),
                                        String.valueOf(mLocation.getLongitude() )
                                );
                            }else {
                                flickrFetcher.searchPhotos(itemList, params[0]);
                            }

                } else
                        {
                            flickrFetcher.getRecentPhotos(itemList);
                        }

                Log.d(TAG, "Fetcher task finish");
                return itemList;
            }

    private List<GalleryItem> mItems;

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;

//            mAdapter = new PhotoGalleryAdapter(galleryItems);

            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));

            String formatString = getResources().getString(R.string.photo_progress_loaded);

            mFetcherTask = null;

        }
    }
}
