package com.example.skshim.jsonfeed;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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


}
