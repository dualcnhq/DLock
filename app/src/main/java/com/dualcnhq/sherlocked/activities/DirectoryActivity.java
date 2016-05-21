package com.dualcnhq.sherlocked.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dualcnhq.sherlocked.R;

public class DirectoryActivity extends BaseActivity implements View.OnClickListener {

    private String city_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            city_name = bundle.getString("city");
        }

        findViewById(R.id.police_stations).setOnClickListener(this);
        findViewById(R.id.fire_stations).setOnClickListener(this);
        findViewById(R.id.hospitals).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        String station = "";
        Intent intent = new Intent(this, DirectoryListActivity.class);

        switch (id) {
            case R.id.police_stations:
                station = getApplicationContext().getResources().
                        getString(R.string.police_stations).toUpperCase();
                break;
            case R.id.fire_stations:
                station = getApplicationContext().getResources().
                        getString(R.string.fire_stations).toUpperCase();
                break;
            case R.id.hospitals:
                station = getApplicationContext().getResources().
                        getString(R.string.hospitals).toUpperCase();
                break;
        }

        intent.putExtra("station", station);
        intent.putExtra("city", city_name);

        startActivity(intent);
    }

}
