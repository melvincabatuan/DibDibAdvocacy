package com.cabatuan.breastfriend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by cobalt on 10/22/15.
 */
public class MyListAdapter extends BaseAdapter {

    private String [] result;
    private Context context;
    private int [] imageId;
    private static LayoutInflater inflater=null;

    public MyListAdapter(MainActivity mainActivity, String[] titleList, int[] images) {

        result=titleList;
        context=mainActivity;
        imageId=images;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.my_list_item, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textview);
        holder.img=(ImageView) rowView.findViewById(R.id.imageview);
        holder.tv.setText(result[position]);
        holder.img.setImageResource(imageId[position]);
        return rowView;
    }
}
