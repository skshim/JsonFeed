package com.example.skshim.jsonfeed.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.skshim.jsonfeed.model.Constants;
import com.example.skshim.jsonfeed.model.FeedResult;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sungki Shim on 2/08/15.
 *
 * AsyncTask that retrieves JSon data from Web.
 */
public class FactAsyncTask extends AsyncTask<String, Void, String> {

    private Context mContext;
    private OnFeedResultListener mFeedListener;
    private boolean error = true;

    public FactAsyncTask(Context context, OnFeedResultListener feedListener) {
        mContext = context;
        mFeedListener = feedListener;
    }


    @Override
    protected String doInBackground(String... urls) {

        // params comes from the execute() call: params[0] is the url.
        try {
            error = false;
            return downloadUrl(urls[0]);
        } catch (IOException e) {
            error = true;
            return "Unable to retrieve json data. URL may be invalid.";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (error) {
            Toast.makeText(mContext, result, Toast.LENGTH_LONG).show();
        }

        if (mFeedListener != null) {
            FeedResult feedResult = null;
            if (!error && !TextUtils.isEmpty(result)){
                feedResult = parseJsonFeed(result);
            }
            mFeedListener.onFeedResult(feedResult);
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
                try {inputStream.close();} catch (Exception e){};
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
        StringBuilder out = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        return out.toString();
    }

    // Convert string to FeedResult using GSon.
    private FeedResult parseJsonFeed(String json) {
        FeedResult feedResult = null;
        try {
            Gson gson = new Gson();
            feedResult = gson.fromJson(json, FeedResult.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }

        return feedResult;
    }

    /**
     * Interface definition for a callback to be invoked when a feed result is received.
     */
    public interface OnFeedResultListener {
        public void onFeedResult(FeedResult feedResult);
    }
}
