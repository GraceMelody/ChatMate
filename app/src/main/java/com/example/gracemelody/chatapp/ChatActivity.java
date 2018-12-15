package com.example.gracemelody.chatapp;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ChatEngine chatEngine;
    EditText txtMsg;
    NavigationView navigationView;

    ArrayList<String> subscribedChannels = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().add("A").setIcon(android.support.design.R.drawable.design_ic_visibility);
        navigationView.getMenu().add("B");
        navigationView.getMenu().add("C");


        txtMsg = findViewById(R.id.txtMsg);
        chatEngine = new ChatEngine();

        RecyclerView chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setAdapter( new ChatAdapter(chatRecyclerView, chatEngine));
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextDialogFragment textDialogFragment = new TextDialogFragment();
                textDialogFragment.setTitle("Add Channel");
                textDialogFragment.setTextDialogFragmentListener(new TextDialogFragment.TextDialogFragmentListener() {
                    @Override
                    public void onPositiveClick(TextDialogFragment dialog) {
                        String newChannel = dialog.getText();
                        addChannel(newChannel);
                    }
                });
                textDialogFragment.show(getSupportFragmentManager(), "TextDialogFragment");
            }
        });

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = txtMsg.getText().toString();
                txtMsg.setText("");
                chatEngine.send("testUser", msg);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chatroom, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        String newSelectedChannel = item.getTitle().toString();
        switchChannel(newSelectedChannel, item);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void switchChannel(String input, MenuItem item) {

        String newSelectedChannel = input.toLowerCase().trim();

        chatEngine.switchChannel(newSelectedChannel);

        Menu menu = navigationView.getMenu();

        for (int i=0; i<menu.size(); i++) {
            menu.getItem(i).setIcon(null);
        }
        item.setIcon(android.support.design.R.drawable.design_ic_visibility);

    }

    void addChannel(String input) {
        String channel = input.toLowerCase().trim();
        subscribedChannels.add(channel);
        MenuItem newItem = navigationView.getMenu().add(channel);

        switchChannel(channel, newItem);
    }
}
