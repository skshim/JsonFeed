package com.example.skshim.jsonfeed;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;

/**
 * Created by Sungki Shim on 2/08/15.
*
 * MemoryCacheHolder stores an object of MemoryCache and retains it over configuration changes.
 */
public class MemoryCache<T, V> extends LruCache<T, V> {

    private static final String TAG = "MemoryCache";

    public MemoryCache(int maxSize){
        super(maxSize);
    }

    public static MemoryCache<String,BitmapDrawable> getInstance(FragmentManager fragmentManager, int maxSize){
        // Search for, or create an instance of the non-UI MemoryCacheHolder
        final MemoryCacheHolder memoryCacheHolder = findOrCreateMemoryCacheHolder(fragmentManager);

        // See if we already have an ImageCache stored in MemoryCacheHolder
        MemoryCache<String,BitmapDrawable> memoryCache = (MemoryCache) memoryCacheHolder.getObject();

        // Create one and store it in MemoryCacheHolder if not exist.
        if (memoryCache == null) {
            memoryCache = new MemoryCache(maxSize);
            memoryCacheHolder.setObject(memoryCache);
        }
        return memoryCache;
    }

    private static MemoryCacheHolder findOrCreateMemoryCacheHolder(FragmentManager fm) {
        // Check to see if we have retained the worker fragment.
        MemoryCacheHolder mHolder = (MemoryCacheHolder) fm.findFragmentByTag(TAG);

        // If not retained (or first time running), we need to create and add it.
        if (mHolder == null) {
            mHolder = new MemoryCacheHolder();
            fm.beginTransaction().add(mHolder, TAG).commitAllowingStateLoss();
        }
        return mHolder;
    }

    /**
     * A simple non-UI Fragment that stores a single Object and is retained over configuration
     * changes. It will be used to retain the MemoryCache object.
     */
    public static class MemoryCacheHolder extends Fragment {
        private Object mRestoredObject;

        public MemoryCacheHolder() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure this Fragment is retained over a configuration change
            setRetainInstance(true);
        }

        /**
         * Store a single object in this Fragment.
         *
         * @param object The object to store
         */
        public void setObject(Object object) {
            mRestoredObject = object;
        }

        /**
         * Get the stored object.
         *
         * @return The stored object
         */
        public Object getObject() {
            return mRestoredObject;
        }
    }
}
