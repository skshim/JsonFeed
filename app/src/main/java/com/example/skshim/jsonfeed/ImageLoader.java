package com.example.skshim.jsonfeed;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sungki Shim on 1/08/15.
 */
public class ImageLoader {

    private Resources mResources;

    public ImageLoader(Context context){
        mResources = context.getResources();
    }

    public void loadImage(String url, ImageView imageView) {
        final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
        task.execute(url);
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {
        private String mData;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(String... urls) {
            Bitmap bitmap = null;
            BitmapDrawable drawable = null;

            bitmap = processBitmap(urls[0]);
            if(bitmap==null){
                return null;
            }else{
                return new BitmapDrawable(mResources, bitmap);
            }
        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            ImageView imageView = imageViewReference.get();
            if(drawable!=null && imageView !=null){
                imageView.setImageDrawable(drawable);
            }else{
                imageView.setVisibility(View.GONE);
            }
        }
    }

    private Bitmap processBitmap(String imageUrl) {
        InputStream inputStream = null;
        Bitmap bitmap=null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(1000 /* milliseconds */);
            conn.setConnectTimeout(1000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query.
            conn.connect();
            int response = conn.getResponseCode();

            inputStream = conn.getInputStream();

            // Decoding stream data back into image Bitmap that android understands.
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (IOException e) {
            Log.e("processBitmap","error "+e);
            // Found the below errors while processing url.
            // FileNotFoundException,SocketTimeoutException,UnknownHostException
            bitmap=null;
        }finally {
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            if (inputStream != null) {
                try{
                    inputStream.close();
                } catch (final IOException e) {}
            }
            return bitmap;
        }
    }
}
