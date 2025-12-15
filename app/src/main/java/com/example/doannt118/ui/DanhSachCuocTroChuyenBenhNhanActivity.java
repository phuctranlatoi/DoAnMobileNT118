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
import com.example.doannt118.model.CuocTroChuyenBenhNhan;
import com.example.doannt118.model.TinNhanBacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DanhSachCuocTroChuyenBenhNhanActivity extends AppCompatActivity 
        implements CuocTroChuyenBenhNhanAdapter.OnItemClickListener {
    
    private Toolbar toolbar;
    private RecyclerView rvDanhSachCuocTroChuyenBenhNhan;
    private LinearLayout layoutEmpty;
    private LinearLayout btnChatNgay;
    private View progressBar;
    
    private CuocTroChuyenBenhNhanAdapter adapter;
    private FirestoreRepository repository;
    private String maBenhNhan;
    private String tenBenhNhan;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_cuoc_tro_chuyen_benh_nhan);
        
        initViews();
        getDataFromIntent();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
        
        if (maBenhNhan != null) {
            loadDanhSachCuocTroChuyenBenhNhan();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvDanhSachCuocTroChuyenBenhNhan = findViewById(R.id.rvDanhSachCuocTroChuyenBenhNhan);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnChatNgay = findViewById(R.id.btnChatNgay);
        progressBar = findViewById(R.id.progressBar);
        
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
        adapter = new CuocTroChuyenBenhNhanAdapter(this);
        rvDanhSachCuocTroChuyenBenhNhan.setLayoutManager(new LinearLayoutManager(this));
        rvDanhSachCuocTroChuyenBenhNhan.setAdapter(adapter);
    }
    
    private void setupListeners() {
        btnChatNgay.setOnClickListener(v -> {
            // Chuyển đến màn hình chọn bác sĩ
            Intent intent = new Intent(this, ChonBacSiChatActivity.class);
            intent.putExtra("MA_BENH_NHAN", maBenhNhan);
            intent.putExtra("TEN_BENH_NHAN", tenBenhNhan);
            startActivity(intent);
        });
    }
    
    private void loadDanhSachCuocTroChuyenBenhNhan() {
        showLoading(true);
        
        // Lấy tất cả tin nhắn của bệnh nhân này với real-time listener
        // Không dùng orderBy để tránh lỗi index, sẽ sort trong code
        FirebaseFirestore.getInstance()
            .collection("TinNhanBacSi")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .addSnapshotListener((querySnapshot, e) -> {
                showLoading(false);
                
                if (e != null) {
                    showEmpty(true);
                    android.util.Log.d("DanhSachTinNhan", "Lỗi tải tin nhắn: " + e.getMessage());
                    return;
                }
                
                if (querySnapshot != null) {
            Map<String, CuocTroChuyenBenhNhan> mapCuocTroChuyenBenhNhan = new HashMap<>();
            Map<String, Integer> mapSoTinNhanChuaDoc = new HashMap<>();
            Map<String, TinNhanBacSi> mapTinNhanCuoi = new HashMap<>();
            
            // Đếm tin nhắn chưa đọc và tìm tin nhắn cuối
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                TinNhanBacSi tinNhan = doc.toObject(TinNhanBacSi.class);
                if (tinNhan != null) {
                    String maBacSi = tinNhan.getMaBacSi();
                    
                    // Cập nhật tin nhắn cuối (theo thời gian)
                    TinNhanBacSi tinNhanCuoi = mapTinNhanCuoi.get(maBacSi);
                    if (tinNhanCuoi == null || 
                        (tinNhan.getThoiGianGui() != null && tinNhanCuoi.getThoiGianGui() != null &&
                         tinNhan.getThoiGianGui().compareTo(tinNhanCuoi.getThoiGianGui()) > 0)) {
                        mapTinNhanCuoi.put(maBacSi, tinNhan);
                    }
                    
                    // Đếm tin nhắn chưa đọc (tin nhắn từ bác sĩ mà bệnh nhân chưa đọc)
                    if (tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BAC_SI &&
                        tinNhan.getTrangThai() != TinNhanBacSi.TrangThaiTinNhan.DA_XEM) {
                        mapSoTinNhanChuaDoc.put(maBacSi, 
                            mapSoTinNhanChuaDoc.getOrDefault(maBacSi, 0) + 1);
                    }
                }
            }
            
            // Tạo danh sách cuộc trò chuyện
            for (Map.Entry<String, TinNhanBacSi> entry : mapTinNhanCuoi.entrySet()) {
                String maBacSi = entry.getKey();
                TinNhanBacSi tinNhanCuoi = entry.getValue();
                
                boolean laBenhNhanGuiCuoi = tinNhanCuoi.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN;
                CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan = new CuocTroChuyenBenhNhan(
                    maBacSi,
                    "BS. " + (tinNhanCuoi.getTenNguoiGui().startsWith("BS.") ? 
                             tinNhanCuoi.getTenNguoiGui().substring(4) : tinNhanCuoi.getTenNguoiGui()),
                    tinNhanCuoi.getNoiDung(),
                    tinNhanCuoi.getThoiGianGui(),
                    laBenhNhanGuiCuoi
                );
                
                // Set số tin nhắn chưa đọc
                cuocTroChuyenBenhNhan.setSoTinNhanChuaDoc(
                    mapSoTinNhanChuaDoc.getOrDefault(maBacSi, 0));
                
                mapCuocTroChuyenBenhNhan.put(maBacSi, cuocTroChuyenBenhNhan);
            }
            
            // Chuyển map thành list và sắp xếp theo thời gian
            List<CuocTroChuyenBenhNhan> danhSachCuocTroChuyenBenhNhan = new ArrayList<>(mapCuocTroChuyenBenhNhan.values());
            danhSachCuocTroChuyenBenhNhan.sort((c1, c2) -> {
                if (c1.getThoiGianCuoi() == null) return 1;
                if (c2.getThoiGianCuoi() == null) return -1;
                return c2.getThoiGianCuoi().compareTo(c1.getThoiGianCuoi());
            });
            
                adapter.setData(danhSachCuocTroChuyenBenhNhan);
                showEmpty(danhSachCuocTroChuyenBenhNhan.isEmpty());
                } else {
                    showEmpty(true);
                }
            });
    }
    
    @Override
    public void onItemClick(CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan) {
        // Mở màn hình chat với bác sĩ
        Intent intent = new Intent(this, NhanTinBacSiActivity.class);
        intent.putExtra("MA_BAC_SI", cuocTroChuyenBenhNhan.getMaBacSi());
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
        if (rvDanhSachCuocTroChuyenBenhNhan != null) {
            rvDanhSachCuocTroChuyenBenhNhan.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}