package com.ilya.kritapalette.utils;

import android.app.Activity;
import android.content.SharedPreferences;

public class Storage {

    private final Activity activity;
    private final SharedPreferences preferences;

    public Storage(Activity activity){
        this.activity = activity;
        preferences = activity.getSharedPreferences("storage",Activity.MODE_PRIVATE);
    }

    public boolean hasIp(){
        return preferences.contains("ip");
    }

    public String getIp(){
        return preferences.getString("ip",null);
    }

    public int getPort(){
        return preferences.getInt("port",-1);
    }

    public void set(String ip,int port){
        preferences.edit().putString("ip",ip).putInt("port",port).apply();
    }

}
