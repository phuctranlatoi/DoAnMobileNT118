package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
//import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainBenhNhanActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private TextView tvHoTen, tvSoDienThoai, tvDiaChi;
    private RecyclerView rvActivityHistory;
    private FirestoreRepository repo;
    private String maTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_benhnhan);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        // Initialize UI components
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navView = findViewById(R.id.navView);
        tvHoTen = findViewById(R.id.tvHoTen);
        tvSoDienThoai = findViewById(R.id.tvSoDienThoai);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        rvActivityHistory = findViewById(R.id.rvActivityHistory);
        rvActivityHistory.setLayoutManager(new LinearLayoutManager(this));

        // Set up navigation drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle navigation menu clicks
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_register_appointment) {
                Toast.makeText(this, "Đăng Ký Lịch Khám", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_manage_profile) {
                Toast.makeText(this, "Quản Lý Hồ Sơ Cá Nhân", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_view_medical_record) {
                Toast.makeText(this, "Xem Bệnh Án", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_confirm_medication) {
                Toast.makeText(this, "Xác Nhận Dùng Thuốc", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_view_invoice) {
                Toast.makeText(this, "Xem Hóa Đơn", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Set up logout button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(MainBenhNhanActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Load data
        loadUserInfo();
//        loadActivityHistory();
    }

    private void loadUserInfo() {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Log.e("MainBenhNhanActivity", "maTaiKhoan is null or empty");
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("MainBenhNhanActivity", "No BenhNhan found for maTaiKhoan: " + maTaiKhoan);
                        Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                        tvHoTen.setText("Họ tên: N/A");
                        tvSoDienThoai.setText("Số điện thoại: N/A");
                        tvDiaChi.setText("Địa chỉ: N/A");
                        return;
                    }

                    try {
                        BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                        if (benhNhan != null) {
                            Log.d("MainBenhNhanActivity", "BenhNhan data: " + benhNhan.toString());
                            tvHoTen.setText("Họ tên: " + (benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "N/A"));
                            tvSoDienThoai.setText("Số điện thoại: " + (benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "N/A"));
                            tvDiaChi.setText("Địa chỉ: " + (benhNhan.getDiaChi() != null ? benhNhan.getDiaChi() : "N/A"));
                        } else {
                            Log.e("MainBenhNhanActivity", "Failed to parse BenhNhan object");
                            Toast.makeText(this, "Lỗi: Dữ liệu bệnh nhân không hợp lệ", Toast.LENGTH_SHORT).show();
                            tvHoTen.setText("Họ tên: N/A");
                            tvSoDienThoai.setText("Số điện thoại: N/A");
                            tvDiaChi.setText("Địa chỉ: N/A");
                        }
                    } catch (Exception e) {
                        Log.e("MainBenhNhanActivity", "Error parsing BenhNhan: ", e);
                        Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                e -> {
                    Log.e("MainBenhNhanActivity", "Firestore query error: ", e);
                    Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvHoTen.setText("Họ tên: N/A");
                    tvSoDienThoai.setText("Số điện thoại: N/A");
                    tvDiaChi.setText("Địa chỉ: N/A");
                });
    }

//    private void loadActivityHistory() {
//        repo.getByField("LichSuHoatDong", "maTaiKhoan", maTaiKhoan,
//                querySnapshot -> {
//                    List<LichSuHoatDong> list = new ArrayList<>();
//                    for (var doc : querySnapshot.getDocuments()) {
//                        LichSuHoatDong lichSu = doc.toObject(LichSuHoatDong.class);
//                        if (lichSu != null) {
//                            list.add(lichSu);
//                        }
//                    }
//                    Log.d("MainBenhNhanActivity", "Loaded " + list.size() + " activity records");
//                    rvActivityHistory.setAdapter(new ActivityHistoryAdapter(list));
//                },
//                e -> {
//                    Log.e("MainBenhNhanActivity", "Error loading activity history: ", e);
//                    Toast.makeText(this, "Lỗi tải lịch sử hoạt động: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    rvActivityHistory.setAdapter(new ActivityHistoryAdapter(new ArrayList<>()));
//                });
//    }
}