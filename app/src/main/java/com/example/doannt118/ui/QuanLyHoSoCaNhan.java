package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private ImageView btnEdit, btnBack, ivAvatar;
    private Button btnConfirm, btnCancel;
    private TextView tvMessage, tvChangeAvatar;
    private LinearLayout editButtonLayout;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBenhNhan; // maProfile hoặc maBacSi
    private String userType; // "benhnhan" hoặc "bacsi"
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quanlyhosocanhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        userType = getIntent().getStringExtra("USER_TYPE");
        
        // Mặc định là bệnh nhân nếu không có userType
        if (userType == null || userType.isEmpty()) {
            userType = "benhnhan";
        }

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showError("Không tìm thấy thông tin tài khoản!");
            finish();
            return;
        }

        initViews();
        setupClickListeners();
        loadUserData();
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
        ivAvatar = findViewById(R.id.ivAvatar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);

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

    private void loadUserData() {
        showLoading("Đang tải thông tin...");
        
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";
        String userLabel = "bacsi".equals(userType) ? "bác sĩ" : "bệnh nhân";

        repo.getByField(collection, "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    hideLoading();
                    if (querySnapshot.isEmpty()) {
                        showError("Không tìm thấy thông tin " + userLabel + "!");
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    
                    if ("bacsi".equals(userType)) {
                        // Load thông tin bác sĩ
                        String maBacSi = doc.getString("maBacSi");
                        String hoTen = doc.getString("hoTen");
                        String soDienThoai = doc.getString("soDienThoai");
                        String diaChi = doc.getString("diaChi");
                        String ngaySinh = doc.getString("ngaySinh");
                        
                        if (maBacSi == null || maBacSi.isEmpty()) {
                            showError("Mã bác sĩ trống!");
                            return;
                        }
                        
                        maBenhNhan = maBacSi; // Dùng chung biến để lưu ID
                        
                        // Hiển thị dữ liệu
                        if (etHoTen != null) etHoTen.setText(safeString(hoTen));
                        if (etSoDienThoai != null) etSoDienThoai.setText(safeString(soDienThoai));
                        if (etDiaChi != null) {
                            etDiaChi.setText(safeString(diaChi));
                            etDiaChi.setVisibility(View.VISIBLE);
                        }
                        if (etNgaySinh != null) etNgaySinh.setText(safeString(ngaySinh));
                    } else {
                        // Load thông tin bệnh nhân
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
                    }

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
        if (tvChangeAvatar != null) {
            tvChangeAvatar.setVisibility(enable ? View.VISIBLE : View.GONE);
        }

        // Nếu hủy → reload dữ liệu từ Firestore (an toàn nhất)
        if (!enable) {
            loadUserData();
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

        if (hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty() || ngaySinh.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!soDienThoai.matches("\\d{10,11}")) {
            showError("Số điện thoại phải 10-11 số!");
            return;
        }

        if (maBenhNhan == null) {
            showError("Lỗi: Không có mã người dùng!");
            return;
        }

        showLoading("Đang lưu...");
        
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";

        if ("bacsi".equals(userType)) {
            // Cập nhật thông tin bác sĩ
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("maBacSi", maBenhNhan);
            updates.put("maTaiKhoan", maTaiKhoan);
            updates.put("hoTen", hoTen);
            updates.put("soDienThoai", soDienThoai);
            updates.put("diaChi", diaChi);
            updates.put("ngaySinh", ngaySinh);
            
            repo.updateDocumentFields(collection, maBenhNhan, updates,
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
        } else {
            // Cập nhật thông tin bệnh nhân
            BenhNhan updated = new BenhNhan();
            updated.setMaBenhNhan(maBenhNhan);
            updated.setMaTaiKhoan(maTaiKhoan);
            updated.setHoTen(hoTen);
            updated.setSoDienThoai(soDienThoai);
            updated.setDiaChi(diaChi);
            updated.setNgaySinh(ngaySinh);

            repo.updateDocument(collection, maBenhNhan, updated,
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