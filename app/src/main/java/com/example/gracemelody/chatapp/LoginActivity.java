package com.example.gracemelody.chatapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    final Context context = this;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = findViewById(R.id.btnLogin);

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);


        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((EditText) findViewById(R.id.txtUsername)).getText().toString().trim().toLowerCase();
                if (username.length() > 0) {
                    login(username);
                    sharedPreferences.edit()
                            .putString("username", username)
                            .apply();
                }
            }
        });

        String savedUsername = sharedPreferences.getString("username", null);

        if (savedUsername != null) {
            login(savedUsername);
        }
    }

    void login(String username) {
        Intent chatActivityIntent = new Intent(context, ChatActivity.class);
        chatActivityIntent.putExtra("username", username);

        // Load channels
        Set<String> channelListSet = sharedPreferences.getStringSet("channelList", null);

        if (channelListSet != null) {
            ArrayList<String> channelList = new ArrayList<>(channelListSet);
            chatActivityIntent.putStringArrayListExtra("channelList", channelList);
        }

        startActivity(chatActivityIntent);
        finish();
    }
}
