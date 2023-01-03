package com.example.friends.map;

import static Utils.HttpsRequest.requestFriend;

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

public class SolicitarAmigo extends AppCompatActivity {

    private AutoCompleteTextView email;
    private Button buscar;
    private String emailPersonal;

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

    public void onClickCheckUser(View view) throws JSONException, IOException { checkUser(); }

    private void checkUser() throws JSONException, IOException {
        String emailUsuario = email.getText().toString();
        int posible = existeUsuario(emailUsuario);
        switch (posible) {
            case 0:
                Toast toast = Toast.makeText(getApplicationContext(), "Solicitud de amistad enviada", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case 1:
                email.setError("No existe dicho usuario");
                break;
            case 2:
                email.setError("Este usuario ya es amigo suyo");
                break;
            case 3:
                email.setError("Este usuario ya ha recibido una solicitud suya");
                break;
        }
    }

    private int existeUsuario(String emailUsario) throws JSONException, IOException { //Mediante HTTP comprueba si el email existe y si es o no amigo
        //0 = existe, 1 = no existe, 2 = existe pero ya es amigo, 3 = existe pero ya hay solicitud
        return requestFriend(emailPersonal, emailUsario);
    }
}
