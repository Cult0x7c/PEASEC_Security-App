package com.peasec.securityapp.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.peasec.securityapp.Network.HttpClient;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Activity_newEvent extends AppCompatActivity implements OnMapReadyCallback {

    private Bitmap selectedImageBitmap;
    private View view;

    MapView mapView;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newevent);
        initMap(savedInstanceState);
        fillSpinner();
    }

    private void initMap(Bundle savedInstanceState) {
        // Gets the MapView from the XML layout and creates it
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);



    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        GoogleMap mMap = googleMap;


        /*
        // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(-34, 151);
                mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("Marker in Sydney"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    }

    // populate spinnter with items
    private void fillSpinner(){
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("item1");
        spinnerArray.add("item2");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinnerCategory);
        sItems.setAdapter(adapter);

    }

    public void submitReport(View v) throws IOException {
        //create event object
        String category = ((Spinner) findViewById(R.id.spinnerCategory)).getSelectedItem().toString();
        //Pair<Double,Double> location = new Pair<Double,Double>(1.00, 2.00);
        Double longitude = 1.00;
        Double latidude = 2.00;
        String description = ((EditText) findViewById(R.id.tbEventDescription)).toString();

        Drawable drawable = ((ImageView) findViewById(R.id.ivEventImg1)).getDrawable();
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmapImage = bd.getBitmap();

        String b64Image = encodeToBase64(bitmapImage);

        Event event = new Event(category,longitude,latidude,description,b64Image);

        //create json object
        Gson gson = new Gson();
        String jsonEvent = gson.toJson(event);

        //send event to API
        HttpClient httpClient = new HttpClient();
        httpClient.post(jsonEvent);
    }

    private String encodeToBase64(Bitmap image)
    {
        int quality= 100;
        Bitmap.CompressFormat compressFormat= Bitmap.CompressFormat.JPEG;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public void openGallery(View v){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        int x=0;
        launchSomeActivity.launch(i);
        this.view = v;

    }
    private ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // do your operation from here....
                    if (data != null && data.getData() != null) {
                        Uri selectedImageUri = data.getData();
                        Bitmap selectedImageBitmap;
                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);
                            //this.selectedImageBitmap = selectedImageBitmap;
                            switch (this.view.getId()) {
                                case R.id.ivEventImg1:
                                    ((ImageView)findViewById(R.id.ivEventImg1)).setImageBitmap(selectedImageBitmap);
                                    break;
                                case R.id.ivEventImg2:
                                    ((ImageView)findViewById(R.id.ivEventImg2)).setImageBitmap(selectedImageBitmap);
                                    break;
                                case R.id.ivEventImg3:
                                    ((ImageView)findViewById(R.id.ivEventImg3)).setImageBitmap(selectedImageBitmap);
                                    break;
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });



}