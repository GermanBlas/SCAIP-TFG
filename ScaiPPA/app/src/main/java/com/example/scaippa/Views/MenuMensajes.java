package com.example.scaippa.Views;

import android.content.Intent;
import android.javax.sip.header.Header;
import android.javax.sip.message.Request;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scaippa.R;
import com.example.scaippa.SCAIPWebApplication;

public class MenuMensajes extends AppCompatActivity {

    private SCAIPWebApplication global;
    private com.example.scaippa.SCAIP.SCAIPListener SCAIPListener;

    private TextView mensajeListenerName;
    private TextView mensajeListenerIP;
    private TextView mensajeListenerPort;
    private TextView mensajeListenerProxyIP;
    private TextView mensajeListenerProxyPort;
    private TextView mensajeListenerTransportProtocol;

    private EditText mensajeToSipUser;
    private EditText mensajeToSipAddress;
    private EditText mensajeToSipPort;
    private Switch mensajeSipMode;
    private EditText mensajeDataMessage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mensaje_layout);
        global = ((SCAIPWebApplication) getApplicationContext());
        SCAIPListener = global.getSCAIPListener();

        mensajeListenerName = (TextView) findViewById(R.id.mensajeListenerName);
        mensajeListenerIP = (TextView) findViewById(R.id.mensajeListenerIP);
        mensajeListenerPort = (TextView) findViewById(R.id.mensajeListenerPort);
        mensajeListenerProxyIP = (TextView) findViewById(R.id.mensajeListenerProxyIP);
        mensajeListenerProxyPort = (TextView) findViewById(R.id.mensajeListenerProxyPort);
        mensajeListenerTransportProtocol = (TextView) findViewById(R.id.mensajeListenerTransportProtocol);

        mensajeListenerName.setText(SCAIPListener.getListenerName());
        mensajeListenerIP.setText(SCAIPListener.getListenerIpAddress());
        mensajeListenerPort.setText(SCAIPListener.getListenerPort());
        mensajeListenerTransportProtocol.setText(SCAIPListener.getListenerTransportProtocol());

        if(SCAIPListener.getListenerProxyMode().equals("ON")) {
            mensajeListenerProxyIP.setText(SCAIPListener.getListenerIpAddressProxy());
            mensajeListenerProxyPort.setText(SCAIPListener.getListenerPortProxy());
        }

        Toast.makeText(getApplicationContext(),
                "Listener is ON", Toast.LENGTH_LONG).show();
    }

    public void SendMessage(View v){
        mensajeToSipUser = (EditText) findViewById(R.id.mensajeToSipUser);
        mensajeToSipAddress = (EditText) findViewById(R.id.mensajeToSipAddress);
        mensajeToSipPort = (EditText) findViewById(R.id.mensajeToSipPort);
        mensajeDataMessage = (EditText) findViewById(R.id.mensajeDataMessage);
        mensajeSipMode = (Switch) findViewById(R.id.mensajeSipMode);


        String toSipUser = mensajeToSipUser.getText().toString();
        String toSipAddress = mensajeToSipAddress.getText().toString();
        String toSipPort = mensajeToSipPort.getText().toString();
        String dataMessage = mensajeDataMessage.getText().toString();

        Request request;
        if (mensajeSipMode.isChecked()){
            request = SCAIPListener.createRequest(toSipAddress, toSipUser, toSipPort, Request.MESSAGE, dataMessage);
        } else{
            request = SCAIPListener.createRequest(toSipAddress, toSipUser, toSipPort, Request.INVITE);
        }
        Log.i("LogMain", "Request = " + request.toString());
        //Thread para enviar mensajes por red, necesario por android

        Header header = request.getHeader("Call-ID");
        String callID = header.toString().substring(9,41);


        // https://www.geeksforgeeks.org/how-to-fix-android-os-network-on-main-thread-exception-error-in-android-studio/
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
        Log.i("LogMain", "Request enviada");
        Toast.makeText(getApplicationContext(),
                "Request enviada", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, MenuTransacciones.class);
        Bundle b = new Bundle();
        b.putString("callID", callID);
        b.putInt("type",1);
        intent.putExtras(b);
        startActivity(intent);


    }
}
