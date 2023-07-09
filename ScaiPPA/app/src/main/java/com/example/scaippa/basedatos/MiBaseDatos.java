package com.example.scaippa.basedatos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class MiBaseDatos extends SQLiteOpenHelper {


    private static final int VERSION_BASEDATOS = 1;
    private static final String NOMBRE_BASEDATOS = "scaippa.db";
    private static final String TABLA_TRANSACCIONES ="CREATE TABLE IF NOT EXISTS transacciones " +
            "(callID STRING, idtupla INTEGER, paquete STRING, PRIMARY KEY(callid, idtupla))";


    public MiBaseDatos(Context context) {
        super(context, NOMBRE_BASEDATOS, null, VERSION_BASEDATOS);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(TABLA_TRANSACCIONES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS transacciones"  + TABLA_TRANSACCIONES);
        onCreate(db);
    }

    //funciones tabla transacciones
    public boolean insertarTransaccion(String callID, int idt, String paquete) {
        long salida=0;
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            ContentValues valores = new ContentValues();
            valores.put("callID", callID);
            if(idt!=0)
                valores.put("idtupla", idt);
            valores.put("paquete", paquete);
            salida=db.insert("transacciones", null, valores);
        }
        db.close();
        return(salida>0);
    }

    public boolean borrarTransaccion(String callID) {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("transacciones", "nombreUsuario='" + callID + "'", null);
        }
        db.close();
        return(salida>0);
    }

    public boolean borrarTransacciones() {
        SQLiteDatabase db = getWritableDatabase();
        long salida=0;
        if (db != null) {
            salida=db.delete("transacciones", null, null);
        }
        db.close();
        return(salida>0);
    }

    public ArrayList<Transaccion> recuperarTransacciones() {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Transaccion> lista_transacciones = new ArrayList<Transaccion>();
        String[] valores_recuperar = {"callID", "idtupla", "paquete"};
        Cursor c = db.query("transacciones", valores_recuperar, null, null, null, null, null, null);
        c.moveToFirst();
        do {
            Transaccion transaccion = new Transaccion(c.getString(0), c.getInt(1), c.getString(2));
            lista_transacciones.add(transaccion);
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_transacciones;
        }

    public ArrayList<Transaccion> recuperarTransacciones(String seleccion) {
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Transaccion> lista_transacciones = new ArrayList<Transaccion>();
        String[] valores_recuperar = {"callID", "idtupla", "paquete"};
        Cursor c = db.query("transacciones", valores_recuperar, seleccion, null, null, null, "idtupla DESC", null);
        c.moveToFirst();
        do {
            Transaccion transaccion = new Transaccion(c.getString(0), c.getInt(1), c.getString(2));
            lista_transacciones.add(transaccion);
        } while (c.moveToNext());
        db.close();
        c.close();
        return lista_transacciones;
    }


}
