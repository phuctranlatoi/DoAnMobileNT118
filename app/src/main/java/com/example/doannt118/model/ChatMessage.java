package com.example.doannt118.model;

import java.util.Date;
import java.util.List;

public class ChatMessage {
    
    public enum MessageType {
        USER,
        BOT,
        SYSTEM,
        ACTION_BUTTONS,  // Tin nhắn có các nút action
        DOCTOR_CARD,     // Card hiển thị thông tin bác sĩ
        APPOINTMENT_CARD // Card hiển thị lịch hẹn
    }
    
    public enum ActionType {
        NONE,
        BOOK_APPOINTMENT,    // Đặt lịch khám
        VIEW_APPOINTMENTS,   // Xem lịch khám
        VIEW_PRESCRIPTIONS,  // Xem đơn thuốc
        FIND_DOCTOR,         // Tìm bác sĩ
        VIEW_DOCTOR_SCHEDULE,// Xem lịch bác sĩ
        CONFIRM,             // Xác nhận
        CANCEL               // Hủy
    }
    
    private String id;
    private String text;
    private MessageType type;
    private Date timestamp;
    private List<String> quickReplies;
    private List<ActionButton> actionButtons;
    private Object cardData; // Dữ liệu cho card (BacSi, LichKham, etc.)
    
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
    
    public List<ActionButton> getActionButtons() { return actionButtons; }
    
    public void setActionButtons(List<ActionButton> actionButtons) {
        this.actionButtons = actionButtons;
    }
    
    public boolean hasActionButtons() {
        return actionButtons != null && !actionButtons.isEmpty();
    }
    
    public Object getCardData() { return cardData; }
    
    public void setCardData(Object cardData) {
        this.cardData = cardData;
    }
    
    // Inner class cho Action Button
    public static class ActionButton {
        private String text;
        private ActionType actionType;
        private String actionData;
        private int iconResId;
        private boolean isPrimary;
        
        public ActionButton(String text, ActionType actionType) {
            this.text = text;
            this.actionType = actionType;
            this.isPrimary = false;
        }
        
        public ActionButton(String text, ActionType actionType, String actionData) {
            this.text = text;
            this.actionType = actionType;
            this.actionData = actionData;
            this.isPrimary = false;
        }
        
        public String getText() { return text; }
        public ActionType getActionType() { return actionType; }
        public String getActionData() { return actionData; }
        public int getIconResId() { return iconResId; }
        public boolean isPrimary() { return isPrimary; }
        
        public ActionButton setIcon(int iconResId) {
            this.iconResId = iconResId;
            return this;
        }
        
        public ActionButton setPrimary(boolean primary) {
            this.isPrimary = primary;
            return this;
        }
    }
}
