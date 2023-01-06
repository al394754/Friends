package com.example.friends.map;

import static Utils.HttpsRequest.requestFriend;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class SolicitarAmigo extends AppCompatActivity {

    private AutoCompleteTextView email;
    private Button buscar;
    private String emailPersonal;
    public static int posible;
    private SolicitarAux aux;

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

    }
    public void onClickCheckUser(View view) throws ExecutionException, InterruptedException { checkUser(); }
    private void checkUser() throws InterruptedException, ExecutionException {
        String emailUsuario = email.getText().toString();
        if(emailUsuario.compareTo("") == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Debe introducir un usuario", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            if (emailUsuario.compareTo(emailPersonal) == 0) {
                Toast toast = Toast.makeText(getApplicationContext(), "No puede agregarse a si mismo", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                aux = new SolicitarAux(emailUsuario);
                aux.execute((Void) null).get();
                switch (posible) {
                    case 0:
                        Toast toast = Toast.makeText(getApplicationContext(), "Solicitud de amistad enviada", Toast.LENGTH_SHORT);
                        toast.show();
                        break;
                    case 1:
                        email.setError("No existe dicho usuario");
                        break;
                    case 2:
                        email.setError("Este usuario ya ha recibido una solicitud suya");
                        break;
                    case 3:
                        email.setError("Este usuario ya es amigo suyo");
                        break;
                }
            }
        }email.setText("");
        aux = null;
    }
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

        private void existeUsuario(String emailUsario) throws JSONException, IOException { //Mediante HTTP comprueba si el email existe y si es o no amigo
            //0 = existe, 1 = no existe, 2 = existe pero ha recibido solicitud, 3 = existe pero ya es amigo
            posible = requestFriend(emailPersonal, emailUsario);
        }
    }


}
