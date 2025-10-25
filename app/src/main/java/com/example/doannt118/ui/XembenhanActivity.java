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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class XembenhanActivity extends AppCompatActivity {

    private RecyclerView rvMedicalRecords;
    private TextView tvMessage;
    private Button btnBack;
    private FirestoreRepository repo;
    private String maTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xembenhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        rvMedicalRecords = findViewById(R.id.rvMedicalRecords);
        tvMessage = findViewById(R.id.tvMessage);
        btnBack = findViewById(R.id.btnBack);

        rvMedicalRecords.setLayoutManager(new LinearLayoutManager(this));

        btnBack.setOnClickListener(v -> finish());

        loadMedicalRecords();
    }

    private void loadMedicalRecords() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Log.e("ViewMedicalRecordActivity", "maTaiKhoan is null or empty");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Mã tài khoản không hợp lệ!");
            return;
        }

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("ViewMedicalRecordActivity", "No BenhNhan found for maTaiKhoan: " + maTaiKhoan);
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Không tìm thấy thông tin bệnh nhân!");
                        return;
                    }

                    try {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            String maBenhNhan = benhNhan.getMaBenhNhan();
                            repo.getByField("BenhAn", "maBenhNhan", maBenhNhan,
                                    querySnapshot1 -> {
                                        List<BenhAn> benhAnList = new ArrayList<>();
                                        for (var doc : querySnapshot1.getDocuments()) {
                                            BenhAn benhAn = doc.toObject(BenhAn.class);
                                            if (benhAn != null) {
                                                benhAnList.add(benhAn);
                                            }
                                        }
                                        if (benhAnList.isEmpty()) {
                                            tvMessage.setVisibility(View.VISIBLE);
                                            tvMessage.setText("Không có bệnh án nào!");
                                        } else {
                                            tvMessage.setVisibility(View.GONE);
                                            rvMedicalRecords.setAdapter(new BenhAnAdapter(benhAnList, maTaiKhoan));
                                        }
                                    },
                                    e -> {
                                        Log.e("ViewMedicalRecordActivity", "Firestore query error: ", e);
                                        Toast.makeText(this, "Lỗi tải bệnh án: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        tvMessage.setVisibility(View.VISIBLE);
                                        tvMessage.setText("Lỗi tải bệnh án: " + e.getMessage());
                                    });
                        }
                    } catch (Exception e) {
                        Log.e("ViewMedicalRecordActivity", "Error parsing BenhNhan: ", e);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Lỗi: " + e.getMessage());
                    }
                },
                e -> {
                    Log.e("ViewMedicalRecordActivity", "Firestore query error: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Lỗi tải thông tin: " + e.getMessage());
                });
    }
}