package Utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class HttpsRequest {
    //TODO: Alex escribe los metodos que necesitos y me encargo yo de hacer la petición
    private static final String URL_DB="https://script.google.com/macros/s/AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg/exec";
    private static final String id="AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg";
    private StringBuilder url;
    private HttpsURLConnection con;
    public HttpsRequest(String Url) throws MalformedURLException {
        super();
        this.url=new StringBuilder(Url);
    }
    public static boolean requestResponse(String userEmail,String emailFriend,Boolean response) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","REQUESTFRIENDRESPONSE");
        parameters.put("USEREMAIL",userEmail);
        parameters.put("FRIENDEMAIL",emailFriend);
        parameters.put("RESPONSEREQUEST",response.toString());
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ResponseRequest");       }
    public static int requestFriend(String userEmail,String emailFriend) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","REQUESTFRIEND");
        parameters.put("USEREMAIL",userEmail);
        parameters.put("FRIENDEMAIL",emailFriend);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getInt("ResponseRequest");       }
    public static String getFriends(String email) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","GETFRIENDS");
        parameters.put("EMAIL",email);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getJSONArray("Friends").toString(); //Lo paso a String ya que tenía problemas de conversión a lista
    }
    public static String getRequestFriends(String email) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","GETREQUESTFRIENDS");
        parameters.put("EMAIL",email);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getJSONArray("RequestFriends").toString(); //Lo paso a String ya que tenía problemas de conversión a lista
    }
    public static String getCoordinates(String email) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","GETCOORDINATES");
        parameters.put("EMAIL",email);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getString("Coordinates");
    }
    public static boolean updateCoordinates(String email,String coordinates) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","UPDATECOORDINATES");
        parameters.put("EMAIL",email);
        parameters.put("COORDINATES",coordinates);
        httpRequest.createPOSTRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ValidUpdate");
    }
    public static boolean loginRequest(String email, String password) throws IOException, JSONException {
        HttpsRequest httpRequest = new HttpsRequest(URL_DB);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("ACTION","LOGIN");
        parameters.put("EMAIL",email);
        parameters.put("PASSWORD",password);
        httpRequest.createGETRequest(parameters);
        String JsonString = httpRequest.getResponse();
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ValidLogin");
    }
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
        Log.d("Response ",JsonString);
        JSONObject object = new JSONObject(JsonString);
        return object.getBoolean("ValidRegister");
    }
    public boolean createGETRequest(Map<String,String> parameters) throws IOException {
        parameters.put("ID",id);
        url.append(ParameterStringBuilder.getParamsString(parameters));
        URL urlObj= new URL(url.toString());
        Log.d("URL: ", url.toString());
        con = (HttpsURLConnection) urlObj.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        return true;
    }
    public boolean createPOSTRequest(Map<String,String> parameters) throws IOException {
        parameters.put("ID",id);
        url.append(ParameterStringBuilder.getParamsString(parameters));
        URL urlObj= new URL(url.toString());
        Log.d("URL: ", url.toString());
        con = (HttpsURLConnection) urlObj.openConnection();
        con.setRequestMethod("POST");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        return true;
    }
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
        return content.toString();
    }

}
