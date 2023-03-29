package com.peasec.securityapp.Network;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.RequestHeaders;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.gson.Gson;
import com.peasec.securityapp.Activities.Activity_HeatMap;
import com.peasec.securityapp.Activities.Activity_NewEvent;
import com.peasec.securityapp.Interface.HttpEventInterface;
import com.peasec.securityapp.Objects.Event;
import com.peasec.securityapp.Objects.UserCred;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class HttpClient {
    private final static String serverUrl = "http://104.248.253.183:5000";
    private final static String endPoint_PostEvent = "/report";
    private final static String endPoint_GetAllEvents = "/get_reports";
    private final static String endPoint_GetEventByID = "/report/";     //-- /report/{reportId} --
    private final static String endPoint_AuthenticateUser = "/authenticate";
    private final static String endPoint_CreateNewUser = "/user";

    private final AsyncHttpClient client;
    private final Activity activity;

    public HttpClient(Activity activity) {
        this.client = new AsyncHttpClient();

        this.activity=activity;
    }

    public void createNewUser(String jsonUserCred){
        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("HttpClient", String.valueOf(statusCode));
                HttpClient.this.activity.runOnUiThread(() -> {
                    Toast.makeText(HttpClient.this.activity, "Successfully created new user!", Toast.LENGTH_LONG).show();
                    Gson gson = new Gson();
                    UserCred userCred = gson.fromJson(jsonUserCred,UserCred.class);
                    ((Activity_HeatMap)HttpClient.this.activity).storeUserCredentials(userCred);
                });
            }
            @Override
            public void onFailure(int statusCode, Headers headers, java.lang.String response, Throwable throwable) {
                Log.d("HttpClient", String.valueOf(statusCode));
                HttpClient.this.activity.runOnUiThread(() ->{
                    Toast.makeText(HttpClient.this.activity,"Error creating new user" + "\n StatusCode: "+ String.valueOf(statusCode),Toast.LENGTH_LONG).show();
                });
            }
        };

        post(jsonUserCred,endPoint_CreateNewUser,jsonHttpResponseHandler);
    }

    public void getAllEvents(){
        JsonHttpResponseHandler jsonHttpResponseHandler= new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {

                //show no events for this location notification
                if(json.jsonArray.length()==0){
                    ((HttpEventInterface)HttpClient.this.activity).showNoEvents();
                }
                List<Event> eventList = new ArrayList<Event>();
                //get event objects from json Array
                for(int i=0;i<json.jsonArray.length();i++){
                    try {
                        Event event = new Gson().fromJson(json.jsonArray.get(i).toString(),Event.class);
                        eventList.add(event);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                /*deserialize JSON array to List<Event>
                Type listType = new TypeToken<ArrayList<Event>>(){}.getType();
                List<Event> eventList = new Gson().fromJson(String.valueOf(json),listType);
                */

                //fill adapter with events
                HttpClient.this.activity.runOnUiThread(() -> {
                        ((HttpEventInterface) HttpClient.this.activity).setAdapter(eventList);

                });
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                HttpClient.this.activity.runOnUiThread(() ->{
                    ((HttpEventInterface)HttpClient.this.activity).showNoNetwork(statusCode);
                    Toast.makeText(HttpClient.this.activity,"Error getting events from server" + "\n StatusCode: "+ String.valueOf(statusCode),Toast.LENGTH_LONG).show();
                });
            }
        };
        get(endPoint_GetAllEvents,jsonHttpResponseHandler);
    }

    private void postEvent(String jsonEvent,String token){
       JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("HttpClient", String.valueOf(statusCode));
                HttpClient.this.activity.runOnUiThread(() -> {
                    Toast.makeText(HttpClient.this.activity, "Successfully created event!", Toast.LENGTH_LONG).show();
                });
            }
            @Override
            public void onFailure(int statusCode, Headers headers, java.lang.String response, Throwable throwable) {
                Log.d("HttpClient", String.valueOf(statusCode));
                HttpClient.this.activity.runOnUiThread(() ->{
                    ((Activity_NewEvent) HttpClient.this.activity).enableSubmitReportButton();
                    if(statusCode==406){
                        Toast.makeText(HttpClient.this.activity, "Description contains hate speech \n Event was not created" + "\n StatusCode: " + String.valueOf(statusCode), Toast.LENGTH_LONG).show();
                    }
                    else {
                        Toast.makeText(HttpClient.this.activity, "Error creating event!" + "\n StatusCode: " + String.valueOf(statusCode), Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

       post(jsonEvent,token,endPoint_PostEvent,jsonHttpResponseHandler);
    }

    //get token from API
    public void authenticate(String jsonUser,String jsonEvent){
        JsonHttpResponseHandler jsonHttpResponseHandler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d("HttpClient", String.valueOf(statusCode));
                String token = null;
                try {
                    token = json.jsonObject.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                postEvent(jsonEvent,token);
                HttpClient.this.activity.runOnUiThread(() -> {
                    //Toast.makeText(HttpClient.this.activity, "Successfully authenticated", Toast.LENGTH_LONG).show();

                });
            }
            @Override
            public void onFailure(int statusCode, Headers headers, java.lang.String response, Throwable throwable) {
                Log.d("HttpClient", String.valueOf(statusCode));
                HttpClient.this.activity.runOnUiThread(() ->{
                    ((Activity_NewEvent) HttpClient.this.activity).enableSubmitReportButton();
                    Toast.makeText(HttpClient.this.activity,"Error authenticating user!" + "\n StatusCode: "+ String.valueOf(statusCode),Toast.LENGTH_LONG).show();
                });
            }
        };

        post(jsonUser,endPoint_AuthenticateUser,jsonHttpResponseHandler);
    }

    //get something from API
    private void get(String endPoint, JsonHttpResponseHandler jsonHttpResponseHandler){
        String endPointUrl = serverUrl + endPoint;
        this.client.get(endPointUrl,jsonHttpResponseHandler);
    }

    //post something to API
    private void post(String json,String endPoint,JsonHttpResponseHandler jsonHttpResponseHandler){
        String endPointUrl = serverUrl + endPoint;
        this.client.post(endPointUrl, json, jsonHttpResponseHandler);
    }

    //post something to API with request parameters
    private void post(String json,String token,String endPoint,JsonHttpResponseHandler jsonHttpResponseHandler){
        String endPointUrl = serverUrl + endPoint;

        RequestHeaders headers = new RequestHeaders();
        headers.put("Authorization","Bearer "+token);

        this.client.post(endPointUrl,headers,null,  json, jsonHttpResponseHandler);
    }
}
