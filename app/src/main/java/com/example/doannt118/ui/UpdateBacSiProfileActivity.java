package com.example.doannt118.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

public class UpdateBacSiProfileActivity extends AppCompatActivity {
    private EditText etHoTen, etSoDienThoai, etBangCap, etHocVi, etChungChi;
    private Button btnLuu, btnQuayLai;
    private FirestoreRepository repo;
    private String maTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_update_bacsi_profile);

        etHoTen = findViewById(R.id.etHoTen);
        etSoDienThoai = findViewById(R.id.etSoDienThoai);
        etBangCap = findViewById(R.id.etBangCap);
        etHocVi = findViewById(R.id.etHocVi);
        etChungChi = findViewById(R.id.etChungChi);
        btnLuu = findViewById(R.id.btnLuu);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSi != null) {
                            etHoTen.setText(bacSi.getHoTen());
                            etSoDienThoai.setText(bacSi.getSoDienThoai());
                            etBangCap.setText(bacSi.getBangCap() != null ? bacSi.getBangCap() : "");
                            etHocVi.setText(bacSi.getHocVi() != null ? bacSi.getHocVi() : "");
                            etChungChi.setText(bacSi.getChungChiHanhNghe() != null ? String.join(", ", bacSi.getChungChiHanhNghe()) : "");
                        }
                    }
                },
                e -> Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        btnLuu.setOnClickListener(v -> {
            String hoTen = etHoTen.getText().toString().trim();
            String sdt = etSoDienThoai.getText().toString().trim();
            String bangCap = etBangCap.getText().toString().trim();
            String hocVi = etHocVi.getText().toString().trim();
            String chungChi = etChungChi.getText().toString().trim();

            if (hoTen.isEmpty() || sdt.isEmpty() || bangCap.isEmpty() || hocVi.isEmpty() || chungChi.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String maBacSi = querySnapshot.getDocuments().get(0).getId();
                            BacSi bacSi = new BacSi(maBacSi, maTaiKhoan, hoTen, sdt, bangCap, hocVi, Arrays.asList(chungChi.split(",\\s*")), "Chờ xác thực");
                            repo.updateDocument("BacSi", maBacSi, bacSi,
                                    v2 -> {
                                        String maLichSu = UUID.randomUUID().toString();
                                        LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, "Cập nhật hồ sơ", new Date(), "Cập nhật hồ sơ bác sĩ: " + hoTen);
                                        repo.logActivity(lichSu);
                                        Toast.makeText(this, "Cập nhật hồ sơ thành công! Chờ admin xác thực.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    },
                                    e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    },
                    e -> Toast.makeText(this, "Lỗi tìm bác sĩ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnQuayLai.setOnClickListener(v -> finish());
    }
}