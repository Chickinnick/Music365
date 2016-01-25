package com.divarc.music365.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.divarc.music365.MainActivity;
import com.divarc.music365.R;
import com.divarc.music365.adapter.ListLaterAdapter;
import com.divarc.music365.adapter.ProgramsAdapter;
import com.divarc.music365.entity.Datachannel;
import com.divarc.music365.entity.Day;
import com.divarc.music365.entity.Program;
import com.divarc.music365.entity.Programmsstats;

import java.util.ArrayList;
import java.util.Calendar;


public class BroadcastFragment extends Fragment implements View.OnClickListener {

    SharedPreferences sharedPreferences;

    Datachannel datachannel;
    MainActivity mainActivity;
    public static final String MESSAGE_PREF = "message_pref";
    ListView listViewLater;
    ListLaterAdapter listLaterAdapter;
    int indexOfChannel;
    PlayerFragment playerFragment;
    public String urlTag;
    private ProgramsAdapter programsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(MESSAGE_PREF, Context.MODE_PRIVATE);
        playerFragment = new PlayerFragment();
        mainActivity = (MainActivity) getActivity();
        datachannel = mainActivity.datachannel;

        if (datachannel != null) {
            urlTag = datachannel.getChannels().get(indexOfChannel).getStream();
            String NAME = datachannel.getChannels().get(indexOfChannel).getTitle();
            mainActivity.setChannelTitle(NAME);
            Log.d("BROADCAST", urlTag + " " + NAME);
           // listLaterAdapter = new ListLaterAdapter(mainActivity, datachannel.getChannels().get(indexOfChannel).getDays(), programsAdapter);
        //    listViewLater.setAdapter(listLaterAdapter);

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_broadcast, container, false);


        ArrayList<Programmsstats> programmsstats = datachannel.getChannels().get(indexOfChannel).getProgrammsstatses();
        programsAdapter = new ProgramsAdapter(mainActivity, programmsstats);
        listViewLater = (ListView) view.findViewById(R.id.watch_later);
        listLaterAdapter = new ListLaterAdapter(mainActivity, datachannel.getChannels().get(indexOfChannel).getDays(), programsAdapter);
        listViewLater.setAdapter(listLaterAdapter);
        urlTag = datachannel.getChannels().get(indexOfChannel).getStream();

        String NAME = datachannel.getChannels().get(indexOfChannel).getTitle();
        Log.d("BROADCAST", urlTag + " " + NAME);

        ImageView fullscreen = (ImageView) view.findViewById(R.id.fullscreen);
        fullscreen.setTag(urlTag);
        fullscreen.setOnClickListener(playerFragment);

        mainActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.video_frame, playerFragment, urlTag).commit();

        Calendar calendar = Calendar.getInstance();
        Day day = datachannel.getChannels().get(indexOfChannel).getDays().get(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        ArrayList<Program> programs = day.getPrograms();
        Program program = programs.get(listLaterAdapter.getCurrentProgrammIndex(programs));
        TextView timeCurrenttextView = (TextView) view.findViewById(R.id.time_program_current);
        TextView titleCurrenttextView = (TextView) view.findViewById(R.id.program_title_current);
        Button infoCurrenttextView = (Button) view.findViewById(R.id.info_program_btn);
        infoCurrenttextView.setOnClickListener(this);
        infoCurrenttextView.setTag(program.getTitle());
        String textTime = program.getTime().substring(0, 2).concat(":").concat(program.getTime().substring(2, 4));
        timeCurrenttextView.setText(textTime);
        titleCurrenttextView.setText(program.getTitle());

        return view;
    }


    public void setIndexOfChannel(int position) {
        Log.d("BROADCAST", "index" + position);
        indexOfChannel = position;

    }

    public int getIndexOfChannel() {
        return indexOfChannel;
    }


    @Override
    public void onClick(View v) {

        String name =  v.getTag().toString();
        Log.d("name", name);
        View view = programsAdapter.getViewByName(name);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
        builder
                .setView(view)
                .setCancelable(false)
                .setNegativeButton("закрыть",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();

    }
}

