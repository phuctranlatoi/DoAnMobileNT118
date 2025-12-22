package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class XacNhanUongThuocActivity extends AppCompatActivity {
    
    private static final String TAG = "XacNhanUongThuocActivity";
    
    private TextView tvCaUong, tvNgayUong;
    private RecyclerView rvThuocCanUong;
    private MaterialButton btnXacNhanTatCa, btnBoQua;
    private ProgressBar progressBar;
    
    private ThuocCanUongAdapter adapter;
    private FirestoreRepository repository;
    private String maLichUong;
    private String maBenhNhan;
    private String caUong;
    private String tenCa;
    private boolean fromNotification;
    private LichUongThuoc lichUongThuoc;
    private SimpleDateFormat dateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xac_nhan_uong_thuoc);

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        // Lấy dữ liệu từ intent
        maLichUong = getIntent().getStringExtra("maLichUong");
        maBenhNhan = getIntent().getStringExtra("maBenhNhan");
        caUong = getIntent().getStringExtra("caUong");
        tenCa = getIntent().getStringExtra("tenCa");
        fromNotification = getIntent().getBooleanExtra("fromNotification", false);
        
        Log.d(TAG, "onCreate: maBenhNhan=" + maBenhNhan + ", caUong=" + caUong + 
            ", fromNotification=" + fromNotification);
        
        if (fromNotification && maBenhNhan != null && caUong != null) {
            // Mở từ notification - load thuốc theo ca
            loadThuocTheoCa();
        } else if (maLichUong != null) {
            // Mở từ lịch uống thuốc cũ
            loadLichUongThuoc();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin", Toast.LENGTH_SHORT).show();
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
        
        btnXacNhanTatCa.setOnClickListener(v -> {
            if (fromNotification) {
                xacNhanCaTuNotification();
            } else {
                xacNhanTatCa();
            }
        });
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
    
    /**
     * Load thuốc theo ca khi mở từ notification
     */
    private void loadThuocTheoCa() {
        showLoading(true);
        
        // Hiển thị thông tin ca
        if (tenCa != null) {
            tvCaUong.setText(tenCa);
        } else {
            tvCaUong.setText(getTenCaFromMaCa(caUong));
        }
        tvNgayUong.setText(dateFormat.format(new Date()));
        
        // Load đơn thuốc đang dùng của bệnh nhân
        repository.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<String> maDonThuocList = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String trangThai = doc.getString("trangThai");
                    if (trangThai == null || "DANG_DUNG".equals(trangThai)) {
                        maDonThuocList.add(doc.getId());
                    }
                }
                
                if (maDonThuocList.isEmpty()) {
                    showLoading(false);
                    Toast.makeText(this, "Không có đơn thuốc đang dùng", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                // Load chi tiết thuốc cho ca này
                loadChiTietThuocTheoCa(maDonThuocList);
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private void loadChiTietThuocTheoCa(List<String> maDonThuocList) {
        List<ChiTietDonThuoc> allThuoc = new ArrayList<>();
        final int[] count = {0};
        
        for (String maDonThuoc : maDonThuocList) {
            repository.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
                querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
                        if (chiTiet != null && thuocThuocCa(chiTiet, caUong)) {
                            allThuoc.add(chiTiet);
                        }
                    }
                    
                    count[0]++;
                    if (count[0] == maDonThuocList.size()) {
                        adapter.setData(allThuoc);
                        showLoading(false);
                        
                        if (allThuoc.isEmpty()) {
                            Toast.makeText(this, "Không có thuốc cần uống trong ca này", 
                                Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                },
                e -> {
                    count[0]++;
                    if (count[0] == maDonThuocList.size()) {
                        adapter.setData(allThuoc);
                        showLoading(false);
                    }
                }
            );
        }
    }
    
    private boolean thuocThuocCa(ChiTietDonThuoc chiTiet, String ca) {
        switch (ca) {
            case "SANG": return chiTiet.isUongSang();
            case "TRUA": return chiTiet.isUongTrua();
            case "CHIEU": return chiTiet.isUongChieu() || chiTiet.isUongToi();
            default: return false;
        }
    }
    
    private String getTenCaFromMaCa(String maCa) {
        switch (maCa) {
            case "SANG": return "Ca Sáng";
            case "TRUA": return "Ca Trưa";
            case "CHIEU": return "Ca Chiều";
            default: return "Ca " + maCa;
        }
    }
    
    /**
     * Xác nhận uống thuốc ca từ notification
     */
    private void xacNhanCaTuNotification() {
        showLoading(true);
        
        // Tạo key xác nhận cho ca
        SimpleDateFormat keyDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String ngayHomNay = keyDateFormat.format(new Date());
        String keyXacNhan = "CA_" + caUong + "_" + maBenhNhan + "_" + ngayHomNay;
        
        // Kiểm tra đã xác nhận chưa
        repository.getCollection("XacNhanUongThuoc").document(keyXacNhan).get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    Boolean daUong = doc.getBoolean("daUong");
                    if (daUong != null && daUong) {
                        showLoading(false);
                        Toast.makeText(this, "Bạn đã xác nhận uống thuốc ca này rồi!", 
                            Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                
                // Lưu xác nhận
                luuXacNhanCa(keyXacNhan);
            })
            .addOnFailureListener(e -> {
                // Nếu chưa có document, tạo mới
                luuXacNhanCa(keyXacNhan);
            });
    }
    
    private void luuXacNhanCa(String keyXacNhan) {
        XacNhanUongThuoc xacNhan = new XacNhanUongThuoc();
        xacNhan.setMaXacNhan(keyXacNhan);
        xacNhan.setMaChiTietDonThuoc("CA_" + caUong);
        xacNhan.setMaBenhNhan(maBenhNhan);
        xacNhan.setDaUong(true);
        xacNhan.setThoiGianXacNhan(Timestamp.now());
        xacNhan.setGhiChu("Xác nhận từ thông báo nhắc nhở - " + getTenCaFromMaCa(caUong) + 
            " ngày " + dateFormat.format(new Date()));
        
        repository.addDocument("XacNhanUongThuoc", keyXacNhan, xacNhan,
            aVoid -> {
                showLoading(false);
                Toast.makeText(this, "✅ Đã xác nhận uống thuốc " + getTenCaFromMaCa(caUong), 
                    Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
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
