package com.example.sclock.Vistas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.WearableLinearLayoutManager;

import com.example.sclock.R;
import com.example.sclock.basedatos.MiBaseDatos;
import com.example.sclock.basedatos.Plantilla;

import java.util.ArrayList;


public class ListaBotones  extends Activity {

    ArrayList<String> nombresAlarmas;
    private RecyclerView vistaScroll;

    private MiBaseDatos MDB;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_layout);

        MDB = new MiBaseDatos(getApplicationContext());
        //Insertamos las plantillas en la base de datos
        insertarPlantillas();

        //Vamos a crear una vista con scroll
        vistaScroll = (RecyclerView) findViewById(R.id.recyclerid);
        WearableLinearLayoutManager layoutManager = new WearableLinearLayoutManager(this);
        layoutManager.setSmoothScrollbarEnabled(true); //todo creo que sobra
        vistaScroll.setLayoutManager(layoutManager);

        //Recuperamos el nombre de cada tipo de alarmas
        ArrayList<Plantilla> plantillasBaseDatos = MDB.recuperarPlantillas("");
        nombresAlarmas = new ArrayList<String>();
        for (int i = 0; i < plantillasBaseDatos.size(); i++) {
            Plantilla plantilla = plantillasBaseDatos.get(i);
            nombresAlarmas.add(plantilla.getNombreAlarma());
        }

        //Creamos un adaptador personalizado para la vista
        //Hacemos override de un metodo para que al pulsar el boton llame al metodo de alarmas
        AdaptadorRecyclerView adaptadorVistaScroll = new AdaptadorRecyclerView(nombresAlarmas)
        {
            @Override
            public void onEntrada(Object entrada, View view) {
                if (entrada != null) {
                    Button boton = (Button) view.findViewById(R.id.buttonlista);
                    if (boton != null) {
                        String textoAlarma = String.valueOf(boton.getText());
                        boton.setOnClickListener(v -> {
                           GoAlarm(textoAlarma);
                        });
                    }
                }
            }
        };
        vistaScroll.setAdapter(adaptadorVistaScroll);

    }
    public void GoAlarm(String alarmText){
        //Recuperamos la plantilla de la alarma para ver el tipo de alarma
        String type = MDB.recuperarPlantillas("nombreAlarma='" + alarmText + "'").get(0).getAutoManual();
        //Dependiendo de ella vamos a la vista manual o automatica
        //todo podemos mejorar la inclusion del intent ese si es unico
        if(type.equals("Manual")){
            Intent intent = new Intent(this, SimpleAlarmView.class);
            Bundle b = new Bundle();
            b.putString("alarmText", alarmText);
            intent.putExtras(b);
            startActivity(intent);
        }else if(type.equals("Automatica")){
            Log.i("ListaBotones", "Tipo 2");
            Intent intent = new Intent(this, ChoiceAlarmView.class);
            Bundle b = new Bundle();
            b.putString("alarmText", alarmText);
            intent.putExtras(b);
            startActivity(intent);
        }else{
            Log.e("ListaBotones", "Error");
        }

    }

    private void insertarPlantillas(){

        MDB.insertarPlantilla("Help Assistance", "Manual","","");
        MDB.insertarPlantilla("Pill taken", "Manual", "ref::cid::dty::stc", "stc=001");
        MDB.insertarPlantilla("Heartbeat", "Automatica", "ref::mty::hbo::cid::dty", "mty=PI::hbo=001");
        MDB.insertarPlantilla("Battery Status","Automatica","","");
        MDB.insertarPlantilla("GPS Status","Automatica","","");
        MDB.insertarPlantilla("Temperature Status","Automatica","","");

    }
}
