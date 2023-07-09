package com.example.scaippa.Views;

import android.content.Context;
import android.content.Intent;
import android.javax.sip.header.Header;
import android.javax.sip.message.Request;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scaippa.R;
import com.example.scaippa.SCAIP.SCAIPListener;
import com.example.scaippa.SCAIPWebApplication;
import com.example.scaippa.basedatos.MiBaseDatos;

public class MenuInicio extends AppCompatActivity {

    private SCAIPWebApplication global;

    private TextView inicioListenerName;
    private TextView inicioListenerIP;
    private EditText inicioListenerPort;
    private Switch inicioListenerTransportProtocol;
    private EditText inicioListenerProxyIP;
    private EditText inicioListenerProxyPort;
    private Switch inicioListenerProxyMode;
    

    private com.example.scaippa.SCAIP.SCAIPListener SCAIPListener;

    private MiBaseDatos MDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio_layout);
        global = ((SCAIPWebApplication) getApplicationContext());
        MDB = new MiBaseDatos(getApplicationContext());

        inicioListenerName = (EditText) findViewById(R.id.inicioListenerName);
        inicioListenerIP = (TextView) findViewById(R.id.inicioListenerIP);
        inicioListenerPort = (EditText) findViewById(R.id.inicioListenerPort);
        inicioListenerTransportProtocol = (Switch) findViewById(R.id.inicioListenerTransportProtocol);
        inicioListenerProxyIP = (EditText) findViewById(R.id.inicioListenerProxyIP);
        inicioListenerProxyPort = (EditText) findViewById(R.id.inicioListenerProxyPort);
        inicioListenerProxyMode = (Switch) findViewById(R.id.inicioListenerProxyMode);
       

        try {
            //Ip del telefono android y del router que esta conectado
            Context context = getApplicationContext();
            WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            Log.i("LogMain", "ip = " + ip);
            inicioListenerIP.setText(ip);

            final WifiManager manager = (WifiManager) super.getSystemService(WIFI_SERVICE);
            final DhcpInfo dhcp = manager.getDhcpInfo();
            final String ipAddress = Formatter.formatIpAddress(dhcp.gateway);
            Log.i("LogMain", "iprouter = " + ipAddress);
        } catch (Exception e) {
            Log.e("LogMain", "Exception = " + e);
        }

    }

    public void StartListener(View v){
        {
            try {


                //Inicializamos a White
                String name = inicioListenerName.getText().toString();
                String ipAddress = inicioListenerIP.getText().toString();
                String port = inicioListenerPort.getText().toString();
                String transportProtocol = "UDP";
                if(inicioListenerTransportProtocol.isChecked()){
                    transportProtocol = "TCP";}
                String ipAddressProxy = inicioListenerProxyIP.getText().toString();
                String portProxy = inicioListenerProxyPort.getText().toString();
                String proxyMode = "OFF";
                if(inicioListenerProxyMode.isChecked()){
                    proxyMode = "ON";
                }


                SCAIPListener = new SCAIPListener();
                SCAIPListener.init(name, ipAddress, port, ipAddressProxy, portProxy, transportProtocol, proxyMode);
                SCAIPListener.MDBinsert(MDB);
                global.setSCAIPListener(SCAIPListener);

                Log.e("LogMain", "Exception = " + ipAddressProxy);

                Toast.makeText(getApplicationContext(),
                        "Listener is ON", Toast.LENGTH_LONG).show();
                if(proxyMode.equals("ON")) {
                    Request request = SCAIPListener.createRequest(ipAddressProxy, name, portProxy, Request.REGISTER);
                    Header header = request.getHeader("Call-ID");
                    String callID = header.toString().substring(9,41);
                    Log.i("LogMain", "Request = " + request);
                    Log.i("LogMain", "callID = " + callID);
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
                    Intent intent = new Intent(this, MenuTransacciones.class);
                    Bundle b = new Bundle();
                    b.putString("callID", callID);
                    intent.putExtras(b);
                    startActivity(intent);
                }else{
                    Intent intent = new Intent(this, MenuMensajes.class);
                    startActivity(intent);
                }
            }catch (Exception e){
                Toast.makeText(getApplicationContext(),
                        "Error", Toast.LENGTH_LONG).show();
            }
        }


    }
}
