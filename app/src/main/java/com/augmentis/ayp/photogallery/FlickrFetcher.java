package com.augmentis.ayp.photogallery;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Apinya on 8/16/2016.
 */
public class FlickrFetcher { private static final String TAG = "FlickrFetcher";

    /**
     *  Open connection.
     *  Read and write data from stream.
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            //read data from Stream
            InputStream in = connection.getInputStream();

            //if connection is not OK throw new IOException
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;

            byte[] buffer = new byte[2048];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();

            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    /**
     * Convert urlByte to string.
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //
    private static final String FLICKR_URL = "https://api.flickr.com/services/rest/";
    private static final String API_KEY = "73110b7d3d1996b9a8126f98d98e5f6e";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SERACH = "flickr.photos.search";

//    public String fetchItems() throws IOException {
//
//        String jsonString = null;
//
//        String url = Uri.parse(FLICKR_URL).buildUpon()
//                .appendQueryParameter("method", METHOD_GET_RECENT)
//                .appendQueryParameter("api_key", API_KEY)
//                .appendQueryParameter("format", "json")
//                .appendQueryParameter("nojsoncallback", "1")
//                .appendQueryParameter("extras", "url_s")
//                .build().toString();
//
//        jsonString = getUrlString(url);
//
//        Log.i(TAG, "Recent: Recieived JSON: " + jsonString);
//
//        return jsonString;
//    }

//    public String searchItems(String key) throws IOException {
//
//        String jsonString = null;
//        Uri baseUrl = Uri.parse(FLICKR_URL);
//        Uri.Builder builder = baseUrl.buildUpon();
//
////        String url = Uri.parse(FLICKR_URL).buildUpon()
//        builder.appendQueryParameter("method", METHOD_SERACH);
//        builder.appendQueryParameter("api_key", API_KEY)
//                .appendQueryParameter("format", "json")
//                .appendQueryParameter("nojsoncallback", "1")
//                .appendQueryParameter("extras", "url_s")
//                .appendQueryParameter("text", key);
////                .build().toString();
//
//        Uri completeUrl = builder.build();
//        String url = completeUrl.toString();
//
//        Log.i(TAG, "Run URL: " + url);
//
//        return url;
//    }

    private String buildUrl(String method, String... param) throws IOException {

        String jsonString = null;
        Uri baseUrl = Uri.parse(FLICKR_URL);
        Uri.Builder builder = baseUrl.buildUpon();

//        String url = Uri.parse(FLICKR_URL).buildUpon()
        builder.appendQueryParameter("method", method);
        builder.appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s , url_o, geo");

        if (METHOD_SERACH.equalsIgnoreCase(method)) {
            builder.appendQueryParameter("text", param[0]);
        }

        if(param.length > 1){
            //Latitude * Longitude Added

            builder.appendQueryParameter("lat", param[1] );
            builder.appendQueryParameter("lon", param[2] );
        }

        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG, "Run URL: " + url);

        return url;
    }

    private String queryItem(String url) throws IOException {

        Log.i(TAG, "Run URL: " + url);
        String jsonString = getUrlString(url);

        Log.i(TAG, "Search: Recieived JSON: " + jsonString);

        return jsonString;
    }

    public void searchPhotos(List<GalleryItem> items, String key,String lat, String lon) {
        try {
            String url = buildUrl(METHOD_SERACH, key, lat, lon);

            fetchPhoto(items, url);

        } catch (Exception e)
            {
                e.printStackTrace();
                Log.e(TAG, "Failed to fetch items ", e);
            }
    }

    public void searchPhotos(List<GalleryItem> items, String key) {
        try {
            String url = buildUrl(METHOD_SERACH, key );
            fetchPhoto(items, url);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetch items ", e);
        }
    }

    public void getRecentPhotos(List<GalleryItem> items) {

        try {
                String url = buildUrl(METHOD_GET_RECENT);
                fetchPhoto(items, url);

        } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to fetch items ", e);
            }
    }

    public void fetchPhoto(List<GalleryItem> items, String url) throws IOException, JSONException{
        String jsonStr = queryItem(url);

            if (jsonStr != null) {
                parseJSON(items, jsonStr);
            }
    }

    /**
     * Convert JSON object detail to data for add in each item of GalleryItem.
     *
     * @param newGalleryItemList List of GalleryItem class
     * @param jsonBodyStr String that data of JSON object
     * @throws IOException
     * @throws JSONException
     */
    private void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr)
            throws IOException, JSONException {

        JSONObject jsonBody = new JSONObject(jsonBodyStr);
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

        for (int i = 0; i < photoListJson.length(); i++) {
            JSONObject jsonPhotoItem = photoListJson.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setId(jsonPhotoItem.getString("id"));
            item.setTitle(jsonPhotoItem.getString("title"));
            item.setOwner(jsonPhotoItem.getString("owner"));

            if (!jsonPhotoItem.has("url_s")) {
                continue;
            }
            item.setUrl(jsonPhotoItem.getString("url_s"));

            if(!jsonPhotoItem.has("url_o")){
                continue;
            }

            item.setmBigSizeUrl(jsonPhotoItem.getString("url_o") );

            item.setmLat(jsonPhotoItem.getString("latitude") );
            item.setmLon(jsonPhotoItem.getString("longitude") );

            newGalleryItemList.add(item);
        }
    }

}
