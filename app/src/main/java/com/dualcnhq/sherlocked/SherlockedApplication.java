package com.dualcnhq.sherlocked;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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
    }

}
