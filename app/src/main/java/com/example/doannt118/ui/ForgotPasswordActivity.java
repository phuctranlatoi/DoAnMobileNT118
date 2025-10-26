package com.example.doannt118.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText edtEmail;
    private Button btnResetPassword;
    private FirestoreRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        repo = new FirestoreRepository();

        btnResetPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Vui lòng nhập email đã đăng ký!", Toast.LENGTH_SHORT).show();
                return;
            }

            ProgressDialog progress = new ProgressDialog(this);
            progress.setMessage("Đang gửi email đặt lại mật khẩu...");
            progress.setCancelable(false);
            progress.show();

            repo.sendPasswordResetEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        progress.dismiss();
                        Toast.makeText(this, "Email đặt lại mật khẩu đã được gửi! Vui lòng kiểm tra hộp thư.", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progress.dismiss();
                        Toast.makeText(this, "Gửi email thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
