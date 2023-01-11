package com.example.friends.map;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Utils.HttpsRequest;

public class FriendsAdapter extends BaseAdapter implements ListAdapter {//Clase para poder realizar el listado de amigos con botones
    private List<String> amigos = new ArrayList<String>(); //Listado de amigos
    private Context context;
    private static String correoAmigo; //Correo que usaremos para ubicar a nuestro amigo
    private String emailPropio; //Correo personal
    private Friends friends;

    public FriendsAdapter(String emailPropio, List<String> amigos, Context context){
        this.emailPropio = emailPropio;
        this.amigos = amigos;
        this.context = context;
        friends = Friends.instancia();
    }

    @Override
    public int getCount() {
        return amigos.size();
    }

    @Override
    public Object getItem(int i) {
        return amigos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_friends_fila, null);
        }

        TextView amigo = (TextView) view.findViewById(R.id.amigo);
        amigo.setText(amigos.get(pos));


        Button chat = (Button) view.findViewById(R.id.chat);
        Button borrarAmigo = (Button) view.findViewById(R.id.borrar);
        borrarAmigo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correoAmigo = amigos.get(pos);
                borrarAmigo(emailPropio);

            }
        });
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correoAmigo = amigos.get(pos);
                abrirChat(emailPropio);

            }
        });

        Button map = (Button) view.findViewById(R.id.map_friend);
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                correoAmigo = amigos.get(pos);
                abrirMapa(emailPropio);
            }
        });

        return view;
    }
    public void borrarAmigo(String emailPropio){
        new RemoveFriend(emailPropio,correoAmigo).execute();
    }
    public void abrirMapa(String emailPropio){
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra("EMAIL", emailPropio); //Usaremos estos extras para enviar a la actividad del mapa los dos posibles correos para sus ubicaciones
        intent.putExtra("EMAIL_AMIGO", correoAmigo);
        friends.startActivity(intent);
    }

    public void abrirChat(String emailPropio){
        Intent intent = new Intent(context, Chat.class);
        intent.putExtra("EMAIL", emailPropio); //Usaremos estos extras para enviar a la actividad del mapa los dos posibles correos para sus ubicaciones
        intent.putExtra("EMAIL_AMIGO", correoAmigo);
        friends.startActivity(intent);
    }
    public class RemoveFriend extends AsyncTask<Void, Void, Boolean> {

        private String emailAmigo;
        private String emailPropio = "";

        RemoveFriend(String emailUser, String emailAmigo) {
            this.emailAmigo = emailAmigo;
            this.emailPropio = emailUser;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                return HttpsRequest.removeFriend(emailPropio,emailAmigo);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //TODO actualizar pantalla
        }
    }
}
