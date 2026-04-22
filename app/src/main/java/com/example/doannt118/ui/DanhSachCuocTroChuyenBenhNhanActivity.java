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
        // Sử dụng conversationId để đảm bảo tính nhất quán
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
            
            // Đếm tin nhắn chưa đọc và tìm tin nhắn cuối theo conversationId
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                TinNhanBacSi tinNhan = doc.toObject(TinNhanBacSi.class);
                if (tinNhan != null && validateMessageForPatient(tinNhan, maBenhNhan)) {
                    String maBacSi = tinNhan.getMaBacSi();
                    String conversationId = tinNhan.getConversationId();
                    
                    // Sử dụng conversationId làm key nếu có, nếu không thì dùng maBacSi
                    String key = conversationId != null ? conversationId : maBacSi;
                    
                    // Cập nhật tin nhắn cuối (theo thời gian)
                    TinNhanBacSi tinNhanCuoi = mapTinNhanCuoi.get(key);
                    if (tinNhanCuoi == null || 
                        (tinNhan.getThoiGianGui() != null && tinNhanCuoi.getThoiGianGui() != null &&
                         tinNhan.getThoiGianGui().compareTo(tinNhanCuoi.getThoiGianGui()) > 0)) {
                        mapTinNhanCuoi.put(key, tinNhan);
                    }
                    
                    // Đếm tin nhắn chưa đọc (tin nhắn từ bác sĩ mà bệnh nhân chưa đọc)
                    if (tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BAC_SI &&
                        tinNhan.getTrangThai() != TinNhanBacSi.TrangThaiTinNhan.DA_XEM) {
                        mapSoTinNhanChuaDoc.put(key, 
                            mapSoTinNhanChuaDoc.getOrDefault(key, 0) + 1);
                    }
                }
            }
            
            // Tạo danh sách cuộc trò chuyện và lấy tên bác sĩ
            for (Map.Entry<String, TinNhanBacSi> entry : mapTinNhanCuoi.entrySet()) {
                String key = entry.getKey();
                TinNhanBacSi tinNhanCuoi = entry.getValue();
                String maBacSi = tinNhanCuoi.getMaBacSi();
                
                boolean laBenhNhanGuiCuoi = tinNhanCuoi.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN;
                
                // Tạo cuộc trò chuyện với tên tạm thời, sẽ cập nhật sau
                CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan = new CuocTroChuyenBenhNhan(
                    maBacSi,
                    "Đang tải...", // Tên tạm thời
                    tinNhanCuoi.getNoiDung(),
                    tinNhanCuoi.getThoiGianGui(),
                    laBenhNhanGuiCuoi
                );
                
                // Set số tin nhắn chưa đọc
                cuocTroChuyenBenhNhan.setSoTinNhanChuaDoc(
                    mapSoTinNhanChuaDoc.getOrDefault(key, 0));
                
                mapCuocTroChuyenBenhNhan.put(maBacSi, cuocTroChuyenBenhNhan);
            }
            
            // Lấy tên bác sĩ từ collection BacSi
            loadTenBacSiForConversations(new ArrayList<>(mapCuocTroChuyenBenhNhan.values()));
            
            // Chuyển map thành list và sắp xếp theo thời gian
            List<CuocTroChuyenBenhNhan> danhSachCuocTroChuyenBenhNhan = new ArrayList<>(mapCuocTroChuyenBenhNhan.values());
            danhSachCuocTroChuyenBenhNhan.sort((c1, c2) -> {
                if (c1.getThoiGianCuoi() == null) return 1;
                if (c2.getThoiGianCuoi() == null) return -1;
                return c2.getThoiGianCuoi().compareTo(c1.getThoiGianCuoi());
            });
            
                adapter.setData(danhSachCuocTroChuyenBenhNhan);
                showEmpty(danhSachCuocTroChuyenBenhNhan.isEmpty());
                
                android.util.Log.d("DanhSachTinNhan", "Loaded " + danhSachCuocTroChuyenBenhNhan.size() + " conversations for patient: " + maBenhNhan);
                } else {
                    showEmpty(true);
                }
            });
    }
    
    /**
     * Validate tin nhắn cho bệnh nhân để đảm bảo không bị lộn xộn
     */
    private boolean validateMessageForPatient(TinNhanBacSi tinNhan, String expectedMaBenhNhan) {
        if (tinNhan == null) return false;
        
        // Kiểm tra mã bệnh nhân
        if (!expectedMaBenhNhan.equals(tinNhan.getMaBenhNhan())) {
            android.util.Log.w("DanhSachTinNhan", "Message validation failed - Expected maBenhNhan: " + 
                expectedMaBenhNhan + ", Got: " + tinNhan.getMaBenhNhan());
            return false;
        }
        
        // Kiểm tra có mã bác sĩ
        if (android.text.TextUtils.isEmpty(tinNhan.getMaBacSi())) {
            android.util.Log.w("DanhSachTinNhan", "Message missing maBacSi");
            return false;
        }
        
        // Kiểm tra nội dung tin nhắn không rỗng
        if (android.text.TextUtils.isEmpty(tinNhan.getNoiDung())) {
            android.util.Log.w("DanhSachTinNhan", "Empty message content filtered out");
            return false;
        }
        
        return true;
    }
    
    /**
     * Lấy tên bác sĩ từ collection BacSi và cập nhật vào danh sách cuộc trò chuyện
     */
    private void loadTenBacSiForConversations(List<CuocTroChuyenBenhNhan> danhSachCuocTroChuyenBenhNhan) {
        if (danhSachCuocTroChuyenBenhNhan.isEmpty()) {
            adapter.setData(danhSachCuocTroChuyenBenhNhan);
            showEmpty(true);
            return;
        }
        
        // Lấy danh sách mã bác sĩ duy nhất
        List<String> danhSachMaBacSi = new ArrayList<>();
        for (CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan : danhSachCuocTroChuyenBenhNhan) {
            if (!danhSachMaBacSi.contains(cuocTroChuyenBenhNhan.getMaBacSi())) {
                danhSachMaBacSi.add(cuocTroChuyenBenhNhan.getMaBacSi());
            }
        }
        
        // Lấy thông tin bác sĩ từ Firestore
        FirebaseFirestore.getInstance()
            .collection("BacSi")
            .whereIn("maBacSi", danhSachMaBacSi)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Map<String, String> mapTenBacSi = new HashMap<>();
                
                // Tạo map mã bác sĩ -> tên bác sĩ
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String maBacSi = doc.getString("maBacSi");
                    String tenBacSi = doc.getString("hoTen");
                    if (maBacSi != null && tenBacSi != null) {
                        mapTenBacSi.put(maBacSi, "BS. " + tenBacSi);
                    }
                }
                
                // Cập nhật tên bác sĩ vào danh sách cuộc trò chuyện
                for (CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan : danhSachCuocTroChuyenBenhNhan) {
                    String tenBacSi = mapTenBacSi.get(cuocTroChuyenBenhNhan.getMaBacSi());
                    if (tenBacSi != null) {
                        cuocTroChuyenBenhNhan.setTenBacSi(tenBacSi);
                    } else {
                        // Nếu không tìm thấy tên, hiển thị mã bác sĩ
                        cuocTroChuyenBenhNhan.setTenBacSi("BS. " + cuocTroChuyenBenhNhan.getMaBacSi());
                    }
                }
                
                // Sắp xếp lại theo thời gian
                danhSachCuocTroChuyenBenhNhan.sort((c1, c2) -> {
                    if (c1.getThoiGianCuoi() == null) return 1;
                    if (c2.getThoiGianCuoi() == null) return -1;
                    return c2.getThoiGianCuoi().compareTo(c1.getThoiGianCuoi());
                });
                
                // Cập nhật adapter
                adapter.setData(danhSachCuocTroChuyenBenhNhan);
                showEmpty(danhSachCuocTroChuyenBenhNhan.isEmpty());
                
                android.util.Log.d("DanhSachTinNhan", "Loaded " + danhSachCuocTroChuyenBenhNhan.size() + " conversations for patient: " + maBenhNhan);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("DanhSachTinNhan", "Lỗi tải tên bác sĩ: " + e.getMessage());
                
                // Nếu lỗi, vẫn hiển thị danh sách với tên mặc định
                for (CuocTroChuyenBenhNhan cuocTroChuyenBenhNhan : danhSachCuocTroChuyenBenhNhan) {
                    cuocTroChuyenBenhNhan.setTenBacSi("BS. " + cuocTroChuyenBenhNhan.getMaBacSi());
                }
                
                adapter.setData(danhSachCuocTroChuyenBenhNhan);
                showEmpty(danhSachCuocTroChuyenBenhNhan.isEmpty());
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