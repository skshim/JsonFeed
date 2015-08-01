package com.example.skshim.jsonfeed;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.List;


public class JsonFeedActivity extends AppCompatActivity {

    private ArrayList<Fact> mFactList;
    private FactAdapter mFactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_json_feed);

        mFactAdapter=new FactAdapter(this,R.id.listView, mFactList);

        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(mFactAdapter);

        refresh();
    }

    private void refresh(){
        String url="https://dl.dropboxusercontent.com/u/746330/facts.json";
        new JSonFeedTask().execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_json_feed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * FactAdapter : The main adapter that backs the ListView.
     */
    private class FactAdapter extends ArrayAdapter<Fact> {

        //
        private class ViewHolder{
            TextView title;
            TextView description;
            ImageView imageView;
        }

        private List<Fact> mItemList;

        public FactAdapter(Context context, int resource, ArrayList<Fact> facts){
            super(context,resource,facts);
            mItemList=facts;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCount() {
            if(mItemList==null){
                return 0;
            }else{
                return mItemList.size();
            }
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         */
        @Override
        public Fact getItem(int position) {
            return mItemList.get(position);
        }

        /**
         * {@inheritDoc}
         *
         * @param position
         */
        @Override
        public long getItemId(int position) {
            if(mItemList==null){
                return 0;
            }else{
                return getItem(position).hashCode();
            }
        }

        /**
         *  JSon data should be converted into List<Fact> and passed to function
         *
         * @param itemList
         */
        public void setItemList(List<Fact> itemList) {
            mItemList = itemList;
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView ==null){
                LayoutInflater layInf=(LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layInf.inflate(R.layout.item_fact,null);

                holder = new ViewHolder();
                holder.title=(TextView)convertView.findViewById(R.id.title);
                holder.description=(TextView)convertView.findViewById(R.id.description);
                holder.imageView =(ImageView)convertView.findViewById(R.id.imageView);

                convertView.setTag(holder);

            }else{
                holder =(ViewHolder) convertView.getTag();
            }

            Fact fact = getItem(position);
            holder.title.setText((fact.getTitle() != null ? fact.getTitle() : ""));
            holder.description.setText((fact.getDescription()!=null?fact.getDescription():""));
            if(fact.getImageHref()==null){
                holder.imageView.setVisibility(View.GONE);
            }else{
                // load the imageView asynchronously into the ImageView
                holder.imageView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }

    /**
     * JSonFeedTask : AsyncTask that retrieves JSon data from Web.
     */
    private class JSonFeedTask extends AsyncTask<String,Void,String> {

        private ProgressDialog dialog = new ProgressDialog(JsonFeedActivity.this);
        private boolean error=true;

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Downloading ...");
            dialog.show();
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
            dialog.dismiss();

            if(error){
                Toast.makeText(JsonFeedActivity.this, result, Toast.LENGTH_LONG)
                        .show();
            }else{
                // Convert string to mFactList and provide it to FactAdapter.
                displayFeed(result);
            }
        }

        @Override
        protected void onCancelled(String s) {
            dialog.dismiss();
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
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
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
            String mainTitle=responseObj.getString("title");
            JSONArray rowItems=responseObj.getJSONArray("rows");

            mFactList =new ArrayList<Fact>();
            Fact rowItem;
            for(int i=0; i<rowItems.length(); i++){
                rowItem = gson.fromJson(rowItems.getJSONObject(i).toString(),Fact.class);

                if(!rowItem.isNull()){
                    mFactList.add(rowItem);
                }
            }

            mFactAdapter.setItemList(mFactList);

        }catch (JSONException e){
            e.printStackTrace();
        }
    }


}
