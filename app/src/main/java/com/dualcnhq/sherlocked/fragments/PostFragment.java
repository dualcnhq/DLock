package com.dualcnhq.sherlocked.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dualcnhq.sherlocked.R;

import butterknife.ButterKnife;

/**
 * Created by dualcnhq on 5/22/16.
 */

public class PostFragment extends Fragment {

    private static PostFragment instance;

    public static PostFragment getInstance() {
        if (instance == null) {
            synchronized (PostFragment.class) {
                if (instance == null) instance = new PostFragment();
            }
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}
