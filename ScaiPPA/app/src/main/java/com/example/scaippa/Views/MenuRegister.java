package com.example.scaippa.Views;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scaippa.R;
import com.example.scaippa.SCAIPWebApplication;

public class MenuRegister extends AppCompatActivity {

    private SCAIPWebApplication global;
    private com.example.scaippa.SCAIP.SCAIPListener SCAIPListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mensaje_layout);
        global = ((SCAIPWebApplication) getApplicationContext());
        SCAIPListener = global.getSCAIPListener();

        try {
            String callID = getIntent().getStringExtra("callID");
            Log.i("LogMensajes", "callID = " + callID);
        }catch (Exception e){
            Log.e("LogMensajes", "Exception = " + e);
        }


    }






}
