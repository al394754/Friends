package com.example.friends.map;

import static Utils.HttpsRequest.requestFriend;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;
import com.example.friends.main_menu.MainMenuActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SolicitarAmigo extends AppCompatActivity {

    private AutoCompleteTextView email;
    private Button buscar;
    private String emailPersonal;
    public static int posible;
    private SolicitarAux aux;
    private View mProgressView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_amigo);

        email = (AutoCompleteTextView) findViewById(R.id.email_friend);
        buscar = (Button) findViewById(R.id.email_send_request);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            emailPersonal = extras.getString("EMAIL");
        }
        mProgressView = findViewById(R.id.request_progress);

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
        }
    }
    public void onClickCheckUser(View view) throws ExecutionException, InterruptedException {
        showProgress(true);
        Log.d("Barra", "Mostrar barra");
        checkUser();
    }

    /**
     * Comprobaciones locales de la información introducida por el usuario
     * @throws InterruptedException
     * @throws ExecutionException
     */
    private void checkUser() throws InterruptedException, ExecutionException {
        String emailUsuario = email.getText().toString();
        if(emailUsuario.compareTo("") == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "You must enter a user", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            if (emailUsuario.compareTo(emailPersonal) == 0) {
                Toast toast = Toast.makeText(getApplicationContext(), "Cannot add itself", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                showProgress(true);
                aux = new SolicitarAux(emailUsuario);
                aux.execute((Void) null);
            }
        }
        aux = null;
    }

    /**
     * Clase para enviar la petición de amistad
     */
    @SuppressLint("StaticFieldLeak")
    private class SolicitarAux extends AsyncTask<Void, Void, Boolean> {

        private String usuario;

        public SolicitarAux(String usuario){
            this.usuario = usuario;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                existeUsuario(usuario);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            showProgress(false);
            switch (posible) {
                case 0:
                    Toast toast = Toast.makeText(getApplicationContext(), "Friend request sent", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                case 1:
                    email.setError("No such user");
                    break;
                case 2:
                    email.setError("This user has already received a request from you");
                    break;
                case 3:
                    email.setError("This user is already a friend of yours");
                    break;
            }
            email.setText("");
        }
        private void existeUsuario(String emailUsario) throws JSONException, IOException { //Mediante HTTP comprueba si el email existe y si es o no amigo
            //0 = existe, 1 = no existe, 2 = existe pero ha recibido solicitud, 3 = existe pero ya es amigo
            posible = requestFriend(emailPersonal, emailUsario);
        }
    }


}
