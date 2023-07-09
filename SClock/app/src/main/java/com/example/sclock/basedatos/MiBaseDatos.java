package com.example.sclock.basedatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringJoiner;

public class MiBaseDatos extends SQLiteOpenHelper {


    private static final int VERSION_BASEDATOS = 1;
    private static final String NOMBRE_BASEDATOS = "scaippa.db";
    private static final String TABLA_PLANTILLAS ="CREATE TABLE IF NOT EXISTS plantillas " +
            "(nombreAlarma STRING, autoManual STRING, argumentos STRING, argumentosSet STRING, PRIMARY KEY(nombreAlarma))";
    private static final String TABLA_AUTOMATICA ="CREATE TABLE IF NOT EXISTS automatica " +
            "(nombreAlarma STRING, argumentosValor STRING, tiempo STRING, PRIMARY KEY(nombreAlarma))";
    private static final String TABLA_REMENSAJES ="CREATE TABLE IF NOT EXISTS remensajes " +
            "(callID STRING, cSeq LONG, ipAddress STRING, name STRING, port STRING, method STRING, message STRING, PRIMARY KEY(callid, cSeq))";


    public MiBaseDatos(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TABLA_PLANTILLAS);
        db.execSQL(TABLA_AUTOMATICA);
        db.execSQL(TABLA_REMENSAJES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS plantillas"  + TABLA_PLANTILLAS);
        db.execSQL("DROP TABLE IF EXISTS automatica"  + TABLA_AUTOMATICA);
        db.execSQL("DROP TABLE IF EXISTS remensajes"  + TABLA_REMENSAJES);
        onCreate(db);
    }

    //Funciones tabla plantillas
    public boolean insertarPlantilla(String nombreAlarma, String autoManual, String argumentos, String argumentosSet) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("nombreAlarma", nombreAlarma);
            valores.put("autoManual", autoManual);
            valores.put("argumentos", argumentos);
            valores.put("argumentosSet", argumentosSet);
            salida=db.insert("plantillas", null, valores);
        }
        db.close();
        return(salida>0);
    }

    public boolean borrarPlantillas() {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("plantillas", null, null);
        }
        db.close();
        return(salida>0);
    }

    public ArrayList<Plantilla> recuperarPlantillas(String seleccion) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Plantilla> lista_plantillas = new ArrayList<Plantilla>();
        String[] valores_recuperar = {"nombreAlarma", "autoManual", "argumentos", "argumentosSet"};
        Cursor c = db.query("plantillas", valores_recuperar, seleccion, null, null, null, "nombreAlarma DESC", null);
        c.moveToFirst();
        do {
            Plantilla plantilla = new Plantilla(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
            lista_plantillas.add(plantilla);
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_plantillas;
    }




    //Funciones tabla automatica
    public boolean insertarAutomatica(String nombreAlarma, String argumentosValor, String tiempo) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("nombreAlarma", nombreAlarma);
            valores.put("argumentosValor", argumentosValor);
            valores.put("tiempo", tiempo);
            salida=db.insert("automatica", null, valores);
        }
        db.close();
        return(salida>0);
    }

    public boolean borrarAutomatica(String nombreAlarma) {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("automatica", "nombreAlarma='" + nombreAlarma + "'", null);
        }
        db.close();
        return(salida>0);
    }

    public boolean borrarAutomaticas() {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("automatica", null, null);
        }
        db.close();
        return(salida>0);
    }

    public ArrayList<Automatica> recuperarAutomaticas(String seleccion) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Automatica> lista_automatica = new ArrayList<Automatica>();
        String[] valores_recuperar = {"nombreAlarma", "argumentosValor", "tiempo"};
        Cursor c = db.query("automatica", valores_recuperar, seleccion, null, null, null, "nombreAlarma DESC", null);
        c.moveToFirst();
        do {
            Automatica automatica = new Automatica(c.getString(0), c.getString(1), c.getString(2));
            lista_automatica.add(automatica);
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_automatica;
    }

    //tabla remensajes
    public boolean insertarRemensaje(String callID, Long cSeq, String ipAddress, String name, String port, String method, String message) {
        long salida = 0;
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                ContentValues valores = new ContentValues();
                valores.put("callID", callID);
                valores.put("cSeq", cSeq);
                valores.put("ipAddress", ipAddress);
                valores.put("name", name);
                valores.put("port", port);
                valores.put("method", method);
                valores.put("message", message);
                salida = db.insert("remensajes", null, valores);
            }
            db.close();
        }catch (Exception e){
            Log.e("Log", "Error: " + e);
        }
        return(salida>0);
    }

    public boolean borrarRemensajes() {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("remensajes", null, null);
        }
        db.close();
        return(salida>0);
    }

    public ArrayList<Remensaje> recuperarRemensajes(String seleccion) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Remensaje> lista_remensajes = new ArrayList<Remensaje>();
        String[] valores_recuperar = {"callID", "cSeq", "ipAddress", "name", "port","method","message"};
        Cursor c = db.query("remensajes", valores_recuperar, seleccion, null, null, null, "cSeq DESC", null);
        c.moveToFirst();
        do {
            Remensaje remensaje = new Remensaje(c.getString(0), c.getLong(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6));
            lista_remensajes.add(remensaje);
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_remensajes;
    }


    public ArrayList<String> stringDecod (String stringCoded){

        String[] partes = stringCoded.split("::");
        ArrayList<String> stringDecod = new ArrayList<>(Arrays.asList(partes));

        return stringDecod;
    }

    public String stringCoded(ArrayList<String> stringDecod){

        StringJoiner joiner = new StringJoiner("::");
        for (String elemento : stringDecod) {
            joiner.add(elemento);
        }

        String stringCoded = joiner.toString();

        return stringCoded;
    }

    public void borrarTodo(){
        this.borrarRemensajes();
        this.borrarAutomaticas();
        this.borrarPlantillas();
    }


}
