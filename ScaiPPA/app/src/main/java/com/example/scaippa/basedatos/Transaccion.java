package com.example.scaippa.basedatos;


public class Transaccion {

    private String callID;
    private int idtupla; //orden tuplas
    private String paquete;


    public Transaccion() {
        //Constructor sin parámetros
    }
    public Transaccion(String callID, int idtupla, String paquete) {
        this.callID = callID;
        this.idtupla = idtupla;
        this.paquete = paquete;
    }

    public String getCallID() {
        return callID;
    }
    public void setCallID(String callID) {
        this.callID = callID;
    }

    public int getIdtupla() {
        return idtupla;
    }
    public void setIdtupla(int idtupla) {
        this.idtupla = idtupla;
    }


    public String getPaquete() {
        return paquete;
    }
    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }
}



