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
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainBenhNhanActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvHoTen;
    private ImageView btnSettings; // Thay Button bằng ImageView cho nút cài đặt
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private String maTaiKhoan;
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
        btnSettings = findViewById(R.id.btnSettings); // Ánh xạ ImageView cho nút cài đặt
        progressBar = findViewById(R.id.progressBar);

        // Kiểm tra null
        if (toolbar == null || tvHoTen == null || btnSettings == null || progressBar == null) {
            showError("Lỗi khởi tạo giao diện!");
            finish();
            return;
        }

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);

        // Thiết lập RecyclerView
//        rvActivityHistory.setLayoutManager(new LinearLayoutManager(this));
//        historyAdapter = new ActivityHistoryAdapter(new ArrayList<>());
//        rvActivityHistory.setAdapter(historyAdapter);

        // Xử lý sự kiện cho các CardView chức năng
        CardView cardRegisterAppointment = findViewById(R.id.cardRegisterAppointment);
        CardView cardManageProfile = findViewById(R.id.cardManageProfile);
        CardView cardViewMedicalRecord = findViewById(R.id.cardViewMedicalRecord);
        CardView cardConfirmMedication = findViewById(R.id.cardConfirmMedication);
        CardView cardViewInvoice = findViewById(R.id.cardViewInvoice);

        if (cardRegisterAppointment != null) {
            cardRegisterAppointment.setOnClickListener(v -> handleDangKyLichKham());
        }
        if (cardManageProfile != null) {
            cardManageProfile.setOnClickListener(v -> handleQuanLyHoSo());
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

        // Xử lý sự kiện cho nút cài đặt
        btnSettings.setOnClickListener(v -> handleSettings());

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
        Toast.makeText(this, "Chức năng Quản Lý Đơn Thuốc đang phát triển!", Toast.LENGTH_SHORT).show();
    }

    private void handleQuanLyHoSo() {
        logActivity("Quản lý hồ sơ cá nhân");
        startActivitySafe(QuanLyHoSoCaNhan.class);
    }

    private void handleXemBenhAn() {
        logActivity("Xem bệnh án");
        startActivitySafe(XembenhanActivity.class);
    }

    private void handleXacNhanDungThuoc() {
        Toast.makeText(this, "Chức năng Quản Lý Đơn Thuốc đang phát triển!", Toast.LENGTH_SHORT).show();
    }

    private void handleXemHoaDon() {
        Toast.makeText(this, "Chức năng Quản Lý Đơn Thuốc đang phát triển!", Toast.LENGTH_SHORT).show();
    }

    private void handleSettings() {
        logActivity("Mở cài đặt");
        startActivitySafe(SettingActivity.class); // Giả sử có màn hình SettingsActivity
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