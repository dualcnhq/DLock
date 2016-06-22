package com.dualcnhq.dlock.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public abstract class AbsCommonFragment extends Fragment {

    protected void setToolBar(Toolbar toolbar) {
        if (toolbar == null) {
            return;
        }
        toolbar.setVisibility(View.VISIBLE);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(hasBackButton());
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    protected boolean hasBackButton() {
        return true;
    }

    protected void onBackPressed() {
        getActivity().onBackPressed();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //setToolBar(getToolbar());
    }

    public boolean goBack() {
        return false;
    }

    public abstract int getFragmentId();

    public void updateArguments(Bundle data) {

    }
}
