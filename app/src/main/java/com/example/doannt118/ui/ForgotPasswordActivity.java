package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.UUID;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText etEmail;
    private Button btnResetPassword, btnBack;
    private FirestoreRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_forgot_password);

        repo = new FirestoreRepository();
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBack = findViewById(R.id.btnBack);

        btnResetPassword.setOnClickListener(v -> handleResetPassword());
        btnBack.setOnClickListener(v -> finish());
    }

    private void handleResetPassword() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập email hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra email có tồn tại trong Firestore
        repo.getByField("TaiKhoan", "email", email,
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Email không tồn tại!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Gửi email đặt lại mật khẩu qua FirebaseAuth
                        repo.sendPasswordResetEmail(email)
                                .addOnSuccessListener(aVoid -> {
                                    String maLichSu = UUID.randomUUID().toString();
                                    String maTaiKhoan = querySnapshot.getDocuments().get(0).toObject(TaiKhoan.class).getMaTaiKhoan();
                                    LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, "Yêu cầu đặt lại mật khẩu", new Date(), "Gửi email đặt lại mật khẩu");
                                    repo.logActivity(lichSu);
                                    Toast.makeText(this, "Email đặt lại mật khẩu đã được gửi! Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                },
                e -> Toast.makeText(this, "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}