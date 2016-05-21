package com.dualcnhq.sherlocked.activities;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.service.GPSTracker;
import com.dualcnhq.sherlocked.utils.PrefsUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LockScreen1Activity extends BaseActivity {

    private static final String TAG = LockScreen1Activity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);


        Button unlock = (Button) findViewById(R.id.btnUnlock);
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //unlockHomeButton();
            }
        });

        final String primaryContactNumber = PrefsUtils.getPrimaryContactNumber(getApplicationContext());

        Button sms = (Button) findViewById(R.id.btnSMS);
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(primaryContactNumber, null, "HELP HELP", null, null);
            }
        });

        Button call = (Button) findViewById(R.id.btnCall);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeACall(primaryContactNumber);
            }
        });

        Button siren = (Button) findViewById(R.id.btnSiren);
        siren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // play siren here
            }
        });

        if (TextUtils.isEmpty(primaryContactNumber)) {
            sms.setEnabled(false);
            call.setEnabled(false);
        }
    }

    private void makeACall(String primaryContactNumber) {
        EndCallListener callListener = new EndCallListener();
        TelephonyManager mTM = (TelephonyManager) this.getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + primaryContactNumber));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            startActivity(callIntent);
            return;
        }
    }

    private class EndCallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                Log.i(TAG, "RINGING, number: " + incomingNumber);
            }
            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                //wait for phone to go offhook (probably set a boolean flag) so you know your app initiated the call.
                Log.i(TAG, "OFFHOOK");
            }
            if (TelephonyManager.CALL_STATE_IDLE == state) {
                //when this state occurs, and your flag is set, restart your app
                Log.i(TAG, "IDLE");
            }
        }
    }

    private String userLocation = "";

    /*---------- Listener class to get coordinates ------------- */
    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            Toast.makeText(getApplicationContext(),
                    "Location changed: Lat: " + loc.getLatitude() + " Lng: "
                            + loc.getLongitude(), Toast.LENGTH_SHORT).show();
            String longitude = "Longitude: " + loc.getLongitude();
            Log.v(TAG, "long: " + longitude);
            String latitude = "Latitude: " + loc.getLatitude();
            Log.v(TAG, "lat: " + latitude);


        /*------- To get city name from coordinates -------- */
            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            String s = longitude + "\n" + latitude + "\n\nMy Current City is: "
                    + cityName;
            userLocation = s;
            Log.d(TAG, "location cityName: " + s);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    private void initLoc() {
        Log.d(TAG, "initLocation");
        LocationManager locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            return;
        }

        // Create class object
        GPSTracker gps = new GPSTracker(getApplicationContext());

        // Check if GPS enabled
        if (gps.canGetLocation()) {
            Log.d(TAG, "enabled");

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line

            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            Log.d(TAG, "lat: " + latitude + " long: " + longitude);
        } else {
            Log.d(TAG, "nah");
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            //gps.showSettingsAlert();
        }
    }


}
