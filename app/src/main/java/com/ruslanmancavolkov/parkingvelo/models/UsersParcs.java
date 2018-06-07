package com.ruslanmancavolkov.parkingvelo.models;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class UsersParcs {

    // Date de création du parc par un utilisateur
    public String dc;

    // Booléen permettant de savoir si le parc créé par l'utilisateur est partagé
    public Boolean s;

    public UsersParcs(Boolean s) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        String strDate = dateFormat.format(date).toString();
        this.dc = strDate;
        this.s = s;
    }

}
