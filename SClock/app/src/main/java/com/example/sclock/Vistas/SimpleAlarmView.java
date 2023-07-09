package com.example.sclock.Vistas;

import android.app.Activity;
import android.gov.nist.javax.sip.header.CSeq;
import android.gov.nist.javax.sip.header.CallID;
import android.javax.sip.message.Request;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sclock.R;
import com.example.sclock.SCAIP.SCAIPConstruct;
import com.example.sclock.SCAIP.SCAIPListener;
import com.example.sclock.SCAIP.SCAIPWebApplication;
import com.example.sclock.basedatos.MiBaseDatos;

public class SimpleAlarmView extends Activity {

    private String nombreAlarma;
    private Button sendButton;
    private Button backButton;
    private TextView textView;
    private SCAIPWebApplication global;
    private SCAIPConstruct SCAIPConstruct;
    private SCAIPListener SCAIPListener;

    private MiBaseDatos MDB;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_alarm_layout);

        global = ((SCAIPWebApplication) getApplicationContext());
        SCAIPConstruct = global.getSCAIPConstruct();
        SCAIPListener = global.getSCAIPListener();
        MDB = new MiBaseDatos(getApplicationContext());

        nombreAlarma = getIntent().getStringExtra("alarmText");

        sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> {SendAlarm();});
        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {finish();});

        textView = (TextView) findViewById(R.id.text);
        textView.setText(nombreAlarma);
    }

    public void SendAlarm(){
        //Construimos el mensaje SCAIP
        String XMLMessage = SCAIPConstruct.CreateRequest(nombreAlarma);
        Request request = SCAIPListener.createRequestEnd(XMLMessage,Request.MESSAGE);

        //Guadamos el mensaje para un posible reenvio
        CallID callIDHeader = (CallID) request.getHeader(CallID.NAME);
        String callID = callIDHeader.getCallId();
        CSeq cSeqHeader = (CSeq) request.getHeader(CSeq.NAME);
        Long cSeg = cSeqHeader.getSeqNumber();
        MDB.insertarRemensaje(callID,cSeg,null,null,null,Request.MESSAGE,XMLMessage);
        Log.i("SimpleAlarmView", "Request : " + request);

        //Creamos un hilo debido a que no puede ser enviado un mensaje en el main thread
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    SCAIPListener.sendRequest(request);
                } catch (Exception e) {
                    Log.e("LogMain", "Exception = " + e);
                }
            }
        });
        thread.start();
        Toast.makeText(getApplicationContext(), "Alarma Enviada", Toast.LENGTH_LONG).show();
        //Despues de enviar la alarma vuelve al menu anterior
        finish();
    }
}
