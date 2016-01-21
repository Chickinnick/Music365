package com.divarc.music365.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divarc.music365.R;
import com.divarc.music365.entity.Channel;
import com.divarc.music365.entity.Day;
import com.divarc.music365.entity.Program;
import com.divarc.music365.fragments.ChannelsFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ChannelsAdapter extends ArrayAdapter<Channel> {

    List<Channel> channels;
    Context context;
    ViewHolder viewHolder;
View.OnClickListener onClickListener;

    public ChannelsAdapter(Context context, List<Channel> channels, ChannelsFragment channelsFragment) {
        super(context, R.layout.item_channel, channels);
        this.channels = channels;
        this.context = context;
        onClickListener = channelsFragment;
        Log.d("adapter", "constrctor");
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public Channel getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public int getPosition(Channel item) {
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
            convertView = inflater.inflate(R.layout.item_channel, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.title = (TextView) convertView.findViewById(R.id.cahnnel_title);
            viewHolder.description = (TextView) convertView.findViewById(R.id.channel_description);
            viewHolder.imageView = (ImageView) convertView.findViewById(R.id.channel_image);
            viewHolder.playView = (ImageView) convertView.findViewById(R.id.circle_play);
            viewHolder.infoBtn = (Button) convertView.findViewById(R.id.info_program_btn);
            viewHolder.nowProgramTime = (TextView) convertView.findViewById(R.id.time_program);
            viewHolder.nowProgramTitle = (TextView) convertView.findViewById(R.id.program_title);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        String textTitle = getItem(position).getTitle().toUpperCase();
        viewHolder.title.setText(textTitle);

        String textDescription = getItem(position).getDescription().getText();
        viewHolder.description.setText(textDescription);


        String imageLink = getItem(position).getDescription().getImage();
        Log.d("adapter", textTitle + " " + textDescription + " " + imageLink);
        Glide.with(context).load(imageLink).into(viewHolder.imageView);


        Calendar calendar = Calendar.getInstance();
        ArrayList<Day> days = getItem(position).getDays();
        Day day = days.get(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        ArrayList<Program> programs = day.getPrograms();

        String textTimeRaw = programs.get(getCurrentProgrammIndex(programs)).getTime();
        String textTime = textTimeRaw.substring(0, 2).concat(":").concat(textTimeRaw.substring(2, 4));
        ;
        viewHolder.nowProgramTime.setText(textTime);

        String name = programs.get(getCurrentProgrammIndex(programs)).getTitle();
        viewHolder.nowProgramTitle.setText(name);


        viewHolder.infoBtn.setTag(position);
        viewHolder.infoBtn.setOnClickListener(onClickListener);
        convertView.setTag(viewHolder);
        return convertView;
    }


    static class ViewHolder {

        TextView title;
        TextView description;
        ImageView imageView;
        ImageView playView;


        TextView nowProgramTime;
        TextView nowProgramTitle;

        Button infoBtn;

    }

    public int getCurrentProgrammIndex(ArrayList<Program> programs) {
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("HHmm");
        String formattedDate = df.format(calendar.getTime());
      int currentTime = Integer.parseInt(formattedDate);


        ArrayList<Integer> times = new ArrayList<>();

        for (Program program : programs) {
            times.add(Integer.valueOf(program.getTime()));
        }
        Integer[] ints = times.toArray(new Integer[times.size()]);

        int min = ints[0];
        int max = ints[0];

        for (int i = 0; i < ints.length; i++) {

            if(ints[i]>max){
                max = ints[i];
            }
            if (ints[i] < min) {
                min = ints[i];
            }
        }


        for (int i = 0; i < programs.size() - 1; i++) {


            int programmTime = Integer.parseInt(programs.get(i).getTime());
//            int previousProgrammTime = Integer.parseInt(programs.get(i - 1).getTime());
            int nextProgrammTime = Integer.parseInt(programs.get(i + 1).getTime());

            boolean isNextProgrammIsLast = (i + 1) == programs.size()-1;



            if(currentTime < min  ){
                if(min  == programmTime)
                return i-1;
            }
            if(currentTime > max  ){
                if(max  == programmTime)
                return i;
            }
            if(currentTime>=programmTime && currentTime< nextProgrammTime){
                return i;
            }

            if (currentTime > programmTime && isNextProgrammIsLast) {
                return i+1;
            }

        }
        return 0;
    }

    public List<Channel> getChannels() {
        return channels;
    }
}
