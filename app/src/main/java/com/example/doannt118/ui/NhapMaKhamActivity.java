package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.MaKhamBenh;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NhapMaKhamActivity extends AppCompatActivity {

    private static final String TAG = "NhapMaKham";
    
    private TextInputEditText edtMaKham;
    private Button btnTimKiem, btnXemHoSo;
    private MaterialCardView cardThongTinBenhNhan;
    private ImageView imgAvatar;
    private TextView tvTenBenhNhan, tvNgaySinh, tvSoDienThoai, tvNgayKham, tvMaKham;
    private Toolbar toolbar;
    
    private FirestoreRepository repo;
    private MaKhamBenh maKhamBenhHienTai;
    private BenhNhan benhNhanHienTai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhap_ma_kham);

        repo = new FirestoreRepository();
        initViews();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        edtMaKham = findViewById(R.id.edtMaKham);
        btnTimKiem = findViewById(R.id.btnTimKiem);
        btnXemHoSo = findViewById(R.id.btnXemHoSo);
        cardThongTinBenhNhan = findViewById(R.id.cardThongTinBenhNhan);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvTenBenhNhan = findViewById(R.id.tvTenBenhNhan);
        tvNgaySinh = findViewById(R.id.tvNgaySinh);
        tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        tvNgayKham = findViewById(R.id.tvNgayKham);
        tvMaKham = findViewById(R.id.tvMaKham);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        btnTimKiem.setOnClickListener(v -> timKiemMaKham());
        btnXemHoSo.setOnClickListener(v -> xemHoSoDayDu());
    }

    private void timKiemMaKham() {
        String maKham = edtMaKham.getText().toString().trim();
        
        if (maKham.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã khám", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (maKham.length() != 6) {
            Toast.makeText(this, "Mã khám phải có 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        btnTimKiem.setEnabled(false);
        btnTimKiem.setText("Đang tìm...");

        // Tìm kiếm mã khám trong Firestore
        repo.getByField("MaKhamBenh", "maKham", maKham,
            querySnapshot -> {
                btnTimKiem.setEnabled(true);
                btnTimKiem.setText("Tìm Kiếm");
                
                if (querySnapshot.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy mã khám này", Toast.LENGTH_SHORT).show();
                    cardThongTinBenhNhan.setVisibility(View.GONE);
                    return;
                }
                
                // Lấy mã khám đầu tiên
                maKhamBenhHienTai = querySnapshot.getDocuments().get(0).toObject(MaKhamBenh.class);
                
                if (maKhamBenhHienTai == null) {
                    Toast.makeText(this, "Lỗi đọc dữ liệu", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Kiểm tra trạng thái
                if ("HOAN_THANH".equals(maKhamBenhHienTai.getTrangThai())) {
                    Toast.makeText(this, "Mã khám này đã được sử dụng", Toast.LENGTH_SHORT).show();
                    cardThongTinBenhNhan.setVisibility(View.GONE);
                    return;
                }
                
                // Kiểm tra hết hạn
                if (maKhamBenhHienTai.getThoiGianHetHan() != null) {
                    long now = System.currentTimeMillis();
                    long hetHan = maKhamBenhHienTai.getThoiGianHetHan().toDate().getTime();
                    if (now > hetHan) {
                        Toast.makeText(this, "Mã khám đã hết hạn", Toast.LENGTH_SHORT).show();
                        cardThongTinBenhNhan.setVisibility(View.GONE);
                        return;
                    }
                }
                
                // Load thông tin bệnh nhân
                loadThongTinBenhNhan(maKhamBenhHienTai.getMaBenhNhan());
            },
            e -> {
                btnTimKiem.setEnabled(true);
                btnTimKiem.setText("Tìm Kiếm");
                Log.e(TAG, "Lỗi tìm kiếm", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loadThongTinBenhNhan(String maBenhNhan) {
        repo.getCollection("BenhNhan")
            .document(maBenhNhan)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                benhNhanHienTai = documentSnapshot.toObject(BenhNhan.class);
                if (benhNhanHienTai == null) {
                    Toast.makeText(this, "Lỗi đọc thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                hienThiThongTinBenhNhan();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi load bệnh nhân", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void hienThiThongTinBenhNhan() {
        cardThongTinBenhNhan.setVisibility(View.VISIBLE);
        
        // Hiển thị thông tin
        tvTenBenhNhan.setText(benhNhanHienTai.getHoTen());
        tvNgaySinh.setText(benhNhanHienTai.getNgaySinh());
        tvSoDienThoai.setText(benhNhanHienTai.getSoDienThoai());
        tvMaKham.setText(maKhamBenhHienTai.getMaKham());
        
        // Format ngày khám
        if (maKhamBenhHienTai.getNgayKham() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = maKhamBenhHienTai.getNgayKham().toDate();
            tvNgayKham.setText(sdf.format(date));
        }
        
        // Load avatar
        if (benhNhanHienTai.getAvatarUrl() != null && !benhNhanHienTai.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                .load(benhNhanHienTai.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .circleCrop()
                .into(imgAvatar);
        }
        
        Toast.makeText(this, "Tìm thấy bệnh nhân: " + benhNhanHienTai.getHoTen(), Toast.LENGTH_SHORT).show();
    }

    private void xemHoSoDayDu() {
        if (benhNhanHienTai == null || maKhamBenhHienTai == null) {
            Toast.makeText(this, "Vui lòng tìm kiếm mã khám trước", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, HoSoBenhNhanActivity.class);
        intent.putExtra("MA_BENH_NHAN", benhNhanHienTai.getMaBenhNhan());
        intent.putExtra("MA_MA_KHAM", maKhamBenhHienTai.getMaMaKham());
        intent.putExtra("MA_LICH_KHAM", maKhamBenhHienTai.getMaLichKham());
        startActivity(intent);
    }
}
