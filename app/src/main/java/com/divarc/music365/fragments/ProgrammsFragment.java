package com.divarc.music365.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.divarc.music365.MainActivity;
import com.divarc.music365.R;
import com.divarc.music365.adapter.ProgramsAdapter;
import com.divarc.music365.entity.Channel;
import com.divarc.music365.entity.Datachannel;
import com.divarc.music365.entity.Programmsstats;

import java.util.List;


public class ProgrammsFragment extends Fragment {


    ListView programsListView;
    ProgramsAdapter programsAdapter;
    SharedPreferences sharedPreferences;

    Datachannel datachannel;
    MainActivity mainActivity;
    public static final String MESSAGE_PREF= "message_pref";
    public int indexOfChannel;
    List<Channel> channelList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(MESSAGE_PREF, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_programs, container, false);


        mainActivity  = (MainActivity) getActivity();
        datachannel = mainActivity.datachannel;
        programsListView = (ListView) view.findViewById(R.id.programs_list);


        channelList = datachannel.getChannels();

        List<Programmsstats> programmsstatses =    channelList.get(indexOfChannel).getProgrammsstatses();

 programsAdapter= new ProgramsAdapter(mainActivity, programmsstatses);
        programsListView.setAdapter(programsAdapter);


        return view;
    }


    public int getIndexOfChannel() {
        return indexOfChannel;
    }

    public void setIndexOfChannel(int indexOfChannel) {
        this.indexOfChannel = indexOfChannel;
        List<Programmsstats> programmsstatses =    channelList.get(indexOfChannel).getProgrammsstatses();

        programsAdapter= new ProgramsAdapter(mainActivity, programmsstatses);
        programsAdapter.notifyDataSetChanged();
        Log.d("PROGRAMMS", "notify" + indexOfChannel);
    }
}
