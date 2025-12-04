package com.example.doannt118.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.ui.LichLamViecAdapter;

import java.util.ArrayList;
import java.util.List;

public class DanhSachLichLamViecFragment extends Fragment {

    private static final String TAG = "DanhSachLichLamViec";
    private static final String ARG_MA_BAC_SI = "maBacSi";

    private RecyclerView rvLichLamViec;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private LichLamViecAdapter adapter;
    private FirestoreRepository repo;
    private String maBacSi;

    public static DanhSachLichLamViecFragment newInstance(String maBacSi) {
        DanhSachLichLamViecFragment fragment = new DanhSachLichLamViecFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MA_BAC_SI, maBacSi);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maBacSi = getArguments().getString(ARG_MA_BAC_SI);
        }
        repo = new FirestoreRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danh_sach_lich_lam_viec, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        rvLichLamViec = view.findViewById(R.id.rvLichLamViec);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmpty = view.findViewById(R.id.tvEmpty);

        setupRecyclerView();
        loadDanhSachLichLamViec();
    }

    private void setupRecyclerView() {
        adapter = new LichLamViecAdapter(getContext(), new ArrayList<>());
        
        rvLichLamViec.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLichLamViec.setAdapter(adapter);
    }

    public void loadDanhSachLichLamViec() {
        if (maBacSi == null || maBacSi.isEmpty()) {
            Log.e(TAG, "Mã bác sĩ không hợp lệ");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        repo.getByField("LichLamViec", "maBacSi", maBacSi,
            querySnapshot -> {
                List<LichLamViec> danhSach = new ArrayList<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    LichLamViec lichLamViec = doc.toObject(LichLamViec.class);
                    if (lichLamViec != null) {
                        danhSach.add(lichLamViec);
                    }
                }

                // Sắp xếp theo ngày giờ
                danhSach.sort((l1, l2) -> {
                    if (l1.getNgayLamViec() == null) return 1;
                    if (l2.getNgayLamViec() == null) return -1;
                    int dateCompare = l1.getNgayLamViec().compareTo(l2.getNgayLamViec());
                    if (dateCompare != 0) return dateCompare;
                    if (l1.getCaLamViec() == null) return 1;
                    if (l2.getCaLamViec() == null) return -1;
                    return l1.getCaLamViec().compareTo(l2.getCaLamViec());
                });

                progressBar.setVisibility(View.GONE);
                
                if (danhSach.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                }
                
                adapter.updateData(danhSach);
                Log.d(TAG, "Loaded " + danhSach.size() + " lịch làm việc");
            },
            e -> {
                progressBar.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
                Log.e(TAG, "Lỗi tải danh sách", e);
            });
    }

    public interface OnFragmentInteractionListener {
        void onEditLichLamViec(LichLamViec lichLamViec);
        void onDeleteLichLamViec(LichLamViec lichLamViec);
    }
}
