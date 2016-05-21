package com.dualcnhq.sherlocked.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.adapters.ListItemAdapter;
import com.dualcnhq.sherlocked.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CityListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ArrayList<String> arrayNames;
    private ArrayList<Item> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);
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
