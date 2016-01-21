package com.divarc.music365.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;

import com.divarc.music365.MainActivity;
import com.divarc.music365.R;
import com.divarc.music365.adapter.ProgramsAdapter;
import com.divarc.music365.entity.Datachannel;


public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {


    ListView programsListView;
    ProgramsAdapter programsAdapter;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    Datachannel datachannel;
    MainActivity mainActivity;
    public static final String MESSAGE_PREF= "message_pref";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getActivity().getSharedPreferences(MESSAGE_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static final String KEY_KEEP_CONN = "keep_conn";
    public static final String KEY_PLAY_IN_BG = "play_in_bg";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);


        mainActivity  = (MainActivity) getActivity();
        datachannel = mainActivity.datachannel;

View contol = view.findViewById(R.id.adcontrol);
contol.setVisibility(mainActivity.mIsPremium? View.GONE : View.VISIBLE);
        SwitchCompat aSwitch1 = (SwitchCompat) view.findViewById(R.id.switch1);
        SwitchCompat aSwitch2 = (SwitchCompat) view.findViewById(R.id.switch2);

        aSwitch1.setOnCheckedChangeListener(this);
        aSwitch2.setOnCheckedChangeListener(this);

        aSwitch1.setChecked(sharedPreferences.getBoolean(KEY_PLAY_IN_BG,false ));
        aSwitch2.setChecked(sharedPreferences.getBoolean(KEY_KEEP_CONN,false ));


        return view;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.switch1: editor.putBoolean(KEY_PLAY_IN_BG, isChecked).commit();  break;
            case R.id.switch2: editor.putBoolean(KEY_KEEP_CONN, isChecked).commit();  break;
        }
    }
}
