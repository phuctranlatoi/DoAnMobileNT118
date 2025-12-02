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

        if (cardRegisterAppointment != null) {
            cardRegisterAppointment.setOnClickListener(v -> handleDangKyLichKham());
        }
        if (cardViewMedicalRecord != null) {
            cardViewMedicalRecord.setOnClickListener(v -> handleXemBenhAn());
        }
        if (cardConfirmMedication != null) {
            cardConfirmMedication.setOnClickListener(v -> handleXacNhanDungThuoc());
        }
        if (cardViewInvoice != null) {
            cardViewInvoice.setOnClickListener(v -> handleXemHoaDon());
        }
        if (cardChatbot != null) {
            cardChatbot.setOnClickListener(v -> handleChatbot());
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
                    handleDangKyLichKham();
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
        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            tvHoTen.setText(getSafeString(benhNhan.getHoTen())); // Chỉ hiển thị họ tên
                            maBenhNhan = benhNhan.getMaBenhNhan();
                            
                            // Load avatar nếu có
                            if (benhNhan.getAvatarUrl() != null && !benhNhan.getAvatarUrl().isEmpty()) {
                                Glide.with(this)
                                    .load(benhNhan.getAvatarUrl())
                                    .placeholder(R.drawable.ic_avatar)
                                    .error(R.drawable.ic_avatar)
                                    .circleCrop()
                                    .into(ivAvatar);
                            }
                        } else {
                            showError("Dữ liệu bệnh nhân không hợp lệ!");
                        }
                    } else {
                        showError("Không tìm thấy thông tin bệnh nhân!");
                    }
                    hideProgress();
                },
                e -> {
                    Log.e("MainBenhNhanActivity", "Lỗi tải thông tin: ", e);
                    showError("Lỗi tải thông tin: " + e.getMessage());
                    hideProgress();
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
        Intent intent = new Intent(this, DanhSachBacSiActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
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
        Toast.makeText(this, "Chức năng Xem lịch khám đang phát triển!", Toast.LENGTH_SHORT).show();
    }

    private void handleXemBenhAn() {
        logActivity("Xem bệnh án");
        startActivitySafe(XembenhanActivity.class);
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
        if (maBenhNhan == null) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ThongBaoActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleChatbot() {
        logActivity("Mở trợ lý ảo");
        if (maBenhNhan == null) {
            Toast.makeText(this, "Vui lòng đợi tải thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        startActivity(intent);
    }

    private void handleXemHoaDon() {
        logActivity("Xem hóa đơn");
        // Lấy mã bệnh nhân trước
        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            Intent intent = new Intent(this, DanhSachHoaDonActivity.class);
                            intent.putExtra("MA_BENH_NHAN", benhNhan.getMaBenhNhan());
                            startActivity(intent);
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
                    }
                },
                e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void handleDangXuat() {
        logActivity("Đăng xuất");
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
}