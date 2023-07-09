package com.example.sserver.basedatos;


public class Remensaje {

    private String callID;
    private Long cSeq; //orden tuplas
    private String ipAddress;
    private String name;
    private String port;
    private String method;
    private String message;


    public Remensaje() {
        //Constructor sin parámetros
    }
    public Remensaje(String callID, Long cSeq, String ipAddress, String name, String port, String method, String message) {
        this.callID = callID;
        this.cSeq = cSeq;
        this.ipAddress = ipAddress;
        this.name = name;
        this.port = port;
        this.method = method;
        this.message = message;
    }

    public String getCallID() {
        return callID;
    }
    public void setCallID(String callID) {
        this.callID = callID;
    }

    public Long getCSeq() {
        return cSeq;
    }
    public void setCSeq(Long cSeq) {this.cSeq = cSeq;
    }

    public String getIpAddress() {
        return ipAddress;
    }
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {return port;}
    public void setPort(String port) {
        this.port = port;
    }

    public String getMethod(){return method;}
    public void setMethod(String method){this.method = method;}

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }


}



