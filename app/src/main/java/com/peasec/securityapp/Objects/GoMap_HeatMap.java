package com.peasec.securityapp.Objects;

import static com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.peasec.securityapp.Activities.Activity_DetailEvent;
import com.peasec.securityapp.Activities.Activity_HeatMap;
import com.peasec.securityapp.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GoMap_HeatMap {
    private static final float DEFAULT_ZOOM = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;

    private final GoogleMap map;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final Context context;
    private final Activity_HeatMap activity_heatmap;
    private Location lastKnownLocation;
    private final String tag = "GoMap_NewEvent";
    private LatLng defaultLocation = new LatLng(-34, 151);
    private List<Marker> markerList = new ArrayList<Marker>();

    public Location getLastKnownLocation() {
        if (lastKnownLocation == null) {
            Location defLoc = new Location("");
            defLoc.setLatitude(0.0d);
            defLoc.setLongitude(0.0d);
            return defLoc;
        }
        return lastKnownLocation;
    }

    public LatLng getMarkerLocation() {
        return markerLocation;
    }

    private LatLng markerLocation;

    public GoMap_HeatMap(GoogleMap googleMap, Context context) {
        this.map = googleMap;
        this.context = context;
        this.activity_heatmap = (Activity_HeatMap) context;

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();


        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setOnMyLocationButtonClickListener(() -> false);
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (this.activity_heatmap.isLocationPermissionGranted()) {
                Task<Location> locationResult = fusedLocationProviderClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null);
                locationResult.addOnCompleteListener(this.activity_heatmap, new OnCompleteListener<Location>() {
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
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    public void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (this.activity_heatmap.isLocationPermissionGranted()) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;

                Activity_HeatMap activity = (Activity_HeatMap) context;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public void addMarker(Event event) {
        // Add a marker in event location and move the camera
        Double lat = event.getLat();
        Double lng = event.getLng();

        LatLng eventLatLong = new LatLng(lat, lng);
        Marker marker = map.addMarker(new MarkerOptions()
                .position(eventLatLong)
                .title(event.getType())
                .visible(false)
        );
        //bind event object to marker
        marker.setTag(event);
        /*
        Bitmap markerIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.attention_sign_icon);
        BitmapDescriptor mIcon = BitmapDescriptorFactory.fromBitmap(markerIcon);
        marker.setIcon(mIcon);*/
        markerList.add(marker);
    }

    public void addHeatMap(List<LatLng> latLngList) {
        Collection<LatLng> latLngs = latLngList;

        // Create a heat map tile provider, passing it the latlngs of the police stations.
        int points;


        // Create the gradient.
        int[] colors = {
                Color.rgb(0, 102, 255), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(latLngs)
                .gradient(gradient)
                .build();

        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlay tileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    public void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.context.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            this.activity_heatmap.setLocationPermissionGranted(true);
        } else {
            ActivityCompat.requestPermissions(this.activity_heatmap,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public void setMarkerVisibleZoomLevel(int level) {
        map.setOnCameraMoveListener(() -> {
            for (Marker m : markerList) {
                m.setVisible(map.getCameraPosition().zoom > level);
                //8 here is your zoom level, you can set it as your need.
            }
        });
    }

    public void addOnClickListenerMarker() {
        // adding on click listener to marker of google maps.

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                Intent i = new Intent(context, Activity_DetailEvent.class);
                Gson gson = new Gson();
                String jsonEvent = gson.toJson(marker.getTag());
                i.putExtra("JsonEvent", jsonEvent);
                context.startActivity(i);
            }
        });
    }
}
