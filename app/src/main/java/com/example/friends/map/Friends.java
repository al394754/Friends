package com.example.friends.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

import org.json.JSONException;

import Utils.HttpsRequest;


public class Friends extends AppCompatActivity{

    private ArrayList<String> friendEmails = new ArrayList<String>(); //Listado de todos los amigos
    private String emailPersonal; //Usado como segundo email para mapa y chat
    private AccessFriends access = null;

    private ListView listado;
    private Button buscarAmigo;
    private Button verSolicitudes;
    private View mProgressView;
    private static Friends friend;

    private FriendsAdapter adapter;
    public void onPostResume() {
        super.onPostResume();
        showProgress(true);
        access = new AccessFriends();
        access.execute((Void) null);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friend = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            emailPersonal = extras.getString("EMAIL");
        }

        listado = (ListView) findViewById(R.id.listViewSolicitudes);
        buscarAmigo = (Button) findViewById(R.id.buscarAmigos);
        verSolicitudes = (Button) findViewById(R.id.solicitudesAmigos);
        mProgressView = findViewById(R.id.request_progress);
        showProgress(true);
        access = new AccessFriends();
        access.execute((Void) null);

    }
    public void onDestroy() {
        super.onDestroy();
        HttpsRequest.finishSession();
    }
    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            listado.setVisibility(show ? View.GONE : View.VISIBLE);
            listado.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listado.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            listado.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    public void onClickBuscarButton(View view){ buscarEmailAmigo(emailPersonal); }
    public void onClickSolicitudesButton(View view){ consultarSolicitudes(emailPersonal); }


    public static Friends instancia(){
        return friend;
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

        private List<String> friendEmails = new ArrayList<String>();
        private List<String> friendNames = new ArrayList<String>();

        @Override
        protected Boolean doInBackground(Void... voids) {
            List<List<String>> listas;
            try {
                listas = HttpsRequest.getFriends(emailPersonal);

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
        protected void onPostExecute(final Boolean success){
            access = null;
            if(success){
                adapter = new FriendsAdapter(emailPersonal, friendEmails, getApplicationContext());
                listado.setAdapter(adapter);
            }else{
                System.out.println("Error");
            }
            showProgress(false);
        }

    }


}