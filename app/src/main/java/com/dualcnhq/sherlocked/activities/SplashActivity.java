package com.dualcnhq.sherlocked.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.fragments.LoginFragment;
import com.dualcnhq.sherlocked.fragments.SignUpFragment;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SplashActivity extends BaseActivity {

    @Bind(R.id.loginText)
    TextView login;
    @Bind(R.id.signUpText)
    TextView signUp;
    @Bind(R.id.layoutView)
    LinearLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLayoutLogin();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeLayoutSignup();
            }
        });

        login.performClick();

        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent ev) {
                hideKeyboard(view);
                return false;
            }
        });
    }

    protected void hideKeyboard(View view) {
        InputMethodManager in = (InputMethodManager) getApplicationContext().getSystemService
                (Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void changeLayoutLogin() {
        LoginFragment fragment = new LoginFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.inflateLayout, fragment);
        ft.commit();

        login.setBackgroundResource(R.drawable.hr_line);
        signUp.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

    private void changeLayoutSignup() {
        SignUpFragment fragment = new SignUpFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.inflateLayout, fragment);
        ft.commit();

        signUp.setBackgroundResource(R.drawable.hr_line);
        login.setBackgroundColor(getResources().getColor(android.R.color.transparent));
    }

//    private void loadSplashHandler() {
//        int interval = 1000;
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                //startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
//                finish();
//            }
//        }, interval);
//    }
}

