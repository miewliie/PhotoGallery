package com.augmentis.ayp.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
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

    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new  URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try{

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            //if connection is not OK throw new IOException
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[2048];
            while ((bytesRead = in.read(buffer)) > 0){

                out.write(buffer, 0, bytesRead);
            }
            out.close();

            return out.toByteArray();

        }finally {
            connection.disconnect();
        }

    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }


    public String fetchItem() throws IOException{
        String jsonString = null;
            String url = Uri.parse(FLICKR_URL).buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();

            jsonString = getUrlString(url);

            Log.i(TAG, "Received JSON: " + jsonString);
        return jsonString;
    }


    public void fetchItem(List<GalleryItem> items){
        try {
                String jsonStr = fetchItem();
                if(jsonStr != null) {
                    parseJSON(items, jsonStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to fetch items", e);
            }
        }

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

            newGalleryItemList.add(item);
        }
    }


}