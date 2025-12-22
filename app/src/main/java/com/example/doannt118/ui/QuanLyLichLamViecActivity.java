package com.example.doannt118.ui;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.Query;
import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class QuanLyLichLamViecActivity extends AppCompatActivity implements LichLamViecAdapter.OnItemClickListener {

    private View toolbar;
    private TextView tvUserName, lblThongBao, tvGioBatDau, tvGioKetThuc;
    private RecyclerView rvLichLamViec;
    private Button btnLogout, btnTraCuu, btnThem, btnCapNhat, btnXoa, btnXacNhanThem, btnXacNhanCapNhat, btnHuy, btnChonGioBatDau, btnChonGioKetThuc;
    private View btnQuayLai;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBacSi;
    private String tenBacSi;
    private DatePicker dpTraCuu, dpNgayLamViec;
    private com.google.android.material.textfield.TextInputEditText edtSoLuongToiDa;
    private View formNhapLieu;

    private List<LichLamViec> lichLamViecList = new ArrayList<>();
    private HashMap<String, String> currentDoctorMap = new HashMap<>();

    private LichLamViecAdapter adapter;
    private boolean isAdding = false;
    private boolean isUpdating = false;

    private LocalTime selectedStartTime = null;
    private LocalTime selectedEndTime = null;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int REQUEST_CODE_THEM_LICH = 1001;
    private static final int REQUEST_CODE_SUA_LICH = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_lich_lam_viec);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // --- findViewById ---
        toolbar = findViewById(R.id.toolbar);
        tvUserName = findViewById(R.id.tvUserName);
        rvLichLamViec = findViewById(R.id.rvLichLamViec);
        btnLogout = findViewById(R.id.btnLogout);
        btnTraCuu = findViewById(R.id.btnTraCuu);
        btnThem = findViewById(R.id.btnThem);
        btnCapNhat = findViewById(R.id.btnCapNhat);
        btnXoa = findViewById(R.id.btnXoa);
        btnXacNhanThem = findViewById(R.id.btnXacNhanThem);
        btnXacNhanCapNhat = findViewById(R.id.btnXacNhanCapNhat);
        btnHuy = findViewById(R.id.btnHuy);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        progressBar = findViewById(R.id.progressBar);
        dpTraCuu = findViewById(R.id.dpTraCuu);
        dpNgayLamViec = findViewById(R.id.dpNgayLamViec);
        edtSoLuongToiDa = findViewById(R.id.edtSoLuongToiDa);
        formNhapLieu = findViewById(R.id.formNhapLieu);
        lblThongBao = findViewById(R.id.lblThongBao);
        btnChonGioBatDau = findViewById(R.id.btnChonGioBatDau);
        tvGioBatDau = findViewById(R.id.tvGioBatDau);
        btnChonGioKetThuc = findViewById(R.id.btnChonGioKetThuc);
        tvGioKetThuc = findViewById(R.id.tvGioKetThuc);
        // --- End findViewById ---

        rvLichLamViec.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LichLamViecAdapter(this, lichLamViecList, this, currentDoctorMap);
        rvLichLamViec.setAdapter(adapter);

        // --- setOnClickListener ---
        btnLogout.setOnClickListener(v -> handleDangXuat());
        btnTraCuu.setOnClickListener(v -> handleTraCuu(v));
        btnThem.setOnClickListener(v -> handleThem());
        btnCapNhat.setOnClickListener(v -> handleCapNhat());
        btnXoa.setOnClickListener(v -> handleXoa());
        btnXacNhanThem.setOnClickListener(v -> handleXacNhanThem(v));
        btnXacNhanCapNhat.setOnClickListener(v -> handleXacNhanCapNhat(v));
        btnHuy.setOnClickListener(v -> handleHuy());
        btnQuayLai.setOnClickListener(v -> handleQuayLai());
        btnChonGioBatDau.setOnClickListener(v -> showTimePickerDialog(true));
        btnChonGioKetThuc.setOnClickListener(v -> showTimePickerDialog(false));
        // --- End setOnClickListener ---

        setFormVisible(false);
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        loadUserInfo();
    }

    private void showTimePickerDialog(boolean isStartTime) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        LocalTime initialTime = isStartTime ? selectedStartTime : selectedEndTime;
        if (initialTime != null) {
            hour = initialTime.getHour();
            minute = initialTime.getMinute();
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minuteOfHour);
                    String formattedTime = selectedTime.format(TIME_FORMATTER);
                    if (isStartTime) {
                        selectedStartTime = selectedTime;
                        tvGioBatDau.setText(formattedTime);
                    } else {
                        selectedEndTime = selectedTime;
                        tvGioKetThuc.setText(formattedTime);
                    }
                }, hour, minute, true);

        timePickerDialog.setTitle(isStartTime ? "Chọn giờ bắt đầu" : "Chọn giờ kết thúc");
        timePickerDialog.show();
    }

    private void loadUserInfo() {
        if (maTaiKhoan == null) {
            showError("Mã tài khoản không hợp lệ!");
            finish();
            return;
        }
        
        com.example.doannt118.utils.UserInfoLoader.loadBacSi(maTaiKhoan, repo,
            new com.example.doannt118.utils.UserInfoLoader.BacSiCallback() {
                @Override
                public void onSuccess(BacSi bacSi) {
                    tvUserName.setText(bacSi.getHoTen());
                    maBacSi = bacSi.getMaBacSi();
                    tenBacSi = bacSi.getHoTen();
                    currentDoctorMap.put(maBacSi, tenBacSi);
                    adapter.updateNhanVienInfo(currentDoctorMap);
                    Log.d("QuanLyLichLamViec", "Loaded maBacSi: " + maBacSi);
                    loadDanhSachLich();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                }
                
                @Override
                public void onError(String message) {
                    showError(message);
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    finish();
                }
            });
    }

    private void loadDanhSachLich() {
        if (maBacSi == null) {
            showError("Lỗi: Không thể tải lịch vì thiếu mã bác sĩ.");
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        lblThongBao.setText("Đang tải lịch làm việc...");
        lblThongBao.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));

        Calendar calendar = Calendar.getInstance();
        calendar.set(dpTraCuu.getYear(), dpTraCuu.getMonth(), dpTraCuu.getDayOfMonth(), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        calendar.set(dpTraCuu.getYear(), dpTraCuu.getMonth(), dpTraCuu.getDayOfMonth(), 23, 59, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        Date endDate = calendar.getTime();

        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
                querySnapshot -> {
                    lichLamViecList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        try {
                            LichLamViec lich = doc.toObject(LichLamViec.class);
                            lichLamViecList.add(lich);
                        } catch (Exception e) {
                            Log.e("LoadLichError", "Lỗi chuyển đổi document: " + doc.getId(), e);
                        }
                    }

                    Collections.sort(lichLamViecList, Comparator.comparing(LichLamViec::getCaLamViec, Comparator.nullsFirst(String::compareTo)));

                    adapter.notifyDataSetChanged();

                    if (lichLamViecList.isEmpty()) {
                        showMessage("Không có lịch làm việc nào cho ngày này.");
                    } else {
                        showMessage("Đã tải " + lichLamViecList.size() + " lịch làm việc.");
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    btnCapNhat.setEnabled(false);
                    btnXoa.setEnabled(false);
                    adapter.resetSelection();
                },
                e -> {
                    Log.e("QuanLyLichLamViecActivity", "Lỗi tải lịch làm việc: ", e);
                    if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                        showError("Lỗi truy vấn Firestore: Cần tạo Index trong Firebase Console. Xem Logcat để lấy link.");
                        Log.e("Firestore Index", "Tạo index tại: " + e.getMessage().substring(e.getMessage().indexOf("https://")));
                    } else {
                        showError("Lỗi tải lịch làm việc: " + e.getMessage());
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    lichLamViecList.clear();
                    adapter.notifyDataSetChanged();
                    btnCapNhat.setEnabled(false);
                    btnXoa.setEnabled(false);
                    adapter.resetSelection();
                });
    }

    public void handleTraCuu(View view) {
        loadDanhSachLich();
    }

    public void handleThem() {
        // Mở Activity mới để thêm lịch làm việc
        Intent intent = new Intent(this, ThemLichLamViecActivity.class);
        intent.putExtra("maBacSi", maBacSi);
        startActivityForResult(intent, REQUEST_CODE_THEM_LICH);
    }

    public void handleXacNhanThem(View view) {
        if (!validateInput()) return;

        LichLamViec lich = new LichLamViec();
        lich.setMaBacSi(maBacSi);
        Calendar calendar = Calendar.getInstance();
        calendar.set(dpNgayLamViec.getYear(), dpNgayLamViec.getMonth(), dpNgayLamViec.getDayOfMonth(), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        lich.setNgayLamViec(calendar.getTime());

        String khungGio = selectedStartTime.format(TIME_FORMATTER) + "-" + selectedEndTime.format(TIME_FORMATTER);
        lich.setCaLamViec(khungGio);
        lich.setMaLichLamViec(UUID.randomUUID().toString());
        
        // Lấy số lượng tối đa từ input
        String soLuongStr = edtSoLuongToiDa.getText().toString().trim();
        int soLuongToiDa = 10; // Mặc định
        try {
            if (!soLuongStr.isEmpty()) {
                soLuongToiDa = Integer.parseInt(soLuongStr);
            }
        } catch (NumberFormatException e) {
            showError("Số lượng bệnh nhân không hợp lệ!");
            return;
        }
        
        lich.setSoLuongToiDa(soLuongToiDa);

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        repo.addDocument("LichLamViec", lich.getMaLichLamViec(), lich,
                (Void v) -> {
                    showMessage("Thêm lịch làm việc thành công!");
                    loadDanhSachLich();
                    setFormVisible(false);
                    isAdding = false;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("XacNhanThemError", "Lỗi thêm lịch làm việc", e);
                    showError("Thêm thất bại: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    public void handleCapNhat() {
        LichLamViec selected = adapter.getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn một lịch làm việc từ danh sách để cập nhật!");
            return;
        }
        
        // Mở Activity mới để sửa lịch làm việc
        Intent intent = new Intent(this, ThemLichLamViecActivity.class);
        intent.putExtra("maBacSi", maBacSi);
        intent.putExtra("maLichLamViec", selected.getMaLichLamViec());
        startActivityForResult(intent, REQUEST_CODE_SUA_LICH);
    }

    public void handleXacNhanCapNhat(View view) {
        if (!validateInput()) return;

        LichLamViec selected = adapter.getSelectedItem();
        if (selected == null) {
            showError("Lỗi: Không tìm thấy lịch làm việc đang chọn để cập nhật.");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(dpNgayLamViec.getYear(), dpNgayLamViec.getMonth(), dpNgayLamViec.getDayOfMonth(), 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Map<String, Object> updates = new HashMap<>();
        updates.put("ngayLamViec", calendar.getTime());
        String khungGio = selectedStartTime.format(TIME_FORMATTER) + "-" + selectedEndTime.format(TIME_FORMATTER);
        updates.put("caLamViec", khungGio);
        
        // Cập nhật số lượng tối đa
        String soLuongStr = edtSoLuongToiDa.getText().toString().trim();
        try {
            if (!soLuongStr.isEmpty()) {
                int soLuongToiDa = Integer.parseInt(soLuongStr);
                updates.put("soLuongBenhNhanToiDa", soLuongToiDa);
            }
        } catch (NumberFormatException e) {
            showError("Số lượng bệnh nhân không hợp lệ!");
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        repo.updateDocument("LichLamViec", selected.getMaLichLamViec(), updates,
                (Void v) -> {
                    showMessage("Cập nhật lịch làm việc thành công!");
                    loadDanhSachLich();
                    setFormVisible(false);
                    isUpdating = false;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("XacNhanCapNhatError", "Lỗi cập nhật lịch làm việc", e);
                    showError("Cập nhật thất bại: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    public void handleXoa() {
        LichLamViec selected = adapter.getSelectedItem();
        if (selected == null) {
            showError("Vui lòng chọn một lịch làm việc từ danh sách để xóa!");
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        lblThongBao.setText("Đang kiểm tra lịch khám liên quan...");
        
        // Kiểm tra xem có bệnh nhân nào đã đăng ký không (theo UC009 bước 3.2.3)
        repo.getByField("LichKham", "maLichLamViec", selected.getMaLichLamViec(),
                querySnapshot -> {
                    int soLuongBenhNhan = 0;
                    for (var doc : querySnapshot.getDocuments()) {
                        String trangThai = doc.getString("trangThai");
                        // Đếm cả lịch chờ xác nhận và đã xác nhận
                        if ("CHO".equals(trangThai) || "XAC_NHAN".equals(trangThai)) {
                            soLuongBenhNhan++;
                        }
                    }
                    
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    
                    if (soLuongBenhNhan > 0) {
                        showError("Không thể xóa lịch làm việc này vì đã có " + soLuongBenhNhan + " bệnh nhân đăng ký.");
                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("Xác nhận xóa")
                                .setMessage("Bạn có chắc chắn muốn xóa lịch làm việc vào ca '"
                                        + selected.getCaLamViec() + "' ngày "
                                        + new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selected.getNgayLamViec()) + "?")
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                    performDelete(selected);
                                })
                                .setNegativeButton(android.R.string.no, (dialog, which) -> {
                                    lblThongBao.setText("Đã hủy thao tác xóa.");
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                },
                e -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    showError("Lỗi khi kiểm tra lịch khám liên quan: " + e.getMessage());
                    Log.e("CheckLichKhamError", "Error checking related appointments", e);
                });
    }

    private void performDelete(LichLamViec lichToDelete) {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        lblThongBao.setText("Đang xóa lịch làm việc...");
        repo.deleteDocument("LichLamViec", lichToDelete.getMaLichLamViec(),
                (Void v) -> {
                    showMessage("Xóa lịch làm việc thành công!");
                    loadDanhSachLich();
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("DeleteLichError", "Lỗi xóa lịch làm việc", e);
                    showError("Xóa thất bại: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                });
    }

    public void handleDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // 🔥 FIX: Clear Stringee connection và cache trước khi logout
                    try {
                        com.example.doannt118.stringee.StringeeManager stringeeManager = 
                            com.example.doannt118.stringee.StringeeManager.getInstance(this);
                        stringeeManager.logout();
                        Log.d("QuanLyLichLamViecActivity", "✅ Stringee logout completed");
                    } catch (Exception e) {
                        Log.e("QuanLyLichLamViecActivity", "❌ Error during Stringee logout: " + e.getMessage());
                    }
                    
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public void handleQuayLai() {
        finish();
    }

    public void handleHuy() {
        setFormVisible(false);
        isAdding = false;
        isUpdating = false;
        lblThongBao.setText("Đã hủy thao tác.");
        adapter.resetSelection();
        btnCapNhat.setEnabled(false);
        btnXoa.setEnabled(false);
    }

    private void setFormVisible(boolean visible) {
        formNhapLieu.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnXacNhanThem.setVisibility(visible && isAdding ? View.VISIBLE : View.GONE);
        btnXacNhanCapNhat.setVisibility(visible && isUpdating ? View.VISIBLE : View.GONE);
        btnHuy.setVisibility(visible ? View.VISIBLE : View.GONE);

        btnThem.setEnabled(!visible);
        btnTraCuu.setEnabled(!visible);
        btnQuayLai.setClickable(!visible);
        btnLogout.setEnabled(!visible);

        boolean itemSelected = (adapter != null && adapter.getSelectedItem() != null);
        btnCapNhat.setEnabled(!visible && itemSelected);
        btnXoa.setEnabled(!visible && itemSelected);

        rvLichLamViec.setClickable(!visible);
        rvLichLamViec.setFocusable(!visible);
    }

    private LocalDate getSelectedDateFromDp(DatePicker datePicker) {
        try {
            return LocalDate.of(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
        } catch (Exception e) {
            Log.e("DatePickerError", "Lỗi lấy ngày từ DatePicker", e);
            return null;
        }
    }

    // Method này không còn cần thiết vì đã bỏ trạng thái
    // private int getTrangThaiPosition(String trangThai) {
    //     return 0;
    // }

    private boolean validateInput() {
        LocalDate ngay = getSelectedDateFromDp(dpNgayLamViec);

        if (ngay == null) {
            showError("Vui lòng chọn ngày làm việc hợp lệ!");
            return false;
        }
        if (selectedStartTime == null) {
            showError("Vui lòng chọn giờ bắt đầu!");
            btnChonGioBatDau.requestFocus();
            return false;
        }
        if (selectedEndTime == null) {
            showError("Vui lòng chọn giờ kết thúc!");
            btnChonGioKetThuc.requestFocus();
            return false;
        }

        if (!selectedEndTime.isAfter(selectedStartTime)) {
            showError("Giờ kết thúc phải sau giờ bắt đầu!");
            btnChonGioKetThuc.requestFocus();
            return false;
        }

        Duration duration = Duration.between(selectedStartTime, selectedEndTime);
        if (duration.toMinutes() != 240) {
            showError("Ca làm việc phải kéo dài đúng 4 tiếng (240 phút)!");
            btnChonGioKetThuc.requestFocus();
            return false;
        }

        if (!isStartTimeValid(ngay, selectedStartTime)) {
            return false;
        }
        return true;
    }

    private LocalTime[] parseKhungGio(String khungGio) {
        if (khungGio == null || !khungGio.contains("-")) return null;
        try {
            String[] parts = khungGio.split("-");
            if (parts.length != 2) return null;
            LocalTime startTime = LocalTime.parse(parts[0].trim(), TIME_FORMATTER);
            LocalTime endTime = LocalTime.parse(parts[1].trim(), TIME_FORMATTER);
            return new LocalTime[]{startTime, endTime};
        } catch (DateTimeParseException e) {
            Log.e("ParseKhungGioError", "Lỗi parse khung giờ: '" + khungGio + "'", e);
            return null;
        }
    }

    private boolean isStartTimeValid(LocalDate ngay, LocalTime gioBatDau) {
        LocalDate homNay = LocalDate.now();
        LocalTime bayGio = LocalTime.now();

        if (ngay.isBefore(homNay)) {
            showError("Không thể chọn ngày trong quá khứ!");
            return false;
        }
        if (ngay.equals(homNay) && gioBatDau.isBefore(bayGio)) {
            showError("Giờ bắt đầu không được nhỏ hơn thời gian hiện tại!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        Calendar now = Calendar.getInstance();
        dpNgayLamViec.updateDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        clearTimeFields();
        edtSoLuongToiDa.setText("10"); // Reset về giá trị mặc định
        lblThongBao.setText("");
    }

    private void clearTimeFields() {
        selectedStartTime = null;
        selectedEndTime = null;
        tvGioBatDau.setText("--:--");
        tvGioKetThuc.setText("--:--");
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        lblThongBao.setText("Lỗi: " + message);
        lblThongBao.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        lblThongBao.setText(message);
        lblThongBao.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    }

    @Override
    public void onItemClick(LichLamViec lichLamViec) {
        if (!formNhapLieu.isShown()) {
            btnCapNhat.setEnabled(true);
            btnXoa.setEnabled(true);
            String dateStr = "N/A";
            if (lichLamViec.getNgayLamViec() != null) {
                dateStr = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(lichLamViec.getNgayLamViec());
            }
            showMessage("Đã chọn: Ca " + lichLamViec.getCaLamViec() + " ngày " + dateStr);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_THEM_LICH || requestCode == REQUEST_CODE_SUA_LICH) {
                // Reload danh sách sau khi thêm/sửa thành công
                loadDanhSachLich();
                Toast.makeText(this, "Đã cập nhật lịch làm việc", Toast.LENGTH_SHORT).show();
            }
        }
    }
}