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
import com.example.doannt118.model.TimeSlot;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChiTietBacSiActivity extends AppCompatActivity {

    private static final String TAG = "ChiTietBacSi";
    
    private ImageView btnBack, ivAvatar;
    private TextView tvHoTen, tvKinhNghiem, tvChuyenKhoa, tvDiaChi, tvGioiThieu, tvSlotCount, tvBuoiKham;
    private TextView btnBuoiSang, btnBuoiChieu;
    private CalendarView calendarView;
    private RecyclerView rvKhungGio;
    private Button btnDatKham;
    
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBacSi;
    private String maBenhNhan;
    private Date selectedDate;
    private List<TimeSlot> timeSlotList = new ArrayList<>();
    private List<TimeSlot> allTimeSlots = new ArrayList<>(); // Lưu tất cả slots
    private TimeSlotAdapter timeSlotAdapter;
    private boolean isShowingMorning = true; // true = buổi sáng, false = buổi chiều

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_bac_si);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN"); // Nhận từ Intent

        if (maBacSi == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin bác sĩ!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadBacSiInfo();
        
        // Chỉ load maBenhNhan nếu chưa có
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            loadMaBenhNhan();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload time slots khi quay lại activity để đảm bảo dữ liệu mới nhất
        if (selectedDate != null) {
            Log.d(TAG, "onResume: Reloading time slots for selected date");
            loadTimeSlots();
        }
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Cũng reload khi activity start để đảm bảo dữ liệu đồng bộ
        if (selectedDate != null) {
            Log.d(TAG, "onStart: Reloading time slots for selected date");
            loadTimeSlots();
        }
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
        tvSlotCount = findViewById(R.id.tvSlotCount);
        tvBuoiKham = findViewById(R.id.tvBuoiKham);
        btnBuoiSang = findViewById(R.id.btnBuoiSang);
        btnBuoiChieu = findViewById(R.id.btnBuoiChieu);

        // Set min date to today
        calendarView.setMinDate(System.currentTimeMillis());
    }

    private void setupRecyclerView() {
        rvKhungGio.setLayoutManager(new GridLayoutManager(this, 3));
        timeSlotAdapter = new TimeSlotAdapter(timeSlotList, (timeSlot, position) -> {
            Log.d(TAG, "Selected time slot: " + timeSlot.getKhungGio());
        });
        rvKhungGio.setAdapter(timeSlotAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            selectedDate = calendar.getTime();
            loadTimeSlots();
        });

        btnDatKham.setOnClickListener(v -> handleDatKham());
        
        // Switch buổi sáng/chiều
        btnBuoiSang.setOnClickListener(v -> switchToBuoiSang());
        btnBuoiChieu.setOnClickListener(v -> switchToBuoiChieu());
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

    private void loadTimeSlots() {
        if (selectedDate == null) return;

        Log.d(TAG, "Loading time slots for date: " + selectedDate);
        timeSlotList.clear();
        
        // Tạo date range cho ngày được chọn
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

        // Load lịch làm việc thực tế từ Firestore
        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
                querySnapshot -> {
                    List<LichLamViec> lichLamViecList = new ArrayList<>();
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        LichLamViec lichLamViec = doc.toObject(LichLamViec.class);
                        if (lichLamViec != null) {
                            lichLamViecList.add(lichLamViec);
                        }
                    }
                    
                    if (lichLamViecList.isEmpty()) {
                        timeSlotList.clear();
                        allTimeSlots.clear();
                        timeSlotAdapter.updateTimeSlots(timeSlotList);
                        updateSlotCounter();
                        Toast.makeText(this, "Bác sĩ chưa đăng ký lịch làm việc cho ngày này!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Log.d(TAG, "Found " + lichLamViecList.size() + " work schedules");
                    
                    // Tạo time slots từ lịch làm việc
                    generateTimeSlotsFromSchedule(lichLamViecList);
                    
                    // Kiểm tra các slot đã được đặt
                    checkBookedSlots();
                },
                e -> {
                    Log.e(TAG, "Error loading lịch làm việc: ", e);
                    Toast.makeText(this, "Lỗi tải lịch làm việc!", Toast.LENGTH_SHORT).show();
                    timeSlotList.clear();
                    allTimeSlots.clear();
                    timeSlotAdapter.updateTimeSlots(timeSlotList);
                    updateSlotCounter();
                });
    }

    private void generateTimeSlotsFromSchedule(List<LichLamViec> lichLamViecList) {
        timeSlotList.clear();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        
        for (LichLamViec lichLamViec : lichLamViecList) {
            try {
                // Parse thời gian bắt đầu và kết thúc từ ca làm việc
                String[] caParts = lichLamViec.getCaLamViec().split("-");
                if (caParts.length != 2) continue;
                
                Date startTime = timeFormat.parse(caParts[0].trim());
                Date endTime = timeFormat.parse(caParts[1].trim());
                
                if (startTime == null || endTime == null) continue;
                
                Calendar startCal = Calendar.getInstance();
                startCal.setTime(startTime);
                
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(endTime);
                
                // Tạo các slot 30 phút
                Calendar currentSlot = Calendar.getInstance();
                currentSlot.set(Calendar.HOUR_OF_DAY, startCal.get(Calendar.HOUR_OF_DAY));
                currentSlot.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
                currentSlot.set(Calendar.SECOND, 0);
                currentSlot.set(Calendar.MILLISECOND, 0);
                
                while (currentSlot.get(Calendar.HOUR_OF_DAY) < endCal.get(Calendar.HOUR_OF_DAY) || 
                       (currentSlot.get(Calendar.HOUR_OF_DAY) == endCal.get(Calendar.HOUR_OF_DAY) && 
                        currentSlot.get(Calendar.MINUTE) < endCal.get(Calendar.MINUTE))) {
                    
                    // Tạo slot 30 phút
                    Calendar slotEnd = (Calendar) currentSlot.clone();
                    slotEnd.add(Calendar.MINUTE, 30);
                    
                    // Đảm bảo không vượt quá thời gian kết thúc
                    if (slotEnd.get(Calendar.HOUR_OF_DAY) > endCal.get(Calendar.HOUR_OF_DAY) || 
                        (slotEnd.get(Calendar.HOUR_OF_DAY) == endCal.get(Calendar.HOUR_OF_DAY) && 
                         slotEnd.get(Calendar.MINUTE) > endCal.get(Calendar.MINUTE))) {
                        break;
                    }
                    
                    String gioStart = String.format(Locale.getDefault(), "%02d:%02d", 
                        currentSlot.get(Calendar.HOUR_OF_DAY), currentSlot.get(Calendar.MINUTE));
                    String gioEnd = String.format(Locale.getDefault(), "%02d:%02d", 
                        slotEnd.get(Calendar.HOUR_OF_DAY), slotEnd.get(Calendar.MINUTE));
                    String khungGio = gioStart + "-" + gioEnd;
                    
                    // Tạo TimeSlot
                    String maTimeSlot = UUID.randomUUID().toString();
                    TimeSlot timeSlot = new TimeSlot(maTimeSlot, maBacSi, selectedDate, gioStart, gioEnd, khungGio);
                    
                    timeSlotList.add(timeSlot);
                    
                    // Chuyển sang slot tiếp theo
                    currentSlot.add(Calendar.MINUTE, 30);
                }
                
            } catch (ParseException e) {
                Log.e(TAG, "Error parsing time: " + lichLamViec.getCaLamViec(), e);
            }
        }
        
        Log.d(TAG, "Generated " + timeSlotList.size() + " time slots");
    }

    private void checkBookedSlots() {
        if (timeSlotList.isEmpty()) {
            allTimeSlots.clear();
            filterSlotsByTime();
            return;
        }
        
        // Tạo date range cho ngày được chọn
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
        
        // Lấy tất cả lịch khám đã đặt cho bác sĩ trong ngày
        repo.getByFieldAndDateRange("LichKham", "maBacSi", maBacSi, "ngayKham", startDate, endDate,
                querySnapshot -> {
                    List<LichKham> bookedAppointments = new ArrayList<>();
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        LichKham lichKham = doc.toObject(LichKham.class);
                        // QUAN TRỌNG: Kiểm tra tất cả trạng thái trừ "HUY"
                        // Bao gồm cả "CHO" (chờ xác nhận) và "XAC_NHAN" (đã xác nhận)
                        // Vì cả hai trạng thái này đều chiếm slot
                        if (lichKham != null && !"HUY".equals(lichKham.getTrangThai())) {
                            bookedAppointments.add(lichKham);
                            Log.d(TAG, "Booked slot found: " + lichKham.getGioKham() + " - Status: " + lichKham.getTrangThai());
                        }
                    }
                    
                    Log.d(TAG, "Total booked appointments: " + bookedAppointments.size());
                    
                    // Đánh dấu các slot đã được đặt TRƯỚC KHI lưu vào allTimeSlots
                    markBookedSlots(bookedAppointments);
                    
                    // Lưu tất cả slots (bao gồm cả đã đặt) vào allTimeSlots
                    allTimeSlots.clear();
                    allTimeSlots.addAll(timeSlotList);
                    
                    // Log để debug
                    int bookedCount = 0;
                    for (TimeSlot slot : allTimeSlots) {
                        if (slot.isBooked()) {
                            bookedCount++;
                            Log.d(TAG, "Slot marked as booked: " + slot.getKhungGio());
                        }
                    }
                    Log.d(TAG, "Total slots marked as booked: " + bookedCount + "/" + allTimeSlots.size());
                    
                    // Filter theo buổi hiện tại (sẽ tự động loại bỏ các slot đã đặt)
                    filterSlotsByTime();
                },
                e -> {
                    Log.e(TAG, "Error checking booked slots: ", e);
                    allTimeSlots.clear();
                    allTimeSlots.addAll(timeSlotList);
                    filterSlotsByTime();
                });
    }

    private void markBookedSlots(List<LichKham> bookedAppointments) {
        Log.d(TAG, "Marking booked slots for " + bookedAppointments.size() + " appointments");
        
        for (LichKham lichKham : bookedAppointments) {
            String gioKham = lichKham.getGioKham();
            if (gioKham != null && !gioKham.isEmpty()) {
                // Tìm slot tương ứng với giờ khám
                boolean found = false;
                for (TimeSlot slot : timeSlotList) {
                    if (gioKham.equals(slot.getKhungGio())) {
                        slot.setBooked(true);
                        slot.setMaBenhNhanDat(lichKham.getMaBenhNhan());
                        found = true;
                        Log.d(TAG, "Marked slot as booked: " + gioKham + " for patient: " + lichKham.getMaBenhNhan() + " (Status: " + lichKham.getTrangThai() + ")");
                        break;
                    }
                }
                
                if (!found) {
                    Log.w(TAG, "Could not find slot for booked time: " + gioKham);
                }
            } else {
                Log.w(TAG, "LichKham has empty gioKham: " + lichKham.getMaLichKham());
            }
        }
    }

    private void updateSlotCounter() {
        int currentBuoiSlots = timeSlotList.size(); // Slots của buổi hiện tại (chỉ tính slots trống)
        int totalMorningSlots = 0;
        int totalAfternoonSlots = 0;
        
        // Đếm tổng slots trống theo buổi từ allTimeSlots
        for (TimeSlot slot : allTimeSlots) {
            if (slot.isBooked()) continue; // Chỉ đếm slots trống
            
            try {
                String[] timeParts = slot.getGioStart().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                
                if (hour < 13) {
                    totalMorningSlots++;
                } else {
                    totalAfternoonSlots++;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing time for slot counter", e);
            }
        }
        
        Log.d(TAG, "Slot counter - Current buoi: " + currentBuoiSlots + ", Morning: " + totalMorningSlots + ", Afternoon: " + totalAfternoonSlots);
        
        if (currentBuoiSlots > 0) {
            tvSlotCount.setText(currentBuoiSlots + " slot");
            tvSlotCount.setVisibility(TextView.VISIBLE);
            
            // Hiển thị thông tin buổi khám
            String buoiInfo = isShowingMorning ? 
                "Buổi sáng: " + totalMorningSlots + " slot còn trống" :
                "Buổi chiều: " + totalAfternoonSlots + " slot còn trống";
            
            tvBuoiKham.setText(buoiInfo);
            tvBuoiKham.setVisibility(TextView.VISIBLE);
        } else {
            tvSlotCount.setVisibility(TextView.GONE);
            
            String buoiInfo = isShowingMorning ? 
                "Buổi sáng: Không có slot trống" :
                "Buổi chiều: Không có slot trống";
            
            tvBuoiKham.setText(buoiInfo);
            tvBuoiKham.setVisibility(TextView.VISIBLE);
        }
    }
    


    private void handleDatKham() {
        if (selectedDate == null) {
            Toast.makeText(this, "Vui lòng chọn ngày khám!", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeSlot selectedTimeSlot = timeSlotAdapter.getSelectedTimeSlot();
        if (selectedTimeSlot == null) {
            Toast.makeText(this, "Vui lòng chọn khung giờ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedTimeSlot.isBooked()) {
            Toast.makeText(this, "Khung giờ này đã được đặt!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (maBenhNhan == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo lịch khám mới
        String maLichKham = UUID.randomUUID().toString();
        LichKham lichKham = new LichKham();
        lichKham.setMaLichKham(maLichKham);
        lichKham.setMaBenhNhan(maBenhNhan);
        lichKham.setMaBacSi(maBacSi);
        lichKham.setNgayKham(new Timestamp(selectedDate));
        lichKham.setGioKham(selectedTimeSlot.getKhungGio()); // Thêm thông tin giờ khám
        lichKham.setTrangThai("CHO");

        repo.addDocument("LichKham", maLichKham, lichKham,
                aVoid -> {
                    Toast.makeText(this, "Đặt lịch thành công cho khung giờ " + selectedTimeSlot.getKhungGio(),
                            Toast.LENGTH_LONG).show();
                    
                    // QUAN TRỌNG: Đánh dấu slot đã được đặt ngay lập tức
                    selectedTimeSlot.setBooked(true);
                    selectedTimeSlot.setMaBenhNhanDat(maBenhNhan);
                    
                    // Cập nhật cả trong allTimeSlots để đảm bảo tính nhất quán
                    for (TimeSlot slot : allTimeSlots) {
                        if (slot.getKhungGio().equals(selectedTimeSlot.getKhungGio())) {
                            slot.setBooked(true);
                            slot.setMaBenhNhanDat(maBenhNhan);
                            break;
                        }
                    }
                    
                    // Cập nhật UI ngay lập tức
                    filterSlotsByTime(); // Sẽ loại bỏ slot vừa đặt khỏi danh sách hiển thị
                    
                    Log.d(TAG, "Successfully booked slot: " + selectedTimeSlot.getKhungGio() + " for patient: " + maBenhNhan);
                    
                    finish();
                },
                e -> {
                    Log.e(TAG, "Error adding lichKham: ", e);
                    Toast.makeText(this, "Đặt lịch thất bại!", Toast.LENGTH_SHORT).show();
                });
    }
    
    private void switchToBuoiSang() {
        if (isShowingMorning) return; // Đã đang hiển thị buổi sáng
        
        isShowingMorning = true;
        updateSwitchUI();
        filterSlotsByTime();
    }
    
    private void switchToBuoiChieu() {
        if (!isShowingMorning) return; // Đã đang hiển thị buổi chiều
        
        isShowingMorning = false;
        updateSwitchUI();
        filterSlotsByTime();
    }
    
    private void updateSwitchUI() {
        if (isShowingMorning) {
            // Buổi sáng được chọn
            btnBuoiSang.setBackgroundResource(R.drawable.bg_switch_selected);
            btnBuoiSang.setTextColor(getResources().getColor(R.color.white));
            btnBuoiChieu.setBackgroundResource(R.drawable.bg_switch_unselected);
            btnBuoiChieu.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            // Buổi chiều được chọn
            btnBuoiSang.setBackgroundResource(R.drawable.bg_switch_unselected);
            btnBuoiSang.setTextColor(getResources().getColor(R.color.colorPrimary));
            btnBuoiChieu.setBackgroundResource(R.drawable.bg_switch_selected);
            btnBuoiChieu.setTextColor(getResources().getColor(R.color.white));
        }
    }
    
    private void filterSlotsByTime() {
        timeSlotList.clear();
        
        Log.d(TAG, "Filtering slots by time. Morning: " + isShowingMorning + ", Total slots: " + allTimeSlots.size());
        
        int availableSlots = 0;
        int bookedSlots = 0;
        
        for (TimeSlot slot : allTimeSlots) {
            // QUAN TRỌNG: Bỏ qua tất cả slots đã được đặt (bao gồm CHO và XAC_NHAN)
            if (slot.isBooked()) {
                bookedSlots++;
                Log.d(TAG, "Skipping booked slot: " + slot.getKhungGio() + " (Patient: " + slot.getMaBenhNhanDat() + ")");
                continue; 
            }
            
            try {
                String[] timeParts = slot.getGioStart().split(":");
                int hour = Integer.parseInt(timeParts[0]);
                
                // Lọc theo buổi sáng (< 13h) hoặc buổi chiều (>= 13h)
                if (isShowingMorning && hour < 13) {
                    timeSlotList.add(slot);
                    availableSlots++;
                    Log.d(TAG, "Added morning slot: " + slot.getKhungGio());
                } else if (!isShowingMorning && hour >= 13) {
                    timeSlotList.add(slot);
                    availableSlots++;
                    Log.d(TAG, "Added afternoon slot: " + slot.getKhungGio());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing time for filtering: " + slot.getGioStart(), e);
            }
        }
        
        Log.d(TAG, "Filter result - Available: " + availableSlots + ", Booked: " + bookedSlots + ", Showing: " + timeSlotList.size());
        
        // Cập nhật adapter và counter
        timeSlotAdapter.updateTimeSlots(timeSlotList);
        updateSlotCounter();
    }
}
