package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvUserName, tvUserPhone, tvMenuHoSoTitle;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    private String maTaiKhoan;
    private String userType; // "benhnhan" hoặc "bacsi"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        userType = getIntent().getStringExtra("USER_TYPE"); // "benhnhan" hoặc "bacsi"

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadUserInfo();
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvMenuHoSoTitle = findViewById(R.id.tvMenuHoSoTitle);
        
        // Cập nhật text dựa vào loại người dùng
        if (tvMenuHoSoTitle != null) {
            if ("bacsi".equals(userType)) {
                tvMenuHoSoTitle.setText("Hồ sơ cá nhân");
            } else {
                tvMenuHoSoTitle.setText("Hồ sơ y tế");
            }
        }
    }

    private void setupClickListeners() {
        // Hồ sơ y tế / Hồ sơ cá nhân
        View menuHoSoYTe = findViewById(R.id.menuHoSoYTe);
        if (menuHoSoYTe != null) {
            menuHoSoYTe.setOnClickListener(v -> {
                Intent intent = new Intent(this, QuanLyHoSoCaNhan.class);
                intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
                intent.putExtra("USER_TYPE", userType);
                startActivity(intent);
            });
        }

        // Danh sách quan tâm
        View menuDanhSachQuanTam = findViewById(R.id.menuDanhSachQuanTam);
        if (menuDanhSachQuanTam != null) {
            menuDanhSachQuanTam.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Điều khoản
        View menuDieuKhoan = findViewById(R.id.menuDieuKhoan);
        if (menuDieuKhoan != null) {
            menuDieuKhoan.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Liên hệ
        View menuLienHe = findViewById(R.id.menuLienHe);
        if (menuLienHe != null) {
            menuLienHe.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Cài đặt
        View menuCaiDat = findViewById(R.id.menuCaiDat);
        if (menuCaiDat != null) {
            menuCaiDat.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingActivity.class);
                intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
                startActivity(intent);
            });
        }

        // Đăng xuất
        View menuDangXuat = findViewById(R.id.menuDangXuat);
        if (menuDangXuat != null) {
            menuDangXuat.setOnClickListener(v -> handleDangXuat());
        }
    }

    private void loadUserInfo() {
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";
        
        repo.getByField(collection, "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        if ("bacsi".equals(userType)) {
                            // Load thông tin bác sĩ
                            var bacSi = querySnapshot.getDocuments().get(0);
                            String hoTen = bacSi.getString("hoTen");
                            String soDienThoai = bacSi.getString("soDienThoai");
                            
                            if (tvUserName != null) {
                                tvUserName.setText(hoTen != null ? hoTen : "Bác sĩ");
                            }
                            if (tvUserPhone != null) {
                                tvUserPhone.setText(soDienThoai != null ? soDienThoai : "");
                            }
                        } else {
                            // Load thông tin bệnh nhân
                            BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                            if (benhNhan != null) {
                                if (tvUserName != null) {
                                    tvUserName.setText(benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "Người dùng");
                                }
                                if (tvUserPhone != null) {
                                    tvUserPhone.setText(benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "");
                                }
                            }
                        }
                    }
                },
                e -> {
                    Log.e("ProfileActivity", "Lỗi tải thông tin: ", e);
                });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            // Set menu dựa vào loại người dùng
            if ("bacsi".equals(userType)) {
                bottomNavigation.inflateMenu(R.menu.bottom_nav_doctor);
            } else {
                bottomNavigation.inflateMenu(R.menu.bottom_nav_patient);
            }
            
            // Set selected item to profile
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Quay về trang chủ
                    finish();
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    Toast.makeText(this, "Chức năng Tin nhắn đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_add) {
                    Toast.makeText(this, "Chức năng Đặt lịch đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_appointments) {
                    Toast.makeText(this, "Chức năng Xem lịch khám đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Toast.makeText(this, "Chức năng Thông báo đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Đã ở trang profile
                    return true;
                }
                return false;
            });
        }
    }

    private void handleDangXuat() {
        // Xóa session
        sessionManager.logout();
        // Đăng xuất Firebase
        auth.signOut();
        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
