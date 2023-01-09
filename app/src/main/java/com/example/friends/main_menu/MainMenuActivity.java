package com.example.friends.main_menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.map.Friends;
import com.example.friends.R;
import com.example.friends.map.MapsActivity;


public class MainMenuActivity extends AppCompatActivity{

//    private String email = getIntent().getStringExtra("EMAIL");//Con esto obtenemos el email del login, debemos pasarlo al resto de actividades manualmente
    //private String email = "Alex@gmail.com";
    private String email = "";
    private View mProgressView;
    private View mMenuFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);//poner la página en pantalla

        Bundle extras = getIntent().getExtras();
        if (extras != null){
            email = extras.getString("EMAIL");
        }
        mMenuFormView = findViewById(R.id.form_progress);
        mProgressView = findViewById(R.id.menu_progress);
        Button friendsButton = (Button) findViewById(R.id.friends_button); //Lista de amigos
        Button soloMapButton = (Button) findViewById(R.id.solo_map_button); //Mapa solo
    }

    public void onClickFriendsButton(View view){
        showProgress(true);
        showFriends(); }
    public void onClickMapButton(View view) { showMap(); }
    public void onPause() {
        super.onPause();
        showProgress(false);
    }
    public void showFriends(){ //Muestra una lista de amigos cada uno con dos botones, uno para chat y otro para ubicación
        Intent intent = new Intent(getApplicationContext(), Friends.class);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
    }
    /**
     * Shows the progress UI and hides the menu.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mMenuFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMenuFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMenuFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mMenuFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void showMap(){ //Muestra el mapa pero solo de la ubicaciónd el propio usuario, no del resto (Función básica de Google Maps)
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("EMAIL", email);
        startActivity(intent);
    }



}
