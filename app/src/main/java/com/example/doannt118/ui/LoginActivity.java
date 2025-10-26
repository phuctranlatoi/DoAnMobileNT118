package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.UUID;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_login);

        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void handleLogin() {
        String input = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (input.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra xem input là email hay username
        if (input.contains("@") && Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            // Đăng nhập bằng email qua FirebaseAuth
            auth.signInWithEmailAndPassword(input, pass)
                    .addOnSuccessListener(authResult -> {
                        // Kiểm tra thông tin tài khoản trong Firestore
                        repo.getByField(COLLECTION_TAIKHOAN, "email", input,
                                querySnapshot -> {
                                    if (querySnapshot.isEmpty()) {
                                        Toast.makeText(this, "Tài khoản không tồn tại trong Firestore!", Toast.LENGTH_SHORT).show();
                                        auth.signOut(); // Đăng xuất nếu không tìm thấy trong Firestore
                                    } else {
                                        processLogin(querySnapshot, input, pass, "email");
                                    }
                                },
                                e -> {
                                    Log.e("LoginActivity", "Lỗi khi truy vấn email: ", e);
                                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    auth.signOut();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoginActivity", "Đăng nhập Firebase thất bại: ", e);
                        Toast.makeText(this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Đăng nhập bằng username qua Firestore
            repo.getByField(COLLECTION_TAIKHOAN, "tenDangNhap", input,
                    querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            Toast.makeText(this, "Tên đăng nhập không tồn tại!", Toast.LENGTH_SHORT).show();
                        } else {
                            processLogin(querySnapshot, input, pass, "tên đăng nhập");
                        }
                    },
                    e -> {
                        Log.e("LoginActivity", "Lỗi khi truy vấn tenDangNhap: ", e);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void processLogin(QuerySnapshot querySnapshot, String input, String pass, String loginMethod) {
        try {
            TaiKhoan taiKhoan = querySnapshot.getDocuments().get(0).toObject(TaiKhoan.class);

            if (taiKhoan == null) {
                Toast.makeText(this, "Lỗi dữ liệu tài khoản!", Toast.LENGTH_SHORT).show();
                auth.signOut();
                return;
            }

            // ✅ Kiểm tra null để tránh crash
            if (taiKhoan.getMatKhau() == null) {
                Toast.makeText(this, "Tài khoản không có mật khẩu!", Toast.LENGTH_SHORT).show();
                auth.signOut();
                return;
            }

            // ✅ Kiểm tra mật khẩu
            if (!BCrypt.checkpw(pass, taiKhoan.getMatKhau())) {
                Toast.makeText(this, "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                auth.signOut();
                return;
            }

            // ✅ Kiểm tra trạng thái tài khoản
            String trangThai = taiKhoan.getTrangThai();
            if (trangThai == null || !trangThai.equals("Hoạt động")) {
                Toast.makeText(this, "Tài khoản đang bị khóa hoặc không hoạt động!", Toast.LENGTH_SHORT).show();
                auth.signOut();
                return;
            }

            // ✅ Kiểm tra vai trò
            String vaiTro = taiKhoan.getVaiTro();
            if (vaiTro == null || vaiTro.isEmpty()) {
                Toast.makeText(this, "Thiếu vai trò tài khoản!", Toast.LENGTH_SHORT).show();
                auth.signOut();
                return;
            }

            // ✅ Ghi lịch sử hoạt động
            String maLichSu = UUID.randomUUID().toString();
            LichSuHoatDong lichSu = new LichSuHoatDong(
                    maLichSu,
                    taiKhoan.getMaTaiKhoan(),
                    "Đăng nhập",
                    new Date(),
                    "Đăng nhập thành công bằng " + loginMethod
            );
            repo.logActivity(lichSu);

            // ✅ Mở giao diện tương ứng
            Toast.makeText(this, "Đăng nhập thành công! (Vai trò: " + vaiTro + ")", Toast.LENGTH_LONG).show();
            Intent intent;

            switch (vaiTro) {
                case "Bệnh nhân":
                    intent = new Intent(this, MainBenhNhanActivity.class);
                    break;
                case "Bác sĩ":
                    intent = new Intent(this, MainBacSiActivity.class);
                    break;
                default:
                    Toast.makeText(this, "Vai trò không hợp lệ: " + vaiTro, Toast.LENGTH_SHORT).show();
                    auth.signOut();
                    return;
            }

            intent.putExtra("MA_TAI_KHOAN", taiKhoan.getMaTaiKhoan());
            intent.putExtra("VAI_TRO", vaiTro);
            startActivity(intent);
            finish();

        } catch (Exception e) {
            Log.e("LoginActivity", "Lỗi khi chuyển đổi đối tượng: ", e);
            Toast.makeText(this, "Lỗi xử lý dữ liệu đăng nhập!", Toast.LENGTH_SHORT).show();
            auth.signOut();
        }
    }
}