package com.example.scaippa.Views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.scaippa.Lista_adaptador;
import com.example.scaippa.R;
import com.example.scaippa.basedatos.MiBaseDatos;
import com.example.scaippa.basedatos.Transaccion;

import java.util.ArrayList;

public class MenuTransacciones extends Activity {

    private ListView lista;
    private String callID = null;
    private MiBaseDatos MDB;
    private int type;

    private Button dualButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transaccion_dialogo);
        MDB=new MiBaseDatos(getApplicationContext());


        dualButton = (Button) findViewById(R.id.dualButton);

        try {
            callID = getIntent().getStringExtra("callID");
            type = getIntent().getIntExtra("type",0);
            if(type==1) {
                dualButton.setText("Return");
            }
            Log.i("LogMensajes", "callID = " + callID);
        }catch (Exception e){
            Log.e("LogMensajes", "Exception = " + e);
        }


        Refresh();
    }

    public void Refresh(){
        try {

            ArrayList<Transaccion> datos = MDB.recuperarTransacciones("callID='" + callID + "'");
            lista = (ListView) findViewById(R.id.ListView_listado);
            lista.setAdapter(new Lista_adaptador(this, R.layout.transaccion, datos) {
                @Override
                public void onEntrada(Object entrada, View view) {
                    if (entrada != null) {
                        TextView texto_paquete = (TextView) view.findViewById(R.id.paquete);
                        if (texto_paquete != null)
                            texto_paquete.setText(((Transaccion) entrada).getPaquete());
                    }
                }
            });

        }catch (Exception e){
            Log.e("LogMensajes", "Exception = " + e);
        }

    }

    public void Refresh(View v){
        Refresh();
    }

    public void DualButton(View v){
        if(type==1){
            finish();
        }else{
            Intent intent = new Intent(this, MenuMensajes.class);
            startActivity(intent);
        }
    }

}
