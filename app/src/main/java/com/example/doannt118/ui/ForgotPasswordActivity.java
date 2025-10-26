package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText txtEmail, txtOTP, txtMatKhauMoi;
    private Button btnGuiOTP, btnXacNhan;
    private FirestoreRepository repo;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        txtEmail = findViewById(R.id.txtEmail);
        txtOTP = findViewById(R.id.txtOTP);
        txtMatKhauMoi = findViewById(R.id.txtMatKhauMoi);
        btnGuiOTP = findViewById(R.id.btnGuiOTP);
        btnXacNhan = findViewById(R.id.btnXacNhan);
        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();

        // Ẩn và vô hiệu hóa các trường không dùng
        txtOTP.setEnabled(false);
        txtOTP.setVisibility(View.GONE);
        txtMatKhauMoi.setEnabled(false);
        txtMatKhauMoi.setVisibility(View.GONE);
        btnXacNhan.setEnabled(false);
        btnXacNhan.setVisibility(View.GONE);

        // Đổi tên nút cho rõ ràng
        btnGuiOTP.setText("Gửi Link Đặt Lại Mật Khẩu");

        btnGuiOTP.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Vui lòng nhập email hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra email tồn tại trong Firestore
            repo.getByField("TaiKhoan", "email", email,
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Gửi email đặt lại mật khẩu qua Firebase
                            auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Link đặt lại mật khẩu đã được gửi tới " + email + ". Vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                                        finish(); // Quay lại LoginActivity
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi link: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Email không tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    e -> Toast.makeText(this, "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}