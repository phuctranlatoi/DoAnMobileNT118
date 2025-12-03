package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaoBenhAnActivity extends AppCompatActivity {
    private AutoCompleteTextView actvBenhNhan;
    private TextView tvThongTinBenhNhan;
    private TextInputEditText edtChanDoan, edtGhiChu, edtSoNgayUong;
    private RecyclerView rvDonThuoc;
    private MaterialButton btnThemThuoc, btnLuuBenhAn;
    private ProgressBar progressBar;
    
    private FirestoreRepository repository;
    private ThuocKeDonAdapter thuocAdapter;
    private List<ChiTietDonThuoc> danhSachThuoc;
    private List<BenhNhan> danhSachBenhNhan;
    private String maBenhNhanChon = "";
    private String maBacSi = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_benh_an);
        
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        loadBenhNhan();
        setupListeners();
    }

    private void initViews() {
        actvBenhNhan = findViewById(R.id.actvBenhNhan);
        tvThongTinBenhNhan = findViewById(R.id.tvThongTinBenhNhan);
        edtChanDoan = findViewById(R.id.edtChanDoan);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        edtSoNgayUong = findViewById(R.id.edtSoNgayUong);
        rvDonThuoc = findViewById(R.id.rvDonThuoc);
        btnThemThuoc = findViewById(R.id.btnThemThuoc);
        btnLuuBenhAn = findViewById(R.id.btnLuuBenhAn);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
        danhSachThuoc = new ArrayList<>();
        danhSachBenhNhan = new ArrayList<>();
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
        thuocAdapter = new ThuocKeDonAdapter(this, danhSachThuoc, position -> {
            danhSachThuoc.remove(position);
            thuocAdapter.notifyItemRemoved(position);
        });
        rvDonThuoc.setLayoutManager(new LinearLayoutManager(this));
        rvDonThuoc.setAdapter(thuocAdapter);
    }

    private void loadBenhNhan() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll("BenhNhan",
            querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                danhSachBenhNhan.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    BenhNhan bn = doc.toObject(BenhNhan.class);
                    if (bn != null) {
                        danhSachBenhNhan.add(bn);
                    }
                }
                setupBenhNhanDropdown();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tải danh sách bệnh nhân: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void setupBenhNhanDropdown() {
        List<String> tenBenhNhan = new ArrayList<>();
        for (BenhNhan bn : danhSachBenhNhan) {
            tenBenhNhan.add(bn.getHoTen() + " - " + bn.getMaBenhNhan());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            tenBenhNhan
        );
        actvBenhNhan.setAdapter(adapter);
        
        actvBenhNhan.setOnItemClickListener((parent, view, position, id) -> {
            BenhNhan bn = danhSachBenhNhan.get(position);
            maBenhNhanChon = bn.getMaBenhNhan();
            showThongTinBenhNhan(bn);
        });
    }

    private void showThongTinBenhNhan(BenhNhan bn) {
        String thongTin = "Mã BN: " + bn.getMaBenhNhan() + "\n" +
                         "Ngày sinh: " + bn.getNgaySinh() + "\n" +
                         "SĐT: " + bn.getSoDienThoai();
        tvThongTinBenhNhan.setText(thongTin);
        tvThongTinBenhNhan.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnThemThuoc.setOnClickListener(v -> {
            // Mở dialog thêm thuốc (tương tự KeDonThuocActivity)
            Toast.makeText(this, "Chức năng thêm thuốc", Toast.LENGTH_SHORT).show();
        });
        
        btnLuuBenhAn.setOnClickListener(v -> luuBenhAn());
    }

    private void luuBenhAn() {
        String chanDoan = edtChanDoan.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();
        
        if (maBenhNhanChon.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (chanDoan.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnLuuBenhAn.setEnabled(false);
        
        // Tạo bệnh án
        Map<String, Object> benhAn = new HashMap<>();
        benhAn.put("maBenhNhan", maBenhNhanChon);
        benhAn.put("maBacSi", maBacSi);
        benhAn.put("chanDoan", chanDoan);
        benhAn.put("ghiChu", ghiChu);
        benhAn.put("ngayKham", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        benhAn.put("trangThai", "Đã khám");
        
        String maBenhAn = "BA" + System.currentTimeMillis();
        benhAn.put("maBenhAn", maBenhAn);
        
        repository.addDocument("BenhAn", maBenhAn, benhAn,
            aVoid -> {
                // Nếu có đơn thuốc thì tạo đơn thuốc
                if (!danhSachThuoc.isEmpty()) {
                    taoDonThuoc(maBenhAn);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(TaoBenhAnActivity.this, 
                        "Tạo bệnh án thành công", 
                        Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                btnLuuBenhAn.setEnabled(true);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tạo bệnh án: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void taoDonThuoc(String maBenhAn) {
        String maDonThuoc = "DT" + System.currentTimeMillis();
        
        repository.addDonThuoc(
            maDonThuoc,
            maBenhAn,
            new Date(),
            aVoid -> {
                // Thêm chi tiết đơn thuốc
                for (ChiTietDonThuoc ct : danhSachThuoc) {
                    repository.addChiTietDonThuoc(
                        maDonThuoc,
                        ct.getMaDuocPham(),
                        ct.getSoLuong(),
                        ct.getLieuDung(),
                        v -> {},
                        e -> Log.e("TaoBenhAn", "Lỗi thêm chi tiết: " + e.getMessage())
                    );
                }
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Tạo bệnh án và đơn thuốc thành công", 
                    Toast.LENGTH_SHORT).show();
                finish();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                btnLuuBenhAn.setEnabled(true);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tạo đơn thuốc: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }
}
