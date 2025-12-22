package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.doannt118.R;
import com.example.doannt118.model.ThongBao;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChiTietThongBaoActivity extends AppCompatActivity {
    private TextView tvLoaiThongBao, tvThoiGian, tvTieuDe, tvNoiDung, tvTenBacSi;
    private ImageView ivLoaiThongBao;
    private MaterialCardView cardBacSi;
    private FirestoreRepository repository;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_thong_bao);

        initViews();
        setupToolbar();
        
        String maThongBao = getIntent().getStringExtra("maThongBao");
        if (maThongBao != null) {
            loadThongBao(maThongBao);
        } else {
            Toast.makeText(this, "Không tìm thấy thông báo", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvLoaiThongBao = findViewById(R.id.tvLoaiThongBao);
        tvThoiGian = findViewById(R.id.tvThoiGian);
        tvTieuDe = findViewById(R.id.tvTieuDe);
        tvNoiDung = findViewById(R.id.tvNoiDung);
        tvTenBacSi = findViewById(R.id.tvTenBacSi);
        ivLoaiThongBao = findViewById(R.id.ivLoaiThongBao);
        cardBacSi = findViewById(R.id.cardBacSi);
        
        repository = new FirestoreRepository();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadThongBao(String maThongBao) {
        repository.getByField("ThongBao", "maThongBao", maThongBao,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    ThongBao thongBao = doc.toObject(ThongBao.class);
                    if (thongBao != null) {
                        displayThongBao(thongBao);
                        markAsRead(maThongBao);
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy thông báo", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        );
    }

    private void displayThongBao(ThongBao thongBao) {
        tvTieuDe.setText(thongBao.getTieuDe());
        tvNoiDung.setText(thongBao.getNoiDung());
        
        if (thongBao.getThoiGianGui() != null) {
            tvThoiGian.setText(dateFormat.format(thongBao.getThoiGianGui().toDate()));
        }
        
        String loaiThongBao = thongBao.getLoaiThongBao();
        if (loaiThongBao != null) {
            switch (loaiThongBao) {
                case "LICH_HEN":
                    tvLoaiThongBao.setText("Lịch hẹn");
                    break;
                case "NHAC_THUOC":
                    tvLoaiThongBao.setText("Nhắc uống thuốc");
                    // Mở màn hình xác nhận uống thuốc
                    openXacNhanUongThuoc(thongBao);
                    return; // Không hiển thị chi tiết, chuyển sang màn hình xác nhận
                default:
                    tvLoaiThongBao.setText("Thông báo chung");
                    break;
            }
        }
        
        if (thongBao.getMaBacSi() != null && !thongBao.getMaBacSi().isEmpty()) {
            cardBacSi.setVisibility(View.VISIBLE);
            loadBacSiInfo(thongBao.getMaBacSi());
        }
    }
    
    private void openXacNhanUongThuoc(ThongBao thongBao) {
        // Lấy thông tin ca uống từ maThongBao
        // Format: NHAC_THUOC_[CA]_[maBenhNhan]_[timestamp]
        String maThongBao = thongBao.getMaThongBao();
        String caUong = "SANG"; // Mặc định
        String tenCa = "Ca Sáng";
        
        if (maThongBao != null && maThongBao.startsWith("NHAC_THUOC_")) {
            String[] parts = maThongBao.split("_");
            if (parts.length >= 3) {
                caUong = parts[2]; // SANG, TRUA, CHIEU
                switch (caUong) {
                    case "SANG": tenCa = "Ca Sáng"; break;
                    case "TRUA": tenCa = "Ca Trưa"; break;
                    case "CHIEU": tenCa = "Ca Chiều"; break;
                }
            }
        }
        
        android.content.Intent intent = new android.content.Intent(this, XacNhanUongThuocActivity.class);
        intent.putExtra("maBenhNhan", thongBao.getMaBenhNhan());
        intent.putExtra("caUong", caUong);
        intent.putExtra("tenCa", tenCa);
        intent.putExtra("fromNotification", true);
        startActivity(intent);
        
        // Đánh dấu đã đọc
        markAsRead(maThongBao);
        
        // Đóng activity này
        finish();
    }

    private void loadBacSiInfo(String maBacSi) {
        repository.getByField("BacSi", "maBacSi", maBacSi,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String hoTen = doc.getString("hoTen");
                    tvTenBacSi.setText("BS. " + (hoTen != null ? hoTen : "Không rõ"));
                }
            },
            e -> {}
        );
    }

    private void markAsRead(String maThongBao) {
        repository.updateDocumentFields("ThongBao", maThongBao,
            java.util.Map.of("daDoc", true),
            aVoid -> {},
            e -> {}
        );
    }
}
