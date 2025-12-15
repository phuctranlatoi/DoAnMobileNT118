package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.DangKyNhanTin;
import com.example.doannt118.repository.FirestoreRepository;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.UUID;

public class ThongTinBacSiActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private CircleImageView ivAvatarBacSi;
    private TextView tvTenBacSi, tvChuyenKhoa, tvKinhNghiem, tvBangCap, tvNoiLamViec;
    private LinearLayout layoutGoiCoBan, layoutGoiNangCao, layoutGoiCaoCap;
    private RadioButton rbGoiCoBan, rbGoiNangCao, rbGoiCaoCap;
    private Button btnDangKyNhanTin;
    
    private FirestoreRepository repository;
    private BacSi bacSi;
    private String maBenhNhan;
    private String tenBenhNhan;
    private String goiDuocChon = "GOI_CO_BAN"; // Mặc định chọn gói cơ bản
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_tin_bac_si);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupListeners();
        loadThongTinBacSi();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivAvatarBacSi = findViewById(R.id.ivAvatarBacSi);
        tvTenBacSi = findViewById(R.id.tvTenBacSi);
        tvChuyenKhoa = findViewById(R.id.tvChuyenKhoa);
        tvKinhNghiem = findViewById(R.id.tvKinhNghiem);
        tvBangCap = findViewById(R.id.tvBangCap);
        tvNoiLamViec = findViewById(R.id.tvNoiLamViec);
        
        layoutGoiCoBan = findViewById(R.id.layoutGoiCoBan);
        layoutGoiNangCao = findViewById(R.id.layoutGoiNangCao);
        layoutGoiCaoCap = findViewById(R.id.layoutGoiCaoCap);
        
        rbGoiCoBan = findViewById(R.id.rbGoiCoBan);
        rbGoiNangCao = findViewById(R.id.rbGoiNangCao);
        rbGoiCaoCap = findViewById(R.id.rbGoiCaoCap);
        
        btnDangKyNhanTin = findViewById(R.id.btnDangKyNhanTin);
        
        repository = new FirestoreRepository();
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        String maBacSi = intent.getStringExtra("MA_BAC_SI");
        maBenhNhan = intent.getStringExtra("MA_BENH_NHAN");
        tenBenhNhan = intent.getStringExtra("TEN_BENH_NHAN");
        
        // Tạo đối tượng BacSi từ intent (có thể truyền toàn bộ object hoặc chỉ ID)
        bacSi = new BacSi();
        bacSi.setMaBacSi(maBacSi);
        bacSi.setHoTen(intent.getStringExtra("TEN_BAC_SI"));
        bacSi.setBangCap(intent.getStringExtra("CHUYEN_KHOA"));
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupListeners() {
        // Xử lý chọn gói
        layoutGoiCoBan.setOnClickListener(v -> chonGoi("GOI_CO_BAN", rbGoiCoBan));
        layoutGoiNangCao.setOnClickListener(v -> chonGoi("GOI_NANG_CAO", rbGoiNangCao));
        layoutGoiCaoCap.setOnClickListener(v -> chonGoi("GOI_CAO_CAP", rbGoiCaoCap));
        
        rbGoiCoBan.setOnClickListener(v -> chonGoi("GOI_CO_BAN", rbGoiCoBan));
        rbGoiNangCao.setOnClickListener(v -> chonGoi("GOI_NANG_CAO", rbGoiNangCao));
        rbGoiCaoCap.setOnClickListener(v -> chonGoi("GOI_CAO_CAP", rbGoiCaoCap));
        
        // Xử lý đăng ký
        btnDangKyNhanTin.setOnClickListener(v -> dangKyNhanTin());
    }
    
    private void loadThongTinBacSi() {
        if (bacSi.getMaBacSi() != null) {
            repository.getByField("BacSi", "maBacSi", bacSi.getMaBacSi(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSiData = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSiData != null) {
                            bacSi = bacSiData;
                            hienThiThongTinBacSi();
                        }
                    } else {
                        hienThiThongTinBacSi();
                    }
                },
                e -> {
                    Toast.makeText(this, "Lỗi tải thông tin bác sĩ: " + e.getMessage(), 
                                  Toast.LENGTH_SHORT).show();
                    // Hiển thị thông tin cơ bản từ intent
                    hienThiThongTinBacSi();
                }
            );
        } else {
            hienThiThongTinBacSi();
        }
    }
    
    private void hienThiThongTinBacSi() {
        if (bacSi != null) {
            tvTenBacSi.setText("BS. " + bacSi.getHoTen());
            
            String chuyenKhoa = bacSi.getBangCap();
            if (chuyenKhoa == null || chuyenKhoa.isEmpty()) {
                chuyenKhoa = bacSi.getHocVi();
            }
            if (chuyenKhoa == null || chuyenKhoa.isEmpty()) {
                chuyenKhoa = "Bác sĩ đa khoa";
            }
            tvChuyenKhoa.setText(chuyenKhoa);
            
            // Hiển thị thông tin chi tiết
            tvKinhNghiem.setText(bacSi.getNamKinhNghiem() > 0 ? bacSi.getNamKinhNghiem() + " năm" : "5+ năm");
            tvBangCap.setText(bacSi.getBangCap() != null ? bacSi.getBangCap() : "Thạc sĩ Y khoa");
            tvNoiLamViec.setText(bacSi.getDiaChi() != null ? bacSi.getDiaChi() : "Bệnh viện Đại học Y Dược");
            
            // Load avatar (có thể dùng Glide nếu có URL)
            ivAvatarBacSi.setImageResource(R.drawable.ic_doctor);
        }
    }
    
    private void chonGoi(String maGoi, RadioButton radioButton) {
        // Reset tất cả radio button
        rbGoiCoBan.setChecked(false);
        rbGoiNangCao.setChecked(false);
        rbGoiCaoCap.setChecked(false);
        
        // Reset background
        layoutGoiCoBan.setBackgroundResource(R.drawable.package_border);
        layoutGoiNangCao.setBackgroundResource(R.drawable.package_border);
        layoutGoiCaoCap.setBackgroundResource(R.drawable.package_border);
        
        // Chọn gói hiện tại
        radioButton.setChecked(true);
        goiDuocChon = maGoi;
        
        // Cập nhật background cho gói được chọn
        switch (maGoi) {
            case "GOI_CO_BAN":
                layoutGoiCoBan.setBackgroundResource(R.drawable.package_border_selected);
                break;
            case "GOI_NANG_CAO":
                layoutGoiNangCao.setBackgroundResource(R.drawable.package_border_selected);
                break;
            case "GOI_CAO_CAP":
                layoutGoiCaoCap.setBackgroundResource(R.drawable.package_border_selected);
                break;
        }
    }
    
    private void dangKyNhanTin() {
        if (maBenhNhan == null || bacSi.getMaBacSi() == null) {
            Toast.makeText(this, "Thiếu thông tin để đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo đăng ký nhắn tin
        String maDangKy = UUID.randomUUID().toString();
        double gia = getGiaGoi(goiDuocChon);
        
        DangKyNhanTin dangKy = new DangKyNhanTin(maDangKy, maBenhNhan, bacSi.getMaBacSi(), goiDuocChon, gia);
        
        // Lưu vào Firestore
        repository.addDocument("DangKyNhanTin", maDangKy, dangKy,
            aVoid -> {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng thanh toán để bắt đầu nhắn tin.", 
                              Toast.LENGTH_LONG).show();
                
                // Chuyển đến màn hình thanh toán
                Intent intent = new Intent(this, ThanhToanActivity.class);
                intent.putExtra("MA_DANG_KY", maDangKy);
                intent.putExtra("MA_BAC_SI", bacSi.getMaBacSi());
                intent.putExtra("TEN_BAC_SI", bacSi.getHoTen());
                intent.putExtra("MA_BENH_NHAN", maBenhNhan);
                intent.putExtra("TEN_BENH_NHAN", tenBenhNhan);
                intent.putExtra("GOI_DANG_KY", goiDuocChon);
                intent.putExtra("GIA_THANH_TOAN", gia);
                startActivity(intent);
                finish();
            },
            e -> {
                Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private double getGiaGoi(String maGoi) {
        switch (maGoi) {
            case "GOI_CO_BAN":
                return 99000;
            case "GOI_NANG_CAO":
                return 199000;
            case "GOI_CAO_CAP":
                return 299000;
            default:
                return 99000;
        }
    }
}