package com.dualcnhq.sherlocked.activities;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.dualcnhq.sherlocked.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WitnessActivity extends BaseActivity {

    @Bind(R.id.witnessToolbar)
    Toolbar toolbar;
    @Bind(R.id.button_capture)
    ImageButton btnCapture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_witness);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Witness");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Still in development", Toast.LENGTH_SHORT).show();
            }
        });

    }
}