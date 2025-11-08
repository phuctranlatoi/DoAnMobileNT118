package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.UUID;

public class QuanLyHoSoCaNhan extends AppCompatActivity {

    private EditText etHoTen, etSoDienThoai, etDiaChi, etNgaySinh;
    private Button btnEdit, btnConfirm, btnCancel, btnBack;
    private TextView tvMessage;
    private LinearLayout editButtonLayout;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBenhNhan; // maProfile
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quanlyhosocanhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showError("Không tìm thấy thông tin tài khoản!");
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadBenhNhanData();
    }

    private void initViews() {
        etHoTen = findViewById(R.id.etHoTen);
        etSoDienThoai = findViewById(R.id.etSoDienThoai);
        etDiaChi = findViewById(R.id.etDiaChi);
        etNgaySinh = findViewById(R.id.etNgaySinh);
        btnEdit = findViewById(R.id.btnEdit);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        tvMessage = findViewById(R.id.tvMessage);
        editButtonLayout = findViewById(R.id.editButtonLayout);

        // Ẩn thông báo lỗi ban đầu
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);

        // Vô hiệu hóa input
        setEditMode(false);
    }

    private void setupClickListeners() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> toggleEditMode(true));
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> saveChanges());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> toggleEditMode(false));
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadBenhNhanData() {
        showLoading("Đang tải thông tin...");

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    hideLoading();
                    if (querySnapshot.isEmpty()) {
                        showError("Không tìm thấy thông tin bệnh nhân!");
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    BenhNhan benhNhan = doc.toObject(BenhNhan.class);
                    if (benhNhan == null) {
                        showError("Dữ liệu không hợp lệ!");
                        return;
                    }

                    maBenhNhan = benhNhan.getMaBenhNhan();
                    if (maBenhNhan == null || maBenhNhan.isEmpty()) {
                        showError("Mã bệnh nhân trống!");
                        return;
                    }

                    // Hiển thị dữ liệu
                    if (etHoTen != null) etHoTen.setText(safeString(benhNhan.getHoTen()));
                    if (etSoDienThoai != null) etSoDienThoai.setText(safeString(benhNhan.getSoDienThoai()));
                    if (etDiaChi != null) {
                        etDiaChi.setText(safeString(benhNhan.getDiaChi()));
                        etDiaChi.setVisibility(View.VISIBLE);
                    }
                    if (etNgaySinh != null) etNgaySinh.setText(safeString(benhNhan.getNgaySinh()));


                    logActivity("Xem hồ sơ cá nhân");
                },
                e -> {
                    hideLoading();
                    Log.e("QuanLyHoSo", "Lỗi Firestore: ", e);
                    showError("Lỗi kết nối: " + e.getMessage());
                });
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;
        setEditMode(enable);

        if (editButtonLayout != null) {
            editButtonLayout.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
        if (btnEdit != null) {
            btnEdit.setVisibility(enable ? View.GONE : View.VISIBLE);
        }

        // Nếu hủy → reload dữ liệu từ Firestore (an toàn nhất)
        if (!enable) {
            loadBenhNhanData();
        }
    }

    private void setEditMode(boolean enable) {
        if (etHoTen != null) etHoTen.setEnabled(enable);
        if (etSoDienThoai != null) etSoDienThoai.setEnabled(enable);
        if (etDiaChi != null) etDiaChi.setEnabled(enable);
        if (etNgaySinh != null) etNgaySinh.setEnabled(enable);
    }

    private void saveChanges() {
        String hoTen = safeTrim(etHoTen);
        String soDienThoai = safeTrim(etSoDienThoai);
        String diaChi = safeTrim(etDiaChi);
        String ngaySinh = safeTrim(etNgaySinh);


        if (hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty()|| ngaySinh.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!soDienThoai.matches("\\d{10,11}")) {
            showError("Số điện thoại phải 10-11 số!");
            return;
        }

        if (maBenhNhan == null) {
            showError("Lỗi: Không có mã bệnh nhân!");
            return;
        }

        showLoading("Đang lưu...");

        BenhNhan updated = new BenhNhan();
        updated.setMaBenhNhan(maBenhNhan);
        updated.setMaTaiKhoan(maTaiKhoan);
        updated.setHoTen(hoTen);
        updated.setSoDienThoai(soDienThoai);
        updated.setDiaChi(diaChi);

        repo.updateDocument("BenhNhan", maBenhNhan, updated,
                aVoid -> {
                    hideLoading();
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    logActivity("Cập nhật hồ sơ cá nhân");
                    toggleEditMode(false);
                },
                e -> {
                    hideLoading();
                    Log.e("QuanLyHoSo", "Lỗi cập nhật: ", e);
                    showError("Lỗi lưu: " + e.getMessage());
                });
    }

    // === HÀM HỖ TRỢ ===
    private String safeTrim(EditText et) {
        return et != null ? et.getText().toString().trim() : "";
    }

    private String safeString(String s) {
        return s != null ? s : "";
    }

    private void showError(String msg) {
        if (tvMessage != null) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showLoading(String msg) {
        if (tvMessage != null) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
            tvMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void hideLoading() {
        if (tvMessage != null && !tvMessage.getText().toString().contains("lỗi")) {
            tvMessage.setVisibility(View.GONE);
        }
    }

    private void logActivity(String tenHoatDong) {
        String maLichSu = UUID.randomUUID().toString();
        LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, tenHoatDong, new Date(), "Bệnh nhân: " + tenHoatDong);
        repo.logActivity(lichSu);
    }
}