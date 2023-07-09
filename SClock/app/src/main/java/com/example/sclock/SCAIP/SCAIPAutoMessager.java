package com.example.sclock.SCAIP;

import android.gov.nist.javax.sip.header.CSeq;
import android.gov.nist.javax.sip.header.CallID;
import android.javax.sip.message.Request;
import android.util.Log;

import com.example.sclock.basedatos.Automatica;
import com.example.sclock.basedatos.MiBaseDatos;

import java.util.ArrayList;
import java.util.HashMap;

public class SCAIPAutoMessager {

    private static final int DELAY = 15000; // Delay inicial en milisegundos
    private static final int PERIOD = 40000; // Periodo en milisegundos

    private Thread requestThread;
    private boolean isRunning;

    private SCAIPWebApplication global;
    private MiBaseDatos MDB;
    private SCAIPConstruct SCAIPConstruct;
    private SCAIPListener SCAIPListener;

    private ArrayList<Automatica> lista_automaticas;
    private String nombreAlarma;
    private String argumentosValor;
    private String tiempo;
    private int timerCount = 1;

    public void MDBinsert(MiBaseDatos MDB){
        this.MDB=MDB;
    }
    public void setGlobal(SCAIPWebApplication global){
        this.global=global;
    }

    public void startSendingRequests() {
        if (requestThread != null && requestThread.isAlive()) {
            // El hilo ya está en ejecución, no se inicia nuevamente
            return;
        }

        isRunning = true;
        SCAIPConstruct = global.getSCAIPConstruct();
        SCAIPListener = global.getSCAIPListener();


        requestThread = new Thread(new Runnable() {
            @Override
            public void run() {

                //Ponemos un delay desde que se inicia
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Hilo eterno de envio de mensajes
                while (isRunning) {
                    try {
                        //Recupera toods los mensajes automaticos
                        Log.i("SCAIPAutoMessager", SCAIPListener.toString());
                        lista_automaticas = MDB.recuperarAutomaticas("");
                        //Por cada mensaje automatico mira el periodo y lo envia si procede
                        for (Automatica mensaje : lista_automaticas) {
                            nombreAlarma = mensaje.getNombreAlarma();
                            argumentosValor = mensaje.getArgumentosValor();
                            tiempo = mensaje.getTiempo();
                            switch (tiempo) {
                                case "1":
                                    logicaDeEnvio();
                                    break;
                                case "2":
                                    if (timerCount % 2 == 0) {
                                        Log.e("SCAIPAutoMessager", "2");
                                        logicaDeEnvio();
                                    }
                                    break;
                                case "5":
                                    if (timerCount % 5 == 0) {
                                        Log.e("SCAIPAutoMessager", "5");
                                        logicaDeEnvio();
                                    }
                                    break;
                                case "10":
                                    if (timerCount % 10 == 0) {
                                        Log.e("SCAIPAutoMessager", "10");
                                        logicaDeEnvio();
                                    }
                                    break;
                                default:
                                    Log.e("SCAIPAutoMessager", "Errocito");
                                    break;
                            }

                        }

                        try {
                            Thread.sleep(PERIOD);
                            timerCount++;
                            if(timerCount==11){
                                timerCount=1;
                            }
                        } catch (InterruptedException e) {
                            Log.e("SCAIPAutoMessager", String.valueOf(e));
                        }
                    }catch (Exception e){
                        Log.i("SCAIPAutoMessager", "Estoy vivo" + e);
                        try {
                            Thread.sleep(PERIOD);
                        } catch (InterruptedException ex) {
                            Log.e("SCAIPAutoMessager", String.valueOf(e));
                        }
                    }
                }
            }
        });

        requestThread.start();
    }

    public void stopSendingRequests() {
        isRunning = false;
        if (requestThread != null && requestThread.isAlive()) {
            requestThread.interrupt();
            requestThread = null;
        }
    }

    public void logicaDeEnvio(){
        //Divide los argumentos y sus valores
        String[] argval = argumentosValor.split("::");
        HashMap<String, String> mapa = new HashMap<String, String>();
        for (String args : argval) {
            String[] splitargs = args.split("=");
            mapa.put(splitargs[0], splitargs[1]);
        }
        //Construye el mensaje SCAIP
        String XMLMessage = SCAIPConstruct.CreateRequest(nombreAlarma, mapa);
        Request request = SCAIPListener.createRequestEnd(XMLMessage, Request.MESSAGE);
        CallID callIDHeader = (CallID) request.getHeader(CallID.NAME);
        String callID = callIDHeader.getCallId();
        CSeq cSeqHeader = (CSeq) request.getHeader(CSeq.NAME);
        Long cSeq = cSeqHeader.getSeqNumber();
        //Guarda el mensaje por un posible reenvio y lo envia
        MDB.insertarRemensaje(callID,cSeq,null,null,null,Request.MESSAGE,XMLMessage);
        SCAIPListener.sendRequest(request);
        Log.i("AutoMessager", "Request: " + request);
    }
}
