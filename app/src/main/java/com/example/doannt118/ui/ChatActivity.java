package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
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
            // Welcome message cho bác sĩ
            welcomeText = "Xin chào Bác sĩ! 👨‍⚕️\n\n" +
                "Tôi là AI Assistant của bạn. Tôi có thể giúp:\n\n" +
                "📊 Thống kê bệnh nhân\n" +
                "📅 Quản lý lịch làm việc\n" +
                "🔍 Tra cứu thông tin bệnh nhân\n" +
                "💊 Tra cứu thuốc và tương tác\n" +
                "📋 Tạo báo cáo nhanh\n" +
                "🏥 Xem lịch sử khám bệnh\n" +
                "💡 Gợi ý chẩn đoán\n\n" +
                "Bác sĩ cần hỗ trợ gì?";
        } else {
            // Welcome message cho bệnh nhân
            welcomeText = "Xin chào! 👋\n\n" +
                "Tôi là trợ lý ảo của phòng khám. Tôi có thể giúp bạn:\n\n" +
                "📅 Đặt lịch khám\n" +
                "💊 Xem đơn thuốc\n" +
                "🏥 Xem bệnh án\n" +
                "👨‍⚕️ Tìm bác sĩ\n" +
                "💰 Xem hóa đơn\n" +
                "❓ Tư vấn sức khỏe\n\n" +
                "Bạn cần giúp gì?";
        }
        
        ChatMessage welcomeMessage = new ChatMessage(welcomeText, ChatMessage.MessageType.BOT);
        
        messages.add(welcomeMessage);
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
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
        
        // Process with chatbot
        chatbot.processMessage(messageText, new ChatbotEngine.ChatCallback() {
            @Override
            public void onResponse(com.example.doannt118.chatbot.ChatResponse response) {
                runOnUiThread(() -> {
                    // Add bot response
                    ChatMessage botMessage = new ChatMessage(
                        response.getMessage(),
                        ChatMessage.MessageType.BOT
                    );
                    
                    messages.add(botMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvChat.scrollToPosition(messages.size() - 1);
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    
                    // Add error message
                    ChatMessage errorMessage = new ChatMessage(
                        "Xin lỗi, tôi gặp lỗi. Vui lòng thử lại!",
                        ChatMessage.MessageType.BOT
                    );
                    messages.add(errorMessage);
                    adapter.notifyItemInserted(messages.size() - 1);
                    rvChat.scrollToPosition(messages.size() - 1);
                });
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
