package com.dualcnhq.sherlocked;

import android.app.Application;

import com.dualcnhq.lockscreenservice.data.Constants;
import com.parse.Parse;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

import static com.dualcnhq.lockscreenservice.data.Constants.*;
import static com.dualcnhq.sherlocked.data.Constants.APP_ID;
import static com.dualcnhq.sherlocked.data.Constants.CLIENT_KEY;

public class SherlockedApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

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
