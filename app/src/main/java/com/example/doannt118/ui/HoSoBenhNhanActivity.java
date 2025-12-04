package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;

import java.util.ArrayList;
import java.util.List;

public class HoSoBenhNhanActivity extends AppCompatActivity {

    private static final String TAG = "HoSoBenhNhan";
    
    private Toolbar toolbar;
    private ImageView imgAvatar;
    private TextView tvTenBenhNhan, tvNgaySinh, tvSoDienThoai, tvDiaChi;
    private TextView tvSoLanKham;
    private View tvEmptyLichSu;
    private RecyclerView rvLichSuKham;
    private Button btnTaoBenhAn, btnKeDonThuoc;
    
    private FirestoreRepository repo;
    private LichSuKhamAdapter lichSuAdapter;
    
    private String maBenhNhan;
    private String maMaKham;
    private String maLichKham;
    private BenhNhan benhNhan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ho_so_benh_nhan);

        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        maMaKham = getIntent().getStringExtra("MA_MA_KHAM");
        maLichKham = getIntent().getStringExtra("MA_LICH_KHAM");
        
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã bệnh nhân", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        repo = new FirestoreRepository();
        initViews();
        setupRecyclerViews();
        setupListeners();
        loadData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgAvatar = findViewById(R.id.imgAvatar);
        tvTenBenhNhan = findViewById(R.id.tvTenBenhNhan);
        tvNgaySinh = findViewById(R.id.tvNgaySinh);
        tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvSoLanKham = findViewById(R.id.tvSoLanKham);
        tvEmptyLichSu = findViewById(R.id.tvEmptyLichSu);
        rvLichSuKham = findViewById(R.id.rvLichSuKham);
        btnTaoBenhAn = findViewById(R.id.btnTaoBenhAn);
        btnKeDonThuoc = findViewById(R.id.btnKeDonThuoc);
        
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Lịch sử khám
        lichSuAdapter = new LichSuKhamAdapter(this, new ArrayList<>(), benhAn -> {
            // Xem chi tiết bệnh án
            Intent intent = new Intent(this, ChiTietBenhAnActivity.class);
            intent.putExtra("MA_BENH_AN", benhAn.getMaBenhAn());
            startActivity(intent);
        });
        rvLichSuKham.setLayoutManager(new LinearLayoutManager(this));
        rvLichSuKham.setAdapter(lichSuAdapter);
    }

    private void setupListeners() {
        btnTaoBenhAn.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaoBenhAnActivity.class);
            intent.putExtra("MA_BENH_NHAN", maBenhNhan);
            intent.putExtra("MA_LICH_KHAM", maLichKham);
            intent.putExtra("MA_MA_KHAM", maMaKham);
            startActivity(intent);
        });
        
        // Ẩn button kê đơn thuốc - đã tích hợp vào tạo bệnh án
        btnKeDonThuoc.setVisibility(View.GONE);
    }

    private void loadData() {
        loadThongTinBenhNhan();
        loadLichSuKham();
    }

    private void loadThongTinBenhNhan() {
        repo.getCollection("BenhNhan")
            .document(maBenhNhan)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                benhNhan = documentSnapshot.toObject(BenhNhan.class);
                if (benhNhan != null) {
                    hienThiThongTinBenhNhan();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi load bệnh nhân", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void hienThiThongTinBenhNhan() {
        tvTenBenhNhan.setText(benhNhan.getHoTen());
        tvNgaySinh.setText(benhNhan.getNgaySinh());
        tvSoDienThoai.setText(benhNhan.getSoDienThoai());
        tvDiaChi.setText(benhNhan.getDiaChi() != null ? benhNhan.getDiaChi() : "Chưa cập nhật");
        
        if (benhNhan.getAvatarUrl() != null && !benhNhan.getAvatarUrl().isEmpty()) {
            Glide.with(this)
                .load(benhNhan.getAvatarUrl())
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .circleCrop()
                .into(imgAvatar);
        }
    }

    private void loadLichSuKham() {
        repo.getCollection("BenhAn")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<BenhAn> danhSach = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    BenhAn benhAn = doc.toObject(BenhAn.class);
                    if (benhAn != null) {
                        danhSach.add(benhAn);
                    }
                }
                
                // Sort trong code thay vì dùng orderBy
                danhSach.sort((a, b) -> {
                    if (a.getNgayKhamAsTimestamp() == null) return 1;
                    if (b.getNgayKhamAsTimestamp() == null) return -1;
                    return b.getNgayKhamAsTimestamp().compareTo(a.getNgayKhamAsTimestamp());
                });
                
                // Giới hạn 10 bản ghi
                if (danhSach.size() > 10) {
                    danhSach = danhSach.subList(0, 10);
                }
                
                tvSoLanKham.setText(danhSach.size() + " lần");
                
                if (danhSach.isEmpty()) {
                    tvEmptyLichSu.setVisibility(View.VISIBLE);
                    rvLichSuKham.setVisibility(View.GONE);
                } else {
                    tvEmptyLichSu.setVisibility(View.GONE);
                    rvLichSuKham.setVisibility(View.VISIBLE);
                    lichSuAdapter.updateData(danhSach);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Lỗi load lịch sử", e);
                tvEmptyLichSu.setVisibility(View.VISIBLE);
                rvLichSuKham.setVisibility(View.GONE);
            });
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Reload data khi quay lại màn hình
        loadLichSuKham();
    }
}
