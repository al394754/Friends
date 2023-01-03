package com.example.friends.map;

import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.R;

public class SolicitarAmigo extends AppCompatActivity {

    private AutoCompleteTextView email;
    private Button buscar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_amigo);

        email = (AutoCompleteTextView) findViewById(R.id.email_friend);
        buscar = (Button) findViewById(R.id.email_send_request);

    }

    public void onClickCheckUser(View view) { checkUser(); }

    private void checkUser() {
        String emailUsuario = email.getText().toString();
        int posible = existeUsuario(emailUsuario);
        switch (posible) {
            case 0:
                //TODO Mandar petición amigo
                Toast toast = Toast.makeText(getApplicationContext(), "Solicitud de amistad enviada", Toast.LENGTH_SHORT);
                toast.show();
                break;
            case 1:
                email.setError("No existe dicho usuario");
                break;
            case 2:
                email.setError("Este usuario ya es amigo suyo");
                break;
        }
    }

    private int existeUsuario(String emailUsario){ //Mediante HTTP comprueba si el email existe y si es o no amigo
        //TODO 0 = existe, 1 = no existe, 2 = existe pero ya es amigo
        return 0;
    }
}
