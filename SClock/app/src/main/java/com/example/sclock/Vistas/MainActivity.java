package com.example.sclock.Vistas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gov.nist.javax.sip.header.CSeq;
import android.gov.nist.javax.sip.header.CallID;
import android.javax.sip.message.Request;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;


import com.example.sclock.R;
import com.example.sclock.SCAIP.SCAIPAutoMessager;
import com.example.sclock.SCAIP.SCAIPConstruct;
import com.example.sclock.SCAIP.SCAIPListener;
import com.example.sclock.SCAIP.SCAIPWebApplication;
import com.example.sclock.basedatos.MiBaseDatos;
import com.example.sclock.basedatos.Remensaje;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class MainActivity extends Activity {


    private SCAIPListener SCAIPListener;
    private SCAIPWebApplication global;
    private SCAIPConstruct SCAIPConstruct;
    private MiBaseDatos MDB;

    private SCAIPAutoMessager SCAIPAutoMessager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        global = ((SCAIPWebApplication) getApplicationContext());
        MDB = new MiBaseDatos(getApplicationContext());
        MDB.borrarTodo();

        //Inicializar SCAIPConstruct
        try {
            Source schemaFile = new StreamSource(getResources().openRawResource(R.raw.xsd_scaip));
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(schemaFile);
            SCAIPConstruct = new SCAIPConstruct("01.00","836710811199107", "0002", schema);
            SCAIPConstruct.MDBinsert(MDB);
            global.setSCAIPConstruct(SCAIPConstruct);
        } catch (SAXException e) {
            Log.e("MainActivity", "Error :" + e);
        }

        //Guardamos los certificados en el almacenamiento del movil
        //Primero la keyStore
        try {
            InputStream keyStoreInputStream = getResources().openRawResource(R.raw.keystore);
            String directoryPath = getFilesDir().getPath();
            String keyStoreName = "keystore.bks";
            String keystorePath = directoryPath + "/" + keyStoreName;

            // Crea el archivo en el sistema de archivos
            File keyStore = new File(keystorePath);
            try {
                OutputStream keyStoreOutputStream = new FileOutputStream(keyStore);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = keyStoreInputStream.read(buffer)) > 0) {
                    keyStoreOutputStream.write(buffer, 0, length);
                }
                keyStoreOutputStream.close();
                keyStoreInputStream.close();
            } catch (IOException e) {
                Log.e("LogMain", "Error :" + e);
            }

            //Despues la trustStore
            InputStream trustStoreInputStream = getResources().openRawResource(R.raw.truststore);
            String trustStoreName = "truststore.bks";
            String trustStorePath = directoryPath + "/" + trustStoreName;

            // Crea el archivo en el sistema de archivos
            File trustStore = new File(trustStorePath);

            try {
                OutputStream trustStoreOutputStream = new FileOutputStream(trustStore);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = trustStoreInputStream.read(buffer)) > 0) {
                    trustStoreOutputStream.write(buffer, 0, length);
                }
                trustStoreOutputStream.close();
                trustStoreInputStream.close();
            } catch (IOException e) {
                Log.e("LogMain", "Error: " + e);
            }

            String keyStoreType = "BKS";
            String keyStorePassword = "SCAIPPa";
            String trustStoreType = "BKS";
            String trustStorePassword = "SCAIPPa";

            //Inicializamos las propiedades de la conexión SSL
            System.setProperty( "javax.net.ssl.keyStore",  keystorePath);
            System.setProperty( "javax.net.ssl.keyStorePassword",keyStorePassword);
            System.setProperty( "javax.net.ssl.keyStoreType", keyStoreType);
            System.setProperty( "javax.net.ssl.trustStore", trustStorePath);
            System.setProperty( "javax.net.ssl.trustStorePassword", trustStorePassword);
            System.setProperty( "javax.net.ssl.trustStoreType", trustStoreType);


            //Datos iniciales del Listener
            String name = "clienteSCAIP";
            String ipAddress = "10.0.2.16";
            String port = "5060";

            String ipAddressProxy = "192.168.1.251";
            String portProxy = "5060";
            String proxyMode = "ON";

            String nameEnd = "servidorSCAIP";
            String ipAddressEnd = "192.168.1.251";
            String portEnd = "5060";

            String transportProtocol = "TLS";
            //Si se usa el protocolo TLS los puertos son 5061
            if(transportProtocol == "TLS"){
                port = "5061";
                portProxy ="5061";
            }

            //Obtenemos ip del telefono android
            try {
                Context context = getApplicationContext();
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                Log.i("LogMain", "ip = " + ipAddress);

            } catch (Exception e) {
                Log.e("LogMain", "Exception = " + e);
            }

            //Inicializamos el Listener
            SCAIPListener = new SCAIPListener();
            SCAIPListener.init(name, ipAddress, port, ipAddressProxy, portProxy, transportProtocol, proxyMode, nameEnd, ipAddressEnd, portEnd);
            SCAIPListener.MDBinsert(MDB);
            global.setSCAIPListener(SCAIPListener);


            //Inicializamos el modulo de los mensajes automaticos
            try{
                SCAIPAutoMessager = new SCAIPAutoMessager();
                SCAIPAutoMessager.setGlobal(global);
                SCAIPAutoMessager.MDBinsert(MDB);
                SCAIPAutoMessager.startSendingRequests();
            }catch (Exception e){
                Log.e("MainActivity", "Error: " + e);
            }

            //Nos registramos en el proxy con un REQUEST
            String method = Request.REGISTER;
            Request request = SCAIPListener.createRequest(ipAddressProxy, name, portProxy, method);
            CallID callIDHeader = (CallID) request.getHeader(CallID.NAME);
            String callID = callIDHeader.getCallId();
            CSeq cSeqHeader = (CSeq) request.getHeader(CSeq.NAME);
            Long cSeq = cSeqHeader.getSeqNumber();
            MDB.insertarRemensaje(callID,cSeq, ipAddressProxy, name, portProxy, method, null);
            Remensaje remensaje = MDB.recuperarRemensajes("callID='" + callID + "'").get(0);
            Log.i("LogMain", "Request : " + request);
            SCAIPListener.MDBinsert(MDB);
            SCAIPListener.globalInsert(global);

            //Thread necesario para enviar mensajes en main thread
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

        }catch (Exception e){
            Log.e("LogMain", "Exception = " + e);
        }

    }
    public void GoMenu(View view){
        //Tras el registro nos vamos a la pantalla principal
        Intent intent = new Intent(this, ListaBotones.class);
        startActivity(intent);
    }
}