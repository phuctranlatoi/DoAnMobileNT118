package com.example.doannt118.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;
    
    private List<ChatMessage> messages;
    
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getType() == ChatMessage.MessageType.USER ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_bot, parent, false);
            return new BotMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message);
        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        
        UserMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
    
    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        
        BotMessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        
        void bind(ChatMessage message) {
            tvMessage.setText(message.getText());
        }
    }
}
