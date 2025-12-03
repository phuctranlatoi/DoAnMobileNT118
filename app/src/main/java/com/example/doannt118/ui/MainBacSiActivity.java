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
import com.example.doannt118.ui.LichLamViecAdapter;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.Date;
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
    private LichLamViecAdapter appointmentAdapter;

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
        appointmentAdapter = new LichLamViecAdapter(this, new ArrayList<>());
        rvAppointments.setAdapter(appointmentAdapter);

        // Ánh xạ và thêm sự kiện cho các chức năng
        View cardManageMedicalRecord = findViewById(R.id.cardManageMedicalRecord);
        View cardManageSchedule = findViewById(R.id.cardManageSchedule);
        View cardManagePrescription = findViewById(R.id.cardManagePrescription);
        View cardConfirmAppointment = findViewById(R.id.cardConfirmAppointment);
        View cardSendNotification = findViewById(R.id.cardSendNotification);

        if (cardManageMedicalRecord != null) {
            cardManageMedicalRecord.setOnClickListener(v -> handleQuanLyBenhAn());
        }
        if (cardManageSchedule != null) {
            cardManageSchedule.setOnClickListener(v -> handleQuanLyLichLamViec());
        }
        if (cardManagePrescription != null) {
            cardManagePrescription.setOnClickListener(v -> handleQuanLyDonThuoc());
        }
        if (cardConfirmAppointment != null) {
            cardConfirmAppointment.setOnClickListener(v -> handleXacNhanLichKham());
        }
        if (cardSendNotification != null) {
            cardSendNotification.setOnClickListener(v -> handleGuiThongBao());
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
                    Toast.makeText(this, "Chức năng Tin nhắn đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Toast.makeText(this, "Chức năng Thông báo đang phát triển!", Toast.LENGTH_SHORT).show();
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
        repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSi != null) {
                            tvHoTen.setText(bacSi.getHoTen());
                            maBacSi = bacSi.getMaBacSi();
                            
                            // Load avatar nếu có
                            if (bacSi.getAvatarUrl() != null && !bacSi.getAvatarUrl().isEmpty() && ivAvatar != null) {
                                Glide.with(this)
                                    .load(bacSi.getAvatarUrl())
                                    .placeholder(R.drawable.ic_avatar)
                                    .error(R.drawable.ic_avatar)
                                    .circleCrop()
                                    .into(ivAvatar);
                            }
                        } else {
                            showError("Không tìm thấy thông tin bác sĩ!");
                        }
                    } else {
                        showError("Không tìm thấy thông tin bác sĩ!");
                    }
                    progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("MainBacSiActivity", "Lỗi tải thông tin: ", e);
                    showError("Lỗi tải thông tin: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
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
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }
        Intent intent = new Intent(this, QuanLyBenhAnBacSiActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleQuanLyLichLamViec() {
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }
        Intent intent = new Intent(this, QuanLyLichLamViecNewActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleQuanLyDonThuoc() {
        Toast.makeText(this, "Chức năng Quản Lý Đơn Thuốc đang phát triển!", Toast.LENGTH_SHORT).show();
    }

    private void handleXacNhanLichKham() {
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }
        Intent intent = new Intent(this, XacNhanLichKhamActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleQuanLyHoaDon() {
        Toast.makeText(this, "Chức năng Quản Lý Hóa Đơn đang phát triển!", Toast.LENGTH_SHORT).show();
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