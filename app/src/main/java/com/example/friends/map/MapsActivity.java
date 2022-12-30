package com.example.friends.map;

// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.


import android.Manifest.permission;
import android.annotation.SuppressLint;

import com.example.friends.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;

import Utils.HttpsRequest;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The "My
 * Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} and {@link
 * android.Manifest.permission#ACCESS_COARSE_LOCATION} are requested at run time. If either
 * permission is not granted, the Activity is finished with an error message.
 */
public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private String emailPropio; //Email del propio usuario
    private String emailAjeno; //Email de amigo
    private LatLng coordinatesOtro = null; //Coordenadas dde amigo
    private LatLng coordenadasActuales; //Con esto actualizaremos nuestra posicion actual
    private AccessCoordinates accessCoordinates; //Utilizar para obtener coordenadas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        askPermissions();
    }

    /**
     * Method executed when all permission are obtained
     */
    private void initiate() {
        mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        map.setMyLocationEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            emailPropio = extras.getString("EMAIL");
            if(extras.size() > 1){
                emailAjeno = extras.getString("EMAIL_AMIGO");
            }
        }

        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Location location = service.getLastKnownLocation(provider);
        coordenadasActuales = new LatLng(location.getLatitude(),location.getLongitude()); //Código para obtener ubicación actual

        accessCoordinates = new AccessCoordinates(emailAjeno);
        accessCoordinates.execute((Void) null);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


//    @Override
//    protected void onResumeFragments() {
//        super.onResumeFragments();
//        if (permissionDenied) {
//            // Permission was not granted, display error dialog.
//            //showMissingPermissionError();
//            permissionDenied = false;
//        }
//    }

    /*
     * Displays a dialog with error message explaining that the location permission is missing.
     */
        /*private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }*/

    /**
     * Ask the user for permissions needed
     */
    private void askPermissions() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationChecker();
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        this.requestPermissions(new String[]{permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        // [END maps_check_location_permission]
    }

    /**
     * Callback for requestPermissions
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Enable the my location layer if the permission has been granted
            locationChecker();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            //showMissingPermissionError();
            finish();
        }
    }

    /**
     * Este método crea una solicitud de ubicación con ciertos parámetros, después de ello compara
     * el estado actual de la ubicación con la solicitud de ubicación. Si todos los requisitos de la
     * solicitud se cumplen la comparación es exitosa, por tanto sigue la ejecucción de código. Si no
     * significa probablemente que la ubicación está desactivada y se crea una petición al usuario
     * para que la active.
     */

    @SuppressLint({"NewApi", "MissingPermission"})
    protected void locationChecker() {
        LocationRequest.Builder locationRequestBuilder = new LocationRequest.Builder(10000);
        locationRequestBuilder.setMinUpdateIntervalMillis(5000);
        locationRequestBuilder.setPriority(Priority.PRIORITY_LOW_POWER);
        LocationRequest locationRequest = locationRequestBuilder.build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            Log.d("RequestLocation", "Success");
            initiate();
        });
        task.addOnFailureListener(this, e -> {
            Log.d("Request", "Location");
            if (e instanceof ApiException) {
                try {
                    ApiException resolvable = (ApiException) e;
                    Status status = resolvable.getStatus();
                    status.startResolutionForResult(this,
                            LOCATION_PERMISSION_REQUEST_CODE);
                } catch (IntentSender.SendIntentException ignored) {
                }
            }
        });
    }

    /**
     * Callback of startResolutionForResult
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE)
            super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Log.d("RequestLocation", "User Enabled Location");
                    initiate();
                } else {
                    Log.d("RequestLocation", "User did not enable Location");
                    //showMissingPermissionError();
                    finish();
                }
        }
    }

    public class AccessCoordinates extends AsyncTask<Void, Void, Boolean> {

        private String emailAmigo;
        private String cadena = "";

        AccessCoordinates(String email) {
            this.emailAmigo = email;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                if(emailAmigo != null) {
                    cadena = HttpsRequest.getCoordinates(emailAmigo);
                }
                String misNuevasCoordenadas = coordenadasActuales.toString().replace("lat/lng: (", "").replace(")", "").replace(",", ":");
                HttpsRequest.updateCoordinates(emailPropio, misNuevasCoordenadas);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            //System.out.println(cadena);
            //amigos.addAll(Arrays.asList(cadena.replace("[", "").replace("]", "").replace("\"", "").split(","))); //pasar la cadena que obtenemos a lista
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            accessCoordinates = null;
            if (success && cadena.compareTo("") != 0) //Segunda condicion comprueba si hay algún email de amigo
                creaCoordenadas(cadena);
            else
                System.out.println("Error coordenadas");
        }
    }

    private void creaCoordenadas(String coordenadas) {
        String[] param = coordenadas.split(":");
        coordinatesOtro = new LatLng(Double.parseDouble(param[0]), Double.parseDouble(param[1])); //Latitud : Longitud
        map.addMarker(new MarkerOptions().position(coordinatesOtro).title("Posicion")); //Añadimos un marcador sobre la posicion de una persona    }
    }
}
