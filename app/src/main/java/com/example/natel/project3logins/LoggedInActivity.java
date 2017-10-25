package com.example.natel.project3logins;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Date;


/**
 * Created by natel on 10/6/2017.
 */

public class LoggedInActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logged_in_activity);
        final String session = getIntent().getStringExtra("Session");

        Button out = (Button) findViewById(R.id.Out);

        TextView loggedInName = (TextView)findViewById(R.id.LoggedInName);
        TextView loggedInEmail = (TextView)findViewById(R.id.LoggedInEmail);
        TextView encryptedPass = (TextView)findViewById(R.id.EncryptedPassword);
        TextView ses = (TextView)findViewById(R.id.Session);

        final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        String current = loggedInName.getText().toString();
        current += db.getNameFromSession(session);
        loggedInName.setText(current);

        current = loggedInEmail.getText().toString();
        current += db.getEmailFromSession(session);
        loggedInEmail.setText(current);

        current = encryptedPass.getText().toString();
        current += db.getEncryptedPasswordFromSession(session);
        current += "\n\n";
        current += db.getEncryptedLocationDetails(session);
        encryptedPass.setText(current);

        out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.deleteSession(session);
                finish();
            }
        });


        ses.setText("Current Session Details: \nSession ID: " + session + "\n" +
                "Session Expiration: " + new Date(Long.valueOf((db.getSessionExpirationFromSession(session)))).toString() + "\n" +
                "Last Logon Date: " +  new Date(Long.valueOf(db.getLastLogInDateFromSession(session))).toString());


    }
}
