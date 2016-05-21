package com.dualcnhq.sherlocked.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.adapters.ContactListAdapter;
import com.dualcnhq.sherlocked.models.ContactNumbers;
import com.dualcnhq.sherlocked.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DirectoryListActivity extends BaseActivity {

    private String city;
    private ListView lstView;
    private ArrayList<ContactNumbers> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_list);

        lstView = (ListView) findViewById(R.id.listContacts);
        arrayList = new ArrayList<>();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String station = bundle.getString("station");
            city = bundle.getString("city");

            if (station.equals(getResources().getString(R.string.police_stations).toUpperCase())) {
                loadFromAssets("police_station");
            } else if (station.equals(getResources().getString(R.string.fire_stations).toUpperCase())) {
                loadFromAssets("fire_stations");
            } else if (station.equals(getResources().getString(R.string.hospitals).toUpperCase())) {
                loadFromAssets("hospitals");
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
    }

    private void displayAlertDialog() {
        new AlertDialog.Builder(this)
                .setMessage("No contact information at this time")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).show();
    }

    private void loadFromAssets(String type) {
        String citiesResponse = AppUtils.loadDirContactJSONFromAsset(getApplicationContext(), type);

        try {
            JSONObject jsonObject = new JSONObject(citiesResponse);
            String data = jsonObject.getString(type);
            JSONObject dataJSON = new JSONObject(data);
            String cityJSON = dataJSON.getString(city);

            JSONArray json_city_station = new JSONArray(cityJSON);
            int size = json_city_station.length();
            for (int i = 0; i < size; i++) {
                JSONObject jsonData = json_city_station.getJSONObject(i);
                String name = jsonData.getString("name");
                String address = null;
                if (type.equals("hospitals")) {
                    if (!jsonData.isNull("address")) {
                        address = jsonData.getString("address");
                    }
                }

                ArrayList<String> arrayNumbers = new ArrayList<>();
                JSONArray jsonArrayNumbers = jsonData.getJSONArray("contactNum");

                int size1 = jsonArrayNumbers.length();
                for (int j = 0; j < size1; j++) {
                    JSONObject jsonData1 = jsonArrayNumbers.getJSONObject(j);
                    arrayNumbers.add(jsonData1.getString("telNum"));
                }
                ContactNumbers contactNum = new ContactNumbers(name, arrayNumbers, address);
                arrayList.add(contactNum);
            }

            if (arrayList != null) {
                lstView.setAdapter(new ContactListAdapter(this, arrayList));
            }
            if (size == 0) {
                displayAlertDialog();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
