package com.example.skshim.jsonfeed.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.example.skshim.jsonfeed.R;
import com.example.skshim.jsonfeed.adapter.FactAdapter;
import com.example.skshim.jsonfeed.asynctask.FactAsyncTask;
import com.example.skshim.jsonfeed.model.Constants;
import com.example.skshim.jsonfeed.model.Fact;
import com.example.skshim.jsonfeed.model.FeedResult;

import java.util.ArrayList;


public class FactListActivity extends AppCompatActivity implements  SwipeRefreshLayout.OnRefreshListener{

    private SwipeRefreshLayout mSwiptLayout;
    private ArrayList<Fact> mFactList;
    private FactAdapter mFactAdapter;
    private ActionBar mActionBar;
    private  String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_feed);

        // Set Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar=getSupportActionBar();

        // Config swip layout
        mSwiptLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwiptLayout.setOnRefreshListener(this);
        mSwiptLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // Set FactAdapter to ListView
        mFactAdapter=new FactAdapter(this,R.id.listView, mFactList);
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(mFactAdapter);

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

            mActionBar.setTitle(mTitle);
            mFactAdapter.setItemList(mFactList);
        }
    }

    private void refresh(){
        if(!mSwiptLayout.isRefreshing()){
            mSwiptLayout.setRefreshing(true);
        }
        mActionBar.setTitle(R.string.loading);
        new FactAsyncTask(this, new FactAsyncTask.OnFeedResultListener() {
            @Override
            public void onFeedResult(FeedResult feedResult) {
                displayFeed(feedResult);
                mSwiptLayout.setRefreshing(false);
            }
        }).execute(Constants.FEED_URL);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    /**
     * Save all appropriate fragment state.
     *
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.BUNDLE_KEY_TITLE, mTitle);
        outState.putParcelableArrayList(Constants.BUNDLE_KEY_FACTLIST, mFactList);
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
