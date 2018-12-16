package com.example.gracemelody.chatapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
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

        ImageView imgLogo = findViewById(R.id.imgLogo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder clearAllDialog = new AlertDialog.Builder(context)
                        .setTitle("Warning")
                        .setMessage("Clear All Data?")
                        .setPositiveButton("Clear All Data", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DatabaseReference root = FirebaseDatabase.getInstance().getReference();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(ChatEngine.CHANNELS, null);
                                hashMap.put(ChatEngine.CHANNEL_NOTIFICATIONS, null);
                                hashMap.put(ChatEngine.CHANNEL_PING, null);
                                root.updateChildren(hashMap);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                clearAllDialog.create().show();
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
