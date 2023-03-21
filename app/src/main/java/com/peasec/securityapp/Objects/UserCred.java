package com.peasec.securityapp.Objects;

import android.content.Context;
import android.provider.Settings;

import java.util.Random;

public class UserCred {

    private String username;
    private String password;
    private String fullname;
    private String accessToken;

    public UserCred(String username,String password, String fullname){
        this.username = username;
        this.password = password;
        this.fullname = fullname;
    }

    //constructor for first user creation
    public UserCred(Context context){
        this.username = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID) + generateRandomPassword(3);
        this.password = generateRandomPassword(12);
        this.fullname = Settings.Secure.getString(context.getContentResolver(), "bluetooth_name");
    }

    public String generateRandomPassword(int len) {
        String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz|!£$%&/=@#";
        Random RANDOM = new Random();

        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }
        return sb.toString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setAccessToken(String token){
        this.accessToken=token;
    }

}
