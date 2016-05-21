package com.dualcnhq.sherlocked.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dualcnhq.lockscreenservice.SharedPreferencesUtil;
import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.utils.PrefsUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int READ_CONTACT_PERMISSION_REQUEST_CODE = 76;

    private Uri uriContact;
    private String contactName;
    private String contactNumber;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addEdit = (Button) findViewById(R.id.btnAddEditContact);
        if (PrefsUtils.getIsPrimaryContactSet(getApplicationContext())) {
            addEdit.setText(getApplicationContext().getResources().getString(R.string.edit_contact) +
                    " no: " + PrefsUtils.getPrimaryContactNumber(getApplicationContext()));
        } else {
            addEdit.setText(getApplicationContext().getResources().getString(R.string.add_contact));
        }
        addEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkForContactPermission();
            }
        });

        Button directory = (Button) findViewById(R.id.btnDirectory);
        directory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CityListActivity.class));
            }
        });
    }

    private void checkForContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED) {
            queryContacts();
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
            queryContacts();
        }
    }

    private void queryContacts() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                PICK_CONTACT_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                uriContact = intent.getData();
                retrieveContactNumber(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Selecting primary contact was cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void retrieveContactNumber(Intent data) {
        Cursor cursor = null;
        String phoneNumber = "";
        List<String> allNumbers = new ArrayList<>();
        int phoneIdx;
        try {
            Uri result = data.getData();
            String id = result.getLastPathSegment();
            cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
            phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    phoneNumber = cursor.getString(phoneIdx);
                    allNumbers.add(phoneNumber);
                    cursor.moveToNext();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No phone number/s found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Choose a number");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    setPrimaryContactNumber(items[item].toString());
                }
            });
            AlertDialog alert = builder.create();
            if (allNumbers.size() > 1) {
                alert.show();
            } else {
                setPrimaryContactNumber(phoneNumber.toString());
            }

            if (phoneNumber.length() == 0) {
                Toast.makeText(getApplicationContext(), "No phone number/s found.", Toast.LENGTH_SHORT).show();
                contactNumber = "";
            }
        }
    }

    private void setPrimaryContactNumber(String selectedNumber) {
        contactNumber = selectedNumber.replace("-", "");
        PrefsUtils.setPrimaryContactNumber(getApplicationContext(), contactNumber);
        SharedPreferencesUtil.setPrimaryContactNumber(getApplicationContext(), contactNumber);
        retrieveContactName();
    }

    private void retrieveContactName() {
        Cursor cursor = getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();

        PrefsUtils.setPrimaryContactName(getApplicationContext(), contactName);
        PrefsUtils.setIsPrimaryContactSet(getApplicationContext(), true);
        showConfirmContactDialog();
    }

    private void showConfirmContactDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_contact);
        TextView txtYes = (TextView) dialog.findViewById(R.id.textYes);
        txtYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showEnableLockDialog();
            }
        });

        TextView txtNo = (TextView) dialog.findViewById(R.id.textNo);
        txtNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        TextView txtConfirmMessage = (TextView) dialog.findViewById(R.id.textConfirmContactMessage);
        txtConfirmMessage.setText("You picked " + contactName +
                " as your primary contact that uses this number: " + contactNumber +
                ".\nDo you want to save it?\n\n" +
                "(You can still edit it later)");

        dialog.show();
    }

    private void showEnableLockDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_enable_lockscreen);
        TextView txtOK = (TextView) dialog.findViewById(R.id.textOK);
        txtOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
