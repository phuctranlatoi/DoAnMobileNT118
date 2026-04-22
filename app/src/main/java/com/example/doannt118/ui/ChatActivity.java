package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.chatbot.ChatbotEngine;
import com.example.doannt118.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;

/**
 * CHAT ACTIVITY - Giao diện chatbot
 * 
 * Tích hợp ChatbotEngine (90% rule-based + 10% Gemini)
 */
public class ChatActivity extends AppCompatActivity {
    
    private RecyclerView rvChat;
    private EditText edtMessage;
    private Button btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> messages;
    private ChatbotEngine chatbot;
    private String maBenhNhan;
    private String maBacSi;
    private String userType; // "benhnhan" hoặc "bacsi"
    private String aiMode; // "patient_assistant" hoặc "doctor_assistant"
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Get parameters from intent
        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        userType = getIntent().getStringExtra("USER_TYPE");
        aiMode = getIntent().getStringExtra("AI_MODE");
        
        // Auto-detect user type if not provided
        if (userType == null) {
            userType = (maBacSi != null) ? "bacsi" : "benhnhan";
        }
        
        // Auto-detect AI mode if not provided
        if (aiMode == null) {
            aiMode = (maBacSi != null) ? "doctor_assistant" : "patient_assistant";
        }
        
        setupToolbar();
        initViews();
        initChatbot();
        sendWelcomeMessage();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Đặt title khác nhau cho bác sĩ và bệnh nhân
            if ("doctor_assistant".equals(aiMode)) {
                getSupportActionBar().setTitle("AI Assistant - Bác sĩ");
            } else {
                getSupportActionBar().setTitle("Trợ lý ảo");
            }
        }
    }
    
    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        
        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages);
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Scroll to bottom
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);
        
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Gửi khi nhấn Enter trên bàn phím
        edtMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }
    
    private void initChatbot() {
        // Khởi tạo chatbot với context phù hợp
        if ("doctor_assistant".equals(aiMode)) {
            chatbot = new ChatbotEngine(this, maBacSi, "bacsi");
        } else {
            chatbot = new ChatbotEngine(this, maBenhNhan, "benhnhan");
        }
    }
    
    private void sendWelcomeMessage() {
        String welcomeText;
        
        if ("doctor_assistant".equals(aiMode)) {
            // Enhanced welcome message for doctors
            welcomeText = "👨‍⚕️ **Xin chào Bác sĩ!**\n\n" +
                "🤖 Tôi là **AI Assistant** nâng cao của bạn. Tôi đã được cải tiến để hỗ trợ tốt hơn:\n\n" +
                "🩺 **Hỗ trợ chuyên môn:**\n" +
                "• Phân tích triệu chứng và gợi ý chẩn đoán\n" +
                "• Tư vấn phác đồ điều trị cập nhật\n" +
                "• Kiểm tra tương tác thuốc thông minh\n" +
                "• Tra cứu thông tin y khoa chuyên sâu\n\n" +
                "📊 **Quản lý thông minh:**\n" +
                "• Thống kê bệnh nhân và hiệu suất\n" +
                "• Quản lý lịch làm việc tối ưu\n" +
                "• Theo dõi và phân tích xu hướng\n\n" +
                "💡 **Tính năng mới:**\n" +
                "• Ghi nhớ ngữ cảnh cuộc trò chuyện\n" +
                "• Hiểu tiếng Việt tự nhiên tốt hơn\n" +
                "• Phản hồi thông minh và cá nhân hóa\n\n" +
                "Hãy hỏi tôi bất cứ điều gì! 🚀";
        } else {
            // Enhanced welcome message for patients
            welcomeText = "👋 **Xin chào!**\n\n" +
                "🤖 Tôi là **MediBot** - trợ lý ảo thông minh của phòng khám, được nâng cấp với nhiều tính năng mới:\n\n" +
                "🏥 **Dịch vụ y tế:**\n" +
                "• 📅 Đặt lịch khám nhanh chóng\n" +
                "• 👨‍⚕️ Tìm bác sĩ phù hợp\n" +
                "• 💊 Quản lý đơn thuốc thông minh\n" +
                "• 📋 Theo dõi sức khỏe cá nhân\n\n" +
                "💡 **Tư vấn sức khỏe:**\n" +
                "• Giải đáp thắc mắc y tế cơ bản\n" +
                "• Hướng dẫn chăm sóc sức khỏe\n" +
                "• Nhắc nhở uống thuốc đúng giờ\n\n" +
                "🆕 **Cải tiến mới:**\n" +
                "• Hiểu ngôn ngữ tự nhiên tốt hơn\n" +
                "• Ghi nhớ cuộc trò chuyện\n" +
                "• Phản hồi thông minh và thân thiện\n\n" +
                "Bạn cần hỗ trợ gì hôm nay? 😊";
        }
        
        ChatMessage welcomeMessage = new ChatMessage(welcomeText, ChatMessage.MessageType.BOT);
        
        messages.add(welcomeMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
        
        // Show initial quick replies based on user type
        showInitialQuickReplies();
    }
    
    private void showInitialQuickReplies() {
        List<String> quickReplies = new ArrayList<>();
        
        if ("doctor_assistant".equals(aiMode)) {
            quickReplies.add("👥 Bệnh nhân hôm nay");
            quickReplies.add("📅 Lịch làm việc");
            quickReplies.add("🩺 Hỗ trợ chẩn đoán");
            quickReplies.add("📊 Xem thống kê");
        } else {
            quickReplies.add("📅 Đặt lịch khám");
            quickReplies.add("👨‍⚕️ Tìm bác sĩ");
            quickReplies.add("💊 Xem đơn thuốc");
            quickReplies.add("🏥 Thông tin bệnh viện");
        }
        
        showQuickReplies(quickReplies);
    }
    
    private void sendMessage() {
        String messageText = edtMessage.getText().toString().trim();
        
        if (messageText.isEmpty()) {
            return;
        }
        
        // Add user message
        ChatMessage userMessage = new ChatMessage(messageText, ChatMessage.MessageType.USER);
        messages.add(userMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
        
        // Clear input
        edtMessage.setText("");
        
        // Hide quick replies when user types
        HorizontalScrollView quickRepliesContainer = findViewById(R.id.quickRepliesContainer);
        if (quickRepliesContainer != null) {
            quickRepliesContainer.setVisibility(android.view.View.GONE);
        }
        
        // Show typing indicator
        showTypingIndicator();
        
        // Process with enhanced chatbot
        chatbot.processMessage(messageText, new ChatbotEngine.ChatCallback() {
            @Override
            public void onResponse(com.example.doannt118.chatbot.ChatResponse response) {
                runOnUiThread(() -> {
                    // Hide typing indicator
                    hideTypingIndicator();
                    
                    // Add bot response with enhanced formatting
                    ChatMessage botMessage = new ChatMessage(
                        response.getMessage(),
                        ChatMessage.MessageType.BOT
                    );
                    
                    messages.add(botMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvChat.scrollToPosition(messages.size() - 1);
                    
                    // Show quick replies if available
                    showQuickReplies(response.getQuickReplies());
                    
                    // Handle special response types
                    handleSpecialResponseTypes(response);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Hide typing indicator
                    hideTypingIndicator();
                    
                    // Show user-friendly error message
                    String errorMessage = "😅 Xin lỗi, tôi gặp chút trục trặc. Hãy thử lại nhé!\n\n" +
                                         "💡 Bạn có thể:\n" +
                                         "• Thử hỏi lại bằng cách khác\n" +
                                         "• Sử dụng menu chức năng\n" +
                                         "• Liên hệ hỗ trợ nếu cần";
                    
                    ChatMessage errorMsg = new ChatMessage(errorMessage, ChatMessage.MessageType.BOT);
                    messages.add(errorMsg);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvChat.scrollToPosition(messages.size() - 1);
                    
                    // Show fallback quick replies
                    List<String> fallbackReplies = new ArrayList<>();
                    if ("doctor_assistant".equals(aiMode)) {
                        fallbackReplies.add("👥 Bệnh nhân hôm nay");
                        fallbackReplies.add("📊 Thống kê");
                        fallbackReplies.add("🤖 AI Assistant");
                    } else {
                        fallbackReplies.add("📅 Đặt lịch khám");
                        fallbackReplies.add("👨‍⚕️ Tìm bác sĩ");
                        fallbackReplies.add("📞 Liên hệ hỗ trợ");
                    }
                    showQuickReplies(fallbackReplies);
                });
            }
        });
    }
    
    private void showTypingIndicator() {
        // Add typing indicator message
        ChatMessage typingMessage = new ChatMessage("💭 Đang suy nghĩ...", ChatMessage.MessageType.BOT);
        typingMessage.setIsTyping(true);
        messages.add(typingMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }
    
    private void hideTypingIndicator() {
        // Remove typing indicator if it exists
        if (!messages.isEmpty()) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            if (lastMessage.getMessageType() == ChatMessage.MessageType.BOT && 
                lastMessage.isTyping()) {
                messages.remove(messages.size() - 1);
                adapter.notifyItemRemoved(messages.size());
            }
        }
    }
    
    private void handleSpecialResponseTypes(com.example.doannt118.chatbot.ChatResponse response) {
        // Handle different response types for enhanced user experience
        switch (response.getType()) {
            case DOCTOR_CARDS:
                // Could implement doctor cards view here
                break;
            case ACTION_BUTTONS:
                // Could implement action buttons here
                break;
            case CONFIRMATION:
                // Could add confirmation dialog here
                break;
            default:
                // Standard text response - already handled
                break;
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showQuickReplies(List<String> quickReplies) {
        HorizontalScrollView quickRepliesContainer = findViewById(R.id.quickRepliesContainer);
        LinearLayout quickRepliesLayout = findViewById(R.id.quickRepliesLayout);
        
        if (quickReplies == null || quickReplies.isEmpty()) {
            quickRepliesContainer.setVisibility(android.view.View.GONE);
            return;
        }
        
        // Clear previous quick replies
        quickRepliesLayout.removeAllViews();
        
        // Add new quick reply buttons
        for (String reply : quickReplies) {
            com.google.android.material.button.MaterialButton button = new com.google.android.material.button.MaterialButton(
                this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            
            button.setText(reply);
            button.setTextSize(14);
            button.setAllCaps(false);
            
            // Set margins
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0);
            button.setLayoutParams(params);
            
            // Click listener
            button.setOnClickListener(v -> {
                // Send the quick reply as user message
                edtMessage.setText(reply);
                sendMessage();
                
                // Hide quick replies after selection
                quickRepliesContainer.setVisibility(android.view.View.GONE);
            });
            
            quickRepliesLayout.addView(button);
        }
        
        quickRepliesContainer.setVisibility(android.view.View.VISIBLE);
    }
}