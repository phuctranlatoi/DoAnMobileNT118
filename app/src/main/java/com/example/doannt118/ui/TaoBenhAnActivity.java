package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
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
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TaoBenhAnActivity extends AppCompatActivity {
    private static final int REQUEST_CHON_DICH_VU = 100;
    
    private AutoCompleteTextView actvBenhNhan, actvBacSi;
    private TextView tvThongTinBenhNhan, tvDichVuChon, tvPhiKham;
    private TextInputEditText edtChanDoan, edtGhiChu, edtSoNgayUong;
    private RecyclerView rvDonThuoc;
    private MaterialButton btnThemThuoc, btnLuuBenhAn, btnChonDichVu;
    private ProgressBar progressBar;
    private View cardThongTinBenhNhan, cardDichVuChon;
    
    private FirestoreRepository repository;
    private ThuocKeDonAdapter thuocAdapter;
    private List<ChiTietDonThuoc> danhSachThuoc;
    private List<BenhNhan> danhSachBenhNhan;
    private List<com.example.doannt118.model.BacSi> danhSachBacSi;
    private String maBenhNhanChon = "";
    private String maBacSi = "";

    private String maLichKham = "";
    private String maMaKham = "";
    private String maBenhAnDangTao = ""; // Lưu mã bệnh án đang tạo
    
    private List<com.example.doannt118.model.DichVuKham> dichVuChon = new ArrayList<>();
    private long tongPhiDichVu = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tao_benh_an);
        
        // Nhận thông tin từ HoSoBenhNhanActivity
        String maBenhNhanIntent = getIntent().getStringExtra("MA_BENH_NHAN");
        maLichKham = getIntent().getStringExtra("MA_LICH_KHAM");
        maMaKham = getIntent().getStringExtra("MA_MA_KHAM");
        
        initViews();
        setupToolbar();
        setupRecyclerView();
        
        // Load danh sách bác sĩ và bệnh nhân
        loadBacSi();
        loadBenhNhan();
        setupListeners();
        
        // Nếu có mã bệnh nhân từ intent, tự động load thông tin
        if (maBenhNhanIntent != null && !maBenhNhanIntent.isEmpty()) {
            maBenhNhanChon = maBenhNhanIntent;
            loadThongTinBenhNhan(maBenhNhanIntent);
        }
    }
    
    private void loadBacSi() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll("BacSi",
            querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                danhSachBacSi.clear();
                String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                boolean foundMatch = false;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.BacSi bs = doc.toObject(com.example.doannt118.model.BacSi.class);
                    if (bs != null) {
                        danhSachBacSi.add(bs);
                        
                        // Tự động chọn bác sĩ nếu maTaiKhoan khớp với user hiện tại
                        if (bs.getMaTaiKhoan() != null && bs.getMaTaiKhoan().equals(userId)) {
                            maBacSi = bs.getMaBacSi();
                            actvBacSi.setText(bs.getHoTen());
                            actvBacSi.setEnabled(false); // Khóa không cho chọn bác sĩ khác
                            foundMatch = true;
                            Log.d("TaoBenhAn", "Auto-selected maBacSi: " + maBacSi);
                        }
                    }
                }
                
                if (!foundMatch) {
                    // Không tìm thấy bác sĩ có maTaiKhoan khớp
                    Log.e("TaoBenhAn", "Không tìm thấy bác sĩ với maTaiKhoan: " + userId);
                    Toast.makeText(TaoBenhAnActivity.this, 
                        "Lỗi: Tài khoản bác sĩ chưa được liên kết. Vui lòng chọn bác sĩ thủ công.", 
                        Toast.LENGTH_LONG).show();
                    actvBacSi.setEnabled(true); // Cho phép chọn thủ công
                }
                
                setupBacSiDropdown();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tải danh sách bác sĩ: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void initViews() {
        actvBenhNhan = findViewById(R.id.actvBenhNhan);
        actvBacSi = findViewById(R.id.actvBacSi);
        tvThongTinBenhNhan = findViewById(R.id.tvThongTinBenhNhan);
        tvDichVuChon = findViewById(R.id.tvDichVuChon);
        tvPhiKham = findViewById(R.id.tvPhiKham);
        edtChanDoan = findViewById(R.id.edtChanDoan);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        edtSoNgayUong = findViewById(R.id.edtSoNgayUong);
        rvDonThuoc = findViewById(R.id.rvDonThuoc);
        btnThemThuoc = findViewById(R.id.btnThemThuoc);
        btnLuuBenhAn = findViewById(R.id.btnLuuBenhAn);
        btnChonDichVu = findViewById(R.id.btnChonDichVu);
        progressBar = findViewById(R.id.progressBar);
        cardThongTinBenhNhan = findViewById(R.id.cardThongTinBenhNhan);
        cardDichVuChon = findViewById(R.id.cardDichVuChon);
        
        repository = new FirestoreRepository();
        danhSachThuoc = new ArrayList<>();
        danhSachBenhNhan = new ArrayList<>();
        danhSachBacSi = new ArrayList<>();
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
        thuocAdapter = new ThuocKeDonAdapter(this, danhSachThuoc, position -> {
            danhSachThuoc.remove(position);
            thuocAdapter.notifyItemRemoved(position);
        });
        rvDonThuoc.setLayoutManager(new LinearLayoutManager(this));
        rvDonThuoc.setAdapter(thuocAdapter);
    }

    private void loadBenhNhan() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAll("BenhNhan",
            querySnapshot -> {
                progressBar.setVisibility(View.GONE);
                danhSachBenhNhan.clear();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    BenhNhan bn = doc.toObject(BenhNhan.class);
                    if (bn != null) {
                        danhSachBenhNhan.add(bn);
                    }
                }
                setupBenhNhanDropdown();
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tải danh sách bệnh nhân: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void setupBacSiDropdown() {
        List<String> tenBacSi = new ArrayList<>();
        for (com.example.doannt118.model.BacSi bs : danhSachBacSi) {
            tenBacSi.add(bs.getHoTen());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            tenBacSi
        );
        actvBacSi.setAdapter(adapter);
        
        actvBacSi.setOnItemClickListener((parent, view, position, id) -> {
            com.example.doannt118.model.BacSi bs = danhSachBacSi.get(position);
            maBacSi = bs.getMaBacSi();
            Log.d("TaoBenhAn", "Selected maBacSi: " + maBacSi);
        });
    }
    
    private void setupBenhNhanDropdown() {
        List<String> tenBenhNhan = new ArrayList<>();
        for (BenhNhan bn : danhSachBenhNhan) {
            tenBenhNhan.add(bn.getHoTen());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_dropdown_item_1line, 
            tenBenhNhan
        );
        actvBenhNhan.setAdapter(adapter);
        
        actvBenhNhan.setOnItemClickListener((parent, view, position, id) -> {
            BenhNhan bn = danhSachBenhNhan.get(position);
            maBenhNhanChon = bn.getMaBenhNhan();
            showThongTinBenhNhan(bn);
        });
    }

    private void loadThongTinBenhNhan(String maBenhNhan) {
        repository.getCollection("BenhNhan")
            .document(maBenhNhan)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    BenhNhan bn = documentSnapshot.toObject(BenhNhan.class);
                    if (bn != null) {
                        actvBenhNhan.setText(bn.getHoTen());
                        showThongTinBenhNhan(bn);
                    }
                }
            })
            .addOnFailureListener(e -> Log.e("TaoBenhAn", "Lỗi load bệnh nhân", e));
    }

    private void showThongTinBenhNhan(BenhNhan bn) {
        String thongTin = "📅 Ngày sinh: " + bn.getNgaySinh() + "\n" +
                         "📞 SĐT: " + bn.getSoDienThoai();
        tvThongTinBenhNhan.setText(thongTin);
        cardThongTinBenhNhan.setVisibility(View.VISIBLE);
    }

    private void setupListeners() {
        btnChonDichVu.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChonDichVuKhamActivity.class);
            startActivityForResult(intent, REQUEST_CHON_DICH_VU);
        });
        
        btnThemThuoc.setOnClickListener(v -> showDialogThemThuoc());
        
        btnLuuBenhAn.setOnClickListener(v -> luuBenhAn());
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHON_DICH_VU && resultCode == RESULT_OK && data != null) {
            dichVuChon = (List<com.example.doannt118.model.DichVuKham>) data.getSerializableExtra("DICH_VU_CHON");
            tongPhiDichVu = data.getLongExtra("TONG_TIEN", 0);
            
            if (dichVuChon != null && !dichVuChon.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (com.example.doannt118.model.DichVuKham dv : dichVuChon) {
                    sb.append("• ").append(dv.getTenDichVu()).append("\n");
                }
                tvDichVuChon.setText(sb.toString());
                cardDichVuChon.setVisibility(View.VISIBLE);
                
                tvPhiKham.setText(String.format("%,d đ", tongPhiDichVu));
                tvPhiKham.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showDialogThemThuoc() {
        // Hiển thị dialog chọn thuốc trước
        showDialogChonThuocTaoBenhAn();
    }

    private void showDialogChonThuocTaoBenhAn() {
        android.app.Dialog dialog = new android.app.Dialog(this);
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
                List<com.example.doannt118.model.DuocPham> danhSachDuocPham = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.DuocPham duocPham = doc.toObject(com.example.doannt118.model.DuocPham.class);
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
                        showDialogNhapThongTinThuocTaoBenhAn(duocPham);
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

    private void showDialogNhapThongTinThuocTaoBenhAn(com.example.doannt118.model.DuocPham duocPham) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_them_thuoc, null);
        builder.setView(dialogView);
        
        AutoCompleteTextView edtTenThuoc = dialogView.findViewById(R.id.edtTenThuoc);
        TextInputEditText edtSoLuong = dialogView.findViewById(R.id.edtSoLuong);
        TextInputEditText edtSoNgayUong = dialogView.findViewById(R.id.edtSoNgayUong);
        TextInputEditText edtSoLanMoiNgay = dialogView.findViewById(R.id.edtSoLanMoiNgay);
        TextInputEditText edtSoVienMoiLan = dialogView.findViewById(R.id.edtSoVienMoiLan);
        TextInputEditText edtCachDung = dialogView.findViewById(R.id.edtCachDung);
        CheckBox cbSang = dialogView.findViewById(R.id.cbSang);
        CheckBox cbTrua = dialogView.findViewById(R.id.cbTrua);
        CheckBox cbChieu = dialogView.findViewById(R.id.cbChieu);
        MaterialButton btnXacNhan = dialogView.findViewById(R.id.btnXacNhan);
        MaterialButton btnHuy = dialogView.findViewById(R.id.btnHuy);
        
        // Set tên thuốc đã chọn
        edtTenThuoc.setText(duocPham.getTenDuocPham());
        edtTenThuoc.setEnabled(false);
        
        android.app.AlertDialog dialog = builder.create();
        
        btnXacNhan.setOnClickListener(v -> {
            String soLuongStr = edtSoLuong.getText().toString();
            String soNgayUongStr = edtSoNgayUong.getText().toString();
            String soLanMoiNgayStr = edtSoLanMoiNgay.getText().toString();
            String soVienMoiLanStr = edtSoVienMoiLan.getText().toString();
            String cachDung = edtCachDung.getText().toString();
            
            if (soLuongStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!cbSang.isChecked() && !cbTrua.isChecked() && !cbChieu.isChecked()) {
                Toast.makeText(this, "Vui lòng chọn ít nhất 1 ca uống", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int soLuong = Integer.parseInt(soLuongStr);
            int soNgayUong = soNgayUongStr.isEmpty() ? 7 : Integer.parseInt(soNgayUongStr);
            int soLanMoiNgay = soLanMoiNgayStr.isEmpty() ? 2 : Integer.parseInt(soLanMoiNgayStr);
            int soVienMoiLan = soVienMoiLanStr.isEmpty() ? 1 : Integer.parseInt(soVienMoiLanStr);
            
            // Tạo liều dùng từ thông tin đã nhập
            String lieuDung = "Uống " + soVienMoiLan + " viên/lần, " + soLanMoiNgay + " lần/ngày";
            if (!cachDung.isEmpty()) {
                lieuDung += " - " + cachDung;
            }
            
            ChiTietDonThuoc chiTiet = new ChiTietDonThuoc();
            chiTiet.setMaDuocPham(duocPham.getMaDuocPham());
            chiTiet.setTenThuoc(duocPham.getTenDuocPham());
            chiTiet.setSoLuong(soLuong);
            chiTiet.setLieuDung(lieuDung);
            chiTiet.setSoNgayUong(soNgayUong);
            chiTiet.setSoLanMoiNgay(soLanMoiNgay);
            chiTiet.setSoVienMoiLan(soVienMoiLan);
            chiTiet.setCachDung(cachDung);
            chiTiet.setUongSang(cbSang.isChecked());
            chiTiet.setUongTrua(cbTrua.isChecked());
            chiTiet.setUongChieu(cbChieu.isChecked());
            chiTiet.setUongToi(false); // Không có ca tối
            
            danhSachThuoc.add(chiTiet);
            thuocAdapter.notifyItemInserted(danhSachThuoc.size() - 1);
            
            Toast.makeText(this, "Đã thêm thuốc", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            
            // Nếu đang trong luồng tạo bệnh án (có maBenhAnDangTao)
            if (maBenhAnDangTao != null && !maBenhAnDangTao.isEmpty()) {
                // Hỏi có muốn thêm thuốc nữa không
                showDialogThemThuocNua();
            }
        });
        
        btnHuy.setOnClickListener(v -> {
            dialog.dismiss();
            // Nếu đang trong luồng tạo bệnh án và đã có thuốc
            if (maBenhAnDangTao != null && !maBenhAnDangTao.isEmpty() && !danhSachThuoc.isEmpty()) {
                showDialogThemThuocNua();
            } else if (maBenhAnDangTao != null && !maBenhAnDangTao.isEmpty()) {
                // Không có thuốc nào, kết thúc
                Toast.makeText(this, "Tạo bệnh án thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        dialog.show();
    }
    
    private void showDialogThemThuocNua() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Thêm thuốc")
            .setMessage("Bạn có muốn thêm thuốc khác không?")
            .setPositiveButton("Có", (dialog, which) -> {
                showDialogChonThuocTaoBenhAn();
            })
            .setNegativeButton("Không, lưu đơn thuốc", (dialog, which) -> {
                if (!danhSachThuoc.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    taoDonThuoc(maBenhAnDangTao);
                } else {
                    Toast.makeText(this, "Tạo bệnh án thành công", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .setCancelable(false)
            .show();
    }
    
    private void luuBenhAn() {
        String chanDoan = edtChanDoan.getText().toString().trim();
        String ghiChu = edtGhiChu.getText().toString().trim();
        
        if (maBenhNhanChon.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn bệnh nhân", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (chanDoan.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập chẩn đoán", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnLuuBenhAn.setEnabled(false);
        
        // Tạo bệnh án
        Map<String, Object> benhAn = new HashMap<>();
        benhAn.put("maBenhNhan", maBenhNhanChon);
        benhAn.put("maBacSi", maBacSi);
        benhAn.put("chanDoan", chanDoan);
        benhAn.put("ghiChu", ghiChu);
        benhAn.put("ngayKham", com.google.firebase.Timestamp.now());
        benhAn.put("trangThai", "Đã khám");
        
        // Thêm thông tin dịch vụ khám
        if (dichVuChon != null && !dichVuChon.isEmpty()) {
            benhAn.put("loaiKham", dichVuChon.get(0).getTenDichVu());
            benhAn.put("maDichVuKham", dichVuChon.get(0).getMaDichVu());
            benhAn.put("phiKham", tongPhiDichVu);
        } else {
            benhAn.put("loaiKham", "Khám tổng quát");
            benhAn.put("phiKham", 0L);
        }
        
        String maBenhAn = "BA" + System.currentTimeMillis();
        benhAn.put("maBenhAn", maBenhAn);
        
        repository.addDocument("BenhAn", maBenhAn, benhAn,
            aVoid -> {
                // Cập nhật trạng thái lịch khám nếu có
                if (maLichKham != null && !maLichKham.isEmpty()) {
                    capNhatTrangThaiLichKham(maLichKham);
                }
                
                progressBar.setVisibility(View.GONE);
                
                // Hỏi có muốn kê đơn thuốc không
                showDialogKeDonThuoc(maBenhAn);
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                btnLuuBenhAn.setEnabled(true);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tạo bệnh án: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void taoDonThuoc(String maBenhAn) {
        String maDonThuoc = "DT" + System.currentTimeMillis();
        
        // Tạo đơn thuốc
        Map<String, Object> donThuoc = new HashMap<>();
        donThuoc.put("maDonThuoc", maDonThuoc);
        donThuoc.put("maBenhAn", maBenhAn);
        donThuoc.put("maBenhNhan", maBenhNhanChon);
        donThuoc.put("maBacSi", maBacSi);
        donThuoc.put("ngayLap", com.google.firebase.Timestamp.now());
        
        repository.addDocument("DonThuoc", maDonThuoc, donThuoc,
            aVoid -> {
                // Thêm chi tiết đơn thuốc
                int[] count = {0};
                for (ChiTietDonThuoc ct : danhSachThuoc) {
                    String maChiTiet = maDonThuoc + "_" + count[0]++;
                    Map<String, Object> chiTiet = new HashMap<>();
                    chiTiet.put("maChiTiet", maChiTiet);
                    chiTiet.put("maDonThuoc", maDonThuoc);
                    chiTiet.put("maDuocPham", ct.getMaDuocPham());
                    chiTiet.put("tenThuoc", ct.getTenThuoc());
                    chiTiet.put("soLuong", ct.getSoLuong());
                    chiTiet.put("lieuDung", ct.getLieuDung());
                    
                    // Thêm thông tin ca uống thuốc
                    chiTiet.put("soNgayUong", ct.getSoNgayUong());
                    chiTiet.put("soLanMoiNgay", ct.getSoLanMoiNgay());
                    chiTiet.put("soVienMoiLan", ct.getSoVienMoiLan());
                    chiTiet.put("uongSang", ct.isUongSang());
                    chiTiet.put("uongTrua", ct.isUongTrua());
                    chiTiet.put("uongChieu", ct.isUongChieu());
                    chiTiet.put("uongToi", ct.isUongToi());
                    chiTiet.put("cachDung", ct.getCachDung());
                    
                    repository.addDocument("ChiTietDonThuoc", maChiTiet, chiTiet,
                        v -> {},
                        e -> Log.e("TaoBenhAn", "Lỗi thêm chi tiết: " + e.getMessage())
                    );
                }
                
                // Sau khi tạo đơn thuốc xong, tạo hóa đơn
                taoHoaDonTuDong(maBenhAn);
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                btnLuuBenhAn.setEnabled(true);
                Toast.makeText(TaoBenhAnActivity.this, 
                    "Lỗi tạo đơn thuốc: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private void showDialogKeDonThuoc(String maBenhAn) {
        maBenhAnDangTao = maBenhAn; // Lưu lại để dùng sau
        
        new android.app.AlertDialog.Builder(this)
            .setTitle("Kê đơn thuốc")
            .setMessage("Bạn có muốn kê đơn thuốc cho bệnh nhân không?")
            .setPositiveButton("Có", (dialog, which) -> {
                // Nếu đã có thuốc trong danh sách thì lưu luôn
                if (!danhSachThuoc.isEmpty()) {
                    progressBar.setVisibility(View.VISIBLE);
                    taoDonThuoc(maBenhAn);
                } else {
                    // Chưa có thuốc thì cho chọn
                    showDialogChonThuocTaoBenhAn();
                }
            })
            .setNegativeButton("Không", (dialog, which) -> {
                // Không kê đơn thuốc nhưng vẫn tạo hóa đơn cho dịch vụ khám
                taoHoaDonTuDong(maBenhAn);
            })
            .setCancelable(false)
            .show();
    }
    
    /**
     * Tạo hóa đơn tự động sau khi hoàn thành bệnh án và đơn thuốc
     */
    private void taoHoaDonTuDong(String maBenhAn) {
        String maHoaDon = "HD_" + System.currentTimeMillis();
        Date ngayLap = new Date();
        
        // Tính toán các loại phí
        long phiKham = tongPhiDichVu; // Phí dịch vụ khám đã chọn
        long phiThuoc = 0; // Sẽ tính sau từ database
        long phiDichVu = 0; // Có thể mở rộng sau cho các dịch vụ khác
        
        double tongTien = phiKham; // Tạm thời chỉ có phí khám, phí thuốc sẽ cập nhật sau
        
        // Tạo hóa đơn với phí thuốc = 0 trước
        com.example.doannt118.model.HoaDon hoaDon = new com.example.doannt118.model.HoaDon();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setMaBenhAn(maBenhAn);
        hoaDon.setMaBenhNhan(maBenhNhanChon);
        hoaDon.setNgayLap(ngayLap);
        hoaDon.setTongTien(tongTien);
        hoaDon.setPhiKham(phiKham);
        hoaDon.setPhiThuoc(phiThuoc);
        hoaDon.setPhiDichVu(phiDichVu);
        hoaDon.setTrangThai("CHUA_THANH_TOAN");
        
        repository.addDocument("HoaDon", maHoaDon, hoaDon,
            aVoid -> {
                // Tạo chi tiết hóa đơn và tính phí thuốc thực tế
                taoChiTietHoaDonVaTinhPhiThuoc(maHoaDon);
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Lỗi tạo hóa đơn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                // Vẫn thông báo thành công vì bệnh án đã tạo
                Toast.makeText(this, "Tạo bệnh án thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        );
    }
    
    /**
     * Tính phí thuốc từ đơn thuốc - lấy giá thực từ database
     */
    private void tinhPhiThuocThucTe(String maHoaDon, Runnable onComplete) {
        if (danhSachThuoc.isEmpty()) {
            // Không có thuốc, hoàn thành luôn
            onComplete.run();
            return;
        }
        
        final long[] tongPhiThuoc = {0};
        final int[] count = {0};
        final int total = danhSachThuoc.size();
        
        for (ChiTietDonThuoc thuoc : danhSachThuoc) {
            // Lấy giá thuốc từ database
            repository.getByField("DuocPham", "maDuocPham", thuoc.getMaDuocPham(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        com.example.doannt118.model.DuocPham duocPham = 
                            querySnapshot.getDocuments().get(0).toObject(com.example.doannt118.model.DuocPham.class);
                        if (duocPham != null) {
                            tongPhiThuoc[0] += (long)(thuoc.getSoLuong() * duocPham.getGiaBan());
                        }
                    } else {
                        // Không tìm thấy thuốc, dùng giá mặc định
                        tongPhiThuoc[0] += thuoc.getSoLuong() * 10000;
                    }
                    
                    count[0]++;
                    if (count[0] == total) {
                        // Đã tính xong tất cả thuốc, cập nhật hóa đơn
                        capNhatPhiThuocTrongHoaDon(maHoaDon, tongPhiThuoc[0], onComplete);
                    }
                },
                e -> {
                    Log.e("TaoBenhAn", "Lỗi lấy giá thuốc: " + e.getMessage());
                    // Dùng giá mặc định khi có lỗi
                    tongPhiThuoc[0] += thuoc.getSoLuong() * 10000;
                    
                    count[0]++;
                    if (count[0] == total) {
                        capNhatPhiThuocTrongHoaDon(maHoaDon, tongPhiThuoc[0], onComplete);
                    }
                }
            );
        }
    }
    
    /**
     * Cập nhật phí thuốc trong hóa đơn sau khi tính toán xong
     */
    private void capNhatPhiThuocTrongHoaDon(String maHoaDon, long phiThuocThucTe, Runnable onComplete) {
        // Cập nhật phí thuốc và tổng tiền trong hóa đơn
        Map<String, Object> updates = new HashMap<>();
        updates.put("phiThuoc", phiThuocThucTe);
        updates.put("tongTien", (double)(tongPhiDichVu + phiThuocThucTe));
        
        repository.updateDocumentFields("HoaDon", maHoaDon, updates,
            aVoid -> {
                Log.d("TaoBenhAn", "Đã cập nhật phí thuốc: " + phiThuocThucTe);
                onComplete.run();
            },
            e -> {
                Log.e("TaoBenhAn", "Lỗi cập nhật phí thuốc: " + e.getMessage());
                // Vẫn tiếp tục dù có lỗi
                onComplete.run();
            }
        );
    }
    
    /**
     * Tạo chi tiết hóa đơn và tính phí thuốc thực tế từ database
     */
    private void taoChiTietHoaDonVaTinhPhiThuoc(String maHoaDon) {
        List<com.example.doannt118.model.ChiTietHoaDon> danhSachChiTiet = new ArrayList<>();
        
        // Thêm chi tiết cho dịch vụ khám
        if (dichVuChon != null && !dichVuChon.isEmpty()) {
            for (int i = 0; i < dichVuChon.size(); i++) {
                com.example.doannt118.model.DichVuKham dv = dichVuChon.get(i);
                String maChiTiet = "CTHD_DV_" + maHoaDon + "_" + i + "_" + dv.getMaDichVu();
                com.example.doannt118.model.ChiTietHoaDon chiTiet = new com.example.doannt118.model.ChiTietHoaDon();
                chiTiet.setMaChiTiet(maChiTiet);
                chiTiet.setMaHoaDon(maHoaDon);
                chiTiet.setTenDichVu(dv.getTenDichVu());
                chiTiet.setSoLuong(1);
                chiTiet.setDonGia(dv.getGiaTien());
                danhSachChiTiet.add(chiTiet);
            }
        }
        
        // Thêm chi tiết cho thuốc - sẽ cập nhật giá sau
        taoChiTietThuocVoiGiaThucTe(maHoaDon, danhSachChiTiet);
    }
    
    /**
     * Tạo chi tiết thuốc với giá thực tế từ database
     */
    private void taoChiTietThuocVoiGiaThucTe(String maHoaDon, List<com.example.doannt118.model.ChiTietHoaDon> danhSachChiTietDichVu) {
        if (danhSachThuoc.isEmpty()) {
            // Không có thuốc, lưu chi tiết dịch vụ và hoàn thành
            luuTatCaChiTietHoaDon(danhSachChiTietDichVu, 0);
            return;
        }
        
        final List<com.example.doannt118.model.ChiTietHoaDon> danhSachChiTietThuoc = new ArrayList<>();
        final int[] count = {0};
        final int total = danhSachThuoc.size();
        
        for (int i = 0; i < danhSachThuoc.size(); i++) {
            ChiTietDonThuoc thuoc = danhSachThuoc.get(i);
            final int index = i; // Để sử dụng trong lambda
            
            // Lấy giá thuốc từ database
            repository.getByField("DuocPham", "maDuocPham", thuoc.getMaDuocPham(),
                querySnapshot -> {
                    double giaThuoc = 10000; // Giá mặc định
                    
                    if (!querySnapshot.isEmpty()) {
                        com.example.doannt118.model.DuocPham duocPham = 
                            querySnapshot.getDocuments().get(0).toObject(com.example.doannt118.model.DuocPham.class);
                        if (duocPham != null) {
                            giaThuoc = duocPham.getGiaBan();
                        }
                    }
                    
                    // Tạo chi tiết hóa đơn cho thuốc với ID unique
                    String maChiTiet = "CTHD_THUOC_" + maHoaDon + "_" + index + "_" + thuoc.getMaDuocPham();
                    com.example.doannt118.model.ChiTietHoaDon chiTiet = new com.example.doannt118.model.ChiTietHoaDon();
                    chiTiet.setMaChiTiet(maChiTiet);
                    chiTiet.setMaHoaDon(maHoaDon);
                    chiTiet.setTenDichVu(thuoc.getTenThuoc());
                    chiTiet.setSoLuong(thuoc.getSoLuong());
                    chiTiet.setDonGia(giaThuoc);
                    
                    synchronized (danhSachChiTietThuoc) {
                        danhSachChiTietThuoc.add(chiTiet);
                    }
                    
                    count[0]++;
                    if (count[0] == total) {
                        // Đã lấy xong giá tất cả thuốc
                        // Gộp chi tiết dịch vụ và thuốc
                        List<com.example.doannt118.model.ChiTietHoaDon> tatCaChiTiet = new ArrayList<>();
                        tatCaChiTiet.addAll(danhSachChiTietDichVu);
                        tatCaChiTiet.addAll(danhSachChiTietThuoc);
                        
                        // Tính tổng phí thuốc
                        long tongPhiThuoc = 0;
                        for (com.example.doannt118.model.ChiTietHoaDon ct : danhSachChiTietThuoc) {
                            tongPhiThuoc += (long)(ct.getSoLuong() * ct.getDonGia());
                        }
                        
                        // Cập nhật phí thuốc trong hóa đơn
                        capNhatPhiThuocTrongHoaDon(maHoaDon, tongPhiThuoc, () -> {
                            // Lưu tất cả chi tiết hóa đơn
                            luuTatCaChiTietHoaDon(tatCaChiTiet, 0);
                        });
                    }
                },
                e -> {
                    Log.e("TaoBenhAn", "Lỗi lấy giá thuốc: " + e.getMessage());
                    
                    // Dùng giá mặc định khi có lỗi
                    String maChiTiet = "CTHD_THUOC_" + maHoaDon + "_" + index + "_" + thuoc.getMaDuocPham();
                    com.example.doannt118.model.ChiTietHoaDon chiTiet = new com.example.doannt118.model.ChiTietHoaDon();
                    chiTiet.setMaChiTiet(maChiTiet);
                    chiTiet.setMaHoaDon(maHoaDon);
                    chiTiet.setTenDichVu(thuoc.getTenThuoc());
                    chiTiet.setSoLuong(thuoc.getSoLuong());
                    chiTiet.setDonGia(10000); // Giá mặc định
                    
                    synchronized (danhSachChiTietThuoc) {
                        danhSachChiTietThuoc.add(chiTiet);
                    }
                    
                    count[0]++;
                    if (count[0] == total) {
                        // Gộp và lưu
                        List<com.example.doannt118.model.ChiTietHoaDon> tatCaChiTiet = new ArrayList<>();
                        tatCaChiTiet.addAll(danhSachChiTietDichVu);
                        tatCaChiTiet.addAll(danhSachChiTietThuoc);
                        
                        long tongPhiThuoc = 0;
                        for (com.example.doannt118.model.ChiTietHoaDon ct : danhSachChiTietThuoc) {
                            tongPhiThuoc += (long)(ct.getSoLuong() * ct.getDonGia());
                        }
                        
                        capNhatPhiThuocTrongHoaDon(maHoaDon, tongPhiThuoc, () -> {
                            luuTatCaChiTietHoaDon(tatCaChiTiet, 0);
                        });
                    }
                }
            );
        }
    }
    
    /**
     * Lưu tất cả chi tiết hóa đơn theo thứ tự
     */
    private void luuTatCaChiTietHoaDon(List<com.example.doannt118.model.ChiTietHoaDon> danhSach, int index) {
        if (index >= danhSach.size()) {
            // Đã lưu xong tất cả, gửi thông báo và kết thúc
            guiThongBaoHoaDonMoi();
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Tạo bệnh án, đơn thuốc và hóa đơn thành công!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        com.example.doannt118.model.ChiTietHoaDon chiTiet = danhSach.get(index);
        repository.addDocument("ChiTietHoaDon", chiTiet.getMaChiTiet(), chiTiet,
            aVoid -> {
                // Lưu chi tiết tiếp theo
                luuTatCaChiTietHoaDon(danhSach, index + 1);
            },
            e -> {
                Log.e("TaoBenhAn", "Lỗi lưu chi tiết hóa đơn: " + e.getMessage());
                // Tiếp tục lưu chi tiết tiếp theo dù có lỗi
                luuTatCaChiTietHoaDon(danhSach, index + 1);
            }
        );
    }
    
    /**
     * Gửi thông báo hóa đơn mới cho bệnh nhân
     */
    private void guiThongBaoHoaDonMoi() {
        // Lấy tổng tiền từ hóa đơn đã được cập nhật
        repository.getByField("HoaDon", "maBenhNhan", maBenhNhanChon,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    // Lấy hóa đơn mới nhất (theo thời gian tạo)
                    com.example.doannt118.model.HoaDon hoaDonMoiNhat = null;
                    for (var doc : querySnapshot.getDocuments()) {
                        com.example.doannt118.model.HoaDon hd = doc.toObject(com.example.doannt118.model.HoaDon.class);
                        if (hd != null && (hoaDonMoiNhat == null || 
                            hd.getNgayLap().after(hoaDonMoiNhat.getNgayLap()))) {
                            hoaDonMoiNhat = hd;
                        }
                    }
                    
                    if (hoaDonMoiNhat != null) {
                        long tongTien = (long)hoaDonMoiNhat.getTongTien();
                        
                        com.example.doannt118.utils.NotificationHelper helper = 
                            new com.example.doannt118.utils.NotificationHelper(this);
                        helper.guiThongBaoTuBacSi(maBenhNhanChon, null,
                            "Hóa đơn khám bệnh",
                            "Bạn có hóa đơn mới với tổng tiền " + String.format("%,d đ", tongTien) + 
                            ". Vui lòng kiểm tra và thanh toán.");
                    }
                }
            },
            e -> Log.e("TaoBenhAn", "Lỗi lấy thông tin hóa đơn để gửi thông báo: " + e.getMessage())
        );
    }
    
    private void capNhatTrangThaiLichKham(String maLichKham) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", "HOAN_THANH");
        
        repository.updateDocumentFields("LichKham", maLichKham, updates,
            aVoid -> Log.d("TaoBenhAn", "Đã cập nhật trạng thái lịch khám"),
            e -> Log.e("TaoBenhAn", "Lỗi cập nhật trạng thái lịch khám: " + e.getMessage())
        );
    }
}
