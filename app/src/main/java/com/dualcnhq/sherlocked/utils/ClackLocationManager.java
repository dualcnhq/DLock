package com.dualcnhq.sherlocked.utils;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.data.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class ClackLocationManager implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    private static final int FAST_CEILING_IN_SECONDS = 1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private static ClackLocationManager mInstance;
    private Activity mActivity;
    private Location mLastLocation;
    private Location mCurrentLocation;
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mFailedFrag;
    private boolean mIsConnecting;

    private ClackLocationManager(Activity activity) {
        mActivity = activity;

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        mLocationClient = new GoogleApiClient.Builder(activity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mIsConnecting = false;
    }

    public static ClackLocationManager getInstance(Activity activity) {
        if (activity == null) {
            mInstance = null;
            return null;
        }
        if (mInstance == null) {
            mInstance = new ClackLocationManager(activity);
        }
        return mInstance;
    }

    public void connect() {
        if (!locationServiceEnabled()) {
            return;
        }
        mFailedFrag = false;
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
            mIsConnecting = true;
        }
    }

    public void disconnect() {
        mFailedFrag = false;
        if (mLocationClient.isConnected()) {
            mLocationClient.disconnect();
            mIsConnecting = false;
        }
    }

    public Location getCurrentLocation() {
        return (mCurrentLocation == null) ? mLastLocation : mCurrentLocation;
    }

    private boolean servicesConnected() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mActivity);
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            return false;
        }
    }

    private Location getLocation() {
        if (servicesConnected()) {
            return LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        } else {
            return null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mIsConnecting = false;
        mFailedFrag = false;
        mCurrentLocation = getLocation();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        if (mLastLocation != null && AppUtils.geoPointFromLocation(location)
                .distanceInKilometersTo(AppUtils.geoPointFromLocation(mLastLocation)) < 0.01) {
            return;
        }
        mLastLocation = location;

        //notify observers about location change
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.LOCATION, (mCurrentLocation == null) ? mLastLocation : mCurrentLocation);
        Observable.getInstance().notifyObservers(Observable.NOTIFICATION_LOCATION_CHANGED, bundle);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        mIsConnecting = false;
        mFailedFrag = true;
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(mActivity, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
            }
        } else {
            Observable.getInstance().notifyObservers(Observable.NOTIFICATION_FAILED_TO_GET_LOCATION, null);
            AppUtils.showToast(mActivity, mActivity.getResources().getString(R.string.location_failed_error)
                    + " " + connectionResult.getErrorCode());
        }
    }

    public boolean locationServiceEnabled() {
        final LocationManager manager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    public boolean isFailed() {
        return mFailedFrag || !locationServiceEnabled();
    }

    public boolean isConnecting() {
        return mIsConnecting;
    }
}
