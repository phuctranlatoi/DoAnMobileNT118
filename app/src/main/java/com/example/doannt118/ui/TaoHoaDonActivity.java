package com.example.doannt118.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietHoaDon;
import com.example.doannt118.model.HoaDon;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaoHoaDonActivity extends AppCompatActivity {
    private TextView tvBenhNhan, tvBenhAn, tvEmptyDichVu, tvTongTien;
    private RecyclerView rvDichVu;
    private MaterialButton btnThemDichVu, btnLuuHoaDon;
    private ProgressBar progressBar;
    
    private DichVuHoaDonAdapter adapter;
    private FirestoreRepository repository;
    private List<ChiTietHoaDon> danhSachDichVu;
    private NumberFormat currencyFormat;
    
    private String maBenhAn;
    private String maBenhNhan;
    private String tenBenhNhan;
    private double tongTien = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_hoa_don);

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        maBenhAn = getIntent().getStringExtra("maBenhAn");
        if (maBenhAn != null) {
            loadBenhAnInfo();
        } else {
            Toast.makeText(this, "Không tìm thấy mã bệnh án", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvBenhNhan = findViewById(R.id.tvBenhNhan);
        tvBenhAn = findViewById(R.id.tvBenhAn);
        tvEmptyDichVu = findViewById(R.id.tvEmptyDichVu);
        tvTongTien = findViewById(R.id.tvTongTien);
        rvDichVu = findViewById(R.id.rvDichVu);
        btnThemDichVu = findViewById(R.id.btnThemDichVu);
        btnLuuHoaDon = findViewById(R.id.btnLuuHoaDon);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
        danhSachDichVu = new ArrayList<>();
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        
        btnThemDichVu.setOnClickListener(v -> showDialogThemDichVu());
        btnLuuHoaDon.setOnClickListener(v -> luuHoaDon());
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
        adapter = new DichVuHoaDonAdapter(this, danhSachDichVu, position -> {
            ChiTietHoaDon dichVu = danhSachDichVu.get(position);
            tongTien -= dichVu.getThanhTien();
            danhSachDichVu.remove(position);
            adapter.notifyItemRemoved(position);
            updateTongTien();
            updateEmptyView();
        });
        rvDichVu.setLayoutManager(new LinearLayoutManager(this));
        rvDichVu.setAdapter(adapter);
    }

    private void loadBenhAnInfo() {
        showLoading(true);
        
        repository.getByField("BenhAn", "maBenhAn", maBenhAn,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    maBenhNhan = doc.getString("maBenhNhan");
                    tvBenhAn.setText("Bệnh án: " + maBenhAn);
                    loadBenhNhanInfo();
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Không tìm thấy bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadBenhNhanInfo() {
        repository.getByField("BenhNhan", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                showLoading(false);
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    tenBenhNhan = doc.getString("hoTen");
                    tvBenhNhan.setText("Bệnh nhân: " + tenBenhNhan);
                }
            },
            e -> {
                showLoading(false);
                tvBenhNhan.setText("Bệnh nhân: Không rõ");
            }
        );
    }

    private void showDialogThemDichVu() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_them_dich_vu);
        dialog.getWindow().setLayout(
            getResources().getDisplayMetrics().widthPixels - 100,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        TextInputEditText edtTenDichVu = dialog.findViewById(R.id.edtTenDichVu);
        TextInputEditText edtSoLuong = dialog.findViewById(R.id.edtSoLuong);
        TextInputEditText edtDonGia = dialog.findViewById(R.id.edtDonGia);
        MaterialButton btnHuy = dialog.findViewById(R.id.btnHuy);
        MaterialButton btnXacNhan = dialog.findViewById(R.id.btnXacNhan);

        btnHuy.setOnClickListener(v -> dialog.dismiss());
        
        btnXacNhan.setOnClickListener(v -> {
            String tenDichVu = edtTenDichVu.getText().toString().trim();
            String soLuongStr = edtSoLuong.getText().toString().trim();
            String donGiaStr = edtDonGia.getText().toString().trim();
            
            if (tenDichVu.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên dịch vụ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int soLuong = 1;
            double donGia = 0;
            
            try {
                soLuong = Integer.parseInt(soLuongStr);
                donGia = Double.parseDouble(donGiaStr);
            } catch (Exception e) {
                Toast.makeText(this, "Số lượng hoặc đơn giá không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (soLuong <= 0 || donGia <= 0) {
                Toast.makeText(this, "Số lượng và đơn giá phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String maChiTiet = "CTHD_" + UUID.randomUUID().toString();
            ChiTietHoaDon dichVu = new ChiTietHoaDon(maChiTiet, null, tenDichVu, soLuong, donGia);
            
            danhSachDichVu.add(dichVu);
            tongTien += dichVu.getThanhTien();
            adapter.notifyItemInserted(danhSachDichVu.size() - 1);
            updateTongTien();
            updateEmptyView();
            
            dialog.dismiss();
        });

        dialog.show();
    }

    private void luuHoaDon() {
        if (danhSachDichVu.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 dịch vụ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        String maHoaDon = "HD_" + UUID.randomUUID().toString();
        Date ngayLap = new Date();
        
        HoaDon hoaDon = new HoaDon(maHoaDon, maBenhAn, maBenhNhan, ngayLap, tongTien);
        
        repository.addDocument("HoaDon", maHoaDon, hoaDon,
            aVoid -> {
                luuChiTietHoaDon(maHoaDon);
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void luuChiTietHoaDon(String maHoaDon) {
        int[] count = {0};
        int total = danhSachDichVu.size();
        
        for (ChiTietHoaDon dichVu : danhSachDichVu) {
            dichVu.setMaHoaDon(maHoaDon);
            
            repository.addDocument("ChiTietHoaDon", dichVu.getMaChiTiet(), dichVu,
                aVoid -> {
                    count[0]++;
                    if (count[0] == total) {
                        guiThongBao();
                        showLoading(false);
                        Toast.makeText(this, "Đã lưu hóa đơn", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi lưu chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            );
        }
    }

    private void guiThongBao() {
        NotificationHelper helper = new NotificationHelper(this);
        helper.guiThongBaoTuBacSi(maBenhNhan, null,
            "Hóa đơn mới",
            "Bạn có hóa đơn mới với tổng tiền " + currencyFormat.format(tongTien) + 
            ". Vui lòng thanh toán.");
    }

    private void updateTongTien() {
        tvTongTien.setText(currencyFormat.format(tongTien));
    }

    private void updateEmptyView() {
        tvEmptyDichVu.setVisibility(danhSachDichVu.isEmpty() ? View.VISIBLE : View.GONE);
        rvDichVu.setVisibility(danhSachDichVu.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLuuHoaDon.setEnabled(!show);
    }
}
