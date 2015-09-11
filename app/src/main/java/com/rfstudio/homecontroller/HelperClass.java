package com.rfstudio.homecontroller;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Raveen on 8/12/2015.
 */
public class HelperClass {
    SharedPreferences preferences;
    HelperDataClass helperDataClass;
    Context context;
    SharedPreferences.Editor editor;

    ArrayList<Drawable> images;

    HelperClass(Context context, HelperDataClass helperDataClass)
    {
        this.context = context;
        this.helperDataClass = helperDataClass;
        preferences = context.getSharedPreferences(helperDataClass.PREF_NAME, Context.MODE_PRIVATE);
        images = new ArrayList<Drawable>();
    }

    private void getImage(int length, int newVer)
    {
        loop:
        for(int i=0; i<length; i++) {

            File file = new File(helperDataClass.APP_PATH+helperDataClass.imageNames.get(i));
            if(!file.exists() || preferences.getInt("dbVer", 0)!=newVer) {
                String url = "http://" + preferences.getString("url", "192.168.1.100") + "/hc/" + helperDataClass.imageNames.get(i);
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    connection.setConnectTimeout(1000);
                    connection.setReadTimeout(1000);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Log.i("RAV-INFO", "File Downloaded : "+i+".jpg");
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[5242880];

                    while ((nRead = input.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();

                    FileOutputStream fileOutputStream = context.openFileOutput(i + ".jpg", Context.MODE_PRIVATE);
                    fileOutputStream.write(buffer.toByteArray());
                    fileOutputStream.close();
                    buffer.close();
                    Log.i("RAV-INFO", "File Saved : " + i + ".jpg");
                } catch (SocketTimeoutException ex) {
                    Toast.makeText(context, "Connection to the server timed out", Toast.LENGTH_LONG).show();
                    Log.i("RAV-INFO", "Socket Timed Out");
                    break loop;
                } catch (FileNotFoundException ex)
                {
                    if(file.exists()) {
                        file.delete();
                        Log.i("RAV-INFO", "File Deleted : " + file.getName());
                    }
                } catch (Exception ex) {
                    Log.e("RAV-ERR", "Error @Main, getImg "+ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        editor = preferences.edit();
        editor.putInt("dbVer", newVer);
        editor.commit();
    }

    ArrayList<Drawable> loadDrawableArray(int length) throws IOException
    {
        for(int i=0; i<length; i++) {
            images.add(drawableFromUrl(i));
        }
        return images;
    }

    public Drawable drawableFromUrl(int no) throws IOException {

        FileInputStream fileInputStream = null;

        try {
            fileInputStream = context.openFileInput(no + ".jpg");
            Bitmap bitmap = BitmapFactory.decodeStream(fileInputStream);
            return new BitmapDrawable(context.getResources(), bitmap);
        }catch (FileNotFoundException ex){
            Log.i("RAV-INFO", "Image Not Found");
            return context.getResources().getDrawable( R.drawable.img_not_found);
        }
        catch (Exception ex)
        {
            Log.e("RAV-ERR", "Error @Main, drawableFromUrl "+ex.getMessage());
            ex.printStackTrace();
        }
        return null;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void sendRequest(String msg)
    {
        final String message = msg;
        new Thread() {
            public void run()
            {
                try {
                    URL url = new URL("http://"+preferences.getString("url", "192.168.1.100")+"/hc/android.php");
                    String urlParameters = "username="+preferences.getString("username", "")+"&password="+preferences.getString("password", "")+"&message="+message;
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                    connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                    connection.setDoOutput(true);
                    DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                    dStream.writeBytes(urlParameters);
                    dStream.flush();
                    dStream.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    StringBuilder responseOutput = new StringBuilder();
                    while((line = br.readLine()) != null ) {
                        responseOutput.append(line);
                    }
                    br.close();

                    if(!responseOutput.equals("")) {
                        if (responseOutput.toString().charAt(0) == 'R' && responseOutput.charAt(responseOutput.toString().length() - 1) == 'N') {
                            helperDataClass.statusLine = responseOutput.toString();
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();

    }

    void getStatus()
    {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();

            URL url = new URL("http://" + preferences.getString("url","192.168.1.100") + "/hc/status.xml");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(HelperDataClass.timeOut);
            urlConnection.setReadTimeout(HelperDataClass.readTimeOut);
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            parser.setInput(reader);

            int pos=0;

            String text="";
            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT)
            {
                String tagName = parser.getName();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        if(tagName.equalsIgnoreCase("status")) {
                            helperDataClass.state.clear();
                        }
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase("command"))
                        {
                            pos=Integer.parseInt(text);
                        } else if (tagName.equalsIgnoreCase("state")) {
                            if(text.equals("1")) {
                                helperDataClass.state.add(pos, true);
                            } else if(text.equals("0")) {
                                helperDataClass.state.add(pos, false);
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        for(Iterator<Boolean> i = helperDataClass.state.iterator(); i.hasNext(); ) {
            boolean state = i.next();
        }
    }

    void parse()
    {
        try {
            List<Room> roomList = new ArrayList<Room>();
            List<Child> childList = null;

            String dbVer="";
            Room room=null;
            Child child=null;
            String text="";

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(context.openFileInput("db.xml"), null);

            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT)
            {
                String tagName = parser.getName();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        if(tagName.equalsIgnoreCase("room"))
                        {
                            room = new Room();
                            childList = new ArrayList<Child>();
                        } else if(tagName.equalsIgnoreCase("dbVer"))
                        {
                            dbVer="";
                        } else if(tagName.equalsIgnoreCase("child"))
                        {
                            child = new Child();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(tagName.equalsIgnoreCase("room")) {
                            room.children = childList;
                            roomList.add(room);
                        } else if (tagName.equalsIgnoreCase("name"))
                        {
                            room.name = text;
                        } else if (tagName.equalsIgnoreCase("image")) {
                            room.image = text;
                        } else if(tagName.equalsIgnoreCase("dbVer"))
                        {
                            dbVer=text;
                        } else if(tagName.equalsIgnoreCase("childName"))
                        {
                            child.childName =text;
                        } else if(tagName.equalsIgnoreCase("command"))
                        {
                            child.command=text;
                        } else if(tagName.equalsIgnoreCase("child"))
                        {
                            childList.add(child);
                        }
                        break;
                }
                eventType = parser.next();
            }

            helperDataClass.ver = Integer.parseInt(dbVer);
            helperDataClass.list_length = roomList.size();

            for(int i=0;i<helperDataClass.list_length;i++)
            {
                helperDataClass.titles.add(i, roomList.get(i).name);
                helperDataClass.imageNames.add(i,roomList.get(i).image);
                ArrayList<String> childNames = new ArrayList<String>();
                ArrayList<String> commandNo = new ArrayList<String>();
                for(int j=0;j<roomList.get(i).children.size();j++)
                {
                    childNames.add(j, roomList.get(i).children.get(j).childName);
                    commandNo.add(j, roomList.get(i).children.get(j).command);
                }
                helperDataClass.titlesChildren.add(i, childNames);
                helperDataClass.childCommands.add(i, commandNo);
            }

            getImage(helperDataClass.list_length, helperDataClass.ver);
            loadDrawableArray(helperDataClass.list_length);


        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean addTask(String name, String date, String time, String commands) {
        if(name.isEmpty() || date.isEmpty() || time.isEmpty() || commands.isEmpty()) {
            return false;
        }
        String message = "setTask:<newtask><name>" + name + "</name><date>" + date + "</date><time>" + time + "</time><msg>" + commands + "</msg></newtask>";
        Log.d("RAV-MSG", message);
        try {
            URL url = new URL("http://" + preferences.getString("url", "192.168.1.100") + "/hc/androidq.php");
            String urlParameters = "username=" + preferences.getString("username", "") + "&password=" + preferences.getString("password", "") + "&message=" + message;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(urlParameters);
            dStream.flush();
            dStream.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while ((line = br.readLine()) != null) {
                responseOutput.append(line);
            }
            br.close();

            if (responseOutput.toString().equals("TASK_ADDED")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void reloadTasks(Spinner spinner) {
        ArrayAdapter<String> arrayAdapter;
        if(helperDataClass.taskTimes.size()!= 0) {
            arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, helperDataClass.taskNames);
        } else {
            ArrayList<String> arr = new ArrayList<String>();
            arr.add("Task List is Empty");
            arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, arr);
        }
        spinner.setAdapter(arrayAdapter);
    }

    public ArrayList<String> getTaskList()
    {
        return helperDataClass.taskNames;
    }

    public boolean decodeTaskList()
    {
        try {
            URL url = new URL("http://" + preferences.getString("url", "192.168.1.100") + "/hc/androidq.php");
            String urlParameters = "username=" + preferences.getString("username", "") + "&password=" + preferences.getString("password", "") + "&message=getTaskList";
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
            connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
            connection.setDoOutput(true);
            DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
            dStream.writeBytes(urlParameters);
            dStream.flush();
            dStream.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            StringBuilder responseOutput = new StringBuilder();
            while((line = br.readLine()) != null ) {
                responseOutput.append(line);
            }
            br.close();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new ByteArrayInputStream(responseOutput.toString().getBytes(StandardCharsets.UTF_8)),null);

            List<Task> taskList = new ArrayList<Task>();
            List<TaskCommand> commandList = new ArrayList<TaskCommand>();
            Task task = null;
            TaskCommand taskCommand = null;
            String text = "";

            int eventType = parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT)
            {
                String tagName = parser.getName();
                switch (eventType)
                {
                    case XmlPullParser.START_TAG:
                        if(tagName.equalsIgnoreCase("task"))
                        {
                            task = new Task();
                            commandList = new ArrayList<TaskCommand>();
                        } else if(tagName.equalsIgnoreCase("command"))
                        {
                            taskCommand = new TaskCommand();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        text = parser.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        if(tagName.equalsIgnoreCase("task")) {
                            task.taskCommands = commandList;
                            taskList.add(task);
                        } else if (tagName.equalsIgnoreCase("name"))
                        {
                            task.taskName = text;
                        } else if (tagName.equalsIgnoreCase("date")) {
                            task.taskDate = text;
                        } else if(tagName.equalsIgnoreCase("time"))
                        {
                            task.taskTime = text;
                        } else if(tagName.equalsIgnoreCase("no"))
                        {
                            taskCommand.commandNo = text;
                        } else if(tagName.equalsIgnoreCase("state"))
                        {
                            taskCommand.commandState = text;
                        } else if(tagName.equalsIgnoreCase("command"))
                        {
                            commandList.add(taskCommand);
                        }
                        break;
                }
                eventType = parser.next();
            }
            helperDataClass.taskNames.clear();
            helperDataClass.taskTimes.clear();
            helperDataClass.taskDates.clear();
            helperDataClass.taskStates.clear();
            helperDataClass.taskCommands.clear();
            for(int i=0; i<taskList.size();i++)
            {
                helperDataClass.taskNames.add(taskList.get(i).taskName);
                helperDataClass.taskDates.add(taskList.get(i).taskDate);
                helperDataClass.taskTimes.add(taskList.get(i).taskTime);

                ArrayList<String> command = new ArrayList<String>();
                ArrayList<String> state = new ArrayList<String>();
                for(int j=0; j<taskList.get(i).taskCommands.size();j++)
                {
                    command.add(taskList.get(i).taskCommands.get(j).commandNo);
                    state.add(taskList.get(i).taskCommands.get(j).commandState);
                }
                helperDataClass.taskCommands.add(command);
                helperDataClass.taskStates.add(state);
            }
            return true;

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public void removeTask(String taskName, final TaskSchedulerActivity taskSchedulerActivity)
    {
        final String message = "removeTask:"+taskName;
        new Thread() {
            public void run()
            {
                try {
                    URL url = new URL("http://"+preferences.getString("url", "192.168.1.100")+"/hc/androidq.php");
                    String urlParameters = "username="+preferences.getString("username", "")+"&password="+preferences.getString("password", "")+"&message="+message;
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                    connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                    connection.setDoOutput(true);
                    DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                    dStream.writeBytes(urlParameters);
                    dStream.flush();
                    dStream.close();

                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    StringBuilder responseOutput = new StringBuilder();
                    while((line = br.readLine()) != null ) {
                        responseOutput.append(line);
                    }
                    br.close();
                    Activity myActivity = (Activity) context;
                    if(responseOutput.toString().equals("TASK_DELETED")) {
                        myActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Task Removed", Toast.LENGTH_LONG).show();
                                taskSchedulerActivity.reloadTaskList(null);
                            }
                        });
                    } else {
                        myActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Failed to Remove Task", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }.start();

    }
}
