package com.dualcnhq.lockscreenservice;

import android.content.Context;
import android.content.Intent;

import com.dualcnhq.lockscreenservice.service.LockScreenService;
import com.dualcnhq.lockscreenservice.service.LockScreenViewService;


public class LockScreen {

    private Context mContext = null;
    public static final String ISSOFTKEY = "ISSOFTKEY";
    public static final String ISLOCK = "ISLOCK";
    private static LockScreen mLockscreenInstance;

    public static LockScreen getInstance(Context context) {
        if (mLockscreenInstance == null) {
            if (null != context) {
                mLockscreenInstance = new LockScreen(context);
            }
            else {
                mLockscreenInstance = new LockScreen();
            }
        }
        return mLockscreenInstance;
    }

    private LockScreen() {
        mContext = null;
    }

    private LockScreen(Context context) {
        mContext = context;
    }

    public void startLockscreenService() {
        SharedPreferencesUtil.init(mContext);
        Intent startLockscreenIntent =  new Intent(mContext, LockScreenService.class);
//        startLockscreenIntent.putExtra(LockScreenService.LOCKSCREENSERVICE_FIRST_START, true);
        mContext.startService(startLockscreenIntent);

    }
    public void stopLockscreenService() {
        Intent stopLockscreenViewIntent =  new Intent(mContext, LockScreenViewService.class);
        mContext.stopService(stopLockscreenViewIntent);
        Intent stopLockscreenIntent =  new Intent(mContext, LockScreenService.class);
        mContext.stopService(stopLockscreenIntent);
    }
}
