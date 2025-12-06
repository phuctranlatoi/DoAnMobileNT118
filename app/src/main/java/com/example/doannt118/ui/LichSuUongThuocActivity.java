package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichUongThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LichSuUongThuocActivity extends AppCompatActivity {
    private TextView tvTiLeTuanThu, tvDaUong, tvBoQua, tvEmpty;
    private RecyclerView rvLichSu;
    private ProgressBar progressBar;
    
    private LichSuUongThuocAdapter adapter;
    private FirestoreRepository repository;
    private String maBenhNhan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_uong_thuoc);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadBenhNhanInfo();
    }

    private void initViews() {
        tvTiLeTuanThu = findViewById(R.id.tvTiLeTuanThu);
        tvDaUong = findViewById(R.id.tvDaUong);
        tvBoQua = findViewById(R.id.tvBoQua);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvLichSu = findViewById(R.id.rvLichSu);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new LichSuUongThuocAdapter(this);
        rvLichSu.setLayoutManager(new LinearLayoutManager(this));
        rvLichSu.setAdapter(adapter);
    }

    private void loadBenhNhanInfo() {
        // Lấy maTaiKhoan từ Intent
        String maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showLoading(false);
            Toast.makeText(this, "Mã tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        repository.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    maBenhNhan = doc.getString("maBenhNhan");
                    loadLichSuUongThuoc();
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadLichSuUongThuoc() {
        repository.getByField("LichUongThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<LichUongThuoc> list = new ArrayList<>();
                int daUong = 0;
                int boQua = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    LichUongThuoc lich = doc.toObject(LichUongThuoc.class);
                    if (lich != null) {
                        list.add(lich);
                        if ("DA_UONG".equals(lich.getTrangThai())) {
                            daUong++;
                        } else if ("BO_QUA".equals(lich.getTrangThai())) {
                            boQua++;
                        }
                    }
                }
                
                adapter.setData(list);
                updateThongKe(daUong, boQua, list.size());
                showLoading(false);
                showEmpty(list.isEmpty());
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void updateThongKe(int daUong, int boQua, int total) {
        if (total > 0) {
            int tiLe = (int) ((daUong * 100.0) / total);
            tvTiLeTuanThu.setText(tiLe + "%");
            tvDaUong.setText("Đã uống: " + daUong + "/" + total + " lần");
            tvBoQua.setText("Bỏ qua: " + boQua + " lần");
        } else {
            tvTiLeTuanThu.setText("0%");
            tvDaUong.setText("Đã uống: 0/0 lần");
            tvBoQua.setText("Bỏ qua: 0 lần");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvLichSu.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
