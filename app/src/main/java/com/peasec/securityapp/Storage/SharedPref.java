package com.peasec.securityapp.Storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.Objects.UserCred;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SharedPref {
    private final static String SHARED_PREF_NAME = "PEASEC_SharedPref";
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPref(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void storeUserCredentials(UserCred userCred){
        String username = userCred.getUsername();
        String password = userCred.getPassword();
        String fullname = userCred.getFullname();

        editor.putString("Username", username);
        editor.putString("Password", password);
        editor.putString("Fullname", fullname);

        editor.commit();
    }

    public UserCred getUserCredentials(){
        String username =sharedPreferences.getString("Username","none");
        String password = sharedPreferences.getString("Password","none");
        String fullname = sharedPreferences.getString("Fullname","none");

        return new UserCred(username,password,fullname);
    }

    public void storeEventList(List<Event> eventList){
        Gson gson = new Gson();

        AtomicInteger i = new AtomicInteger();
        eventList.forEach((e) -> {
            String jsonEvent = gson.toJson(e);
            editor.putString("Event"+i,jsonEvent);
            i.getAndIncrement();
        });
        editor.commit();
    }

    public List<Event> getEventList(){
        List<Event> eventList = new ArrayList<>();

        Gson gson = new Gson();
        for (String key : sharedPreferences.getAll().keySet()) {
            if (key.startsWith("Event")) {
                String jsonEvent = sharedPreferences.getString(key,"");
                Event event = gson.fromJson(jsonEvent,Event.class);
                eventList.add(event);
            }
        }
        return eventList;
    }

}