package com.dualcnhq.dlock;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;

import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static com.dualcnhq.dlock.data.Constants.APP_ID;
import static com.dualcnhq.dlock.data.Constants.CLIENT_KEY;

public class DLockApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

//        TwitterAuthConfig authConfig = new TwitterAuthConfig(
//                getResources().getString(R.string.twitter_key),
//                getResources().getString(R.string.twitter_secret));
//        Fabric.with(this, new Twitter(authConfig), new Crashlytics());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        Parse.initialize(this, APP_ID, CLIENT_KEY);
    }

}
