package com.divarc.music365.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divarc.music365.MainActivity;
import com.divarc.music365.R;
import com.divarc.music365.adapter.ChannelsAdapter;
import com.divarc.music365.entity.Channel;
import com.divarc.music365.entity.Datachannel;
import com.divarc.music365.entity.Message;

import java.util.List;


public class ChannelsFragment extends Fragment implements  AdapterView.OnItemClickListener, View.OnClickListener {

    ListView channelsListView;
    ChannelsAdapter channelsAdapter;
    SharedPreferences sharedPreferences;

    int prefsValue =-1;
    int oldPrefsValue =-1;
    Datachannel datachannel;
    MainActivity mainActivity;
    public static final String MESSAGE_PREF= "message_pref";

    BroadcastFragment broadcastFragment;
   ProgrammsFragment  programmsFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(MESSAGE_PREF, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channels, container, false);


        mainActivity  = (MainActivity) getActivity();
         datachannel = mainActivity.datachannel;
        channelsListView = (ListView) view.findViewById(R.id.channels_list);
        List<Channel> channelList =    datachannel.getChannels();
        oldPrefsValue = sharedPreferences.getInt(MESSAGE_PREF, -1);

        Message message = datachannel.getMessage();
        sharedPreferences.edit().putInt(MESSAGE_PREF, message.getId()).apply();

        prefsValue = message.getId();
        Log.d("Message", message.toString());
         ViewGroup viewGroup = (ViewGroup) view.findViewById(R.id.message_layout);
        viewGroup.setVisibility(View.GONE);

         if(prefsValue > oldPrefsValue) {
             initMessage(message, view);
         }

         channelsAdapter = new ChannelsAdapter(mainActivity, channelList, this);
         channelsListView.setAdapter(channelsAdapter);
        channelsListView.setOnItemClickListener(this);
        broadcastFragment = new BroadcastFragment();
        return view;
    }



    private void initMessage(Message message, View view) {
        ViewGroup viewGroup = (ViewGroup)      view.findViewById(R.id.message_layout);
        viewGroup.setVisibility(View.VISIBLE);
        ImageView messageImage = (ImageView)   view.findViewById(R.id.message_image);
        ImageView messageCrossBtn = (ImageView)view.findViewById(R.id.cross_btn);
        TextView messageTextView = (TextView)  view.findViewById(R.id.message_text);
        Button buttonMore= (Button)            view.findViewById(R.id.button_more);

        messageTextView.setText(message.getText());
        Glide.with(this).load(message.getImage()).into(messageImage);

        messageCrossBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup viewGroup = (ViewGroup) mainActivity.findViewById(R.id.message_layout);
                viewGroup.setVisibility(View.GONE);
            }
        });

    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {



        Log.d("tag", position + "  " );
        broadcastFragment.setIndexOfChannel(position);
        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, broadcastFragment).commit();
    }

    @Override
    public void onClick(View v) {
        Log.d("ONCLICK",v.getTag()+"");
        programmsFragment = new ProgrammsFragment();

        mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,programmsFragment).commit();
     //   programmsFragment.setIndexOfChannel((Integer) v.getTag());
    }
}
