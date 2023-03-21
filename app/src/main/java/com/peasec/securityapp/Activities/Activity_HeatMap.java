package com.peasec.securityapp.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.gson.Gson;
import com.peasec.securityapp.Interface.HttpEventInterface;
import com.peasec.securityapp.Network.HttpClient;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.Objects.GoMap_HeatMap;
import com.peasec.securityapp.Objects.UserCred;
import com.peasec.securityapp.R;
import com.peasec.securityapp.Storage.SharedPref;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Activity_HeatMap extends AppCompatActivity  implements OnMapReadyCallback, HttpEventInterface {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100 ;
    private boolean isLocationPermissionGranted;
    private UserCred userCred;
    private SharedPref sharedPref;
    private HttpClient httpClient;
    private GoMap_HeatMap goMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heat_map);
        initMap();

        //create new httpClient and call getAllReports in API
        HttpClient httpClient = new HttpClient(this);
        httpClient.getAllEvents();

        //create sharedPref and httpClient for later
        this.sharedPref = new SharedPref(this);
        this.httpClient = new HttpClient(this);

        //check if user already has credentials if not create new user
        this.userCred = getUserCred();
    }

    public boolean isLocationPermissionGranted(){
        return isLocationPermissionGranted;
    }

    //check if user already created if not then create new one
    private UserCred getUserCred(){

        UserCred tempCred = this.sharedPref.getUserCredentials();
        if(tempCred.getUsername().equals("none")){
            UserCred newUser = new UserCred(this);
            String jsonNewUser =  new Gson().toJson(newUser);
            //create new user
            this.httpClient.createNewUser(jsonNewUser);
        }

        return sharedPref.getUserCredentials();
    }

    public void storeUserCredentials(UserCred newUser){
        sharedPref.storeUserCredentials(newUser);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        isLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isLocationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        goMap.updateLocationUI();
    }


    private void initMap() {
        // Gets the MapView from the XML layout and creates it
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHeatMap);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        GoogleMap mMap = googleMap;

        //create GoMap_NewEvent
        this.goMap = new GoMap_HeatMap(googleMap,this);

        /*
        // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(-34, 151);
                mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    //opens NewEvent activity
    public void openActivityNewEvent(View view){
        Intent intent = new Intent (this, Activity_NewEvent.class);
        startActivity(intent);
    }

    //opens NewEvent activity
    public void openActivityEventList(View view){
        Intent intent = new Intent (this, Activity_EventList.class);
        LatLng myLocation = new LatLng(goMap.getLastKnownLocation().getLatitude(),goMap.getLastKnownLocation().getLongitude());
        Gson gson = new Gson();
        String jsonMyLoc = gson.toJson(myLocation,LatLng.class);
        intent.putExtra("MyLocation",jsonMyLoc);
        startActivity(intent);
    }


    private List<LatLng> readItems(@RawRes int resource) throws JSONException {
        List<LatLng> result = new ArrayList<>();
        InputStream inputStream = this.getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            result.add(new LatLng(lat, lng));
        }
        return result;
    }

    @Override
    public void setAdapter(List<Event> eventList) {
        //fill heatMap with coordinates from eventList
        List<LatLng> result = new ArrayList<>();

        //create list for heatmap tile provider and add markers
        eventList.forEach((e) ->{
            double lat = e.getLat();
            double lng = e.getLng();
            result.add(new LatLng(lat,lng));
            goMap.addMarker(e);
        });

        //add onclick listener for markers
        goMap.addOnClickListenerMarker();

        //set zoom level at which the markers are going to be visible
        goMap.setMarkerVisibleZoomLevel(8);

        //add Heatmap tiles to goMap
        if (eventList.size() > 0)   goMap.addHeatMap(result);
    }

    public void setLocationPermissionGranted(boolean permissionGranted){
        this.isLocationPermissionGranted=permissionGranted;
    }

    @Override
    public void showNoNetwork(int statusCode) {

    }

    @Override
    public void showNoEvents() {

    }
}