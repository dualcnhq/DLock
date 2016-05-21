package com.dualcnhq.sherlocked.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.dualcnhq.sherlocked.R;

public class SplashActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        loadSplashHandler();
    }

    private void loadSplashHandler() {
        int interval = 1000;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                finish();
            }
        }, interval);
    }
}

