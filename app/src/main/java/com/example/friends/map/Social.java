package com.example.friends.map;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

public class Social extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        //TextView amigos = (TextView) findViewById(R.id.listaAmigos);
        Button buscar = (Button) findViewById(R.id.buscarAmigos);
        Button solicitudes = (Button) findViewById(R.id.solicitudesAmigos);
    }
}
