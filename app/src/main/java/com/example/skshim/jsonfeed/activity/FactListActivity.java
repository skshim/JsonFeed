package com.example.skshim.jsonfeed.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.Toast;

import com.example.skshim.jsonfeed.R;
import com.example.skshim.jsonfeed.adapter.FactAdapter;
import com.example.skshim.jsonfeed.asynctask.FactAsyncTask;
import com.example.skshim.jsonfeed.model.Constants;
import com.example.skshim.jsonfeed.model.Fact;
import com.example.skshim.jsonfeed.model.FeedResult;

import java.util.ArrayList;


public class FactListActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwiptLayout;
    private ArrayList<Fact> mFactList;
    private FactAdapter mFactAdapter;
    private ActionBar mActionBar;
    private String mTitle;
    private FactAsyncTask mFactAsyncTask;
    private FactAsyncTask.OnFeedResultListener mOnFeedResultListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_feed);

        // Set Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar=getSupportActionBar();

        // Config swap layout
        mSwiptLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwiptLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mSwiptLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Set FactAdapter to ListView
        mFactAdapter=new FactAdapter(this,R.id.listView, mFactList);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(mFactAdapter);

        // Create OnFeedResultListener
        mOnFeedResultListener=new FactAsyncTask.OnFeedResultListener() {
            @Override
            public void onFeedResult(FeedResult feedResult) {
                // Disconnect as all work has been done.
                mFactAsyncTask.disconnect();
                mFactAsyncTask = null;

                displayFeed(feedResult);
                mSwiptLayout.setRefreshing(false);
            }
        };

        if(savedInstanceState==null){
            // A little bit of delay will enable refreshing animation when launch the app.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }, Constants.LOADING_DELAY);
        }else{
            // Restore saved instance state
            mTitle=savedInstanceState.getString(Constants.BUNDLE_KEY_TITLE);
            mFactList=savedInstanceState.getParcelableArrayList(Constants.BUNDLE_KEY_FACTLIST);

            // Restore saved AsyncTask
            mFactAsyncTask =(FactAsyncTask)getLastCustomNonConfigurationInstance();
            if(mFactAsyncTask !=null){
                /**
                 * If FactAsyncTask is still in progress,
                 * it should be reconnected with new activity and OnFeedResultListener.
                 */
                mFactAsyncTask.connect(this, mOnFeedResultListener);
            }

            if(mTitle==null){
                mActionBar.setTitle(R.string.loading);
            }else{
                mActionBar.setTitle(mTitle);
                mFactAdapter.setItemList(mFactList);
            }
        }
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        // Return FactAsnycTask. It will be used when rotate screen if it is still in progress.
        return mFactAsyncTask;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BUNDLE_KEY_TITLE, mTitle);
        outState.putParcelableArrayList(Constants.BUNDLE_KEY_FACTLIST, mFactList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFactAsyncTask !=null){
            // Disconnect if still in progress.
            mFactAsyncTask.disconnect();
        }
    }

    private void refresh() {
        // Refresh only when network is connected.
        if (isConnectedToInternet()) {
            if (!mSwiptLayout.isRefreshing()) {
                mSwiptLayout.setRefreshing(true);
            }
            mActionBar.setTitle(R.string.loading);

            // Create FactAsnycTask and connect with Activity and OnFeedResultListener
            mFactAsyncTask=new FactAsyncTask();
            mFactAsyncTask.connect(this,mOnFeedResultListener);
            mFactAsyncTask.execute(Constants.FEED_URL);
        }else{
            // Otherwise show display message.
            if (mSwiptLayout.isRefreshing()) {
                mSwiptLayout.setRefreshing(false);
            }
            mActionBar.setTitle(R.string.no_internet_title);
            Toast.makeText(this,R.string.no_internet,Toast.LENGTH_LONG)
                    .show();
        }
    }

    // Check if network is available.
    private boolean isConnectedToInternet(){
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Provide feed result to FactAdapter.
    private void displayFeed(FeedResult feedResult){

        if (feedResult == null) {
            // Restore title if can't get the result.
            mActionBar.setTitle(mTitle);
            return;
        }

        // Update ActionBar title as per requirement.
        mTitle = feedResult.getTitle();
        mActionBar.setTitle(mTitle);

        if (feedResult.getRows() != null) {
            mFactList = feedResult.getRows();
            mFactAdapter.setItemList(mFactList);
        }
    }
}
