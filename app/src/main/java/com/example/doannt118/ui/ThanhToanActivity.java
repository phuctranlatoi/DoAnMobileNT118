package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class ThanhToanActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextView tvTenBacSi, tvTenGoi, tvTongTien;
    private LinearLayout layoutViDienTu, layoutTheTinDung, layoutChuyenKhoan;
    private RadioButton rbViDienTu, rbTheTinDung, rbChuyenKhoan;
    private Button btnThanhToan;
    
    private FirestoreRepository repository;
    private String maDangKy;
    private String maBacSi;
    private String tenBacSi;
    private String maBenhNhan;
    private String tenBenhNhan;
    private String goiDangKy;
    private double giaThanhToan;
    private String phuongThucThanhToan = "Ví điện tử";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupListeners();
        hienThiThongTinDonHang();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTenBacSi = findViewById(R.id.tvTenBacSi);
        tvTenGoi = findViewById(R.id.tvTenGoi);
        tvTongTien = findViewById(R.id.tvTongTien);
        
        layoutViDienTu = findViewById(R.id.layoutViDienTu);
        layoutTheTinDung = findViewById(R.id.layoutTheTinDung);
        layoutChuyenKhoan = findViewById(R.id.layoutChuyenKhoan);
        
        rbViDienTu = findViewById(R.id.rbViDienTu);
        rbTheTinDung = findViewById(R.id.rbTheTinDung);
        rbChuyenKhoan = findViewById(R.id.rbChuyenKhoan);
        
        btnThanhToan = findViewById(R.id.btnThanhToan);
        
        repository = new FirestoreRepository();
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        maDangKy = intent.getStringExtra("MA_DANG_KY");
        maBacSi = intent.getStringExtra("MA_BAC_SI");
        tenBacSi = intent.getStringExtra("TEN_BAC_SI");
        maBenhNhan = intent.getStringExtra("MA_BENH_NHAN");
        tenBenhNhan = intent.getStringExtra("TEN_BENH_NHAN");
        goiDangKy = intent.getStringExtra("GOI_DANG_KY");
        giaThanhToan = intent.getDoubleExtra("GIA_THANH_TOAN", 0);
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
        // Xử lý chọn phương thức thanh toán
        layoutViDienTu.setOnClickListener(v -> chonPhuongThucThanhToan("Ví điện tử", rbViDienTu));
        layoutTheTinDung.setOnClickListener(v -> chonPhuongThucThanhToan("Thẻ tín dụng", rbTheTinDung));
        layoutChuyenKhoan.setOnClickListener(v -> chonPhuongThucThanhToan("Chuyển khoản", rbChuyenKhoan));
        
        rbViDienTu.setOnClickListener(v -> chonPhuongThucThanhToan("Ví điện tử", rbViDienTu));
        rbTheTinDung.setOnClickListener(v -> chonPhuongThucThanhToan("Thẻ tín dụng", rbTheTinDung));
        rbChuyenKhoan.setOnClickListener(v -> chonPhuongThucThanhToan("Chuyển khoản", rbChuyenKhoan));
        
        // Xử lý thanh toán
        btnThanhToan.setOnClickListener(v -> xuLyThanhToan());
    }
    
    private void hienThiThongTinDonHang() {
        tvTenBacSi.setText("BS. " + tenBacSi);
        tvTenGoi.setText(getTenGoi(goiDangKy));
        
        // Format tiền tệ
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTongTien.setText(formatter.format(giaThanhToan));
    }
    
    private String getTenGoi(String maGoi) {
        switch (maGoi) {
            case "GOI_CO_BAN":
                return "Gói Cơ Bản";
            case "GOI_NANG_CAO":
                return "Gói Nâng Cao";
            case "GOI_CAO_CAP":
                return "Gói Cao Cấp";
            default:
                return "Gói Cơ Bản";
        }
    }
    
    private void chonPhuongThucThanhToan(String phuongThuc, RadioButton radioButton) {
        // Reset tất cả radio button
        rbViDienTu.setChecked(false);
        rbTheTinDung.setChecked(false);
        rbChuyenKhoan.setChecked(false);
        
        // Reset background
        layoutViDienTu.setBackgroundResource(R.drawable.package_border);
        layoutTheTinDung.setBackgroundResource(R.drawable.package_border);
        layoutChuyenKhoan.setBackgroundResource(R.drawable.package_border);
        
        // Chọn phương thức hiện tại
        radioButton.setChecked(true);
        phuongThucThanhToan = phuongThuc;
        
        // Cập nhật background cho phương thức được chọn
        switch (phuongThuc) {
            case "Ví điện tử":
                layoutViDienTu.setBackgroundResource(R.drawable.package_border_selected);
                break;
            case "Thẻ tín dụng":
                layoutTheTinDung.setBackgroundResource(R.drawable.package_border_selected);
                break;
            case "Chuyển khoản":
                layoutChuyenKhoan.setBackgroundResource(R.drawable.package_border_selected);
                break;
        }
    }
    
    private void xuLyThanhToan() {
        if (maDangKy == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Chuyển đến màn hình thanh toán QR
        Intent intent = new Intent(this, ThanhToanQRActivity.class);
        intent.putExtra("MA_DANG_KY", maDangKy);
        intent.putExtra("MA_BAC_SI", maBacSi);
        intent.putExtra("TEN_BAC_SI", tenBacSi);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        intent.putExtra("TEN_BENH_NHAN", tenBenhNhan);
        intent.putExtra("GOI_DANG_KY", goiDangKy);
        intent.putExtra("GIA_THANH_TOAN", giaThanhToan);
        intent.putExtra("PHUONG_THUC_THANH_TOAN", phuongThucThanhToan);
        startActivity(intent);
        finish();
    }
    
    private int getSoNgayGoi(String maGoi) {
        switch (maGoi) {
            case "GOI_CO_BAN":
                return 7;
            case "GOI_NANG_CAO":
                return 15;
            case "GOI_CAO_CAP":
                return 30;
            default:
                return 7;
        }
    }
}