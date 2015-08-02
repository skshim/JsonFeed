package com.example.skshim.jsonfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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
            refresh();
        }else{
            // Restore saved instance state
            mTitle=savedInstanceState.getString(Constants.BUNDLE_KEY_TITLE);
            mFactList=savedInstanceState.getParcelableArrayList(Constants.BUNDLE_KEY_FACTLIST);

            mActionBar.setTitle(mTitle);
            mFactAdapter.setItemList(mFactList);
        }
    }

    private void refresh(){
        new JSonFeedTask().execute(Constants.FEED_URL);
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

    /**
     * JSonFeedTask : AsyncTask that retrieves JSon data from Web.
     */
    private class JSonFeedTask extends AsyncTask<String,Void,String> {

        private boolean error=true;

        /**
         * Runs on the UI thread before {@link #doInBackground}.
         *
         * @see #onPostExecute
         * @see #doInBackground
         */
        @Override
        protected void onPreExecute() {
            if(!mSwiptLayout.isRefreshing()){
                mSwiptLayout.setRefreshing(true);
            }
        }

        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                error=false;
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve json data. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if(error){
                Toast.makeText(FactListActivity.this, result, Toast.LENGTH_LONG)
                        .show();
            }else{
                // Convert string to mFactList and provide it to FactAdapter.
                displayFeed(result);
            }

            if(mSwiptLayout.isRefreshing()){
                mSwiptLayout.setRefreshing(false);
            }
        }

        @Override
        protected void onCancelled(String s) {
            // Stop the refreshing indicator or progress dialog
            if(mSwiptLayout.isRefreshing()){
                mSwiptLayout.setRefreshing(false);
            }
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the json content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        InputStream inputStream = null;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(Constants.TIME_OUT /* milliseconds */);
            conn.setConnectTimeout(Constants.TIME_OUT /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();

            inputStream = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(inputStream);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder out = new StringBuilder();
        String line;
        while((line=reader.readLine()) != null){
            out.append(line);
        }
        return out.toString();
    }

    // Convert string to mFactList and provide it to FactAdapter.
    private void displayFeed(String result){
        JSONObject responseObj=null;
        try{
            Gson gson=new Gson();
            responseObj=new JSONObject(result);
            mTitle=responseObj.getString("title");
            JSONArray rowItems=responseObj.getJSONArray("rows");

            // Update title from json.
            mActionBar.setTitle(mTitle);

            mFactList =new ArrayList<Fact>();
            Fact rowItem;
            for(int i=0; i<rowItems.length(); i++){
                rowItem = gson.fromJson(rowItems.getJSONObject(i).toString(),Fact.class);

                if(!rowItem.isNull()){
                    mFactList.add(rowItem);
                }
            }

            // Set FactList to FactAdapter
            mFactAdapter.setItemList(mFactList);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }


}
