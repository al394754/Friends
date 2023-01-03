package com.example.friends.map;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

import java.util.ArrayList;

public class Solicitudes extends AppCompatActivity { //Igual que en el listado de mostrar amigos
    private ArrayList<String> peticiones = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_solicitudes);
    }
}
