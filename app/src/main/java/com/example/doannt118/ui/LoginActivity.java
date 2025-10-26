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
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import org.mindrot.jbcrypt.BCrypt;

public class LoginActivity extends AppCompatActivity {
    private EditText txtTenDangNhap, txtMatKhau;
    private Button btnDangNhap;
    private TextView tvQuenMatKhau, tvDangKy;
    private FirebaseAuth auth;
    private FirestoreRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txtTenDangNhap = findViewById(R.id.txtTenDangNhap);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        btnDangNhap = findViewById(R.id.btnDangNhap);
        tvQuenMatKhau = findViewById(R.id.tvQuenMatKhau);
        tvDangKy = findViewById(R.id.tvDangKy);
        auth = FirebaseAuth.getInstance();
        repo = new FirestoreRepository();

        btnDangNhap.setOnClickListener(v -> {
            String input = txtTenDangNhap.getText().toString().trim();
            String matKhau = txtMatKhau.getText().toString().trim();

            if (input.isEmpty() || matKhau.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            String field = Patterns.EMAIL_ADDRESS.matcher(input).matches() ? "email" : "tenDangNhap";
            Log.d("LoginActivity", "Attempting login with field: " + field + ", value: " + input);

            repo.getByField("TaiKhoan", field, input,
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            var doc = querySnapshot.getDocuments().get(0);
                            String storedHash = doc.getString("matKhau");
                            String vaiTro = doc.getString("vaiTro");
                            String maTaiKhoan = doc.getString("maTaiKhoan");
                            String email = doc.getString("email");
                            String trangThai = doc.getString("trangThai");

                            Log.d("LoginActivity", "Found account: maTaiKhoan=" + maTaiKhoan + ", vaiTro=" + vaiTro + ", trangThai=" + trangThai);

                            if (!trangThai.equals("Hoạt động")) {
                                String message = trangThai.equals("Chờ duyệt")
                                        ? "Tài khoản đang chờ duyệt. Vui lòng liên hệ quản trị viên!"
                                        : "Tài khoản bị khóa!";
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            auth.signInWithEmailAndPassword(email, matKhau)
                                    .addOnSuccessListener(authResult -> {
                                        if (authResult.getUser() == null) {
                                            Log.e("LoginActivity", "Firebase user is null");
                                            Toast.makeText(this, "Lỗi đăng nhập Firebase!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if (!authResult.getUser().isEmailVerified()) {
                                            authResult.getUser().sendEmailVerification();
                                            Toast.makeText(this, "Vui lòng xác thực email! Đã gửi lại link xác thực.", Toast.LENGTH_LONG).show();
                                            return;
                                        }

                                        if (!BCrypt.checkpw(matKhau, storedHash)) {
                                            String newHashedPassword = BCrypt.hashpw(matKhau, BCrypt.gensalt());
                                            repo.updatePassword(email, newHashedPassword,
                                                    aVoid -> {
                                                        Log.d("LoginActivity", "Password synced, navigating for vaiTro=" + vaiTro);
                                                        navigateToActivity(vaiTro, maTaiKhoan);
                                                    },
                                                    e -> {
                                                        Log.e("LoginActivity", "Password sync failed: ", e);
                                                        Toast.makeText(this, "Lỗi đồng bộ mật khẩu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        } else {
                                            Log.d("LoginActivity", "Password matched, navigating for vaiTro=" + vaiTro);
                                            navigateToActivity(vaiTro, maTaiKhoan);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("LoginActivity", "Firebase login failed: ", e);
                                        if (BCrypt.checkpw(matKhau, storedHash)) {
                                            Toast.makeText(this, "Mật khẩu Firebase không khớp. Vui lòng đặt lại mật khẩu!", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.w("LoginActivity", "Account not found for input: " + input);
                            Toast.makeText(this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                        }
                    },
                    e -> {
                        Log.e("LoginActivity", "Firestore query failed: ", e);
                        Toast.makeText(this, "Lỗi kiểm tra tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        tvQuenMatKhau.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));
        tvDangKy.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void navigateToActivity(String vaiTro, String maTaiKhoan) {
        Intent intent;
        try {
            if (vaiTro.equals("Admin")) {
                intent = new Intent(LoginActivity.this, MainAdminActivity.class);
            } else if (vaiTro.equals("Bác sĩ")) {
                intent = new Intent(LoginActivity.this, MainBacSiActivity.class);
            } else {
                intent = new Intent(LoginActivity.this, MainActivity.class);
            }
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e("LoginActivity", "Navigation failed: ", e);
            Toast.makeText(this, "Lỗi chuyển hướng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}