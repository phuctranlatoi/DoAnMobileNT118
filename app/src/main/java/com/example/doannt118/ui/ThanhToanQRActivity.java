package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import com.example.doannt118.R;
import com.example.doannt118.model.TinNhanBacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ThanhToanQRActivity extends AppCompatActivity {
    
    private Toolbar toolbar;
    private TextView tvTenBacSi, tvTenGoi, tvTongTien, tvPhuongThucThanhToan;
    private TextView tvTrangThaiThanhToan;
    private ImageView ivQRCode;
    private CardView cardTrangThaiThanhToan;
    private Button btnHuyThanhToan;
    
    private FirestoreRepository repository;
    private String maDangKy;
    private String maBacSi;
    private String tenBacSi;
    private String maBenhNhan;
    private String tenBenhNhan;
    private String goiDangKy;
    private double giaThanhToan;
    private String phuongThucThanhToan;
    
    private Handler handler;
    private Runnable thanhToanRunnable;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan_qr);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupListeners();
        hienThiThongTinThanhToan();
        
        // Tự động "thanh toán" sau 5 giây
        batDauThanhToan();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTenBacSi = findViewById(R.id.tvTenBacSi);
        tvTenGoi = findViewById(R.id.tvTenGoi);
        tvTongTien = findViewById(R.id.tvTongTien);
        tvPhuongThucThanhToan = findViewById(R.id.tvPhuongThucThanhToan);
        tvTrangThaiThanhToan = findViewById(R.id.tvTrangThaiThanhToan);
        ivQRCode = findViewById(R.id.ivQRCode);
        cardTrangThaiThanhToan = findViewById(R.id.cardTrangThaiThanhToan);
        btnHuyThanhToan = findViewById(R.id.btnHuyThanhToan);
        
        repository = new FirestoreRepository();
        handler = new Handler();
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
        phuongThucThanhToan = intent.getStringExtra("PHUONG_THUC_THANH_TOAN");
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbar.setNavigationOnClickListener(v -> {
            huyThanhToan();
            finish();
        });
    }
    
    private void setupListeners() {
        btnHuyThanhToan.setOnClickListener(v -> {
            huyThanhToan();
            finish();
        });
    }
    
    private void hienThiThongTinThanhToan() {
        tvTenBacSi.setText("BS. " + tenBacSi);
        tvTenGoi.setText(getTenGoi(goiDangKy));
        
        // Format tiền tệ
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTongTien.setText(formatter.format(giaThanhToan));
        
        // Hiển thị phương thức thanh toán
        String phuongThuc = phuongThucThanhToan != null ? phuongThucThanhToan : "Ví điện tử";
        tvPhuongThucThanhToan.setText(getPhuongThucText(phuongThuc));
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
    
    private String getPhuongThucText(String phuongThuc) {
        switch (phuongThuc) {
            case "Ví điện tử":
                return "Ví điện tử MoMo";
            case "Thẻ tín dụng":
                return "Thẻ Visa/MasterCard";
            case "Chuyển khoản":
                return "Chuyển khoản ngân hàng";
            default:
                return "Ví điện tử MoMo";
        }
    }
    
    private void batDauThanhToan() {
        // Hiển thị trạng thái đang xử lý sau 2 giây
        handler.postDelayed(() -> {
            cardTrangThaiThanhToan.setVisibility(android.view.View.VISIBLE);
            tvTrangThaiThanhToan.setText("Đang xử lý thanh toán...");
        }, 2000);
        
        // Hoàn thành thanh toán sau 5 giây
        thanhToanRunnable = () -> {
            tvTrangThaiThanhToan.setText("Thanh toán thành công!");
            
            // Cập nhật database sau 1 giây nữa
            handler.postDelayed(this::capNhatThanhToan, 1000);
        };
        
        handler.postDelayed(thanhToanRunnable, 5000);
    }
    
    private void capNhatThanhToan() {
        if (maDangKy == null) {
            Toast.makeText(this, "Không tìm thấy thông tin đăng ký!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tạo mã giao dịch
        String maGiaoDich = "GD" + System.currentTimeMillis();
        
        // Cập nhật thông tin thanh toán
        Map<String, Object> capNhat = new HashMap<>();
        capNhat.put("trangThaiThanhToan", "Đã thanh toán");
        capNhat.put("phuongThucThanhToan", phuongThucThanhToan);
        capNhat.put("maGiaoDich", maGiaoDich);
        
        // Tính ngày hết hạn
        Calendar calendar = Calendar.getInstance();
        int soNgay = getSoNgayGoi(goiDangKy);
        calendar.add(Calendar.DAY_OF_MONTH, soNgay);
        capNhat.put("ngayHetHan", new Timestamp(calendar.getTime()));
        
        // Cập nhật vào Firestore
        repository.update("DangKyNhanTin", maDangKy, capNhat,
            aVoid -> {
                // Chuyển thẳng đến chat, không tạo tin nhắn chào mừng
                chuyenDenChat();
            },
            e -> {
                Toast.makeText(this, "Lỗi cập nhật thanh toán: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Vẫn chuyển đến chat dù có lỗi
                chuyenDenChat();
            }
        );
    }
    
    private void chuyenDenChat() {
        Toast.makeText(this, "Thanh toán thành công! Chào mừng bạn đến với dịch vụ tư vấn.", 
                      Toast.LENGTH_LONG).show();
        
        // Chuyển đến màn hình chat với bác sĩ
        Intent intent = new Intent(this, NhanTinBacSiActivity.class);
        intent.putExtra("MA_BAC_SI", maBacSi);
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        intent.putExtra("TEN_BENH_NHAN", tenBenhNhan);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
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
    
    private void huyThanhToan() {
        if (thanhToanRunnable != null) {
            handler.removeCallbacks(thanhToanRunnable);
        }
        handler.removeCallbacksAndMessages(null);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        huyThanhToan();
    }
}