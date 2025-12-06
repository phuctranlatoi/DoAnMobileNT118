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
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.model.LichUongThuoc;
import com.example.doannt118.model.XacNhanUongThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Activity để bệnh nhân điểm danh uống thuốc hàng ngày
 * Hiển thị thuốc cần uống theo từng ca (sáng, trưa, chiều, tối)
 */
public class DiemDanhUongThuocActivity extends AppCompatActivity {
    
    private TextView tvNgayHomNay, tvEmpty;
    private RecyclerView rvCaSang, rvCaTrua, rvCaChieu, rvCaToi;
    private View layoutCaSang, layoutCaTrua, layoutCaChieu, layoutCaToi;
    private ProgressBar progressBar;
    
    private DiemDanhThuocAdapter adapterSang, adapterTrua, adapterChieu, adapterToi;
    private FirestoreRepository repository;
    private String maBenhNhan;
    private SimpleDateFormat dateFormat;
    private Date ngayHomNay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diem_danh_uong_thuoc);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        
        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        loadThuocHomNay();
    }

    private void initViews() {
        tvNgayHomNay = findViewById(R.id.tvNgayHomNay);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        
        layoutCaSang = findViewById(R.id.layoutCaSang);
        layoutCaTrua = findViewById(R.id.layoutCaTrua);
        layoutCaChieu = findViewById(R.id.layoutCaChieu);
        layoutCaToi = findViewById(R.id.layoutCaToi);
        
        rvCaSang = findViewById(R.id.rvCaSang);
        rvCaTrua = findViewById(R.id.rvCaTrua);
        rvCaChieu = findViewById(R.id.rvCaChieu);
        rvCaToi = findViewById(R.id.rvCaToi);
        
        repository = new FirestoreRepository();
        dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        
        // Lấy ngày hôm nay (chỉ lấy ngày, bỏ giờ)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ngayHomNay = cal.getTime();
        
        tvNgayHomNay.setText(dateFormat.format(ngayHomNay));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Điểm danh uống thuốc");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        adapterSang = new DiemDanhThuocAdapter(this, this::onDiemDanh);
        adapterTrua = new DiemDanhThuocAdapter(this, this::onDiemDanh);
        adapterChieu = new DiemDanhThuocAdapter(this, this::onDiemDanh);
        adapterToi = new DiemDanhThuocAdapter(this, this::onDiemDanh);
        
        rvCaSang.setLayoutManager(new LinearLayoutManager(this));
        rvCaSang.setAdapter(adapterSang);
        
        rvCaTrua.setLayoutManager(new LinearLayoutManager(this));
        rvCaTrua.setAdapter(adapterTrua);
        
        rvCaChieu.setLayoutManager(new LinearLayoutManager(this));
        rvCaChieu.setAdapter(adapterChieu);
        
        rvCaToi.setLayoutManager(new LinearLayoutManager(this));
        rvCaToi.setAdapter(adapterToi);
    }

    private void loadThuocHomNay() {
        showLoading(true);
        
        android.util.Log.d("DiemDanhThuoc", "Loading thuốc for maBenhNhan: " + maBenhNhan);
        
        // Load tất cả đơn thuốc đang active của bệnh nhân
        repository.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                android.util.Log.d("DiemDanhThuoc", "Found " + querySnapshot.size() + " đơn thuốc");
                
                List<String> danhSachMaDonThuoc = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    DonThuoc donThuoc = doc.toObject(DonThuoc.class);
                    if (donThuoc != null) {
                        // Nếu trangThai null hoặc "DANG_DUNG" thì hiển thị
                        String trangThai = donThuoc.getTrangThai();
                        android.util.Log.d("DiemDanhThuoc", "DonThuoc: " + donThuoc.getMaDonThuoc() + ", trangThai: " + trangThai);
                        
                        if (trangThai == null || "DANG_DUNG".equals(trangThai)) {
                            danhSachMaDonThuoc.add(donThuoc.getMaDonThuoc());
                        }
                    }
                }
                
                android.util.Log.d("DiemDanhThuoc", "Filtered to " + danhSachMaDonThuoc.size() + " đơn thuốc đang dùng");
                
                if (danhSachMaDonThuoc.isEmpty()) {
                    showLoading(false);
                    showEmpty(true);
                    Toast.makeText(this, "Không có đơn thuốc nào đang sử dụng", Toast.LENGTH_LONG).show();
                    return;
                }
                
                loadChiTietThuoc(danhSachMaDonThuoc);
            },
            e -> {
                showLoading(false);
                android.util.Log.e("DiemDanhThuoc", "Error loading DonThuoc", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadChiTietThuoc(List<String> danhSachMaDonThuoc) {
        List<ChiTietDonThuoc> tatCaThuoc = new ArrayList<>();
        final int[] count = {0};
        
        for (String maDonThuoc : danhSachMaDonThuoc) {
            repository.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
                querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
                        if (chiTiet != null) {
                            tatCaThuoc.add(chiTiet);
                        }
                    }
                    
                    count[0]++;
                    if (count[0] == danhSachMaDonThuoc.size()) {
                        phanLoaiThuocTheoCa(tatCaThuoc);
                    }
                },
                e -> {
                    count[0]++;
                    if (count[0] == danhSachMaDonThuoc.size()) {
                        phanLoaiThuocTheoCa(tatCaThuoc);
                    }
                }
            );
        }
    }

    private void phanLoaiThuocTheoCa(List<ChiTietDonThuoc> tatCaThuoc) {
        List<ChiTietDonThuoc> thuocSang = new ArrayList<>();
        List<ChiTietDonThuoc> thuocTrua = new ArrayList<>();
        List<ChiTietDonThuoc> thuocChieu = new ArrayList<>();
        
        for (ChiTietDonThuoc thuoc : tatCaThuoc) {
            // Kiểm tra xem có thông tin ca uống không
            boolean coThongTinCaUong = thuoc.isUongSang() || thuoc.isUongTrua() || 
                                       thuoc.isUongChieu() || thuoc.isUongToi();
            
            if (!coThongTinCaUong) {
                // Dữ liệu cũ không có thông tin ca uống
                // Mặc định hiển thị ở cả 3 ca (sáng, trưa, chiều)
                android.util.Log.d("DiemDanhThuoc", "Thuốc " + thuoc.getTenThuoc() + " không có thông tin ca uống, hiển thị ở tất cả ca");
                thuocSang.add(thuoc);
                thuocTrua.add(thuoc);
                thuocChieu.add(thuoc);
            } else {
                // Dữ liệu mới có thông tin ca uống
                if (thuoc.isUongSang()) thuocSang.add(thuoc);
                if (thuoc.isUongTrua()) thuocTrua.add(thuoc);
                if (thuoc.isUongChieu()) thuocChieu.add(thuoc);
                // Không xử lý ca tối
            }
        }
        
        android.util.Log.d("DiemDanhThuoc", "Phân loại: Sáng=" + thuocSang.size() + 
                          ", Trưa=" + thuocTrua.size() + ", Chiều=" + thuocChieu.size());
        
        // Hiển thị từng ca (chỉ sáng, trưa, chiều)
        adapterSang.setData(thuocSang);
        adapterTrua.setData(thuocTrua);
        adapterChieu.setData(thuocChieu);
        
        // Ẩn/hiện layout theo ca có thuốc
        layoutCaSang.setVisibility(thuocSang.isEmpty() ? View.GONE : View.VISIBLE);
        layoutCaTrua.setVisibility(thuocTrua.isEmpty() ? View.GONE : View.VISIBLE);
        layoutCaChieu.setVisibility(thuocChieu.isEmpty() ? View.GONE : View.VISIBLE);
        layoutCaToi.setVisibility(View.GONE); // Luôn ẩn ca tối
        
        showLoading(false);
        showEmpty(tatCaThuoc.isEmpty());
    }

    private void onDiemDanh(ChiTietDonThuoc thuoc, String caUong) {
        // Lưu xác nhận uống thuốc
        String maXacNhan = "XN_" + UUID.randomUUID().toString();
        XacNhanUongThuoc xacNhan = new XacNhanUongThuoc();
        xacNhan.setMaXacNhan(maXacNhan);
        xacNhan.setMaChiTietDonThuoc(thuoc.getMaChiTiet());
        xacNhan.setMaBenhNhan(maBenhNhan);
        xacNhan.setDaUong(true);
        xacNhan.setThoiGianXacNhan(Timestamp.now());
        xacNhan.setGhiChu("Điểm danh ca " + caUong.toLowerCase());
        
        repository.addDocument("XacNhanUongThuoc", maXacNhan, xacNhan,
            aVoid -> {
                Toast.makeText(this, "Đã xác nhận uống " + thuoc.getTenThuoc(), Toast.LENGTH_SHORT).show();
            },
            e -> {
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
