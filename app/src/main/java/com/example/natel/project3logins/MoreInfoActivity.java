package com.example.natel.project3logins;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import static com.example.natel.project3logins.R.id.SignUp;

/**
 * Created by natel on 10/6/2017.
 */

public class MoreInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_info_fragment);

        final DatabaseHelper db = new DatabaseHelper(getApplicationContext());

        final CheckBox accept = (CheckBox)findViewById(R.id.accept) ;
        final EditText name = (EditText)findViewById(R.id.NewName);
        final EditText address = (EditText)findViewById(R.id.Address);
        final EditText state = (EditText)findViewById(R.id.State);
        Button SignUp = (Button) findViewById(R.id.SignUp);


        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(accept.isChecked()){
                    switch(db.createUser(getIntent().getStringExtra("email"), name.getText().toString(),getIntent().getStringExtra("password"),address.getText().toString(),state.getText().toString())){
                        case 0:
                            Intent i = new Intent(getApplicationContext(),LoggedInActivity.class);
                            i.putExtra("Session",db.createSession(getIntent().getStringExtra("email")));
                            startActivity(i);
                            finish();

                            break;
                        case 4:
                            Toast.makeText(getApplicationContext(),"Your name must be longer than that. Please change your name", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Please Check the box",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
