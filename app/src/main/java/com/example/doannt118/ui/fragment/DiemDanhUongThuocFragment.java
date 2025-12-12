package com.example.doannt118.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.model.DonThuoc;
import com.example.doannt118.model.XacNhanUongThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.ui.DiemDanhThuocAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DiemDanhUongThuocFragment extends Fragment {
    
    private TextView tvNgayHomNay;
    private View tvEmpty;
    private RecyclerView rvCaSang, rvCaTrua, rvCaChieu;
    private View layoutCaSang, layoutCaTrua, layoutCaChieu;
    private View progressBar;
    
    private DiemDanhThuocAdapter adapterSang, adapterTrua, adapterChieu;
    private FirestoreRepository repository;
    private String maBenhNhan;
    private SimpleDateFormat dateFormat;
    private Date ngayHomNay;

    public static DiemDanhUongThuocFragment newInstance(String maBenhNhan) {
        DiemDanhUongThuocFragment fragment = new DiemDanhUongThuocFragment();
        Bundle args = new Bundle();
        args.putString("MA_BENH_NHAN", maBenhNhan);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maBenhNhan = getArguments().getString("MA_BENH_NHAN");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_diem_danh_uong_thuoc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerViews();
        
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        loadThuocHomNay();
    }

    private void initViews(View view) {
        tvNgayHomNay = view.findViewById(R.id.tvNgayHomNay);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        
        layoutCaSang = view.findViewById(R.id.layoutCaSang);
        layoutCaTrua = view.findViewById(R.id.layoutCaTrua);
        layoutCaChieu = view.findViewById(R.id.layoutCaChieu);
        
        rvCaSang = view.findViewById(R.id.rvCaSang);
        rvCaTrua = view.findViewById(R.id.rvCaTrua);
        rvCaChieu = view.findViewById(R.id.rvCaChieu);
        
        repository = new FirestoreRepository();
        dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        
        // Lấy ngày hôm nay (chỉ lấy ngày, bỏ giờ)
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        ngayHomNay = cal.getTime();
        
        tvNgayHomNay.setText(dateFormat.format(ngayHomNay));
    }

    private void setupRecyclerViews() {
        adapterSang = new DiemDanhThuocAdapter(getContext(), "SANG", this::onDiemDanh);
        adapterTrua = new DiemDanhThuocAdapter(getContext(), "TRUA", this::onDiemDanh);
        adapterChieu = new DiemDanhThuocAdapter(getContext(), "CHIEU", this::onDiemDanh);
        
        rvCaSang.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCaSang.setAdapter(adapterSang);
        
        rvCaTrua.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCaTrua.setAdapter(adapterTrua);
        
        rvCaChieu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCaChieu.setAdapter(adapterChieu);
    }

    private void loadThuocHomNay() {
        showLoading(true);
        
        // Load tất cả đơn thuốc đang active của bệnh nhân
        repository.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<String> danhSachMaDonThuoc = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    DonThuoc donThuoc = doc.toObject(DonThuoc.class);
                    if (donThuoc != null) {
                        String trangThai = donThuoc.getTrangThai();
                        if (trangThai == null || "DANG_DUNG".equals(trangThai)) {
                            danhSachMaDonThuoc.add(donThuoc.getMaDonThuoc());
                        }
                    }
                }
                
                if (danhSachMaDonThuoc.isEmpty()) {
                    showLoading(false);
                    showEmpty(true);
                    Toast.makeText(getContext(), "Không có đơn thuốc nào đang sử dụng", Toast.LENGTH_LONG).show();
                    return;
                }
                
                loadChiTietThuoc(danhSachMaDonThuoc);
            },
            e -> {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadChiTietThuoc(List<String> danhSachMaDonThuoc) {
        List<ChiTietDonThuoc> tatCaThuoc = new ArrayList<>();
        final int[] count = {0};
        
        for (String maDonThuoc : danhSachMaDonThuoc) {
            repository.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
                querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
                        if (chiTiet != null) {
                            tatCaThuoc.add(chiTiet);
                        }
                    }
                    
                    count[0]++;
                    if (count[0] == danhSachMaDonThuoc.size()) {
                        phanLoaiThuocTheoCa(tatCaThuoc);
                    }
                },
                e -> {
                    count[0]++;
                    if (count[0] == danhSachMaDonThuoc.size()) {
                        phanLoaiThuocTheoCa(tatCaThuoc);
                    }
                }
            );
        }
    }

    private void phanLoaiThuocTheoCa(List<ChiTietDonThuoc> tatCaThuoc) {
        List<ChiTietDonThuoc> thuocSang = new ArrayList<>();
        List<ChiTietDonThuoc> thuocTrua = new ArrayList<>();
        List<ChiTietDonThuoc> thuocChieu = new ArrayList<>();
        
        for (ChiTietDonThuoc thuoc : tatCaThuoc) {
            boolean coThongTinCaUong = thuoc.isUongSang() || thuoc.isUongTrua() || 
                                       thuoc.isUongChieu() || thuoc.isUongToi();
            
            if (!coThongTinCaUong) {
                thuocSang.add(thuoc);
                thuocTrua.add(thuoc);
                thuocChieu.add(thuoc);
            } else {
                if (thuoc.isUongSang()) thuocSang.add(thuoc);
                if (thuoc.isUongTrua()) thuocTrua.add(thuoc);
                if (thuoc.isUongChieu()) thuocChieu.add(thuoc);
            }
        }
        
        adapterSang.setData(thuocSang);
        adapterTrua.setData(thuocTrua);
        adapterChieu.setData(thuocChieu);
        
        layoutCaSang.setVisibility(thuocSang.isEmpty() ? View.GONE : View.VISIBLE);
        layoutCaTrua.setVisibility(thuocTrua.isEmpty() ? View.GONE : View.VISIBLE);
        layoutCaChieu.setVisibility(thuocChieu.isEmpty() ? View.GONE : View.VISIBLE);
        
        showLoading(false);
        showEmpty(tatCaThuoc.isEmpty());
    }

    private void onDiemDanh(ChiTietDonThuoc thuoc, String caUong) {
        String maXacNhan = "XN_" + UUID.randomUUID().toString();
        XacNhanUongThuoc xacNhan = new XacNhanUongThuoc();
        xacNhan.setMaXacNhan(maXacNhan);
        xacNhan.setMaChiTietDonThuoc(thuoc.getMaChiTiet());
        xacNhan.setMaBenhNhan(maBenhNhan);
        xacNhan.setDaUong(true);
        xacNhan.setThoiGianXacNhan(Timestamp.now());
        xacNhan.setGhiChu("Điểm danh ca " + caUong.toLowerCase());
        
        repository.addDocument("XacNhanUongThuoc", maXacNhan, xacNhan,
            aVoid -> {
                Toast.makeText(getContext(), "Đã xác nhận uống " + thuoc.getTenThuoc(), Toast.LENGTH_SHORT).show();
            },
            e -> {
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmpty(boolean show) {
        if (tvEmpty != null) {
            tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}