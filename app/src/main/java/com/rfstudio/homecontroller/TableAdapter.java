package com.rfstudio.homecontroller;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by Raveen on 9/9/2015.
 */
public class TableAdapter extends RecyclerView.Adapter<TableAdapter.TableViewHolder> {

    private int length;
    private ArrayList<String> commandNames;
    private ArrayList<String> commandNos;

    private TaskSchedulerActivity task;

    public TableAdapter(Context context, ArrayList<String> commands, ArrayList<String> commandNos, TaskSchedulerActivity activity)
    {
        task = activity;
        commandNames = commands;
        this.commandNos = commandNos;
        length = commandNames.size();
    }

    @Override
    public TableViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.table_single_row, parent, false);
        TableViewHolder tableViewHolder = new TableViewHolder(linearLayout);
        return tableViewHolder;
    }

    @Override
    public void onBindViewHolder(TableViewHolder tableViewHolder, int i)
    {
        if(task.checkedOnStatus.get(i))
            tableViewHolder.setData(i, commandNames, commandNos, true, false, false);
        else if(task.checkedOffStatus.get(i))
            tableViewHolder.setData(i, commandNames, commandNos, false, true, false);
        else if(task.checkedNoneStatus.get(i))
            tableViewHolder.setData(i, commandNames, commandNos, false, false, true);

    }

    @Override
    public int getItemCount() { return length; }

    public static class TableViewHolder extends RecyclerView.ViewHolder
    {
        public TextView command;
        public RadioButton onButton, offButton, noneButton;

        public TableViewHolder(LinearLayout commandList)
        {
            super(commandList);
            command = (TextView) commandList.findViewById(R.id.table_text_id);
            onButton = (RadioButton) commandList.findViewById(R.id.table_on);
            offButton = (RadioButton) commandList.findViewById(R.id.table_off);
            noneButton = (RadioButton) commandList.findViewById(R.id.table_none);
        }

        public void setData(int position, ArrayList<String> commandNames, ArrayList<String> commandNos, boolean on, boolean off, boolean none)
        {
            command.setText(commandNames.get(position).toString());

            onButton.setTag(position);
            offButton.setTag(position);
            noneButton.setTag(position);

            onButton.setChecked(on);
            offButton.setChecked(off);
            noneButton.setChecked(none);
        }
    }
}
