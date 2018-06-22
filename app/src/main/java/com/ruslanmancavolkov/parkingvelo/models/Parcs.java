package com.ruslanmancavolkov.parkingvelo.models;

import com.ruslanmancavolkov.parkingvelo.utils.DateBuilder;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Parcs implements Serializable {

    // Identifiant de l'utilisateur
    public String id;

    // Nom du parc
    public String n;

    // Capacité du parc
    public Integer cp;

    // Date de création du parc par un utilisateur
    public String dc;

    // Booléen permettant de savoir si le parc créé par l'utilisateur est partagé
    public Boolean s;

    // Identifiant de l'utilisateur
    public String u;

    // Nombre de likes
    public Integer lc;

    // Nombre de dislikes
    public Integer dlc;

    public Parcs(){}

    public Parcs(String n, Integer cp, Boolean s, String u) {
        /*DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String strDate = dateFormat.format(date).toString();*/
        this.n = n;
        this.cp = cp;
        this.dc = DateBuilder.GetCurrentDate();
        this.s = s;
        this.u = u;
    }

    public Parcs(String n, Integer cp, String dc, Boolean s, String u) {
        this.n = n;
        this.cp = cp;
        this.dc = dc;
        this.s = s;
        this.u = u;
    }

    public Parcs(Parcs parc) {
        this.n = parc.getN();
        this.cp = parc.getCp();
        this.dc = parc.getDc();
        this.s = parc.getS();
        this.u = parc.getU();
    }

    public Parcs(String id, String n, Integer cp, String dc, Boolean s, String u, Integer lc, Integer dlc) {
        this.id = id;
        this.n = n;
        this.cp = cp;
        this.dc = dc;
        this.s = s;
        this.u = u;
        this.lc = lc;
        this.dlc = dlc;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }

    public Integer getCp() {
        return cp;
    }

    public void setCp(Integer cp) {
        this.cp = cp;
    }

    public String getDc() {
        return dc;
    }

    public void setDc(String dc) {
        this.dc = dc;
    }

    public Boolean getS() {
        return s;
    }

    public void setS(Boolean s) {
        this.s = s;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getU() {
        return u;
    }

    public void setU(String u) {
        this.u = u;
    }

    public Integer getLc() {
        return lc;
    }

    public void setLc(Integer lc) {
        this.lc = lc;
    }

    public Integer getDlc() {
        return dlc;
    }

    public void setDlc(Integer dlc) {
        this.dlc = dlc;
    }
}
