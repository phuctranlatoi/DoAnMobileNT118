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
        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        maBenhNhan = querySnapshot.getDocuments().get(0).getString("maBenhNhan");
                        loadLichKham();
                    } else {
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                e -> {
                    Log.e(TAG, "Error loading maBenhNhan: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin!", Toast.LENGTH_SHORT).show();
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
        
        // Load tất cả lịch làm việc trong khoảng thời gian
        repo.getCollection("LichLamViec")
                .whereGreaterThanOrEqualTo("ngayLamViec", startDate)
                .whereLessThanOrEqualTo("ngayLamViec", endDate)
                .whereEqualTo("trangThai", "CON_TRONG")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> bacSiList = new ArrayList<>();
                    bacSiList.add("-- Chọn bác sĩ --");
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String maBacSi = doc.getString("maBacSi");
                        
                        if (maBacSi != null && !bacSiMap.containsValue(maBacSi)) {
                            // Load tên bác sĩ
                            repo.getByField("BacSi", "maBacSi", maBacSi,
                                    bacSiSnapshot -> {
                                        if (!bacSiSnapshot.isEmpty()) {
                                            String hoTen = bacSiSnapshot.getDocuments().get(0).getString("hoTen");
                                            if (hoTen != null && !bacSiMap.containsKey(hoTen)) {
                                                bacSiMap.put(hoTen, maBacSi);
                                                bacSiList.add(hoTen);
                                                updateBacSiSpinner(bacSiList);
                                            }
                                        }
                                    },
                                    e -> Log.e(TAG, "Error loading bacSi: ", e));
                        }
                    }
                    
                    if (bacSiList.size() == 1) {
                        showMessage("Không có bác sĩ trống cho ngày này!");
                    } else {
                        hideMessage();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading lichLamViec: ", e);
                    showMessage("Lỗi tải danh sách bác sĩ!");
                });
    }

    private void updateBacSiSpinner(List<String> bacSiList) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, bacSiList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBacSi.setAdapter(adapter);
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
        
        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
                querySnapshot -> {
                    List<String> khungGioList = new ArrayList<>();
                    khungGioList.add("-- Chọn khung giờ --");
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String trangThai = doc.getString("trangThai");
                        
                        if ("CON_TRONG".equals(trangThai)) {
                            String caLamViec = doc.getString("caLamViec");
                            String maLichLamViec = doc.getString("maLichLamViec");
                            
                            if (caLamViec != null && maLichLamViec != null) {
                                khungGioMap.put(caLamViec, maLichLamViec);
                                khungGioList.add(caLamViec);
                            }
                        }
                    }
                    
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_spinner_item, khungGioList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerKhungGio.setAdapter(adapter);
                    
                    if (khungGioList.size() == 1) {
                        showMessage("Bác sĩ không có khung giờ trống!");
                    } else {
                        hideMessage();
                    }
                },
                e -> {
                    Log.e(TAG, "Error loading khungGio: ", e);
                    showMessage("Lỗi tải khung giờ!");
                });
    }

    // Tiếp tục trong phần 2...

    private void handleDangKy() {
        if (!validateInput()) return;

        String selectedBacSi = spinnerBacSi.getSelectedItem().toString();
        String selectedKhungGio = spinnerKhungGio.getSelectedItem().toString();
        String maBacSi = bacSiMap.get(selectedBacSi);
        String maLichLamViec = khungGioMap.get(selectedKhungGio);

        // Kiểm tra số lượng bệnh nhân đã đăng ký
        repo.getByField("LichKham", "maLichLamViec", maLichLamViec,
                querySnapshot -> {
                    int soLuongHienTai = querySnapshot.size();
                    
                    if (soLuongHienTai >= 6) {
                        // Cập nhật trạng thái lịch làm việc
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("trangThai", "DA_DAY");
                        repo.updateDocumentFields("LichLamViec", maLichLamViec, updates,
                                aVoid -> {
                                    showMessage("Khung giờ này đã đầy!");
                                    loadKhungGio();
                                },
                                e -> Log.e(TAG, "Error updating trangThai: ", e));
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
                                
                                // Kiểm tra nếu đã đủ 6 người thì cập nhật trạng thái
                                if (soThuTu >= 6) {
                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("trangThai", "DA_DAY");
                                    repo.updateDocumentFields("LichLamViec", maLichLamViec, updates,
                                            v -> loadKhungGio(),
                                            e -> Log.e(TAG, "Error updating: ", e));
                                }
                                
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
                                
                                // Cập nhật trạng thái lịch làm việc về CON_TRONG
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("trangThai", "CON_TRONG");
                                repo.updateDocumentFields("LichLamViec",
                                        lichKham.getMaLichLamViec(), updates,
                                        v -> {},
                                        e -> Log.e(TAG, "Error updating: ", e));
                                
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
}
