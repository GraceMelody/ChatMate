package com.example.gracemelody.chatapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private static final String CHANNEL_LOBBY = "lobby";
    ChatEngine chatEngine;
    EditText txtMsg;
    NavigationView navigationView;

    MenuItem leaveChannelMenuItem;

    Intent serviceIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (!isMyServiceRunning(NotificationService.class)) {
            serviceIntent = new Intent(this, NotificationService.class);
            startService(serviceIntent);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        txtMsg = findViewById(R.id.txtMsg);
        chatEngine = ChatEngine.Instance();

        RecyclerView chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setAdapter( new ChatAdapter(chatRecyclerView, chatEngine));
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        addChannel("lobby");

        Bundle bundle = getIntent().getExtras();

        String username = bundle.getString("username");

        chatEngine.setUsername(username);

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
                chatEngine.send(msg);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        NotificationService.ShowNotification = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationService.ShowNotification = true;
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
        leaveChannelMenuItem = menu.findItem(R.id.action_leave_channel);
        leaveChannelMenuItem.setEnabled(false);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(serviceIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leave_channel) {
            leaveCurrentChannel();
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
        switchChannel(newSelectedChannel);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void switchChannel(String input) {

        String newSelectedChannel = input.toLowerCase().trim();

        if (leaveChannelMenuItem != null) {
            if (newSelectedChannel.equals(CHANNEL_LOBBY)) {
                leaveChannelMenuItem.setEnabled(false);
            } else {
                leaveChannelMenuItem.setEnabled(true);
            }
        }

        chatEngine.switchChannel(newSelectedChannel);

        Menu menu = navigationView.getMenu();

        //currentChannel = newSelectedChannel;

        for (int i=0; i<menu.size(); i++) {
            if (menu.getItem(i).getTitle().equals(newSelectedChannel)) {
                menu.getItem(i).setIcon(android.support.design.R.drawable.design_ic_visibility);
                menu.getItem(i).setChecked(true);
            } else {
                menu.getItem(i).setIcon(null);
                menu.getItem(i).setChecked(false);
            }
        }
    }

    void addChannel(String input) {
        String channel = input.toLowerCase().trim();

        chatEngine.addChannel(channel);
        //subscribedChannels.add(channel);

        MenuItem newItem = navigationView.getMenu().add(Menu.NONE, channel.hashCode(), Menu.NONE, channel).setChecked(true);

        navigationView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), ""+v.getId(),Toast.LENGTH_SHORT ).show();
                MenuItem menuItem = (MenuItem) v;
                Toast.makeText(v.getContext(), menuItem.getTitle().toString(), Toast.LENGTH_SHORT ).show();

                return false;
            }
        });

        switchChannel(channel);
    }

    void leaveCurrentChannel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format("Leave channel %s? ", chatEngine.getCurrentChannel()));
        builder.setPositiveButton("Leave", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Menu menu = navigationView.getMenu();

                menu.removeItem(chatEngine.getCurrentChannel().hashCode());

                chatEngine.leaveChannel(chatEngine.getCurrentChannel());
                //subscribedChannels.remove(currentChannel);
                switchChannel(CHANNEL_LOBBY);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.create().show();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
