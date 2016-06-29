package org.juanjcsr.cloudotp.config;

import android.content.Context;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by jezz on 21/06/16.
 */
public class AccessTokenRetriever {


    private static String NAME = "cloud_providers";
    private static String DBSTORAGE = "db-access-token";
    private  Context mContext;

    public AccessTokenRetriever(Context aContext) {
        this.mContext = aContext;
    }

    public boolean dbTokenExists() {
        SharedPreferences prefs = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(DBSTORAGE, null);
        return accessToken != null;
    }

    public String retrieveDbAccessToken() {
        SharedPreferences prefs = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        String accessToken = prefs.getString(DBSTORAGE, null);
        if (accessToken == null ) {
            //Log.d("DB Access Token Status", "No Token found");
            return null;
        } else {
            //Log.d("DB Access Token Status", "Token found");
            return accessToken;
        }
    }

    public void setDbAccessToken(String theToken) {
        SharedPreferences prefs = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(DBSTORAGE, theToken).apply();
    }
}
