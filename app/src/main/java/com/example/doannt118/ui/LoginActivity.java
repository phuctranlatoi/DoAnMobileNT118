package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private FirestoreRepository repo = new FirestoreRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void handleLogin() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        repo.getByField("TaiKhoan", "tenDangNhap", user, task -> {
            QuerySnapshot result = (QuerySnapshot) task;
            if (result.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy tài khoản!", Toast.LENGTH_SHORT).show();
                return;
            }
            String mk = result.getDocuments().get(0).getString("matKhau");
            String vaiTro = result.getDocuments().get(0).getString("vaiTro");

            if (pass.equals(mk)) {
                Toast.makeText(this, "Đăng nhập thành công (" + vaiTro + ")", Toast.LENGTH_SHORT).show();
                // điều hướng sang màn hình phù hợp
            } else {
                Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
            }
        }, e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}

