package com.example.doannt118.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.LichUongThuoc;
import com.example.doannt118.model.XacNhanUongThuoc;
import com.example.doannt118.model.ChiTietDonThuoc;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.ui.LichSuUongThuocAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class LichSuUongThuocFragment extends Fragment {
    private TextView tvTiLeTuanThu, tvDaUong, tvBoQua, tvEmpty;
    private RecyclerView rvLichSu;
    private ProgressBar progressBar;
    
    private LichSuUongThuocAdapter adapter;
    private FirestoreRepository repository;
    private String maBenhNhan;

    public static LichSuUongThuocFragment newInstance(String maBenhNhan) {
        LichSuUongThuocFragment fragment = new LichSuUongThuocFragment();
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
        return inflater.inflate(R.layout.fragment_lich_su_uong_thuoc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        setupRecyclerView();
        
        if (maBenhNhan != null && !maBenhNhan.isEmpty()) {
            loadLichSuUongThuoc();
        } else {
            Toast.makeText(getContext(), "Không tìm thấy thông tin bệnh nhân!", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews(View view) {
        tvTiLeTuanThu = view.findViewById(R.id.tvTiLeTuanThu);
        tvDaUong = view.findViewById(R.id.tvDaUong);
        tvBoQua = view.findViewById(R.id.tvBoQua);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rvLichSu = view.findViewById(R.id.rvLichSu);
        progressBar = view.findViewById(R.id.progressBar);
        
        repository = new FirestoreRepository();
    }

    private void setupRecyclerView() {
        adapter = new LichSuUongThuocAdapter(getContext(), () -> {
            // Reload data khi có thay đổi
            if (maBenhNhan != null) {
                loadLichSuUongThuoc();
            }
        });
        rvLichSu.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLichSu.setAdapter(adapter);
    }

    private void loadLichSuUongThuoc() {
        showLoading(true);
        
        // Bước 1: Load tất cả đơn thuốc của bệnh nhân để tính tổng số ca cần uống
        repository.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            donThuocSnapshot -> {
                // Bước 2: Load chi tiết đơn thuốc để tính số ca/ngày
                loadChiTietDonThuocForStats(donThuocSnapshot.getDocuments());
            },
            e -> {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private void loadChiTietDonThuocForStats(List<DocumentSnapshot> donThuocDocs) {
        List<String> danhSachMaDonThuoc = new ArrayList<>();
        
        // Lọc các đơn thuốc đang active
        for (DocumentSnapshot doc : donThuocDocs) {
            com.example.doannt118.model.DonThuoc donThuoc = doc.toObject(com.example.doannt118.model.DonThuoc.class);
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
            updateThongKeTheoCa(0, 0, 0, 0.0, new ArrayList<>());
            return;
        }
        
        // Load chi tiết đơn thuốc
        List<ChiTietDonThuoc> tatCaChiTiet = new ArrayList<>();
        final int[] loadedCount = {0};
        
        for (String maDonThuoc : danhSachMaDonThuoc) {
            repository.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
                chiTietSnapshot -> {
                    for (DocumentSnapshot doc : chiTietSnapshot.getDocuments()) {
                        ChiTietDonThuoc chiTiet = doc.toObject(ChiTietDonThuoc.class);
                        if (chiTiet != null) {
                            tatCaChiTiet.add(chiTiet);
                        }
                    }
                    
                    loadedCount[0]++;
                    if (loadedCount[0] == danhSachMaDonThuoc.size()) {
                        // Đã load xong tất cả chi tiết, bây giờ load xác nhận uống thuốc
                        loadXacNhanUongThuocForStats(tatCaChiTiet);
                    }
                },
                e -> {
                    loadedCount[0]++;
                    if (loadedCount[0] == danhSachMaDonThuoc.size()) {
                        loadXacNhanUongThuocForStats(tatCaChiTiet);
                    }
                }
            );
        }
    }
    
    private void loadXacNhanUongThuocForStats(List<ChiTietDonThuoc> tatCaChiTiet) {
        // Load dữ liệu xác nhận uống thuốc thực tế
        repository.getByField("XacNhanUongThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList = new ArrayList<>();
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.XacNhanUongThuoc xacNhan = doc.toObject(com.example.doannt118.model.XacNhanUongThuoc.class);
                    if (xacNhan != null) {
                        xacNhanList.add(xacNhan);
                    }
                }
                
                // Tính toán thống kê theo ca mới
                tinhToanThongKeTheoCa(tatCaChiTiet, xacNhanList);
            },
            e -> {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private void tinhToanThongKeTheoCa(List<ChiTietDonThuoc> tatCaChiTiet, List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList) {
        // Xác định các ca cần uống mỗi ngày
        java.util.Set<String> cacCaCanUong = new java.util.HashSet<>();
        
        for (ChiTietDonThuoc chiTiet : tatCaChiTiet) {
            boolean coThongTinCaUong = chiTiet.isUongSang() || chiTiet.isUongTrua() || 
                                       chiTiet.isUongChieu() || chiTiet.isUongToi();
            
            if (!coThongTinCaUong) {
                // Dữ liệu cũ - mặc định có ca sáng và chiều
                cacCaCanUong.add("SANG");
                cacCaCanUong.add("CHIEU");
            } else {
                // Thêm các ca được chỉ định
                if (chiTiet.isUongSang()) cacCaCanUong.add("SANG");
                if (chiTiet.isUongTrua()) cacCaCanUong.add("TRUA");
                if (chiTiet.isUongChieu()) cacCaCanUong.add("CHIEU");
                if (chiTiet.isUongToi()) cacCaCanUong.add("TOI");
            }
        }
        
        // Tính tỷ lệ tuân thủ theo ngày mới
        TinhToanTuanThuTheoNgay tinhToan = new TinhToanTuanThuTheoNgay(cacCaCanUong, xacNhanList);
        TinhToanTuanThuTheoNgay.KetQuaTinhToan ketQua = tinhToan.tinhToan();
        
        // Chuyển đổi dữ liệu để hiển thị
        convertToLichSuDisplay(xacNhanList, ketQua.soCaDaUong, ketQua.soCaBoQua, ketQua.tongSoCaCanUong, ketQua.tiLeTuanThu);
    }
    
    // Class helper để tính toán tuân thủ theo ngày
    private static class TinhToanTuanThuTheoNgay {
        private java.util.Set<String> cacCaCanUong;
        private List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList;
        
        public TinhToanTuanThuTheoNgay(java.util.Set<String> cacCaCanUong, List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList) {
            this.cacCaCanUong = cacCaCanUong;
            this.xacNhanList = xacNhanList;
        }
        
        public KetQuaTinhToan tinhToan() {
            // Tính tỷ lệ tuân thủ theo thời gian thực
            return tinhTiLeTuanThuTheoThoiGianThuc();
        }
        
        private KetQuaTinhToan tinhTiLeTuanThuTheoThoiGianThuc() {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            java.util.Date ngayHienTai = new java.util.Date();
            String ngayHienTaiStr = dateFormat.format(ngayHienTai);
            
            // Nhóm xác nhận theo ngày
            java.util.Map<String, java.util.List<com.example.doannt118.model.XacNhanUongThuoc>> xacNhanTheoNgay = new java.util.HashMap<>();
            
            for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanList) {
                if (xacNhan.getMaXacNhan() != null && xacNhan.getMaXacNhan().startsWith("CA_") && 
                    xacNhan.getThoiGianXacNhan() != null) {
                    
                    String ngay = dateFormat.format(xacNhan.getThoiGianXacNhan().toDate());
                    
                    if (!xacNhanTheoNgay.containsKey(ngay)) {
                        xacNhanTheoNgay.put(ngay, new java.util.ArrayList<>());
                    }
                    xacNhanTheoNgay.get(ngay).add(xacNhan);
                }
            }
            
            double tongTiLe = 0;
            int soNgayTinhToan = 0;
            int tongSoCaDaUong = 0;
            int tongSoCaBoQua = 0;
            
            // Tính tỷ lệ cho từng ngày
            for (java.util.Map.Entry<String, java.util.List<com.example.doannt118.model.XacNhanUongThuoc>> entry : xacNhanTheoNgay.entrySet()) {
                String ngay = entry.getKey();
                java.util.List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanTrongNgay = entry.getValue();
                
                double tiLeTrongNgay;
                if (ngay.equals(ngayHienTaiStr)) {
                    // Ngày hôm nay - tính theo thời gian thực
                    tiLeTrongNgay = tinhTiLeTrongNgayHienTai(xacNhanTrongNgay);
                } else {
                    // Ngày khác - tính bình thường
                    tiLeTrongNgay = tinhTiLeTrongNgayDaQua(xacNhanTrongNgay);
                }
                
                tongTiLe += tiLeTrongNgay;
                soNgayTinhToan++;
                
                // Đếm số ca đã uống
                for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanTrongNgay) {
                    if (xacNhan.isDaUong()) {
                        tongSoCaDaUong++;
                    }
                }
                
                android.util.Log.d("TuanThuTheoNgay", 
                    "Ngày " + ngay + ": " + String.format("%.1f", tiLeTrongNgay) + "%");
            }
            
            // Nếu không có dữ liệu gì, tính cho ngày hôm nay
            if (soNgayTinhToan == 0) {
                double tiLeHomNay = tinhTiLeTrongNgayHienTai(new java.util.ArrayList<>());
                tongTiLe = tiLeHomNay;
                soNgayTinhToan = 1;
            }
            
            double tiLeTuanThuTrungBinh = soNgayTinhToan > 0 ? tongTiLe / soNgayTinhToan : 0;
            
            return new KetQuaTinhToan(tongSoCaDaUong, tongSoCaBoQua, tongSoCaDaUong + tongSoCaBoQua, tiLeTuanThuTrungBinh);
        }
        
        private double tinhTiLeTrongNgayHienTai(java.util.List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanTrongNgay) {
            // Tạo set các ca đã được xác nhận uống
            java.util.Set<String> cacCaDaUong = new java.util.HashSet<>();
            for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanTrongNgay) {
                if (xacNhan.isDaUong()) {
                    String maXacNhan = xacNhan.getMaXacNhan();
                    if (maXacNhan.contains("_SANG_")) cacCaDaUong.add("SANG");
                    else if (maXacNhan.contains("_TRUA_")) cacCaDaUong.add("TRUA");
                    else if (maXacNhan.contains("_CHIEU_")) cacCaDaUong.add("CHIEU");
                    else if (maXacNhan.contains("_TOI_")) cacCaDaUong.add("TOI");
                }
            }
            
            // Đếm số ca đã qua khung giờ
            int soCaDaQua = 0;
            java.util.Date ngayHienTai = new java.util.Date();
            
            for (String ca : cacCaCanUong) {
                if (com.example.doannt118.utils.CaUongThuocManager.isCaDaQua(ca, ngayHienTai)) {
                    soCaDaQua++;
                }
            }
            
            // Nếu chưa có ca nào qua thì ít nhất tính 1 ca (ca hiện tại)
            if (soCaDaQua == 0) {
                soCaDaQua = 1;
            }
            
            // Tỷ lệ = số ca đã uống / số ca đã qua khung giờ
            double tiLe = (double) cacCaDaUong.size() / soCaDaQua * 100;
            
            android.util.Log.d("TiLeThoiGianThuc", 
                "Ca đã uống: " + cacCaDaUong.size() + 
                ", Ca đã qua: " + soCaDaQua + 
                ", Tỷ lệ: " + String.format("%.1f", tiLe) + "%");
            
            return tiLe;
        }
        
        private double tinhTiLeTrongNgayDaQua(java.util.List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanTrongNgay) {
            // Ngày đã qua - tính bình thường
            int soCaDaUong = 0;
            for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanTrongNgay) {
                if (xacNhan.isDaUong()) {
                    soCaDaUong++;
                }
            }
            
            return (double) soCaDaUong / cacCaCanUong.size() * 100;
        }
        
        private int tinhSoCaBoQuaTrongNgay(String ngayStr, java.util.List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanTrongNgay) {
            try {
                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                java.util.Date ngay = dateFormat.parse(ngayStr);
                
                // Tạo set các ca đã được xác nhận (dù uống hay không)
                java.util.Set<String> cacCaDaXacNhan = new java.util.HashSet<>();
                for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanTrongNgay) {
                    String maXacNhan = xacNhan.getMaXacNhan();
                    if (maXacNhan.contains("_SANG_")) cacCaDaXacNhan.add("SANG");
                    else if (maXacNhan.contains("_TRUA_")) cacCaDaXacNhan.add("TRUA");
                    else if (maXacNhan.contains("_CHIEU_")) cacCaDaXacNhan.add("CHIEU");
                    else if (maXacNhan.contains("_TOI_")) cacCaDaXacNhan.add("TOI");
                }
                
                // Đếm số ca bỏ qua (ca cần uống nhưng chưa xác nhận và đã qua khung giờ)
                int soCaBoQua = 0;
                for (String ca : cacCaCanUong) {
                    if (!cacCaDaXacNhan.contains(ca) && com.example.doannt118.utils.CaUongThuocManager.isCaDaQua(ca, ngay)) {
                        soCaBoQua++;
                    }
                }
                
                return soCaBoQua;
            } catch (Exception e) {
                return 0;
            }
        }
        
        public static class KetQuaTinhToan {
            public int soCaDaUong;
            public int soCaBoQua;
            public int tongSoCaCanUong;
            public double tiLeTuanThu;
            
            public KetQuaTinhToan(int soCaDaUong, int soCaBoQua, int tongSoCaCanUong, double tiLeTuanThu) {
                this.soCaDaUong = soCaDaUong;
                this.soCaBoQua = soCaBoQua;
                this.tongSoCaCanUong = tongSoCaCanUong;
                this.tiLeTuanThu = tiLeTuanThu;
            }
        }
    }
    
    private int tinhSoNgayCanUongThuoc(List<ChiTietDonThuoc> tatCaChiTiet, List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList) {
        // Tìm ngày đầu tiên và cuối cùng có xác nhận uống thuốc
        java.util.Date ngayDauTien = null;
        java.util.Date ngayCuoiCung = null;
        
        for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanList) {
            if (xacNhan.getThoiGianXacNhan() != null && xacNhan.getMaXacNhan() != null && xacNhan.getMaXacNhan().startsWith("CA_")) {
                java.util.Date ngayXacNhan = xacNhan.getThoiGianXacNhan().toDate();
                
                if (ngayDauTien == null || ngayXacNhan.before(ngayDauTien)) {
                    ngayDauTien = ngayXacNhan;
                }
                
                if (ngayCuoiCung == null || ngayXacNhan.after(ngayCuoiCung)) {
                    ngayCuoiCung = ngayXacNhan;
                }
            }
        }
        
        // Nếu không có dữ liệu, mặc định 7 ngày
        if (ngayDauTien == null || ngayCuoiCung == null) {
            return 7;
        }
        
        // Tính số ngày giữa ngày đầu và cuối + thêm ngày hiện tại
        long diffInMillies = Math.abs(ngayCuoiCung.getTime() - ngayDauTien.getTime());
        int soNgayThucTe = (int) (diffInMillies / (24 * 60 * 60 * 1000)) + 1;
        
        // Đảm bảo ít nhất 1 ngày, tối đa 30 ngày
        return Math.max(1, Math.min(soNgayThucTe, 30));
    }
    
    private void convertToLichSuDisplay(List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList, int daUong, int boQua, int tongSoCa, double tiLeTuanThu) {
        List<LichUongThuoc> displayList = new ArrayList<>();
        final int[] loadedCount = {0};
        
        if (xacNhanList.isEmpty()) {
            adapter.setData(displayList);
            updateThongKeTheoCa(daUong, boQua, tongSoCa, 0.0, displayList);
            showLoading(false);
            showEmpty(true);
            return;
        }
        
        // Chỉ hiển thị những xác nhận theo ca (có key bắt đầu bằng "CA_")
        List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanTheoCa = new ArrayList<>();
        for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanList) {
            if (xacNhan.getMaXacNhan() != null && xacNhan.getMaXacNhan().startsWith("CA_")) {
                xacNhanTheoCa.add(xacNhan);
            }
        }
        
        if (xacNhanTheoCa.isEmpty()) {
            adapter.setData(displayList);
            updateThongKeTheoCa(daUong, boQua, tongSoCa, tiLeTuanThu, displayList);
            showLoading(false);
            showEmpty(true);
            return;
        }
        
        // Tạo LichUongThuoc từ dữ liệu XacNhanUongThuoc theo ca
        for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanTheoCa) {
            LichUongThuoc lich = new LichUongThuoc();
            lich.setMaLichUong(xacNhan.getMaXacNhan());
            lich.setMaBenhNhan(xacNhan.getMaBenhNhan());
            
            if (xacNhan.getThoiGianXacNhan() != null) {
                lich.setNgayUong(xacNhan.getThoiGianXacNhan().toDate());
                lich.setThoiGianXacNhan(xacNhan.getThoiGianXacNhan());
            }
            
            lich.setTrangThai(xacNhan.isDaUong() ? "DA_UONG" : "BO_QUA");
            
            // Xác định ca uống từ key (không hiển thị khung giờ)
            String maXacNhan = xacNhan.getMaXacNhan();
            if (maXacNhan.contains("_SANG_")) {
                lich.setCaUong("SANG");
                lich.setMaDonThuoc(com.example.doannt118.utils.CaUongThuocManager.getTenCa("SANG"));
            } else if (maXacNhan.contains("_TRUA_")) {
                lich.setCaUong("TRUA");
                lich.setMaDonThuoc(com.example.doannt118.utils.CaUongThuocManager.getTenCa("TRUA"));
            } else if (maXacNhan.contains("_CHIEU_")) {
                lich.setCaUong("CHIEU");
                lich.setMaDonThuoc(com.example.doannt118.utils.CaUongThuocManager.getTenCa("CHIEU"));
            } else if (maXacNhan.contains("_TOI_")) {
                lich.setCaUong("TOI");
                lich.setMaDonThuoc(com.example.doannt118.utils.CaUongThuocManager.getTenCa("TOI"));
            } else {
                lich.setCaUong("KHAC");
                lich.setMaDonThuoc("Ca không xác định");
            }
            
            // Thêm thông tin từ ghi chú
            if (xacNhan.getGhiChu() != null) {
                lich.setMaDonThuoc(lich.getMaDonThuoc() + " - " + xacNhan.getGhiChu());
            }
            
            displayList.add(lich);
        }
        
        // Sắp xếp theo ngày giảm dần
        displayList.sort((a, b) -> {
            if (a.getNgayUong() != null && b.getNgayUong() != null) {
                return b.getNgayUong().compareTo(a.getNgayUong());
            }
            return 0;
        });
        
        adapter.setData(displayList);
        updateThongKeTheoCa(daUong, boQua, tongSoCa, tiLeTuanThu, displayList);
        showLoading(false);
        showEmpty(displayList.isEmpty());
    }
    


    private void updateThongKeTheoCa(int daUong, int boQua, int tongSoCa, double tiLeTuanThu, List<LichUongThuoc> displayList) {
        if (tongSoCa > 0) {
            // Sử dụng tỷ lệ tuân thủ đã tính theo ngày
            int tiLe = (int) Math.round(tiLeTuanThu);
            
            // Hiệu ứng cập nhật số liệu
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, tiLe);
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                tvTiLeTuanThu.setText(animatedValue + "%");
            });
            animator.start();
            
            // Hiển thị thông tin chi tiết
            tvDaUong.setText("✅ Đã uống: " + daUong + " ca");
            tvBoQua.setText("⏭️ Bỏ qua: " + boQua + " ca");
            
            // Thay đổi màu sắc dựa trên tỷ lệ tuân thủ
            if (tiLe >= 80) {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#27AE60"));
            } else if (tiLe >= 60) {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#F39C12"));
            } else {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#E74C3C"));
            }
            
            // Log để debug
            android.util.Log.d("ThongKeTuanThu", 
                "Tỷ lệ tuân thủ trung bình: " + String.format("%.1f", tiLeTuanThu) + "%" +
                ", Đã uống: " + daUong + " ca, Bỏ qua: " + boQua + " ca");
            
        } else {
            tvTiLeTuanThu.setText("0%");
            tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#95A5A6"));
            tvDaUong.setText("✅ Đã uống: 0 ca");
            tvBoQua.setText("⏭️ Bỏ qua: 0 ca");
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showEmpty(boolean show) {
        if (tvEmpty != null && rvLichSu != null) {
            tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
            rvLichSu.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data khi quay lại fragment với hiệu ứng
        if (maBenhNhan != null) {
            // Thêm delay nhỏ để tạo hiệu ứng mượt mà
            new android.os.Handler().postDelayed(() -> {
                loadLichSuUongThuoc();
            }, 300);
        }
    }
}