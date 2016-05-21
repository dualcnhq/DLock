package com.dualcnhq.sherlocked.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.data.Constants;
import com.dualcnhq.sherlocked.models.Post;
import com.dualcnhq.sherlocked.utils.AppUtils;
import com.dualcnhq.sherlocked.utils.Observable;

import butterknife.Bind;
import butterknife.ButterKnife;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PostActivity extends AppCompatActivity implements Constants, Observable.Observer {

    @Bind(R.id.goingOn)
    EditText mGoingOn;
    @Bind(R.id.textNumCharacters)
    TextView textNumCharacters;
    @Bind(R.id.textUserName)
    TextView textUsername;
    @Bind(R.id.postToolbar)
    Toolbar toolbar;

    private Location location;
    private Post post;
    private ProgressDialog progress;
    private boolean editMode = false;

    private static final String TAG = "PostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ParseUser.getCurrentUser() == null) {
            finish();
            return;
        }
        Observable.getInstance().registerObserver(Observable.NOTIFICATION_LOGGED_OUT, this);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        location = intent.getParcelableExtra(LOCATION);

        //get post for editing
        String postId = intent.getStringExtra(POST_ID);
        if (postId != null) {
//            editMode = true;
//            post = AppUtils.getEditedPost();
//            if (post == null) {
//                finish();
//            }
            String mes = post.getPost();
            mGoingOn.setText(mes);
            mGoingOn.setSelection(mes.length());
        }

        progress = new ProgressDialog(this);
        progress.setMessage("Posting message...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);

        findViewById(R.id.clackText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ParseUser user = ParseUser.getCurrentUser();
                if (user == null) {
                    return;
                }

                String postText = mGoingOn.getText().toString();
                if (AppUtils.isEmpty(postText)) {
                    Toast.makeText(PostActivity.this, getResources().getString(R.string.text_not_entered),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!editMode) {
                    post = new Post();
                    post.setUser(user);
                    post.setCount(0);
                    ParseGeoPoint geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                    post.setLocation(geoPoint);
                    post.setDeleted(false);
                    post.setInappropriateCount(0);
                    ParseACL acl = new ParseACL();
                    acl.setPublicReadAccess(true);
                    acl.setPublicWriteAccess(true);
                    post.setACL(acl);
                }
                post.setPost(postText);
                progress.show();
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (progress.isShowing()) {
                            progress.dismiss();
                        }
                        if (e != null) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.posting_clack_error)
                                    + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
//                            String mes = editMode ? getResources().getString(R.string.post_updated) :
//                                    getResources().getString(R.string.post_sent);
//                            Toast.makeText(getApplicationContext(), mes, Toast.LENGTH_SHORT).show();
//
//                            //add karma
//                            ClackUtils.incrementUserCredits(user, Constants.USER_KARMA_FOR_NEW_POST, new Runnable() {
//                                @Override
//                                public void run() {
//                                    Observable.getInstance().notifyObservers(Observable.NOTIFICATION_USER_KARMA_CHANGED, null);
//                                }
//                            });
                        }
                        finish();
                    }
                });
            }
        });

        textUsername.setText("@" + ParseUser.getCurrentUser().getUsername());
        textNumCharacters.setText("0");
        mGoingOn.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = mGoingOn.getText().toString();
                int len = text.length();
                if (len > AppUtils.CHARACTER_MAX_COUNT) {
                    mGoingOn.setText(text.substring(0, AppUtils.CHARACTER_MAX_COUNT));
                    mGoingOn.setSelection(AppUtils.CHARACTER_MAX_COUNT);
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.limit)
                                    + AppUtils.CHARACTER_MAX_COUNT + " "
                                    + getResources().getString(R.string.limit_exceeded),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                textNumCharacters.setText(String.valueOf(len));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
//
//        ClackUtils.loadUserPhoto(getApplicationContext(), ParseUser.getCurrentUser(), userImage,
//                (int) getResources().getDimension(R.dimen.ab_profile_image_size));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Observable.getInstance().unregisterObserver(Observable.NOTIFICATION_LOGGED_OUT, this);
    }

    /*************************************************************************************/

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void notify(int notification, Bundle data) {
        finish();
    }
}
