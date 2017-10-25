package com.example.natel.project3logins;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.mindrot.jbcrypt.BCrypt;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final SharedPreferences shar = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());

        DatabaseHelper myHelper = new DatabaseHelper(this.getApplicationContext());

        myHelper.deleteOldSessions();
        final EncryptDecrypt enc = new EncryptDecrypt();

        if (myHelper.sessionExists(shar.getString("Session",null))){
            Log.i("HELLOOOOOOOOOOOO", shar.getString("Session",null));
            Intent i = new Intent(getApplicationContext(),LoggedInActivity.class);
            i.putExtra("Session", shar.getString("Session",null));
            startActivity(i);
        }

        int currentPage = 0;

        if (currentPage == 0) {

            Button loginButton = (Button) findViewById(R.id.Login);
            Button signupButton = (Button) findViewById(R.id.SignUp);

            final TextView errorText = (TextView) findViewById(R.id.Error);

            final EditText emailEditText = (EditText) findViewById(R.id.Email);
            final EditText passwordEditText = (EditText) findViewById(R.id.Password);

            final DatabaseHelper db = new DatabaseHelper(this.getApplicationContext());


            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int errorCode = db.logIntoUser(enc.encrypt(emailEditText.getText().toString(),DatabaseHelper.cypher), passwordEditText.getText().toString());
                    switch (errorCode) {
                        case 0:
                            errorText.setText("Login Matched!");
                            Intent i = new Intent(getBaseContext(),LoggedInActivity.class);
                            String session = db.createSession(enc.encrypt(emailEditText.getText().toString(),DatabaseHelper.cypher));
                            shar.edit().putString("Session",session).apply();
                            i.putExtra("Session",session);
                            db.updateLastLoginTime(session);
                            startActivity(i);
                            passwordEditText.setText("");
                            break;
                        case 1:
                            errorText.setText("The email you entered does not have an account. Create one!");
                            break;
                        case 2:
                            errorText.setText("The password does not match the password in the system");
                            break;
                    }
                }
            });
            signupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int errorCode = db.part1CreateUser(enc.encrypt(emailEditText.getText().toString(),DatabaseHelper.cypher),
                                                     passwordEditText.getText().toString());
                    switch (errorCode){
                        case 0:
                            Intent i = new Intent(getBaseContext(), MoreInfoActivity.class);
                            i.putExtra("email", enc.encrypt(emailEditText.getText().toString(),DatabaseHelper.cypher));
                            i.putExtra("password", BCrypt.hashpw(passwordEditText.getText().toString(),BCrypt.gensalt()));
                            startActivity(i);
                            passwordEditText.setText("");
                            break;
                        case 1:
                            errorText.setText("Email entered is not a valid email...");
                            break;
                        case 2:
                            errorText.setText("Email already exists. Please try logging in or try a different email address");
                            break;
                        case 3:
                            errorText.setText("Password must be at least 8 characters in length.");
                            break;
                    }
                }
            });
        }
    }
}
