package com.example.friends.map;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.friends.R;

import org.json.JSONException;

import Utils.HttpsRequest;


public class Friends extends AppCompatActivity{

    private ArrayList<String> amigos = new ArrayList<String>(); //Listado de todos los amigos
    private String emailPersonal; //Usado como segundo email para mapa y chat
    private String correoAmigo;
    private AccessFriends access = null;

    private ListView listado;
    private Button buscarAmigo;
    private Button verSolicitudes;

    private FriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            emailPersonal = extras.getString("EMAIL");
        }

        listado = (ListView) findViewById(R.id.listView);
        buscarAmigo = (Button) findViewById(R.id.buscarAmigos);
        verSolicitudes = (Button) findViewById(R.id.solicitudesAmigos);

        //adapter = new FriendsAdapter(emailPersonal, amigos, getApplicationContext());

        //emailPersonal = getIntent().getStringExtra("EMAIL");
        access = new AccessFriends();
        access.execute((Void) null);

    }

    public void onClickFriendMapButton(View view){ abrirMapa(emailPersonal); }
    public void onClickChatButton(View view){ abrirChat(emailPersonal); }
    public void onClickBuscarButton(View view){ buscarEmailAmigo(emailPersonal); }
    public void onClickSolicitudesButton(View view){ consultarSolicitudes(emailPersonal); }


    public void abrirMapa(String emailPropio){
        correoAmigo = FriendsAdapter.getCorreoAmigo();
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("EMAIL", emailPropio); //Usaremos estos extras para enviar a la actividad del mapa los dos posibles correos para sus ubicaciones
        intent.putExtra("EMAIL_AMIGO", correoAmigo);
        startActivity(intent);
    }

    public void abrirChat(String emailPropio){
        correoAmigo = FriendsAdapter.getCorreoAmigo();
        Intent intent = new Intent(getApplicationContext(), Chat.class);
        intent.putExtra("EMAIL", emailPropio); //Usaremos estos extras para enviar a la actividad del mapa los dos posibles correos para sus ubicaciones
        intent.putExtra("EMAIL_AMIGO", correoAmigo);
        startActivity(intent);
    }

    public void buscarEmailAmigo(String emailPropio){
        Intent intent = new Intent(getApplicationContext(), SolicitarAmigo.class);
        intent.putExtra("EMAIL", emailPropio); //Usaremos estos extras para enviar a la actividad del mapa los dos posibles correos para sus ubicaciones
        startActivity(intent);
    }

    public void consultarSolicitudes(String emailPropio){
        Intent intent = new Intent(getApplicationContext(), Solicitudes.class);
        intent.putExtra("EMAIL", emailPropio); //Usaremos estos extras para enviar a la actividad del mapa los dos posibles correos para sus ubicaciones
        startActivity(intent);
    }

    public class AccessFriends extends AsyncTask<Void, Void, Boolean> {

        private String cadena;
        private ArrayList<String> amigos = new ArrayList<String>();

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                cadena = HttpsRequest.getFriends(emailPersonal);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            amigos.addAll(Arrays.asList(cadena.replace("[", "").replace("]", "").replace("\"", "").split(","))); //pasar la cadena que obtenemos a lista
            return true;
        }
        @Override
        protected void onPostExecute(final Boolean success){
            access = null;
            if(success){
                adapter = new FriendsAdapter(emailPersonal, amigos, getApplicationContext());
                listado.setAdapter(adapter);
            }else{
                System.out.println("Error");
            }
        }

    }


}




//https://stackoverflow.com/questions/36787562/adding-a-button-to-each-row-of-a-list-view-in-android --> Listado
//https://stackoverflow.com/questions/40862154/how-to-create-listview-items-button-in-each-row
//https://www.youtube.com/watch?v=rN7x3ovWepM