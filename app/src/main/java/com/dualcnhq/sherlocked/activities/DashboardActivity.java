package com.dualcnhq.sherlocked.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.camnter.easyslidingtabs.widget.EasySlidingTabs;
import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.adapters.TabsFragmentAdapter;
import com.dualcnhq.sherlocked.fragments.CityListFragment;
import com.dualcnhq.sherlocked.fragments.PostFragment;
import com.dualcnhq.sherlocked.fragments.PrimaryContactFragment;
import com.dualcnhq.sherlocked.utils.PrefsUtils;

import java.util.LinkedList;
import java.util.List;

public class DashboardActivity extends BaseActivity {

    private EasySlidingTabs easySlidingTabs;
    private ViewPager easyVP;
    private TabsFragmentAdapter adapter;
    List<Fragment> fragments;

    private static final int READ_CONTACT_PERMISSION_REQUEST_CODE = 76;

    public static final String[] titles = { "Primary Contact", "City List", "Posts"};

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        this.initViews();
        this.initData();
        checkForPermissions();
    }

    private void initViews() {
        this.easySlidingTabs = (EasySlidingTabs) this.findViewById(R.id.easy_sliding_tabs);
        this.easyVP = (ViewPager) this.findViewById(R.id.easy_vp);
    }

    private void initData() {
        this.fragments = new LinkedList<>();
        PrimaryContactFragment first = PrimaryContactFragment.getInstance();
        CityListFragment second = CityListFragment.getInstance();
        PostFragment third = PostFragment.getInstance();

        this.fragments.add(first);
        this.fragments.add(second);
        this.fragments.add(third);

        this.adapter = new TabsFragmentAdapter(this.getSupportFragmentManager(), titles,
                this.fragments);
        this.easyVP.setAdapter(this.adapter);
        this.easySlidingTabs.setViewPager(this.easyVP);
    }

    private void checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            //queryContacts();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CALL_PHONE,
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            if (!TextUtils.isEmpty(PrefsUtils.getPrimaryContactNumber(getApplicationContext()))) {
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), "You need to pick a primary contact first.",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
