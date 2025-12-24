package com.example.doannt118.chatbot;

import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.ChatMessage;
import java.util.List;

public class ChatResponse {
    
    public enum ResponseType {
        TEXT,           // Tin nhắn text thông thường
        CONFIRMATION,   // Cần xác nhận (Yes/No buttons)
        ACTION,         // Cần thực hiện action (mở màn hình khác)
        QUICK_REPLY,    // Có quick reply buttons
        ACTION_BUTTONS, // Có action buttons
        DOCTOR_CARDS    // Hiển thị danh sách bác sĩ dạng cards
    }
    
    private String message;
    private ResponseType type;
    private List<String> quickReplies;
    private List<ChatMessage.ActionButton> actionButtons;
    private List<BacSi> doctorCards;
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
        if (this.type == ResponseType.TEXT) {
            this.type = ResponseType.QUICK_REPLY;
        }
    }
    
    public boolean hasQuickReplies() {
        return quickReplies != null && !quickReplies.isEmpty();
    }
    
    public List<ChatMessage.ActionButton> getActionButtons() {
        return actionButtons;
    }
    
    public void setActionButtons(List<ChatMessage.ActionButton> actionButtons) {
        this.actionButtons = actionButtons;
        this.type = ResponseType.ACTION_BUTTONS;
    }
    
    public boolean hasActionButtons() {
        return actionButtons != null && !actionButtons.isEmpty();
    }
    
    public List<BacSi> getDoctorCards() {
        return doctorCards;
    }
    
    public void setDoctorCards(List<BacSi> doctorCards) {
        this.doctorCards = doctorCards;
        this.type = ResponseType.DOCTOR_CARDS;
    }
    
    public boolean hasDoctorCards() {
        return doctorCards != null && !doctorCards.isEmpty();
    }
    
    public Object getActionData() {
        return actionData;
    }
    
    public void setActionData(Object actionData) {
        this.actionData = actionData;
    }
}
