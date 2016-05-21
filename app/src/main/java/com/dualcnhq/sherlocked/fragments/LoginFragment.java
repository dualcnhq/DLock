package com.dualcnhq.sherlocked.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.activities.DashboardActivity;
import com.dualcnhq.sherlocked.activities.MainActivity;
import com.dualcnhq.sherlocked.utils.AppUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

public class LoginFragment extends Fragment {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private Context ctx;

    @Bind(R.id.loginEmailUser)
    EditText emailUsername;
    @Bind(R.id.loginPassword)
    EditText password;
    @Bind(R.id.loginStart)
    Button startButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.layout_login, null);
        ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ctx = getActivity();

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = emailUsername.getText().toString();
                String pswd = password.getText().toString();
                if (AppUtils.isInternetOn(ctx)) {
                    login(username, pswd);
                } else {
                    AppUtils.showToast(ctx, "Please connect to a wireless connection");
                }
            }
        });

    }

    private void login(String username, String password) {
        boolean hasInvalid = isValidLoginFields(username, password);

        if (!hasInvalid) {
            // Set up a progress dialog
            final ProgressDialog dialog = new ProgressDialog(ctx);
            dialog.setMessage("Logging in...");
            dialog.show();
            // Call the Parse login method
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    dialog.dismiss();
                    if (e != null) {
                        Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Logged in");
                        startActivity(new Intent(ctx, DashboardActivity.class));

                        getActivity().finish();
                    }
                }
            });
        }
    }

    private boolean isValidLoginFields(String username, String password) {
        // Validate the log in data
        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getString(R.string.error_intro));
        if (username.length() == 0) {
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_username));
        }
        if (password.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_password));
        }
        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            Toast.makeText(ctx, validationErrorMessage.toString(),
                    Toast.LENGTH_LONG).show();
        }

        return validationError;
    }

}