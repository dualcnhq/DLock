package com.dualcnhq.sherlocked.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dualcnhq.lockscreenservice.SharedPreferencesUtil;
import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.activities.DirectoryActivity;
import com.dualcnhq.sherlocked.activities.MainActivity;
import com.dualcnhq.sherlocked.adapters.ListItemAdapter;
import com.dualcnhq.sherlocked.utils.PrefsUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dualcnhq on 5/21/16.
 */

public class PrimaryContactFragment extends Fragment {

    @Bind(R.id.btnAddEditContact)
    Button addEditContact;
    @Bind(R.id.txtPrimaryContactDetails)
    TextView txtPrimary;

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int READ_CONTACT_PERMISSION_REQUEST_CODE = 76;

    private Uri uriContact;
    private String contactName;
    private String contactNumber;

    private static final String TAG = PrimaryContactFragment.class.getSimpleName();

    private static PrimaryContactFragment instance;

    public static PrimaryContactFragment getInstance() {
        if (instance == null) {
            synchronized (PrimaryContactFragment.class) {
                if (instance == null) instance = new PrimaryContactFragment();
            }
        }
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_primary_contact, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        addEditContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI),
                        PICK_CONTACT_REQUEST);
            }
        });

        setContactDetails();
    }

    private void setContactDetails(){

        if(TextUtils.isEmpty(PrefsUtils.getPrimaryContactName(getActivity()))) {
            txtPrimary.setText("Primary Contact Name: none" +
                    "\nPrimary Contact Number: none");
        } else {
            txtPrimary.setText("Primary Contact Name: " + PrefsUtils.getPrimaryContactName(getActivity()) +
                    "\nPrimary Contact Number: " + PrefsUtils.getPrimaryContactNumber(getActivity()));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Check which request it is that we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            if (resultCode == getActivity().RESULT_OK) {
                uriContact = intent.getData();
                retrieveContactNumber(intent);
            } else {
                Toast.makeText(getActivity(), "Selecting primary contact was cancelled", Toast.LENGTH_SHORT).show();
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
            cursor = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{id}, null);
            phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA);
            if (cursor.moveToFirst()) {
                while (cursor.isAfterLast() == false) {
                    phoneNumber = cursor.getString(phoneIdx);
                    allNumbers.add(phoneNumber);
                    cursor.moveToNext();
                }
            } else {
                Toast.makeText(getActivity(), "No phone number/s found.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            final CharSequence[] items = allNumbers.toArray(new String[allNumbers.size()]);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                Toast.makeText(getActivity(), "No phone number/s found.", Toast.LENGTH_SHORT).show();
                contactNumber = "";
            }
        }
    }

    private void setPrimaryContactNumber(String selectedNumber) {
        contactNumber = selectedNumber.replace("-", "");
        PrefsUtils.setPrimaryContactNumber(getActivity(), contactNumber);
        SharedPreferencesUtil.setPrimaryContactNumber(getActivity(), contactNumber);
        retrieveContactName();
    }

    private void retrieveContactName() {
        Cursor cursor = getActivity().getContentResolver().query(uriContact, null, null, null, null);
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        cursor.close();

        PrefsUtils.setPrimaryContactName(getActivity(), contactName);
        PrefsUtils.setIsPrimaryContactSet(getActivity(), true);
        showConfirmContactDialog();
    }

    private void showConfirmContactDialog() {
        final Dialog dialog = new Dialog(getActivity());
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
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_enable_lockscreen);
        TextView txtOK = (TextView) dialog.findViewById(R.id.textOK);
        txtOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                setContactDetails();
            }
        });

        dialog.show();
    }

}
