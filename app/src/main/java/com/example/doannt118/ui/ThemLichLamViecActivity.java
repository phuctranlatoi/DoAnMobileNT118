package com.example.doannt118.ui;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ThemLichLamViecActivity extends AppCompatActivity {

    private static final String TAG = "ThemLichLamViec";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));

    private ImageView btnBack;
    private TextView tvTitle, tvGioBatDau, tvGioKetThuc, lblThongBao, tvNgayDaChon, tvThoiLuong;
    private CalendarView calendarView;
    private LinearLayout btnChonGioBatDau, btnChonGioKetThuc, layoutThoiLuong;
    private TextView btnCaSang, btnCaTrua, btnCaToi;
    private Button btnHuy, btnLuu;
    private TextInputEditText edtGhiChu;
    private ProgressBar progressBar;
    private FrameLayout loadingOverlay;
    private CardView cardThongBao;

    private FirestoreRepository repo;
    private String maBacSi;
    private String maLichLamViec;
    private LocalTime selectedStartTime;
    private LocalTime selectedEndTime;
    private Date selectedDate;
    private int selectedCaIndex = -1; // -1 = custom, 0 = sáng, 1 = trưa, 2 = tối

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_them_lich_lam_viec);

            initViews();
            initData();
            setupListeners();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khởi tạo Activity", e);
            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        tvGioBatDau = findViewById(R.id.tvGioBatDau);
        tvGioKetThuc = findViewById(R.id.tvGioKetThuc);
        lblThongBao = findViewById(R.id.lblThongBao);
        tvNgayDaChon = findViewById(R.id.tvNgayDaChon);
        tvThoiLuong = findViewById(R.id.tvThoiLuong);
        calendarView = findViewById(R.id.calendarView);
        btnChonGioBatDau = findViewById(R.id.btnChonGioBatDau);
        btnChonGioKetThuc = findViewById(R.id.btnChonGioKetThuc);
        layoutThoiLuong = findViewById(R.id.layoutThoiLuong);
        btnCaSang = findViewById(R.id.btnCaSang);
        btnCaTrua = findViewById(R.id.btnCaTrua);
        btnCaToi = findViewById(R.id.btnCaToi);
        btnHuy = findViewById(R.id.btnHuy);
        btnLuu = findViewById(R.id.btnLuu);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        progressBar = findViewById(R.id.progressBar);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        cardThongBao = findViewById(R.id.cardThongBao);

        // Set min date to today
        calendarView.setMinDate(System.currentTimeMillis() - 1000);
        
        // Set default selected date to today
        selectedDate = new Date();
        updateSelectedDateDisplay();
    }

    private void initData() {
        repo = new FirestoreRepository();
        
        maBacSi = getIntent().getStringExtra("maBacSi");
        maLichLamViec = getIntent().getStringExtra("maLichLamViec");
        
        if (maBacSi == null || maBacSi.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có thông tin bác sĩ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (maLichLamViec != null && !maLichLamViec.isEmpty()) {
            tvTitle.setText("Sửa lịch làm việc");
            btnLuu.setText("Cập nhật");
            loadLichLamViec();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnHuy.setOnClickListener(v -> finish());
        btnLuu.setOnClickListener(v -> saveLichLamViec());
        
        // Calendar listener
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth, 0, 0, 0);
            cal.set(Calendar.MILLISECOND, 0);
            selectedDate = cal.getTime();
            updateSelectedDateDisplay();
        });
        
        // Time picker listeners
        btnChonGioBatDau.setOnClickListener(v -> showModernTimePicker(true));
        btnChonGioKetThuc.setOnClickListener(v -> showModernTimePicker(false));
        
        // Quick select ca listeners
        btnCaSang.setOnClickListener(v -> selectCa(0, 7, 0, 11, 0));
        btnCaTrua.setOnClickListener(v -> selectCa(1, 13, 0, 17, 0));
        btnCaToi.setOnClickListener(v -> selectCa(2, 18, 0, 22, 0));
    }

    private void updateSelectedDateDisplay() {
        if (selectedDate != null) {
            String formattedDate = DATE_FORMAT.format(selectedDate);
            // Capitalize first letter
            formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
            tvNgayDaChon.setText(formattedDate);
        }
    }

    private void selectCa(int caIndex, int startHour, int startMinute, int endHour, int endMinute) {
        selectedCaIndex = caIndex;
        selectedStartTime = LocalTime.of(startHour, startMinute);
        selectedEndTime = LocalTime.of(endHour, endMinute);
        
        tvGioBatDau.setText(selectedStartTime.format(TIME_FORMATTER));
        tvGioKetThuc.setText(selectedEndTime.format(TIME_FORMATTER));
        
        updateCaButtonStyles();
        updateDurationDisplay();
        hideError();
    }

    private void updateCaButtonStyles() {
        // Reset all buttons
        btnCaSang.setBackgroundResource(R.drawable.bg_time_slot_unselected);
        btnCaTrua.setBackgroundResource(R.drawable.bg_time_slot_unselected);
        btnCaToi.setBackgroundResource(R.drawable.bg_time_slot_unselected);
        
        // Highlight selected
        switch (selectedCaIndex) {
            case 0:
                btnCaSang.setBackgroundResource(R.drawable.bg_time_slot_selected);
                break;
            case 1:
                btnCaTrua.setBackgroundResource(R.drawable.bg_time_slot_selected);
                break;
            case 2:
                btnCaToi.setBackgroundResource(R.drawable.bg_time_slot_selected);
                break;
        }
    }

    private void showModernTimePicker(boolean isStartTime) {
        int hour = 8;
        int minute = 0;
        
        if (isStartTime && selectedStartTime != null) {
            hour = selectedStartTime.getHour();
            minute = selectedStartTime.getMinute();
        } else if (!isStartTime && selectedEndTime != null) {
            hour = selectedEndTime.getHour();
            minute = selectedEndTime.getMinute();
        }

        // Sử dụng style spinner (kiểu kéo cuộn như báo thức)
        TimePickerDialog picker = new TimePickerDialog(
            this,
            android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
            (view, selectedHour, selectedMinute) -> {
                LocalTime time = LocalTime.of(selectedHour, selectedMinute);
                if (isStartTime) {
                    selectedStartTime = time;
                    tvGioBatDau.setText(time.format(TIME_FORMATTER));
                } else {
                    selectedEndTime = time;
                    tvGioKetThuc.setText(time.format(TIME_FORMATTER));
                }
                
                // Reset ca selection when custom time is picked
                selectedCaIndex = -1;
                updateCaButtonStyles();
                updateDurationDisplay();
                hideError();
            },
            hour,
            minute,
            true
        );
        picker.setTitle(isStartTime ? "Chọn giờ bắt đầu" : "Chọn giờ kết thúc");
        picker.show();
    }

    private void updateDurationDisplay() {
        if (selectedStartTime != null && selectedEndTime != null) {
            long minutes = java.time.Duration.between(selectedStartTime, selectedEndTime).toMinutes();
            if (minutes > 0) {
                long hours = minutes / 60;
                long mins = minutes % 60;
                String durationText;
                if (mins == 0) {
                    durationText = "Thời lượng: " + hours + " giờ";
                } else {
                    durationText = "Thời lượng: " + hours + " giờ " + mins + " phút";
                }
                tvThoiLuong.setText(durationText);
                layoutThoiLuong.setVisibility(View.VISIBLE);
            } else {
                layoutThoiLuong.setVisibility(View.GONE);
            }
        } else {
            layoutThoiLuong.setVisibility(View.GONE);
        }
    }

    private void loadLichLamViec() {
        showLoading(true);
        
        repo.getByField("LichLamViec", "maLichLamViec", maLichLamViec,
            querySnapshot -> {
                showLoading(false);
                
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
                showLoading(false);
                Log.e(TAG, "Lỗi tải lịch làm việc", e);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void fillFormWithData(LichLamViec lich) {
        // Set ngày
        if (lich.getNgayLamViec() != null) {
            selectedDate = lich.getNgayLamViec();
            calendarView.setDate(selectedDate.getTime());
            updateSelectedDateDisplay();
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
                    updateDurationDisplay();
                    
                    // Check if matches a preset ca
                    checkAndSelectPresetCa();
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

    private void checkAndSelectPresetCa() {
        if (selectedStartTime == null || selectedEndTime == null) return;
        
        if (selectedStartTime.equals(LocalTime.of(7, 0)) && selectedEndTime.equals(LocalTime.of(11, 0))) {
            selectedCaIndex = 0;
        } else if (selectedStartTime.equals(LocalTime.of(13, 0)) && selectedEndTime.equals(LocalTime.of(17, 0))) {
            selectedCaIndex = 1;
        } else if (selectedStartTime.equals(LocalTime.of(18, 0)) && selectedEndTime.equals(LocalTime.of(22, 0))) {
            selectedCaIndex = 2;
        } else {
            selectedCaIndex = -1;
        }
        updateCaButtonStyles();
    }

    private void saveLichLamViec() {
        if (!validateInput()) {
            return;
        }

        // Lấy ngày
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date ngayLamViec = calendar.getTime();

        // Tạo ca làm việc
        String caLamViec = String.format("%s-%s",
            selectedStartTime.format(TIME_FORMATTER),
            selectedEndTime.format(TIME_FORMATTER));

        // Lấy ghi chú
        String ghiChu = edtGhiChu.getText().toString().trim();

        showLoading(true);
        
        // Kiểm tra trùng lặp
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(ngayLamViec);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        Date startDate = startCal.getTime();
        
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(ngayLamViec);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        Date endDate = endCal.getTime();
        
        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
            querySnapshot -> {
                for (var doc : querySnapshot.getDocuments()) {
                    String existingId = doc.getString("maLichLamViec");
                    String existingCa = doc.getString("caLamViec");
                    
                    if (maLichLamViec != null && maLichLamViec.equals(existingId)) {
                        continue;
                    }
                    
                    if (caLamViec.equals(existingCa)) {
                        showLoading(false);
                        showError("Lịch làm việc đã tồn tại cho ca này!");
                        return;
                    }
                    
                    if (existingCa != null && existingCa.contains("-")) {
                        String[] parts = existingCa.split("-");
                        if (parts.length == 2) {
                            try {
                                LocalTime existingStart = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
                                LocalTime existingEnd = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
                                
                                boolean overlap = selectedStartTime.isBefore(existingEnd) && 
                                                existingStart.isBefore(selectedEndTime);
                                
                                if (overlap) {
                                    showLoading(false);
                                    showError("Khung giờ bị trùng với lịch làm việc khác (" + existingCa + ")!");
                                    return;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Lỗi parse giờ existing", e);
                            }
                        }
                    }
                }
                
                String id = (maLichLamViec != null && !maLichLamViec.isEmpty()) 
                    ? maLichLamViec 
                    : UUID.randomUUID().toString();

                LichLamViec lich = new LichLamViec(
                    id,
                    maBacSi,
                    ngayLamViec,
                    caLamViec,
                    1,
                    "OFFLINE"
                );
                lich.setGhiChu(ghiChu);
                
                repo.addDocument("LichLamViec", id, lich,
                    aVoid -> {
                        showLoading(false);
                        String message = (maLichLamViec != null) 
                            ? "Cập nhật lịch làm việc thành công!" 
                            : "Thêm lịch làm việc thành công!";
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    },
                    e -> {
                        showLoading(false);
                        Log.e(TAG, "Lỗi lưu lịch làm việc", e);
                        showError("Lỗi: " + e.getMessage());
                    });
            },
            e -> {
                showLoading(false);
                Log.e(TAG, "Lỗi kiểm tra trùng lặp", e);
                showError("Lỗi kiểm tra trùng lặp: " + e.getMessage());
            });
    }

    private boolean validateInput() {
        hideError();

        if (selectedDate == null) {
            showError("Vui lòng chọn ngày làm việc!");
            return false;
        }

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
        cardThongBao.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        cardThongBao.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
