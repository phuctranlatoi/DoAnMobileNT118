package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuanLyDonThuocBacSiActivity extends AppCompatActivity {

    private static final String TAG = "QuanLyDonThuocBacSi";

    private ImageView btnBack;
    private RecyclerView rvDonThuoc;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private FirestoreRepository repo;
    private String maBacSi;
    private DonThuocBacSiAdapter adapter;
    private List<DonThuoc> donThuocList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_don_thuoc_bac_si);

        repo = new FirestoreRepository();
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");

        initViews();
        setupListeners();
        loadDonThuoc();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvDonThuoc = findViewById(R.id.rvDonThuoc);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);

        rvDonThuoc.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DonThuocBacSiAdapter(this, donThuocList);
        rvDonThuoc.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDonThuoc() {
        if (maBacSi == null || maBacSi.isEmpty()) {
            showEmpty();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        repo.getByField("DonThuoc", "maBacSi", maBacSi,
            querySnapshot -> {
                donThuocList.clear();
                for (var doc : querySnapshot.getDocuments()) {
                    try {
                        DonThuoc donThuoc = doc.toObject(DonThuoc.class);
                        if (donThuoc != null) {
                            donThuocList.add(donThuoc);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing DonThuoc", e);
                    }
                }

                // Sắp xếp theo ngày kê mới nhất
                Collections.sort(donThuocList, (a, b) -> {
                    if (a.getNgayKeDon() == null && a.getNgayLap() == null) return 1;
                    if (b.getNgayKeDon() == null && b.getNgayLap() == null) return -1;
                    
                    java.util.Date dateA = a.getNgayKeDon() != null ? a.getNgayKeDon().toDate() : a.getNgayLap();
                    java.util.Date dateB = b.getNgayKeDon() != null ? b.getNgayKeDon().toDate() : b.getNgayLap();
                    
                    if (dateA == null) return 1;
                    if (dateB == null) return -1;
                    return dateB.compareTo(dateA);
                });

                progressBar.setVisibility(View.GONE);
                adapter.updateData(donThuocList);

                if (donThuocList.isEmpty()) {
                    showEmpty();
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                }

                Log.d(TAG, "Loaded " + donThuocList.size() + " prescriptions");
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                showEmpty();
                Log.e(TAG, "Error loading prescriptions", e);
            });
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        rvDonThuoc.setVisibility(View.GONE);
    }
}
