package com.example.friends.main_menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.map.Friends;
import com.example.friends.map.MapsActivity;
import com.example.friends.R;
import com.example.friends.map.Social;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import Utils.AESCrypt;
import Utils.HttpsRequest;


public class MainMenuActivity extends AppCompatActivity{

    //TODO Comprobación de buen inicio de sesión

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);//poner la página en pantalla


        Button friendsButton = (Button) findViewById(R.id.friends_button); //Lista de amigos
        Button socialButton = (Button) findViewById(R.id.social_button); //Peticiones de amistad
        Button soloMapButton = (Button) findViewById(R.id.solo_map_button); //Mapa solo
    }

    public void onClickFriendsButton(View view){ showFriends(); }
    public void onClickSocialButton(View view) { showSocial(); }
    public void onClickMapButton(View view) { showMap(); }

    public void showFriends(){ //Muestra una lista de amigos cada uno con dos botones, uno para chat y otro para ubicación
        Intent intent = new Intent(getApplicationContext(), Friends.class);
        startActivity(intent);
    }

    public void showSocial(){ //Muestra una lista de peticiones de amistad y permite poder agregar a usuarios
        Intent intent = new Intent(getApplicationContext(), Social.class);
        startActivity(intent);
    }

    public void showMap(){ //Muestra el mapa pero solo de la ubicaciónd el propio usuario, no del resto (Función básica de Google Maps)
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }



}
