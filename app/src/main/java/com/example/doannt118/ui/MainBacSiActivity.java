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
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainBacSiActivity extends AppCompatActivity {

    private View toolbar;
    private TextView tvHoTen;
    private ImageView ivAvatar, btnNotification;
    private RecyclerView rvAppointments;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBacSi;
    private LichHenHomNayAdapter lichHenAdapter;
    private List<LichKham> lichHenList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bacsi);

        // Khởi tạo Repository
        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        if (maTaiKhoan == null) {
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
        rvAppointments = findViewById(R.id.rvAppointments);

        // Kiểm tra null cho các view
        if (toolbar == null || tvHoTen == null || progressBar == null || rvAppointments == null) {
            showError("Lỗi khởi tạo giao diện!");
            finish();
            return;
        }

        // Thiết lập RecyclerView
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvAppointments.setNestedScrollingEnabled(false);
        lichHenAdapter = new LichHenHomNayAdapter(lichHenList);
        rvAppointments.setAdapter(lichHenAdapter);

        // Ánh xạ và thêm sự kiện cho các chức năng
        View cardManageMedicalRecord = findViewById(R.id.cardManageMedicalRecord);
        View cardManageSchedule = findViewById(R.id.cardManageSchedule);
        View cardManagePrescription = findViewById(R.id.cardManagePrescription);
        View cardConfirmAppointment = findViewById(R.id.cardConfirmAppointment);
        View cardNhapMaKham = findViewById(R.id.cardNhapMaKham);
//        View cardSendNotification = findViewById(R.id.cardSendNotification);

        if (cardManageMedicalRecord != null) {
            cardManageMedicalRecord.setOnClickListener(v -> handleQuanLyBenhAn());
        }
        if (cardManageSchedule != null) {
            cardManageSchedule.setOnClickListener(v -> handleQuanLyLichLamViec());
        }
        if (cardManagePrescription != null) {
            cardManagePrescription.setOnClickListener(v -> handleAIAssistant());
        }
        if (cardConfirmAppointment != null) {
            cardConfirmAppointment.setOnClickListener(v -> handleXacNhanLichKham());
        }
        if (cardNhapMaKham != null) {
            cardNhapMaKham.setOnClickListener(v -> handleNhapMaKham());
        }
//        if (cardSendNotification != null) {
//            cardSendNotification.setOnClickListener(v -> handleGuiThongBao());
//        }

        // Xử lý Bottom Navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Đã ở trang chủ
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    // Mở danh sách tin nhắn từ bệnh nhân
                    if (maBacSi == null || maBacSi.isEmpty()) {
                        Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
                        loadUserInfo();
                        return false;
                    }
                    Intent intent = new Intent(this, DanhSachTinNhanBacSiActivity.class);
                    intent.putExtra("MA_BAC_SI", maBacSi);
                    intent.putExtra("TEN_BAC_SI", tvHoTen.getText().toString());
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    // Mở danh sách thông báo
                    if (maBacSi == null || maBacSi.isEmpty()) {
                        Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
                        loadUserInfo();
                        return false;
                    }
                    Intent intent = new Intent(this, ThongBaoActivity.class);
                    intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
                    intent.putExtra("MA_BAC_SI", maBacSi);
                    intent.putExtra("USER_TYPE", "bacsi");
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    handleProfile();
                    return false; // Không select để khi quay lại vẫn ở home
                }
                return false;
            });
        }

        // Hiển thị progress bar và load thông tin
        progressBar.setVisibility(View.VISIBLE);
        loadUserInfo();
    }

    private void loadUserInfo() {
        com.example.doannt118.utils.UserInfoLoader.loadBacSi(maTaiKhoan, repo,
            new com.example.doannt118.utils.UserInfoLoader.BacSiCallback() {
                @Override
                public void onSuccess(BacSi bacSi) {
                    tvHoTen.setText(bacSi.getHoTen());
                    maBacSi = bacSi.getMaBacSi();
                    
                    // Load avatar nếu có
                    if (bacSi.getAvatarUrl() != null && !bacSi.getAvatarUrl().isEmpty() && ivAvatar != null) {
                        Glide.with(MainBacSiActivity.this)
                            .load(bacSi.getAvatarUrl())
                            .placeholder(R.drawable.ic_avatar)
                            .error(R.drawable.ic_avatar)
                            .circleCrop()
                            .into(ivAvatar);
                    }
                    progressBar.setVisibility(View.GONE);
                    
                    // Load lịch hẹn hôm nay
                    loadLichHenHomNay();
                }
                
                @Override
                public void onError(String message) {
                    showError(message);
                    progressBar.setVisibility(View.GONE);
                }
            });
    }

    private void loadLichHenHomNay() {
        if (maBacSi == null || maBacSi.isEmpty()) return;

        // Lấy ngày hôm nay (từ 00:00:00 đến 23:59:59)
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calStart.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calEnd.getTime();

        // Query lịch khám của bác sĩ hôm nay với trạng thái XAC_NHAN
        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                lichHenList.clear();
                for (var doc : querySnapshot.getDocuments()) {
                    try {
                        String trangThai = doc.getString("trangThai");
                        if (!"XAC_NHAN".equals(trangThai)) continue;

                        // Kiểm tra ngày khám
                        Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                        if (ngayKhamTs == null) continue;
                        
                        Date ngayKham = ngayKhamTs.toDate();
                        if (ngayKham.before(startOfDay) || ngayKham.after(endOfDay)) continue;

                        LichKham lichKham = doc.toObject(LichKham.class);
                        if (lichKham != null) {
                            lichHenList.add(lichKham);
                        }
                    } catch (Exception e) {
                        Log.e("MainBacSi", "Error parsing LichKham", e);
                    }
                }

                // Sắp xếp theo giờ khám
                Collections.sort(lichHenList, (a, b) -> {
                    String gioA = a.getGioKham() != null ? a.getGioKham() : "";
                    String gioB = b.getGioKham() != null ? b.getGioKham() : "";
                    return gioA.compareTo(gioB);
                });

                lichHenAdapter.updateData(lichHenList);
                Log.d("MainBacSi", "Loaded " + lichHenList.size() + " appointments for today");
            },
            e -> {
                Log.e("MainBacSi", "Error loading appointments", e);
            });
    }

    private void handleProfile() {
        logActivity("Mở trang cá nhân");
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("USER_TYPE", "bacsi");
        startActivity(intent);
    }

    private void handleQuanLyHoSo() {
        logActivity("Quản lý hồ sơ cá nhân");
        startActivitySafe(QuanLyHoSoCaNhan.class);
    }

    private void handleQuanLyBenhAn() {
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, QuanLyBenhAnBacSiActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleQuanLyLichLamViec() {
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, QuanLyLichLamViecActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleAIAssistant() {
        // Mở AI Assistant với context bác sĩ
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
            loadUserInfo();
            return;
        }
        logActivity("Sử dụng AI Assistant");
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        intent.putExtra("USER_TYPE", "bacsi");
        intent.putExtra("AI_MODE", "doctor_assistant"); // Chế độ đặc biệt cho bác sĩ
        startActivity(intent);
    }

    private void handleXacNhanLichKham() {
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
            loadUserInfo(); // Thử load lại
            return;
        }
        Intent intent = new Intent(this, XacNhanLichKhamActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleQuanLyHoaDon() {
        // Mở danh sách hóa đơn
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ...", Toast.LENGTH_SHORT).show();
            loadUserInfo();
            return;
        }
        Intent intent = new Intent(this, DanhSachHoaDonActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        intent.putExtra("USER_TYPE", "bacsi");
        startActivity(intent);
    }

    private void handleNhapMaKham() {
        logActivity("Nhập mã khám");
        Intent intent = new Intent(this, NhapMaKhamActivity.class);
        startActivity(intent);
    }

    private void handleGuiThongBao() {
        logActivity("Gửi thông báo");
        if (maBacSi == null) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin bác sĩ!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, GuiThongBaoActivity.class);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

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

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        tvHoTen.setText("Họ tên: ");
    }
    

}