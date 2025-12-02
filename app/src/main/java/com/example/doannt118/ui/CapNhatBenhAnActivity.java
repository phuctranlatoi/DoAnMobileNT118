package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.HashMap;
import java.util.Map;

public class CapNhatBenhAnActivity extends AppCompatActivity {
    private TextInputEditText edtChanDoan, edtGhiChu;
    private MaterialButton btnCapNhat, btnKeDonThuoc;
    private ProgressBar progressBar;
    
    private FirestoreRepository repository;
    private String maBenhAn;
    private BenhAn benhAn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cap_nhat_benh_an);

        initViews();
        setupToolbar();
        
        maBenhAn = getIntent().getStringExtra("maBenhAn");
        if (maBenhAn != null) {
            loadBenhAn();
        } else {
            Toast.makeText(this, "Không tìm thấy mã bệnh án", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        edtChanDoan = findViewById(R.id.edtChanDoan);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        btnCapNhat = findViewById(R.id.btnCapNhat);
        btnKeDonThuoc = findViewById(R.id.btnKeDonThuoc);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
        
        btnCapNhat.setOnClickListener(v -> capNhatBenhAn());
        btnKeDonThuoc.setOnClickListener(v -> keDonThuoc());
    }
    
    private void keDonThuoc() {
        android.content.Intent intent = new android.content.Intent(this, KeDonThuocActivity.class);
        intent.putExtra("maBenhAn", maBenhAn);
        startActivity(intent);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadBenhAn() {
        showLoading(true);
        
        repository.getByField("BenhAn", "maBenhAn", maBenhAn,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    benhAn = doc.toObject(BenhAn.class);
                    if (benhAn != null) {
                        displayBenhAn();
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                }
                showLoading(false);
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void displayBenhAn() {
        if (benhAn.getChanDoan() != null) {
            edtChanDoan.setText(benhAn.getChanDoan());
        }
        if (benhAn.getGhiChu() != null) {
            edtGhiChu.setText(benhAn.getGhiChu());
        }
    }

    private void capNhatBenhAn() {
        String chanDoan = edtChanDoan.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();
        
        if (chanDoan.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("chanDoan", chanDoan);
        updates.put("ghiChu", ghiChu);
        updates.put("ngayKham", Timestamp.now());
        
        repository.updateDocumentFields("BenhAn", maBenhAn, updates,
            aVoid -> {
                showLoading(false);
                Toast.makeText(this, "Đã cập nhật bệnh án", Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnCapNhat.setEnabled(!show);
    }
}
