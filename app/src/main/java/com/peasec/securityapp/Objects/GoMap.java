package com.peasec.securityapp.Objects;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.peasec.securityapp.Activities.Activity_NewEvent;
import com.peasec.securityapp.R;

public class GoMap {
    private static final float DEFAULT_ZOOM = 17;

    private final PlacesClient placesClient;
    private final GoogleMap map;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Context context;
    private final Activity_NewEvent activity_newEvent;
    private Location lastKnownLocation;
    private final String tag ="GoMap";
    private LatLng defaultLocation = new LatLng(-34, 151);

    public Location getLastKnownLocation() {
        return lastKnownLocation;
    }

    public LatLng getMarkerLocation() {
        return markerLocation;
    }

    private LatLng markerLocation;

    public GoMap(GoogleMap googleMap, Context context){
        this.map = googleMap;
        this.context = context;
        this.activity_newEvent = (Activity_NewEvent) context;


        // Construct a PlacesClient
        Places.initialize(context, context.getString(R.string.MapsApiKey));
        placesClient = Places.createClient(context);

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                //allPoints.add(point);
                map.clear();
                map.addMarker(new MarkerOptions().position(point));
                markerLocation = point;
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                map.clear();
                markerLocation=null;
                return false;
            }

        });

    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (this.activity_newEvent.isLocationPermissionGranted()) {
                Task<Location> locationResult = fusedLocationProviderClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY,null);
                locationResult.addOnCompleteListener(this.activity_newEvent, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                map.setMyLocationEnabled(true);
                            }
                        } else {
                            Log.d(tag, "Current location is null. Using defaults.");
                            Log.e(tag, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (this.activity_newEvent.isLocationPermissionGranted()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;

                Activity_NewEvent activity = (Activity_NewEvent) context;
                activity.getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }




}
