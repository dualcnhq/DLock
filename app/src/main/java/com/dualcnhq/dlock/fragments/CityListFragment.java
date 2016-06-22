package com.dualcnhq.dlock.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dualcnhq.dlock.R;
import com.dualcnhq.dlock.activities.DirectoryActivity;
import com.dualcnhq.dlock.adapters.ListItemAdapter;
import com.dualcnhq.dlock.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by dualcnhq on 5/21/16.
 */

public class CityListFragment extends Fragment {

    @Bind(R.id.listCities)
    ListView lstView;

    private ArrayList<String> arrayNames;
    private ArrayList<CityListFragment.Item> arrayList;

    private static CityListFragment instance;
    public static CityListFragment getInstance() {
        if (instance == null) {
            synchronized (CityListFragment.class) {
                if (instance == null) instance = new CityListFragment();
            }
        }
        return instance;
    }

    public static CityListFragment newInstance() {
        return new CityListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_city_list, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        arrayNames =  new ArrayList<>();
        arrayList =  new ArrayList<>();

        loadFromAssets();
        lstView.setAdapter(new ListItemAdapter(getActivity(), arrayNames));
        lstView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DirectoryActivity.class);
                intent.putExtra("city", arrayList.get(position).getName());
                intent.putExtra("id", arrayList.get(position).getId());
                startActivity(intent);
            }
        });

    }

    private void loadFromAssets(){
        String citiesResponse = AppUtils.loadCitiesJSONFromAsset(getActivity());

        try{
            JSONObject jsonObject = new JSONObject(citiesResponse);
            String city = jsonObject.getString("cities");

            JSONArray cityJSON = new JSONArray(city);
            int size = cityJSON.length();
            for(int i=0;i<size;i++){
                JSONObject jsonObj = cityJSON.getJSONObject(i);
                CityListFragment.Item item = new CityListFragment.Item(jsonObj.getInt("id"), jsonObj.getString("name"));
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

}
