package com.augmentis.ayp.photogallery;

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
public class FlickrFetcher {

    private static final String TAG = "FlickrFetcher";
    private static final String FLICKR_URL = "https://api.flickr.com/services/rest";
    private static final String API_KEY = "4150ad5d155e4dd7aa5bffda93c902ca";
    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";


    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new  URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // ask for connection

        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();// get input stream into 'in'

            //if connection is not OK throw new IOException
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){ //if reponse code not equal HTTP ok
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec); //then throw except.
            }

            int bytesRead = 0;
            byte[] buffer = new byte[2048]; // create buffer that have 2048 byte
            while ((bytesRead = in.read(buffer)) > 0){ //read input stream then get into buffer

                out.write(buffer, 0, bytesRead);  //write to output stream
            }

            out.close();// close stop write output stream

            return out.toByteArray();// return out

        }finally {
            connection.disconnect(); //stop connect
        }

    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec)); //return
    }


    public String buildUri(String method, String ... param) throws IOException{

        Uri baseUrl = Uri.parse(FLICKR_URL);
        Uri.Builder builder = baseUrl.buildUpon();

        builder.appendQueryParameter("method", method);
        builder.appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s, url_o");

                if(METHOD_SEARCH.equalsIgnoreCase(method)){
                   builder.appendQueryParameter("text", param[0]);
                }

        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG, "Run URL: " + url);

        return url;
    }

    /**
     * Search Photo then put into <b>items</b>
     *
     * @param items array target
     * @param key to search
     */
    public void searchPhotos(List<GalleryItem> items, String key){
        try{
            String url = buildUri(METHOD_SEARCH, key);
            String jsonStr = queryItem(url);

            if(jsonStr != null){
                parseJSON(items, jsonStr);
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Failed to fetch items ", e);
        }
    }

    public void getRecentPhotos(List<GalleryItem> items){
        try{
            String url = buildUri(METHOD_GET_RECENT);
            String jsonStr = queryItem(url);

            if(jsonStr != null){
                parseJSON(items, jsonStr);
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Failed to fetch items ", e);
        }
    }

    private String queryItem(String url) throws IOException{
        Log.i(TAG, "Run URL: " + url);
        String jsonString = getUrlString(url);

        Log.i(TAG, "Search: Received JSON: " + jsonString);
        return jsonString;
    }

    /**
     * get object photo from Json then add item to gallery list
     *
     * @param newGalleryItemList keep list galleryItem
     * @param jsonBodyStr  Json object string type
     * @throws IOException
     * @throws JSONException
     */

    public void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr)
        throws IOException, JSONException{

        JSONObject jsonBody = new JSONObject(jsonBodyStr);
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

        for(int i = 0; i < photoListJson.length(); i++){
            JSONObject jsonPhotoItem = photoListJson.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setId(jsonPhotoItem.getString("id"));
            item.setTitle(jsonPhotoItem.getString("title"));

            if(!jsonPhotoItem.has("url_s")){
                continue;
            }

            item.setUrl(jsonPhotoItem.getString("url_s"));

            if(!jsonPhotoItem.has("url_o")){
                continue;
            }

            item.setBigSizeUrl(jsonPhotoItem.getString("url_o"));

            newGalleryItemList.add(item);
        }
    }


}
