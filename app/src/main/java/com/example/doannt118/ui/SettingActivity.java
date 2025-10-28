package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.doannt118.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Button btnLogout;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbar);
        btnLogout = findViewById(R.id.btnLogout);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Nút back
        }

        // Xử lý sự kiện đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogout();
            }
        });
    }

    private void handleLogout() {
        auth.signOut();
        Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Xử lý nút back trên Toolbar
        return true;
    }
}