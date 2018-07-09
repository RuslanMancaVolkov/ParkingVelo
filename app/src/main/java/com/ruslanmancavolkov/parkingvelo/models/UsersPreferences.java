package com.ruslanmancavolkov.parkingvelo.models;

public class UsersPreferences {
    // True si l'utilisateur veut voir les parcs partagés
    public Boolean ss;

    // Capacité minimale
    public Integer cmi;

    // Capacité maximale
    public Integer cma;

    // Note minimale
    public Integer nmi;

    // Capacité maximale
    public Integer nma;

    public UsersPreferences(){}

    public UsersPreferences(Boolean ss, Integer cmi, Integer cma, Integer nmi, Integer nma) {
        this.ss = ss;
        this.cmi = cmi;
        this.cma = cma;
        this.nmi = nmi;
        this.nma = nma;
    }

    public Boolean getSs() {
        return ss;
    }

    public void setSs(Boolean ss) {
        this.ss = ss;
    }

    public Integer getCmi() {
        return cmi;
    }

    public void setCmi(Integer cmi) {
        this.cmi = cmi;
    }

    public Integer getCma() {
        return cma;
    }

    public void setCma(Integer cma) {
        this.cma = cma;
    }

    public Integer getNmi() {
        return nmi;
    }

    public void setNmi(Integer nmi) {
        this.nmi = nmi;
    }

    public Integer getNma() {
        return nma;
    }

    public void setNma(Integer nma) {
        this.nma = nma;
    }
}
