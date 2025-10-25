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
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.QuerySnapshot;

public class QuanLyHoSoCaNhan extends AppCompatActivity {

    private EditText etHoTen, etSoDienThoai, etDiaChi;
    private Button btnEdit, btnConfirm, btnCancel, btnBack;
    private TextView tvMessage;
    private LinearLayout editButtonLayout;
    private FirestoreRepository repo;
    private String maTaiKhoan, vaiTro;
    private String maProfile;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quanlyhosocanhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        vaiTro = getIntent().getStringExtra("VAI_TRO");

        // Ánh xạ views với kiểm tra null
        etHoTen = findViewById(R.id.etHoTen);
        etSoDienThoai = findViewById(R.id.etSoDienThoai);
        etDiaChi = findViewById(R.id.etDiaChi);
        btnEdit = findViewById(R.id.btnEdit);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        tvMessage = findViewById(R.id.tvMessage);
        editButtonLayout = findViewById(R.id.editButtonLayout);

        // Kiểm tra view null
        if (etHoTen == null || etSoDienThoai == null || btnEdit == null) {
            Log.e("QuanLyHoSoCaNhanActivity", "Một hoặc nhiều view không tìm thấy trong layout!");
            Toast.makeText(this, "Lỗi giao diện, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etHoTen.setEnabled(false);
        etSoDienThoai.setEnabled(false);
        if (etDiaChi != null) {
            etDiaChi.setEnabled(false);
        }

        btnEdit.setOnClickListener(v -> toggleEditMode());
        if (btnConfirm != null) btnConfirm.setOnClickListener(v -> saveChanges());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> toggleEditMode());
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        loadProfileData();
    }

    private void loadProfileData() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty() || vaiTro == null) {
            if (tvMessage != null) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("Thông tin tài khoản không hợp lệ!");
            }
            return;
        }

        String collection = vaiTro.equals("Bệnh nhân") ? "BenhNhan" : "BacSi";
        repo.getByField(collection, "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        if (tvMessage != null) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("Không tìm thấy thông tin!");
                        }
                        return;
                    }

                    try {
                        if (vaiTro.equals("Bệnh nhân")) {
                            BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                            if (benhNhan != null) {
                                maProfile = benhNhan.getMaBenhNhan();
                                if (etHoTen != null) etHoTen.setText(benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "");
                                if (etSoDienThoai != null) etSoDienThoai.setText(benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "");
                                if (etDiaChi != null) {
                                    etDiaChi.setText(benhNhan.getDiaChi() != null ? benhNhan.getDiaChi() : "");
                                    etDiaChi.setVisibility(View.VISIBLE); // Hiển thị địa chỉ cho bệnh nhân
                                }
                            }
                        } else if (vaiTro.equals("Bác sĩ")) {
                            BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                            if (bacSi != null) {
                                maProfile = bacSi.getMaBacSi();
                                if (etHoTen != null) etHoTen.setText(bacSi.getHoTen() != null ? bacSi.getHoTen() : "");
                                if (etSoDienThoai != null) etSoDienThoai.setText(bacSi.getSoDienThoai() != null ? bacSi.getSoDienThoai() : "");
                                if (etDiaChi != null) {
                                    etDiaChi.setVisibility(View.GONE); // Ẩn địa chỉ cho bác sĩ
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("QuanLyHoSoCaNhanActivity", "Lỗi tải dữ liệu: ", e);
                        if (tvMessage != null) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("Lỗi: " + e.getMessage());
                        }
                    }
                },
                e -> {
                    Log.e("QuanLyHoSoCaNhanActivity", "Lỗi tải hồ sơ: ", e);
                    if (tvMessage != null) {
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Lỗi tải hồ sơ: " + e.getMessage());
                    }
                });
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        if (etHoTen != null) etHoTen.setEnabled(isEditing);
        if (etSoDienThoai != null) etSoDienThoai.setEnabled(isEditing);
        if (etDiaChi != null) etDiaChi.setEnabled(isEditing && vaiTro.equals("Bệnh nhân"));
        if (editButtonLayout != null) editButtonLayout.setVisibility(isEditing ? View.VISIBLE : View.GONE);
        if (btnEdit != null) btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        if (!isEditing) {
            loadProfileData(); // Reload to discard changes
        }
    }

    private void saveChanges() {
        String hoTen = (etHoTen != null) ? etHoTen.getText().toString().trim() : "";
        String soDienThoai = (etSoDienThoai != null) ? etSoDienThoai.getText().toString().trim() : "";
        String diaChi = (etDiaChi != null && vaiTro.equals("Bệnh nhân")) ? etDiaChi.getText().toString().trim() : null;

        if (hoTen.isEmpty() || soDienThoai.isEmpty() || (vaiTro.equals("Bệnh nhân") && diaChi == null)) {
            if (tvMessage != null) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("Vui lòng điền đầy đủ thông tin!");
            }
            return;
        }

        if (!soDienThoai.matches("\\d{10,11}")) {
            if (tvMessage != null) {
                tvMessage.setVisibility(View.VISIBLE);
                tvMessage.setText("Số điện thoại phải có 10 hoặc 11 chữ số!");
            }
            return;
        }

        String collection = vaiTro.equals("Bệnh nhân") ? "BenhNhan" : "BacSi";
        Object updatedProfile;
        if (vaiTro.equals("Bệnh nhân")) {
            BenhNhan benhNhan = new BenhNhan();
            benhNhan.setMaBenhNhan(maProfile);
            benhNhan.setMaTaiKhoan(maTaiKhoan);
            benhNhan.setHoTen(hoTen);
            benhNhan.setSoDienThoai(soDienThoai);
            benhNhan.setDiaChi(diaChi);
            updatedProfile = benhNhan;
        } else {
            BacSi bacSi = new BacSi();
            bacSi.setMaBacSi(maProfile);
            bacSi.setMaTaiKhoan(maTaiKhoan);
            bacSi.setHoTen(hoTen);
            bacSi.setSoDienThoai(soDienThoai);
            updatedProfile = bacSi;
        }

        repo.updateDocument(collection, maProfile, updatedProfile,
                aVoid -> {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    toggleEditMode();
                },
                e -> {
                    Log.e("QuanLyHoSoCaNhanActivity", "Lỗi cập nhật hồ sơ: ", e);
                    if (tvMessage != null) {
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Lỗi cập nhật: " + e.getMessage());
                    }
                });
    }
}