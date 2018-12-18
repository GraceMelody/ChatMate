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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.example.gracemelody.chatapp.TimeUtil.secondsToMillis;

public class ChatEngine implements ChildEventListener{

    public static final String CHANNELS = "channels";
    public static final String CHANNEL_NOTIFICATIONS = "channels_notifs";
    public static final String CHANNEL_PING = "channels_ping";


    public static final String CHANNEL_LOBBY = "lobby";

    private DatabaseReference root;
    private DatabaseReference channel;
    private DatabaseReference channelNotifications;
    private DatabaseReference channelPing;

    private static ChatEngine chatEngineInstance;

    private ArrayList<String> subscribedChannels = new ArrayList<>();
    private HashSet<String> globalChannels = new HashSet<>();
    private String channelName;
    private String username;

    private Thread pingerThread;
    boolean sendPing = false;

    private ArrayList<Chat> messages = new ArrayList<>();
    private ChildEventListener activeUserPingListener = new ActiveUserPingListener();

    private HashMap<String, Long> userPings = new HashMap<>();
    OnMessageListener messageListener = null;
    private OnOtherChannelListener onOtherChannelListener;

    public String getCurrentChannel() {
        return channelName;
    }

    public static ChatEngine Instance() {
        if (chatEngineInstance == null) {
            chatEngineInstance = new ChatEngine();
        }
        return chatEngineInstance;
    }

    private ChatEngine() {
        root = FirebaseDatabase.getInstance().getReference();

        channelNotifications = root.child(CHANNEL_NOTIFICATIONS);
        channelNotifications.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String channelName = dataSnapshot.getKey();
                globalChannels.add(channelName);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("CHANNEL", dataSnapshot.getKey() + dataSnapshot.getValue());

                String channelName = dataSnapshot.getKey();
                if (subscribedChannels.contains(channelName) &&  ! channelName.equals(getCurrentChannel())) {
                    onOtherChannelListener.messageReceivedOnChannel(channelName);
                    Log.d("CHANNEL", "Listener called");
                }
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

        pingerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {

                    if (sendPing && username != null && channelPing != null ) {
                        DatabaseReference userPing = channelPing.child(username);
                        userPing.updateChildren(new HashMap<String, Object>());
                        userPing.setValue(System.currentTimeMillis());
                    }
                    try {
                        Thread.sleep(5_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        pingerThread.start();
    }

    public void onPause() {
        sendPing = false;
    }

    public void onResume() {
        sendPing = true;
        pingerThread.interrupt();
    }

    public void addChannel(String channel) {
        subscribedChannels.add(channel);
    }

    public void leaveChannel(String channel) {
        subscribedChannels.remove(channel);
    }

    public boolean isSubscribedTo(String channel) {
        return subscribedChannels.contains(channel);
    }

    public void send(String message) {
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

        messageListener.messageReceived(messages.size() - 1);
    }

    public int getItemCount() {
        return messages.size();
    }

    public Chat getMessage(int i) {
        return messages.get(i);
    }

    public void switchChannel(String title) {

        messageListener.switchChannel();

        messages.clear();
        channelName = title;

        channel = root.child(CHANNELS).child(channelName);
        channel.removeEventListener(this);
        channel.addChildEventListener(this);

        userPings.clear();
        channelPing = root.child(CHANNEL_PING).child(channelName);
        channelPing.addChildEventListener(activeUserPingListener);
    }


    public Set<String> getChannels() {
        return new HashSet(subscribedChannels);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setChannelList(ArrayList<String> channelList) {
        for (String channel : channelList) {
            if (channel != CHANNEL_LOBBY) {
                addChannel(channel);
            }
        }
    }

    public interface OnMessageListener {
        void messageReceived(int i);
        void switchChannel();
    }

    public interface OnOtherChannelListener {
        void messageReceivedOnChannel (String channelName);
    }

    public void setOnMessageListener(OnMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void setOnOtherChannelListener (OnOtherChannelListener onOtherChannelListener) {
        this.onOtherChannelListener = onOtherChannelListener;
    }

    public ArrayList<String> getActiveUsers() {
        ArrayList<String> activeUsers = new ArrayList<>();

        for (String user : userPings.keySet()) {
            Long lastSeenTimestamp = userPings.get(user);
            Long currentTimestamp = (new Date()).getTime();
            Long timeDifference = currentTimestamp - lastSeenTimestamp;
            if (timeDifference < secondsToMillis(10) ) {
                activeUsers.add(user);
            }
        }
        return activeUsers;
    }

    public HashSet<String> getAllChannels() {
        return globalChannels;
    }

    private class ActiveUserPingListener implements ChildEventListener {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String username = dataSnapshot.getKey();
            Long lastSeen = (Long) dataSnapshot.getValue();
            userPings.put(username, lastSeen);
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            String username = dataSnapshot.getKey();
            Long lastSeen = (Long) dataSnapshot.getValue();
            userPings.put(username, lastSeen);
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
}
