package com.example.sclock.basedatos;


public class Automatica {

    private String nombreAlarma;
    private String argumentosValor;
    private String tiempo;


    public Automatica() {
        //Constructor sin parámetros
    }
    public Automatica(String nombreAlarma, String argumentosValor, String tiempo) {
        this.nombreAlarma = nombreAlarma;
        this.argumentosValor = argumentosValor;
        this.tiempo = tiempo;
    }

    public String getNombreAlarma() {
        return nombreAlarma;
    }
    public void setNombreAlarma(String nombreAlarma) {
        this.nombreAlarma = nombreAlarma;
    }

    public String getArgumentosValor() {
        return argumentosValor;
    }
    public void setArgumentosValor(String argumentosValor) {this.argumentosValor = argumentosValor;}

    public String getTiempo() {
        return tiempo;
    }
    public void setTiempo(String tiempo) {
        this.tiempo = tiempo;
    }
}



