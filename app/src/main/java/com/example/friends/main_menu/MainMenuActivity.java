package com.example.friends.main_menu;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.map.Chat;
import com.example.friends.map.Friends;
import com.example.friends.R;
import com.example.friends.map.MapsActivity;


public class MainMenuActivity extends AppCompatActivity{

//    private String email = getIntent().getStringExtra("EMAIL");//Con esto obtenemos el email del login, debemos pasarlo al resto de actividades manualmente
    //private String email = "Alex@gmail.com";
    private String email = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);//poner la página en pantalla

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            email = extras.getString("EMAIL");
        }

        Button friendsButton = (Button) findViewById(R.id.friends_button); //Lista de amigos
        Button soloMapButton = (Button) findViewById(R.id.solo_map_button); //Mapa solo
    }

    public void onClickFriendsButton(View view){ showFriends(); }
    public void onClickMapButton(View view) { showMap(); }

    public void showFriends(){ //Muestra una lista de amigos cada uno con dos botones, uno para chat y otro para ubicación
        Intent intent = new Intent(getApplicationContext(), Friends.class);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
    }


    public void showMap(){ //Muestra el mapa pero solo de la ubicaciónd el propio usuario, no del resto (Función básica de Google Maps)
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
    }



}
