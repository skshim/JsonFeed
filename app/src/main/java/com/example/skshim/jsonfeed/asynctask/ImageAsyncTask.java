package com.example.skshim.jsonfeed.asynctask;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.example.skshim.jsonfeed.model.Constants;
import com.example.skshim.jsonfeed.image.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sungki Shim on 2/08/15.
 *
 * The actual AsyncTask that will asynchronously process the image.
 */
public class ImageAsyncTask extends AsyncTask<String, Void, BitmapDrawable> {

    public static final String TAG="ImageAsyncTask";

    private Resources mResources;
    private final WeakReference<ImageView> imageViewReference;
    private OnImageDownloadListener mOnImageDownloadListener;
    private String mUrl;

    public ImageAsyncTask(Resources resources, ImageView imageView, OnImageDownloadListener listener) {
        mResources = resources;
        mOnImageDownloadListener=listener;
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    /**
     * Background processing.
     */
    @Override
    protected BitmapDrawable doInBackground(String... urls) {
        mUrl = urls[0];

        Bitmap bitmap = null;
        BitmapDrawable drawable = null;

        bitmap = processBitmap(mUrl);

        if(bitmap!=null){
            drawable= new BitmapDrawable(mResources, bitmap);
        }
        return drawable;
    }

    @Override
    protected void onPostExecute(BitmapDrawable drawable) {

        // call this callback function in order to put image to memory cache
        // for future use.
        mOnImageDownloadListener.onImageDownload(mUrl, drawable);

        final ImageView imageView = getAttachedImageView();
        if (drawable != null && imageView != null) {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Returns the ImageView associated with this task as long as the ImageView's task still
     * points to this task as well. Returns null otherwise.
     */
    private ImageView getAttachedImageView() {
        final ImageView imageView = imageViewReference.get();
        final ImageAsyncTask bitmapWorkerTask = getAttachedWorkerTask(imageView);

        if (this == bitmapWorkerTask) {
            return imageView;
        }

        return null;
    }

    /**
     * Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    public static ImageAsyncTask getAttachedWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof ImageLoader.DrawableWithAsyncTask) {
                final ImageLoader.DrawableWithAsyncTask drawableWithAsyncTask
                        = (ImageLoader.DrawableWithAsyncTask) drawable;
                return drawableWithAsyncTask.getWorkerTask();
            }
        }
        return null;
    }

    private Bitmap processBitmap(String imageUrl) {
        InputStream inputStream = null;
        Bitmap bitmap=null;
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Constants.TIME_OUT /* milliseconds */);
            conn.setConnectTimeout(Constants.TIME_OUT /* milliseconds */);
            conn.addRequestProperty("Accept-Encoding", "identity");
            conn.addRequestProperty("User-Agent", System.getProperty("http.agent"));
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query.
            conn.connect();
            int response = conn.getResponseCode();

            // Handle redirect code 301: Moved Permanently
            if (response / 100 == 3) {
                String redirectUrlString = conn.getHeaderField("Location");
                URL redirectUrl = new URL(redirectUrlString);
                conn = (HttpURLConnection) redirectUrl.openConnection();
                conn.addRequestProperty("Accept-Encoding", "identity");
                conn.addRequestProperty("User-Agent", System.getProperty("http.agent"));
                conn.connect();
            }

            inputStream = conn.getInputStream();

            // Decoding stream data back into image Bitmap that android understands.
            bitmap = BitmapFactory.decodeStream(inputStream);

        }catch (IOException e) {
            Log.e("processBitmap", "error " + e+" "+imageUrl);
            /**
             * Found the below errors while processing url.
             * FileNotFoundException,SocketTimeoutException,UnknownHostException
             */
            bitmap=null;
        }finally {
            /**
             * Makes sure that the InputStream is closed after the app is finished using it.
             */
            if (inputStream != null) {
                try{
                    inputStream.close();
                } catch (final IOException e) {}
            }
            return bitmap;
        }
    }

    public String getmUrl(){return mUrl;}

    /**
     * Interface definition for a callback to be invoked when a image download is finished.
     */
    public interface OnImageDownloadListener{
        public void onImageDownload(String url, BitmapDrawable drawable);
    }
}
