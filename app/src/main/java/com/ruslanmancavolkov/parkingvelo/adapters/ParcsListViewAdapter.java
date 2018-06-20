package com.ruslanmancavolkov.parkingvelo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ruslanmancavolkov.parkingvelo.R;
import com.ruslanmancavolkov.parkingvelo.models.Parcs;

import java.util.ArrayList;

public class ParcsListViewAdapter extends BaseAdapter {
    ArrayList<Parcs> data;
    Context context;
    LayoutInflater layoutInflater;

    public ParcsListViewAdapter(ArrayList<Parcs> data, Context context) {
        super();
        this.data = data;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Parcs getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ViewHolder holder = null;

        if(convertView == null)
        {
            vi = layoutInflater.inflate(R.layout.parcs_listview_row, null);
            holder = new ViewHolder((TextView) vi.findViewById(R.id.name));
            vi.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) vi.getTag();
        }

        Parcs parc = getItem(position);

        holder.tvName.setText(parc.getN());

        return vi;
    }
}

class ViewHolder{
    public TextView tvName;

    public ViewHolder(TextView tvName){
        this.tvName = tvName;
    }
}
