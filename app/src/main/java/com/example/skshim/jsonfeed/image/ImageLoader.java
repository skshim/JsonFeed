package com.example.skshim.jsonfeed.image;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentManager;
import android.widget.ImageView;

import com.example.skshim.jsonfeed.asynctask.ImageAsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by Sungki Shim on 1/08/15.
 *
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory cache, running the work in a background
 * thread and setting a placeholder image.
 */
public class ImageLoader {

    private Resources mResources;
    private Bitmap mLoadingBitmap;
    private MemoryCache<String, BitmapDrawable> mMemoryCache;

    public ImageLoader(Context context){
        mResources = context.getResources();
    }

    public void addMemoryCache(FragmentManager fragmentManager, int maxSize){
        mMemoryCache=MemoryCache.getInstance(fragmentManager, maxSize);
    }

    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    public void loadImage(String url, ImageView imageView) {

        BitmapDrawable bitmap=null;

        // Check whether image is cached in memory.
        if(mMemoryCache!=null){
            bitmap = mMemoryCache.get(url);
        }

        // Use cached image if exist.
        if(bitmap!=null){
            imageView.setImageDrawable(bitmap);

        }else{
            // Download from url otherwise.
            final ImageAsyncTask imageTask = new ImageAsyncTask(mResources,imageView,
                    new ImageAsyncTask.OnImageDownloadListener(){
                        @Override
                        public void onImageDownload(String url, BitmapDrawable drawable) {
                            if(url!=null && drawable!=null && mMemoryCache!=null){
                                mMemoryCache.put(url,drawable);
                            }

                        }
                    });

            // Make a link between imageView and task that returns bitmap drawable.
            final DrawableWithAsyncTask drawableWithAsyncTask=
                    new DrawableWithAsyncTask(mResources, mLoadingBitmap, imageTask);
            imageView.setImageDrawable(drawableWithAsyncTask);

            imageTask.execute(url);
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that only the last started worker process
     * can bind its result, independently of the finish order.
     */
    public static class DrawableWithAsyncTask extends BitmapDrawable {
        private final WeakReference<ImageAsyncTask> bitmapWorkerTaskReference;

        public DrawableWithAsyncTask(Resources res, Bitmap bitmap, ImageAsyncTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<ImageAsyncTask>(bitmapWorkerTask);
        }

        public ImageAsyncTask getWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

}
