package com.example.doannt118.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.utils.NotificationHelper;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class DangKyLichKhamActivity extends AppCompatActivity {

    private static final String TAG = "DangKyLichKham";
    
    private Button btnChonNgay, btnDangKy;
    private Spinner spinnerBacSi, spinnerKhungGio;
    private TextView tvThongBao;
    private ImageView btnBack;
    private RecyclerView rvLichKham;
    
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBenhNhan;
    private Date selectedDate;
    private Map<String, String> bacSiMap = new HashMap<>(); // Tên -> maBacSi
    private Map<String, String> khungGioMap = new HashMap<>(); // Khung giờ -> maLichLamViec
    private List<LichKham> lichKhamList = new ArrayList<>();
    private LichKhamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangky_lichkham);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadMaBenhNhan();
        setupClickListeners();
    }

    private void initViews() {
        btnChonNgay = findViewById(R.id.btnChonNgay);
        btnDangKy = findViewById(R.id.btnDangKy);
        spinnerBacSi = findViewById(R.id.spinnerBacSi);
        spinnerKhungGio = findViewById(R.id.spinnerKhungGio);
        tvThongBao = findViewById(R.id.tvThongBao);
        btnBack = findViewById(R.id.btnBack);
        rvLichKham = findViewById(R.id.rvLichKham);

        // Setup RecyclerView
        rvLichKham.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LichKhamAdapter(lichKhamList, this::handleHuyLichKham);
        rvLichKham.setAdapter(adapter);
    }

    private void loadMaBenhNhan() {
        com.example.doannt118.utils.UserInfoLoader.loadBenhNhan(maTaiKhoan, repo,
            new com.example.doannt118.utils.UserInfoLoader.BenhNhanCallback() {
                @Override
                public void onSuccess(com.example.doannt118.model.BenhNhan benhNhan) {
                    maBenhNhan = benhNhan.getMaBenhNhan();
                    loadLichKham();
                }
                
                @Override
                public void onError(String message) {
                    Toast.makeText(DangKyLichKhamActivity.this, message, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnChonNgay.setOnClickListener(v -> showDatePicker());

        spinnerBacSi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Bỏ qua item đầu tiên (placeholder)
                    loadKhungGio();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnDangKy.setOnClickListener(v -> handleDangKy());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    selectedDate = calendar.getTime();
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    btnChonNgay.setText(sdf.format(selectedDate));
                    
                    loadBacSi();
                }, year, month, day);

        // Chỉ cho phép chọn từ hôm nay trở đi
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void loadBacSi() {
        if (selectedDate == null) {
            showMessage("Vui lòng chọn ngày khám!");
            return;
        }

        bacSiMap.clear();
        
        // Tạo start và end date cho ngày được chọn
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
        
        Calendar now = Calendar.getInstance();
        boolean isToday = isSameDay(selectedDate, now.getTime());
        
        // Load tất cả lịch làm việc trong ngày được chọn
        repo.getCollection("LichLamViec")
                .whereGreaterThanOrEqualTo("ngayLamViec", startDate)
                .whereLessThanOrEqualTo("ngayLamViec", endDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Map<String, String> tempBacSiMap = new HashMap<>();
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String maBacSi = doc.getString("maBacSi");
                        String caLamViec = doc.getString("caLamViec");
                        
                        if (maBacSi == null || caLamViec == null) continue;
                        
                        // Nếu là hôm nay, bỏ qua khung giờ đã qua
                        if (isToday && isTimeSlotPassed(caLamViec)) {
                            continue;
                        }
                        
                        // Kiểm tra còn chỗ trống không
                        String maLichLamViec = doc.getString("maLichLamViec");
                        Long soLuongToiDaLong = doc.getLong("soLuongToiDa");
                        int soLuongToiDa = (soLuongToiDaLong != null) ? soLuongToiDaLong.intValue() : 10;
                        
                        // Đếm số lượng đã đăng ký cho lịch làm việc này
                        repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
                            lichKhamSnapshot -> {
                                int soLuongDaDangKy = 0;
                                for (var lkDoc : lichKhamSnapshot.getDocuments()) {
                                    String trangThai = lkDoc.getString("trangThai");
                                    // Chỉ đếm lịch chờ xác nhận và đã xác nhận
                                    if ("CHO".equals(trangThai) || "XAC_NHAN".equals(trangThai)) {
                                        soLuongDaDangKy++;
                                    }
                                }
                                
                                // Nếu còn chỗ trống, thêm bác sĩ vào danh sách
                                if (soLuongDaDangKy < soLuongToiDa) {
                                    if (!tempBacSiMap.containsValue(maBacSi)) {
                                        tempBacSiMap.put("temp_" + maBacSi, maBacSi);
                                        
                                        // Load tên bác sĩ
                                        repo.getByField("BacSi", "maBacSi", maBacSi,
                                            bacSiSnapshot -> {
                                                if (!bacSiSnapshot.isEmpty()) {
                                                    String hoTen = bacSiSnapshot.getDocuments().get(0).getString("hoTen");
                                                    String chuyenKhoa = bacSiSnapshot.getDocuments().get(0).getString("chuyenKhoa");
                                                    
                                                    if (hoTen != null && !bacSiMap.containsKey(hoTen)) {
                                                        bacSiMap.put(hoTen, maBacSi);
                                                        updateBacSiSpinnerFromMap();
                                                    }
                                                }
                                            },
                                            e -> Log.e(TAG, "Error loading bacSi: ", e));
                                    }
                                }
                            },
                            e -> Log.e(TAG, "Error counting lichKham: ", e)
                        );
                    }
                    
                    // Kiểm tra sau 1 giây xem có bác sĩ nào không
                    new android.os.Handler().postDelayed(() -> {
                        if (bacSiMap.isEmpty()) {
                            showMessage("Không có bác sĩ có lịch trống cho ngày này!");
                            updateBacSiSpinnerFromMap();
                        } else {
                            hideMessage();
                        }
                    }, 1000);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lichLamViec: ", e);
                    showMessage("Lỗi tải danh sách bác sĩ!");
                });
    }
    
    private void updateBacSiSpinnerFromMap() {
        runOnUiThread(() -> {
            List<String> bacSiList = new ArrayList<>();
            bacSiList.add("-- Chọn bác sĩ --");
            bacSiList.addAll(bacSiMap.keySet());
            
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, bacSiList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerBacSi.setAdapter(adapter);
        });
    }



    private void loadKhungGio() {
        if (selectedDate == null) {
            showMessage("Vui lòng chọn ngày khám!");
            return;
        }

        String selectedBacSi = spinnerBacSi.getSelectedItem().toString();
        if (selectedBacSi.equals("-- Chọn bác sĩ --")) {
            return;
        }

        String maBacSi = bacSiMap.get(selectedBacSi);
        khungGioMap.clear();

        // Tạo start và end date
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
        
        Calendar now = Calendar.getInstance();
        boolean isToday = isSameDay(selectedDate, now.getTime());
        
        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
                querySnapshot -> {
                    List<String> khungGioList = new ArrayList<>();
                    khungGioList.add("-- Chọn khung giờ --");
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String caLamViec = doc.getString("caLamViec");
                        String maLichLamViec = doc.getString("maLichLamViec");
                        
                        if (caLamViec == null || maLichLamViec == null) continue;
                        
                        // Kiểm tra nếu là hôm nay thì bỏ qua khung giờ đã qua
                        if (isToday && isTimeSlotPassed(caLamViec)) {
                            continue;
                        }
                        
                        // Lấy số lượng tối đa từ lịch làm việc
                        Long soLuongToiDaLong = doc.getLong("soLuongToiDa");
                        int soLuongToiDa = (soLuongToiDaLong != null) ? soLuongToiDaLong.intValue() : 6;
                        
                        // Đếm số lượng đã đăng ký
                        repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
                            lichKhamSnapshot -> {
                                int soLuongDaDangKy = lichKhamSnapshot.size();
                                int soLuongConTrong = soLuongToiDa - soLuongDaDangKy;
                                    
                                    if (soLuongConTrong > 0) {
                                        String displayText = caLamViec + " (Còn " + soLuongConTrong + "/" + soLuongToiDa + " chỗ)";
                                        khungGioMap.put(displayText, maLichLamViec);
                                        khungGioList.add(displayText);
                                        updateKhungGioSpinner(khungGioList);
                                    }
                                },
                                e -> Log.e(TAG, "Error counting lichKham: ", e)
                            );
                    }
                    
                    // Cập nhật spinner ngay cả khi không có khung giờ nào
                    if (khungGioList.size() == 1) {
                        showMessage("Bác sĩ không có khung giờ trống!");
                        updateKhungGioSpinner(khungGioList);
                    } else {
                        hideMessage();
                    }
                },
                e -> {
                    Log.e(TAG, "Error loading khungGio: ", e);
                    showMessage("Lỗi tải khung giờ!");
                });
    }
    
    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    private boolean isTimeSlotPassed(String caLamViec) {
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        
        // Parse khung giờ (ví dụ: "08:00 - 10:00")
        if (caLamViec.contains("-")) {
            String[] parts = caLamViec.split("-");
            if (parts.length > 0) {
                String startTime = parts[0].trim();
                String[] timeParts = startTime.split(":");
                if (timeParts.length > 0) {
                    try {
                        int slotHour = Integer.parseInt(timeParts[0]);
                        return currentHour >= slotHour;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                }
            }
        }
        return false;
    }
    
    private void updateKhungGioSpinner(List<String> khungGioList) {
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, khungGioList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerKhungGio.setAdapter(adapter);
        });
    }

    // Tiếp tục trong phần 2...

    private void handleDangKy() {
        if (!validateInput()) return;

        String selectedBacSi = spinnerBacSi.getSelectedItem().toString();
        String selectedKhungGio = spinnerKhungGio.getSelectedItem().toString();
        String maBacSi = bacSiMap.get(selectedBacSi);
        String maLichLamViec = khungGioMap.get(selectedKhungGio);

        // Lấy thông tin lịch làm việc để kiểm tra số lượng tối đa
        repo.getCollection("LichLamViec")
                .document(maLichLamViec)
                .get()
                .addOnSuccessListener(lichLamViecDoc -> {
                    if (!lichLamViecDoc.exists()) {
                        showMessage("Lịch làm việc không tồn tại!");
                        return;
                    }
                    
                    Long soLuongToiDaLong = lichLamViecDoc.getLong("soLuongToiDa");
                    int soLuongToiDa = (soLuongToiDaLong != null) ? soLuongToiDaLong.intValue() : 6;
                    
                    // Kiểm tra số lượng bệnh nhân đã đăng ký
                    repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
                        querySnapshot -> {
                            int soLuongHienTai = querySnapshot.size();
                            
                            if (soLuongHienTai >= soLuongToiDa) {
                                showMessage("Khung giờ này đã đầy!");
                                return;
                            }

                    // Tính số thứ tự
                    int soThuTu = soLuongHienTai + 1;

                    // Tạo lịch khám mới
                    String maLichKham = UUID.randomUUID().toString();
                    LichKham lichKham = new LichKham();
                    lichKham.setMaLichKham(maLichKham);
                    lichKham.setMaBenhNhan(maBenhNhan);
                    lichKham.setMaBacSi(maBacSi);
                    lichKham.setMaLichLamViec(maLichLamViec);
                    lichKham.setNgayKham(new Timestamp(selectedDate));
                    lichKham.setTrangThai("CHO");
                    lichKham.setSoThuTu(soThuTu);

                    // Lưu vào Firestore
                    repo.addDocument("LichKham", maLichKham, lichKham,
                            aVoid -> {
                                Toast.makeText(this, "Đăng ký thành công! Số thứ tự: " + soThuTu,
                                        Toast.LENGTH_LONG).show();
                                
                                // Gửi thông báo cho bác sĩ
                                guiThongBaoDangKyChoBS(maBacSi, selectedBacSi, selectedKhungGio);
                                
                                clearFields();
                                loadLichKham();
                            },
                            e -> {
                                Log.e(TAG, "Error adding lichKham: ", e);
                                Toast.makeText(this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                            });
                        },
                        e -> {
                            Log.e(TAG, "Error checking soLuong: ", e);
                            Toast.makeText(this, "Lỗi kiểm tra số lượng!", Toast.LENGTH_SHORT).show();
                        }
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lichLamViec: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin lịch làm việc!", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleHuyLichKham(LichKham lichKham) {
        if (!"CHO".equals(lichKham.getTrangThai())) {
            Toast.makeText(this, "Chỉ có thể hủy lịch ở trạng thái 'Chờ xác nhận'!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn hủy lịch khám này?")
                .setPositiveButton("Có", (dialog, which) -> {
                    repo.deleteDocument("LichKham", lichKham.getMaLichKham(),
                            aVoid -> {
                                Toast.makeText(this, "Hủy lịch khám thành công!",
                                        Toast.LENGTH_SHORT).show();
                                loadLichKham();
                            },
                            e -> {
                                Log.e(TAG, "Error deleting lichKham: ", e);
                                Toast.makeText(this, "Hủy lịch khám thất bại!",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void loadLichKham() {
        if (maBenhNhan == null) return;

        repo.getByField("LichKham", "maBenhNhan", maBenhNhan,
                querySnapshot -> {
                    lichKhamList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        LichKham lichKham = doc.toObject(LichKham.class);
                        if (lichKham != null) {
                            lichKhamList.add(lichKham);
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                e -> {
                    Log.e(TAG, "Error loading lichKham: ", e);
                    Toast.makeText(this, "Lỗi tải danh sách lịch khám!", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInput() {
        if (selectedDate == null) {
            showMessage("Vui lòng chọn ngày khám!");
            return false;
        }

        String selectedBacSi = spinnerBacSi.getSelectedItem().toString();
        if (selectedBacSi.equals("-- Chọn bác sĩ --")) {
            showMessage("Vui lòng chọn bác sĩ!");
            return false;
        }

        String selectedKhungGio = spinnerKhungGio.getSelectedItem().toString();
        if (selectedKhungGio.equals("-- Chọn khung giờ --")) {
            showMessage("Vui lòng chọn khung giờ!");
            return false;
        }

        return true;
    }

    private void clearFields() {
        selectedDate = null;
        btnChonNgay.setText("Chọn ngày khám");
        bacSiMap.clear();
        khungGioMap.clear();
        
        ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"-- Chọn bác sĩ --"});
        spinnerBacSi.setAdapter(emptyAdapter);
        
        ArrayAdapter<String> emptyAdapter2 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, new String[]{"-- Chọn khung giờ --"});
        spinnerKhungGio.setAdapter(emptyAdapter2);
        
        hideMessage();
    }

    private void showMessage(String message) {
        tvThongBao.setText(message);
        tvThongBao.setVisibility(View.VISIBLE);
    }

    private void hideMessage() {
        tvThongBao.setVisibility(View.GONE);
    }
    
    private void guiThongBaoDangKyChoBS(String maBacSi, String tenBacSi, String khungGio) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayKham = sdf.format(selectedDate);
        
        // Lấy tên bệnh nhân
        repo.getByField("BenhNhan", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String tenBenhNhan = querySnapshot.getDocuments().get(0).getString("hoTen");
                    
                    String tieuDe = "Đăng ký lịch khám mới";
                    String noiDung = tenBenhNhan + " đã đăng ký lịch khám vào " + ngayKham + " - " + khungGio;
                    
                    NotificationHelper.guiThongBaoChoBacSi(
                        this,
                        maBacSi,
                        tieuDe,
                        noiDung,
                        "LICH_HEN",
                        maBenhNhan
                    );
                }
            },
            e -> Log.e(TAG, "Error loading benh nhan: ", e)
        );
    }
}
