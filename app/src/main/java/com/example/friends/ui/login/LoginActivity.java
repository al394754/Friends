package com.example.friends.ui.login;

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

/**
 * Actividad que se encarga de iniciar sesión.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
        /**
         * Id de permisos de contactos
         */
        private static final int REQUEST_READ_CONTACTS = 0;

        /**
         * Clase encarga de procesar el inicio de sesión
         */
        private UserLoginTask mAuthTask = null;

        // UI references.
        private AutoCompleteTextView mEmailView;
        private EditText mPasswordView;
        private View mProgressView;
        private View mLoginFormView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                        attemptLogin();
                        return true;
                }
                return false;
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        Button mEmailSignUpButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(view -> attemptLogin());
        mEmailSignUpButton.setOnClickListener(view -> launchRegister());

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        }

        private void populateAutoComplete() {
        if (!mayRequestContacts()) {
                return;
        }

        getLoaderManager().initLoader(0, null, this);
        }
        /**
        * Petición para pedir al usuario leer sus contactos, se utiliza para que el teclado muestre sugerencias
        * @return si se conceder el permiso**/
        private boolean mayRequestContacts() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        return true;
                }
                if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        return true;
                }
                if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                        Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                                requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                        });
                } else {
                        requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                }
                return false;
        }

        /**
         * Callback llamado en la respuesta a los permisos
         */
        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            if (requestCode == REQUEST_READ_CONTACTS) {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    populateAutoComplete();
                }
            }
        }


                /**
                 * Comprueba se el inicio de sesión es válido, comprobando que no hay campos vacíos,
                 * comprobando es un correo válido
                 */
        private void attemptLogin() {
                if (mAuthTask != null) {
                        return;
                }

                // Reset errors.
                mEmailView.setError(null);
                mPasswordView.setError(null);

                // Store values at the time of the login attempt.
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();

                boolean cancel = false;
                View focusView = null;

                // Check for a valid password, if the user entered one.
                if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
                        mPasswordView.setError(getString(R.string.error_invalid_password));
                        focusView = mPasswordView;
                        cancel = true;
                }

                // Check for a valid email address.
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                        mEmailView.setError(getString(R.string.error_field_required));
                        focusView = mEmailView;
                        cancel = true;
                } else if (!isEmailValid(email)) {
                        mEmailView.setError(getString(R.string.error_invalid_email));
                        focusView = mEmailView;
                        cancel = true;
                }

                if (cancel) {
                        // There was an error; don't attempt login and focus the first
                        // form field with an error.
                        focusView.requestFocus();
                } else {
                        // Show a progress spinner, and kick off a background task to
                        // perform the user login attempt.
                        showProgress(true);
                        mAuthTask = new UserLoginTask(email, password);
                        mAuthTask.execute((Void) null);
                }
        }

        private void launchRegister(){
                Intent intent= new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(intent);
        }
        private boolean isEmailValid(String email) {
                return email.contains("@");
        }

        private boolean isPasswordValid(String password) {
                return password.length() > 4;
        }

        /**
         * Se encarga de crear una barra de carga mientras se procesa la solicitud
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
        private void showProgress(final boolean show) {
                // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
                // for very easy animations. If available, use these APIs to fade-in
                // the progress spinner.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                List<String> emails = new ArrayList<>();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                emails.add(cursor.getString(ProfileQuery.ADDRESS));
                cursor.moveToNext();
                }

                addEmailsToAutoComplete(emails);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }

        private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
                //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
                ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

                mEmailView.setAdapter(adapter);
                }


        private interface ProfileQuery {
                String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
                };

                int ADDRESS = 0;
                int IS_PRIMARY = 1;
        }

        /**
         * Crea la petición HTTP asíncronamente para realizar el login en el sistema, también
         * encripta la contraseña
         */
        public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
                private final String mEmail;
                private final String mPassword;

                UserLoginTask(String email, String password) {
                        mEmail = email;
                        mPassword = password;
                }

                @Override
                protected Boolean doInBackground(Void... params) {
                        String passwordEncrypted=mPassword;
                        try {
                                passwordEncrypted = AESCrypt.encrypt(mPassword);
                        } catch (Exception e) {
                                e.printStackTrace();
                        }
                        try {
                                boolean logged = HttpsRequest.loginRequest(mEmail,passwordEncrypted);
                                if (logged)
                                        return true;
                        } catch (JSONException | IOException e) {
                                e.printStackTrace();
                        }

                        return false;
                }

                @Override
                protected void onPostExecute(final Boolean success) {
                        mAuthTask = null;
                        showProgress(false);

                        if (success) {
                                Intent intent= new Intent(getApplicationContext(), MainMenuActivity.class);
                                intent.putExtra("EMAIL", mEmail.toString());
                                startActivity(intent);
                        } else {
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                        }
                }

                @Override
                protected void onCancelled() {
                        mAuthTask = null;
                        showProgress(false);
                }
        }
}