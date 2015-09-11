package com.rfstudio.homecontroller;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class TaskSchedulerActivity extends AppCompatActivity {

    private Activity myActivity;

    private int year_x, month_x, day_x;
    private int hour_x, minute_x;

    private TextView textDate, textTime;
    private RecyclerView recyclerView;
    private Spinner spinner;

    protected ArrayList<Boolean> checkedOnStatus;
    protected ArrayList<Boolean> checkedOffStatus;
    protected ArrayList<Boolean> checkedNoneStatus;

    private ArrayList<String> commands;
    private ArrayList<String> commandNos;
    private HelperDataClass helperDataClass;
    private HelperClass helperClass;

    private TableAdapter tableAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_scheduler);

        myActivity=this;

        helperDataClass = (HelperDataClass) getIntent().getSerializableExtra("helper");
        helperClass = new HelperClass(this, helperDataClass);

        Calendar cal = Calendar.getInstance();
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        hour_x = cal.get(Calendar.HOUR_OF_DAY);
        minute_x = cal.get(Calendar.MINUTE);

        spinner = (Spinner) findViewById(R.id.taskListSpinner);
        textDate = (TextView) findViewById(R.id.textDate);
        textTime = (TextView) findViewById(R.id.textTime);
        textDate.setText(formatDate(year_x, month_x+1, day_x));
        textTime.setText(formatTime(hour_x, minute_x));

        commands = new ArrayList<String>();
        commandNos = new ArrayList<String>();
        checkedOnStatus = new ArrayList<Boolean>();
        checkedOffStatus = new ArrayList<Boolean>();
        checkedNoneStatus = new ArrayList<Boolean>();

        for (int i=0;i<helperDataClass.list_length;i++)
        {
            for (String str : helperDataClass.titlesChildren.get(i))
            {
                commands.add(str);
                checkedOnStatus.add(false);
                checkedOffStatus.add(false);
                checkedNoneStatus.add(true);
            }
        }
        for (int i=0;i<helperDataClass.list_length;i++)
        {
            for (String no : helperDataClass.childCommands.get(i))
            {
                commandNos.add(no);
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.tsCommandsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        tableAdapter = new TableAdapter(this, commands, commandNos, this);
        recyclerView.setAdapter(tableAdapter);

        helperClass.decodeTaskList();
        helperClass.reloadTasks(spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView taskName = (TextView) findViewById(R.id.txtTaskName);
                if(position!=0) {
                    clearTask();
                    taskName.setText(helperDataClass.taskNames.get(position));
                    textDate.setText(helperDataClass.taskDates.get(position));
                    textTime.setText(helperDataClass.taskTimes.get(position));
                    for(int i=0; i<helperDataClass.taskStates.get(position).size(); i++) {
                        int pos=0;
                        loop: for(int j=0; j<commandNos.size(); j++) {
                            if(commandNos.get(j).equals(helperDataClass.taskCommands.get(position).get(i))) {
                                pos = j;
                                break loop;
                            }
                        }
                        if(helperDataClass.taskStates.get(position).get(i).equals("true")) {
                            checkedOnStatus.set(pos, true);
                        } else if(helperDataClass.taskStates.get(position).get(i).equals("false")) {
                            checkedOffStatus.set(pos, true);
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        clearTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_scheduler, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        //noinspection SimplifiableIfStatement
        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        } else if (id == R.id.action_settings) {
            intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openDatePickerDialog(View view)
    {
        showDialog(0);
    }

    public void openTimePickerDialog(View view)
    {
        showDialog(1);
    }

    @Override
    protected Dialog onCreateDialog(int id)
    {
        if(id==0)
        {
            return new DatePickerDialog(this, datePickerListener, year_x, month_x, day_x);
        } else if(id==1)
        {
            return new TimePickerDialog(this, timePickerListener, hour_x, minute_x, true);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            year_x=year;
            month_x=monthOfYear;
            day_x=dayOfMonth;
            textDate.setText(formatDate(year_x, month_x+1, day_x));
        }
    };

    private TimePickerDialog.OnTimeSetListener timePickerListener
            = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            hour_x = hourOfDay;
            minute_x = minute;
            textTime.setText(formatTime(hour_x, minute_x));
        }
    };

    private String formatDate(int year, int month, int day)
    {
        String year_s = Integer.toString(year);
        String month_s , day_s;
        if(month<10) { month_s = "0" + month; } else { month_s=Integer.toString(month); }
        if(day<10) { day_s = "0" + day; } else { day_s=Integer.toString(day); }
        return year_s+"-"+month_s+"-"+day_s;
    }

    private String formatTime(int hour, int minute)
    {
        String hour_s, minute_s;
        if(hour<10) { hour_s = "0"+hour; } else { hour_s=Integer.toString(hour); }
        if(minute<10) { minute_s="0"+minute; } else { minute_s=Integer.toString(minute); }
        return hour_s+":"+minute_s+":00";
    }

    public void stateChanged(View v)
    {
        if(v.getId()==R.id.table_on)
        {
            checkedOnStatus.set(Integer.parseInt(v.getTag().toString()),true);
            checkedOffStatus.set(Integer.parseInt(v.getTag().toString()),false);
            checkedNoneStatus.set(Integer.parseInt(v.getTag().toString()),false);
        } else if (v.getId()==R.id.table_off)
        {
            checkedOnStatus.set(Integer.parseInt(v.getTag().toString()),false);
            checkedOffStatus.set(Integer.parseInt(v.getTag().toString()),true);
            checkedNoneStatus.set(Integer.parseInt(v.getTag().toString()),false);
        } else if (v.getId()==R.id.table_none)
        {
            checkedOnStatus.set(Integer.parseInt(v.getTag().toString()),false);
            checkedOffStatus.set(Integer.parseInt(v.getTag().toString()),false);
            checkedNoneStatus.set(Integer.parseInt(v.getTag().toString()),true);
        }
    }

    public void taskCommands(View view)
    {
        final TextView taskName = (TextView) findViewById(R.id.txtTaskName);
        switch (view.getId())
        {
            case R.id.saveTask:
                new Thread() {
                    public void run() {
                        if (helperClass.addTask(taskName.getText().toString(), textDate.getText().toString(), textTime.getText().toString(), getCommands())) {
                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TaskSchedulerActivity.this, "Task Added Successfully", Toast.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            myActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(TaskSchedulerActivity.this, "Failed to Add Task", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }.start();

                break;
            case R.id.clearTask:
                clearTask();
                break;
            case R.id.removeTask:
                helperClass.removeTask(taskName.getText().toString(), this);
                break;
        }
    }

    private void clearTask()
    {
        TextView taskName = (TextView) findViewById(R.id.txtTaskName);
        for(int i=0; i<commands.size();i++)
        {
            checkedOnStatus.set(i, false);
            checkedOffStatus.set(i, false);
            checkedNoneStatus.set(i, true);
        }
        taskName.setText("Task");
        Calendar calendar = Calendar.getInstance();
        textDate.setText(formatDate(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH)+1,calendar.get(Calendar.DAY_OF_MONTH)));
        textTime.setText(formatTime(calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE)));
        tableAdapter.notifyItemRangeChanged(0, tableAdapter.getItemCount());
    }

    private String getCommands()
    {
        String msg="";
        for(int i=0;i<commands.size();i++)
        {
            if(checkedOnStatus.get(i))
            {
                msg+="X"+commandNos.get(i)+"Y1Z";
            }else if(checkedOffStatus.get(i))
            {
                msg+="X"+commandNos.get(i)+"Y0Z";
            }
        }
        return msg;
    }

    public void reloadTaskList(View view)
    {
        helperClass.decodeTaskList();
        helperClass.reloadTasks(spinner);
        clearTask();
    }
}
