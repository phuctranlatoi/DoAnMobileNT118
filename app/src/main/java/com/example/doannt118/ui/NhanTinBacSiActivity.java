package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.TinNhanBacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.stringee.StringeeManager;
import com.example.doannt118.utils.NotificationHelper;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.ArrayList;
import java.util.List;

public class NhanTinBacSiActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextView tvTenBacSi, tvTrangThaiBacSi;
    private CircleImageView ivAvatarBacSi;
    private RecyclerView rvTinNhan;
    private EditText etTinNhan;
    private ImageButton btnGui;
    private View progressBar;
    
    private TinNhanBacSiAdapter adapter;
    private FirestoreRepository repository;
    private ListenerRegistration messageListener;
    
    private String maBenhNhan;
    private String maBacSi;
    private String tenBenhNhan;
    private String tenBacSi;
    private BacSi bacSi;
    private boolean isDoctorView = false; // true nếu là view của bác sĩ
    private boolean isMessageLoaded = false; // flag để tránh load tin nhắn nhiều lần
    
    // Call buttons
    private ImageButton btnVoiceCall, btnVideoCall;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhan_tin_bac_si);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        
        if (maBacSi != null) {
            loadThongTinBacSi();
            // Chỉ load tin nhắn nếu đã có maBenhNhan hoặc không phải view bệnh nhân
            if (!TextUtils.isEmpty(maBenhNhan) || isDoctorView) {
                loadTinNhan();
                isMessageLoaded = true;
            }
            // Nếu maBenhNhan trống và không phải doctor view, 
            // loadTinNhan() sẽ được gọi trong getDataFromIntent() sau khi load user info
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin bác sĩ!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTenBacSi = findViewById(R.id.tvTenBacSi);
        tvTrangThaiBacSi = findViewById(R.id.tvTrangThaiBacSi);
        ivAvatarBacSi = findViewById(R.id.ivAvatarBacSi);
        rvTinNhan = findViewById(R.id.rvTinNhan);
        etTinNhan = findViewById(R.id.etTinNhan);
        btnGui = findViewById(R.id.btnGui);
        progressBar = findViewById(R.id.progressBar);
        btnVoiceCall = findViewById(R.id.btnVoiceCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);
        
        repository = new FirestoreRepository();
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        maBacSi = intent.getStringExtra("MA_BAC_SI");
        maBenhNhan = intent.getStringExtra("MA_BENH_NHAN");
        tenBenhNhan = intent.getStringExtra("TEN_BENH_NHAN");
        tenBacSi = intent.getStringExtra("TEN_BAC_SI");
        isDoctorView = intent.getBooleanExtra("IS_DOCTOR_VIEW", false);
        
        // 🔥 FIX: Xử lý thông tin user dựa trên role
        if (isDoctorView) {
            // Bác sĩ view: Đảm bảo có đủ thông tin bác sĩ và bệnh nhân
            if (TextUtils.isEmpty(maBacSi)) {
                // Lấy maBacSi từ SessionManager nếu không có trong Intent
                try {
                    com.example.doannt118.utils.SessionManager sessionManager = new com.example.doannt118.utils.SessionManager(this);
                    maBacSi = sessionManager.getMaTaiKhoan();
                    Log.d("NhanTinBacSi", "🔍 Doctor view - maBacSi from SessionManager: " + maBacSi);
                } catch (Exception e) {
                    Log.e("NhanTinBacSi", "❌ Error getting maBacSi from SessionManager: " + e.getMessage());
                }
            }
            
            if (TextUtils.isEmpty(maBacSi) || TextUtils.isEmpty(maBenhNhan)) {
                Log.e("NhanTinBacSi", "❌ Missing info for doctor view - maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan);
                Toast.makeText(this, "Thiếu thông tin bác sĩ hoặc bệnh nhân!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            // Bệnh nhân view: Lấy thông tin bệnh nhân từ SharedPreferences nếu cần
            if (TextUtils.isEmpty(maBenhNhan)) {
                android.content.SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
                maBenhNhan = prefs.getString("maBenhNhan", "");
                tenBenhNhan = prefs.getString("tenBenhNhan", "");
                
                if (TextUtils.isEmpty(maBenhNhan)) {
                    Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
            
            // Load tin nhắn sau khi có thông tin bệnh nhân (chỉ nếu chưa load)
            if (!isMessageLoaded) {
                loadTinNhan();
                isMessageLoaded = true;
            }
        }
        
        Log.d("NhanTinBacSi", "🔍 Final info - isDoctorView: " + isDoctorView + ", maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new TinNhanBacSiAdapter(isDoctorView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Cuộn xuống tin nhắn mới nhất
        rvTinNhan.setLayoutManager(layoutManager);
        rvTinNhan.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnGui.setOnClickListener(v -> guiTinNhan());
        
        etTinNhan.setOnEditorActionListener((v, actionId, event) -> {
            guiTinNhan();
            return true;
        });
        
        btnVoiceCall.setOnClickListener(v -> {
            Log.d("NhanTinBacSi", "🔘 Voice call button clicked - isDoctorView: " + isDoctorView);
            makeVoiceCall();
        });
        btnVideoCall.setOnClickListener(v -> {
            Log.d("NhanTinBacSi", "🔘 Video call button clicked - isDoctorView: " + isDoctorView);
            makeVideoCall();
        });
    }
    
    private void loadThongTinBacSi() {
        if (isDoctorView) {
            // Nếu là view của bác sĩ, hiển thị thông tin bệnh nhân
            tvTenBacSi.setText(tenBenhNhan);
            tvTrangThaiBacSi.setText("Bệnh nhân");
            ivAvatarBacSi.setImageResource(R.drawable.ic_patient);
        } else {
            // Nếu là view của bệnh nhân, hiển thị thông tin bác sĩ
            FirebaseFirestore.getInstance().collection("BacSi").document(maBacSi)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        bacSi = documentSnapshot.toObject(BacSi.class);
                        if (bacSi != null) {
                            tvTenBacSi.setText("BS. " + bacSi.getHoTen());
                            tvTrangThaiBacSi.setText("Đang hoạt động");
                            // Có thể load avatar từ URL nếu có
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải thông tin bác sĩ: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show());
        }
    }
    
    private void loadTinNhan() {
        if (TextUtils.isEmpty(maBenhNhan) || TextUtils.isEmpty(maBacSi)) {
            android.util.Log.d("NhanTinBacSi", "Thiếu thông tin: maBenhNhan=" + maBenhNhan + ", maBacSi=" + maBacSi);
            Toast.makeText(this, "Thiếu thông tin để tải tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo conversationId để đảm bảo tính duy nhất
        String conversationId = TinNhanBacSi.generateConversationId(maBenhNhan, maBacSi);
        
        android.util.Log.d("NhanTinBacSi", "Bắt đầu load tin nhắn với conversationId: " + conversationId);
        showLoading(true);
        
        // Remove listener cũ nếu có
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        
        // Tạo query để lấy tin nhắn theo conversationId (đảm bảo chỉ lấy tin nhắn của 1 cuộc trò chuyện)
        Query query = FirebaseFirestore.getInstance()
            .collection("TinNhanBacSi")
            .whereEqualTo("conversationId", conversationId)
            .whereEqualTo("maBenhNhan", maBenhNhan)  // Double check để đảm bảo
            .whereEqualTo("maBacSi", maBacSi);       // Double check để đảm bảo
        
        // Lắng nghe thay đổi real-time
        messageListener = query.addSnapshotListener((querySnapshot, e) -> {
            showLoading(false);
            
            if (e != null) {
                Toast.makeText(this, "Lỗi tải tin nhắn: " + e.getMessage(), 
                              Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (querySnapshot != null) {
                android.util.Log.d("NhanTinBacSi", "Snapshot received: " + querySnapshot.size() + " documents for conversationId: " + conversationId);
                
                List<TinNhanBacSi> danhSachTinNhan = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    TinNhanBacSi tinNhan = doc.toObject(TinNhanBacSi.class);
                    if (tinNhan != null) {
                        tinNhan.setId(doc.getId());
                        
                        // Validate dữ liệu tin nhắn để đảm bảo không bị lộn xộn
                        if (validateMessage(tinNhan, maBenhNhan, maBacSi)) {
                            danhSachTinNhan.add(tinNhan);
                            android.util.Log.d("NhanTinBacSi", "Valid message: " + tinNhan.getNoiDung() + " - ID: " + doc.getId());
                        } else {
                            android.util.Log.w("NhanTinBacSi", "Invalid message filtered out: " + doc.getId());
                        }
                    }
                }
                
                // Sort tin nhắn theo thời gian
                danhSachTinNhan.sort((t1, t2) -> {
                    if (t1.getThoiGianGui() == null) return -1;
                    if (t2.getThoiGianGui() == null) return 1;
                    return t1.getThoiGianGui().compareTo(t2.getThoiGianGui());
                });
                
                android.util.Log.d("NhanTinBacSi", "Setting " + danhSachTinNhan.size() + " validated messages to adapter");
                adapter.setData(danhSachTinNhan);
                
                // Cuộn xuống tin nhắn mới nhất
                if (!danhSachTinNhan.isEmpty()) {
                    rvTinNhan.scrollToPosition(danhSachTinNhan.size() - 1);
                }
            }
        });
    }
    
    /**
     * Validate tin nhắn để đảm bảo không bị lộn xộn
     * Kiểm tra tin nhắn có thuộc về đúng cuộc trò chuyện này không
     */
    private boolean validateMessage(TinNhanBacSi tinNhan, String expectedMaBenhNhan, String expectedMaBacSi) {
        if (tinNhan == null) return false;
        
        // Kiểm tra mã bệnh nhân và bác sĩ
        if (!expectedMaBenhNhan.equals(tinNhan.getMaBenhNhan()) || 
            !expectedMaBacSi.equals(tinNhan.getMaBacSi())) {
            android.util.Log.w("NhanTinBacSi", "Message validation failed - Expected: " + 
                expectedMaBenhNhan + "/" + expectedMaBacSi + 
                ", Got: " + tinNhan.getMaBenhNhan() + "/" + tinNhan.getMaBacSi());
            return false;
        }
        
        // Kiểm tra conversationId nếu có
        String expectedConversationId = TinNhanBacSi.generateConversationId(expectedMaBenhNhan, expectedMaBacSi);
        if (tinNhan.getConversationId() != null && !expectedConversationId.equals(tinNhan.getConversationId())) {
            android.util.Log.w("NhanTinBacSi", "ConversationId validation failed - Expected: " + 
                expectedConversationId + ", Got: " + tinNhan.getConversationId());
            return false;
        }
        
        // Kiểm tra nội dung tin nhắn không rỗng
        if (TextUtils.isEmpty(tinNhan.getNoiDung())) {
            android.util.Log.w("NhanTinBacSi", "Empty message content filtered out");
            return false;
        }
        
        return true;
    }
    
    private void guiTinNhan() {
        String noiDung = etTinNhan.getText().toString().trim();
        
        if (TextUtils.isEmpty(noiDung)) {
            Toast.makeText(this, "Vui lòng nhập nội dung tin nhắn", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (TextUtils.isEmpty(maBenhNhan) || TextUtils.isEmpty(maBacSi)) {
            Toast.makeText(this, "Không tìm thấy thông tin cần thiết", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate độ dài tin nhắn
        if (noiDung.length() > 1000) {
            Toast.makeText(this, "Tin nhắn quá dài (tối đa 1000 ký tự)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo tin nhắn mới với conversationId
        TinNhanBacSi tinNhan;
        if (isDoctorView) {
            // Bác sĩ gửi tin nhắn
            tinNhan = new TinNhanBacSi(
                noiDung,
                maBenhNhan,
                maBacSi,
                TinNhanBacSi.LoaiTinNhan.BAC_SI,
                tenBacSi != null ? tenBacSi : "Bác sĩ"
            );
        } else {
            // Bệnh nhân gửi tin nhắn
            if (TextUtils.isEmpty(tenBenhNhan)) {
                Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                return;
            }
            tinNhan = new TinNhanBacSi(
                noiDung,
                maBenhNhan,
                maBacSi,
                TinNhanBacSi.LoaiTinNhan.BENH_NHAN,
                tenBenhNhan
            );
        }
        
        // Log để debug
        android.util.Log.d("NhanTinBacSi", "Sending message with conversationId: " + tinNhan.getConversationId());
        android.util.Log.d("NhanTinBacSi", "Message details - maBenhNhan: " + tinNhan.getMaBenhNhan() + 
                          ", maBacSi: " + tinNhan.getMaBacSi() + ", loai: " + tinNhan.getLoaiTinNhan());
        
        // Vô hiệu hóa nút gửi để tránh gửi duplicate
        btnGui.setEnabled(false);
        etTinNhan.setEnabled(false);
        
        // Lưu tin nhắn vào Firestore
        FirebaseFirestore.getInstance().collection("TinNhanBacSi")
            .add(tinNhan)
            .addOnSuccessListener(documentReference -> {
                // Xóa nội dung EditText
                etTinNhan.setText("");
                btnGui.setEnabled(true);
                etTinNhan.setEnabled(true);
                
                android.util.Log.d("NhanTinBacSi", "Message sent successfully with ID: " + documentReference.getId());
                
                // Gửi push notification
                NotificationHelper.sendMessageNotification(tinNhan);
                
                // Giả lập cập nhật trạng thái tin nhắn từ DA_GUI → DA_NHAN
                simulateMessageDelivery(documentReference.getId());
                
                // Tin nhắn sẽ được cập nhật tự động qua listener
            })
            .addOnFailureListener(e -> {
                btnGui.setEnabled(true);
                etTinNhan.setEnabled(true);
                
                android.util.Log.e("NhanTinBacSi", "Failed to send message: " + e.getMessage());
                Toast.makeText(this, "Lỗi gửi tin nhắn: " + e.getMessage(), 
                              Toast.LENGTH_SHORT).show();
            });
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void makeVoiceCall() {
        Log.d("NhanTinBacSi", "🎯 makeVoiceCall() called");
        
        // Kiểm tra kết nối Stringee trước
        StringeeManager stringeeManager = StringeeManager.getInstance(this);
        
        if (!stringeeManager.isConnected()) {
            Toast.makeText(this, "❌ Chưa kết nối được với server. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
            // Try to reconnect
            connectToStringee();
            return;
        }
        
        // Debug: Kiểm tra dữ liệu
        Log.d("NhanTinBacSi", "🔍 makeVoiceCall - maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan);
        Log.d("NhanTinBacSi", "🔍 isDoctorView: " + isDoctorView + ", tenBacSi: " + tenBacSi);
        
        if (TextUtils.isEmpty(maBacSi) || TextUtils.isEmpty(maBenhNhan)) {
            Toast.makeText(this, "❌ Lỗi: Thiếu thông tin - maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan, Toast.LENGTH_LONG).show();
            return;
        }
        
        // Thực hiện cuộc gọi voice thực sự với Stringee
        String fromUserId, toUserId, callerName;
        if (isDoctorView) {
            // Bác sĩ gọi cho bệnh nhân
            fromUserId = "doctor_" + maBacSi;
            toUserId = "patient_" + maBenhNhan;
            callerName = tenBacSi != null ? tenBacSi : "Bác sĩ";
        } else {
            // Bệnh nhân gọi cho bác sĩ
            fromUserId = "patient_" + maBenhNhan;
            toUserId = "doctor_" + maBacSi;
            callerName = tenBacSi != null ? tenBacSi : "Bác sĩ";
        }
        
        Log.d("NhanTinBacSi", "🎯 Starting voice call: " + fromUserId + " -> " + toUserId);
        Log.d("NhanTinBacSi", "🔍 DEBUG INTENT DATA:");
        Log.d("NhanTinBacSi", "🔍 - CALLER_NAME: " + callerName);
        Log.d("NhanTinBacSi", "🔍 - CALLER_ID: " + fromUserId);
        Log.d("NhanTinBacSi", "🔍 - RECEIVER_ID: " + toUserId);
        Log.d("NhanTinBacSi", "🔍 - IS_INCOMING_CALL: false");
        Log.d("NhanTinBacSi", "🔍 - isDoctorView: " + isDoctorView);
        
        // Mở VoiceCallActivity - Activity sẽ tự tạo call
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra("CALLER_NAME", callerName);
        intent.putExtra("CALLER_ID", fromUserId);
        intent.putExtra("RECEIVER_ID", toUserId);
        intent.putExtra("IS_INCOMING_CALL", false);
        startActivity(intent);
    }
    
    private void makeVideoCall() {
        Log.d("NhanTinBacSi", "🎯 makeVideoCall() called");
        
        // Kiểm tra kết nối Stringee trước
        StringeeManager stringeeManager = StringeeManager.getInstance(this);
        
        if (!stringeeManager.isConnected()) {
            Toast.makeText(this, "❌ Chưa kết nối được với server. Vui lòng thử lại sau.", Toast.LENGTH_LONG).show();
            // Try to reconnect
            connectToStringee();
            return;
        }
        
        // Debug: Kiểm tra dữ liệu
        Log.d("NhanTinBacSi", "🔍 makeVideoCall - maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan);
        
        if (TextUtils.isEmpty(maBacSi) || TextUtils.isEmpty(maBenhNhan)) {
            Toast.makeText(this, "❌ Lỗi: Thiếu thông tin - maBacSi: " + maBacSi + ", maBenhNhan: " + maBenhNhan, Toast.LENGTH_LONG).show();
            return;
        }
        
        // Thực hiện cuộc gọi video thực sự với Stringee
        String fromUserId, toUserId, callerName;
        if (isDoctorView) {
            // Bác sĩ gọi cho bệnh nhân
            fromUserId = "doctor_" + maBacSi;
            toUserId = "patient_" + maBenhNhan;
            callerName = tenBacSi != null ? tenBacSi : "Bác sĩ";
        } else {
            // Bệnh nhân gọi cho bác sĩ
            fromUserId = "patient_" + maBenhNhan;
            toUserId = "doctor_" + maBacSi;
            callerName = tenBacSi != null ? tenBacSi : "Bác sĩ";
        }
        
        Log.d("NhanTinBacSi", "🎯 Starting video call: " + fromUserId + " -> " + toUserId);
        Log.d("NhanTinBacSi", "🔍 DEBUG VIDEO INTENT DATA:");
        Log.d("NhanTinBacSi", "🔍 - CALLER_NAME: " + callerName);
        Log.d("NhanTinBacSi", "🔍 - CALLER_ID: " + fromUserId);
        Log.d("NhanTinBacSi", "🔍 - RECEIVER_ID: " + toUserId);
        Log.d("NhanTinBacSi", "🔍 - IS_INCOMING_CALL: false");
        Log.d("NhanTinBacSi", "🔍 - isDoctorView: " + isDoctorView);
        
        // Mở VideoCallActivity - Activity sẽ tự tạo call
        Intent intent = new Intent(this, VideoCallActivity.class);
        intent.putExtra("CALLER_NAME", callerName);
        intent.putExtra("CALLER_ID", fromUserId);
        intent.putExtra("RECEIVER_ID", toUserId);
        intent.putExtra("IS_INCOMING_CALL", false);
        startActivity(intent);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Đánh dấu tin nhắn đã đọc khi vào chat
        if (!TextUtils.isEmpty(maBenhNhan) && !TextUtils.isEmpty(maBacSi)) {
            markMessagesAsRead();
        }
        
        // Connect to Stringee for calling features
        connectToStringee();
    }
    
    private void connectToStringee() {
        try {
            Log.d("NhanTinBacSi", "🚀 === BẮT ĐẦU KẾT NỐI STRINGEE ===");
            
            StringeeManager stringeeManager = StringeeManager.getInstance(this);
            
            // 🔥 VALIDATE DATA TRƯỚC KHI KẾT NỐI
            if (TextUtils.isEmpty(maBacSi) || TextUtils.isEmpty(maBenhNhan)) {
                Log.e("NhanTinBacSi", "❌ CRITICAL: Missing required data!");
                Log.e("NhanTinBacSi", "❌ maBacSi: " + maBacSi);
                Log.e("NhanTinBacSi", "❌ maBenhNhan: " + maBenhNhan);
                Toast.makeText(this, "❌ Lỗi: Thiếu thông tin cần thiết để kết nối", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Determine user ID based on role
            String userId;
            if (isDoctorView) {
                userId = "doctor_" + maBacSi;
            } else {
                userId = "patient_" + maBenhNhan;
            }
            
            Log.d("NhanTinBacSi", "🆔 Connection details:");
            Log.d("NhanTinBacSi", "🆔 - userId: " + userId);
            Log.d("NhanTinBacSi", "🆔 - isDoctorView: " + isDoctorView);
            Log.d("NhanTinBacSi", "🆔 - maBacSi: " + maBacSi);
            Log.d("NhanTinBacSi", "🆔 - maBenhNhan: " + maBenhNhan);
            
            // Set connection callback để theo dõi trạng thái
            stringeeManager.setConnectionCallback(new StringeeManager.StringeeConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d("NhanTinBacSi", "🎉 Stringee connected successfully!");
                    runOnUiThread(() -> {
                        Toast.makeText(NhanTinBacSiActivity.this, "✅ Đã kết nối server thành công!", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onDisconnected() {
                    Log.d("NhanTinBacSi", "⚠️ Stringee disconnected");
                    runOnUiThread(() -> {
                        Toast.makeText(NhanTinBacSiActivity.this, "⚠️ Mất kết nối server", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onConnectionError(String error) {
                    Log.e("NhanTinBacSi", "❌ Stringee connection error: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(NhanTinBacSiActivity.this, "❌ Lỗi kết nối: " + error, Toast.LENGTH_LONG).show();
                        
                        // 🔥 SHOW DEBUG INFO TO USER
                        if (error.contains("authentication") || error.contains("invalid signature")) {
                            showAuthenticationErrorDialog(error);
                        }
                    });
                }
            });
            
            // 🔥 DEBUG: Test token generation trước khi kết nối
            Log.d("NhanTinBacSi", "🧪 Testing token generation...");
            String testToken = com.example.doannt118.stringee.StringeeTokenGenerator.generateAccessToken("test_user_" + System.currentTimeMillis());
            if (testToken == null) {
                Log.e("NhanTinBacSi", "❌ CRITICAL: Cannot generate test token!");
                Toast.makeText(this, "❌ Lỗi tạo token xác thực. Kiểm tra API keys!", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Thử kết nối
            if (!stringeeManager.isConnected()) {
                Log.d("NhanTinBacSi", "🔄 Starting connection...");
                stringeeManager.connect(userId);
                
                // Debug connection sau 3 giây
                new android.os.Handler().postDelayed(() -> {
                    Log.d("NhanTinBacSi", "🧪 === CONNECTION STATUS CHECK ===");
                    Log.d("NhanTinBacSi", "🧪 Is connected: " + stringeeManager.isConnected());
                    
                    if (!stringeeManager.isConnected()) {
                        Log.d("NhanTinBacSi", "🧪 Still not connected, running debug...");
                        stringeeManager.debugUserInfo();
                        stringeeManager.testConnection();
                    }
                }, 3000);
                
            } else {
                Log.d("NhanTinBacSi", "🔄 Already connected, checking connection health...");
                stringeeManager.softReconnect();
            }
            
        } catch (Exception e) {
            Log.e("NhanTinBacSi", "💥 Exception connecting to Stringee: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "💥 Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Đánh dấu tất cả tin nhắn trong cuộc trò chuyện này đã được đọc
     */
    private void markMessagesAsRead() {
        // Xác định loại tin nhắn cần đánh dấu đã đọc
        TinNhanBacSi.LoaiTinNhan loaiTinNhanCanDanhDau;
        if (isDoctorView) {
            // Bác sĩ đọc tin nhắn từ bệnh nhân
            loaiTinNhanCanDanhDau = TinNhanBacSi.LoaiTinNhan.BENH_NHAN;
        } else {
            // Bệnh nhân đọc tin nhắn từ bác sĩ
            loaiTinNhanCanDanhDau = TinNhanBacSi.LoaiTinNhan.BAC_SI;
        }
        
        // Query tin nhắn chưa đọc
        FirebaseFirestore.getInstance()
            .collection("TinNhanBacSi")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .whereEqualTo("maBacSi", maBacSi)
            .whereEqualTo("loaiTinNhan", loaiTinNhanCanDanhDau)
            .whereNotEqualTo("trangThai", TinNhanBacSi.TrangThaiTinNhan.DA_XEM)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                // Update tất cả tin nhắn chưa đọc thành đã đọc
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    doc.getReference().update("trangThai", TinNhanBacSi.TrangThaiTinNhan.DA_XEM);
                }
                android.util.Log.d("NhanTinBacSi", "Đã đánh dấu " + querySnapshot.size() + " tin nhắn là đã đọc");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("NhanTinBacSi", "Lỗi đánh dấu tin nhắn đã đọc: " + e.getMessage());
            });
    }
    
    /**
     * Cập nhật trạng thái tin nhắn từ DA_GUI → DA_NHAN sau 1 giây (giả lập)
     */
    private void simulateMessageDelivery(String messageId) {
        new Handler().postDelayed(() -> {
            FirebaseFirestore.getInstance()
                .collection("TinNhanBacSi")
                .document(messageId)
                .update("trangThai", TinNhanBacSi.TrangThaiTinNhan.DA_NHAN)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("NhanTinBacSi", "Cập nhật trạng thái tin nhắn thành DA_NHAN: " + messageId);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("NhanTinBacSi", "Lỗi cập nhật trạng thái tin nhắn: " + e.getMessage());
                });
        }, 1000); // Delay 1 giây
    }
    
    /**
     * 🔥 SHOW AUTHENTICATION ERROR DIALOG WITH DEBUG INFO
     */
    private void showAuthenticationErrorDialog(String error) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("🔥 Lỗi Xác Thực Stringee");
        
        StringBuilder message = new StringBuilder();
        message.append("Chi tiết lỗi: ").append(error).append("\n\n");
        message.append("Thông tin debug:\n");
        message.append("- maBacSi: ").append(maBacSi).append("\n");
        message.append("- maBenhNhan: ").append(maBenhNhan).append("\n");
        message.append("- isDoctorView: ").append(isDoctorView).append("\n");
        
        String userId = isDoctorView ? "doctor_" + maBacSi : "patient_" + maBenhNhan;
        message.append("- userId: ").append(userId).append("\n\n");
        message.append("Giải pháp:\n");
        message.append("1. Kiểm tra API keys trong StringeeTokenGenerator\n");
        message.append("2. Kiểm tra kết nối internet\n");
        message.append("3. Thử đăng nhập lại");
        
        builder.setMessage(message.toString());
        builder.setPositiveButton("Thử lại", (dialog, which) -> {
            // Force reconnect
            StringeeManager.getInstance(this).forceReconnect();
        });
        builder.setNegativeButton("Đóng", null);
        builder.show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy listener để tránh memory leak
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}