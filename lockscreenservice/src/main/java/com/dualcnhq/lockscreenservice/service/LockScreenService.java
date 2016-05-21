package com.dualcnhq.lockscreenservice.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.dualcnhq.lockscreenservice.LockScreenActivity;
import com.dualcnhq.lockscreenservice.LockScreenUtil;

public class LockScreenService extends Service {
    private final String TAG = LockScreenService.class.getSimpleName();

    //    public static final String LOCKSCREENSERVICE_FIRST_START = "LOCKSCREENSERVICE_FIRST_START";
    private int mServiceStartId = 0;
    private Context mContext = null;


    private BroadcastReceiver mLockScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != context) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Intent intentLockScreen = new Intent(mContext, LockScreenViewService.class);
                    stopService(intentLockScreen);
                    TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    boolean isPhoneIdle = tManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
                    if (isPhoneIdle) {
                        startLockScreenActivity();
                    }
                }
            }
        }
    };

    private void stateReceiver(boolean isStartRecever) {
        if (isStartRecever) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mLockScreenReceiver, filter);
        } else {
            if (null != mLockScreenReceiver) {
                unregisterReceiver(mLockScreenReceiver);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        stateReceiver(true);
        Intent bundleIntent = intent;
        if (null != bundleIntent) {
            startLockScreenActivity();
        } else {
            Log.d(TAG, TAG + " onStartCommand intent NOT existed");
        }
        setLockGuard();
        return com.dualcnhq.lockscreenservice.service.LockScreenService.START_STICKY;
    }

    private void setLockGuard() {
        initKeyguardService();
        if (!LockScreenUtil.getInstance(mContext).isStandardKeyguardState()) {
            setStandardKeyguardState(false);
        } else {
            setStandardKeyguardState(true);
        }
    }

    private KeyguardManager mKeyManager = null;
    private KeyguardManager.KeyguardLock mKeyLock = null;

    private void initKeyguardService() {
        if (null != mKeyManager) {
            mKeyManager = null;
        }
        mKeyManager =(KeyguardManager)getSystemService(mContext.KEYGUARD_SERVICE);
        if (null != mKeyManager) {
            if (null != mKeyLock) {
                mKeyLock = null;
            }
            mKeyLock = mKeyManager.newKeyguardLock(mContext.KEYGUARD_SERVICE);
        }
    }

    private void setStandardKeyguardState(boolean isStart) {
        if (isStart) {
            if(null != mKeyLock){
                mKeyLock.reenableKeyguard();
            }
        }
        else {

            if(null != mKeyManager){
                mKeyLock.disableKeyguard();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stateReceiver(false);
        setStandardKeyguardState(true);
    }

    private void startLockScreenActivity() {
        Intent startLockScreenActIntent = new Intent(mContext, LockScreenActivity.class);
        startLockScreenActIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startLockScreenActIntent);
    }

}
