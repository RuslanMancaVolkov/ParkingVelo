package com.ruslanmancavolkov.parkingvelo.models;

import com.google.android.gms.maps.model.LatLng;

public class ParcWithPosition {
    public Parcs parc;
    public LatLng position;

    public ParcWithPosition(){}

    public ParcWithPosition(Parcs parc, LatLng position) {
        this.parc = parc;
        this.position = position;
    }

    public Parcs getParc() {
        return parc;
    }

    public void setParc(Parcs parc) {
        this.parc = parc;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }
}
