package com.example.doannt118.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.model.DuocPham;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.utils.MedicationScheduler;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class KeDonThuocActivity extends AppCompatActivity {
    private TextView tvBenhNhan, tvBenhAn, tvEmptyThuoc;
    private TextInputEditText edtSoNgayUong;
    private RecyclerView rvThuoc;
    private MaterialButton btnThemThuoc, btnLuuDonThuoc;
    private ProgressBar progressBar;
    
    private ThuocKeDonAdapter adapter;
    private FirestoreRepository repository;
    private List<ChiTietDonThuoc> danhSachThuoc;
    
    private String maBenhAn;
    private String maBenhNhan;
    private String tenBenhNhan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ke_don_thuoc);

        initViews();
        setupToolbar();
        setupRecyclerView();
        
        maBenhAn = getIntent().getStringExtra("maBenhAn");
        if (maBenhAn != null) {
            loadBenhAnInfo();
        } else {
            Toast.makeText(this, "Không tìm thấy mã bệnh án", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        tvBenhNhan = findViewById(R.id.tvBenhNhan);
        tvBenhAn = findViewById(R.id.tvBenhAn);
        tvEmptyThuoc = findViewById(R.id.tvEmptyThuoc);
        edtSoNgayUong = findViewById(R.id.edtSoNgayUong);
        rvThuoc = findViewById(R.id.rvThuoc);
        btnThemThuoc = findViewById(R.id.btnThemThuoc);
        btnLuuDonThuoc = findViewById(R.id.btnLuuDonThuoc);
        progressBar = findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
        danhSachThuoc = new ArrayList<>();
        
        btnThemThuoc.setOnClickListener(v -> showDialogThemThuoc());
        btnLuuDonThuoc.setOnClickListener(v -> luuDonThuoc());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new ThuocKeDonAdapter(this, danhSachThuoc, position -> {
            danhSachThuoc.remove(position);
            adapter.notifyItemRemoved(position);
            updateEmptyView();
        });
        rvThuoc.setLayoutManager(new LinearLayoutManager(this));
        rvThuoc.setAdapter(adapter);
    }

    private void loadBenhAnInfo() {
        showLoading(true);
        
        repository.getByField("BenhAn", "maBenhAn", maBenhAn,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    maBenhNhan = doc.getString("maBenhNhan");
                    tvBenhAn.setText("Bệnh án: " + maBenhAn);
                    loadBenhNhanInfo();
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Không tìm thấy bệnh án", Toast.LENGTH_SHORT).show();
                    finish();
                }
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadBenhNhanInfo() {
        repository.getByField("BenhNhan", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                showLoading(false);
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    tenBenhNhan = doc.getString("hoTen");
                    tvBenhNhan.setText("Bệnh nhân: " + tenBenhNhan);
                }
            },
            e -> {
                showLoading(false);
                tvBenhNhan.setText("Bệnh nhân: Không rõ");
            }
        );
    }

    private void showDialogThemThuoc() {
        // Hiển thị dialog chọn thuốc trước
        showDialogChonThuoc();
    }

    private void showDialogChonThuoc() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chon_thuoc);
        dialog.getWindow().setLayout(
            getResources().getDisplayMetrics().widthPixels - 100,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        RecyclerView rvDanhSachThuoc = dialog.findViewById(R.id.rvDanhSachThuoc);
        TextView tvEmptyThuoc = dialog.findViewById(R.id.tvEmptyThuoc);

        rvDanhSachThuoc.setLayoutManager(new LinearLayoutManager(this));

        // Load danh sách thuốc
        repository.getAll("DuocPham",
            querySnapshot -> {
                List<DuocPham> danhSachDuocPham = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    DuocPham duocPham = doc.toObject(DuocPham.class);
                    if (duocPham != null) {
                        danhSachDuocPham.add(duocPham);
                    }
                }

                if (danhSachDuocPham.isEmpty()) {
                    tvEmptyThuoc.setVisibility(View.VISIBLE);
                    rvDanhSachThuoc.setVisibility(View.GONE);
                } else {
                    tvEmptyThuoc.setVisibility(View.GONE);
                    rvDanhSachThuoc.setVisibility(View.VISIBLE);

                    ChonThuocAdapter adapter = new ChonThuocAdapter(danhSachDuocPham, duocPham -> {
                        dialog.dismiss();
                        showDialogNhapThongTinThuoc(duocPham);
                    });
                    rvDanhSachThuoc.setAdapter(adapter);
                }
            },
            e -> {
                Toast.makeText(this, "Lỗi tải danh sách thuốc: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        );

        dialog.show();
    }

    private void showDialogNhapThongTinThuoc(DuocPham duocPham) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_them_thuoc);
        dialog.getWindow().setLayout(
            getResources().getDisplayMetrics().widthPixels - 100,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        AutoCompleteTextView edtTenThuoc = dialog.findViewById(R.id.edtTenThuoc);
        TextInputEditText edtSoLuong = dialog.findViewById(R.id.edtSoLuong);
        TextInputEditText edtSoNgayUong = dialog.findViewById(R.id.edtSoNgayUong);
        TextInputEditText edtSoLanMoiNgay = dialog.findViewById(R.id.edtSoLanMoiNgay);
        TextInputEditText edtSoVienMoiLan = dialog.findViewById(R.id.edtSoVienMoiLan);
        TextInputEditText edtCachDung = dialog.findViewById(R.id.edtCachDung);
        CheckBox cbSang = dialog.findViewById(R.id.cbSang);
        CheckBox cbTrua = dialog.findViewById(R.id.cbTrua);
        CheckBox cbChieu = dialog.findViewById(R.id.cbChieu);
        MaterialButton btnHuy = dialog.findViewById(R.id.btnHuy);
        MaterialButton btnXacNhan = dialog.findViewById(R.id.btnXacNhan);

        // Set tên thuốc đã chọn
        edtTenThuoc.setText(duocPham.getTenDuocPham());
        edtTenThuoc.setEnabled(false); // Không cho sửa tên thuốc

        btnHuy.setOnClickListener(v -> dialog.dismiss());
        
        btnXacNhan.setOnClickListener(v -> {
            String tenThuoc = edtTenThuoc.getText().toString().trim();
            String soLuongStr = edtSoLuong.getText().toString().trim();
            String soNgayUongStr = edtSoNgayUong.getText().toString().trim();
            String soLanMoiNgayStr = edtSoLanMoiNgay.getText().toString().trim();
            String soVienMoiLanStr = edtSoVienMoiLan.getText().toString().trim();
            String cachDung = edtCachDung.getText().toString().trim();
            
            if (!cbSang.isChecked() && !cbTrua.isChecked() && !cbChieu.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ca uống", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int soLuong = 1;
            int soNgayUong = 7;
            int soLanMoiNgay = 2;
            int soVienMoiLan = 1;
            
            try {
                soLuong = Integer.parseInt(soLuongStr);
                soNgayUong = Integer.parseInt(soNgayUongStr);
                soLanMoiNgay = Integer.parseInt(soLanMoiNgayStr);
                soVienMoiLan = Integer.parseInt(soVienMoiLanStr);
            } catch (Exception e) {
                Toast.makeText(this, "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Tạo liều dùng từ thông tin đã nhập
            String lieuDung = "Uống " + soVienMoiLan + " viên/lần, " + soLanMoiNgay + " lần/ngày";
            if (!cachDung.isEmpty()) {
                lieuDung += " - " + cachDung;
            }
            
            String maChiTiet = "CT_" + UUID.randomUUID().toString();
            ChiTietDonThuoc thuoc = new ChiTietDonThuoc(
                null, duocPham.getMaDuocPham(), tenThuoc, soLuong, lieuDung,
                soNgayUong, soLanMoiNgay, soVienMoiLan,
                cbSang.isChecked(), cbTrua.isChecked(), cbChieu.isChecked(), false // Không có ca tối
            );
            thuoc.setMaChiTiet(maChiTiet);
            thuoc.setCachDung(cachDung);
            
            danhSachThuoc.add(thuoc);
            adapter.notifyItemInserted(danhSachThuoc.size() - 1);
            updateEmptyView();
            
            dialog.dismiss();
        });

        dialog.show();
    }

    private void luuDonThuoc() {
        if (danhSachThuoc.isEmpty()) {
            Toast.makeText(this, "Vui lòng thêm ít nhất 1 thuốc", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String soNgayStr = edtSoNgayUong.getText().toString().trim();
        int soNgayUong = 7;
        try {
            soNgayUong = Integer.parseInt(soNgayStr);
            if (soNgayUong <= 0) {
                Toast.makeText(this, "Số ngày uống phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Số ngày uống không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        String maDonThuoc = "DT_" + UUID.randomUUID().toString();
        Date ngayLap = new Date();
        
        // Tạo đơn thuốc
        DonThuoc donThuoc = new DonThuoc(maDonThuoc, maBenhAn, maBenhNhan, ngayLap, soNgayUong);
        donThuoc.setNgayBatDau(ngayLap);
        
        repository.addDocument("DonThuoc", maDonThuoc, donThuoc,
            aVoid -> {
                // Lưu chi tiết đơn thuốc
                luuChiTietDonThuoc(maDonThuoc, donThuoc);
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void luuChiTietDonThuoc(String maDonThuoc, DonThuoc donThuoc) {
        int[] count = {0};
        int total = danhSachThuoc.size();
        
        for (ChiTietDonThuoc thuoc : danhSachThuoc) {
            thuoc.setMaDonThuoc(maDonThuoc);
            
            repository.addDocument("ChiTietDonThuoc", thuoc.getMaChiTiet(), thuoc,
                aVoid -> {
                    count[0]++;
                    if (count[0] == total) {
                        // Tạo lịch uống thuốc
                        taoLichUongThuoc(donThuoc);
                    }
                },
                e -> {
                    showLoading(false);
                    Toast.makeText(this, "Lỗi lưu chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            );
        }
    }

    private void taoLichUongThuoc(DonThuoc donThuoc) {
        MedicationScheduler scheduler = new MedicationScheduler();
        scheduler.taoLichUongThuoc(donThuoc, new MedicationScheduler.OnScheduleCreatedListener() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(KeDonThuocActivity.this, 
                    "Đã lưu đơn thuốc và tạo lịch uống thuốc", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                showLoading(false);
                Toast.makeText(KeDonThuocActivity.this, 
                    "Đã lưu đơn thuốc nhưng lỗi tạo lịch: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateEmptyView() {
        tvEmptyThuoc.setVisibility(danhSachThuoc.isEmpty() ? View.VISIBLE : View.GONE);
        rvThuoc.setVisibility(danhSachThuoc.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLuuDonThuoc.setEnabled(!show);
    }
}
