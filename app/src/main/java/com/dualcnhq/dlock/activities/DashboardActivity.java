package com.dualcnhq.dlock.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.camnter.easyslidingtabs.widget.EasySlidingTabs;
import com.dualcnhq.dlock.R;
import com.dualcnhq.dlock.adapters.TabFragmentAdapter;
import com.dualcnhq.dlock.adapters.TabsFragmentAdapter;
import com.dualcnhq.dlock.utils.AppUtils;
import com.dualcnhq.dlock.utils.ClackLocationManager;
import com.dualcnhq.dlock.utils.Prefs;
import com.dualcnhq.dlock.utils.PrefsUtils;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class DashboardActivity extends BaseActivity {

    private static final String TAG = DashboardActivity.class.getSimpleName();

    private EasySlidingTabs easySlidingTabs;
    private ViewPager easyVP;
    private TabsFragmentAdapter adapter;
    List<Fragment> fragments;
    
    private AlertDialog alertGPS;
    private AlertDialog alertNetwork;
    private boolean alertNetworkShown;
    private GpsChangeReceiver gpsChangeReceiver;

    private ClackLocationManager mClackLocationManager;

    private static final int READ_CONTACT_PERMISSION_REQUEST_CODE = 76;
    private FragmentPagerAdapter adapterViewPager;

    public static final String[] titles = {"Primary Contact", "City List", "Posts"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        checkForPermissions();

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new TabFragmentAdapter((getSupportFragmentManager()));
        vpPager.setAdapter(adapterViewPager);

        Prefs.getInstance().setContext(getApplicationContext());

        mClackLocationManager = ClackLocationManager.getInstance(DashboardActivity.this);

        gpsChangeReceiver = new GpsChangeReceiver();
        getApplicationContext().registerReceiver(gpsChangeReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        //this.initViews();
        //this.initData();
    }

//    private void initViews() {
//        this.easySlidingTabs = (EasySlidingTabs) this.findViewById(R.id.easy_sliding_tabs);
//        this.easyVP = (ViewPager) this.findViewById(R.id.easy_vp);
//    }

//    private void initData() {
//        this.fragments = new LinkedList<>();
//        PrimaryContactFragment first = PrimaryContactFragment.getInstance();
//        CityListFragment second = CityListFragment.getInstance();
//        PostFragment third = PostFragment.getInstance();
//
//        this.fragments.add(first);
//        this.fragments.add(second);
//        this.fragments.add(third);
//
//        this.adapter = new TabsFragmentAdapter(this.getSupportFragmentManager(), titles,
//                this.fragments);
//        this.easyVP.setAdapter(this.adapter);
//        this.easySlidingTabs.setViewPager(this.easyVP);
//    }

    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            // DO NOTHING
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                }, READ_CONTACT_PERMISSION_REQUEST_CODE);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == READ_CONTACT_PERMISSION_REQUEST_CODE
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                if (!TextUtils.isEmpty(PrefsUtils.getPrimaryContactNumber(getApplicationContext()))) {
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "You need to pick a primary contact first.",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_logout:
                Log.d(TAG, "Logging out...");
                if(AppUtils.isInternetOn(getApplicationContext())) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage(getResources().getString(R.string.logging_out));
                    progressDialog.show();
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            progressDialog.dismiss();
                            PrefsUtils.resetBooleanPrefs(getApplicationContext(), false);
                            PrefsUtils.resetStringPrefs(getApplicationContext(), "");
                            startActivity(new Intent(getApplicationContext(), DispatchActivity.class));
                            finish();
                        }
                    });
                } else {
                    AppUtils.showToast(getApplicationContext(), getResources().getString(R.string.network_disabled));
                }
                break;
            case R.id.action_witness:
                startActivity(new Intent(getApplicationContext(), WitnessActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    
    // GPS
    private class GpsChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mClackLocationManager.locationServiceEnabled()) {
                if (alertGPS != null && alertGPS.isShowing()) {
                    alertGPS.dismiss();
                }
                mClackLocationManager.connect();
                if (!AppUtils.isInternetOn(DashboardActivity.this)) {
                    showNetworkDisabledDialog();
                    return;
                }
            }
        }
    }

    @Override
    public void onStop() {
        mClackLocationManager.disconnect();
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!ClackLocationManager.getInstance(this).locationServiceEnabled()) {
            showLocationDisabledDialog();
            return;
        }
        mClackLocationManager.connect();
        if (!AppUtils.isInternetOn(this)) {
            showNetworkDisabledDialog();
            return;
        }
    }

    private void showLocationDisabledDialog() {
        if (alertGPS == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.location_disabled))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.action_settings), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            try {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            } catch (Throwable th) {
                                if (AppUtils.DBG) {
                                    Log.e(TAG, "Unable to start GPS settings activity");
                                }
                            }
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            alertGPS = builder.create();
        }
        alertGPS.show();
    }

    private void showNetworkDisabledDialog() {
        if (alertNetworkShown) {
            return;
        }
        if (alertNetwork == null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.network_disabled))
                    .setCancelable(false)
                    .setPositiveButton(getResources().getString(R.string.action_settings), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            alertNetwork.dismiss();
                            try {
                                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                            } catch (Throwable th) {
                                if (AppUtils.DBG) {
                                    Log.e(TAG, "Unable to start wifi settings activity");
                                }
                            }
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            alertNetwork = builder.create();
        }
        alertNetworkShown = true;
        alertNetwork.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getApplicationContext().unregisterReceiver(gpsChangeReceiver);
        ClackLocationManager.getInstance(null);
    }

}
