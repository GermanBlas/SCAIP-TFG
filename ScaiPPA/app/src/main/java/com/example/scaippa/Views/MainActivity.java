package com.example.scaippa.Views;

import android.content.Intent;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.scaippa.R;
import com.example.scaippa.basedatos.MiBaseDatos;

import java.security.KeyStore;
import java.security.KeyStoreException;

public class MainActivity extends AppCompatActivity {

    private MiBaseDatos MDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logo_layout);
        MDB = new MiBaseDatos(getApplicationContext());
        String keystoreType = "PKCS12"; // o JKS si estás utilizando el formato JKS
        String keystoreProvider = "BC"; // Proveedor de seguridad, puede variar según tu entorno
        try {
            KeyStore keyStore = KeyStore.getInstance(keystoreType);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }

    }

    public void IrMenuInicio(View v) {

        Intent intent = new Intent(this, MenuInicio.class);
        startActivity(intent);

    }
}