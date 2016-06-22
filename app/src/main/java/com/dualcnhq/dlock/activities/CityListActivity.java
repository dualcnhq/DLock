package com.dualcnhq.dlock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dualcnhq.dlock.R;
import com.dualcnhq.dlock.adapters.ListItemAdapter;
import com.dualcnhq.dlock.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CityListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    @Bind(R.id.cityToolbar)
    Toolbar toolbar;

    private ArrayList<String> arrayNames;
    private ArrayList<Item> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select a city");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        arrayNames =  new ArrayList<>();
        arrayList =  new ArrayList<>();

        ListView lstView = (ListView)findViewById(R.id.listCities);

        loadFromAssets();
        lstView.setAdapter(new ListItemAdapter(this, arrayNames));
        lstView.setOnItemClickListener(this);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void loadFromAssets(){
        String citiesResponse = AppUtils.loadCitiesJSONFromAsset(getApplicationContext());

        try{
            JSONObject jsonObject = new JSONObject(citiesResponse);
            String city = jsonObject.getString("cities");

            JSONArray cityJSON = new JSONArray(city);
            int size = cityJSON.length();
            for(int i=0;i<size;i++){
                JSONObject jsonObj = cityJSON.getJSONObject(i);
                Item item = new Item(jsonObj.getInt("id"), jsonObj.getString("name"));
                arrayList.add(item);
                arrayNames.add(jsonObj.getString("name"));
            }
        } catch(JSONException e) {}
    }

    private class Item{
        int id;
        String name;

        Item(int id ,String name) {
            this.id = id;
            this.name = name;
        }


        public int getId() {
            return id;
        }
        public String getName() {
            return name;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(this,DirectoryActivity.class);
        intent.putExtra("city", arrayList.get(position).getName());
        intent.putExtra("id", arrayList.get(position).getId());
        startActivity(intent);
    }

}
