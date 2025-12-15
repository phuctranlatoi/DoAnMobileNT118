package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ChonBacSiChatActivity extends AppCompatActivity implements BacSiChatAdapter.OnBacSiClickListener {
    
    private Toolbar toolbar;
    private RecyclerView rvDanhSachBacSi;
    private View progressBar;
    private View layoutEmpty;
    
    private BacSiChatAdapter adapter;
    private FirestoreRepository repository;
    private String maBenhNhan;
    private String tenBenhNhan;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chon_bac_si_chat);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupRecyclerView();
        loadDanhSachBacSi();
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvDanhSachBacSi = findViewById(R.id.rvDanhSachBacSi);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        
        repository = new FirestoreRepository();
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        maBenhNhan = intent.getStringExtra("MA_BENH_NHAN");
        tenBenhNhan = intent.getStringExtra("TEN_BENH_NHAN");
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        adapter = new BacSiChatAdapter(this);
        rvDanhSachBacSi.setLayoutManager(new LinearLayoutManager(this));
        rvDanhSachBacSi.setAdapter(adapter);
    }
    
    private void loadDanhSachBacSi() {
        showLoading(true);
        
        repository.getAll("BacSi",
            querySnapshot -> {
                List<BacSi> danhSachBacSi = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    BacSi bacSi = doc.toObject(BacSi.class);
                    if (bacSi != null) {
                        // Chỉ hiển thị bác sĩ đã được xác thực
                        if ("Đã xác thực".equals(bacSi.getTrangThaiXacThuc())) {
                            danhSachBacSi.add(bacSi);
                        }
                    }
                }
                
                adapter.setData(danhSachBacSi);
                showLoading(false);
                showEmpty(danhSachBacSi.isEmpty());
                
            },
            e -> {
                showLoading(false);
                showEmpty(true);
                Toast.makeText(this, "Lỗi tải danh sách bác sĩ: " + e.getMessage(), 
                              Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    @Override
    public void onBacSiClick(BacSi bacSi) {
        // Mở màn hình thông tin bác sĩ để xem chi tiết và đăng ký
        Intent intent = new Intent(this, ThongTinBacSiActivity.class);
        intent.putExtra("MA_BAC_SI", bacSi.getMaBacSi());
        intent.putExtra("TEN_BAC_SI", bacSi.getHoTen());
        intent.putExtra("CHUYEN_KHOA", bacSi.getBangCap());
        intent.putExtra("MA_BENH_NHAN", maBenhNhan);
        intent.putExtra("TEN_BENH_NHAN", tenBenhNhan);
        startActivity(intent);
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void showEmpty(boolean show) {
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        if (rvDanhSachBacSi != null) {
            rvDanhSachBacSi.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}