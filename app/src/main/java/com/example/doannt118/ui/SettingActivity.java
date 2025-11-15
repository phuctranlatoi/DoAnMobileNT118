package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.example.doannt118.R;

public class SettingActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private SwitchCompat switchThongBao, switchCamera, switchMicro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ View
        toolbar = findViewById(R.id.toolbar);
        switchThongBao = findViewById(R.id.switchThongBao);
        switchCamera = findViewById(R.id.switchCamera);
        switchMicro = findViewById(R.id.switchMicro);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Thông báo
        View settingThongBao = findViewById(R.id.settingThongBao);
        if (settingThongBao != null) {
            settingThongBao.setOnClickListener(v -> {
                if (switchThongBao != null) {
                    switchThongBao.setChecked(!switchThongBao.isChecked());
                }
            });
        }

        // Camera
        View settingCamera = findViewById(R.id.settingCamera);
        if (settingCamera != null) {
            settingCamera.setOnClickListener(v -> {
                if (switchCamera != null) {
                    switchCamera.setChecked(!switchCamera.isChecked());
                }
            });
        }

        // Micro
        View settingMicro = findViewById(R.id.settingMicro);
        if (settingMicro != null) {
            settingMicro.setOnClickListener(v -> {
                if (switchMicro != null) {
                    switchMicro.setChecked(!switchMicro.isChecked());
                }
            });
        }

        // Thay đổi mật khẩu
        View settingChangePassword = findViewById(R.id.settingChangePassword);
        if (settingChangePassword != null) {
            settingChangePassword.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Xử lý switch listeners
        if (switchThongBao != null) {
            switchThongBao.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Thông báo: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show()
            );
        }

        if (switchCamera != null) {
            switchCamera.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Camera: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show()
            );
        }

        if (switchMicro != null) {
            switchMicro.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(this, "Micro: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show()
            );
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}