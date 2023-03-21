package com.peasec.securityapp.Interface;

import com.peasec.securityapp.Objects.Event;

import java.util.List;

public interface HttpEventInterface {
    public void setAdapter(List<Event> eventList);
    public void showNoNetwork(int statusCode);
    public void showNoEvents();
}
