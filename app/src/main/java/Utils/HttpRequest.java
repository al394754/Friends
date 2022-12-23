package Utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class HttpRequest {
    //TODO: Alex escribe los metodos que necesitos y me encargo yo de hacer la petición
    private static final String URL_DB="https://script.google.com/macros/s/AKfycbwRoz_VM-6332p4laOoGD1WIxmOMnhxRwZI6Lm3ensG5bHkLpV2n8CWAnLo6I6Gklg0jg/exec";
    private StringBuilder url;
    private HttpURLConnection con;
    public HttpRequest(String Url) throws MalformedURLException {
        super();
        this.url=new StringBuilder(Url);
    }
    public static boolean loginRequest(String email, String password) throws IOException, JSONException {
        HttpRequest httpRequest = new HttpRequest(URL_DB);
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
        HttpRequest httpRequest = new HttpRequest(URL_DB);
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
        url.append(ParameterStringBuilder.getParamsString(parameters));
        URL urlObj= new URL(url.toString());
        Log.d("URL: ", url.toString());
        con = (HttpURLConnection) urlObj.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        con.connect();
        return true;
    }
    public boolean createPOSTRequest(Map<String,String> parameters) throws IOException {
        url.append(ParameterStringBuilder.getParamsString(parameters));
        URL urlObj= new URL(url.toString());
        Log.d("URL: ", url.toString());
        con = (HttpURLConnection) urlObj.openConnection();
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
