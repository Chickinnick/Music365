package com.divarc.music365.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.divarc.music365.R;

import java.util.List;


public class VolumeControlAdapter extends ArrayAdapter<String> {
    Context context;


    int[] colors = {
            R.color.volume_bar_0,
            R.color.volume_bar_1,
            R.color.volume_bar_2,
            R.color.volume_bar_3,
            R.color.volume_bar_4,
            R.color.volume_bar_5,
            R.color.volume_bar_6,
            R.color.volume_bar_7

    };
    public VolumeControlAdapter(Context context, int resource, List<String> objects) {
        super(context, resource, objects);
        this.context = context;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view ==null){
            view = View.inflate(context, R.layout.volume_item, null);

        }

        TextView textView = (TextView) view.findViewById(R.id.item_vol);
        textView.setText(" ");

        textView.setBackgroundColor(context.getResources().getColor(colors[position]));
        return view;
    }
}
