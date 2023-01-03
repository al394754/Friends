package com.example.friends.map;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import com.example.friends.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Chat extends AppCompatActivity {

    private TextView chat;
    private EditText entrada;
    Button enviar;

    List<String> participantes; //los dos participantes para formar el nombre del fichero
    ChatManager manager;
    String nombreFichero;

    String contenidoPrevio = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat = (TextView) findViewById(R.id.chatLocal);
        entrada = (EditText) findViewById(R.id.entrada);
        enviar = (Button) findViewById(R.id.enviar);

        manager = new ChatManager();
        manager.execute((Void) null);


        enviar.setOnClickListener(new View.OnClickListener() { //Enviar mensaje
            @Override
            public void onClick(View view) { //Lee de la entrada y lo envia al apretar el boton
                String mensaje = entrada.getText().toString();
                entrada.setText("");
                try {
                    chat(mensaje);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void actualizaPantalla(String texto){ //La interfaz gráfica no puede ser actualizada desde un hilo trabajador, siempre del principal
        contenidoPrevio = texto;
        chat.setText(texto);
    }


    public void chat(String mensaje) throws IOException {

        if (participantes.equals(null))
            return;
        if(participantes.size() != 2)
            return;
        contenidoPrevio = contenidoPrevio + "\n" + participantes.get(0) + ": " + mensaje; //Deberá aparecer como: Alex: Hola, Adrián: Hola....
        manager.escritura(nombreFichero, contenidoPrevio);
        chat.setText(contenidoPrevio);
    }



    public class ChatManager extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            participantes = obtenerParticipantes();
            try {
                iniciarChat();
                while(true){
                    Thread.sleep(3000);
                    actualizaPantalla(lectura(nombreFichero));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }


        public void iniciarChat() throws IOException { //Iniciar la pantalla inicial con el chat
            nombreFichero = participantes.get(0) + ":" + participantes.get(1);
            actualizaPantalla(lectura(nombreFichero));

        }

        public String lectura(String nombreFichero) throws IOException { //Leer localmente para enviar por pantalla
            File file = new File(getApplicationContext().getFilesDir(), nombreFichero); //Abrimos el fichero nuevo
            StringBuilder texto = new StringBuilder();
            try {
                BufferedReader buf = new BufferedReader(new FileReader(file));
                String linea;
                while ((linea = buf.readLine()) != null) {
                    texto.append(linea);
                    texto.append("\n");
                }
                buf.close();
                actualizaPantalla(texto.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }return texto.toString();
        }

        public String escritura(String nombreFichero, String mensaje) throws IOException {  //Se necesita almacenar igualmente en fichero, no podemos eliminar
            File file = new File(getApplicationContext().getFilesDir(), nombreFichero); //Abrimos el fichero nuevo
            System.out.println(file.toPath());
            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(file));
                buf.write(mensaje);
                buf.write("\n");
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            } return mensaje + "\n";
        }

        public String lecturaExterior(){ //Lee del sheet y almacena localmente
            return null;//TODO
        }

        public void escrituraExterior(){ //Manda el mensaje al sheet
            //TODO
        }

        public List<String> obtenerParticipantes() {
            List<String> participantes = new ArrayList<>();
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                participantes.add(extras.getString("EMAIL"));
                if (extras.size() > 1) {
                    participantes.add(extras.getString("EMAIL_AMIGO"));
                }
            }
            return participantes;
        }
        @Override
        protected void onCancelled(){
            manager = null;
        }
    }
}