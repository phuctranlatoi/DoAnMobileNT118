package com.example.doannt118.model;

import java.util.Date;
import java.util.List;

public class ChatMessage {
    
    public enum MessageType {
        USER,
        BOT,
        SYSTEM
    }
    
    private String id;
    private String text;
    private MessageType type;
    private Date timestamp;
    private List<String> quickReplies;
    
    public ChatMessage(String text, MessageType type) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.text = text;
        this.type = type;
        this.timestamp = new Date();
    }
    
    public String getId() { return id; }
    public String getText() { return text; }
    public MessageType getType() { return type; }
    public Date getTimestamp() { return timestamp; }
    public List<String> getQuickReplies() { return quickReplies; }
    
    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies;
    }
    
    public boolean hasQuickReplies() {
        return quickReplies != null && !quickReplies.isEmpty();
    }
}
