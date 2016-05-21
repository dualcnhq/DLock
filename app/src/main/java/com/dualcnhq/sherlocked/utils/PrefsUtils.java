package com.dualcnhq.sherlocked.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.dualcnhq.sherlocked.data.Constants;

/**
 * Created by dualcnhq on 5/20/16.
 */

public class PrefsUtils {

    private static SharedPreferences prefs;

    public static void setPrimaryContactNumber(Context context, String value){
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PRIMARY_CONTACT, value);
        editor.apply();
    }

    public static String getPrimaryContactNumber(Context context){
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.PRIMARY_CONTACT, null);
    }

    // contact name getter and setter
    public static void setPrimaryContactName(Context context, String value){
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PRIMARY_CONTACT, value);
        editor.apply();
    }

    public static String getPrimaryContactName(Context context){
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(Constants.PRIMARY_CONTACT, null);
    }


    // lock checker
    public static void setIsLockEnabled(Context context, Boolean value){
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.IS_LOCK_ENABLED, value);
        editor.apply();
    }

    public static boolean getIsLockEnabled(Context context){
        prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.IS_LOCK_ENABLED, false);
    }

}
