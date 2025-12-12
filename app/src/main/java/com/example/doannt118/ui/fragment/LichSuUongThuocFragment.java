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
        
        // Load dữ liệu xác nhận uống thuốc thực tế
        repository.getByField("XacNhanUongThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList = new ArrayList<>();
                int daUong = 0;
                int boQua = 0;
                
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.XacNhanUongThuoc xacNhan = doc.toObject(com.example.doannt118.model.XacNhanUongThuoc.class);
                    if (xacNhan != null) {
                        xacNhanList.add(xacNhan);
                        if (xacNhan.isDaUong()) {
                            daUong++;
                        } else {
                            boQua++;
                        }
                    }
                }
                
                // Chuyển đổi dữ liệu XacNhanUongThuoc thành LichUongThuoc để hiển thị
                convertToLichSuDisplay(xacNhanList, daUong, boQua);
            },
            e -> {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }
    
    private void convertToLichSuDisplay(List<com.example.doannt118.model.XacNhanUongThuoc> xacNhanList, int daUong, int boQua) {
        List<LichUongThuoc> displayList = new ArrayList<>();
        final int[] loadedCount = {0};
        
        if (xacNhanList.isEmpty()) {
            adapter.setData(displayList);
            updateThongKe(daUong, boQua, 0);
            showLoading(false);
            showEmpty(true);
            return;
        }
        
        // Tạo LichUongThuoc từ dữ liệu XacNhanUongThuoc và load thông tin thuốc
        for (com.example.doannt118.model.XacNhanUongThuoc xacNhan : xacNhanList) {
            LichUongThuoc lich = new LichUongThuoc();
            lich.setMaLichUong(xacNhan.getMaXacNhan());
            lich.setMaBenhNhan(xacNhan.getMaBenhNhan());
            lich.setMaDonThuoc(xacNhan.getMaChiTietDonThuoc()); // Tạm thời lưu maChiTietDonThuoc vào maDonThuoc
            
            if (xacNhan.getThoiGianXacNhan() != null) {
                lich.setNgayUong(xacNhan.getThoiGianXacNhan().toDate());
                lich.setThoiGianXacNhan(xacNhan.getThoiGianXacNhan());
            }
            
            lich.setTrangThai(xacNhan.isDaUong() ? "DA_UONG" : "BO_QUA");
            
            // Xác định ca uống từ ghiChu (ví dụ: "Điểm danh ca sáng")
            String ghiChu = xacNhan.getGhiChu();
            if (ghiChu != null) {
                if (ghiChu.toLowerCase().contains("sáng")) {
                    lich.setCaUong("SANG");
                } else if (ghiChu.toLowerCase().contains("trưa")) {
                    lich.setCaUong("TRUA");
                } else if (ghiChu.toLowerCase().contains("chiều")) {
                    lich.setCaUong("CHIEU");
                } else if (ghiChu.toLowerCase().contains("tối")) {
                    lich.setCaUong("TOI");
                } else {
                    lich.setCaUong("KHAC");
                }
            }
            
            displayList.add(lich);
            
            // Load thông tin thuốc từ ChiTietDonThuoc
            loadThuocInfoForLich(lich, xacNhan.getMaChiTietDonThuoc(), () -> {
                loadedCount[0]++;
                if (loadedCount[0] == xacNhanList.size()) {
                    // Sắp xếp theo ngày giảm dần
                    displayList.sort((a, b) -> {
                        if (a.getNgayUong() != null && b.getNgayUong() != null) {
                            return b.getNgayUong().compareTo(a.getNgayUong());
                        }
                        return 0;
                    });
                    
                    adapter.setData(displayList);
                    updateThongKe(daUong, boQua, xacNhanList.size());
                    showLoading(false);
                    showEmpty(displayList.isEmpty());
                }
            });
        }
    }
    
    private void loadThuocInfoForLich(LichUongThuoc lich, String maChiTietDonThuoc, Runnable onComplete) {
        repository.getByField("ChiTietDonThuoc", "maChiTiet", maChiTietDonThuoc,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    com.example.doannt118.model.ChiTietDonThuoc chiTiet = querySnapshot.getDocuments().get(0).toObject(com.example.doannt118.model.ChiTietDonThuoc.class);
                    if (chiTiet != null) {
                        // Lưu tên thuốc vào maDonThuoc field (tạm thời để hiển thị)
                        lich.setMaDonThuoc(chiTiet.getTenThuoc() + " - " + chiTiet.getSoLuong() + " viên");
                    }
                }
                onComplete.run();
            },
            e -> {
                android.util.Log.e("LichSuFragment", "Error loading thuoc info", e);
                onComplete.run();
            }
        );
    }

    private void updateThongKe(int daUong, int boQua, int total) {
        if (total > 0) {
            int tiLe = (int) ((daUong * 100.0) / total);
            
            // Hiệu ứng cập nhật số liệu
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, tiLe);
            animator.setDuration(1000);
            animator.addUpdateListener(animation -> {
                int animatedValue = (int) animation.getAnimatedValue();
                tvTiLeTuanThu.setText(animatedValue + "%");
            });
            animator.start();
            
            tvDaUong.setText("✅ Đã uống: " + daUong + "/" + total + " lần");
            tvBoQua.setText("⏭️ Bỏ qua: " + boQua + " lần");
            
            // Thay đổi màu sắc dựa trên tỷ lệ tuân thủ
            if (tiLe >= 80) {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#27AE60"));
            } else if (tiLe >= 60) {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#F39C12"));
            } else {
                tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#E74C3C"));
            }
        } else {
            tvTiLeTuanThu.setText("0%");
            tvTiLeTuanThu.setTextColor(android.graphics.Color.parseColor("#95A5A6"));
            tvDaUong.setText("✅ Đã uống: 0/0 lần");
            tvBoQua.setText("⏭️ Bỏ qua: 0 lần");
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