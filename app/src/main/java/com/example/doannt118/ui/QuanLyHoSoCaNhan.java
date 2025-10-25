package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.UUID;

public class QuanLyHoSoCaNhan extends AppCompatActivity {

    private EditText etHoTen, etSoDienThoai, etDiaChi;
    private Button btnEdit, btnConfirm, btnCancel, btnBack;
    private TextView tvMessage;
    private FirestoreRepository repo;
    private String maTaiKhoan, maBenhNhan;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quanlyhosocanhan); // Use the new XML layout

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // Ánh xạ views
        etHoTen = findViewById(R.id.etHoTen);
        etSoDienThoai = findViewById(R.id.etSoDienThoai);
        etDiaChi = findViewById(R.id.etDiaChi);
        btnEdit = findViewById(R.id.btnEdit);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        tvMessage = findViewById(R.id.tvMessage);

        // Disable editing fields initially
        etHoTen.setEnabled(false);
        etSoDienThoai.setEnabled(false);
        etDiaChi.setEnabled(false);

        // Load patient data
        loadPatientData();

        // Button listeners
        btnEdit.setOnClickListener(v -> toggleEditMode());
        btnConfirm.setOnClickListener(v -> saveChanges());
        btnCancel.setOnClickListener(v -> toggleEditMode());
        btnBack.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadPatientData() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Mã tài khoản không hợp lệ!");
            return;
        }

        repo.getByField("TaiKhoan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Không tìm thấy tài khoản!");
                        return;
                    }

                    try {
                        String vaiTro = querySnapshot.getDocuments().get(0).getString("vaiTro");
                        if ("Bệnh nhân".equals(vaiTro)) {
                            repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                                    querySnapshotBenhNhan -> {
                                        if (querySnapshotBenhNhan.isEmpty()) {
                                            tvMessage.setVisibility(View.VISIBLE);
                                            tvMessage.setText("Không tìm thấy bệnh nhân!");
                                            return;
                                        }

                                        BenhNhan benhNhan = querySnapshotBenhNhan.getDocuments().get(0).toObject(BenhNhan.class);
                                        if (benhNhan != null) {
                                            maBenhNhan = benhNhan.getMaBenhNhan();
                                            etHoTen.setText(benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "");
                                            etSoDienThoai.setText(benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "");
                                            etDiaChi.setText(benhNhan.getDiaChi() != null ? benhNhan.getDiaChi() : "");
                                        }
                                    },
                                    e -> {
                                        Log.e("QuanLyHoSoCaNhanActivity", "Lỗi tải thông tin bệnh nhân: ", e);
                                        tvMessage.setVisibility(View.VISIBLE);
                                        tvMessage.setText("Lỗi tải thông tin: " + e.getMessage());
                                    });
                        } else {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("Tài khoản không phải bệnh nhân!");
                        }
                    } catch (Exception e) {
                        Log.e("QuanLyHoSoCaNhanActivity", "Lỗi tải dữ liệu: ", e);
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Lỗi: " + e.getMessage());
                    }
                },
                e -> {
                    Log.e("QuanLyHoSoCaNhanActivity", "Lỗi tải tài khoản: ", e);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Lỗi tải tài khoản: " + e.getMessage());
                });
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        etHoTen.setEnabled(isEditing);
        etSoDienThoai.setEnabled(isEditing);
        etDiaChi.setEnabled(isEditing);
        findViewById(R.id.editButtonLayout).setVisibility(isEditing ? View.VISIBLE : View.GONE);
        btnEdit.setVisibility(isEditing ? View.GONE : View.VISIBLE);
        if (!isEditing) {
            loadPatientData(); // Reload to discard changes
        }
    }

    private void saveChanges() {
        String hoTen = etHoTen.getText().toString().trim();
        String soDienThoai = etSoDienThoai.getText().toString().trim();
        String diaChi = etDiaChi.getText().toString().trim();

        if (hoTen.isEmpty() || soDienThoai.isEmpty()) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Vui lòng nhập Họ tên và Số điện thoại!");
            return;
        }

        if (!soDienThoai.matches("\\d{10,11}")) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Số điện thoại phải có 10 hoặc 11 chữ số!");
            return;
        }

        BenhNhan updatedBenhNhan = new BenhNhan();
        updatedBenhNhan.setMaBenhNhan(maBenhNhan);
        updatedBenhNhan.setMaTaiKhoan(maTaiKhoan);
        updatedBenhNhan.setHoTen(hoTen);
        updatedBenhNhan.setSoDienThoai(soDienThoai);
        updatedBenhNhan.setDiaChi(diaChi);

        repo.updateDocument("BenhNhan", maBenhNhan, updatedBenhNhan,
                aVoid -> {
                    Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
                    toggleEditMode();
//                    logActivityHistory("CAPNHATHOSO", "Cập nhật hồ sơ cá nhân thành công");
                },
                e -> {
                    Log.e("QuanLyHoSoCaNhanActivity", "Lỗi cập nhật hồ sơ: ", e);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Lỗi cập nhật: " + e.getMessage());
                });
    }

//    private void logActivityHistory(String hanhDong, String moTa) {
//        repo.addDocument("LichSuHoatDong", UUID.randomUUID().toString(),
//                new LichSuHoatDong(maTaiKhoan, hanhDong, new Timestamp(new Date()), moTa),
//                aVoid -> Log.d("QuanLyHoSoCaNhanActivity", "Ghi lịch sử thành công"),
//                e -> Log.e("QuanLyHoSoCaNhanActivity", "Lỗi ghi lịch sử: ", e));
//    }
}