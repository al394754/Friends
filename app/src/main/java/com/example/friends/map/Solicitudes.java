package com.example.friends.map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Utils.HttpsRequest;

public class Solicitudes extends AppCompatActivity { //Igual que en el listado de mostrar amigos
    private ArrayList<String> peticiones = new ArrayList<>();
    private String emailPersonal; //Mi correo
    private String emailSolicitud; //Correo de una solicitud
    private SolicitudesAux aux; //Auxiliar para poder conectar con google sheet
    private SolicitudesAdapter adapter;

    private ListView listado;

    private static Solicitudes solicitudes;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitudes);

        solicitudes = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            emailPersonal = extras.getString("EMAIL");
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBarSolicitudes);
        progressBar.setVisibility(View.VISIBLE);
        listado = (ListView) findViewById(R.id.listViewSolicitudes);

        aux = new SolicitudesAux();
        aux.execute((Void) null);

    }

    public void mensajePorPantalla(boolean resultado){
        if(resultado){
            Toast toast = Toast.makeText(getApplicationContext(), "Petición aceptada", Toast.LENGTH_LONG);
            toast.show();
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "Petición rechazada", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public void actualizaVista(){
        listado.invalidate();
        aux = new SolicitudesAux();
        aux.execute((Void) null);
    }

    public static Solicitudes instancia() {
        return solicitudes;
    }

    public class SolicitudesAux extends AsyncTask<Void, Void, Boolean> {

        private List<String> friendEmails = new ArrayList<String>();
        private List<String> friendNames = new ArrayList<String>();

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<List<String>> listas;
            try {
                listas = HttpsRequest.getRequestFriends(emailPersonal);
                if(listas.get(0).size() == 0){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast toast = Toast.makeText(getApplicationContext(), "No tiene solicitudes pendientes", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            friendEmails = listas.get(0);
            friendNames = listas.get(1);
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            aux = null;
            if (success) {
                adapter = new SolicitudesAdapter(emailPersonal, friendEmails, getApplicationContext());
                listado.setAdapter(adapter);
                progressBar.setVisibility(View.GONE);
            } else {
                System.out.println("Error");
            }
        }
    }
}
