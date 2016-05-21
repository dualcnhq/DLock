package com.dualcnhq.sherlocked.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dualcnhq.sherlocked.R;
import com.dualcnhq.sherlocked.models.ContactNumbers;

import java.util.ArrayList;

public class ContactListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ContactNumbers> arrayList;

    @SuppressWarnings("unused")
    private static LayoutInflater inflater = null;

    public ContactListAdapter(Context context, ArrayList<ContactNumbers> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return this.arrayList.size();
    }

    @Override
    public ContactNumbers getItem(int position) {
        return this.arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View layout = convertView;
        if (layout == null) {
            layout = View.inflate(context, R.layout.layout_contact_item_row, null);
        }
        setContent(layout, position);
        return layout;
    }

    @SuppressLint("InflateParams")
    private void setContent(View layout, int position) {

        TextView txtName = (TextView) layout.findViewById(R.id.txtName);
        txtName.setText(arrayList.get(position).getName());

        TextView txt = (TextView) layout.findViewById(R.id.txtAddress);
        txt.setVisibility(View.GONE);
        String address = arrayList.get(position).getAddress();
        final String add = arrayList.get(position).getName();

        if (address != null) {
            txt.setVisibility(View.VISIBLE);
            txt.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + add));
                    context.startActivity(geoIntent);
                }
            });

            txtName.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + add));
                    context.startActivity(geoIntent);
                }
            });

            txt.setText(address);
        }

        LinearLayout pnlContacts = (LinearLayout) layout.findViewById(R.id.pnlContacts);
        ArrayList<String> arrayContacts = arrayList.get(position).getContactNumbers();
        pnlContacts.removeAllViews();

        for (int i = 0; i < arrayContacts.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout parent1 = (LinearLayout) inflater.inflate(R.layout.layout_list_row, null);
            TextView txt1 = (TextView) parent1.findViewById(R.id.txtContact);
            txt1.setText(arrayContacts.get(i));
            pnlContacts.addView(parent1);
        }

    }
}
