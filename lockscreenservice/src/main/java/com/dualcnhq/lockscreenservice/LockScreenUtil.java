package com.dualcnhq.lockscreenservice;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Created by mugku on 15. 5. 20..
 */
public class LockScreenUtil {

    private Context mContext = null;
    private static LockScreenUtil mLockScreenUtilInstance;

    public static LockScreenUtil getInstance(Context context) {
        if (mLockScreenUtilInstance == null) {
            if (null != context) {
                mLockScreenUtilInstance = new LockScreenUtil(context);
            }
            else {
                mLockScreenUtilInstance = new LockScreenUtil();
            }
        }
        return mLockScreenUtilInstance;
    }

    private LockScreenUtil() {
        mContext = null;
    }

    private LockScreenUtil(Context context) {
        mContext = context;
    }
    public boolean isStandardKeyguardState() {
        boolean isStandardKeyguqrd = false;
        KeyguardManager keyManager =(KeyguardManager) mContext.getSystemService(mContext.KEYGUARD_SERVICE);
        if (null != keyManager) {
            isStandardKeyguqrd = keyManager.isKeyguardSecure();
        }

        return isStandardKeyguqrd;
    }

    public boolean isSoftKeyAvail(Context context) {
        final boolean[] isSoftkey = {false};
        final View activityRootView = ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int rootViewHeight = activityRootView.getRootView().getHeight();
                int viewHeight = activityRootView.getHeight();
                int heightDiff = rootViewHeight - viewHeight;
                if (heightDiff > 100) { // 99% of the time the height diff will be due to a keyboard.
                    isSoftkey[0] = true;
                }
            }
        });
        return isSoftkey[0];
    }

    public int getStatusBarHeight(){
        int result=0;
        int resourceId= mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if(resourceId >0)
            result = mContext.getResources().getDimensionPixelSize(resourceId);

        return result;
    }
}
