package com.divarc.music365.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.divarc.music365.R;
import com.divarc.music365.entity.Day;
import com.divarc.music365.entity.Program;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListLaterAdapter extends BaseAdapter implements View.OnClickListener {

    Context context;
    ViewHolder viewHolder;
    List<Day> days;
    List<Program> trimmedProgs;
    ProgramsAdapter programsAdapter;


    public ListLaterAdapter(Context context, ArrayList<Day> programmsstatses, ProgramsAdapter programsAdapter) {
        days = programmsstatses;
        Calendar calendar = Calendar.getInstance();
        Day day = days.get(calendar.get(Calendar.DAY_OF_WEEK) - 1);
        ArrayList<Program> programs = day.getPrograms();
        trimmedProgs = programs.subList(getCurrentProgrammIndex(programs) + 1, programs.size());
        this.programsAdapter = programsAdapter;
        this.context = context;
    }

    @Override
    public int getCount() {
        return trimmedProgs.size();
    }

    @Override
    public Program getItem(int position) {
        return trimmedProgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_list_programs_later, parent, false);
            viewHolder = new ViewHolder();

            viewHolder.title = (TextView) convertView.findViewById(R.id.time_program_later);
            viewHolder.description = (TextView) convertView.findViewById(R.id.program_title_later);
            viewHolder.buttonInfo = (Button) convertView.findViewById(R.id.info_program_btn_later);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        Program program = trimmedProgs.get(position);
        String textTime = program.getTime().substring(0, 2).concat(":").concat(program.getTime().substring(2, 4));

        viewHolder.title.setText(textTime);
        viewHolder.description.setText(program.getTitle());

        viewHolder.buttonInfo.setOnClickListener(this);
        viewHolder.buttonInfo.setTag(program.getTitle());
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public void onClick(View v) {

        String name = (String) v.getTag();
        View view = getViewByName(name);

        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
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

    private View getViewByName(String name) {

        return programsAdapter.getViewByName(name);
    }

    ;


    static class ViewHolder {

        TextView title;
        TextView description;
        Button buttonInfo;

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

            if (ints[i] > max) {
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

            boolean isNextProgrammIsLast = (i + 1) == programs.size() - 1;


            if (currentTime < min) {
                if (min == programmTime)
                    return i - 1;
            }
            if (currentTime > max) {
                if (max == programmTime)
                    return i;
            }
            if (currentTime >= programmTime && currentTime < nextProgrammTime) {
                return i;
            }

            if (currentTime > programmTime && isNextProgrammIsLast) {
                return i + 1;
            }

        }
        return 0;
    }


}
