package com.example.gracemelody.chatapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationService extends Service implements ChildEventListener {

    private DatabaseReference root;
    private DatabaseReference channelNotifications;

    public static boolean ShowNotification = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, NotificationService.class);

        sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        root = FirebaseDatabase.getInstance().getReference();

        channelNotifications = root.child(ChatEngine.CHANNEL_NOTIFICATIONS);

        channelNotifications.addChildEventListener(this);
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        String channelName = dataSnapshot.getKey();

        if ( !ShowNotification || !ChatEngine.Instance().isSubscribedTo(channelName)) {
            return;
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ChatApp")
                .setContentText(String.format("New messages on #%s", channelName))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                
                ;


        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(123, notificationBuilder.build());

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
}
