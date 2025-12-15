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
        
        // Nếu không có mã bệnh nhân và không phải view của bác sĩ, lấy từ SharedPreferences
        if (TextUtils.isEmpty(maBenhNhan) && !isDoctorView) {
            // Lấy từ SharedPreferences hoặc Intent
            android.content.SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
            maBenhNhan = prefs.getString("maBenhNhan", "");
            tenBenhNhan = prefs.getString("tenBenhNhan", "");
            
            if (TextUtils.isEmpty(maBenhNhan)) {
                Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            
            // Load tin nhắn sau khi có thông tin bệnh nhân (chỉ nếu chưa load)
            if (!isMessageLoaded) {
                loadTinNhan();
                isMessageLoaded = true;
            }
        }
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
        adapter = new TinNhanBacSiAdapter();
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
        
        btnVoiceCall.setOnClickListener(v -> makeVoiceCall());
        btnVideoCall.setOnClickListener(v -> makeVideoCall());
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
        
        android.util.Log.d("NhanTinBacSi", "Bắt đầu load tin nhắn: maBenhNhan=" + maBenhNhan + ", maBacSi=" + maBacSi);
        showLoading(true);
        
        // Remove listener cũ nếu có
        if (messageListener != null) {
            messageListener.remove();
            messageListener = null;
        }
        
        // Tạo query để lấy tin nhắn giữa bệnh nhân và bác sĩ
        // Không dùng orderBy để tránh lỗi index, sẽ sort trong code
        Query query = FirebaseFirestore.getInstance()
            .collection("TinNhanBacSi")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .whereEqualTo("maBacSi", maBacSi);
        
        // Lắng nghe thay đổi real-time
        messageListener = query.addSnapshotListener((querySnapshot, e) -> {
            showLoading(false);
            
            if (e != null) {
                Toast.makeText(this, "Lỗi tải tin nhắn: " + e.getMessage(), 
                              Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (querySnapshot != null) {
                android.util.Log.d("NhanTinBacSi", "Snapshot received: " + querySnapshot.size() + " documents");
                
                List<TinNhanBacSi> danhSachTinNhan = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    TinNhanBacSi tinNhan = doc.toObject(TinNhanBacSi.class);
                    if (tinNhan != null) {
                        tinNhan.setId(doc.getId());
                        danhSachTinNhan.add(tinNhan);
                        android.util.Log.d("NhanTinBacSi", "Message: " + tinNhan.getNoiDung() + " - ID: " + doc.getId());
                    }
                }
                
                // Sort tin nhắn theo thời gian
                danhSachTinNhan.sort((t1, t2) -> {
                    if (t1.getThoiGianGui() == null) return -1;
                    if (t2.getThoiGianGui() == null) return 1;
                    return t1.getThoiGianGui().compareTo(t2.getThoiGianGui());
                });
                
                android.util.Log.d("NhanTinBacSi", "Setting " + danhSachTinNhan.size() + " messages to adapter");
                adapter.setData(danhSachTinNhan);
                
                // Cuộn xuống tin nhắn mới nhất
                if (!danhSachTinNhan.isEmpty()) {
                    rvTinNhan.scrollToPosition(danhSachTinNhan.size() - 1);
                }
            }
        });
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
        
        // Tạo tin nhắn mới
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
        
        // Vô hiệu hóa nút gửi
        btnGui.setEnabled(false);
        
        // Lưu tin nhắn vào Firestore
        FirebaseFirestore.getInstance().collection("TinNhanBacSi")
            .add(tinNhan)
            .addOnSuccessListener(documentReference -> {
                // Xóa nội dung EditText
                etTinNhan.setText("");
                btnGui.setEnabled(true);
                
                // Gửi push notification
                NotificationHelper.sendMessageNotification(tinNhan);
                
                // Tin nhắn sẽ được cập nhật tự động qua listener
            })
            .addOnFailureListener(e -> {
                btnGui.setEnabled(true);
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
            
            // Determine user ID based on role
            String userId;
            if (isDoctorView) {
                userId = "doctor_" + maBacSi;
            } else {
                userId = "patient_" + maBenhNhan;
            }
            
            Log.d("NhanTinBacSi", "🆔 Connecting with userId: " + userId);
            Log.d("NhanTinBacSi", "🆔 isDoctorView: " + isDoctorView);
            Log.d("NhanTinBacSi", "🆔 maBacSi: " + maBacSi);
            Log.d("NhanTinBacSi", "🆔 maBenhNhan: " + maBenhNhan);
            
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
                    });
                }
            });
            
            // Thử kết nối
            if (!stringeeManager.isConnected()) {
                Log.d("NhanTinBacSi", "🔄 Starting connection...");
                stringeeManager.connect(userId);
                
                // Thêm test connection để debug
                new android.os.Handler().postDelayed(() -> {
                    Log.d("NhanTinBacSi", "🧪 Running test connection...");
                    stringeeManager.testConnection();
                }, 2000);
                
            } else {
                Log.d("NhanTinBacSi", "🔄 Already connected, force reconnecting...");
                stringeeManager.forceReconnect();
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy listener để tránh memory leak
        if (messageListener != null) {
            messageListener.remove();
        }
    }
}