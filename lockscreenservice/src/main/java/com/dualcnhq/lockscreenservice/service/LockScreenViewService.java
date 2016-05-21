package com.dualcnhq.lockscreenservice.service;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dualcnhq.lockscreenservice.LockScreen;
import com.dualcnhq.lockscreenservice.LockScreenActivity;
import com.dualcnhq.lockscreenservice.LockScreenUtil;
import com.dualcnhq.lockscreenservice.R;
import com.dualcnhq.lockscreenservice.SharedPreferencesUtil;
import com.permissioneverywhere.PermissionEverywhere;
import com.permissioneverywhere.PermissionResponse;
import com.permissioneverywhere.PermissionResultCallback;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

import static android.support.v4.app.ActivityCompat.*;

public class LockScreenViewService extends Service {

    private final int LOCK_OPEN_OFFSET_VALUE = 50;
    private Context mContext = null;
    private LayoutInflater mInflater = null;
    private View mLockScreenView = null;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mParams;
    private RelativeLayout mBackgroundLayout = null;
    private RelativeLayout mBackgroundInLayout = null;
    private ImageView mBackgroundLockImageView = null;
    private RelativeLayout mForegroundLayout = null;
    private RelativeLayout mStatusBackgroundDummyView = null;
    private RelativeLayout mStatusForgroundDummyView = null;
    private ShimmerTextView mShimmerTextView = null;
    private boolean mIsLockEnable = false;
    private boolean mIsSoftkeyEnable = false;
    private int mDeviceWidth = 0;
    private int mDeviceDeviceWidth = 0;
    private float mLastLayoutX = 0;
    private int mServiceStartId = 0;
    private SendMessageHandler mMainHandler = null;
//    private boolean sIsSoftKeyEnable = false;

    private class SendMessageHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            changeBackGroundLockView(mLastLayoutX);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        SharedPreferencesUtil.init(mContext);
//        sIsSoftKeyEnable = SharedPreferencesUtil.get(LockScreen.ISSOFTKEY);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMainHandler = new SendMessageHandler();
        if (isLockScreenAble()) {
            if (null != mWindowManager) {
                if (null != mLockScreenView) {
                    mWindowManager.removeView(mLockScreenView);
                }
                mWindowManager = null;
                mParams = null;
                mInflater = null;
                mLockScreenView = null;
            }
            initState();
            initView();
            attachLockScreenView();
        }
        return LockScreenViewService.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        dettachLockScreenView();
    }

    private void initState() {

        mIsLockEnable = LockScreenUtil.getInstance(mContext).isStandardKeyguardState();
        if (mIsLockEnable) {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
        } else {
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                    PixelFormat.TRANSLUCENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mIsLockEnable && mIsSoftkeyEnable) {
                mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            } else {
                mParams.flags = WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
            }
        } else {
            mParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        if (null == mWindowManager) {
            mWindowManager = ((WindowManager) mContext.getSystemService(WINDOW_SERVICE));
        }
    }

    private void initView() {
        if (null == mInflater) {
            mInflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (null == mLockScreenView) {
            mLockScreenView = mInflater.inflate(R.layout.view_lock_screen, null);
        }

    }

    private boolean isLockScreenAble() {
        boolean isLock = SharedPreferencesUtil.get(LockScreen.ISLOCK);
        if (isLock) {
            isLock = true;
        } else {
            isLock = false;
        }
        return isLock;
    }


    private void attachLockScreenView() {

        if (null != mWindowManager && null != mLockScreenView && null != mParams) {
            mLockScreenView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
            mWindowManager.addView(mLockScreenView, mParams);
            settingLockView();
        }
    }

    private boolean dettachLockScreenView() {
        if (null != mWindowManager && null != mLockScreenView) {
            mWindowManager.removeView(mLockScreenView);
            mLockScreenView = null;
            mWindowManager = null;
            stopSelf(mServiceStartId);
            return true;
        } else {
            return false;
        }
    }

    private void settingLockView() {
        mBackgroundLayout = (RelativeLayout) mLockScreenView.findViewById(R.id.lockscreen_background_layout);
        mBackgroundInLayout = (RelativeLayout) mLockScreenView.findViewById(R.id.lockscreen_background_in_layout);
        mBackgroundLockImageView = (ImageView) mLockScreenView.findViewById(R.id.lockscreen_background_image);
        mForegroundLayout = (RelativeLayout) mLockScreenView.findViewById(R.id.lockscreen_forground_layout);
        mShimmerTextView = (ShimmerTextView) mLockScreenView.findViewById(R.id.shimmer_tv);
        (new Shimmer()).start(mShimmerTextView);
        mForegroundLayout.setOnTouchListener(mViewTouchListener);

        mStatusBackgroundDummyView = (RelativeLayout) mLockScreenView.findViewById(R.id.lockscreen_background_status_dummy);
        mStatusForgroundDummyView = (RelativeLayout) mLockScreenView.findViewById(R.id.lockscreen_forground_status_dummy);
        setBackGroundLockView();

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        mDeviceWidth = displayMetrics.widthPixels;
        mDeviceDeviceWidth = (mDeviceWidth / 2);
        mBackgroundLockImageView.setX((int) (((mDeviceDeviceWidth) * -1)));

        //kitkat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int val = LockScreenUtil.getInstance(mContext).getStatusBarHeight();
            RelativeLayout.LayoutParams forgroundParam = (RelativeLayout.LayoutParams) mStatusForgroundDummyView.getLayoutParams();
            forgroundParam.height = val;
            mStatusForgroundDummyView.setLayoutParams(forgroundParam);
            AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.5F);
            alpha.setDuration(0); // Make animation instant
            alpha.setFillAfter(true); // Tell it to persist after the animation ends
            mStatusForgroundDummyView.startAnimation(alpha);
            RelativeLayout.LayoutParams backgroundParam = (RelativeLayout.LayoutParams) mStatusBackgroundDummyView.getLayoutParams();
            backgroundParam.height = val;
            mStatusBackgroundDummyView.setLayoutParams(backgroundParam);
        }

        final String primaryContactNumber = SharedPreferencesUtil.getPrimaryContactNumber(getApplicationContext());
        Log.d(TAG, "primary: " + primaryContactNumber);

        Button sms = (Button) mLockScreenView.findViewById(R.id.btnSMS);
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMS(primaryContactNumber);
            }
        });

        Button call = (Button) mLockScreenView.findViewById(R.id.btnCall);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeACall(primaryContactNumber);
            }
        });
    }

    private void sendSMS(String primaryContactNumber){
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(primaryContactNumber, null, "HELP HELP", null, null);
    }

    private void makeACall(String primaryContactNumber) {
//        PermissionEverywhere.getPermission(getApplicationContext(), new String[]{Manifest.permission.CALL_PHONE},
//                1234, "My Awesome App", "This app needs a permission", R.drawable.lock).enqueue(new PermissionResultCallback() {
//            @Override
//            public void onComplete(PermissionResponse permissionResponse) {
//                Toast.makeText(LockScreenViewService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
//            }
//        });

        EndCallListener callListener = new EndCallListener();
        TelephonyManager mTM = (TelephonyManager) this.getSystemService(getApplicationContext().TELEPHONY_SERVICE);
        mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);

        final Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + primaryContactNumber));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callIntent.addFlags(Intent.FLAG_FROM_BACKGROUND);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            PermissionEverywhere.getPermission(getApplicationContext(), new String[]{Manifest.permission.CALL_PHONE},
//                    123, "My Awesome App", "This app needs a permission", R.drawable.lock).enqueue(new PermissionResultCallback() {
//                @Override
//                public void onComplete(PermissionResponse permissionResponse) {
//                    Toast.makeText(LockScreenViewService.this, "is Granted " + permissionResponse.isGranted(), Toast.LENGTH_SHORT).show();
//                    startActivity(callIntent);
//                }
//            });
            return;
        }
        startActivity(callIntent);
    }

    private static final String TAG = "LockScreenViewService";
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

    private void setBackGroundLockView() {
        if (mIsLockEnable) {
            mBackgroundInLayout.setBackgroundColor(getResources().getColor(R.color.lock_background_color));
            mBackgroundLockImageView.setVisibility(View.VISIBLE);

        } else {
            mBackgroundInLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            mBackgroundLockImageView.setVisibility(View.GONE);
        }
    }

    private void changeBackGroundLockView(float forgroundX) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (forgroundX < mDeviceWidth) {
                mBackgroundLockImageView.setBackground(getResources().getDrawable(R.drawable.lock));
            } else {
                mBackgroundLockImageView.setBackground(getResources().getDrawable(R.drawable.unlock));
            }
        } else {
            if (forgroundX < mDeviceWidth) {
                mBackgroundLockImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.lock));
            } else {
                mBackgroundLockImageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.unlock));
            }
        }
    }

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        private float firstTouchX = 0;
        private float layoutPrevX = 0;
        private float lastLayoutX = 0;
        private float layoutInPrevX = 0;
        private boolean isLockOpen = false;
        private int touchMoveX = 0;
        private int touchInMoveX = 0;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN: {// 0
                    firstTouchX = event.getX();
                    layoutPrevX = mForegroundLayout.getX();
                    layoutInPrevX = mBackgroundLockImageView.getX();
                    if (firstTouchX <= LOCK_OPEN_OFFSET_VALUE) {
                        isLockOpen = true;
                    }
                }
                break;
                case MotionEvent.ACTION_MOVE: { // 2
                    if (isLockOpen) {
                        touchMoveX = (int) (event.getRawX() - firstTouchX);
                        if (mForegroundLayout.getX() >= 0) {
                            mForegroundLayout.setX((int) (layoutPrevX + touchMoveX));
                            mBackgroundLockImageView.setX((int) (layoutInPrevX + (touchMoveX / 1.8)));
                            mLastLayoutX = lastLayoutX;
                            mMainHandler.sendEmptyMessage(0);
                            if (mForegroundLayout.getX() < 0) {
                                mForegroundLayout.setX(0);
                            }
                            lastLayoutX = mForegroundLayout.getX();
                        }
                    } else {
                        return false;
                    }
                }
                break;
                case MotionEvent.ACTION_UP: { // 1
                    if (isLockOpen) {
                        mForegroundLayout.setX(lastLayoutX);
                        mForegroundLayout.setY(0);
                        optimizeForground(lastLayoutX);
                    }
                    isLockOpen = false;
                    firstTouchX = 0;
                    layoutPrevX = 0;
                    layoutInPrevX = 0;
                    touchMoveX = 0;
                    lastLayoutX = 0;
                }
                break;
                default:
                    break;
            }

            return true;
        }
    };

    private void optimizeForground(float forgroundX) {
//        final int devideDeviceWidth = (mDeviceWidth / 2);
        if (forgroundX < mDeviceDeviceWidth) {
            int startPostion = 0;
            for (startPostion = mDeviceDeviceWidth; startPostion >= 0; startPostion--) {
                mForegroundLayout.setX(startPostion);
            }
        } else {
            TranslateAnimation animation = new TranslateAnimation(0, mDeviceDeviceWidth, 0, 0);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mForegroundLayout.setX(mDeviceDeviceWidth);
                    mForegroundLayout.setY(0);
                    dettachLockScreenView();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            mForegroundLayout.startAnimation(animation);
        }
    }

}
