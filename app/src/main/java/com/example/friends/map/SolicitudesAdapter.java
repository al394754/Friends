package com.example.friends.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.friends.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Utils.HttpsRequest;

public class SolicitudesAdapter extends BaseAdapter implements ListAdapter {
    private List<String> solicitudesPendientes = new ArrayList<>();
    private String correoPersonal;
    private static String correoSolicitante;
    private Context context;
    private Solicitudes solicitudes;

    public SolicitudesAdapter(String correoPersonal, List<String> solicitudesPendientes, Context context){
        this.correoPersonal = correoPersonal;
        this.solicitudesPendientes = solicitudesPendientes;
        this.context = context;
        this.solicitudes = Solicitudes.instancia();
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.activity_solicitudes_fila, null);
        }

        TextView solicitudes_amigo = (TextView) view.findViewById(R.id.solicitudes_amigo);
        solicitudes_amigo.setText(solicitudesPendientes.get(pos));

        correoSolicitante = solicitudesPendientes.get(pos);

        Button agregar = (Button) view.findViewById(R.id.agregar);
        agregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    aceptarSolicitud(correoSolicitante);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Button rechazar = (Button) view.findViewById(R.id.rechazar);
        rechazar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    rechazarSolicitud(correoSolicitante);
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return view;
    }

    public void aceptarSolicitud(String emailSolicitante) throws JSONException, IOException {
        HttpsRequest.requestResponse(correoPersonal, emailSolicitante, true);
    }

    public void rechazarSolicitud(String emailSolicitante) throws JSONException, IOException {
        HttpsRequest.requestResponse(correoPersonal, emailSolicitante, false);
    }
}
