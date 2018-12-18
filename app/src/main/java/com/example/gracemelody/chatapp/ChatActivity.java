package com.example.gracemelody.chatapp;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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
import java.util.Set;

import static com.example.gracemelody.chatapp.ChatEngine.CHANNEL_LOBBY;

public class ChatActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ChatEngine.OnOtherChannelListener {


    ChatEngine chatEngine;
    EditText txtMsg;
    NavigationView navigationView;

    MenuItem leaveChannelMenuItem;

    Intent serviceIntent;

    SharedPreferences sharedPreferences;


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
        chatEngine.setOnOtherChannelListener(this);

        RecyclerView chatRecyclerView = findViewById(R.id.chatRecyclerView);

        chatRecyclerView.setAdapter( new ChatAdapter(chatRecyclerView, chatEngine));
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);

        addChannel(CHANNEL_LOBBY);
        switchChannel(CHANNEL_LOBBY);

        Bundle bundle = getIntent().getExtras();

        String username = bundle.getString("username");

        ArrayList<String> channelList = bundle.getStringArrayList("channelList");
        if (channelList != null) {
            //chatEngine.setChannelList(channelList);
            for (String channel : channelList) {
                if (! channel.equals(CHANNEL_LOBBY)) {
                    addChannel(channel);
                }
            }

        }

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
                        switchChannel(newChannel);
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
        chatEngine.onResume();
        NotificationService.ShowNotification = false;

    }

    @Override
    protected void onPause() {
        super.onPause();
        chatEngine.onPause();
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
        switch(id) {
            case R.id.action_leave_channel:
                leaveCurrentChannel();
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_users:
                showActiveUsers();
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
            if (menu.getItem(i) == menu.findItem(newSelectedChannel.hashCode())) {
                menu.getItem(i).setIcon(android.support.design.R.drawable.design_ic_visibility);
                menu.getItem(i).setChecked(false);
            } else {
                menu.getItem(i).setIcon(null);
            }
        }
    }

    void addChannel(String input) {
        String channel = input.toLowerCase().trim();

        chatEngine.addChannel(channel);

        Set<String> channelList = chatEngine.getChannels();

        sharedPreferences.edit()
                .putStringSet("channelList", channelList)
                .apply();

        MenuItem newItem = navigationView.getMenu().add(Menu.NONE, channel.hashCode(), Menu.NONE, channel);

        navigationView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), ""+v.getId(),Toast.LENGTH_SHORT ).show();
                MenuItem menuItem = (MenuItem) v;
                Toast.makeText(v.getContext(), menuItem.getTitle().toString(), Toast.LENGTH_SHORT ).show();

                return false;
            }
        });


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

                Set<String> channelList = chatEngine.getChannels();
                sharedPreferences.edit()
                        .putStringSet("channelList", channelList)
                        .apply();

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

    private void logout() {
        SharedPreferences sharedPreferences = getSharedPreferences("user", MODE_PRIVATE);
        sharedPreferences.edit()
                .clear()
                .apply();
        Intent goToLoginIntent = new Intent(this, LoginActivity.class);
        startActivity(goToLoginIntent);
        finish();
    }

    private void showActiveUsers() {
        ArrayList<String> activeUsers = chatEngine.getActiveUsers();

        StringBuilder activeUsersStringBuilder = new StringBuilder();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Active Users");

        for (String user : activeUsers) {
            activeUsersStringBuilder.append(user + "\n");
        }

        builder.setMessage(activeUsersStringBuilder.toString());
        builder.create().show();


    }

    @Override
    public void messageReceivedOnChannel(String channelName) {
        navigationView.getMenu().findItem(channelName.hashCode()).setChecked(true);
        //navigationView.getMenu().findItem(channelName.hashCode()).setIcon(ic_notification_overlay)
        //navigationView.getMenu().findItem(channelName.hashCode()).setTitle("!" + channelName);

        // Sound only notifications
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("ChatApp")
//                .setContentText(String.format("New messages on #%s", channelName))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setOnlyAlertOnce(true)
                ;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123, notificationBuilder.build());
    }
}
