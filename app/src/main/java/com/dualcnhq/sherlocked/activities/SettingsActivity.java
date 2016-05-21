package com.dualcnhq.sherlocked.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.dualcnhq.lockscreenservice.LockScreen;
import com.dualcnhq.lockscreenservice.SharedPreferencesUtil;
import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.utils.PrefsUtils;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingsActivity extends BaseActivity {

    @Bind(R.id.settingsToolbar)
    Toolbar toolbar;

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private SwitchCompat mSwitch = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Settings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        SharedPreferencesUtil.init(getApplicationContext());

        mSwitch = (SwitchCompat) this.findViewById(R.id.switchLockSetting);
        mSwitch.setTextOn("yes");
        mSwitch.setTextOff("no");
        mSwitch.setEnabled(false);

        Log.d(TAG, "isEnabled: " + PrefsUtils.getIsLockEnabled(getApplicationContext()));
        if(PrefsUtils.getIsLockEnabled(getApplicationContext())) {
            mSwitch.setEnabled(true);
        } else {
            mSwitch.setEnabled(false);
            checkDrawOverlayPermission();
        }

        boolean lockState = SharedPreferencesUtil.get(LockScreen.ISLOCK);
        if (lockState) {
            mSwitch.setChecked(true);
        } else {
            mSwitch.setChecked(false);
        }

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SharedPreferencesUtil.setBoolean(LockScreen.ISLOCK, true);
                    LockScreen.getInstance(getApplicationContext()).startLockscreenService();
                } else {
                    SharedPreferencesUtil.setBoolean(LockScreen.ISLOCK, false);
                    LockScreen.getInstance(getApplicationContext()).stopLockscreenService();
                }
            }
        });
    }

    /** code to post/handler request for permission */
    public final static int REQUEST_CODE = 2;

    public void checkDrawOverlayPermission() {
        /** check if we already  have permission to draw over other apps */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(SettingsActivity.this)){
                /** if not construct intent to request permission */
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE);
            }
        } else {
            setLockStatus(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // continue here - permission was granted
                    setLockStatus(true);
                } else {
                    Toast.makeText(getApplicationContext(), "Need permission to enable lock screen", Toast.LENGTH_SHORT).show();
                    setLockStatus(false);
                }
            } else {
                setLockStatus(true);
            }
        }
    }

    private void setLockStatus(boolean value){
        mSwitch.setEnabled(value);
        PrefsUtils.setIsLockEnabled(getApplicationContext(), value);
    }

}
