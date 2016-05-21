package com.dualcnhq.sherlocked.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Prefs {

    private static final String PREFS_NAME = "CLACK_PREFS";

    private static Prefs mIntstance;
    private SharedPreferences mSharedPreferences;

    private Prefs() {
    }

    public static Prefs getInstance() {
        if (mIntstance == null) {
            mIntstance = new Prefs();
        }
        return mIntstance;
    }

    public void setContext(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, 0);
    }

    public void setLocation(Location loc) {
        if (mSharedPreferences == null) {
            return;
        }
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putFloat("lat", (float) loc.getLatitude());
        editor.putFloat("lng", (float) loc.getLongitude());
        editor.commit();
    }

    public Location getLocation() {
        if (mSharedPreferences == null) {
            return null;
        }
        float lat = mSharedPreferences.getFloat("lat", -1);
        float lng = mSharedPreferences.getFloat("lng", -1);
        if (lat == -1 || lng == -1) {
            return null;
        }
        Location loc = new Location("");
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        return loc;
    }

    public void setCachedClacks(ArrayList<String> list) {
        if (mSharedPreferences == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String id : list) {
            sb.append(",");
            sb.append(id);
        }
        String ids = sb.toString().substring(1);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("clack_cache", ids);
        editor.commit();
    }

    public ArrayList<String> getCachedClacks() {
        if(mSharedPreferences.getString("clack_cache", null) != null) {
            return null;
        }
        String clackIdString = mSharedPreferences.getString("clack_cache", null);
        if (AppUtils.isEmpty(clackIdString)) {
            return null;
        }
        String[] clackIdArray = clackIdString.split(",");
        if (clackIdArray == null || clackIdArray.length == 0) {
            return null;
        }
        return new ArrayList<>(Arrays.asList(clackIdArray));
    }
}
