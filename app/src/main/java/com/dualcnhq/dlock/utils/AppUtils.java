package com.dualcnhq.dlock.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.dualcnhq.dlock.models.Post;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AppUtils {

    public static boolean SHOW_POSTS_WITHIN_EXPIRY_TIME = true;
    public static long POST_EXPIRY_TIME_HOURS = 24 * 7;
    public static int CHARACTER_MAX_COUNT = 320;
    public static int RADIUS = 1; //in km
    public static int POST_VOTES_FOR_AUTODELETE = -3;

    public static boolean SHOW_SNOOP_SCREEN = false;
    public static boolean SHOW_SNAP_SCREEN = true;
    public static boolean SHOW_HASH_TAG = false;
    public static boolean SHOW_NOTIFICATIONS_PANEL = false;
    public static boolean ENABLE_PUSH_NOTIFICATIONS = false;

    public static boolean DBG = false;

    public boolean appinstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    public static String loadCitiesJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data/cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String loadDirContactJSONFromAsset(Context context, String type) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data/" + type + ".json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String loadFAQJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data/faqs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static String loadHotLinesJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("data/hotlines.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    /****************************************************************************************************************/

    public static boolean isInternetOn(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager in = (InputMethodManager) context.getSystemService
                (Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * For validating email address
     * @param target
     * @return
     */
    public static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    public static boolean isMyPost(Post post) {
        if (post.getUser() == null) {
            return false;
        }
        return ParseUser.getCurrentUser().getObjectId().equals(post.getUser().getObjectId());
    }

    public static void shareVia(Context context, String text) {
        if (context == null || text == null || text.length() == 0) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        try {
            context.startActivity(intent);
        } catch (Throwable th) { }
    }

    public static String getTimeDiff(Date createdAt) {
        Date now = new Date();
        long timeDiff = now.getTime() - createdAt.getTime();
        long days = TimeUnit.MILLISECONDS.toDays(timeDiff);
        long w = days / 7;
        long d = days - w * 7;
        if (w != 0) {
            return d <= 3 ? w + "w" : (w + 1) + "w";
        }
        long h = TimeUnit.MILLISECONDS.toHours(timeDiff) - TimeUnit.DAYS.toHours(days);
        if (d != 0) {
            return h <= 12 ? d + "d" : (d + 1) + "d";
        }
        long m = TimeUnit.MILLISECONDS.toMinutes(timeDiff) - TimeUnit.HOURS.toMinutes(h) - TimeUnit.DAYS.toHours(days);

        return h == 0 ? m +"m" : m >= 30 ? (h + 1) + "h" : h + "h";
    }

}
