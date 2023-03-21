package com.peasec.securityapp.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.Objects.GoMap_Detail;
import com.peasec.securityapp.R;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ImageListener;

import java.util.ArrayList;
import java.util.List;

public class Activity_DetailEvent extends AppCompatActivity implements OnMapReadyCallback {
    private CarouselView carouselView;
    private List<Bitmap> imageList = new ArrayList<Bitmap>();
    private ImageListener imageListener = new ImageListener() {
        @Override
        public void setImageForPosition(int position, ImageView imageView) {
            imageView.setImageBitmap(imageList.get(position));
        }
    };
    private GoMap_Detail goMap;
    private Event event;
    private boolean isLocationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_event);

        //get jsonEvent string from intent
        Intent intent = getIntent();
        String jsonEvent = intent.getStringExtra("JsonEvent");

        //serialize to event object
        Gson gson = new Gson();
        this.event = gson.fromJson(jsonEvent,Event.class);

        //init map
        initMap();

        //add images to carousel slider
        fillCarousel(event);

        //fill elements in view
        ((TextView) findViewById(R.id.tvCategoryDetail)).setText(event.getType());
        ((TextView) findViewById(R.id.tvDescriptionDetail)).setText(event.getContent());
    }

    private void fillCarousel(Event event) {
        carouselView = (CarouselView) findViewById(R.id.carouselView);

        if (!event.getImage1().equals("")) {
            byte[] decodedImage1 = Base64.decode(event.getImage1(), Base64.DEFAULT);
            Bitmap bitmapImage1 = BitmapFactory.decodeByteArray(decodedImage1, 0, decodedImage1.length);
            imageList.add(bitmapImage1);
        }
        if (!event.getImage2().equals("")) {
            byte[] decodedImage1 = Base64.decode(event.getImage2(), Base64.DEFAULT);
            Bitmap bitmapImage1 = BitmapFactory.decodeByteArray(decodedImage1, 0, decodedImage1.length);
            imageList.add(bitmapImage1);
        }
        if (!event.getImage3().equals("")) {
            byte[] decodedImage1 = Base64.decode(event.getImage3(), Base64.DEFAULT);
            Bitmap bitmapImage1 = BitmapFactory.decodeByteArray(decodedImage1, 0, decodedImage1.length);
            imageList.add(bitmapImage1);
        }

            carouselView.setPageCount(imageList.size());
            carouselView.setImageListener(imageListener);
        }

    private void initMap() {
        // Gets the MapView from the XML layout and creates it
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapEventDetails);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add a marker in event location and move the camera
        Double lat = this.event.getLat();
        Double lng = this.event.getLng();

        LatLng eventLatLong = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions()
                .position(eventLatLong)
                .title(event.getType()));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(eventLatLong));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(eventLatLong,15.0f));
        goMap = new GoMap_Detail(googleMap,this);
    }

    public boolean isLocationPermissionGranted() {
        return this.isLocationPermissionGranted;
    }

    public void setLocationPermissionGranted(boolean b) {
        this.isLocationPermissionGranted=b;
    }
}
