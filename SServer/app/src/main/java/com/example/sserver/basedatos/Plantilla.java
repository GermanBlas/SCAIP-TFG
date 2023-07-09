package com.example.sserver.basedatos;


public class Plantilla {

    private String nombreAlarma;
    private String autoManual;
    private String argumentos;
    private String argumentosSet;


    public Plantilla() {
        //Constructor sin parámetros
    }
    public Plantilla(String nombreAlarma, String autoManual, String argumentos, String argumentosSet) {
        this.nombreAlarma = nombreAlarma;
        this.autoManual = autoManual;
        this.argumentos = argumentos;
        this.argumentosSet = argumentosSet;
    }

    public String getNombreAlarma() {
        return nombreAlarma;
    }
    public void setNombreAlarma(String nombreAlarma) {
        this.nombreAlarma = nombreAlarma;
    }

    public String getAutoManual() {
        return autoManual;
    }
    public void setAutoManual(String autoManual) {
        this.autoManual = autoManual;
    }

    public String getArgumentos() {
        return argumentos;
    }
    public void setArgumentos(String argumentos) {
        this.argumentos = argumentos;
    }

    public String getArgumentosSet() {
        return argumentosSet;
    }
    public void setArgumentosSet(String argumentosSet) {
        this.argumentosSet = argumentosSet;
    }
}



