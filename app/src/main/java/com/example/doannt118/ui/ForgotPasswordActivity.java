package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText txtEmail;
    private Button btnGuiOTP; // chỉ còn 1 nút
    private FirestoreRepository repo;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ
        txtEmail = findViewById(R.id.txtEmail);
        btnGuiOTP = findViewById(R.id.btnGuiOTP);
        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();

        // Đổi text nút
        btnGuiOTP.setText("Gửi link đặt lại mật khẩu");

        // Xử lý khi bấm gửi
        btnGuiOTP.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Vui lòng nhập email hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra email có tồn tại trong Firestore không
            repo.getByField("TaiKhoan", "email", email,
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            // Nếu có -> gửi email reset mật khẩu
                            auth.sendPasswordResetEmail(email)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this,
                                                "Đã gửi link đặt lại mật khẩu tới " + email + ". Vui lòng kiểm tra hộp thư!",
                                                Toast.LENGTH_LONG).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this,
                                            "Lỗi gửi link: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Email không tồn tại trong hệ thống!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    e -> Toast.makeText(this, "Lỗi khi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}
