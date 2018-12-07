package com.example.gracemelody.chatapp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatEngine implements ChildEventListener{

    private final String CHANNELS = "channels";
    private final String CHANNEL_NOTIFICATIONS = "channels_notifs";
    private DatabaseReference root;
    private DatabaseReference channel;
    private DatabaseReference channelNotifications;


    private String channelName;

    ArrayList<Chat> messages = new ArrayList<>();

    public ChatEngine() {
        root = FirebaseDatabase.getInstance().getReference();

        //channel = root.child(CHANNELS).child(channelName);
        //channel.addChildEventListener(this);

        switchChannel("A");


        channelNotifications = root.child(CHANNEL_NOTIFICATIONS);
        channelNotifications.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("CHANNEL", dataSnapshot.getKey() + dataSnapshot.getValue());

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
        });
    }

    void send(String username, String message) {
        String temp_key = channel.push().getKey();

        DatabaseReference message_root = channel.child(temp_key);
        Map<String, Object> map = new HashMap<>();
        map.put("name", username);
        map.put("msg", message);

        DatabaseReference currentChannelNotif = channelNotifications.child(channelName);

        currentChannelNotif.updateChildren(new HashMap<String, Object>());
        currentChannelNotif.setValue(System.currentTimeMillis());

        message_root.updateChildren(map);

    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        appendMessage(dataSnapshot);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        //appendMessage(dataSnapshot);
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

    private void appendMessage(DataSnapshot dataSnapshot) {

        String username = "NONAME";
        String message = "NULL";
        for (DataSnapshot child : dataSnapshot.getChildren()) {
            switch(child.getKey()) {
                case "msg":
                    message = (String) child.getValue();
                    break;
                case "name":
                    username = (String) child.getValue();
                    break;
            }
        }

        Chat chat = new Chat(username, message);
        messages.add(chat);

        for (OnMessageListener l : messageListeners) {
            l.messageReceived(messages.size() - 1);
        }

        /*
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {

            String chatMessage = (String)((DataSnapshot)i.next()).getValue();
            String chatUsername = (String)((DataSnapshot)i.next()).getValue();

            //tvMessage.append(chatUsername + " : " + chatMessage + "\n");
            messages.add(new Chat(chatUsername, chatMessage));
            for (OnMessageListener l : messageListeners) {
                l.messageReceived(messages.size() - 1);
            }
            Log.d("Chat", chatUsername + ":" + chatMessage);
        }
        */
    }

    public int getItemCount() {
        return messages.size();
    }

    public Chat getMessage(int i) {
        return messages.get(i);
    }

    public void switchChannel(String title) {

        for (OnMessageListener l : messageListeners) {
            l.switchChannel();
        }

        messages.clear();
        channelName = title;

        channel = root.child(CHANNELS).child(channelName);
        channel.addChildEventListener(this);


    }

    public interface OnMessageListener {
        void messageReceived(int i);
        void switchChannel();
    }

    ArrayList<OnMessageListener> messageListeners = new ArrayList<>();
    public void setOnMessageListener(OnMessageListener messageListener) {
        messageListeners.add(messageListener);
    }
}
