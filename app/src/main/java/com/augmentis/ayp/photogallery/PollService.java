package com.augmentis.ayp.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apinya on 8/22/2016.
 */


public class PollService extends IntentService {

    private static final String TAG = "PollService";

    private static final int POLL_INTERVAL = 1000 * 60; //60 sec
    public static final String ACTION_SHOW_NOTIFICATION = "ayp.aug.photogallery.ACTION_SHOW_NOTIFICATION";

    //permission
    public static final String PERMISSION_SHOW_NOTIF = "ayp.aug.photogallery.RECEIVE_SHOW_NOTIFICATION";
    public static final String REQUESTCODE = "REQUES_CODE_INTENT";
    public static final String NOTIFICATION = "NOTIF";


    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static void setServiceAlarm(Context c, boolean isOn){
        Intent i = PollService.newIntent(c);
        PendingIntent pi = PendingIntent.getService(c, 0, i, 0);

        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
//            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                //AlarmManager.RTC -> System.currentTimeMillis();
                am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,   //param1: Mode
                        SystemClock.elapsedRealtime(),                  //param2: Start
                        POLL_INTERVAL,                                  //param3: Interval
                        pi);                                            //param4: Pending action(intent)
//            }else {
                PollJobService.start(c);
//                Log.d(TAG, "Run by Scheduler");
//            }
        } else {
//            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                am.cancel(pi); //cancel interval call
                pi.cancel(); // cancel pending intent call
//            }else {
                PollJobService.stop(c);
//            }
//
        }

        PhotoGalleryPreference.setStoredIsAlarmOn(c, isOn);
    }

    public static boolean isServiceAlarmOn(Context ctx){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent i = PollService.newIntent(ctx);
            PendingIntent pi = PendingIntent.getService(ctx, 0, i, PendingIntent.FLAG_NO_CREATE);
            return pi != null;
        }else {
            return PollJobService.isRun(ctx);
        }
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "Receive a call from intent: " + intent);

        if (!isNetworkAvailableAndConnected()) {
            return;
        }
        Log.i(TAG, "Active network!");

        String query = PhotoGalleryPreference.getStoredSearchKey(this);
        String storedId = PhotoGalleryPreference.getStoredLastId(this);

        List<GalleryItem> galleryItemList = new ArrayList<>();

        FlickrFetcher flickrFetcher = new FlickrFetcher();
        if(query == null) {
            flickrFetcher.getRecentPhotos(galleryItemList);
        } else {
            flickrFetcher.searchPhotos(galleryItemList, query);
        }

        if (galleryItemList.size() == 0) {
            return;
        }

        Log.i(TAG, "Found search or te ited");

        String newestId = galleryItemList.get(0).getId(); // fetching first item

        if (newestId.equals(storedId)) {
            Log.i(TAG, "No new item");

        } else {
            Log.i(TAG, "New item found");

            Resources res = getResources();

            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            //Build to build notification object
            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this);
            notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
            notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
            notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
            notiBuilder.setContentText(res.getString(R.string.new_picture_content));
            notiBuilder.setContentIntent(pi);
            notiBuilder.setAutoCancel(true); //if it already have it not appear

            Notification notification = notiBuilder.build(); // << Build notification from builder

            sendBackgroundNotification(0, notification);
        }
        PhotoGalleryPreference.setStoredLastId(this, newestId);
    }

    private void sendBackgroundNotification(int requestCode, Notification notification){

        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUESTCODE, requestCode);
        intent.putExtra(NOTIFICATION, notification);

        sendOrderedBroadcast(intent, PERMISSION_SHOW_NOTIF,
                null, null,
                Activity.RESULT_OK,
                null, null);

    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isActiveNetwork = cm.getActiveNetworkInfo() != null;
        boolean isActiveNetworkConnected = isActiveNetwork && cm.getActiveNetworkInfo().isConnected();
        return isActiveNetworkConnected;
    }
}