package com.augmentis.ayp.photogallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Apinya on 8/22/2016.
 */
public class PhotoDialog extends DialogFragment implements DialogInterface.OnClickListener{

    private ImageView imageView;
    private String BigUrl;
    private FlickrFetcher mPicFlickrFetcher;
    private static final String TAG = "PhotoDialog";

    public static PhotoDialog newInstance(String mBigUrl){
        PhotoDialog photoDialog = new PhotoDialog();
        Bundle args = new Bundle();
        args.putSerializable("PHOTOURL", mBigUrl);
        photoDialog.setArguments(args);
        return photoDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        BigUrl = (String) getArguments().getSerializable("PHOTOURL");

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
        imageView = (ImageView) v.findViewById(R.id.photo_view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(v);
        builder.setPositiveButton("Close", this);

        return builder.create();

    }


    public class AsyncPhoto extends AsyncTask<String, Void, Bitmap>{

        boolean running = false;


        @Override
        protected Bitmap doInBackground(String... url) {
            synchronized (this) {
                running = true;
            }

            try {

                Bitmap bitmap = bitmapfromUrl();
                return ;
                
            }finally {
                synchronized (this) {
                    running = false;
                }
            }

        }
    }

    private Bitmap bitmapfromUrl() {
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

    }
}
