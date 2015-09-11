package com.rfstudio.homecontroller;

/**
 * Created by Raveen on 8/12/2015.
 */
import java.io.Serializable;
import java.util.ArrayList;

public class HelperDataClass implements Serializable {

    static final String APP_PATH = "/data/data/com.rfstudio.homecontroller/files/";
    static final String PREF_NAME = "settings";

    static int timeOut = 1000;
    static int readTimeOut = 1000;

    boolean serverOnline = false;

    String ownerName="";
    String statusLine="";
    String fullString="";
    
    int ver=0;
    int list_length=0;
    
    ArrayList<String> titles = new ArrayList<String>();
    ArrayList<String> imageNames = new ArrayList<String>();
    ArrayList<ArrayList<String>> titlesChildren = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> childCommands = new ArrayList<ArrayList<String>>();
    ArrayList<Boolean> status = new ArrayList<Boolean>();

    ArrayList<Boolean> state= new ArrayList<Boolean>();



}
