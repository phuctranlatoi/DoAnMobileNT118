package com.example.doannt118.chatbot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ENHANCED CONVERSATION CONTEXT
 * 
 * Advanced conversation state management with memory and context awareness
 * Supports multi-turn conversations and complex workflows
 */
public class ConversationContext {
    
    public enum ConversationState {
        IDLE,                           // Chờ input mới
        WAITING_ROLE_SELECTION,         // Đang chờ chọn role (Bệnh nhân/Bác sĩ)
        WAITING_AUTHENTICATION,         // Đang chờ xác thực thông tin
        WAITING_DATE,                   // Đang chờ user nhập ngày
        WAITING_SPECIALTY_SELECTION,    // Đang chờ chọn chuyên khoa
        WAITING_DOCTOR_SELECTION,       // Đang chờ chọn bác sĩ
        WAITING_TIME_SELECTION,         // Đang chờ chọn giờ
        WAITING_CONFIRMATION,           // Đang chờ xác nhận
        WAITING_CANCEL_SELECTION,       // Đang chờ chọn lịch để hủy
        WAITING_SCHEDULE_ACTION,        // Đang chờ chọn hành động với lịch (bác sĩ)
        WAITING_SCHEDULE_UPDATE,        // Đang chờ cập nhật lịch làm việc
        WAITING_AI_FOLLOWUP,           // Đang chờ câu hỏi tiếp theo cho AI
        WAITING_MEDICAL_DETAILS,       // Đang chờ chi tiết y tế
        COMPLETED                       // Hoàn thành
    }
    
    private ConversationState state;
    private Map<String, Object> data;
    private String currentIntent;
    private List<String> conversationHistory;
    private long lastInteractionTime;
    private int conversationTurn;
    private String lastUserMessage;
    private String lastBotResponse;
    
    public ConversationContext() {
        this.state = ConversationState.IDLE;
        this.data = new HashMap<>();
        this.conversationHistory = new ArrayList<>();
        this.lastInteractionTime = System.currentTimeMillis();
        this.conversationTurn = 0;
    }
    
    public void setState(ConversationState state) {
        this.state = state;
        this.lastInteractionTime = System.currentTimeMillis();
    }
    
    public ConversationState getState() {
        return state;
    }
    
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    public Object getData(String key) {
        return data.get(key);
    }
    
    public String getString(String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }
    
    public void setCurrentIntent(String intent) {
        this.currentIntent = intent;
    }
    
    public String getCurrentIntent() {
        return currentIntent;
    }
    
    /**
     * Enhanced conversation management
     */
    public void addToHistory(String message, boolean isUser) {
        String timestamp = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(new java.util.Date());
        String entry = "[" + timestamp + "] " + (isUser ? "User: " : "Bot: ") + message;
        
        conversationHistory.add(entry);
        
        // Keep only last 10 entries to prevent memory issues
        if (conversationHistory.size() > 10) {
            conversationHistory.remove(0);
        }
        
        if (isUser) {
            lastUserMessage = message;
            conversationTurn++;
        } else {
            lastBotResponse = message;
        }
        
        lastInteractionTime = System.currentTimeMillis();
    }
    
    public List<String> getConversationHistory() {
        return new ArrayList<>(conversationHistory);
    }
    
    public String getLastUserMessage() {
        return lastUserMessage;
    }
    
    public String getLastBotResponse() {
        return lastBotResponse;
    }
    
    public int getConversationTurn() {
        return conversationTurn;
    }
    
    public long getLastInteractionTime() {
        return lastInteractionTime;
    }
    
    /**
     * Check if conversation has been idle for too long
     */
    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - lastInteractionTime > timeoutMillis;
    }
    
    /**
     * Get conversation summary for AI context
     */
    public String getConversationSummary() {
        if (conversationHistory.isEmpty()) {
            return "Cuộc trò chuyện mới bắt đầu.";
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append("Lịch sử trò chuyện (").append(conversationHistory.size()).append(" tin nhắn):\n");
        
        // Get last 3 messages for context
        int start = Math.max(0, conversationHistory.size() - 3);
        for (int i = start; i < conversationHistory.size(); i++) {
            summary.append(conversationHistory.get(i)).append("\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Enhanced reset with partial data preservation
     */
    public void reset() {
        reset(false);
    }
    
    public void reset(boolean preserveUserInfo) {
        ConversationState oldState = this.state;
        this.state = ConversationState.IDLE;
        this.currentIntent = null;
        this.conversationTurn = 0;
        
        if (!preserveUserInfo) {
            this.data.clear();
            this.conversationHistory.clear();
            this.lastUserMessage = null;
            this.lastBotResponse = null;
        } else {
            // Preserve important user data
            Object userType = data.get("userType");
            Object userId = data.get("userId");
            Object userName = data.get("userName");
            
            this.data.clear();
            
            if (userType != null) data.put("userType", userType);
            if (userId != null) data.put("userId", userId);
            if (userName != null) data.put("userName", userName);
        }
        
        this.lastInteractionTime = System.currentTimeMillis();
    }
    
    /**
     * Context-aware data management
     */
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
    
    public void removeData(String key) {
        data.remove(key);
    }
    
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
    
    /**
     * Smart context switching
     */
    public boolean canSwitchToIntent(String newIntent) {
        // Allow switching from IDLE state
        if (state == ConversationState.IDLE) {
            return true;
        }
        
        // Allow switching if user explicitly requests it
        if (lastUserMessage != null) {
            String lower = lastUserMessage.toLowerCase();
            return lower.contains("hủy") || 
                   lower.contains("dừng") || 
                   lower.contains("quay lại") ||
                   lower.contains("menu") ||
                   lower.contains("bắt đầu lại");
        }
        
        return false;
    }
    
    /**
     * Get contextual suggestions based on current state
     */
    public List<String> getContextualSuggestions() {
        List<String> suggestions = new ArrayList<>();
        
        switch (state) {
            case IDLE:
                String userType = getString("userType");
                if ("benhnhan".equals(userType)) {
                    suggestions.add("📅 Đặt lịch khám");
                    suggestions.add("👀 Xem lịch của tôi");
                    suggestions.add("💊 Xem đơn thuốc");
                    suggestions.add("👨‍⚕️ Tìm bác sĩ");
                } else if ("bacsi".equals(userType)) {
                    suggestions.add("👥 Bệnh nhân hôm nay");
                    suggestions.add("📅 Lịch làm việc");
                    suggestions.add("✅ Xác nhận lịch khám");
                    suggestions.add("🤖 AI Assistant");
                }
                break;
                
            case WAITING_CONFIRMATION:
                suggestions.add("✅ Xác nhận");
                suggestions.add("❌ Hủy bỏ");
                suggestions.add("🔄 Thay đổi");
                break;
                
            default:
                suggestions.add("🏠 Về menu chính");
                suggestions.add("❌ Hủy bỏ");
                break;
        }
        
        return suggestions;
    }
}
