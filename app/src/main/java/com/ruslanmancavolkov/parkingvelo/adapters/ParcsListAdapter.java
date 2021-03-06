package com.ruslanmancavolkov.parkingvelo.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ruslanmancavolkov.parkingvelo.R;
import com.ruslanmancavolkov.parkingvelo.models.Parcs;

import java.util.List;

public class ParcsListAdapter extends RecyclerView.Adapter<ParcsListAdapter.MyViewHolder> {

    public Context context;
    private List<Parcs> parcs;
    public ParcsAdapterListener onClickListener;

    public ParcsListAdapter(Context context, List<Parcs> parcs, ParcsAdapterListener listener) {
        this.context = context;
        this.parcs = parcs;
        this.onClickListener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.parcs_listview_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Parcs item = parcs.get(position);
        holder.name.setText(item.getN());
        holder.shared.setChecked(item.s);

    }

    @Override
    public int getItemCount() {
        return parcs.size();
    }

    public void removeItem(int position){
        parcs.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Parcs item, int position){
        parcs.add(position, item);
        notifyItemInserted(position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView thumbnail;
        public Switch shared;
        public RelativeLayout viewBackground, viewForeground;
        public MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            shared = itemView.findViewById(R.id.shared);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

            shared.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.publishedOnClick(v, getAdapterPosition());
                }
            });

            viewForeground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.editionOnClick(v, getAdapterPosition());
                }
            });
        }
    }

    public interface ParcsAdapterListener {

        void publishedOnClick(View v, int position);
        void editionOnClick(View v, int position);
    }
}
