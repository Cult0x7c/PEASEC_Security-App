package com.peasec.securityapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.peasec.securityapp.Adapter.EventListAdapter;
import com.peasec.securityapp.Interface.HttpEventInterface;
import com.peasec.securityapp.Network.HttpClient;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.R;

import java.lang.reflect.GenericSignatureFormatError;
import java.util.List;

public class Activity_EventList extends AppCompatActivity implements HttpEventInterface {
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;

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