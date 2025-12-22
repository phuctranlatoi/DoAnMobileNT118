package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doannt118.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainBenhNhanActivity extends AppCompatActivity {

    private View toolbar;
    private TextView tvHoTen;
    private ImageView ivAvatar, btnNotification;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private String maTaiKhoan;
    private String maBenhNhan;
    private ActivityHistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_benhnhan);

        // Khởi tạo
        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showError("Mã tài khoản không hợp lệ!");
            finish();
            return;
        }
        


        // Ánh xạ View
        toolbar = findViewById(R.id.toolbar);
        tvHoTen = findViewById(R.id.tvHoTen);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnNotification = findViewById(R.id.btnNotification);
        progressBar = findViewById(R.id.progressBar);

        // Kiểm tra null
        if (toolbar == null || tvHoTen == null || progressBar == null) {
            showError("Lỗi khởi tạo giao diện!");
            finish();
            return;
        }

        // Thiết lập RecyclerView
//        rvActivityHistory.setLayoutManager(new LinearLayoutManager(this));
//        historyAdapter = new ActivityHistoryAdapter(new ArrayList<>());
//        rvActivityHistory.setAdapter(historyAdapter);

        // Xử lý sự kiện cho các chức năng
        View cardRegisterAppointment = findViewById(R.id.cardRegisterAppointment);
        View cardViewMedicalRecord = findViewById(R.id.cardViewMedicalRecord);
        View cardConfirmMedication = findViewById(R.id.cardConfirmMedication);
        View cardViewInvoice = findViewById(R.id.cardViewInvoice);
        View cardChatbot = findViewById(R.id.cardChatbot);
        View cardChatWithDoctor = findViewById(R.id.cardChatWithDoctor);

        if (cardRegisterAppointment != null) {
            cardRegisterAppointment.setOnClickListener(v -> handleDangKyLichKham());
        }
        if (cardViewMedicalRecord != null) {
            cardViewMedicalRecord.setOnClickListener(v -> handleXemBenhAn());
        }
        if (cardConfirmMedication != null) {
            cardConfirmMedication.setOnClickListener(v -> handleLichSuUongThuoc());
        }
        if (cardViewInvoice != null) {
            cardViewInvoice.setOnClickListener(v -> handleXemHoaDon());
        }
        if (cardChatbot != null) {
            cardChatbot.setOnClickListener(v -> handleChatbot());
        }
        if (cardChatWithDoctor != null) {
            cardChatWithDoctor.setOnClickListener(v -> handleChonBacSiChat());
        }

        // Xử lý sự kiện cho nút thông báo
        if (btnNotification != null) {
            btnNotification.setOnClickListener(v -> handleXemThongBao());
        }

        // Xử lý Bottom Navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Đã ở trang chủ
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    handleChatbot();
                    return true;
                } else if (itemId == R.id.nav_add) {
                    handleDanhSachTinNhan();
                    return true;
                } else if (itemId == R.id.nav_appointments) {
                    handleXemLichKham();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    handleProfile();
                    return false; // Không select để khi quay lại vẫn ở home
                }
                return false;
            });
        }

        // Hiển thị loading và load dữ liệu
        progressBar.setVisibility(View.VISIBLE);
        loadUserInfo();
//        loadActivityHistory();
    }

    private void loadUserInfo() {
        com.example.doannt118.utils.UserInfoLoader.loadBenhNhan(maTaiKhoan, repo,
            new com.example.doannt118.utils.UserInfoLoader.BenhNhanCallback() {
                @Override
                public void onSuccess(BenhNhan benhNhan) {
                    tvHoTen.setText(getSafeString(benhNhan.getHoTen()));
                    maBenhNhan = benhNhan.getMaBenhNhan();
                    
                    // Load avatar nếu có
                    if (benhNhan.getAvatarUrl() != null && !benhNhan.getAvatarUrl().isEmpty()) {
                        Glide.with(MainBenhNhanActivity.this)
                            .load(benhNhan.getAvatarUrl())
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .circleCrop()
                            .into(ivAvatar);
                    }
                    
                    // Lưu maBenhNhan vào SharedPreferences để dùng cho BootReceiver
                    getSharedPreferences("user_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("maBenhNhan", maBenhNhan)
                        .apply();
                    
                    // Setup nhắc nhở uống thuốc
                    setupMedicineReminders();
                    
                    hideProgress();
                }
                
                @Override
                public void onError(String message) {
                    showError(message);
                    hideProgress();
                }
            });
    }

//    private void loadActivityHistory() {
//        repo.getByField("LichSuHoatDong", "maTaiKhoan", maTaiKhoan,
//                querySnapshot -> {
//                    List<LichSuHoatDong> list = new ArrayList<>();
//                    for (var doc : querySnapshot.getDocuments()) {
//                        LichSuHoatDong item = doc.toObject(LichSuHoatDong.class);
//                        if (item != null) list.add(item);
//                    }
//                    historyAdapter = new ActivityHistoryAdapter(list);
//                    rvActivityHistory.setAdapter(historyAdapter);
//                },
//                e -> {
//                    Log.e("MainBenhNhanActivity", "Lỗi tải lịch sử: ", e);
//                    showError("Lỗi tải lịch sử hoạt động!");
//                    historyAdapter = new ActivityHistoryAdapter(new ArrayList<>());
//                    rvActivityHistory.setAdapter(historyAdapter);
//                });
//    }

    // === XỬ LÝ CHỨC NĂNG ===
    private void handleDangKyLichKham() {
        logActivity("Mở đăng ký lịch khám");
        // Mở danh sách bác sĩ để chọn
        Intent intent = new Intent(this, DanhSachBacSiActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleProfile() {
        logActivity("Mở trang cá nhân");
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("USER_TYPE", "benhnhan");
        startActivity(intent);
    }

    private void handleQuanLyHoSo() {
        logActivity("Quản lý hồ sơ cá nhân");
        startActivitySafe(QuanLyHoSoCaNhan.class);
    }

    private void handleXemLichKham() {
        logActivity("Xem lịch khám");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, LichKhamCuaToiActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleXemBenhAn() {
        logActivity("Xem bệnh án");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, XemBenhAnActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleLichSuUongThuoc() {
        logActivity("Điểm danh uống thuốc");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, QuanLyUongThuocActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleXacNhanDungThuoc() {
        logActivity("Xem đơn thuốc");
        // Lấy mã bệnh nhân trước
        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            Intent intent = new Intent(this, DanhSachDonThuocActivity.class);
                            intent.putExtra("MA_BENH_NHAN", benhNhan.getMaBenhNhan());
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
                    }
                },
                e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleXemThongBao() {
        logActivity("Xem thông báo");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, ThongBaoActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleChatbot() {
        logActivity("Mở trợ lý ảo");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleChonBacSiChat() {
        logActivity("Chọn bác sĩ để chat");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        
        // Mở màn hình chọn bác sĩ để chat
        Intent intent = new Intent(this, ChonBacSiChatActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        intent.putExtra("TEN_BENH_NHAN", tvHoTen.getText().toString());
        startActivity(intent);
    }

    private void handleDanhSachTinNhan() {
        logActivity("Xem danh sách tin nhắn");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        
        // Mở màn hình danh sách cuộc trò chuyện
        Intent intent = new Intent(this, DanhSachCuocTroChuyenBenhNhanActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        intent.putExtra("TEN_BENH_NHAN", tvHoTen.getText().toString());
        startActivity(intent);
    }

    private void handleXemHoaDon() {
        logActivity("Xem hóa đơn");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bệnh nhân...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, DanhSachHoaDonActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleDangXuat() {
        logActivity("Đăng xuất");
        
        // 🔥 FIX: Clear Stringee connection và cache trước khi logout
        try {
            com.example.doannt118.stringee.StringeeManager stringeeManager = 
                com.example.doannt118.stringee.StringeeManager.getInstance(this);
            stringeeManager.logout();
            Log.d("MainBenhNhanActivity", "✅ Stringee logout completed");
        } catch (Exception e) {
            Log.e("MainBenhNhanActivity", "❌ Error during Stringee logout: " + e.getMessage());
        }
        
        auth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // === HÀM HỖ TRỢ ===
    private void logActivity(String tenHoatDong) {
        String maLichSu = UUID.randomUUID().toString();
        LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, tenHoatDong, new Date(), "Truy cập " + tenHoatDong);
        repo.logActivity(lichSu);
    }

    private void startActivitySafe(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        startActivity(intent);
    }

    private String getSafeString(String value) {
        return value != null ? value : "N/A";
    }

    private void hideProgress() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        tvHoTen.setText("Họ tên: N/A");
    }
    
    /**
     * Setup nhắc nhở uống thuốc theo ca
     * Ca sáng: 7:30, Ca trưa: 11:30, Ca chiều: 17:00
     */
    private void setupMedicineReminders() {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) return;
        
        com.example.doannt118.utils.MedicineReminderManager reminderManager = 
            new com.example.doannt118.utils.MedicineReminderManager(this);
        reminderManager.setupRemindersForPatient(maBenhNhan);
        
        Log.d("MainBenhNhanActivity", "Medicine reminders setup for: " + maBenhNhan);
    }
    

}