package com.example.skshim.jsonfeed;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sungki Shim on 1/08/15.
 *
 * FactAdapter : The main adapter that backs the ListView.
 */
public class FactAdapter extends ArrayAdapter<Fact> {

    //
    private class ViewHolder{
        TextView title;
        TextView description;
        ImageView imageView;
    }

    private List<Fact> mItemList;
    private Context mContext;
    private ImageLoader mImageLoader;

    public FactAdapter(Context context, int resource, ArrayList<Fact> facts){
        super(context,resource,facts);
        mContext=context;
        mItemList=facts;
        mImageLoader= new ImageLoader(context);

        // Use 20% of available heap size as memory cache
        int maxSize = (int)Runtime.getRuntime().maxMemory()/5;
        mImageLoader.addMemoryCache(((FactListActivity)context).getSupportFragmentManager(),maxSize);
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
            LayoutInflater layInf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
            // remove from the list if image link doesn't exist
            holder.imageView.setVisibility(View.GONE);
        }else{
            holder.imageView.setVisibility(View.VISIBLE);
            // load the imageView asynchronously into the ImageView
            mImageLoader.loadImage(fact.getImageHref(),holder.imageView);
        }

        return convertView;
    }
}
