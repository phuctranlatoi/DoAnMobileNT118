package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class XembenhanActivity extends AppCompatActivity {

    private ImageView btnBack;
    private Button btnKetQuaKham, btnGhiChu;
    private TextView tvUserName, tvMaHoSo, tvAvatarLetter;
    private View layoutKetQuaKham;
    private RecyclerView rvKetQuaKham, rvGhiChu;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBenhNhan;
    private boolean isKetQuaKhamTab = true;
    private List<BenhAn> ketQuaList;
    private List<BenhAn> ghiChuList;
    private BenhAnAdapter ketQuaAdapter;
    private GhiChuAdapter ghiChuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xembenhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        initViews();
        setupClickListeners();
        loadUserInfoAndData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnKetQuaKham = findViewById(R.id.btnKetQuaKham);
        btnGhiChu = findViewById(R.id.btnGhiChu);
        tvUserName = findViewById(R.id.tvUserName);
        tvMaHoSo = findViewById(R.id.tvMaHoSo);
        tvAvatarLetter = findViewById(R.id.tvAvatarLetter);
        layoutKetQuaKham = findViewById(R.id.layoutKetQuaKham);
        rvKetQuaKham = findViewById(R.id.rvKetQuaKham);
        rvGhiChu = findViewById(R.id.rvGhiChu);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView cho Kết quả khám
        rvKetQuaKham.setLayoutManager(new LinearLayoutManager(this));
        ketQuaList = new ArrayList<>();
        ketQuaAdapter = new BenhAnAdapter(ketQuaList, benhAn -> {
            Intent intent = new Intent(this, ChiTietBenhAnActivity.class);
            intent.putExtra("MA_BENH_AN", benhAn.getMaBenhAn());
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
        });
        rvKetQuaKham.setAdapter(ketQuaAdapter);

        // Setup RecyclerView cho Ghi chú
        rvGhiChu.setLayoutManager(new LinearLayoutManager(this));
        ghiChuList = new ArrayList<>();
        ghiChuAdapter = new GhiChuAdapter(ghiChuList, benhAn -> {
            Intent intent = new Intent(this, ChiTietBenhAnActivity.class);
            intent.putExtra("MA_BENH_AN", benhAn.getMaBenhAn());
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
        });
        rvGhiChu.setAdapter(ghiChuAdapter);
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnKetQuaKham != null) {
            btnKetQuaKham.setOnClickListener(v -> switchTab(true));
        }

        if (btnGhiChu != null) {
            btnGhiChu.setOnClickListener(v -> switchTab(false));
        }
    }

    private void switchTab(boolean isKetQuaKham) {
        isKetQuaKhamTab = isKetQuaKham;

        if (isKetQuaKham) {
            // Kết quả khám tab - hiển thị chuẩn đoán
            btnKetQuaKham.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            btnKetQuaKham.setTextColor(0xFFFFFFFF);
            btnGhiChu.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
            btnGhiChu.setTextColor(0xFF7F8C8D);

            // Hiển thị kết quả khám
            if (ketQuaList.isEmpty()) {
                layoutKetQuaKham.setVisibility(View.VISIBLE);
                rvKetQuaKham.setVisibility(View.GONE);
            } else {
                layoutKetQuaKham.setVisibility(View.GONE);
                rvKetQuaKham.setVisibility(View.VISIBLE);
            }
            rvGhiChu.setVisibility(View.GONE);
        } else {
            // Ghi chú tab - hiển thị ghi chú của bác sĩ
            btnGhiChu.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3));
            btnGhiChu.setTextColor(0xFFFFFFFF);
            btnKetQuaKham.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE0E0E0));
            btnKetQuaKham.setTextColor(0xFF7F8C8D);

            layoutKetQuaKham.setVisibility(View.GONE);
            rvKetQuaKham.setVisibility(View.GONE);
            rvGhiChu.setVisibility(View.VISIBLE);

            // Load ghi chú nếu chưa load
            if (ghiChuList.isEmpty()) {
                loadGhiChu();
            }
        }
    }

    private void loadUserInfoAndData() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            String hoTen = benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "Người dùng";
                            maBenhNhan = benhNhan.getMaBenhNhan() != null ? benhNhan.getMaBenhNhan() : "N/A";

                            tvUserName.setText(hoTen);
                            tvMaHoSo.setText("Mã hồ sơ: " + maBenhNhan);

                            // Set avatar letter
                            if (hoTen.length() > 0) {
                                tvAvatarLetter.setText(String.valueOf(hoTen.charAt(0)).toUpperCase());
                            }

                            // Load kết quả khám ngay sau khi có maBenhNhan
                            loadKetQuaKham();
                        }
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("XembenhanActivity", "Lỗi tải thông tin: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadKetQuaKham() {
        if (maBenhNhan == null || maBenhNhan.isEmpty() || maBenhNhan.equals("N/A")) {
            progressBar.setVisibility(View.GONE);
            layoutKetQuaKham.setVisibility(View.VISIBLE);
            return;
        }

        repo.getByField("BenhAn", "maBenhNhan", maBenhNhan,
                querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    
                    ketQuaList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        BenhAn benhAn = doc.toObject(BenhAn.class);
                        if (benhAn != null && benhAn.getChanDoan() != null && !benhAn.getChanDoan().isEmpty()) {
                            benhAn.setMaBenhAn(doc.getId());
                            ketQuaList.add(benhAn);
                        }
                    }
                    
                    if (ketQuaList.isEmpty()) {
                        // Không có dữ liệu - hiển thị empty state
                        layoutKetQuaKham.setVisibility(View.VISIBLE);
                        rvKetQuaKham.setVisibility(View.GONE);
                    } else {
                        // Có dữ liệu - ẩn empty state và hiển thị RecyclerView
                        layoutKetQuaKham.setVisibility(View.GONE);
                        rvKetQuaKham.setVisibility(View.VISIBLE);
                        ketQuaAdapter.notifyDataSetChanged();
                    }
                },
                e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutKetQuaKham.setVisibility(View.VISIBLE);
                    Log.e("XembenhanActivity", "Lỗi tải kết quả khám: ", e);
                    Toast.makeText(this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadGhiChu() {
        if (maBenhNhan == null || maBenhNhan.isEmpty() || maBenhNhan.equals("N/A")) {
            Toast.makeText(this, "Chưa có thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        repo.getByField("BenhAn", "maBenhNhan", maBenhNhan,
                querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    ghiChuList.clear();
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        BenhAn benhAn = doc.toObject(BenhAn.class);
                        if (benhAn != null && benhAn.getGhiChu() != null && !benhAn.getGhiChu().isEmpty()) {
                            benhAn.setMaBenhAn(doc.getId());
                            ghiChuList.add(benhAn);
                        }
                    }
                    
                    if (ghiChuList.isEmpty()) {
                        Toast.makeText(this, "Chưa có ghi chú nào", Toast.LENGTH_SHORT).show();
                    } else {
                        ghiChuAdapter.notifyDataSetChanged();
                    }
                },
                e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("XembenhanActivity", "Lỗi tải ghi chú: ", e);
                    Toast.makeText(this, "Lỗi tải ghi chú", Toast.LENGTH_SHORT).show();
                });
    }
}
