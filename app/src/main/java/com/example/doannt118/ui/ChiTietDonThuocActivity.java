package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChiTietDonThuocActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvMaDonThuoc, tvNgayLap, tvBacSi, tvChanDoan;
    private RecyclerView rvChiTiet;
    private ProgressBar progressBar;
    private ChiTietDonThuocAdapter adapter;
    private FirestoreRepository repo;
    private String maDonThuoc;
    private String maBenhAn;
    private List<ChiTietDonThuoc> chiTietList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_don_thuoc);

        // Hỗ trợ cả 2 key để tương thích
        maDonThuoc = getIntent().getStringExtra("MA_DON_THUOC");
        if (maDonThuoc == null) {
            maDonThuoc = getIntent().getStringExtra("maDonThuoc");
        }
        
        maBenhAn = getIntent().getStringExtra("MA_BENH_AN");
        if (maBenhAn == null) {
            maBenhAn = getIntent().getStringExtra("maBenhAn");
        }

        if (maDonThuoc == null || maDonThuoc.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã đơn thuốc!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        chiTietList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadDonThuocInfo();
        loadChiTietDonThuoc();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvMaDonThuoc = findViewById(R.id.tvMaDonThuoc);
        tvNgayLap = findViewById(R.id.tvNgayLap);
        tvBacSi = findViewById(R.id.tvBacSi);
        tvChanDoan = findViewById(R.id.tvChanDoan);
        rvChiTiet = findViewById(R.id.rvChiTiet);
        progressBar = findViewById(R.id.progressBar);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new ChiTietDonThuocAdapter(this, chiTietList);
        rvChiTiet.setLayoutManager(new LinearLayoutManager(this));
        rvChiTiet.setAdapter(adapter);
    }

    private void loadDonThuocInfo() {
        progressBar.setVisibility(View.VISIBLE);

        repo.getByField("DonThuoc", "maDonThuoc", maDonThuoc,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DonThuoc donThuoc = querySnapshot.getDocuments().get(0).toObject(DonThuoc.class);
                        if (donThuoc != null) {
                            tvMaDonThuoc.setText("Mã đơn: " + donThuoc.getMaDonThuoc());
                            
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            if (donThuoc.getNgayLap() != null) {
                                tvNgayLap.setText("Ngày lập: " + sdf.format(donThuoc.getNgayLap()));
                            }

                            // Lấy mã bệnh án từ đơn thuốc nếu chưa có
                            if (maBenhAn == null || maBenhAn.isEmpty()) {
                                maBenhAn = donThuoc.getMaBenhAn();
                            }
                            
                            // Load thông tin bệnh án
                            if (maBenhAn != null && !maBenhAn.isEmpty()) {
                                loadBenhAnInfo(maBenhAn);
                            } else {
                                // Nếu không có mã bệnh án, load trực tiếp thông tin bác sĩ từ đơn thuốc
                                if (donThuoc.getMaBacSi() != null) {
                                    loadBacSiInfo(donThuoc.getMaBacSi());
                                }
                                tvChanDoan.setText("Chẩn đoán: Không có thông tin");
                            }
                        }
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin đơn thuốc", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("ChiTietDonThuoc", "Lỗi tải thông tin đơn thuốc: ", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBenhAnInfo(String maBenhAn) {
        repo.getByField("BenhAn", "maBenhAn", maBenhAn,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BenhAn benhAn = querySnapshot.getDocuments().get(0).toObject(BenhAn.class);
                        if (benhAn != null) {
                            tvChanDoan.setText("Chẩn đoán: " + benhAn.getChanDoan());
                            
                            // Load tên bác sĩ
                            loadBacSiInfo(benhAn.getMaBacSi());
                        }
                    }
                },
                e -> Log.e("ChiTietDonThuoc", "Lỗi tải bệnh án: ", e));
    }

    private void loadBacSiInfo(String maBacSi) {
        repo.getByField("BacSi", "maBacSi", maBacSi,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String hoTen = querySnapshot.getDocuments().get(0).getString("hoTen");
                        tvBacSi.setText("Bác sĩ: " + hoTen);
                    }
                },
                e -> Log.e("ChiTietDonThuoc", "Lỗi tải bác sĩ: ", e));
    }

    private void loadChiTietDonThuoc() {
        repo.getChiTietDonThuoc(maDonThuoc,
                querySnapshot -> {
                    chiTietList.clear();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        querySnapshot.forEach(doc -> {
                            ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
                            if (chiTiet != null) {
                                chiTietList.add(chiTiet);
                            }
                        });
                    }
                    adapter.notifyDataSetChanged();
                    
                    if (chiTietList.isEmpty()) {
                        Toast.makeText(this, "Đơn thuốc này chưa có chi tiết", Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    Log.e("ChiTietDonThuoc", "Lỗi tải chi tiết: ", e);
                    Toast.makeText(this, "Lỗi tải chi tiết đơn thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
