package com.dualcnhq.dlock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dualcnhq.dlock.R;

import java.util.ArrayList;

public class ListItemAdapter extends BaseAdapter {

    private ArrayList<String> arrayItems;

    private static LayoutInflater inflater = null;
    private int defaultLayout;

    public ListItemAdapter(Context context, ArrayList<String> arrayItems) {
        this.arrayItems = arrayItems;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultLayout = R.layout.layout_list_item_row;
    }

    @Override
    public int getCount() {
        return arrayItems.size();
    }

    @Override
    public String getItem(int position) {
        return arrayItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = convertView;
        if (layout == null) {
            layout = inflater.inflate(defaultLayout, null);
        }

        TextView txt = (TextView) layout.findViewById(R.id.listItemText);
        txt.setText(arrayItems.get(position));
        return layout;
    }

}
