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
        
        // Thêm menu tạo dữ liệu test
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Tạo dữ liệu test")) {
                createTestData();
                Toast.makeText(this, "Đang tạo dữ liệu test...", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        // Thêm menu item
        toolbar.getMenu().add("Tạo dữ liệu test");
    }

    private void setupRecyclerView() {
        adapter = new LichSuUongThuocAdapter(this, () -> {
            // Reload data khi có thay đổi
            if (maBenhNhan != null) {
                loadLichSuUongThuoc();
            }
        });
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
                
                // Sắp xếp: Chờ xác nhận lên đầu, sau đó theo ngày giảm dần
                list.sort((a, b) -> {
                    // Chờ xác nhận lên đầu
                    if ("CHO_XAC_NHAN".equals(a.getTrangThai()) && !"CHO_XAC_NHAN".equals(b.getTrangThai())) {
                        return -1;
                    }
                    if (!"CHO_XAC_NHAN".equals(a.getTrangThai()) && "CHO_XAC_NHAN".equals(b.getTrangThai())) {
                        return 1;
                    }
                    
                    // Cùng trạng thái thì sắp xếp theo ngày giảm dần
                    if (a.getNgayUong() != null && b.getNgayUong() != null) {
                        return b.getNgayUong().compareTo(a.getNgayUong());
                    }
                    return 0;
                });
                
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
            
            // Hiệu ứng cập nhật số liệu
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, tiLe);
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                tvTiLeTuanThu.setText(animatedValue + "%");
            });
            animator.start();
            
            tvDaUong.setText("✅ Đã uống: " + daUong + "/" + total + " lần");
            tvBoQua.setText("⏭️ Bỏ qua: " + boQua + " lần");
            
            // Thay đổi màu sắc dựa trên tỷ lệ tuân thủ
            if (tiLe >= 80) {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#27AE60"));
            } else if (tiLe >= 60) {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#F39C12"));
            } else {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#E74C3C"));
            }
        } else {
            tvTiLeTuanThu.setText("0%");
            tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#95A5A6"));
            tvDaUong.setText("✅ Đã uống: 0/0 lần");
            tvBoQua.setText("⏭️ Bỏ qua: 0 lần");
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvLichSu.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data khi quay lại activity với hiệu ứng
        if (maBenhNhan != null) {
            // Thêm delay nhỏ để tạo hiệu ứng mượt mà
            new android.os.Handler().postDelayed(() -> {
                loadLichSuUongThuoc();
            }, 300);
        }
    }
    
    // Method để tạo dữ liệu test (gọi 1 lần để tạo data)
    private void createTestData() {
        if (maBenhNhan == null) return;
        
        // Tạo 3 lịch uống thuốc test
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        for (int i = 0; i < 3; i++) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, -i);
            String maLichUong = "LU" + System.currentTimeMillis() + "_" + i;
            
            LichUongThuoc lich = new LichUongThuoc(
                maLichUong,
                "DT001", // maDonThuoc
                maBenhNhan,
                cal.getTime(),
                i == 0 ? "SANG" : i == 1 ? "TRUA" : "TOI"
            );
            
            repository.addDocument("LichUongThuoc", maLichUong, lich,
                aVoid -> android.util.Log.d("LichSuUongThuocActivity", "Created test data: " + maLichUong),
                e -> android.util.Log.e("LichSuUongThuocActivity", "Failed to create test data", e)
            );
        }
        
        // Reload sau khi tạo
        new android.os.Handler().postDelayed(() -> loadLichSuUongThuoc(), 2000);
    }
}
