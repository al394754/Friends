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

import Utils.HttpsRequest;

public class FriendsAdapter extends BaseAdapter implements ListAdapter {//Clase para poder realizar el listado de amigos con botones
    private ArrayList<String> amigos = new ArrayList<String>(); //Listado de amigos
    private Context context;
    private static String correoAmigo; //Correo que usaremos para ubicar a nuestro amigo
    private String emailPropio; //Correo personal
    private int opcion = -1; //Variable para decidir cual de los dos botones se ha presionado

    public FriendsAdapter(String emailPropio, ArrayList<String> amigos, Context context){
        this.emailPropio = emailPropio;
        this.amigos = amigos;
        this.context = context;
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

        correoAmigo = amigos.get(pos);

        Button chat = (Button) view.findViewById(R.id.chat);
        Button map = (Button) view.findViewById(R.id.map_friend);

        return view;
    }

    public static String getCorreoAmigo(){
        return correoAmigo;
    }


}