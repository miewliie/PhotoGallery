package com.augmentis.ayp.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apinya on 8/23/2016.
 */

@TargetApi(21)
public class PollJobService extends JobService {
    
    private static final String TAG = "PollJobService";
    
    private PollTask mPollTask;
    
    @Override
    public boolean onStartJob(JobParameters params) {
        mPollTask = new PollTask();
        mPollTask.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(mPollTask != null){
            mPollTask.cancel(true);
        }
        
        return true;
    }

    public static boolean isRun (Context context){
        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        List<JobInfo> jobInfoList = sch.getAllPendingJobs();
        for(JobInfo jobInfo : jobInfoList){
            if(jobInfo.getId() == JOB_ID){
                return true;
            }
        }

        return false;
    }

    public static void stop(Context context){
        JobScheduler sch = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        sch.cancel(JOB_ID);
    }

    private static final int JOB_ID = 2142;
    public static void start(Context context){
        JobScheduler sch = (JobScheduler)  context.getSystemService(context.JOB_SCHEDULER_SERVICE);
        
        //create job
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, 
                new ComponentName(context, PollJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        builder.setPeriodic(1000 * 1);
        builder.setRequiresDeviceIdle(false);
        JobInfo jobInfo = builder.build();
        
        sch.schedule(jobInfo);
        
    }
    
    
    
    public class PollTask extends AsyncTask<JobParameters, Void, Void>{

        @Override
        protected Void doInBackground(JobParameters... params) {


            Log.d(TAG, "Job poll running");
            jobFinished(params[0], false);

            /////

            {
                String query = PhotoGalleryPreference.getStoredSearchKey(PollJobService.this);
                String storedId = PhotoGalleryPreference.getStoredLastId(PollJobService.this);

                List<GalleryItem> galleryItemList = new ArrayList<>();

                FlickrFetcher flickrFetcher = new FlickrFetcher();
                if(query == null) {
                    flickrFetcher.getRecentPhotos(galleryItemList);
                } else {
                    flickrFetcher.searchPhotos(galleryItemList, query);
                }

                if (galleryItemList.size() == 0) {
                    return null;
                }

                Log.i(TAG, "Found search or te ited");

                String newestId = galleryItemList.get(0).getId(); // fetching first item

                if (newestId.equals(storedId)) {
                    Log.i(TAG, "No new item");

                } else {
                    Log.i(TAG, "New item found");

                    Resources res = getResources();
                    Intent i = PhotoGalleryActivity.newIntent(PollJobService.this);
                    PendingIntent pi = PendingIntent.getActivity(PollJobService.this, 0, i, 0);

                    //Build to build notification object
                    NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(PollJobService.this);
                    notiBuilder.setTicker(res.getString(R.string.new_picture_arriving));
                    notiBuilder.setSmallIcon(android.R.drawable.ic_menu_report_image);
                    notiBuilder.setContentTitle(res.getString(R.string.new_picture_title));
                    notiBuilder.setContentText(res.getString(R.string.new_picture_content));
                    notiBuilder.setContentIntent(pi);
                    notiBuilder.setAutoCancel(true); //if it already have it not appear

                    Notification notification = notiBuilder.build(); // << Build notification from builder

                    //Get notification manager from contect
                    NotificationManagerCompat nm = NotificationManagerCompat.from(PollJobService.this);
                    nm.notify(Long.valueOf(newestId).intValue(), notification);

                    new Screen().on(PollJobService.this);

//            nm.notify(0, notification);
                }
                PhotoGalleryPreference.setStoredLastId(PollJobService.this, newestId);
            }

            /////

            return null;
        }
    }
}
