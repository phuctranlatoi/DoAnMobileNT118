package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.CuocTroChuyenBacSi;
import com.example.doannt118.model.TinNhanBacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DanhSachTinNhanBacSiActivity extends AppCompatActivity 
        implements CuocTroChuyenBacSiAdapter.OnItemClickListener {
    
    private Toolbar toolbar;
    private RecyclerView rvDanhSachCuocTroChuyenBacSi;
    private LinearLayout layoutEmpty;
    private View progressBar;
    
    private CuocTroChuyenBacSiAdapter adapter;
    private FirestoreRepository repository;
    private String maBacSi;
    private String tenBacSi;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_tin_nhan_bac_si);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupRecyclerView();
        
        if (maBacSi != null) {
            loadDanhSachCuocTroChuyenBacSi();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin bác sĩ!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvDanhSachCuocTroChuyenBacSi = findViewById(R.id.rvDanhSachCuocTroChuyenBacSi);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        maBacSi = intent.getStringExtra("MA_BAC_SI");
        tenBacSi = intent.getStringExtra("TEN_BAC_SI");
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
        adapter = new CuocTroChuyenBacSiAdapter(this);
        rvDanhSachCuocTroChuyenBacSi.setLayoutManager(new LinearLayoutManager(this));
        rvDanhSachCuocTroChuyenBacSi.setAdapter(adapter);
    }
    
    private void loadDanhSachCuocTroChuyenBacSi() {
        showLoading(true);
        
        // Lấy tất cả tin nhắn của bác sĩ này với real-time listener
        // Không dùng orderBy để tránh lỗi index
        Query query = FirebaseFirestore.getInstance()
            .collection("TinNhanBacSi")
            .whereEqualTo("maBacSi", maBacSi);
        
        query.addSnapshotListener((querySnapshot, e) -> {
            showLoading(false);
            
            if (e != null) {
                android.util.Log.d("DanhSachTinNhanBacSi", "Lỗi tải tin nhắn: " + e.getMessage());
                showEmpty(true);
                return;
            }
            
            if (querySnapshot != null) {
            Map<String, CuocTroChuyenBacSi> mapCuocTroChuyenBacSi = new HashMap<>();
            Map<String, Integer> mapSoTinNhanChuaDoc = new HashMap<>();
            Map<String, TinNhanBacSi> mapTinNhanCuoi = new HashMap<>();
            
            // Đếm tin nhắn chưa đọc và tìm tin nhắn cuối
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                TinNhanBacSi tinNhan = doc.toObject(TinNhanBacSi.class);
                if (tinNhan != null) {
                    String maBenhNhan = tinNhan.getMaBenhNhan();
                    
                    // Cập nhật tin nhắn cuối (theo thời gian)
                    TinNhanBacSi tinNhanCuoi = mapTinNhanCuoi.get(maBenhNhan);
                    if (tinNhanCuoi == null || 
                        (tinNhan.getThoiGianGui() != null && tinNhanCuoi.getThoiGianGui() != null &&
                         tinNhan.getThoiGianGui().compareTo(tinNhanCuoi.getThoiGianGui()) > 0)) {
                        mapTinNhanCuoi.put(maBenhNhan, tinNhan);
                    }
                    
                    // Đếm tin nhắn chưa đọc (tin nhắn từ bệnh nhân mà bác sĩ chưa đọc)
                    if (tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN &&
                        tinNhan.getTrangThai() != TinNhanBacSi.TrangThaiTinNhan.DA_XEM) {
                        mapSoTinNhanChuaDoc.put(maBenhNhan, 
                            mapSoTinNhanChuaDoc.getOrDefault(maBenhNhan, 0) + 1);
                    }
                }
            }
            
            // Tạo danh sách cuộc trò chuyện
            for (Map.Entry<String, TinNhanBacSi> entry : mapTinNhanCuoi.entrySet()) {
                String maBenhNhan = entry.getKey();
                TinNhanBacSi tinNhanCuoi = entry.getValue();
                
                boolean laBacSiGuiCuoi = tinNhanCuoi.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BAC_SI;
                CuocTroChuyenBacSi cuocTroChuyenBacSi = new CuocTroChuyenBacSi(
                    maBenhNhan,
                    tinNhanCuoi.getTenNguoiGui(),
                    tinNhanCuoi.getNoiDung(),
                    tinNhanCuoi.getThoiGianGui(),
                    laBacSiGuiCuoi
                );
                
                // Set số tin nhắn chưa đọc
                cuocTroChuyenBacSi.setSoTinNhanChuaDoc(
                    mapSoTinNhanChuaDoc.getOrDefault(maBenhNhan, 0));
                
                mapCuocTroChuyenBacSi.put(maBenhNhan, cuocTroChuyenBacSi);
            }
            
            // Chuyển map thành list và sắp xếp theo thời gian
            List<CuocTroChuyenBacSi> danhSachCuocTroChuyenBacSi = new ArrayList<>(mapCuocTroChuyenBacSi.values());
            danhSachCuocTroChuyenBacSi.sort((c1, c2) -> {
                if (c1.getThoiGianCuoi() == null) return 1;
                if (c2.getThoiGianCuoi() == null) return -1;
                return c2.getThoiGianCuoi().compareTo(c1.getThoiGianCuoi());
            });
            
                adapter.setData(danhSachCuocTroChuyenBacSi);
                showEmpty(danhSachCuocTroChuyenBacSi.isEmpty());
            } else {
                showEmpty(true);
            }
        });
    }
    
    @Override
    public void onItemClick(CuocTroChuyenBacSi cuocTroChuyenBacSi) {
        // Mở màn hình chat với bệnh nhân
        Intent intent = new Intent(this, NhanTinBacSiActivity.class);
        intent.putExtra("MA_BAC_SI", maBacSi);
        intent.putExtra("MA_BENH_NHAN", cuocTroChuyenBacSi.getMaBenhNhan());
        intent.putExtra("TEN_BENH_NHAN", cuocTroChuyenBacSi.getTenBenhNhan());
        intent.putExtra("TEN_BAC_SI", tenBacSi);
        intent.putExtra("IS_DOCTOR_VIEW", true); // Đánh dấu là view của bác sĩ
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
        if (rvDanhSachCuocTroChuyenBacSi != null) {
            rvDanhSachCuocTroChuyenBacSi.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}