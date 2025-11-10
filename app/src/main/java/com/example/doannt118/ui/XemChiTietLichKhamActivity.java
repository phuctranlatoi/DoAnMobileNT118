package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class XemChiTietLichKhamActivity extends AppCompatActivity {
    private static final String TAG = "XemChiTietLichKhamActivity";
    private FirestoreRepository repository;
    private RecyclerView recyclerView;
    private TextView tvThongBao;
    private Button btnQuayLai;
    private LichKhamAdapter adapter; // Bạn cần tạo LichKhamAdapter
    private List<LichKham> lichKhamList;
    private HashMap<String, String> benhNhanMap;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_chi_tiet_lich_kham);

        repository = new FirestoreRepository();
        recyclerView = findViewById(R.id.recyclerViewLichKham);
        tvThongBao = findViewById(R.id.tvThongBao);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        lichKhamList = new ArrayList<>();
        benhNhanMap = new HashMap<>();

        // Khởi tạo adapter (bạn cần tạo LichKhamAdapter tương tự LichLamViecAdapter)
        adapter = new LichKhamAdapter(this, lichKhamList, benhNhanMap);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String maLichLamViec = getIntent().getStringExtra("maLichLamViec");
        if (maLichLamViec == null) {
            tvThongBao.setText("Lỗi: Không có mã lịch làm việc!");
            Log.e(TAG, "maLichLamViec is null");
            return;
        }

        loadBenhNhanInfo();
        loadLichKham(maLichLamViec);

        btnQuayLai.setOnClickListener(v -> finish());
    }

    private void loadBenhNhanInfo() {
        repository.getAll("BenhNhan",
                querySnapshot -> {
                    benhNhanMap.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String maBenhNhan = document.getString("maBenhNhan");
                        String hoTen = document.getString("hoTen");
                        if (maBenhNhan != null && hoTen != null) {
                            benhNhanMap.put(maBenhNhan, hoTen);
                        }
                    }
                    adapter.updateBenhNhanInfo(benhNhanMap); // Gọi phương thức update trong adapter
                    Log.d(TAG, "Loaded " + benhNhanMap.size() + " benhNhan");
                },
                e -> {
                    Log.e(TAG, "Error loading BenhNhan: " + e.getMessage());
                    Toast.makeText(this, "Lỗi tải thông tin bệnh nhân: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void loadLichKham(String maLichLamViec) {
        tvThongBao.setText("Đang tải lịch khám...");
        repository.getByField("LichKham", "maLichLamViec", maLichLamViec,
                querySnapshot -> {
                    lichKhamList.clear();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        LichKham lichKham = document.toObject(LichKham.class);
                        if (lichKham != null) {
                            lichKhamList.add(lichKham);
                        }
                    }
                    adapter.updateData(lichKhamList); // Gọi phương thức update trong adapter
                    tvThongBao.setText(lichKhamList.isEmpty() ? "Không có lịch khám cho lịch làm việc này!" : "");
                    Log.d(TAG, "Loaded " + lichKhamList.size() + " lichKham for maLichLamViec: " + maLichLamViec);
                },
                e -> {
                    Log.e(TAG, "Error loading lichKham: " + e.getMessage());
                    tvThongBao.setText("Lỗi tải lịch khám: " + e.getMessage());
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}