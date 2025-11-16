package com.example.doannt118.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.Timestamp;
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
    private AutoCompleteTextView etSearchBenhNhan;
    private TextView tvSelectedBenhNhan, tvNgayKham, tvMessage;
    private EditText etSearch, etChanDoan, etGhiChu;
    private RecyclerView rvBenhAn;
    private Button btnThem, btnCapNhat, btnXoa, btnQuayLai;
    private ProgressBar progressBar;
    private View cardForm, loadingOverlay, layoutEmpty, btnCloseForm;
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabAdd;
    private TextView tvCount;
    private FirestoreRepository repo;
    private String maTaiKhoan, maBacSi;
    private BenhAn selectedBenhAn;
    private BenhNhan selectedBenhNhan; // Lưu bệnh nhân được chọn
    private BenhAnAdapter benhAnAdapter;
    private List<BenhAn> benhAnList;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_benh_an_new);

        // Initialize Firestore and get intent data
        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Quản Lý Bệnh Án");

        etSearchBenhNhan = findViewById(R.id.etSearchBenhNhan);
        tvSelectedBenhNhan = findViewById(R.id.tvSelectedBenhNhan);
        etSearch = findViewById(R.id.etSearch);
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
        
        // New layout views
        cardForm = findViewById(R.id.cardForm);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        btnCloseForm = findViewById(R.id.btnCloseForm);
        fabAdd = findViewById(R.id.fabAdd);
        tvCount = findViewById(R.id.tvCount);

        // Set up RecyclerView
        rvBenhAn.setLayoutManager(new LinearLayoutManager(this));
        benhAnList = new ArrayList<>();
        benhAnAdapter = new BenhAnAdapter(benhAnList, benhAn -> {
            selectedBenhAn = benhAn;
            loadBenhAnForUpdate(benhAn);
            
            // Show form in edit mode
            if (cardForm != null) {
                cardForm.setVisibility(View.VISIBLE);
            }
            btnCapNhat.setVisibility(View.VISIBLE);
            btnXoa.setVisibility(View.VISIBLE);
            btnThem.setVisibility(View.GONE);
            
            Toast.makeText(this, "Chỉnh sửa bệnh án", Toast.LENGTH_SHORT).show();
        });
        rvBenhAn.setAdapter(benhAnAdapter);

        // Set up button listeners
        btnThem.setOnClickListener(v -> confirmAction("Thêm bệnh án", "Bạn có chắc muốn thêm bệnh án này?", this::handleThem));
        btnCapNhat.setOnClickListener(v -> confirmAction("Cập nhật bệnh án", "Bạn có chắc muốn cập nhật bệnh án này?", this::handleCapNhat));
        btnXoa.setOnClickListener(v -> confirmAction("Xóa bệnh án", "Bạn có chắc muốn xóa bệnh án này?", this::handleXoa));
        btnQuayLai.setOnClickListener(v -> handleQuayLai());
        
        // FAB - Show form to add new
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                clearForm();
                selectedBenhAn = null;
                selectedBenhNhan = null;
                cardForm.setVisibility(View.VISIBLE);
                btnThem.setVisibility(View.VISIBLE);
                btnCapNhat.setVisibility(View.GONE);
                btnXoa.setVisibility(View.GONE);
                Toast.makeText(this, "Thêm bệnh án mới", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Close form button
        if (btnCloseForm != null) {
            btnCloseForm.setOnClickListener(v -> {
                cardForm.setVisibility(View.GONE);
                clearForm();
            });
        }
        
        // Toolbar navigation
        toolbar.setNavigationOnClickListener(v -> finish());
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            handleTraCuu();
            return true;
        });
        tvNgayKham.setOnClickListener(v -> showDatePickerDialog());

        // Set up search patient
        etSearchBenhNhan.setOnEditorActionListener((v, actionId, event) -> {
            searchBenhNhan(etSearchBenhNhan.getText().toString().trim());
            return true;
        });

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
                    for (DocumentSnapshot doc : querySnapshot) {
                        BenhAn benhAn = doc.toObject(BenhAn.class);
                        if (benhAn != null) {
                            benhAn.setMaBenhAn(doc.getId());
                            benhAnList.add(benhAn);
                        }
                    }
                    benhAnAdapter.notifyDataSetChanged();
                    
                    // Update count
                    if (tvCount != null) {
                        tvCount.setText(benhAnList.size() + " bệnh án");
                    }
                    
                    // Show/hide empty state
                    if (benhAnList.isEmpty()) {
                        if (layoutEmpty != null) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                        }
                        showError("Không có bệnh án!");
                    } else {
                        if (layoutEmpty != null) {
                            layoutEmpty.setVisibility(View.GONE);
                        }
                        hideMessage();
                    }
                    hideProgressBar();
                },
                e -> {
                    showError("Lỗi tải bệnh án: " + e.toString());
                    hideProgressBar();
                });
    }

    private void loadBenhAnForUpdate(BenhAn benhAn) {
        // Load selected patient info
        repo.getByField("BenhNhan", "maBenhNhan", benhAn.getMaBenhNhan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        selectedBenhNhan = doc.toObject(BenhNhan.class);
                        if (selectedBenhNhan != null) {
                            tvSelectedBenhNhan.setText("Bệnh nhân: " + selectedBenhNhan.getHoTen() /*+ " (Mã: " + selectedBenhNhan.getMaBenhNhan() + ")"*/);
                        }
                    }
                },
                e -> showError("Lỗi tải thông tin bệnh nhân: " + e.toString()));
        etChanDoan.setText(benhAn.getChanDoan());
        etGhiChu.setText(benhAn.getGhiChu());
        tvNgayKham.setText(benhAn.getNgayKham() != null
                ? DATE_FORMAT.format(benhAn.getNgayKham().toDate())
                : "");
    }

    private void searchBenhNhan(String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            showError("Vui lòng nhập tên hoặc số điện thoại bệnh nhân!");
            return;
        }
        showProgressBar();
        // Tìm kiếm theo hoTen hoặc soDienThoai
        repo.getAll("BenhNhan",
                querySnapshot -> {
                    List<BenhNhan> benhNhanList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot) {
                        BenhNhan benhNhan = doc.toObject(BenhNhan.class);
                        if (benhNhan != null && (benhNhan.getHoTen().toLowerCase().contains(keyword.toLowerCase()) ||
                                benhNhan.getSoDienThoai().contains(keyword))) {
                            benhNhanList.add(benhNhan);
                            if (benhNhanList.size() >= 10) break; // Giới hạn 10 kết quả
                        }
                    }
                    if (benhNhanList.isEmpty()) {
                        showError("Không tìm thấy bệnh nhân!");
                        hideProgressBar();
                    } else {
                        showBenhNhanListDialog(benhNhanList);
                        hideProgressBar();
                    }
                },
                e -> {
                    showError("Lỗi tìm kiếm bệnh nhân: " + e.toString());
                    hideProgressBar();
                });
    }

    private void showBenhNhanListDialog(List<BenhNhan> benhNhanList) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_benh_nhan_list, null);
        RecyclerView rvBenhNhanList = dialogView.findViewById(R.id.rvBenhNhanList);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        rvBenhNhanList.setLayoutManager(new LinearLayoutManager(this));
        BenhNhanAdapter benhNhanAdapter = new BenhNhanAdapter(benhNhanList, benhNhan -> {
            selectedBenhNhan = benhNhan;
            tvSelectedBenhNhan.setText("Bệnh nhân: " + benhNhan.getHoTen() /*+ " (Mã: " + benhNhan.getMaBenhNhan() + ")"*/);
            etSearchBenhNhan.setText("");
        });
        rvBenhNhanList.setAdapter(benhNhanAdapter);

        AlertDialog dialog = builder.setView(dialogView).create();
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void handleThem() {
        if (!validateInput()) return;
        BenhAn benhAn = new BenhAn();
        String documentId = "BA" + UUID.randomUUID().toString().substring(0, 8);
        benhAn.setMaBenhAn(documentId);
        benhAn.setMaBenhNhan(selectedBenhNhan.getMaBenhNhan());
        benhAn.setMaBacSi(maBacSi);
        benhAn.setChanDoan(etChanDoan.getText().toString().trim());
        benhAn.setGhiChu(etGhiChu.getText().toString().trim());
        benhAn.setNgayKham(getSelectedDateAsTimestamp());

        showProgressBar();
        // Kiểm tra mã bệnh án trùng
        repo.getByField("BenhAn", "maBenhAn", documentId,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        showError("Mã bệnh án đã tồn tại, thử lại!");
                        hideProgressBar();
                        return;
                    }
                    repo.addDocument("BenhAn", documentId, benhAn,
                            aVoid -> {
                                Toast.makeText(this, "Thêm bệnh án thành công!", Toast.LENGTH_SHORT).show();
                                loadDanhSachBenhAn();
                                clearFields();
                                hideProgressBar();
                            },
                            e -> {
                                showError("Thêm thất bại: " + e.toString());
                                hideProgressBar();
                            });
                },
                e -> {
                    showError("Lỗi kiểm tra mã bệnh án: " + e.toString());
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
        benhAn.setMaBenhNhan(selectedBenhNhan.getMaBenhNhan());
        benhAn.setMaBacSi(maBacSi);
        benhAn.setChanDoan(etChanDoan.getText().toString().trim());
        benhAn.setGhiChu(etGhiChu.getText().toString().trim());
        benhAn.setNgayKham(getSelectedDateAsTimestamp());

        showProgressBar();
        repo.updateDocument("BenhAn", benhAn.getMaBenhAn(), benhAn,
                aVoid -> {
                    Toast.makeText(this, "Cập nhật bệnh án thành công!", Toast.LENGTH_SHORT).show();
                    loadDanhSachBenhAn();
                    clearFields();
                    hideProgressBar();
                },
                e -> {
                    showError("Cập nhật thất bại: " + e.toString());
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
                    hideProgressBar();
                },
                e -> {
                    showError("Xóa thất bại: " + e.toString());
                    hideProgressBar();
                });
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
                    for (DocumentSnapshot doc : querySnapshot) {
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
                    showError("Tra cứu thất bại: " + e.toString());
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
        if (selectedBenhNhan == null) {
            showError("Vui lòng chọn bệnh nhân!");
            return false;
        }
        if (TextUtils.isEmpty(tvNgayKham.getText())) {
            showError("Vui lòng chọn ngày khám!");
            return false;
        }
        return true;
    }

    private void clearFields() {
        etSearchBenhNhan.setText("");
        tvSelectedBenhNhan.setText("Chưa chọn bệnh nhân");
        etSearch.setText("");
        etChanDoan.setText("");
        etGhiChu.setText("");
        tvNgayKham.setText("");
        selectedBenhAn = null;
        selectedBenhNhan = null;
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
            showError("Định dạng ngày không hợp lệ: " + e.toString());
            return Timestamp.now();
        }
    }

    private void confirmAction(String title, String message, Runnable action) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Xác nhận", (dialog, which) -> action.run())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showProgressBar() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        } else if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        } else if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
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

    private void clearForm() {
        etSearchBenhNhan.setText("");
        tvSelectedBenhNhan.setText("Chưa chọn bệnh nhân");
        etSearch.setText("");
        etChanDoan.setText("");
        etGhiChu.setText("");
        tvNgayKham.setText("");
        selectedBenhAn = null;
        selectedBenhNhan = null;
    }
}
