package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainBacSiActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private TextView tvUserName;
    private RecyclerView rvAppointments;
    private FirestoreRepository repo;
    private String maTaiKhoan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_bacsi);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        tvUserName = toolbar.findViewById(R.id.tvUserName);
        rvAppointments = findViewById(R.id.rvAppointments);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_manage_patient) {
                Toast.makeText(this, "Quản Lý Bệnh Nhân", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_manage_medical_record) {
                Toast.makeText(this, "Quản Lý Bệnh Án", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_manage_schedule) {
                Toast.makeText(this, "Quản Lý Lịch Làm Việc", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_manage_prescription) {
                Toast.makeText(this, "Quản Lý Đơn Thuốc", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_confirm_appointment) {
                Toast.makeText(this, "Xác Nhận Lịch Khám", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_manage_invoice) {
                Toast.makeText(this, "Quản Lý Hóa Đơn", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawers();
            return true;
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            String maLichSu = UUID.randomUUID().toString();
            LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, "Đăng xuất", new Date(), "Đăng xuất khỏi hệ thống");
            repo.logActivity(lichSu);
            Intent intent = new Intent(MainBacSiActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        loadUserInfo();
        // loadAppointments();
    }

    private void loadUserInfo() {
        repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                        if (bacSi != null) {
                            tvUserName.setText(bacSi.getHoTen());
                        }
                    }
                },
                e -> Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}