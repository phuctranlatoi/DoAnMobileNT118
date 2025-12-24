package com.example.doannt118.chatbot;

import java.util.HashMap;
import java.util.Map;

/**
 * CONVERSATION CONTEXT - TỰ BUILD
 * 
 * Track trạng thái conversation và lưu data tạm
 * VD: Đang đặt lịch → lưu ngày, bác sĩ đã chọn
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
        COMPLETED                       // Hoàn thành
    }
    
    private ConversationState state;
    private Map<String, Object> data;
    private String currentIntent;
    
    public ConversationContext() {
        this.state = ConversationState.IDLE;
        this.data = new HashMap<>();
    }
    
    public void setState(ConversationState state) {
        this.state = state;
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
    
    public void reset() {
        state = ConversationState.IDLE;
        data.clear();
        currentIntent = null;
    }
    
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
}
