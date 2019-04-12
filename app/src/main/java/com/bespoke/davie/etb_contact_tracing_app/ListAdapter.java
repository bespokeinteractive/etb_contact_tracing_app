package com.bespoke.davie.etb_contact_tracing_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> ID;
    ArrayList<String> S_Name;
    ArrayList<String> S_Full_Form;


    public ListAdapter(
            Context context2,
            ArrayList<String> id,
            ArrayList<String> sub_name,
            ArrayList<String> Sub_full
    )
    {

        this.context = context2;
        this.ID = id;
        this.S_Name = sub_name;
        this.S_Full_Form = Sub_full;
    }

    public int getCount() {
        // TODO Auto-generated method stub
        return ID.size();
    }

    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View child, ViewGroup parent) {

        Holder holder;

        LayoutInflater layoutInflater;

        if (child == null) {
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            child = layoutInflater.inflate(R.layout.items, null);

            holder = new Holder();

            holder.Subject_TextView = (TextView) child.findViewById(R.id.textViewSubject);
            holder.SubjectFullFormTextView = (TextView) child.findViewById(R.id.textViewSubjectFullForm);

            child.setTag(holder);

        } else {

            holder = (Holder) child.getTag();
        }
        holder.Subject_TextView.setText(S_Name.get(position));
        holder.SubjectFullFormTextView.setText(S_Full_Form.get(position));

        return child;
    }

    public class Holder {

        TextView Subject_TextView;
        TextView SubjectFullFormTextView;
    }

}