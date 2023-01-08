package com.example.friends.map;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    Button enviar;

    List<String> participantes; //los dos participantes para formar el nombre del fichero
    ChatManager manager;
    EnvioAux envioAux;
    LecturaAux lecturaAux;

    String nombreFichero;
    String contenidoPrevio = "";


    private final ScheduledExecutorService mainScheduler = Executors.newScheduledThreadPool(1);


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chat = (TextView) findViewById(R.id.chatLocal);
        entrada = (EditText) findViewById(R.id.entrada);
        enviar = (Button) findViewById(R.id.enviar);


        manager = new ChatManager();
        manager.execute((Void) null);



        System.out.println("Texto: " + contenidoPrevio);

        String[] listadoMensajes = contenidoPrevio.split("---");
        for(String mensaje: listadoMensajes){
            chat.append(mensaje);
        }

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


        lecturaAux = new LecturaAux();
        lecturaAux.execute((Void) null);

        Runnable lect = () -> {

            try {
                actualizaPantalla(lectura(nombreFichero));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ScheduledFuture<?> handler = mainScheduler.scheduleAtFixedRate(lect, 10, 10, TimeUnit.SECONDS);
        Runnable parar = () -> handler.cancel(false);
        mainScheduler.schedule(parar, 30, TimeUnit.MINUTES);



    }


    public void chat(String mensaje) throws IOException, JSONException {

        if (participantes.equals(null))
            return;
        if(participantes.size() != 2)
            return;
        contenidoPrevio = contenidoPrevio + "\n" + participantes.get(0) + ": " + mensaje; //Deberá aparecer como: Alex: Hola, Adrián: Hola....
        envioAux = new EnvioAux(mensaje, nombreFichero);
        envioAux.execute((Void) null);
        manager.escritura(nombreFichero, contenidoPrevio);
    }

    public void iniciarChat() throws IOException, InterruptedException { //Iniciar la pantalla inicial con el chat
        nombreFichero = participantes.get(0) + ":" + participantes.get(1);
        contenidoPrevio = lectura(nombreFichero);
    }

    public void actualizaPantalla(String texto) throws IOException { //La interfaz gráfica no puede ser actualizada desde un hilo trabajador, siempre del principal
        /*new Thread(() -> runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chat.setText(texto);
            }
        })).start();*/
        //chat.setText(texto);
        String[] chatCompleto = texto.split("---");
        for (String mensaje: chatCompleto){
            chat.append(mensaje);
        }
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
        } catch (IOException e) {
            e.printStackTrace();
        }return texto.toString();
    }



    public class ChatManager extends AsyncTask<Void, Void, Boolean> { //Leer/Escribir localmente

        @Override
        protected Boolean doInBackground(Void... voids) {
            participantes = obtenerParticipantes();

            try {
                iniciarChat();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return true;
        }




        public String escritura(String nombreFichero, String mensaje) throws IOException, JSONException {  //Se necesita almacenar igualmente en fichero, no podemos eliminar
            File file = new File(getApplicationContext().getFilesDir(), nombreFichero); //Abrimos el fichero nuevo
            System.out.println(file.toPath());
            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(file));
                buf.write(mensaje);
                buf.write("---");
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mensaje + "\n";
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

    public class EnvioAux extends AsyncTask<Void, Void, Boolean>{ //Esta clase escribe hacia el exterior y guarda localmente

        private String mensaje;
        private String nombreFichero;

        public EnvioAux(String mensaje, String nombreFichero){
            this.mensaje = mensaje;
            this.nombreFichero = nombreFichero;
        }
        @Override
        protected Boolean doInBackground(Void... voids) { //Mando fuera y escribo local --> Leo local --> Actualizo pantalla
            try {
                escrituraExterior(mensaje);
                //manager.escritura(nombreFichero, mensaje);
                //String nuevoContenido = manager.lectura(nombreFichero);
                //actualizaPantalla(nuevoContenido);
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
    public class LecturaAux extends AsyncTask<Void, Void, Boolean>{

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        @Override
        protected Boolean doInBackground(Void... voids) { //Espera 10 segundos entre lectura y lectura, al cabo de 30 minutos finaliza la ejecución

            Runnable lect = () -> {
                try {
                    lecturaExterior(); //primero leemos de fuera
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            };
            ScheduledFuture<?> handler = scheduler.scheduleAtFixedRate(lect, 10, 10, TimeUnit.SECONDS);
            Runnable parar = () -> handler.cancel(false);
            scheduler.schedule(parar, 30, TimeUnit.MINUTES);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success){
            lecturaAux = null;
        }

        public void lecturaExterior() throws IOException, JSONException { //Lee del sheet y almacena localmente
           List<Message> mensajes = HttpsRequest.getChat(participantes.get(0), participantes.get(1));
           System.out.println("Cantidad de mensajes: " + mensajes.size());
           Message mensaje = new Message();
           StringBuilder chatActual = new StringBuilder();
           for(int i = 0; i < mensajes.size(); i++){
               mensaje = mensajes.get(i);
               chatActual.append(mensaje.getWriter()).append(":").append(mensaje.getMessage()).append("---");
           }
           if (contenidoPrevio.length() < chatActual.toString().length()) {
               contenidoPrevio = chatActual.toString();
               actualizaPantalla(contenidoPrevio);
           }
        }
    }

}