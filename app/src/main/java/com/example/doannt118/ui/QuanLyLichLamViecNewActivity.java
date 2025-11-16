package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QuanLyLichLamViecNewActivity extends AppCompatActivity {

    private static final String TAG = "QuanLyLichLamViecNew";
    
    private CalendarView calendarView;
    private RecyclerView rvLichLamViec;
    private TextView tvTongSoLich, tvTongBenhNhan, tvEmpty;
    private ProgressBar progressBar;
    private ImageView btnBack;
    
    private FirestoreRepository repo;
    private LichLamViecNewAdapter adapter;
    private String maTaiKhoan;
    private String maBacSi;
    private Date selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_lich_lam_viec_new);

        // Nhận dữ liệu từ Intent
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");

        if (maTaiKhoan == null || maBacSi == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo
        repo = new FirestoreRepository();
        selectedDate = new Date(); // Mặc định là hôm nay
        
        initViews();
        setupRecyclerView();
        setupListeners();
        
        // Load dữ liệu cho ngày hiện tại
        loadLichLamViec();
    }

    private void initViews() {
        calendarView = findViewById(R.id.calendarView);
        rvLichLamViec = findViewById(R.id.rvLichLamViec);
        tvTongSoLich = findViewById(R.id.tvTongSoLich);
        tvTongBenhNhan = findViewById(R.id.tvTongBenhNhan);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupRecyclerView() {
        adapter = new LichLamViecNewAdapter(this, new ArrayList<>());
        rvLichLamViec.setLayoutManager(new LinearLayoutManager(this));
        rvLichLamViec.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDate = calendar.getTime();
            
            Log.d(TAG, "Selected date: " + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate));
            loadLichLamViec();
        });
    }

    private void loadLichLamViec() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Tạo khoảng thời gian cho ngày được chọn
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(selectedDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startDate = startCal.getTime();

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(selectedDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endDate = endCal.getTime();

        Log.d(TAG, "Loading lịch làm việc from " + startDate + " to " + endDate);

        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
            querySnapshot -> {
                List<LichLamViec> danhSach = new ArrayList<>();
                
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    LichLamViec lichLamViec = doc.toObject(LichLamViec.class);
                    if (lichLamViec != null) {
                        danhSach.add(lichLamViec);
                    }
                }

                progressBar.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvTongSoLich.setText("0");
                    tvTongBenhNhan.setText("0");
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    tvTongSoLich.setText(String.valueOf(danhSach.size()));
                    loadTongBenhNhan(danhSach);
                }
                
                adapter.updateData(danhSach);
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch làm việc");
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi tải lịch làm việc", e);
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
                tvTongSoLich.setText("0");
                tvTongBenhNhan.setText("0");
            });
    }

    private void loadTongBenhNhan(List<LichLamViec> danhSachLichLamViec) {
        int[] totalCount = {0};
        int[] processedCount = {0};
        int totalLich = danhSachLichLamViec.size();

        if (totalLich == 0) {
            tvTongBenhNhan.setText("0");
            return;
        }

        for (LichLamViec lichLamViec : danhSachLichLamViec) {
            repo.getByField("LichKham", "maLichLamViec", lichLamViec.getMaLichLamViec(),
                querySnapshot -> {
                    int count = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String trangThai = doc.getString("trangThai");
                        // Chỉ đếm lịch khám chưa hủy
                        if (!"HUY".equals(trangThai)) {
                            count++;
                        }
                    }
                    totalCount[0] += count;
                    processedCount[0]++;
                    
                    // Cập nhật UI khi đã xử lý hết
                    if (processedCount[0] == totalLich) {
                        tvTongBenhNhan.setText(String.valueOf(totalCount[0]));
                    }
                },
                e -> {
                    processedCount[0]++;
                    Log.e(TAG, "Lỗi đếm bệnh nhân", e);
                    
                    if (processedCount[0] == totalLich) {
                        tvTongBenhNhan.setText(String.valueOf(totalCount[0]));
                    }
                });
        }
    }
}
