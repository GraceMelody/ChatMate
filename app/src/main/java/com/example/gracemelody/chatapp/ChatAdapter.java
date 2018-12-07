package com.example.gracemelody.chatapp;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> implements ChatEngine.OnMessageListener {

    ChatEngine chatEngine;
    RecyclerView recyclerView;

    @Override
    public void messageReceived(int pos) {
        notifyItemInserted(pos);
        //notifyItemChanged(pos);
        recyclerView.smoothScrollToPosition(pos);
        Log.d("Chat", "Notify: " + pos);
    }

    @Override
    public void switchChannel() {
        notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView txtChat;
        private TextView txtSender;
        public ChatViewHolder(View v) {
            super(v);
            txtChat = v.findViewById(R.id.txtChat);
            txtSender = v.findViewById(R.id.txtSender);
        }

        public void setChatText(String m) {
            txtChat.setText(m);
        }

        public void setUser(String u) {
            txtSender.setText(u);
        }
    }

    public ChatAdapter(RecyclerView recyclerView, ChatEngine chatEngine) {
        this.recyclerView = recyclerView;
        this.chatEngine = chatEngine;
        this.chatEngine.setOnMessageListener(this);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_chat, viewGroup, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i) {
        chatViewHolder.setChatText(chatEngine.getMessage(i).message);
        chatViewHolder.setUser(chatEngine.getMessage(i).sender);
    }

    @Override
    public int getItemCount() {
        return chatEngine.getItemCount();
}



}
