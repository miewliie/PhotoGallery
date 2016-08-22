package com.augmentis.ayp.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Apinya on 8/18/2016.
 */
public class ThumbnailDownloader<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownloader";
    private static final int DOWNLOAD_FILE = 214;

    private Handler mRequestHandler;
    private final ConcurrentMap<T, String> mRequestUrlMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloaderListener<T> mTThumbnailDownloaderListener;


    interface ThumbnailDownloaderListener<T>{
        void onThumbnailDownloaded(T target, Bitmap thumbnail, String url);
    }

    public void setmTThumbnailDownloaderListener(ThumbnailDownloaderListener<T> mTThumbnailDownloaderListener) {
        this.mTThumbnailDownloaderListener = mTThumbnailDownloaderListener;
    }

    public ThumbnailDownloader(Handler mUIHandler) {
        super(TAG);

        mResponseHandler = mUIHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                //work in the queue
                if(msg.what == DOWNLOAD_FILE){
                    T target = (T) msg.obj;

                    String url = mRequestUrlMap.get(target);
                    Log.i(TAG, "Got message from queue: pls download this URL: " + url);

                    handleRequestDownload(target, url);
                }
            }
        };
    }

    private void handleRequestDownload(final T target, final String url){

        try {
            if(url == null){
                return;
            }
            byte[] bitMapBytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapBytes, 0, bitMapBytes.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    String currentUrl = mRequestUrlMap.get(target);
                    if(currentUrl != null && ! currentUrl.equals(url)){
                        return;
                    }

                    //url is ok (the same one)
                    mRequestUrlMap.remove(target);
                    mTThumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap, url);
                }
            });

            Log.i(TAG, "Bitmap URL downloaded: ");
        }catch (IOException ioe){
            Log.e(TAG, "Error downloading: ", ioe);
        }

    }
    public void queueThumbnailDownload(T target, String url){
        Log.i(TAG, "Got url : " + url);


        if(null == url){
            mRequestUrlMap.remove(target);
        }else {
            mRequestUrlMap.put(target, url);
        }

        Message msg = mRequestHandler.obtainMessage(DOWNLOAD_FILE, target);//get mag from handler
        msg.sendToTarget(); //send to hand
    }



    public void clearQueue() {
        mRequestHandler.removeMessages(DOWNLOAD_FILE);
    }
}
