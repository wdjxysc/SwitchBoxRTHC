package com.rthc.switchboxrthc.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.rthc.switchboxrthc.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/8/9.
 */
public class MeterInfoAdapter extends BaseAdapter {

    private Context context;

    /**
     * 气表的列表 每项有网络地址netid 表地址addressid
     */
    private List<HashMap<String,String>> meterlist;

    public MeterInfoAdapter(List<HashMap<String,String>> meterlist, Context context){
        this.meterlist = meterlist;
        this.context = context;
    }

    @Override
    public int getCount() {
        return meterlist.size();
    }

    @Override
    public Object getItem(int i) {
        return meterlist.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        // A ViewHolder keeps references to children views to avoid unneccessary calls
        // to findViewById() on each row.
        ViewHolder holder;

        // When convertView is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the convertView supplied
        // by ListView is null.
        if(view == null){
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            view = layoutInflater.inflate(R.layout.meter_info_item,null);
            // Creates a ViewHolder and store references to the two children views
            // we want to bind data to.
            holder = new ViewHolder();
//            holder.netidTextView = (TextView) view.findViewById(R.id.netidTextView);
//            holder.addressidTextView = (TextView) view.findViewById(R.id.meteradressTextView);

            view.setTag(holder);
        }else {
            // Get the ViewHolder back to get fast access to the TextView
            // and the ImageView.
            holder = (ViewHolder)view.getTag();
        }
        // Bind the data efficiently with the holder.
        holder.netidTextView.setText(meterlist.get(i).get("netid"));
        holder.addressidTextView.setText(meterlist.get(i).get("addressid"));


        return view;
    }


    static class ViewHolder {
        TextView netidTextView;
        TextView addressidTextView;
    }
}
