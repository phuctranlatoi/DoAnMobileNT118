package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChiTietBenhAnActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView tvMaBenhAn, tvMaBenhNhan, tvTenBenhNhan, tvNgayLap, tvMessage;
    private RecyclerView rvLichSuChanDoan;
    private Button btnBack;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maBenhAn, maTaiKhoan;
    private List<BenhAn> benhAnList;
    private BenhAnAdapter lichSuChanDoanAdapter;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitietbenhan);

        // Initialize Firestore and intent data
        repo = new FirestoreRepository();
        maBenhAn = getIntent().getStringExtra("MA_BENH_AN");
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar); // Updated to match layout if added
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("Chi Tiết Bệnh Án");
        }
        tvMaBenhAn = findViewById(R.id.tvMaBenhAn);
        tvMaBenhNhan = findViewById(R.id.tvMaBenhNhan);
        tvTenBenhNhan = findViewById(R.id.tvTenBenhNhan);
        tvNgayLap = findViewById(R.id.tvNgayLap); // Updated to match layout
        tvMessage = findViewById(R.id.tvMessage);
        rvLichSuChanDoan = findViewById(R.id.rvLichSuChanDoan);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Set up RecyclerView for diagnosis history
        rvLichSuChanDoan.setLayoutManager(new LinearLayoutManager(this));
        benhAnList = new ArrayList<>();
        lichSuChanDoanAdapter = new BenhAnAdapter(benhAnList, benhAn -> {
            // Optional: Handle click on diagnosis history item if needed
        });
        rvLichSuChanDoan.setAdapter(lichSuChanDoanAdapter);

        // Set up button listener
        btnBack.setOnClickListener(v -> finish());

        // Load medical record details
        loadBenhAnDetails();
    }

    private void loadBenhAnDetails() {
        if (maBenhAn == null || maBenhAn.isEmpty() || maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showError("Lỗi: Thông tin không hợp lệ");
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        showError("Không tìm thấy thông tin bệnh nhân");
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    try {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            String maBenhNhan = benhNhan.getMaBenhNhan();
                            tvMaBenhNhan.setText(maBenhNhan);
                            tvTenBenhNhan.setText(benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "N/A");

                            repo.getByField("BenhAn", "maBenhAn", maBenhAn,
                                    querySnapshot1 -> {
                                        if (querySnapshot1.isEmpty()) {
                                            showError("Không tìm thấy bệnh án");
                                            progressBar.setVisibility(View.GONE);
                                            return;
                                        }

                                        try {
                                            BenhAn benhAn = querySnapshot1.getDocuments().get(0).toObject(BenhAn.class);
                                            if (benhAn != null && benhAn.getMaBenhNhan().equals(maBenhNhan)) {
                                                benhAn.setMaBenhAn(querySnapshot1.getDocuments().get(0).getId());
                                                tvMaBenhAn.setText(benhAn.getMaBenhAn() != null ? benhAn.getMaBenhAn() : "N/A");
                                                tvNgayLap.setText(benhAn.getNgayKham() != null
                                                        ? DATE_FORMAT.format(benhAn.getNgayKham().toDate())
                                                        : "N/A");

                                                repo.getByField("BenhAn", "maBenhNhan", maBenhNhan,
                                                        querySnapshot2 -> {
                                                            benhAnList.clear();
                                                            for (var doc : querySnapshot2.getDocuments()) {
                                                                BenhAn historyBenhAn = doc.toObject(BenhAn.class);
                                                                if (historyBenhAn != null) {
                                                                    historyBenhAn.setMaBenhAn(doc.getId());
                                                                    benhAnList.add(historyBenhAn);
                                                                }
                                                            }
                                                            if (benhAnList.isEmpty()) {
                                                                rvLichSuChanDoan.setVisibility(View.GONE);
                                                            } else {
                                                                rvLichSuChanDoan.setVisibility(View.VISIBLE);
                                                                lichSuChanDoanAdapter.notifyDataSetChanged();
                                                            }
                                                            progressBar.setVisibility(View.GONE);
                                                        },
                                                        e -> {
                                                            Log.e("ChiTietBenhAnActivity", "Firestore query error for history: ", e);
                                                            showError("Lỗi tải lịch sử chẩn đoán: " + e.getMessage());
                                                            progressBar.setVisibility(View.GONE);
                                                        });
                                            } else {
                                                showError("Bệnh án không thuộc bệnh nhân này");
                                                progressBar.setVisibility(View.GONE);
                                                finish();
                                            }
                                        } catch (Exception e) {
                                            Log.e("ChiTietBenhAnActivity", "Error parsing BenhAn: ", e);
                                            showError("Lỗi: " + e.getMessage());
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    },
                                    e -> {
                                        Log.e("ChiTietBenhAnActivity", "Firestore query error: ", e);
                                        showError("Lỗi tải bệnh án: " + e.getMessage());
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    } catch (Exception e) {
                        Log.e("ChiTietBenhAnActivity", "Error parsing BenhNhan: ", e);
                        showError("Lỗi: " + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                },
                e -> {
                    Log.e("ChiTietBenhAnActivity", "Firestore query error: ", e);
                    showError("Lỗi tải thông tin: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void showError(String message) {
        if (tvMessage != null) {
            tvMessage.setText(message);
            tvMessage.setVisibility(View.VISIBLE);
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}