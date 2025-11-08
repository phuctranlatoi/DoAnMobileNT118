package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import org.mindrot.jbcrypt.BCrypt;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private EditText txtTenDangNhap, txtMatKhau, txtHoTen, txtSoDienThoai, txtEmail, txtDiaChi, txtNgaySinh;
    private Button btnDangKy, btnQuayLai;
    private FirebaseAuth auth;
    private FirestoreRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txtTenDangNhap = findViewById(R.id.txtTenDangNhap);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        txtHoTen = findViewById(R.id.txtHoTen);
        txtSoDienThoai = findViewById(R.id.txtSoDienThoai);
        txtEmail = findViewById(R.id.txtEmail);
        txtDiaChi = findViewById(R.id.txtDiaChi);
        txtNgaySinh = findViewById(R.id.txtNgaySinh);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnQuayLai = findViewById(R.id.btnQuayLai);
        auth = FirebaseAuth.getInstance();
        repo = new FirestoreRepository();

        btnDangKy.setOnClickListener(v -> {
            String tenDangNhap = txtTenDangNhap.getText().toString().trim();
            String matKhau = txtMatKhau.getText().toString().trim();
            String hoTen = txtHoTen.getText().toString().trim();
            String sdt = txtSoDienThoai.getText().toString().trim();
            String email = txtEmail.getText().toString().trim();
            String diaChi = txtDiaChi.getText().toString().trim();
            String ngaySinh = txtNgaySinh.getText().toString().trim();
            String vaiTro = "Bệnh nhân";

            // Kiểm tra đầu vào
            if (tenDangNhap.isEmpty() || matKhau.isEmpty() || hoTen.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (matKhau.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra tên đăng nhập tồn tại
            repo.getByField("TaiKhoan", "tenDangNhap", tenDangNhap,
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                        } else {
                            // Kiểm tra email tồn tại
                            repo.getByField("TaiKhoan", "email", email,
                                    emailSnapshot -> {
                                        if (!emailSnapshot.isEmpty()) {
                                            Toast.makeText(this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            String maTaiKhoan = UUID.randomUUID().toString();
                                            String maBenhNhan = UUID.randomUUID().toString();
                                            String matKhauDaBam;
                                            try {
                                                matKhauDaBam = BCrypt.hashpw(matKhau, BCrypt.gensalt());
                                            } catch (Exception e) {
                                                Log.e("RegisterActivity", "Error hashing password: ", e);
                                                Toast.makeText(this, "Lỗi băm mật khẩu!", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Tạo tài khoản Firebase
                                            auth.createUserWithEmailAndPassword(email, matKhau)
                                                    .addOnSuccessListener(authResult -> {
                                                        // Gửi email xác thực
                                                        if (authResult.getUser() != null) {
                                                            authResult.getUser().sendEmailVerification();
                                                        }

                                                        // Tạo dữ liệu cho Firestore
                                                        TaiKhoan newTaiKhoan = new TaiKhoan(maTaiKhoan, tenDangNhap, matKhauDaBam, vaiTro, email, "Hoạt động");
                                                        BenhNhan benhNhan = new BenhNhan(maBenhNhan, maTaiKhoan, hoTen, sdt, diaChi, ngaySinh);

                                                        // Lưu vào Firestore
                                                        repo.registerNewUserBatch(newTaiKhoan, benhNhan,
                                                                aVoid -> {
                                                                    Log.d("RegisterActivity", "User registered successfully: " + email);
                                                                    Toast.makeText(this, "Đăng ký thành công! Vui lòng xác thực email.", Toast.LENGTH_LONG).show();
                                                                    auth.signOut(); // Đăng xuất để yêu cầu đăng nhập lại
                                                                    finish();
                                                                },
                                                                e -> {
                                                                    Log.e("RegisterActivity", "Error registering user in Firestore: ", e);
                                                                    // Xóa tài khoản Firebase nếu Firestore thất bại
                                                                    if (auth.getCurrentUser() != null) {
                                                                        auth.getCurrentUser().delete()
                                                                                .addOnSuccessListener(a -> Log.d("RegisterActivity", "Rollback: Firebase user deleted"))
                                                                                .addOnFailureListener(a -> Log.e("RegisterActivity", "Rollback failed: ", a));
                                                                    }
                                                                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("RegisterActivity", "Error creating Firebase user: ", e);
                                                        Toast.makeText(this, "Lỗi tạo tài khoản Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        }
                                    },
                                    e -> {
                                        Log.e("RegisterActivity", "Error checking email: ", e);
                                        Toast.makeText(this, "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    },
                    e -> {
                        Log.e("RegisterActivity", "Error checking username: ", e);
                        Toast.makeText(this, "Lỗi kiểm tra tên đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnQuayLai.setOnClickListener(v -> finish());
    }
}