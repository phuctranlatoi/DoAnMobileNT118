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
import com.example.doannt118.ui.CaUongThuocAdapter;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiemDanhUongThuocFragment extends Fragment {
    
    private TextView tvNgayHomNay;
    private View tvEmpty;
    private RecyclerView rvCacCaUongThuoc;
    private View progressBar;
    
    private CaUongThuocAdapter caUongThuocAdapter;
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
        
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        setupRecyclerViews();
        loadThuocHomNay();
    }

    private void initViews(View view) {
        tvNgayHomNay = view.findViewById(R.id.tvNgayHomNay);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        progressBar = view.findViewById(R.id.progressBar);
        rvCacCaUongThuoc = view.findViewById(R.id.rvCacCaUongThuoc);
        
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
        caUongThuocAdapter = new CaUongThuocAdapter(getContext(), maBenhNhan, this::onXacNhanCa);
        rvCacCaUongThuoc.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCacCaUongThuoc.setAdapter(caUongThuocAdapter);
    }

    private void loadThuocHomNay() {
        showLoading(true);
        
        // Load tất cả đơn thuốc đang active của bệnh nhân
        repository.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<DonThuoc> danhSachDonThuoc = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    DonThuoc donThuoc = doc.toObject(DonThuoc.class);
                    if (donThuoc != null) {
                        String trangThai = donThuoc.getTrangThai();
                        
                        // Chỉ lấy đơn thuốc đang dùng
                        if (trangThai == null || "DANG_DUNG".equals(trangThai)) {
                            danhSachDonThuoc.add(donThuoc);
                        }
                    }
                }
                
                if (danhSachDonThuoc.isEmpty()) {
                    showLoading(false);
                    showEmpty(true);
                    Toast.makeText(getContext(), "Không có đơn thuốc nào đang sử dụng", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // Load chi tiết và kiểm tra hết hạn dựa trên soNgayUong từ ChiTietDonThuoc
                checkAndLoadChiTietThuoc(danhSachDonThuoc);
            },
            e -> {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private void checkAndLoadChiTietThuoc(List<DonThuoc> danhSachDonThuoc) {
        List<String> danhSachMaDonThuocConHan = new ArrayList<>();
        final int[] count = {0};
        
        for (DonThuoc donThuoc : danhSachDonThuoc) {
            String maDonThuoc = donThuoc.getMaDonThuoc();
            
            // Lấy soNgayUong từ ChiTietDonThuoc
            repository.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
                chiTietSnapshot -> {
                    int soNgayUong = 0;
                    for (DocumentSnapshot chiTietDoc : chiTietSnapshot.getDocuments()) {
                        Long soNgay = chiTietDoc.getLong("soNgayUong");
                        if (soNgay != null && soNgay > soNgayUong) {
                            soNgayUong = soNgay.intValue();
                        }
                    }
                    
                    // Lấy ngày kê đơn
                    Date ngayKeDon = null;
                    if (donThuoc.getNgayKeDon() != null) {
                        ngayKeDon = donThuoc.getNgayKeDon().toDate();
                    } else if (donThuoc.getNgayLap() != null) {
                        ngayKeDon = donThuoc.getNgayLap();
                    }
                    
                    android.util.Log.d("DiemDanhFragment", "DonThuoc " + maDonThuoc + 
                        ": soNgayUong=" + soNgayUong + ", ngayKeDon=" + ngayKeDon);
                    
                    // Kiểm tra nếu đã hết thời gian uống thuốc
                    boolean daHetHan = false;
                    if (ngayKeDon != null && soNgayUong > 0) {
                        Calendar calNgayKetThuc = Calendar.getInstance();
                        calNgayKetThuc.setTime(ngayKeDon);
                        calNgayKetThuc.add(Calendar.DAY_OF_MONTH, soNgayUong);
                        
                        // Nếu ngày kết thúc < ngày hôm nay → đã hoàn thành
                        if (calNgayKetThuc.getTime().before(ngayHomNay)) {
                            daHetHan = true;
                            // Cập nhật trạng thái trong Firestore
                            updateTrangThaiDonThuoc(maDonThuoc, "DA_HET");
                            android.util.Log.d("DiemDanhFragment", "DonThuoc " + maDonThuoc + " đã hết hạn");
                        }
                    }
                    
                    if (!daHetHan) {
                        synchronized (danhSachMaDonThuocConHan) {
                            danhSachMaDonThuocConHan.add(maDonThuoc);
                        }
                    }
                    
                    count[0]++;
                    if (count[0] == danhSachDonThuoc.size()) {
                        // Đã kiểm tra xong tất cả đơn thuốc
                        if (danhSachMaDonThuocConHan.isEmpty()) {
                            showLoading(false);
                            showEmpty(true);
                            Toast.makeText(getContext(), "Không có đơn thuốc nào đang sử dụng", Toast.LENGTH_LONG).show();
                        } else {
                            loadChiTietThuoc(danhSachMaDonThuocConHan);
                        }
                    }
                },
                e -> {
                    count[0]++;
                    if (count[0] == danhSachDonThuoc.size()) {
                        if (danhSachMaDonThuocConHan.isEmpty()) {
                            showLoading(false);
                            showEmpty(true);
                        } else {
                            loadChiTietThuoc(danhSachMaDonThuocConHan);
                        }
                    }
                }
            );
        }
    }
    
    private void updateTrangThaiDonThuoc(String maDonThuoc, String trangThai) {
        if (maDonThuoc == null || maDonThuoc.isEmpty()) return;
        
        repository.getCollection("DonThuoc").document(maDonThuoc)
            .update("trangThai", trangThai)
            .addOnSuccessListener(aVoid -> android.util.Log.d("DiemDanhFragment", "Updated trangThai to " + trangThai + " for " + maDonThuoc))
            .addOnFailureListener(e -> android.util.Log.e("DiemDanhFragment", "Error updating trangThai", e));
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
        
        android.util.Log.d("DiemDanhFragment", "Phân loại " + tatCaThuoc.size() + " loại thuốc");
        
        for (ChiTietDonThuoc thuoc : tatCaThuoc) {
            boolean coThongTinCaUong = thuoc.isUongSang() || thuoc.isUongTrua() || 
                                       thuoc.isUongChieu() || thuoc.isUongToi();
            
            android.util.Log.d("DiemDanhFragment", "Thuốc " + thuoc.getTenThuoc() + 
                ": Sáng=" + thuoc.isUongSang() + ", Trưa=" + thuoc.isUongTrua() + 
                ", Chiều=" + thuoc.isUongChieu() + ", Tối=" + thuoc.isUongToi());
            
            if (!coThongTinCaUong) {
                // Dữ liệu cũ không có thông tin ca uống - chỉ cho phép ca sáng và chiều
                android.util.Log.d("DiemDanhFragment", "Thuốc " + thuoc.getTenThuoc() + " không có thông tin ca uống - thêm vào sáng và chiều");
                thuocSang.add(thuoc);
                thuocChieu.add(thuoc);
            } else {
                // Dữ liệu mới có thông tin ca uống - phân loại chính xác
                if (thuoc.isUongSang()) {
                    android.util.Log.d("DiemDanhFragment", "Thuốc " + thuoc.getTenThuoc() + " thêm vào ca sáng");
                    thuocSang.add(thuoc);
                }
                if (thuoc.isUongTrua()) {
                    android.util.Log.d("DiemDanhFragment", "Thuốc " + thuoc.getTenThuoc() + " thêm vào ca trưa");
                    thuocTrua.add(thuoc);
                }
                if (thuoc.isUongChieu() || thuoc.isUongToi()) {
                    android.util.Log.d("DiemDanhFragment", "Thuốc " + thuoc.getTenThuoc() + " thêm vào ca chiều");
                    thuocChieu.add(thuoc); // Gộp chiều và tối
                }
            }
        }
        
        android.util.Log.d("DiemDanhFragment", "Kết quả phân loại: Sáng=" + thuocSang.size() + 
            ", Trưa=" + thuocTrua.size() + ", Chiều=" + thuocChieu.size());
        
        // Tạo danh sách các ca uống thuốc - CHỈ hiển thị những ca có thuốc
        List<CaUongThuocAdapter.CaUongThuoc> danhSachCa = new ArrayList<>();
        
        if (!thuocSang.isEmpty()) {
            danhSachCa.add(new CaUongThuocAdapter.CaUongThuoc("Ca Sáng", "🌅", "SANG", thuocSang));
        }
        
        if (!thuocTrua.isEmpty()) {
            danhSachCa.add(new CaUongThuocAdapter.CaUongThuoc("Ca Trưa", "☀️", "TRUA", thuocTrua));
        }
        
        if (!thuocChieu.isEmpty()) {
            danhSachCa.add(new CaUongThuocAdapter.CaUongThuoc("Ca Chiều", "🌤️", "CHIEU", thuocChieu));
        }
        
        android.util.Log.d("DiemDanhFragment", "Hiển thị " + danhSachCa.size() + " ca uống thuốc");
        
        // Cập nhật adapter
        caUongThuocAdapter.setData(danhSachCa);
        
        showLoading(false);
        
        // Chỉ hiển thị empty state khi không có ca nào có thuốc
        showEmpty(danhSachCa.isEmpty());
    }

    private void onXacNhanCa(CaUongThuocAdapter.CaUongThuoc caUong) {
        // Callback khi xác nhận ca thành công
        Toast.makeText(getContext(), 
            "✅ Đã xác nhận uống hết " + caUong.getTenCa().toLowerCase() + 
            " (" + caUong.getDanhSachThuoc().size() + " loại thuốc)", 
            Toast.LENGTH_SHORT).show();
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