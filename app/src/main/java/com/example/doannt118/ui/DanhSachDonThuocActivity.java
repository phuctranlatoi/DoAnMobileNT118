package com.example.doannt118.ui;

import android.content.Intent;
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
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class DanhSachDonThuocActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView rvDonThuoc;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private DonThuocAdapter adapter;
    private FirestoreRepository repo;
    private String maBenhNhan;
    private List<DonThuoc> donThuocList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_don_thuoc);

        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã bệnh nhân!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        donThuocList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadDonThuoc();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvDonThuoc = findViewById(R.id.rvDonThuoc);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        adapter = new DonThuocAdapter(this, donThuocList, donThuoc -> {
            Intent intent = new Intent(this, ChiTietDonThuocActivity.class);
            intent.putExtra("MA_DON_THUOC", donThuoc.getMaDonThuoc());
            intent.putExtra("MA_BENH_AN", donThuoc.getMaBenhAn());
            startActivity(intent);
        });
        rvDonThuoc.setLayoutManager(new LinearLayoutManager(this));
        rvDonThuoc.setAdapter(adapter);
    }

    private void loadDonThuoc() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Lấy danh sách đơn thuốc theo mã bệnh nhân
        repo.getDonThuocByBenhNhan(maBenhNhan,
                querySnapshot -> {
                    donThuocList.clear();
                    querySnapshot.forEach(doc -> {
                        DonThuoc donThuoc = doc.toObject(DonThuoc.class);
                        donThuocList.add(donThuoc);
                    });

                    progressBar.setVisibility(View.GONE);
                    if (donThuocList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                },
                e -> {
                    Log.e("DanhSachDonThuoc", "Lỗi tải đơn thuốc: ", e);
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
