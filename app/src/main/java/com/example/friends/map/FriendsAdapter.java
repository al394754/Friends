package com.example.friends.map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.friends.R;

import java.util.ArrayList;

public class FriendsAdapter extends BaseAdapter implements ListAdapter {//Clase para poder realizar el listado de amigos con botones
    private ArrayList<String> amigos = new ArrayList<String>();
    private Context context;

    public FriendsAdapter(ArrayList<String> amigos, Context context){
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
        System.out.println(amigos);
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_friends_fila, null);
        }

        TextView amigo = (TextView) view.findViewById(R.id.amigo);
        amigo.setText(amigos.get(pos));

        Button chat = (Button) view.findViewById(R.id.chat);

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO abrir chat
            }
        });

        Button map = (Button) view.findViewById(R.id.map_friend);

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO abrir mapa con persona
            }
        });return view;
    }
}
