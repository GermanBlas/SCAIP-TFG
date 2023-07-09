package com.example.sclock.Vistas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sclock.R;

import java.util.ArrayList;

public abstract class AdaptadorRecyclerView extends RecyclerView.Adapter<AdaptadorRecyclerView.ViewHolderDatos> {

    ArrayList<String> listDatos;
    View view;

    public AdaptadorRecyclerView(ArrayList<String> listDatos) {
        this.listDatos = listDatos;
    }

    @NonNull
    @Override
    public ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lista_layout_button,null,false);
        return new ViewHolderDatos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderDatos holder, int position) {
        holder.asignarDatos(listDatos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return listDatos.size();
    }

    public abstract void onEntrada(Object entrada, View view);


    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        Button boton;

        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            boton = itemView.findViewById(R.id.buttonlista);
        }

        public void asignarDatos(String dato, int position) {
            boton.setText(dato);
            onEntrada(boton,view);

        }
    }
}