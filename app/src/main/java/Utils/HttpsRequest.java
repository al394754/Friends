package Utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

/**
 * Clase encargada de crear todas las peticiones HTTP necesarias para conectar la aplicación
 * con la base de datos que se aloja en la spreadsheet de Google, además utiliza algunas
 * medidas de seguridad como un ID para identifiar que se está accediendo
 * desde la aplicación, además de un token_ID único para cada usuario para identicar quién está
 * realizando las peticiones en cualquier momento. Por otra parte las comunicaciones se producen por
 * https para conseguir la integridad, confidencialidad y autenticidad de los paquetes.
 */
public class HttpsRequest {
    /**
     * URL de la base de datos de google spreadsheet
     */
    private static final String URL_DB="https://script.google.com/macros/s/AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg/exec";
    /**
     * ID que identifica que la petición proviene de la aplicación
     */
    private static final String id="AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg";
    /**
     * Builder que se encarga de crear la URL añadiendole la query-string
     */
    private StringBuilder url;
    /**
     * Clase encargada de realizar las peticiones HTTPS.
     */
    private HttpsURLConnection con;
    /**
     * ID único de cada usuario, necesario para identificarse en la aplicación, el método login, lo
     * registra cada vez que el usuario inicia sesión
     */
    private static String token = "";

    /**
     * Constructor de la clase que se utiliza para indicar la URL a la que se va conectar
     * @param Url URL del lugar que se va a intentar acceder
     * @throws MalformedURLException
     */
    public HttpsRequest(String Url) throws MalformedURLException {
        super();
        this.url=new StringBuilder(Url);
    }

    /**
     * Método que se va a encargar de generar una petición de tipo GET HTTP para la base de datos de la
     * aplicación. Debe recibir la query-string por parámetros
     * @param parameters Utilizando un mapa clave-valor el usuario pasa la query-string que desea
     * para crear la petición.
     * @return Devuelve si se ha podido crear la petición correctamente
     * @throws IOException
     */
    public boolean createGETRequest(Map<String,String> parameters) throws IOException {
        parameters.put("ID",id);
        parameters.put("TOKEN",token);
        url.append(ParameterStringBuilder.getParamsString(parameters));
        URL urlObj= new URL(url.toString());
        Log.d("URL: ", url.toString());
        con = (HttpsURLConnection) urlObj.openConnection();
        Log.d("Headers ",con.getHeaderFields().toString());
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        return true;
    }
    /**
     * Método que se va a encargar de generar una petición de tipo POST HTTP para la base de datos de la
     * aplicación. Debe recibir la query-string por parámetros
     * @param parameters Utilizando un mapa clave-valor el usuario pasa la query-string que desea
     * para crear la petición.
     * @return Devuelve si se ha podido crear la petición correctamente
     * @throws IOException
     */
    public boolean createPOSTRequest(Map<String,String> parameters) throws IOException {
        parameters.put("ID",id);
        parameters.put("TOKEN",token);
        url.append(ParameterStringBuilder.getParamsString(parameters));
        URL urlObj= new URL(url.toString());
        Log.d("URL: ", url.toString());
        con = (HttpsURLConnection) urlObj.openConnection();
        con.setRequestMethod("POST");
        Log.d("Headers ",con.getHeaderFields().toString());
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        return true;
    }

    /** Método que devuelve el JSON de respuesta, o el error, si ha ocurrido uno, de la petición
     * actual.
     * @return Devuelve un String del JSON de respuesta, o un string que índica el error ocurrido
     * @throws IOException
     */
    public String getResponse() throws IOException {
        int status = con.getResponseCode();
        if (status>299){
            String result;
            if (con.getErrorStream()!=null)
                result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
            else result="";
            throw new IOException("Code error status: "+status+": " + result);
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();
        Log.d("Response ",content.toString());
        return content.toString();
    }

    /**
     * Método que elimina la sesión activa de un usuario.
     */
    public static void finishSession(){
        token="";
    }
    /**Genera la petición http para leer el chat, y devuelve una lista con los mensajes de clase Message del chat email1-email2
     * @param email1
     * @param email2
     * @return List<Message>
     * @throws IOException
     * @throws JSONException
     */
    public static List<Message> getChat(String email1, String email2) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","READCHAT");
        parameters.put("EMAIL",email1);
        parameters.put("EMAIL2",email2);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        JSONArray jsonArray = object.getJSONArray("CHAT");
        List<Message> messages = new ArrayList<>();
        int i = 0;
        while (i < jsonArray.length()){
            Message message = new Message();
            message.setWriter(jsonArray.getString(i++));
            message.setMessage(jsonArray.getString(i++));
            messages.add(message);
        }
        return messages;
    }

    /**
     * Genera una petición HTTP para escribir en el chat un mensaje, y te devuelve si se ha podido escribir correctamente
     * @param email1
     * @param email2
     * @param message
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static boolean writeChat(String email1, String email2, String message) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","WRITECHAT");
        parameters.put("EMAIL",email1);
        parameters.put("EMAIL2",email2);
        parameters.put("MESSAGE",message);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("WROTE");
    }

    /** Método que se encarga crear una petición HTTP de responder a una petición de amistad.
     * @param userEmail
     * @param emailFriend
     * @param response Indica si se ha rechazado u aceptado la petición.
     * @return Indica si se ha procesado correctamente la solicitud.
     * @throws IOException
     * @throws JSONException
     */
    public static boolean requestResponse(String userEmail,String emailFriend,Boolean response) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","REQUESTFRIENDRESPONSE");
        parameters.put("EMAIL",userEmail);
        parameters.put("FRIENDEMAIL",emailFriend);
        parameters.put("RESPONSEREQUEST",response.toString());
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ResponseRequest");       }

    /**
     * Método que se encarga de crear una petición HTTP para una solicitud de amistad.
     * @param userEmail
     * @param emailFriend
     * @return Indica si se ha procesado correctamente la solicitud.
     * @throws IOException
     * @throws JSONException
     */
    public static int requestFriend(String userEmail,String emailFriend) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","REQUESTFRIEND");
        parameters.put("EMAIL",userEmail);
        parameters.put("FRIENDEMAIL",emailFriend);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        return object.getInt("ResponseRequest");       }

    /**
     * Método que se encarga de crear una petición HTTP para borrar un amigo.
     * @param userEmail
     * @param emailFriend
     * @return Indica si se ha procesado correctamente la solicitud.
     * @throws IOException
     * @throws JSONException
     */
    public static boolean removeFriend(String userEmail,String emailFriend) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","REMOVEFRIEND");
        parameters.put("EMAIL",userEmail);
        parameters.put("FRIENDEMAIL",emailFriend);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ResponseRemove");       }

    /**
     * Método que se encarga de crear una petición HTTP para obtener los amigos de un usuario.
     * @param email
     * @return Devuelve una lista con los correos y otra con los nombres de los amigos.
     * @throws IOException
     * @throws JSONException
     */
    public static List<List<String>> getFriends(String email) throws IOException, JSONException {
        Log.d("Get Friends: ", email);
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","GETFRIENDS");
        parameters.put("EMAIL",email);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        List<List<String>> emailFriendString = new ArrayList<>();
        JSONArray emails = object.getJSONArray("FriendsEmail");
        JSONArray names = object.getJSONArray("FriendsName");
        List<String> listEmails= new ArrayList<>();
        List<String> listNames= new ArrayList<>();
        for (int i = 0; i < emails.length();i++ ){
            listEmails.add(emails.getString(i));
            listNames.add(names.getString(i));
        }
        emailFriendString.add(listEmails);
        emailFriendString.add(listNames);
        return emailFriendString;
    }
    /**
     * Método que se encarga de crear una petición HTTP para obtener las solicitudes
     * de amigos de un usuario.
     * @param email
     * @return Devuelve una lista con los correos y otra con los nombres de los amigos.
     * @throws IOException
     * @throws JSONException
     */
    public static List<List<String>> getRequestFriends(String email) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","GETREQUESTFRIENDS");
        parameters.put("EMAIL",email);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        List<List<String>> FriendRequests = new ArrayList<>();
        JSONArray emailsRequest = object.getJSONArray("FriendRequestsEmail");
        JSONArray namesRequest = object.getJSONArray("FriendRequestsName");
        List<String> listEmails= new ArrayList<>();
        List<String> listNames= new ArrayList<>();
        for (int i = 0; i < emailsRequest.length();i++ ){
            listEmails.add(emailsRequest.getString(i));
            listNames.add(namesRequest.getString(i));
        }
        FriendRequests.add(listEmails);
        FriendRequests.add(listNames);
        return FriendRequests;
    }

    /** Método que se encarga de crear una petición HTTP para solicitar las coordinadas de un amigo
     * @param emailUser
     * @param emailFriend
     * @return Devuelve un JSON con las coordinadas del amigo.
     * @throws IOException
     * @throws JSONException
     */
    public static String getCoordinates(String emailUser, String emailFriend) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","GETCOORDINATES");
        parameters.put("EMAIL",emailUser);
        parameters.put("EMAILFRIEND",emailFriend);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        return object.getString("Coordinates");
    }

    /**
     * Método que se encarga crear una petición HTTP que se encarga de actualizar las coordinadas
     * de un usuario en el mapa
     * @param email
     * @param coordinates
     * @return Indica si se ha procesado correctamente la solicitud.
     * @throws IOException
     * @throws JSONException
     */
    public static boolean updateCoordinates(String email,String coordinates) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","UPDATECOORDINATES");
        parameters.put("EMAIL",email);
        parameters.put("COORDINATES",coordinates);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ValidUpdate");
    }

    /**
     * Método que se encarga de crear una petición HTTP para crear una solicitud de inicio de sesión
     * de un usuario en la aplicación
     * @param email
     * @param password
     * @return Devuelve true si el inicio de sesión es correcto u false si no se ha conseguido
     * iniciar sesión
     * @throws IOException
     * @throws JSONException
     */
    public static boolean loginRequest(String email, String password) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","LOGIN");
        parameters.put("EMAIL",email);
        parameters.put("PASSWORD",password);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        token= token.replaceAll("(\n|\r)", "");
        token = object.getString("Token");
        Log.d("Token",token);
        return object.getBoolean("ValidLogin");
    }

    /**Método que se encarga de crear una petición HTTP para crear una solicitud de registro
     * de un usuario en la aplicación
     * @param email
     * @param password
     * @param name
     * @param surname
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static boolean registerRequest(String email,String password,String name,String surname) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","REGISTER");
        parameters.put("EMAIL",email);
        parameters.put("PASSWORD",password);
        parameters.put("NAME",name);
        parameters.put("SURNAME",surname);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        JSONObject object = new JSONObject(JsonString);
        token= token.replaceAll("(\n|\r)", "");
        token = object.getString("Token");
        return object.getBoolean("ValidRegister");
    }


}
