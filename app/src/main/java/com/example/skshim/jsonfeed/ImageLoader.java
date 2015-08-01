package com.example.skshim.jsonfeed;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

        // Make a link between imageView and task that returns bitmap drawable.
        final DrawableWithAsyncTask drawableWithAsyncTask=
                new DrawableWithAsyncTask(mResources, null, task);
        imageView.setImageDrawable(drawableWithAsyncTask);

        task.execute(url);
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that only the last started worker process
     * can bind its result, independently of the finish order.
     */
    private static class DrawableWithAsyncTask extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public DrawableWithAsyncTask(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getAttachedWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DrawableWithAsyncTask) {
                final DrawableWithAsyncTask drawableWithAsyncTask = (DrawableWithAsyncTask) drawable;
                return drawableWithAsyncTask.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<String, Void, BitmapDrawable> {

        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
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

            final ImageView imageView = getAttachedImageView();
            if (drawable != null && imageView != null) {
                imageView.setImageDrawable(drawable);
            }else{
                if(imageView!=null){
                    imageView.setVisibility(View.GONE);
                }
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getAttachedWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
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
