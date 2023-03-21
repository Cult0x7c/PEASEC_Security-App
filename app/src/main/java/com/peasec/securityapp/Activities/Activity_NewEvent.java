package com.peasec.securityapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.gson.Gson;
import com.peasec.securityapp.Network.HttpClient;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.Objects.GoMap_NewEvent;
import com.peasec.securityapp.Objects.UserCred;
import com.peasec.securityapp.R;
import com.peasec.securityapp.Storage.SharedPref;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Activity_NewEvent extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100 ;
    private View view;
    private UserCred userCred;
    private GoMap_NewEvent goMap;


    private boolean locationPermissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newevent);

        fillSpinner();
        initMap();

        //get user credentials to post new event
        SharedPref sharedPref = new SharedPref(this);
        this.userCred=sharedPref.getUserCredentials();

        //manipulate alpha for imageRecognition
        ((ImageView)findViewById(R.id.ivEventImg1)).setImageAlpha(254);
        ((ImageView)findViewById(R.id.ivEventImg2)).setImageAlpha(254);
        ((ImageView)findViewById(R.id.ivEventImg3)).setImageAlpha(254);
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

        //create GoMap_NewEvent
        this.goMap = new GoMap_NewEvent(googleMap,this);

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
        spinnerArray.add("Demonstration");
        spinnerArray.add("Traffic Accident");
        spinnerArray.add("Explosion");
        spinnerArray.add("Chemical Hazard");
        spinnerArray.add("Natural Catastrophe");
        spinnerArray.add("Other");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sItems = (Spinner) findViewById(R.id.spinnerCategory);
        sItems.setAdapter(adapter);

    }

    //create new event
    public void submitReport(View v) {
        disableSubmitReportButton();
        //create event object
        String category = ((Spinner) findViewById(R.id.spinnerCategory)).getSelectedItem().toString();
        //Pair<Double,Double> location = new Pair<Double,Double>(1.00, 2.00);
        double longitude;
        double latidude;
        if(goMap.getMarkerLocation()!=null){
            //use marker location
            longitude = goMap.getMarkerLocation().longitude;
            latidude = goMap.getMarkerLocation().latitude;
        }
        else{
            longitude = goMap.getLastKnownLocation().getLongitude();
            latidude = goMap.getLastKnownLocation().getLatitude();
        }

        String description = String.valueOf(((EditText) findViewById(R.id.tbEventDescription)).getText());
        //ToDo: implement geolocating by coordinates

        String country = locateCountry(latidude, longitude);

        String b64Image1 = encodeToBase64(((BitmapDrawable) ((ImageView) findViewById(R.id.ivEventImg1)).getDrawable()).getBitmap());
        String b64Image2 = encodeToBase64(((BitmapDrawable) ((ImageView) findViewById(R.id.ivEventImg2)).getDrawable()).getBitmap());
        String b64Image3 = encodeToBase64(((BitmapDrawable) ((ImageView) findViewById(R.id.ivEventImg3)).getDrawable()).getBitmap());

        if (((ImageView) findViewById(R.id.ivEventImg1)).getDrawable().getAlpha() < 255){
            b64Image1 = "";
        }
        if (((ImageView) findViewById(R.id.ivEventImg2)).getDrawable().getAlpha() < 255){
            b64Image2 = "";
        }
        if (((ImageView) findViewById(R.id.ivEventImg3)).getDrawable().getAlpha() < 255){
            b64Image3 = "";
        }



        Event event = new Event(category,description,longitude,latidude,country,b64Image1,b64Image2,b64Image3);

        //create json object of userCred
        Gson gson = new Gson();
        String jsonEvent = gson.toJson(event);
        String jsonUserCred = gson.toJson(this.userCred);

        //send event to API
        HttpClient httpClient = new HttpClient(this);
        try {
            httpClient.authenticate(jsonUserCred,jsonEvent);
            //httpClient.postEvent(jsonEvent);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private String locateCountry(Double lat, Double lng) {
        String countryName = "None";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

            if (addresses.size() > 0)
            {
                countryName=addresses.get(0).getCountryName();
            }

        }
        catch (IOException e){
            e.printStackTrace();
        }
        return countryName;
    }

    private void disableSubmitReportButton() {
        ((Button)findViewById(R.id.btnSubmitReport)).setEnabled(false);
    }

    public void enableSubmitReportButton(){
        ((Button)findViewById(R.id.btnSubmitReport)).setEnabled(true);
    }

    //encode bitmap to b64String
    private String encodeToBase64(Bitmap image)
    {
        int quality= 10;
        Bitmap.CompressFormat compressFormat= Bitmap.CompressFormat.WEBP;
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        String b64String = Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
        byte[] b64Bytes = b64String.getBytes(StandardCharsets.UTF_8);

        return new String(b64Bytes,StandardCharsets.UTF_8);
    }

    public void openGallery(View v){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
        this.view = v;

    }

    //this is to open the dialog on the phone to select the image
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
                                    ((ImageView)findViewById(R.id.ivEventImg1)).setImageAlpha(255);
                                    break;
                                case R.id.ivEventImg2:
                                    ((ImageView)findViewById(R.id.ivEventImg2)).setImageBitmap(selectedImageBitmap);
                                    ((ImageView)findViewById(R.id.ivEventImg2)).setImageAlpha(255);
                                    break;
                                case R.id.ivEventImg3:
                                    ((ImageView)findViewById(R.id.ivEventImg3)).setImageBitmap(selectedImageBitmap);
                                    ((ImageView)findViewById(R.id.ivEventImg3)).setImageAlpha(255);
                                    break;
                            }
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        goMap.updateLocationUI();
    }

    public void setAccessTokentoUserCred(String token){
        this.userCred.setAccessToken(token);
    }

    public boolean isLocationPermissionGranted() {
        return locationPermissionGranted;
    }


    public void setLocationPermissionGranted(boolean b) {
        this.locationPermissionGranted = b;
    }
}