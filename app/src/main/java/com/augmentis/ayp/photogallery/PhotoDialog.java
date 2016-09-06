package com.augmentis.ayp.photogallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by Apinya on 8/29/2016.
 */

public class PhotoDialog extends DialogFragment implements DialogInterface.OnClickListener{

    private ImageView imageView;
    private String BigUrl;
    private FlickrFetcher mPicFlickrFetcher;
    private loadImageTask mLoadImageTask;

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

        Bitmap loadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.aa);


        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo, null);
        imageView = (ImageView) v.findViewById(R.id.photo_view);
        imageView.setImageBitmap(loadBitmap);

//        if(BigUrl != null){
//            Glide.with(getActivity() ).load(BigUrl).into(imageView);
//        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setPositiveButton("Close", this);
        loadBigImage();
        return builder.create();

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        getActivity().getFragmentManager().popBackStack();

    }


    public class loadImageTask extends AsyncTask<String, Void, Bitmap> {

        boolean running = false;

        boolean isRunning(){
            return running;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            synchronized (this){
                running = true;
            }

            try {
                Bitmap bitmap = bitmapFromUrl(BigUrl);
                return bitmap;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                synchronized (this){
                    running = false;
                }
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap bitmapFromUrl(String bigUrl) throws IOException{
        Bitmap bitmap;

        HttpURLConnection connection = (HttpURLConnection)new URL(BigUrl).openConnection();
        connection.setRequestProperty("User-agent", "Mozilla/4.0");
        connection.connect();
        InputStream inputStream = connection.getInputStream();
        bitmap = BitmapFactory.decodeStream(inputStream);
        return bitmap;
    }

    private void loadBigImage(){
        if(mLoadImageTask == null || !mLoadImageTask.isRunning()){
            mLoadImageTask = new loadImageTask();
        }

        mLoadImageTask.execute();


    }
}
