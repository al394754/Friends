package com.example.friends.map;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.content.AsyncTaskLoader;

import com.example.friends.R;

import org.json.JSONException;

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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import Utils.HttpsRequest;
import Utils.Message;

public class Chat extends AppCompatActivity {

    private TextView chat;
    private EditText entrada;
    private ProgressBar progressBar;
    Button enviar;

    private ScheduledFuture<?> handler;

    List<String> participantes; //los dos participantes para formar el nombre del fichero
    ChatManager manager;
    EnvioAux envioAux;
    LecturaAux lecturaAux;

    String nombreFichero;
    String contenidoPrevio = "";


    /**
     * Con esto paramos la lectura periódica cuando volvemos hacia atrás empleando el botón del móvil
     */
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        handler.cancel(true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        chat = (TextView) findViewById(R.id.chatLocal);
        chat.setMovementMethod(new ScrollingMovementMethod());
        chat.setText("Loading messages...");
        entrada = (EditText) findViewById(R.id.entrada);
        enviar = (Button) findViewById(R.id.enviar);

        manager = new ChatManager();
        manager.execute((Void) null);

        lecturaAux = new LecturaAux();
        lecturaAux.execute((Void) null);

        enviar.setOnClickListener(new View.OnClickListener() { //Enviar mensaje
            @Override
            public void onClick(View view) { //Lee de la entrada y lo envia al apretar el boton
                String mensaje = entrada.getText().toString();
                entrada.setText("");
                try {
                    chat(mensaje);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * Método encargado del inicio del envio de un mensaje hacia el Google Sheet desde la pantalla
     * @param mensaje
     * @throws IOException
     * @throws JSONException
     */
    public void chat(String mensaje) throws IOException, JSONException {

        if (participantes.equals(null))
            return;
        if(participantes.size() != 2)
            return;
        envioAux = new EnvioAux(mensaje, nombreFichero);
        envioAux.execute((Void) null);
    }

    /**
     * Actualiza la pantalla
     * @param texto
     * @throws IOException
     */
    public void actualizaPantalla(String texto) throws IOException { //La interfaz gráfica no puede ser actualizada desde un hilo trabajador, siempre del principal
        chat.setText(texto);
        chat.invalidate();
    }

    /**
     * Clase encargada de obtener los participantes del chat
     */
    public class ChatManager extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            participantes = obtenerParticipantes();
            return true;
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

    /**
     * Clase que realiza el proceso de envio de mensajes hacia el exterior
     */
    public class EnvioAux extends AsyncTask<Void, Void, Boolean>{ //Esta clase escribe hacia el exterior

        private String mensaje;
        private String nombreFichero;

        public EnvioAux(String mensaje, String nombreFichero){
            this.mensaje = mensaje;
            this.nombreFichero = nombreFichero;
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                escrituraExterior(mensaje);
            } catch (JSONException | IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            envioAux = null;
        }


        public void escrituraExterior(String mensaje) throws JSONException, IOException { //Manda el mensaje al sheet
            HttpsRequest.writeChat(participantes.get(0), participantes.get(1), mensaje);
        }
    }

    /**
     * Clase que realiza el proceso de lectura de mensajes hacia el exterior
     */
    public class LecturaAux extends AsyncTask<Void, Void, Boolean>{

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); //Se usará para actualizar el chat periodicamente

        @Override
        protected Boolean doInBackground(Void... voids) { //Espera periodica entre lectura y lectura, al cabo de 10 minutos finaliza la ejecución

            Runnable lect = () -> { //Proceso que se ejecutará en bucle, en este caso las lecturas
                try {
                    lecturaExterior();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            };
            handler = scheduler.scheduleAtFixedRate(lect, 2, 2, TimeUnit.SECONDS); //Encargado del bucle de lectura
            Runnable parar = () -> handler.cancel(false); //Para finalizar dicho bucle
            scheduler.schedule(parar, 10, TimeUnit.MINUTES); //A los 10 minutos, ejecuta el Runnable parar
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            lecturaAux = null;
        }

        public void lecturaExterior() throws IOException, JSONException { //Lee del sheet
           List<Message> mensajes = HttpsRequest.getChat(participantes.get(0), participantes.get(1));
           Message mensaje = new Message();
           StringBuilder chatActual = new StringBuilder();
           for(int i = 0; i < mensajes.size(); i++){
               mensaje = mensajes.get(i);
               chatActual.append(mensaje.getWriter()).append(": ").append(mensaje.getMessage()).append("\n");
           }

           if (contenidoPrevio.length() < chatActual.toString().length()) {
               contenidoPrevio = chatActual.toString();
               actualizaPantalla(contenidoPrevio);
           }
           if(chatActual.length() == 0){
               actualizaPantalla("There are no messages in this conversation yet");
           }
        }
    }

}