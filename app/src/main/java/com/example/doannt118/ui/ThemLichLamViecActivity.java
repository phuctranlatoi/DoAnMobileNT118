package com.example.doannt118.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ThemLichLamViecActivity extends AppCompatActivity {

    private static final String TAG = "ThemLichLamViec";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private ImageView btnBack;
    private TextView tvTitle, tvGioBatDau, tvGioKetThuc, lblThongBao;
    private DatePicker dpNgayLamViec;
    private Button btnChonGioBatDau, btnChonGioKetThuc, btnHuy, btnLuu;
    private TextInputEditText edtGhiChu;
    private ProgressBar progressBar;

    private FirestoreRepository repo;
    private String maBacSi;
    private String maLichLamViec; // Null nếu thêm mới, có giá trị nếu sửa
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_lich_lam_viec);

        initViews();
        initData();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvGioBatDau = findViewById(R.id.tvGioBatDau);
        tvGioKetThuc = findViewById(R.id.tvGioKetThuc);
        lblThongBao = findViewById(R.id.lblThongBao);
        dpNgayLamViec = findViewById(R.id.dpNgayLamViec);
        btnChonGioBatDau = findViewById(R.id.btnChonGioBatDau);
        btnChonGioKetThuc = findViewById(R.id.btnChonGioKetThuc);
        btnHuy = findViewById(R.id.btnHuy);
        btnLuu = findViewById(R.id.btnLuu);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initData() {
        repo = new FirestoreRepository();
        
        // Lấy dữ liệu từ Intent
        maBacSi = getIntent().getStringExtra("maBacSi");
        maLichLamViec = getIntent().getStringExtra("maLichLamViec");
        
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Nếu là chế độ sửa
        if (maLichLamViec != null && !maLichLamViec.isEmpty()) {
            tvTitle.setText("Sửa lịch làm việc");
            btnLuu.setText("✓ Cập nhật");
            loadLichLamViec();
        } else {
            tvTitle.setText("Thêm lịch làm việc");
            btnLuu.setText("✓ Lưu lịch làm việc");
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveLichLamViec());
        
        btnChonGioBatDau.setOnClickListener(v -> showTimePickerDialog(true));
        btnChonGioKetThuc.setOnClickListener(v -> showTimePickerDialog(false));
    }

    private void showTimePickerDialog(boolean isStartTime) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, selectedHour, selectedMinute) -> {
                LocalTime time = LocalTime.of(selectedHour, selectedMinute);
                if (isStartTime) {
                    selectedStartTime = time;
                    tvGioBatDau.setText(time.format(TIME_FORMATTER));
                } else {
                    selectedEndTime = time;
                    tvGioKetThuc.setText(time.format(TIME_FORMATTER));
                }
                lblThongBao.setVisibility(View.GONE);
            },
            hour,
            minute,
            true
        );
        timePickerDialog.show();
    }

    private void loadLichLamViec() {
        progressBar.setVisibility(View.VISIBLE);
        
        repo.getByField("LichLamViec", "maLichLamViec", maLichLamViec,
            querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                
                if (!querySnapshot.isEmpty()) {
                    LichLamViec lich = querySnapshot.getDocuments().get(0).toObject(LichLamViec.class);
                    if (lich != null) {
                        fillFormWithData(lich);
                    }
                } else {
                    Toast.makeText(this, "Không tìm thấy lịch làm việc", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi tải lịch làm việc", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void fillFormWithData(LichLamViec lich) {
        // Set ngày
        if (lich.getNgayLamViec() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(lich.getNgayLamViec());
            dpNgayLamViec.updateDate(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            );
        }

        // Set giờ
        if (lich.getCaLamViec() != null && lich.getCaLamViec().contains("-")) {
            String[] parts = lich.getCaLamViec().split("-");
            if (parts.length == 2) {
                try {
                    selectedStartTime = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
                    selectedEndTime = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
                    tvGioBatDau.setText(selectedStartTime.format(TIME_FORMATTER));
                    tvGioKetThuc.setText(selectedEndTime.format(TIME_FORMATTER));
                } catch (Exception e) {
                    Log.e(TAG, "Lỗi parse giờ", e);
                }
            }
        }

        // Set ghi chú
        String ghiChu = lich.getGhiChu();
        if (ghiChu != null) {
            edtGhiChu.setText(ghiChu);
        }
    }

    private void saveLichLamViec() {
        if (!validateInput()) {
            return;
        }

        // Lấy ngày
        int day = dpNgayLamViec.getDayOfMonth();
        int month = dpNgayLamViec.getMonth();
        int year = dpNgayLamViec.getYear();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date ngayLamViec = calendar.getTime();

        // Tạo ca làm việc
        String caLamViec = String.format("%s-%s",
            selectedStartTime.format(TIME_FORMATTER),
            selectedEndTime.format(TIME_FORMATTER));

        // Lấy ghi chú
        String ghiChu = edtGhiChu.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);
        
        // Kiểm tra trùng lặp lịch làm việc (UC009 bước 3.1.4 và 3.3.4)
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(ngayLamViec);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startDate = startCal.getTime();
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(ngayLamViec);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endDate = endCal.getTime();
        
        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
            querySnapshot -> {
                // Kiểm tra trùng lặp
                for (var doc : querySnapshot.getDocuments()) {
                    String existingId = doc.getString("maLichLamViec");
                    String existingCa = doc.getString("caLamViec");
                    
                    // Nếu đang sửa, bỏ qua chính nó
                    if (maLichLamViec != null && maLichLamViec.equals(existingId)) {
                        continue;
                    }
                    
                    // Kiểm tra trùng ca làm việc
                    if (caLamViec.equals(existingCa)) {
                        progressBar.setVisibility(View.GONE);
                        showError("Lịch làm việc đã tồn tại cho ca này!");
                        return;
                    }
                    
                    // Kiểm tra trùng khung giờ (overlap)
                    if (existingCa != null && existingCa.contains("-")) {
                        String[] parts = existingCa.split("-");
                        if (parts.length == 2) {
                            try {
                                LocalTime existingStart = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
                                LocalTime existingEnd = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
                                
                                // Kiểm tra overlap: (start1 < end2) && (start2 < end1)
                                boolean overlap = selectedStartTime.isBefore(existingEnd) && 
                                                existingStart.isBefore(selectedEndTime);
                                
                                if (overlap) {
                                    progressBar.setVisibility(View.GONE);
                                    showError("Khung giờ bị trùng với lịch làm việc khác (" + existingCa + ")!");
                                    return;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi parse giờ existing", e);
                            }
                        }
                    }
                }
                
                // Không có trùng lặp, tiến hành lưu
                String id = (maLichLamViec != null && !maLichLamViec.isEmpty()) 
                    ? maLichLamViec 
                    : UUID.randomUUID().toString();

                LichLamViec lich = new LichLamViec(
                    id,
                    maBacSi,
                    ngayLamViec,
                    caLamViec,
                    1, // Không cần số lượng tối đa với TimeSlot system
                    "OFFLINE"
                );
                lich.setGhiChu(ghiChu);
                
                repo.addDocument("LichLamViec", id, lich,
                    aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        String message = (maLichLamViec != null) 
                            ? "Cập nhật lịch làm việc thành công!" 
                            : "Thêm lịch làm việc thành công!";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    },
                    e -> {
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Lỗi lưu lịch làm việc", e);
                        showError("Lỗi: " + e.getMessage());
                    });
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi kiểm tra trùng lặp", e);
                showError("Lỗi kiểm tra trùng lặp: " + e.getMessage());
            });
    }

    private boolean validateInput() {
        lblThongBao.setVisibility(View.GONE);

        if (selectedStartTime == null) {
            showError("Vui lòng chọn giờ bắt đầu!");
            return false;
        }

        if (selectedEndTime == null) {
            showError("Vui lòng chọn giờ kết thúc!");
            return false;
        }

        if (selectedStartTime.isAfter(selectedEndTime) || selectedStartTime.equals(selectedEndTime)) {
            showError("Giờ bắt đầu phải trước giờ kết thúc!");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        lblThongBao.setText(message);
        lblThongBao.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
