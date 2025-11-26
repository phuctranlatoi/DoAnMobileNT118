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
import com.example.doannt118.model.ChiTietHoaDon;
import com.example.doannt118.model.HoaDon;
import com.example.doannt118.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChiTietHoaDonActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvMaHoaDon, tvNgayLap, tvBacSi, tvChanDoan, tvTongTien;
    private RecyclerView rvChiTiet;
    private ProgressBar progressBar;
    private ChiTietHoaDonAdapter adapter;
    private FirestoreRepository repo;
    private String maHoaDon;
    private String maBenhAn;
    private List<ChiTietHoaDon> chiTietList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hoa_don);

        maHoaDon = getIntent().getStringExtra("MA_HOA_DON");
        maBenhAn = getIntent().getStringExtra("MA_BENH_AN");

        if (maHoaDon == null || maHoaDon.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã hóa đơn!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        chiTietList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadHoaDonInfo();
        loadChiTietHoaDon();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvMaHoaDon = findViewById(R.id.tvMaHoaDon);
        tvNgayLap = findViewById(R.id.tvNgayLap);
        tvBacSi = findViewById(R.id.tvBacSi);
        tvChanDoan = findViewById(R.id.tvChanDoan);
        tvTongTien = findViewById(R.id.tvTongTien);
        rvChiTiet = findViewById(R.id.rvChiTiet);
        progressBar = findViewById(R.id.progressBar);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new ChiTietHoaDonAdapter(this, chiTietList);
        rvChiTiet.setLayoutManager(new LinearLayoutManager(this));
        rvChiTiet.setAdapter(adapter);
    }

    private void loadHoaDonInfo() {
        progressBar.setVisibility(View.VISIBLE);

        repo.getByField("HoaDon", "maHoaDon", maHoaDon,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        HoaDon hoaDon = querySnapshot.getDocuments().get(0).toObject(HoaDon.class);
                        if (hoaDon != null) {
                            tvMaHoaDon.setText("Mã hóa đơn: " + hoaDon.getMaHoaDon());
                            
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            tvNgayLap.setText("Ngày lập: " + sdf.format(hoaDon.getNgayLap()));
                            
                            tvTongTien.setText(String.format("Tổng tiền: %,.0f đ", hoaDon.getTongTien()));

                            if (maBenhAn != null) {
                                loadBenhAnInfo(maBenhAn);
                            }
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("ChiTietHoaDon", "Lỗi tải thông tin hóa đơn: ", e);
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBenhAnInfo(String maBenhAn) {
        repo.getByField("BenhAn", "maBenhAn", maBenhAn,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BenhAn benhAn = querySnapshot.getDocuments().get(0).toObject(BenhAn.class);
                        if (benhAn != null) {
                            tvChanDoan.setText("Chẩn đoán: " + benhAn.getChanDoan());
                            loadBacSiInfo(benhAn.getMaBacSi());
                        }
                    }
                },
                e -> Log.e("ChiTietHoaDon", "Lỗi tải bệnh án: ", e));
    }

    private void loadBacSiInfo(String maBacSi) {
        repo.getByField("BacSi", "maBacSi", maBacSi,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        String hoTen = querySnapshot.getDocuments().get(0).getString("hoTen");
                        tvBacSi.setText("Bác sĩ: " + hoTen);
                    }
                },
                e -> Log.e("ChiTietHoaDon", "Lỗi tải bác sĩ: ", e));
    }

    private void loadChiTietHoaDon() {
        repo.getChiTietHoaDon(maHoaDon,
                querySnapshot -> {
                    chiTietList.clear();
                    querySnapshot.forEach(doc -> {
                        ChiTietHoaDon chiTiet = doc.toObject(ChiTietHoaDon.class);
                        chiTietList.add(chiTiet);
                    });
                    adapter.notifyDataSetChanged();
                },
                e -> {
                    Log.e("ChiTietHoaDon", "Lỗi tải chi tiết: ", e);
                    Toast.makeText(this, "Lỗi tải chi tiết hóa đơn", Toast.LENGTH_SHORT).show();
                });
    }
}
