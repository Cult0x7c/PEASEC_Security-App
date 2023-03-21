package com.peasec.securityapp.Objects;

import android.location.Geocoder;

public class Event {
    private final String country;
    private final String type;
    private final String content;
    private final double lng;
    private final double lat;
    private final String image1;
    private final String image2;
    private final String image3;

    /*
    private String category;
    private Pair<Double,Double> location;
    private String country;
    private String description;
    private String b64image;
    */

    public Event(String category, String content,double longitude, double latitude, String country, String image1, String image2, String image3){
        this.type = category;
        this.content = content;
        this.lng=longitude;
        this.lat=latitude;
        this.country= country;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
    }


    public String getCountry() {
        return country;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public double getLng() {
        return lng;
    }

    public double getLat() {
        return lat;
    }

    public String getImage1() {
        return image1;
    }

    public String getImage2() {
        return image2;
    }

    public String getImage3() {
        return image3;
    }
    /*
    public Event(String category, Double longitude, Double latidude, String country, String description, String b64image, String image3){

        this.country= country;
        this.image3 = image3;
        this.category = category;


        this.location= new Pair<Double,Double>(longitude,latidude);
        this.description = description;
        this.b64image= b64image;
    }*/
}
