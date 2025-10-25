package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;

import org.mindrot.jbcrypt.BCrypt; // Thêm để kiểm tra mật khẩu đã băm

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private FirestoreRepository repo;

    // Thêm hằng số này để tránh gõ nhầm
    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);  // Thêm để init Firebase nếu cần
        setContentView(R.layout.activity_login);

        repo = new FirestoreRepository();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void handleLogin() {
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. === SỬA LỖI Ở ĐÂY ===
        // Thêm "COLLECTION_TAIKHOAN" làm tham số đầu tiên
        repo.getByField(COLLECTION_TAIKHOAN, "tenDangNhap", user,

                // 2. Xử lý khi thành công
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Tên đăng nhập không tồn tại!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        TaiKhoan taiKhoan = querySnapshot.getDocuments().get(0).toObject(TaiKhoan.class);
                        if (taiKhoan == null) {
                            Toast.makeText(this, "Lỗi dữ liệu tài khoản!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 3. So sánh mật khẩu (sửa thành checkpw cho BCrypt)
                        if (BCrypt.checkpw(pass, taiKhoan.getMatKhau())) {
                            Toast.makeText(this, "Đăng nhập thành công! (Vai trò: " + taiKhoan.getVaiTro() + ")", Toast.LENGTH_LONG).show();

                            // TODO: Chuyển sang màn hình chính
                        } else {
                            Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Lỗi khi chuyển đổi đối tượng: ", e);
                    }
                },

                // 4. Xử lý khi thất bại
                e -> {
                    Log.e("LoginActivity", "Lỗi khi truy vấn Firestore: ", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}