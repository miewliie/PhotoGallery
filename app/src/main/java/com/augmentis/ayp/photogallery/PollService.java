package com.augmentis.ayp.photogallery;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Apinya on 8/22/2016.
 */
public class PollService extends IntentService {
    private static final String TAG = "PollService";

    public PollService() {
        super(TAG);
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "Receive a call from intent: " + intent);

    }
}
