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
import androidx.appcompat.widget.Toolbar;
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

    private Toolbar toolbar;
    private TextView tvUserName, lblThongBao, tvGioBatDau, tvGioKetThuc;
    private RecyclerView rvLichLamViec;
    private Button btnLogout, btnTraCuu, btnThem, btnCapNhat, btnXoa, btnXacNhanThem, btnXacNhanCapNhat, btnHuy, btnQuayLai, btnChonGioBatDau, btnChonGioKetThuc;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBacSi;
    private String tenBacSi;
    private DatePicker dpTraCuu, dpNgayLamViec;
    private Spinner spTrangThai;
    private View formNhapLieu;

    private List<LichLamViec> lichLamViecList = new ArrayList<>();
    private HashMap<String, String> currentDoctorMap = new HashMap<>();

    private LichLamViecAdapter adapter;
    private boolean isAdding = false;
    private boolean isUpdating = false;

    private LocalTime selectedStartTime = null;
    private LocalTime selectedEndTime = null;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_lich_lam_viec_new);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // --- findViewById ---
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
        spTrangThai = findViewById(R.id.spTrangThai);
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

        ArrayAdapter<CharSequence> adapterSpinner = ArrayAdapter.createFromResource(this,
                R.array.trang_thai_array, android.R.layout.simple_spinner_item);
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTrangThai.setAdapter(adapterSpinner);

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
        repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSi != null) {
                            tvUserName.setText(bacSi.getHoTen());
                            maBacSi = bacSi.getMaBacSi();
                            tenBacSi = bacSi.getHoTen();
                            currentDoctorMap.put(maBacSi, tenBacSi);
                            adapter.updateNhanVienInfo(currentDoctorMap);
                            Log.d("QuanLyLichLamViec", "Loaded maBacSi: " + maBacSi);
                            loadDanhSachLich();
                        } else {
                            showError("Không tìm thấy thông tin bác sĩ!");
                            finish();
                        }
                    } else {
                        showError("Không tìm thấy thông tin bác sĩ!");
                        finish();
                    }
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                e -> {
                    Log.e("QuanLyLichLamViec", "Lỗi tải thông tin bác sĩ: ", e);
                    showError("Lỗi tải thông tin bác sĩ: " + e.getMessage());
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    finish();
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
        isAdding = true;
        isUpdating = false;
        clearFields();
        setFormVisible(true);
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
        lich.setTrangThai(spTrangThai.getSelectedItem().toString());
        lich.setMaLichLamViec(UUID.randomUUID().toString());

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
        isUpdating = true;
        isAdding = false;
        setFormVisible(true);

        Calendar calendar = Calendar.getInstance();
        if (selected.getNgayLamViec() != null) {
            calendar.setTime(selected.getNgayLamViec());
            dpNgayLamViec.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        } else {
            calendar = Calendar.getInstance();
            dpNgayLamViec.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        }

        LocalTime[] times = parseKhungGio(selected.getCaLamViec());
        if (times != null) {
            selectedStartTime = times[0];
            selectedEndTime = times[1];
            tvGioBatDau.setText(selectedStartTime.format(TIME_FORMATTER));
            tvGioKetThuc.setText(selectedEndTime.format(TIME_FORMATTER));
        } else {
            clearTimeFields();
            Log.w("HandleCapNhat", "Không thể parse khung giờ cũ: " + selected.getCaLamViec());
        }

        spTrangThai.setSelection(getTrangThaiPosition(selected.getTrangThai()));
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
        updates.put("trangThai", spTrangThai.getSelectedItem().toString());

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
        if (!"CON_TRONG".equalsIgnoreCase(selected.getTrangThai())) {
            showError("Chỉ có thể xóa lịch làm việc có trạng thái 'Còn trống'.");
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        lblThongBao.setText("Đang kiểm tra lịch khám liên quan...");
        repo.countByField("LichKham", "maLichLamViec", selected.getMaLichLamViec(),
                count -> {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (count > 0) {
                        showError("Không thể xóa lịch làm việc này vì đã có " + count + " lịch khám được đặt.");
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
        btnQuayLai.setEnabled(!visible);
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

    private int getTrangThaiPosition(String trangThai) {
        ArrayAdapter<CharSequence> spinnerAdapter = (ArrayAdapter<CharSequence>) spTrangThai.getAdapter();
        if (trangThai != null && spinnerAdapter != null) {
            for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                if (trangThai.equalsIgnoreCase(spinnerAdapter.getItem(i).toString())) {
                    return i;
                }
            }
        }
        Log.w("SpinnerWarning", "Không tìm thấy trạng thái '" + trangThai + "' trong Spinner.");
        return 0;
    }

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
        spTrangThai.setSelection(0);
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
            // Mở XemChiTietLichKhamActivity
            Intent intent = new Intent(this, XemChiTietLichKhamActivity.class);
            intent.putExtra("maLichLamViec", lichLamViec.getMaLichLamViec());
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
        }
    }
}