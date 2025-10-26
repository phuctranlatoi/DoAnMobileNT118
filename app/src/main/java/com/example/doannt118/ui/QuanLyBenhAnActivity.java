package com.example.doannt118.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class QuanLyBenhAnActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private EditText etSearch, etMaBenhNhan, etChanDoan, etGhiChu;
    private TextView tvNgayKham, tvMessage;
    private RecyclerView rvBenhAn;
    private Button btnThem, btnCapNhat, btnXoa, btnQuayLai;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan, maBacSi;
    private BenhAn selectedBenhAn;
    private BenhAnAdapter benhAnAdapter;
    private List<BenhAn> benhAnList;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_benh_an);

        // Initialize Firestore and get intent data
        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản Lý Bệnh Án");

        etSearch = findViewById(R.id.etSearch);
        etMaBenhNhan = findViewById(R.id.etMaBenhNhan);
        tvNgayKham = findViewById(R.id.tvNgayKham);
        etChanDoan = findViewById(R.id.etChanDoan);
        etGhiChu = findViewById(R.id.etGhiChu);
        rvBenhAn = findViewById(R.id.rvBenhAn);
        btnThem = findViewById(R.id.btnThem);
        btnCapNhat = findViewById(R.id.btnCapNhat);
        btnXoa = findViewById(R.id.btnXoa);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        tvMessage = findViewById(R.id.tvMessage);
        progressBar = findViewById(R.id.progressBar);

        // Set up RecyclerView
        rvBenhAn.setLayoutManager(new LinearLayoutManager(this));
        benhAnList = new ArrayList<>();
        benhAnAdapter = new BenhAnAdapter(benhAnList, benhAn -> {
            selectedBenhAn = benhAn;
            loadBenhAnForUpdate(benhAn);
            btnCapNhat.setVisibility(View.VISIBLE);
            btnXoa.setVisibility(View.VISIBLE);
            btnThem.setVisibility(View.GONE);
        });
        rvBenhAn.setAdapter(benhAnAdapter);

        // Set up button listeners
        btnThem.setOnClickListener(v -> handleThem());
        btnCapNhat.setOnClickListener(v -> handleCapNhat());
        btnXoa.setOnClickListener(v -> handleXoa());
        btnQuayLai.setOnClickListener(v -> handleQuayLai());
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            handleTraCuu();
            return true;
        });
        tvNgayKham.setOnClickListener(v -> showDatePickerDialog());

        // Initially hide update/delete buttons
        btnCapNhat.setVisibility(View.GONE);
        btnXoa.setVisibility(View.GONE);

        // Load medical records
        loadDanhSachBenhAn();
    }

    private void loadDanhSachBenhAn() {
        if (maBacSi == null) {
            showError("Lỗi: Không tìm thấy mã bác sĩ");
            return;
        }
        showProgressBar();
        repo.getByField("BenhAn", "maBacSi", maBacSi,
                querySnapshot -> {
                    benhAnList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        BenhAn benhAn = doc.toObject(BenhAn.class);
                        if (benhAn != null) {
                            benhAn.setMaBenhAn(doc.getId());
                            benhAnList.add(benhAn);
                        }
                    }
                    benhAnAdapter.notifyDataSetChanged();
                    if (benhAnList.isEmpty()) {
                        showError("Không có bệnh án!");
                    } else {
                        hideMessage();
                    }
                    hideProgressBar();
                },
                e -> {
                    showError("Lỗi tải bệnh án: " + e.getMessage());
                    hideProgressBar();
                });
    }

    private void loadBenhAnForUpdate(BenhAn benhAn) {
        etMaBenhNhan.setText(benhAn.getMaBenhNhan());
        etChanDoan.setText(benhAn.getChanDoan());
        etGhiChu.setText(benhAn.getGhiChu());
        tvNgayKham.setText(benhAn.getNgayKham() != null
                ? DATE_FORMAT.format(benhAn.getNgayKham().toDate())
                : "");
    }

    private void handleThem() {
        if (!validateInput()) return;
        BenhAn benhAn = new BenhAn();
        String documentId = "BA" + UUID.randomUUID().toString().substring(0, 8);
        benhAn.setMaBenhAn(documentId);
        benhAn.setMaBenhNhan(etMaBenhNhan.getText().toString().trim());
        benhAn.setMaBacSi(maBacSi);
        benhAn.setChanDoan(etChanDoan.getText().toString().trim());
        benhAn.setGhiChu(etGhiChu.getText().toString().trim());
        benhAn.setNgayKham(getSelectedDateAsTimestamp());

        showProgressBar();
        repo.getByField("BenhNhan", "maBenhNhan", benhAn.getMaBenhNhan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        repo.addDocument("BenhAn", documentId, benhAn,
                                aVoid -> {
                                    Toast.makeText(this, "Thêm bệnh án thành công!", Toast.LENGTH_SHORT).show();
                                    loadDanhSachBenhAn();
                                    clearFields();
                                },
                                e -> showError("Thêm thất bại: " + e.getMessage()));
                    } else {
                        showError("Mã bệnh nhân không tồn tại!");
                    }
                    hideProgressBar();
                },
                e -> {
                    showError("Lỗi kiểm tra bệnh nhân: " + e.getMessage());
                    hideProgressBar();
                });
    }

    private void handleCapNhat() {
        if (selectedBenhAn == null) {
            showError("Vui lòng chọn bệnh án để cập nhật!");
            return;
        }
        if (!validateInput()) return;
        BenhAn benhAn = new BenhAn();
        benhAn.setMaBenhAn(selectedBenhAn.getMaBenhAn());
        benhAn.setMaBenhNhan(etMaBenhNhan.getText().toString().trim());
        benhAn.setMaBacSi(maBacSi);
        benhAn.setChanDoan(etChanDoan.getText().toString().trim());
        benhAn.setGhiChu(etGhiChu.getText().toString().trim());
        benhAn.setNgayKham(getSelectedDateAsTimestamp());

        showProgressBar();
        repo.getByField("BenhNhan", "maBenhNhan", benhAn.getMaBenhNhan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        repo.updateDocument("BenhAn", benhAn.getMaBenhAn(), benhAn,
                                aVoid -> {
                                    Toast.makeText(this, "Cập nhật bệnh án thành công!", Toast.LENGTH_SHORT).show();
                                    loadDanhSachBenhAn();
                                    clearFields();
                                },
                                e -> showError("Cập nhật thất bại: " + e.getMessage()));
                    } else {
                        showError("Mã bệnh nhân không tồn tại!");
                    }
                    hideProgressBar();
                },
                e -> {
                    showError("Lỗi kiểm tra bệnh nhân: " + e.getMessage());
                    hideProgressBar();
                });
    }

    private void handleXoa() {
        if (selectedBenhAn == null) {
            showError("Vui lòng chọn bệnh án để xóa!");
            return;
        }
        showProgressBar();
        repo.deleteDocument("BenhAn", selectedBenhAn.getMaBenhAn(),
                aVoid -> {
                    Toast.makeText(this, "Xóa bệnh án thành công!", Toast.LENGTH_SHORT).show();
                    loadDanhSachBenhAn();
                    clearFields();
                },
                e -> showError("Xóa thất bại: " + e.getMessage()));
        hideProgressBar();
    }

    private void handleTraCuu() {
        String keyword = etSearch.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) {
            loadDanhSachBenhAn();
            return;
        }
        showProgressBar();
        repo.getByField("BenhAn", "maBenhNhan", keyword,
                querySnapshot -> {
                    benhAnList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        BenhAn benhAn = doc.toObject(BenhAn.class);
                        if (benhAn != null && benhAn.getMaBacSi().equals(maBacSi)) {
                            benhAn.setMaBenhAn(doc.getId());
                            benhAnList.add(benhAn);
                        }
                    }
                    benhAnAdapter.notifyDataSetChanged();
                    if (benhAnList.isEmpty()) {
                        showError("Không tìm thấy bệnh án!");
                    } else {
                        hideMessage();
                    }
                    hideProgressBar();
                },
                e -> {
                    showError("Tra cứu thất bại: " + e.getMessage());
                    hideProgressBar();
                });
    }

    private void handleQuayLai() {
        Intent intent = new Intent(this, MainBacSiActivity.class);
        intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
        startActivity(intent);
        finish();
    }

    private boolean validateInput() {
        String maBenhNhan = etMaBenhNhan.getText().toString().trim();
        if (TextUtils.isEmpty(maBenhNhan)) {
            showError("Vui lòng nhập mã bệnh nhân!");
            return false;
        }
        if (TextUtils.isEmpty(tvNgayKham.getText())) {
            showError("Vui lòng chọn ngày khám!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        etMaBenhNhan.setText("");
        etChanDoan.setText("");
        etGhiChu.setText("");
        tvNgayKham.setText("");
        selectedBenhAn = null;
        btnCapNhat.setVisibility(View.GONE);
        btnXoa.setVisibility(View.GONE);
        btnThem.setVisibility(View.VISIBLE);
        hideMessage();
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        // Restrict to past 5 years and future 1 year
        calendar.add(Calendar.YEAR, -5);
        Date minDate = calendar.getTime();
        calendar.add(Calendar.YEAR, 6);
        Date maxDate = calendar.getTime();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    calendar.set(year1, month1, dayOfMonth);
                    if (calendar.getTime().before(minDate) || calendar.getTime().after(maxDate)) {
                        showError("Ngày phải trong khoảng 5 năm trước đến 1 năm sau!");
                        return;
                    }
                    tvNgayKham.setText(DATE_FORMAT.format(calendar.getTime()));
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTime());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());
        datePickerDialog.show();
    }

    private Timestamp getSelectedDateAsTimestamp() {
        if (TextUtils.isEmpty(tvNgayKham.getText())) return Timestamp.now();
        try {
            Date date = DATE_FORMAT.parse(tvNgayKham.getText().toString());
            if (date == null) throw new ParseException("Invalid date format", 0);
            // Validate date range (e.g., not before 1970 or after 1 year from now)
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            Calendar minCal = Calendar.getInstance();
            minCal.set(1970, 0, 1);
            Calendar maxCal = Calendar.getInstance();
            maxCal.add(Calendar.YEAR, 1);
            if (cal.before(minCal) || cal.after(maxCal)) {
                showError("Ngày phải trong khoảng từ 1970 đến 1 năm sau hiện tại!");
                return Timestamp.now();
            }
            return new Timestamp(date);
        } catch (ParseException e) {
            showError("Định dạng ngày không hợp lệ: " + e.getMessage());
            return Timestamp.now();
        }
    }

    private void showProgressBar() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private void showError(String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void hideMessage() {
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);
    }
}