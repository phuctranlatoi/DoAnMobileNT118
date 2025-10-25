// Đường dẫn: app/src/main/java/com/example/doannt118/ui/LoginActivity.java
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

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private FirestoreRepository repo; // Sử dụng Repository

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Repository
        repo = new FirestoreRepository();

        // Ánh xạ View
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Bắt sự kiện click
        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> {
            // Chuyển sang màn hình RegisterActivity
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

        // 1. Gọi hàm getByField từ repository
        repo.getByField("tenDangNhap", user,
                // 2. Xử lý khi thành công
                querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        // Không tìm thấy tài khoản
                        Toast.makeText(this, "Tên đăng nhập không tồn tại!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        // Lấy tài khoản đầu tiên tìm thấy
                        TaiKhoan taiKhoan = querySnapshot.getDocuments().get(0).toObject(TaiKhoan.class);

                        if (taiKhoan == null) {
                            Toast.makeText(this, "Lỗi dữ liệu tài khoản!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 3. So sánh mật khẩu
                        if (pass.equals(taiKhoan.getMatKhau())) {
                            Toast.makeText(this, "Đăng nhập thành công! (Vai trò: " + taiKhoan.getVaiTro() + ")", Toast.LENGTH_LONG).show();

                            // TODO: Chuyển sang màn hình chính (ví dụ: HomeActivity)
                            // Intent intent = new Intent(this, HomeActivity.class);
                            // intent.putExtra("USER_ID", taiKhoan.getMaTaiKhoan());
                            // startActivity(intent);
                            // finish(); // Đóng LoginActivity
                        } else {
                            // Sai mật khẩu
                            Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("LoginActivity", "Lỗi khi chuyển đổi đối tượng: ", e);
                        Toast.makeText(this, "Có lỗi xảy ra khi xử lý dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                },
                // 4. Xử lý khi thất bại (ví dụ: không có mạng)
                e -> {
                    Log.e("LoginActivity", "Lỗi khi truy vấn Firestore: ", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }
}