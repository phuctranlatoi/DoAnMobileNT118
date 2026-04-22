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
        // Sử dụng conversationId để đảm bảo tính nhất quán
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
            
            // Đếm tin nhắn chưa đọc và tìm tin nhắn cuối theo conversationId
            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                TinNhanBacSi tinNhan = doc.toObject(TinNhanBacSi.class);
                if (tinNhan != null && validateMessageForDoctor(tinNhan, maBacSi)) {
                    String maBenhNhan = tinNhan.getMaBenhNhan();
                    String conversationId = tinNhan.getConversationId();
                    
                    // Sử dụng conversationId làm key nếu có, nếu không thì dùng maBenhNhan
                    String key = conversationId != null ? conversationId : maBenhNhan;
                    
                    // Cập nhật tin nhắn cuối (theo thời gian)
                    TinNhanBacSi tinNhanCuoi = mapTinNhanCuoi.get(key);
                    if (tinNhanCuoi == null || 
                        (tinNhan.getThoiGianGui() != null && tinNhanCuoi.getThoiGianGui() != null &&
                         tinNhan.getThoiGianGui().compareTo(tinNhanCuoi.getThoiGianGui()) > 0)) {
                        
                        // Debug log
                        android.util.Log.d("DanhSachTinNhanBacSi", "Updating last message for " + key + 
                            ": " + tinNhan.getNoiDung().substring(0, Math.min(50, tinNhan.getNoiDung().length())) + 
                            " at " + tinNhan.getThoiGianGui());
                        
                        mapTinNhanCuoi.put(key, tinNhan);
                    } else if (tinNhanCuoi != null) {
                        // Debug log cho tin nhắn bị bỏ qua
                        android.util.Log.d("DanhSachTinNhanBacSi", "Skipping older message for " + key + 
                            ": " + tinNhan.getNoiDung().substring(0, Math.min(50, tinNhan.getNoiDung().length())) + 
                            " at " + tinNhan.getThoiGianGui() + 
                            " (current last: " + tinNhanCuoi.getThoiGianGui() + ")");
                    }
                    
                    // Đếm tin nhắn chưa đọc (tin nhắn từ bệnh nhân mà bác sĩ chưa đọc)
                    if (tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN &&
                        tinNhan.getTrangThai() != TinNhanBacSi.TrangThaiTinNhan.DA_XEM) {
                        mapSoTinNhanChuaDoc.put(key, 
                            mapSoTinNhanChuaDoc.getOrDefault(key, 0) + 1);
                    }
                }
            }
            
            // Tạo danh sách cuộc trò chuyện và lấy tên bệnh nhân
            for (Map.Entry<String, TinNhanBacSi> entry : mapTinNhanCuoi.entrySet()) {
                String key = entry.getKey();
                TinNhanBacSi tinNhanCuoi = entry.getValue();
                String maBenhNhan = tinNhanCuoi.getMaBenhNhan();
                
                boolean laBacSiGuiCuoi = tinNhanCuoi.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BAC_SI;
                
                // Tạo cuộc trò chuyện với tên tạm thời, sẽ cập nhật sau
                CuocTroChuyenBacSi cuocTroChuyenBacSi = new CuocTroChuyenBacSi(
                    maBenhNhan,
                    "Đang tải...", // Tên tạm thời
                    tinNhanCuoi.getNoiDung(),
                    tinNhanCuoi.getThoiGianGui(),
                    laBacSiGuiCuoi
                );
                
                // Set số tin nhắn chưa đọc
                cuocTroChuyenBacSi.setSoTinNhanChuaDoc(
                    mapSoTinNhanChuaDoc.getOrDefault(key, 0));
                
                // Set trạng thái tin nhắn cuối
                cuocTroChuyenBacSi.setTrangThaiTinNhanCuoi(tinNhanCuoi.getTrangThai());
                
                mapCuocTroChuyenBacSi.put(maBenhNhan, cuocTroChuyenBacSi);
            }
            
            // Lấy tên bệnh nhân từ collection BenhNhan
            loadTenBenhNhanForConversations(new ArrayList<>(mapCuocTroChuyenBacSi.values()));
            
            // Chuyển map thành list và sắp xếp theo thời gian
            List<CuocTroChuyenBacSi> danhSachCuocTroChuyenBacSi = new ArrayList<>(mapCuocTroChuyenBacSi.values());
            danhSachCuocTroChuyenBacSi.sort((c1, c2) -> {
                if (c1.getThoiGianCuoi() == null) return 1;
                if (c2.getThoiGianCuoi() == null) return -1;
                return c2.getThoiGianCuoi().compareTo(c1.getThoiGianCuoi());
            });
            
                adapter.setData(danhSachCuocTroChuyenBacSi);
                showEmpty(danhSachCuocTroChuyenBacSi.isEmpty());
                
                android.util.Log.d("DanhSachTinNhanBacSi", "Loaded " + danhSachCuocTroChuyenBacSi.size() + " conversations for doctor: " + maBacSi);
            } else {
                showEmpty(true);
            }
        });
    }
    
    /**
     * Validate tin nhắn cho bác sĩ để đảm bảo không bị lộn xộn
     */
    private boolean validateMessageForDoctor(TinNhanBacSi tinNhan, String expectedMaBacSi) {
        if (tinNhan == null) return false;
        
        // Kiểm tra mã bác sĩ
        if (!expectedMaBacSi.equals(tinNhan.getMaBacSi())) {
            android.util.Log.w("DanhSachTinNhanBacSi", "Message validation failed - Expected maBacSi: " + 
                expectedMaBacSi + ", Got: " + tinNhan.getMaBacSi());
            return false;
        }
        
//        // Kiểm tra có mã bệnh nhân
//        if (TextUtils.isEmpty(tinNhan.getMaBenhNhan())) {
//            android.util.Log.w("DanhSachTinNhanBacSi", "Message missing maBenhNhan");
//            return false;
//        }
//
//        // Kiểm tra nội dung tin nhắn không rỗng
//        if (TextUtils.isEmpty(tinNhan.getNoiDung())) {
//            android.util.Log.w("DanhSachTinNhanBacSi", "Empty message content filtered out");
//            return false;
//        }
        
        return true;
    }
    
    /**
     * Lấy tên bệnh nhân từ collection BenhNhan và cập nhật vào danh sách cuộc trò chuyện
     */
    private void loadTenBenhNhanForConversations(List<CuocTroChuyenBacSi> danhSachCuocTroChuyenBacSi) {
        if (danhSachCuocTroChuyenBacSi.isEmpty()) {
            adapter.setData(danhSachCuocTroChuyenBacSi);
            showEmpty(true);
            return;
        }
        
        // Lấy danh sách mã bệnh nhân duy nhất
        List<String> danhSachMaBenhNhan = new ArrayList<>();
        for (CuocTroChuyenBacSi cuocTroChuyenBacSi : danhSachCuocTroChuyenBacSi) {
            if (!danhSachMaBenhNhan.contains(cuocTroChuyenBacSi.getMaBenhNhan())) {
                danhSachMaBenhNhan.add(cuocTroChuyenBacSi.getMaBenhNhan());
            }
        }
        
        // Lấy thông tin bệnh nhân từ Firestore
        FirebaseFirestore.getInstance()
            .collection("BenhNhan")
            .whereIn("maBenhNhan", danhSachMaBenhNhan)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Map<String, String> mapTenBenhNhan = new HashMap<>();
                
                // Tạo map mã bệnh nhân -> tên bệnh nhân
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String maBenhNhan = doc.getString("maBenhNhan");
                    String tenBenhNhan = doc.getString("hoTen");
                    if (maBenhNhan != null && tenBenhNhan != null) {
                        mapTenBenhNhan.put(maBenhNhan, tenBenhNhan);
                    }
                }
                
                // Cập nhật tên bệnh nhân vào danh sách cuộc trò chuyện
                for (CuocTroChuyenBacSi cuocTroChuyenBacSi : danhSachCuocTroChuyenBacSi) {
                    String tenBenhNhan = mapTenBenhNhan.get(cuocTroChuyenBacSi.getMaBenhNhan());
                    if (tenBenhNhan != null) {
                        cuocTroChuyenBacSi.setTenBenhNhan(tenBenhNhan);
                    } else {
                        // Nếu không tìm thấy tên, hiển thị mã bệnh nhân
                        cuocTroChuyenBacSi.setTenBenhNhan("Bệnh nhân " + cuocTroChuyenBacSi.getMaBenhNhan());
                    }
                }
                
                // Sắp xếp lại theo thời gian
                danhSachCuocTroChuyenBacSi.sort((c1, c2) -> {
                    if (c1.getThoiGianCuoi() == null) return 1;
                    if (c2.getThoiGianCuoi() == null) return -1;
                    return c2.getThoiGianCuoi().compareTo(c1.getThoiGianCuoi());
                });
                
                // Cập nhật adapter
                adapter.setData(danhSachCuocTroChuyenBacSi);
                showEmpty(danhSachCuocTroChuyenBacSi.isEmpty());
                
                android.util.Log.d("DanhSachTinNhanBacSi", "Loaded " + danhSachCuocTroChuyenBacSi.size() + " conversations for doctor: " + maBacSi);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("DanhSachTinNhanBacSi", "Lỗi tải tên bệnh nhân: " + e.getMessage());
                
                // Nếu lỗi, vẫn hiển thị danh sách với tên mặc định
                for (CuocTroChuyenBacSi cuocTroChuyenBacSi : danhSachCuocTroChuyenBacSi) {
                    cuocTroChuyenBacSi.setTenBenhNhan("Bệnh nhân " + cuocTroChuyenBacSi.getMaBenhNhan());
                }
                
                adapter.setData(danhSachCuocTroChuyenBacSi);
                showEmpty(danhSachCuocTroChuyenBacSi.isEmpty());
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