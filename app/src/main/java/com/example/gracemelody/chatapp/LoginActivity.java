package com.example.gracemelody.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);
        final Context context = this;

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatActivityIntent = new Intent(context, ChatActivity.class);
                //Bundle bundle = new Bundle();

                String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString();
                //bundle.putString("username", username );
                chatActivityIntent.putExtra("username", username);
                startActivity(chatActivityIntent);
                finish();
            }
        });
    }
}
