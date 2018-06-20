package com.ruslanmancavolkov.parkingvelo.helpers;

import android.support.v7.widget.RecyclerView;

public interface RecyclerParcsTouchHelperListener {
    void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
}
