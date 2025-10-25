package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChiTietBenhAnActivity extends AppCompatActivity {

    private TextView tvMaBenhNhan, tvTenBenhNhan, tvNgayLap, tvMessage;
    private RecyclerView rvLichSuChanDoan;
    private Button btnBack;
    private FirestoreRepository repo;
    private String maBenhAn, maTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chitietbenhan); // Correct layout name

        repo = new FirestoreRepository();
        maBenhAn = getIntent().getStringExtra("MA_BENH_AN");
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // Ánh xạ views
        tvMaBenhNhan = findViewById(R.id.tvMaBenhNhan);
        tvTenBenhNhan = findViewById(R.id.tvTenBenhNhan);
        tvNgayLap = findViewById(R.id.tvNgayLap);
        rvLichSuChanDoan = findViewById(R.id.rvLichSuChanDoan);
        tvMessage = findViewById(R.id.tvMessage);
        btnBack = findViewById(R.id.btnBack);

        rvLichSuChanDoan.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        loadChiTietBenhAn();
    }

    private void loadChiTietBenhAn() {
        if (maBenhAn == null || maBenhAn.isEmpty()) {
            Log.e("ChiTietBenhAnActivity", "Mã bệnh án không hợp lệ");
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Mã bệnh án không hợp lệ!");
            return;
        }

        // Lấy thông tin bệnh án
        repo.getByField("BenhAn", "maBenhAn", maBenhAn,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("ChiTietBenhAnActivity", "Không tìm thấy bệnh án: " + maBenhAn);
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Không tìm thấy bệnh án!");
                        return;
                    }

                    try {
                        BenhAn benhAn = querySnapshot.getDocuments().get(0).toObject(BenhAn.class);
                        if (benhAn == null) {
                            tvMessage.setVisibility(View.VISIBLE);
                            tvMessage.setText("Không tìm thấy bệnh án!");
                            return;
                        }

                        // Hiển thị thông tin bệnh án
                        tvMaBenhNhan.setText(benhAn.getMaBenhNhan());
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        tvNgayLap.setText(benhAn.getNgayKham() != null ? dateFormat.format(benhAn.getNgayKham().toDate()) : "N/A");

                        // Lấy thông tin bệnh nhân
                        repo.getByField("BenhNhan", "maBenhNhan", benhAn.getMaBenhNhan(),
                                querySnapshotBenhNhan -> {
                                    if (querySnapshotBenhNhan.isEmpty()) {
                                        Log.e("ChiTietBenhAnActivity", "Không tìm thấy bệnh nhân: " + benhAn.getMaBenhNhan());
                                        tvTenBenhNhan.setText("Không tìm thấy tên");
                                        return;
                                    }

                                    try {
                                        BenhNhan benhNhan = querySnapshotBenhNhan.getDocuments().get(0).toObject(BenhNhan.class);
                                        tvTenBenhNhan.setText(benhNhan != null && benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "Không tìm thấy tên");

                                        // Lấy tất cả bệnh án của bệnh nhân
                                        repo.getByField("BenhAn", "maBenhNhan", benhAn.getMaBenhNhan(),
                                                querySnapshotBenhAnList -> {
                                                    List<BenhAn> benhAnList = new ArrayList<>();
                                                    for (var doc : querySnapshotBenhAnList.getDocuments()) {
                                                        BenhAn ba = doc.toObject(BenhAn.class);
                                                        if (ba != null) {
                                                            benhAnList.add(ba);
                                                        }
                                                    }

                                                    // Sắp xếp bệnh án theo số thứ tự trong maBenhAn (BA001 -> 001)
                                                    benhAnList.sort((a, b) -> {
                                                        try {
                                                            int seqA = Integer.parseInt(a.getMaBenhAn().replaceAll("[^0-9]", ""));
                                                            int seqB = Integer.parseInt(b.getMaBenhAn().replaceAll("[^0-9]", ""));
                                                            return Integer.compare(seqA, seqB);
                                                        } catch (NumberFormatException e) {
                                                            return 0;
                                                        }
                                                    });

                                                    // Thu thập lịch sử chẩn đoán
                                                    List<DiagnosisEntryAdapter.DiagnosisEntry> diagnosisList = new ArrayList<>();
                                                    for (BenhAn ba : benhAnList) {
                                                        if (ba.getChanDoan() != null && !ba.getChanDoan().trim().isEmpty()) {
                                                            diagnosisList.addAll(parseDiagnosisHistory(ba.getChanDoan(), ba.getNgayKham()));
                                                        }
                                                    }

                                                    if (diagnosisList.isEmpty()) {
                                                        tvMessage.setVisibility(View.VISIBLE);
                                                        tvMessage.setText("Không có lịch sử chẩn đoán!");
                                                    } else {
                                                        tvMessage.setVisibility(View.GONE);
                                                        rvLichSuChanDoan.setAdapter(new DiagnosisEntryAdapter(diagnosisList));
                                                    }
                                                },
                                                e -> {
                                                    Log.e("ChiTietBenhAnActivity", "Lỗi tải danh sách bệnh án: ", e);
                                                    tvMessage.setVisibility(View.VISIBLE);
                                                    tvMessage.setText("Lỗi tải danh sách bệnh án: " + e.getMessage());
                                                });
                                    } catch (Exception e) {
                                        Log.e("ChiTietBenhAnActivity", "Lỗi tải thông tin bệnh nhân: ", e);
                                        tvTenBenhNhan.setText("Không tìm thấy tên");
                                        tvMessage.setVisibility(View.VISIBLE);
                                        tvMessage.setText("Lỗi: " + e.getMessage());
                                    }
                                },
                                e -> {
                                    Log.e("ChiTietBenhAnActivity", "Lỗi tải thông tin bệnh nhân: ", e);
                                    tvTenBenhNhan.setText("Không tìm thấy tên");
                                    tvMessage.setVisibility(View.VISIBLE);
                                    tvMessage.setText("Lỗi tải thông tin bệnh nhân: " + e.getMessage());
                                });
                    } catch (Exception e) {
                        Log.e("ChiTietBenhAnActivity", "Lỗi tải bệnh án: ", e);
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Lỗi: " + e.getMessage());
                    }
                },
                e -> {
                    Log.e("ChiTietBenhAnActivity", "Lỗi tải bệnh án: ", e);
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Lỗi tải bệnh án: " + e.getMessage());
                });
    }

    private List<DiagnosisEntryAdapter.DiagnosisEntry> parseDiagnosisHistory(String chanDoan, Timestamp ngayKham) {
        List<DiagnosisEntryAdapter.DiagnosisEntry> diagnosisList = new ArrayList<>();
        if (chanDoan == null || chanDoan.trim().isEmpty()) {
            return diagnosisList;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = ngayKham != null ? dateFormat.format(ngayKham.toDate()) : dateFormat.format(new Date());
        String[] lines = chanDoan.split("\n");
        Pattern pattern = Pattern.compile("^(\\d{2}/\\d{2}/\\d{4}):\\s*(.+)$");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.matches()) {
                String date = matcher.group(1);
                String diagnosis = matcher.group(2);
                diagnosisList.add(new DiagnosisEntryAdapter.DiagnosisEntry(date, diagnosis));
            } else {
                diagnosisList.add(new DiagnosisEntryAdapter.DiagnosisEntry(dateStr, line.trim()));
            }
        }
        return diagnosisList;
    }
}