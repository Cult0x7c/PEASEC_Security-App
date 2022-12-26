package com.peasec.securityapp.Network;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpClient {

    private static final String serverUrl = "http://<server>";
    private static final MediaType JSON  = MediaType.get("application/json; charset=utf-8");
    private  OkHttpClient client;

    public HttpClient() {
        this.client = new OkHttpClient();
    }

    public String post(String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(serverUrl)
                .post(body)
                .build();
        try (Response response = this.client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}

