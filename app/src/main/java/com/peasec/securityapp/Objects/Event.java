package com.peasec.securityapp.Objects;

import android.util.Pair;

public class Event {
    private String category;
    private Pair<Double,Double> location;
    private String description;
    private String b64image;

    public Event(String category, Double longitude, Double latidude, String description, String b64image){
        this.category = category;
        this.location= new Pair<Double,Double>(longitude,latidude);
        this.description = description;
        this.b64image= b64image;
    }
}
