package com.dualcnhq.dlock.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dualcnhq.dlock.R;
import com.dualcnhq.dlock.activities.DashboardActivity;
import com.dualcnhq.dlock.utils.AppUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SignUpFragment extends Fragment {

    private static final String TAG = SignUpFragment.class.getSimpleName();

    private Context ctx;

    @Bind(R.id.signUpEmail)
    EditText emailText;
    @Bind(R.id.signUpUsername)
    EditText usernameText;
    @Bind(R.id.signUpPassword)
    EditText passwordText;
    @Bind(R.id.signUpRepeatPassword)
    EditText repeatPasswordText;
    @Bind(R.id.signUpStart)
    Button startButton;

    ProgressDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.layout_signup, null);
        //layoutView = root;
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
                String name = usernameText.getText().toString();
                String em = emailText.getText().toString();
                String pswd = passwordText.getText().toString();
                String repswd = repeatPasswordText.getText().toString();

                if (AppUtils.isInternetOn(ctx)) {
                    signUp(name, em, pswd, repswd);
                } else {
                    AppUtils.showToast(ctx, ctx.getResources().getString(R.string.connect_wireless));
                }
            }
        });
    }

    private void signUp(String username, String email,
                        String password, String repeatPassword) {
        boolean hasInvalid = isValidSignUpFields(username, email, password, repeatPassword);

        if (!hasInvalid) {
            // Set up a progress dialog
            dialog = new ProgressDialog(ctx);
            dialog.setMessage(getString(R.string.text_progress_signup));
            dialog.show();

            // Set up a new Parse user
            ParseUser user = new ParseUser();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(email);
            //user.put(FULL_NAME, "Full Name User");

            // Call the Parse signup method
            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        if (getActivity() != null) {
                            getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                            getActivity().finish();
                        }
                    }
                }
            });
        }
    }

    private boolean isValidSignUpFields(String username, String email,
                                        String password, String repeatPassword) {
        // Validate the sign up data
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
        if (!password.equals(repeatPassword)) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_mismatched_passwords));
        }
        if (email.length() == 0) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_blank_email));
        }
        if (!AppUtils.isValidEmail(email)) {
            if (validationError) {
                validationErrorMessage.append(getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getString(R.string.error_email_format));
        }


        validationErrorMessage.append(getString(R.string.error_end));

        // If there is a validation error, display the error
        if (validationError) {
            AppUtils.showToast(ctx, validationErrorMessage.toString());
        }

        return validationError;
    }

}
