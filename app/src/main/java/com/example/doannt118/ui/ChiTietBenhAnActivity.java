package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ChiTietBenhAnActivity extends AppCompatActivity {
    private TextView tvMaBenhAn, tvNgayKham, tvBacSi, tvChanDoan, tvGhiChu;
    private ProgressBar progressBar;
    private FirestoreRepository repository;
    private String maBenhAn;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_benh_an);

        initViews();
        setupToolbar();
        
        maBenhAn = getIntent().getStringExtra("maBenhAn");
        if (maBenhAn != null) {
            loadBenhAnDetail();
        } else {
            Toast.makeText(this, "Không tìm thấy mã bệnh án", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvMaBenhAn = findViewById(R.id.tvMaBenhAn);
        tvNgayKham = findViewById(R.id.tvNgayKham);
        tvBacSi = findViewById(R.id.tvBacSi);
        tvChanDoan = findViewById(R.id.tvChanDoan);
        tvGhiChu = findViewById(R.id.tvGhiChu);
        progressBar = findViewById(R.id.progressBar);
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

    private void loadBenhAnDetail() {
        showLoading(true);
        
        repository.getByField("BenhAn", "maBenhAn", maBenhAn,
            querySnapshot -> {
                showLoading(false);
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    BenhAn benhAn = doc.toObject(BenhAn.class);
                    if (benhAn != null) {
                        displayBenhAnInfo(benhAn);
                        loadBacSiInfo(benhAn.getMaBacSi());
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void displayBenhAnInfo(BenhAn benhAn) {
        tvMaBenhAn.setText(benhAn.getMaBenhAn());
        
        if (benhAn.getNgayKham() != null) {
            tvNgayKham.setText(dateFormat.format(benhAn.getNgayKham().toDate()));
        }
        
        tvChanDoan.setText(benhAn.getChanDoan() != null ? benhAn.getChanDoan() : "Chưa có chẩn đoán");
        tvGhiChu.setText(benhAn.getGhiChu() != null ? benhAn.getGhiChu() : "Không có ghi chú");
    }

    private void loadBacSiInfo(String maBacSi) {
        if (maBacSi == null) {
            tvBacSi.setText("Chưa xác định");
            return;
        }
        
        repository.getByField("BacSi", "maBacSi", maBacSi,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    String hoTen = doc.getString("hoTen");
                    tvBacSi.setText("BS. " + (hoTen != null ? hoTen : maBacSi));
                } else {
                    tvBacSi.setText("Mã BS: " + maBacSi);
                }
            },
            e -> tvBacSi.setText("Mã BS: " + maBacSi)
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
