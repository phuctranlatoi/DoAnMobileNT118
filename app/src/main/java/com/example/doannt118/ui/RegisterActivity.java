package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QuerySnapshot;

import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {
    private EditText txtTenDangNhap, txtMatKhau, txtHoTen, txtSoDienThoai, txtDiaChi, txtEmail;
    private RadioGroup groupVaiTro;
    private Button btnDangKy, btnQuayLai;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_register);

        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        txtTenDangNhap = findViewById(R.id.txtTenDangNhap);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        txtHoTen = findViewById(R.id.txtHoTen);
        txtSoDienThoai = findViewById(R.id.txtSoDienThoai);
        txtDiaChi = findViewById(R.id.txtDiaChi);
        txtEmail = findViewById(R.id.txtEmail);
        groupVaiTro = findViewById(R.id.groupVaiTro);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnQuayLai = findViewById(R.id.btnQuayLai);

        btnDangKy.setOnClickListener(v -> handleRegister());
        btnQuayLai.setOnClickListener(v -> finish());

        groupVaiTro.setOnCheckedChangeListener((group, checkedId) -> {
            txtDiaChi.setVisibility(checkedId == R.id.radioBenhNhan ? View.VISIBLE : View.GONE);
        });
    }

    private void handleRegister() {
        String tenDangNhap = txtTenDangNhap.getText().toString().trim();
        String matKhau = txtMatKhau.getText().toString().trim();
        String hoTen = txtHoTen.getText().toString().trim();
        String sdt = txtSoDienThoai.getText().toString().trim();
        String diaChi = txtDiaChi.getText().toString().trim();
        String email = txtEmail.getText().toString().trim();

        int selectedRoleId = groupVaiTro.getCheckedRadioButtonId();
        String vaiTro = selectedRoleId == R.id.radioBenhNhan ? "Bệnh nhân" : selectedRoleId == R.id.radioBacSi ? "Bác sĩ" : "";
        if (vaiTro.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn vai trò!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (tenDangNhap.isEmpty() || matKhau.isEmpty() || hoTen.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (vaiTro.equals("Bệnh nhân") && diaChi.isEmpty()) {
            Toast.makeText(this, "Bệnh nhân cần nhập địa chỉ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra trùng tenDangNhap hoặc email
        repo.getByField(COLLECTION_TAIKHOAN, "tenDangNhap", tenDangNhap,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                    } else {
                        repo.getByField(COLLECTION_TAIKHOAN, "email", email,
                                emailSnapshot -> {
                                    if (!emailSnapshot.isEmpty()) {
                                        Toast.makeText(this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        createNewAccount(tenDangNhap, matKhau, hoTen, sdt, diaChi, vaiTro, email);
                                    }
                                },
                                e -> Toast.makeText(this, "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                },
                e -> Toast.makeText(this, "Lỗi kiểm tra tên đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void createNewAccount(String tenDangNhap, String matKhau, String hoTen, String sdt, String diaChi, String vaiTro, String email) {
        String maTaiKhoan = UUID.randomUUID().toString();
        String maProfile = UUID.randomUUID().toString();
        String matKhauDaBam;
        try {
            matKhauDaBam = BCrypt.hashpw(matKhau, BCrypt.gensalt());
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi băm mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo người dùng trong FirebaseAuth
        auth.createUserWithEmailAndPassword(email, matKhau)
                .addOnSuccessListener(authResult -> {
                    TaiKhoan newTaiKhoan = new TaiKhoan(maTaiKhoan, tenDangNhap, matKhauDaBam, vaiTro, email, "Hoạt động");
                    Object userProfile = vaiTro.equals("Bệnh nhân") ?
                            new BenhNhan(maProfile, maTaiKhoan, hoTen, sdt, diaChi) :
                            new BacSi(maProfile, maTaiKhoan, hoTen, sdt);

                    repo.registerNewUserBatch(newTaiKhoan, userProfile)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Nếu Firestore thất bại, xóa người dùng FirebaseAuth để tránh dữ liệu không nhất quán
                                if (authResult.getUser() != null) {
                                    authResult.getUser().delete();
                                }
                                Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}