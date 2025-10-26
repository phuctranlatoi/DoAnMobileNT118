package com.example.doannt118.ui;

import android.content.Intent;
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
import java.util.ArrayList;
import java.util.List;

public class XembenhanActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView rvMedicalRecords;
    private TextView tvMessage;
    private Button btnBack;
    private ProgressBar progressBar;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private BenhAnAdapter benhAnAdapter;
    private List<BenhAn> benhAnList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xembenhan);

        // Initialize Firestore and intent data
        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Danh Sách Bệnh Án");

        rvMedicalRecords = findViewById(R.id.rvMedicalRecords);
        tvMessage = findViewById(R.id.tvMessage);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Set up RecyclerView
        rvMedicalRecords.setLayoutManager(new LinearLayoutManager(this));
        benhAnList = new ArrayList<>();
        benhAnAdapter = new BenhAnAdapter(benhAnList, benhAn -> {
            Intent intent = new Intent(XembenhanActivity.this, ChiTietBenhAnActivity.class);
            intent.putExtra("MA_BENH_AN", benhAn.getMaBenhAn());
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
        });
        rvMedicalRecords.setAdapter(benhAnAdapter);

        // Set up button listener
        btnBack.setOnClickListener(v -> finish());

        // Load medical records
        loadMedicalRecords();
    }

    private void loadMedicalRecords() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Log.e("XembenhanActivity", "maTaiKhoan is null or empty");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText("Mã tài khoản không hợp lệ!");
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("XembenhanActivity", "No BenhNhan found for maTaiKhoan: " + maTaiKhoan);
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Không tìm thấy thông tin bệnh nhân!");
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    try {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            String maBenhNhan = benhNhan.getMaBenhNhan();
                            repo.getByField("BenhAn", "maBenhNhan", maBenhNhan,
                                    querySnapshot1 -> {
                                        benhAnList.clear();
                                        for (var doc : querySnapshot1.getDocuments()) {
                                            BenhAn benhAn = doc.toObject(BenhAn.class);
                                            if (benhAn != null) {
                                                benhAn.setMaBenhAn(doc.getId()); // Set Firestore document ID
                                                benhAnList.add(benhAn);
                                            }
                                        }
                                        if (benhAnList.isEmpty()) {
                                            tvMessage.setVisibility(View.VISIBLE);
                                            tvMessage.setText("Không có bệnh án nào!");
                                        } else {
                                            tvMessage.setVisibility(View.GONE);
                                            benhAnAdapter.notifyDataSetChanged();
                                        }
                                        progressBar.setVisibility(View.GONE);
                                    },
                                    e -> {
                                        Log.e("XembenhanActivity", "Firestore query error: ", e);
                                        Toast.makeText(this, "Lỗi tải bệnh án: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        tvMessage.setVisibility(View.VISIBLE);
                                        tvMessage.setText("Lỗi tải bệnh án: " + e.getMessage());
                                        progressBar.setVisibility(View.GONE);
                                    });
                        }
                    } catch (Exception e) {
                        Log.e("XembenhanActivity", "Error parsing BenhNhan: ", e);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        tvMessage.setVisibility(View.VISIBLE);
                        tvMessage.setText("Lỗi: " + e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                },
                e -> {
                    Log.e("XembenhanActivity", "Firestore query error: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvMessage.setVisibility(View.VISIBLE);
                    tvMessage.setText("Lỗi tải thông tin: " + e.getMessage());
                    progressBar.setVisibility(View.GONE);
                });
    }
}