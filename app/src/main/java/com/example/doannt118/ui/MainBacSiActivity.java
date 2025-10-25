package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainBacSiActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvUserName;
    private RecyclerView rvAppointments;
    private Button btnLogout;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBacSi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bacsi);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvUserName = findViewById(R.id.tvUserName);
        rvAppointments = findViewById(R.id.rvAppointments);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        btnLogout.setOnClickListener(v -> handleDangXuat());

        // Set click listeners for function cards
        findViewById(R.id.cardManageMedicalRecord).setOnClickListener(v -> handleQuanLyBenhAn());
        findViewById(R.id.cardManageSchedule).setOnClickListener(v -> handleQuanLyLichLamViec());
        findViewById(R.id.cardManagePrescription).setOnClickListener(v -> handleQuanLyDonThuoc());
        findViewById(R.id.cardConfirmAppointment).setOnClickListener(v -> handleXacNhanLichKham());
        findViewById(R.id.cardManageInvoice).setOnClickListener(v -> handleQuanLyHoaDon());

        // Start with ProgressBar visible
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        loadUserInfo();
    }

    private void loadUserInfo() {
        if (maTaiKhoan == null) {
            showError("Mã tài khoản không hợp lệ!");
            finish();
            return;
        }

        repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSi != null) {
                            tvUserName.setText(bacSi.getHoTen());
                            maBacSi = bacSi.getMaBacSi();
                            loadAppointments(); // Load appointments only after maBacSi is set
                        } else {
                            showError("Không tìm thấy thông tin bác sĩ!");
                        }
                    } else {
                        showError("Không tìm thấy thông tin bác sĩ!");
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("MainBacSiActivity", "Lỗi tải thông tin: ", e);
                    showError("Lỗi tải thông tin: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    private void loadAppointments() {
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        repo.getByField("LichKham", "maBacSi", maBacSi,
                querySnapshot -> {
                    List<LichKham> list = new ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        LichKham lichKham = doc.toObject(LichKham.class);
                        if (lichKham != null) {
                            list.add(lichKham);
                        }
                    }
                    if (list.isEmpty()) {
                        showError("Không có lịch khám!");
                    } else {
                        // Assume LichKhamAdapter is created; replace with actual adapter
                        // rvAppointments.setAdapter(new LichKhamAdapter(list));
                        Toast.makeText(this, "Đã tải " + list.size() + " lịch khám!", Toast.LENGTH_SHORT).show();
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("MainBacSiActivity", "Lỗi tải lịch khám: ", e);
                    showError("Lỗi tải lịch khám: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    private void handleQuanLyHoSo() {
        Intent intent = new Intent(this, QuanLyHoSoCaNhan.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("VAI_TRO", "Bác sĩ");
        startActivity(intent);
    }

    private void handleQuanLyBenhAn() {
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }
        Intent intent = new Intent(this, QuanLyBenhAnActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi); // Pass maBacSi to QuanLyBenhAnActivity
        startActivity(intent);
    }

    private void handleQuanLyLichLamViec() {
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }
        Intent intent = new Intent(this, QuanLyLichLamViecActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        intent.putExtra("MA_BAC_SI", maBacSi);
        startActivity(intent);
    }

    private void handleQuanLyDonThuoc() {
        Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show();
        // Cần tạo QuanLyDonThuocActivity
    }

    private void handleXacNhanLichKham() {
        Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show();
        // Cần tạo XacNhanLichKhamActivity
    }

    private void handleQuanLyHoaDon() {
        Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show();
        // Cần tạo QuanLyHoaDonActivity
    }

    private void handleDangXuat() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (tvUserName != null) tvUserName.setText(""); // Clear username on error
    }
}