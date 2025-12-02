package com.example.doannt118.chatbot;

import java.util.List;

public class ChatResponse {
    
    public enum ResponseType {
        TEXT,           // Tin nhắn text thông thường
        CONFIRMATION,   // Cần xác nhận (Yes/No buttons)
        ACTION,         // Cần thực hiện action (mở màn hình khác)
        QUICK_REPLY     // Có quick reply buttons
    }
    
    private String message;
    private ResponseType type;
    private List<String> quickReplies;
    private Object actionData;
    
    public ChatResponse(String message, ResponseType type) {
        this.message = message;
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public ResponseType getType() {
        return type;
    }
    
    public List<String> getQuickReplies() {
        return quickReplies;
    }
    
    public void setQuickReplies(List<String> quickReplies) {
        this.quickReplies = quickReplies;
        this.type = ResponseType.QUICK_REPLY;
    }
    
    public Object getActionData() {
        return actionData;
    }
    
    public void setActionData(Object actionData) {
        this.actionData = actionData;
    }
    
    public boolean hasQuickReplies() {
        return quickReplies != null && !quickReplies.isEmpty();
    }
}
