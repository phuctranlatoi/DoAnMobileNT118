package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;

public class MainBenhNhanActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvHoTen, tvSoDienThoai, tvDiaChi;
    private CardView cardRegisterAppointment, cardManageProfile, cardViewMedicalRecord, cardConfirmMedication, cardViewInvoice;
    private FirestoreRepository repo;
    private String maTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_benhnhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvHoTen = findViewById(R.id.tvHoTen);
        tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        tvDiaChi = findViewById(R.id.tvDiaChi);

        // Initialize function cards
        cardRegisterAppointment = findViewById(R.id.cardRegisterAppointment);
        cardManageProfile = findViewById(R.id.cardManageProfile);
        cardViewMedicalRecord = findViewById(R.id.cardViewMedicalRecord);
        cardConfirmMedication = findViewById(R.id.cardConfirmMedication);
        cardViewInvoice = findViewById(R.id.cardViewInvoice);

        // Set up logout button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainBenhNhanActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Set up click listeners for function cards
        cardRegisterAppointment.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng Ký Lịch Khám", Toast.LENGTH_SHORT).show();
            // TODO: Implement RegisterAppointmentActivity
        });

        cardManageProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainBenhNhanActivity.this, QuanLyHoSoCaNhan.class);
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
        });

        cardViewMedicalRecord.setOnClickListener(v -> {
            Intent intent = new Intent(MainBenhNhanActivity.this, XembenhanActivity.class);
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
        });

        cardConfirmMedication.setOnClickListener(v -> {
            Toast.makeText(this, "Xác Nhận Dùng Thuốc", Toast.LENGTH_SHORT).show();
            // TODO: Implement ConfirmMedicationActivity
        });

        cardViewInvoice.setOnClickListener(v -> {
            Toast.makeText(this, "Xem Hóa Đơn", Toast.LENGTH_SHORT).show();
            // TODO: Implement ViewInvoiceActivity
        });

        // Load data
        loadUserInfo();
    }

    private void loadUserInfo() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Log.e("MainBenhNhanActivity", "maTaiKhoan is null or empty");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("MainBenhNhanActivity", "No BenhNhan found for maTaiKhoan: " + maTaiKhoan);
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                        tvHoTen.setText("Họ tên: N/A");
                        tvSoDienThoai.setText("Số điện thoại: N/A");
                        tvDiaChi.setText("Địa chỉ: N/A");
                        return;
                    }

                    try {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            Log.d("MainBenhNhanActivity", "BenhNhan data: " + benhNhan.toString());
                            tvHoTen.setText("Họ tên: " + (benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "N/A"));
                            tvSoDienThoai.setText("Số điện thoại: " + (benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "N/A"));
                            tvDiaChi.setText("Địa chỉ: " + (benhNhan.getDiaChi() != null ? benhNhan.getDiaChi() : "N/A"));
                        } else {
                            Log.e("MainBenhNhanActivity", "Failed to parse BenhNhan object");
                            Toast.makeText(this, "Lỗi: Dữ liệu bệnh nhân không hợp lệ", Toast.LENGTH_SHORT).show();
                            tvHoTen.setText("Họ tên: N/A");
                            tvSoDienThoai.setText("Số điện thoại: N/A");
                            tvDiaChi.setText("Địa chỉ: N/A");
                        }
                    } catch (Exception e) {
                        Log.e("MainBenhNhanActivity", "Error parsing BenhNhan: ", e);
                        Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    Log.e("MainBenhNhanActivity", "Firestore query error: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvHoTen.setText("Họ tên: N/A");
                    tvSoDienThoai.setText("Số điện thoại: N/A");
                    tvDiaChi.setText("Địa chỉ: N/A");
                });
    }
}