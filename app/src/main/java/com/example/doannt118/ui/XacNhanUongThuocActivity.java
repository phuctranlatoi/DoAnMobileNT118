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
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.model.LichUongThuoc;
import com.example.doannt118.model.XacNhanUongThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class XacNhanUongThuocActivity extends AppCompatActivity {
    private TextView tvCaUong, tvNgayUong;
    private RecyclerView rvThuocCanUong;
    private MaterialButton btnXacNhanTatCa, btnBoQua;
    private ProgressBar progressBar;
    
    private ThuocCanUongAdapter adapter;
    private FirestoreRepository repository;
    private String maLichUong;
    private String maBenhNhan;
    private LichUongThuoc lichUongThuoc;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_uong_thuoc);

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        maLichUong = getIntent().getStringExtra("maLichUong");
        maBenhNhan = getIntent().getStringExtra("maBenhNhan");
        
        if (maLichUong != null) {
            loadLichUongThuoc();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin lịch uống thuốc", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvCaUong = findViewById(R.id.tvCaUong);
        tvNgayUong = findViewById(R.id.tvNgayUong);
        rvThuocCanUong = findViewById(R.id.rvThuocCanUong);
        btnXacNhanTatCa = findViewById(R.id.btnXacNhanTatCa);
        btnBoQua = findViewById(R.id.btnBoQua);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        btnXacNhanTatCa.setOnClickListener(v -> xacNhanTatCa());
        btnBoQua.setOnClickListener(v -> boQuaLanNay());
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
        adapter = new ThuocCanUongAdapter(this);
        rvThuocCanUong.setLayoutManager(new LinearLayoutManager(this));
        rvThuocCanUong.setAdapter(adapter);
    }

    private void loadLichUongThuoc() {
        showLoading(true);
        
        repository.getByField("LichUongThuoc", "maLichUong", maLichUong,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    lichUongThuoc = doc.toObject(LichUongThuoc.class);
                    if (lichUongThuoc != null) {
                        displayLichInfo();
                        loadThuocCanUong();
                    }
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Không tìm thấy lịch uống thuốc", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void displayLichInfo() {
        String caUong = "Ca " + lichUongThuoc.getCaUong().toLowerCase();
        tvCaUong.setText(caUong);
        
        if (lichUongThuoc.getNgayUong() != null) {
            tvNgayUong.setText(dateFormat.format(lichUongThuoc.getNgayUong()));
        }
    }

    private void loadThuocCanUong() {
        repository.getByField("ChiTietDonThuoc", "maDonThuoc", lichUongThuoc.getMaDonThuoc(),
            querySnapshot -> {
                List<ChiTietDonThuoc> list = new ArrayList<>();
                String caUong = lichUongThuoc.getCaUong();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
                    if (chiTiet != null) {
                        boolean canUong = false;
                        if ("SANG".equals(caUong) && chiTiet.isUongSang()) canUong = true;
                        if ("TRUA".equals(caUong) && chiTiet.isUongTrua()) canUong = true;
                        if ("CHIEU".equals(caUong) && chiTiet.isUongChieu()) canUong = true;
                        if ("TOI".equals(caUong) && chiTiet.isUongToi()) canUong = true;
                        
                        if (canUong) {
                            list.add(chiTiet);
                        }
                    }
                }
                
                adapter.setData(list);
                showLoading(false);
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi tải danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void xacNhanTatCa() {
        showLoading(true);
        
        // Cập nhật trạng thái lịch uống thuốc
        repository.updateDocumentFields("LichUongThuoc", maLichUong,
            java.util.Map.of(
                "trangThai", "DA_UONG",
                "thoiGianXacNhan", Timestamp.now()
            ),
            aVoid -> {
                // Lưu xác nhận cho từng thuốc
                List<ChiTietDonThuoc> danhSachThuoc = adapter.getData();
                for (ChiTietDonThuoc thuoc : danhSachThuoc) {
                    luuXacNhanUongThuoc(thuoc.getMaChiTiet(), true);
                }
                
                showLoading(false);
                Toast.makeText(this, "Đã xác nhận uống thuốc", Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void boQuaLanNay() {
        showLoading(true);
        
        repository.updateDocumentFields("LichUongThuoc", maLichUong,
            java.util.Map.of(
                "trangThai", "BO_QUA",
                "thoiGianXacNhan", Timestamp.now()
            ),
            aVoid -> {
                showLoading(false);
                Toast.makeText(this, "Đã bỏ qua lần uống thuốc này", Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void luuXacNhanUongThuoc(String maChiTietDonThuoc, boolean daUong) {
        String maXacNhan = "XN_" + UUID.randomUUID().toString();
        XacNhanUongThuoc xacNhan = new XacNhanUongThuoc(
            maXacNhan, maLichUong, maChiTietDonThuoc, maBenhNhan, daUong
        );
        
        repository.addDocument("XacNhanUongThuoc", maXacNhan, xacNhan,
            aVoid -> {},
            e -> {}
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
