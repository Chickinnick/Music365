package com.divarc.music365.adapter;

import android.content.Context;
import android.media.tv.TvContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divarc.music365.R;
import com.divarc.music365.entity.Programmsstats;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

public class ProgramsAdapter extends ArrayAdapter<Programmsstats> {

    Context context;
    ViewHolder viewHolder;
    List<Programmsstats> programmsstatses;

    public ProgramsAdapter(Context context, List<Programmsstats> programmsstatses) {
        super(context, R.layout.item_channel, programmsstatses);
        this.programmsstatses = programmsstatses;
        this.context = context;
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Programmsstats getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(Programmsstats item) {
        return super.getPosition(item);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_programm, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.title = (TextView) convertView.findViewById(R.id.program_title_list);
            viewHolder.description = (TextView) convertView.findViewById(R.id.program_description_list);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.list_channel_image);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String textTitle = getItem(position).getPdescription();
        viewHolder.title.setText(textTitle.toUpperCase());

        String textDescription = getItem(position).getPtext();
        viewHolder.description.setText(textDescription);

        String imageLink = getItem(position).getPimagesrc();
        Log.d("adapter", textTitle + " " + textDescription + " " + imageLink);
        Glide.with(context).load(imageLink).into(viewHolder.imageView);

        convertView.setTag(viewHolder);
       ProjectViewController.getInstance().integerViewHashMap.put(textTitle, convertView);
        return convertView;
    }

    static class ViewHolder {

        TextView title;
        TextView description;
        ImageView imageView;



    }

    public View getViewByName(String name) {
        //ProjectViewController.getInstance().integerViewHashMap.get(name);
        for(int i=0 ;i<programmsstatses.size() ; i++){
            if(programmsstatses.get(i).getPdescription().equals(name)){
                return getView(i, null, null);
            }
       }
        return null;
    }
}
