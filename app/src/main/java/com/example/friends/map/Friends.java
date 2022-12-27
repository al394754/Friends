package com.example.friends.map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.friends.main_menu.MainMenuActivity;
import com.example.friends.map.Friends;
import com.example.friends.map.MapsActivity;
import com.example.friends.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;

import Utils.AESCrypt;
import Utils.HttpsRequest;


public class Friends extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private List<String> amigos; //Listado de todos los amigos

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        ;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ;
    }
}
