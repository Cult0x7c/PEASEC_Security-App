package com.peasec.securityapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.peasec.securityapp.Adapter.EventListAdapter;
import com.peasec.securityapp.Interface.HttpEventInterface;
import com.peasec.securityapp.Network.HttpClient;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Activity_EventList extends AppCompatActivity implements HttpEventInterface {
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;
    private ArrayList<Event> eventListCopy;

    public LatLng getMyLocation() {
        return myLocation;
    }
    private LatLng myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        //get myLocation from extra
        Intent intent = getIntent();
        String jsonMyLoc =intent.getStringExtra("MyLocation");
        Gson gson = new Gson();
        myLocation = gson.fromJson(jsonMyLoc,LatLng.class);

        //initialize recyclerView
        this.recyclerView = findViewById(R.id.recycler_eventList);

        //create new httpClient and get all events from DB
        HttpClient httpClient = new HttpClient(this);
        httpClient.getAllEvents();
    }

    public void setAdapter(List<Event> eventList){
        EventListAdapter adapter = new EventListAdapter(this,eventList,this.myLocation);
        this.eventListAdapter=adapter;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        
        //create spinner list with available countries
        List<String> spinnerArray =  new ArrayList<String>();

        //safe a copy of the full list before filtering
        eventListCopy = new ArrayList<>(eventList);

        eventList.forEach(e ->{
            spinnerArray.add(e.getCountry());
        });
        spinnerArray.add("All");

        //remove duplicates
        List<String> spinnerArrayCleaned =  new ArrayList<String>(new HashSet<>(spinnerArray));

        //finally fill spinner with data
        fillSpinner(spinnerArrayCleaned);
    }

    private void fillSpinner(List<String> spinnerArray){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinnerEventList);
        sItems.setAdapter(adapter);
        //add listener for country change
        sItems.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                filter(adapterView.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void filter(String country) {
        if (!country.equals("All")) {
            List<Event> eventList = new ArrayList<>(eventListCopy);
            /*eventList.forEach(e->{
                if(!e.getCountry().equals(country)){
                    eventList.remove(e);
                }*/

            for (Iterator<Event> iterator = eventList.iterator(); iterator.hasNext(); ) {
                String value = iterator.next().getCountry();
                if (!value.equals(country)) {
                    iterator.remove();
                }
            }
            eventListAdapter.setEventList(eventList);
        }
        else{
            eventListAdapter.setEventList(eventListCopy);
        }
        eventListAdapter.notifyDataSetChanged();
    }

    @Override
    public void showNoEvents() {
        TextView tvError = ((TextView) findViewById(R.id.tvErrorEventList));
        tvError.setVisibility(View.VISIBLE);
        tvError.setText("No events listed for this country!");
    }

    @Override
    public void showNoNetwork(int statusCode) {
        TextView tvError = ((TextView) findViewById(R.id.tvErrorEventList));
        tvError.setVisibility(View.VISIBLE);
        tvError.setText("Network Error: "+statusCode);
    }
}