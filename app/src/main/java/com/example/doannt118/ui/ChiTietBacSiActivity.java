package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ChiTietBacSiActivity extends AppCompatActivity {

    private static final String TAG = "ChiTietBacSi";
    
    private ImageView btnBack, ivAvatar;
    private TextView tvHoTen, tvKinhNghiem, tvChuyenKhoa, tvDiaChi, tvGioiThieu;
    private CalendarView calendarView;
    private RecyclerView rvKhungGio;
    private Button btnDatKham;
    
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBacSi;
    private String maBenhNhan;
    private Date selectedDate;
    private List<LichLamViec> khungGioList = new ArrayList<>();
    private KhungGioAdapter khungGioAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_bac_si);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");

        if (maBacSi == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin bác sĩ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadBacSiInfo();
        loadMaBenhNhan();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvHoTen = findViewById(R.id.tvHoTen);
        tvKinhNghiem = findViewById(R.id.tvKinhNghiem);
        tvChuyenKhoa = findViewById(R.id.tvChuyenKhoa);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvGioiThieu = findViewById(R.id.tvGioiThieu);
        calendarView = findViewById(R.id.calendarView);
        rvKhungGio = findViewById(R.id.rvKhungGio);
        btnDatKham = findViewById(R.id.btnDatKham);

        // Set min date to today
        calendarView.setMinDate(System.currentTimeMillis());
    }

    private void setupRecyclerView() {
        rvKhungGio.setLayoutManager(new GridLayoutManager(this, 3));
        khungGioAdapter = new KhungGioAdapter(khungGioList, (lichLamViec, position) -> {
            Log.d(TAG, "Selected: " + lichLamViec.getCaLamViec());
        });
        rvKhungGio.setAdapter(khungGioAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDate = calendar.getTime();
            loadKhungGio();
        });

        btnDatKham.setOnClickListener(v -> handleDatKham());
    }

    private void loadBacSiInfo() {
        repo.getByField("BacSi", "maBacSi", maBacSi,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSi != null) {
                            tvHoTen.setText(bacSi.getHoTen() != null ? bacSi.getHoTen() : "");
                            tvKinhNghiem.setText(bacSi.getNamKinhNghiem() > 0 ? 
                                bacSi.getNamKinhNghiem() + " năm kinh nghiệm" : "");
                            tvChuyenKhoa.setText(bacSi.getChuyenKhoa() != null ? bacSi.getChuyenKhoa() : "");
                            tvDiaChi.setText(bacSi.getDiaChi() != null ? bacSi.getDiaChi() : "");
                            tvGioiThieu.setText(bacSi.getGioiThieu() != null ? bacSi.getGioiThieu() : "");
                        }
                    }
                },
                e -> {
                    Log.e(TAG, "Error loading bác sĩ: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin bác sĩ!", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMaBenhNhan() {
        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        maBenhNhan = querySnapshot.getDocuments().get(0).getString("maBenhNhan");
                    }
                },
                e -> Log.e(TAG, "Error loading maBenhNhan: ", e));
    }

    private void loadKhungGio() {
        if (selectedDate == null) return;

        khungGioList.clear();
        
        // Tự động tạo lịch theo quy tắc
        Calendar cal = Calendar.getInstance();
        cal.setTime(selectedDate);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        
        // Thứ 7 = 7, Chủ nhật = 1, Thứ 2-6 = 2-6
        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
        
        if (isWeekend) {
            // Thứ 7, CN: 8h-11h sáng + 3h-10h chiều
            generateKhungGio(8, 11);  // Sáng
            generateKhungGio(15, 22); // Chiều
        } else {
            // Thứ 2-6: chỉ 3h-10h chiều
            generateKhungGio(15, 22);
        }
        
        khungGioAdapter.notifyDataSetChanged();
        
        if (khungGioList.isEmpty()) {
            Toast.makeText(this, "Không có khung giờ trống!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void generateKhungGio(int startHour, int endHour) {
        // Tạo các khung giờ 30 phút
        for (int hour = startHour; hour < endHour; hour++) {
            for (int minute = 0; minute < 60; minute += 30) {
                int endMinute = minute + 30;
                int endHourAdjusted = hour;
                
                if (endMinute >= 60) {
                    endMinute = 0;
                    endHourAdjusted = hour + 1;
                }
                
                if (endHourAdjusted > endHour || (endHourAdjusted == endHour && endMinute > 0)) {
                    break;
                }
                
                String caLamViec = String.format("%02d:%02d-%02d:%02d", 
                    hour, minute, endHourAdjusted, endMinute);
                
                // Tạo LichLamViec tạm thời (không lưu vào Firestore)
                LichLamViec lichLamViec = new LichLamViec();
                lichLamViec.setMaLichLamViec(UUID.randomUUID().toString());
                lichLamViec.setMaBacSi(maBacSi);
                lichLamViec.setNgayLamViec(selectedDate);
                lichLamViec.setCaLamViec(caLamViec);
                lichLamViec.setSoLuongToiDa(6);
                lichLamViec.setLoaiHinh("OFFLINE");
                
                khungGioList.add(lichLamViec);
            }
        }
    }

    private void handleDatKham() {
        if (selectedDate == null) {
            Toast.makeText(this, "Vui lòng chọn ngày khám!", Toast.LENGTH_SHORT).show();
            return;
        }

        LichLamViec selectedLichLamViec = khungGioAdapter.getSelectedItem();
        if (selectedLichLamViec == null) {
            Toast.makeText(this, "Vui lòng chọn khung giờ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (maBenhNhan == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Bước 1: Kiểm tra xem LichLamViec đã tồn tại chưa
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

        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
                querySnapshot -> {
                    String maLichLamViecToUse = null;
                    
                    // Tìm xem có lịch làm việc trùng ca không
                    for (var doc : querySnapshot.getDocuments()) {
                        LichLamViec existing = doc.toObject(LichLamViec.class);
                        if (existing != null && existing.getCaLamViec().equals(selectedLichLamViec.getCaLamViec())) {
                            maLichLamViecToUse = existing.getMaLichLamViec();
                            break;
                        }
                    }
                    
                    if (maLichLamViecToUse == null) {
                        // Chưa có → Tạo mới LichLamViec
                        repo.addDocument("LichLamViec", selectedLichLamViec.getMaLichLamViec(), selectedLichLamViec,
                                aVoid -> {
                                    // Sau khi tạo xong, tạo LichKham
                                    createLichKham(selectedLichLamViec.getMaLichLamViec());
                                },
                                e -> {
                                    Log.e(TAG, "Error creating LichLamViec: ", e);
                                    Toast.makeText(this, "Lỗi tạo lịch làm việc!", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Đã có → Dùng luôn
                        createLichKham(maLichLamViecToUse);
                    }
                },
                e -> {
                    Log.e(TAG, "Error checking LichLamViec: ", e);
                    Toast.makeText(this, "Lỗi kiểm tra lịch làm việc!", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void createLichKham(String maLichLamViec) {
        // Kiểm tra số lượng
        repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
                querySnapshot -> {
                    int soLuong = querySnapshot.size();
                    
                    if (soLuong >= 6) {
                        Toast.makeText(this, "Khung giờ này đã đầy!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Tạo lịch khám
                    String maLichKham = UUID.randomUUID().toString();
                    LichKham lichKham = new LichKham();
                    lichKham.setMaLichKham(maLichKham);
                    lichKham.setMaBenhNhan(maBenhNhan);
                    lichKham.setMaBacSi(maBacSi);
                    lichKham.setMaLichLamViec(maLichLamViec);
                    lichKham.setNgayKham(new Timestamp(selectedDate));
                    lichKham.setTrangThai("CHO");
                    lichKham.setSoThuTu(soLuong + 1);

                    repo.addDocument("LichKham", maLichKham, lichKham,
                            aVoid -> {
                                Toast.makeText(this, "Đặt lịch thành công! Số thứ tự: " + (soLuong + 1),
                                        Toast.LENGTH_LONG).show();
                                finish();
                            },
                            e -> {
                                Log.e(TAG, "Error adding lichKham: ", e);
                                Toast.makeText(this, "Đặt lịch thất bại!", Toast.LENGTH_SHORT).show();
                            });
                },
                e -> {
                    Log.e(TAG, "Error checking soLuong: ", e);
                    Toast.makeText(this, "Lỗi kiểm tra số lượng!", Toast.LENGTH_SHORT).show();
                });
    }
}
