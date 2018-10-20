package com.example.jonathan.ics.util.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jonathan.ics.model.LoggingElement;
import com.example.jonathan.ics.Activities.main.SettingsActivity;
import com.example.jonathan.ics.util.interfaceHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Storage {

    public enum storageVariables {
        calender,mail,password,account,lastChecked,basMail,log
    }

    private Context context;

    private static SharedPreferences sP;

    public Storage(){

    }
    public void setContext(Context context) {
        this.context=context;
        if (sP == null) {
            sP = context.getApplicationContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        }
    }

    public void register(storageVariables storageKey,storageListener listener){
        sP.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(storageKey.toString())) {
                    listener.OnStorageChange(sharedPreferences.getString(key, ""));
                }
            }
        });
    }

    public void write(storageVariables key, String value ){
        if(key.equals(storageVariables.log)) {
            interfaceHandler.update(SettingsActivity.actions.logUpdate,"",context);
        }
        SharedPreferences.Editor editor = sP.edit();
        editor.putString(key.toString(),value);
        editor.commit();
        editor.apply();
    }
    public List<LoggingElement> getLog(){
        String logString=get(storageVariables.log);
        return parseToList(logString);
    }

    public List<LoggingElement> parseToList(String storageString){
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<LoggingElement> list=mapper.readValue(storageString,new TypeReference<List<LoggingElement>>(){});
            if(list.size()>0&& !(list.get(0) instanceof  LoggingElement)){
                return new ArrayList<>();
            }
            return list;
        } catch (IOException | NullPointerException |ClassCastException e) {
            interfaceHandler.note(e.getMessage());
            return new ArrayList<>();
        }
    }

    public String get(storageVariables key ){
        return sP.getString(key.toString(),null);
    }

    public void log(LoggingElement value ){
        List<LoggingElement> currentLog=getLog();
        Collections.reverse(currentLog);
        currentLog.add(value);
        Collections.reverse(currentLog);
        write(storageVariables.log,new Gson().toJson(currentLog));
    }

    public void log(String value ){
        log(new LoggingElement(value));
    }
    public void log(Throwable value ){
        log(new LoggingElement(value));
    }
}
