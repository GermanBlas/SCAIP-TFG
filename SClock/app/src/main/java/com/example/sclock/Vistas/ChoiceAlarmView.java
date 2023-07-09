package com.example.sclock.Vistas;

import android.app.Activity;
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
import com.example.sclock.basedatos.Plantilla;

public class ChoiceAlarmView extends Activity {

    private String nombreAlarma;
    private Button firstButton;
    private Button secondButton;
    private Button thirdButton;
    private Button fourthButton;
    private Button fifthButton;
    private Button backButton;
    private TextView textView;

    private MiBaseDatos MDB;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choice_alarm_layout);
        MDB = new MiBaseDatos(getApplicationContext());

        nombreAlarma = getIntent().getStringExtra("alarmText");

        firstButton = (Button) findViewById(R.id.firstButton);
        firstButton.setOnClickListener(v -> {SendAlarm("1");});
        secondButton = (Button) findViewById(R.id.secondButton);
        secondButton.setOnClickListener(v -> {SendAlarm( "2");});
        thirdButton = (Button) findViewById(R.id.thirdButton);
        thirdButton.setOnClickListener(v -> {SendAlarm("5");});
        fourthButton = (Button) findViewById(R.id.fourthButton);
        fourthButton.setOnClickListener(v -> {SendAlarm("10");});
        fifthButton = (Button) findViewById(R.id.fifthButton);
        fifthButton.setOnClickListener(v -> {SendAlarm("0");});

        backButton = (Button) findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {finish();});
        textView = (TextView) findViewById(R.id.text);
        textView.setText(nombreAlarma);
    }

    public void SendAlarm(String botonPulsado){
        //Cargamos la alarma o la borramos
        if(botonPulsado.equals("0")){
            try{
                MDB.borrarAutomatica(nombreAlarma);
                Log.i("SimpleAlarmView", "Alarma Borrada");
                Toast.makeText(getApplicationContext(),"Alarma Borrada", Toast.LENGTH_LONG).show();
            }catch (Exception e){
                Log.e("ChoiceAlarm","Error: " + e);
            }
        }
        else{
            try {
                Plantilla plantilla = MDB.recuperarPlantillas("nombreAlarma='" + nombreAlarma + "'").get(0);
                MDB.insertarAutomatica(nombreAlarma, plantilla.getArgumentosSet(), botonPulsado);
                Log.i("SimpleAlarmView", "Alarma Programada");
                Toast.makeText(getApplicationContext(), "Alarma Programada", Toast.LENGTH_LONG).show();
            }catch (Exception e){
                Log.e("ChoiceAlarm","Error: " + e);
            }
        }
        //Despues de enviar la alarma vuelve al menu anterior
        finish();

    }

}
