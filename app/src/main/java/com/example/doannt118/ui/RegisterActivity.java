// Đường dẫn: app/src/main/java/com/example/doannt118/ui/RegisterActivity.java
package com.example.doannt118.ui;

import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.tasks.Task;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    private EditText txtTenDangNhap, txtMatKhau, txtHoTen, txtSoDienThoai, txtDiaChi;
    private RadioGroup groupVaiTro;
    private Button btnDangKy, btnQuayLai;
    private FirestoreRepository repo;

    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        repo = new FirestoreRepository();

        // Ánh xạ views
        txtTenDangNhap = findViewById(R.id.txtTenDangNhap);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        txtHoTen = findViewById(R.id.txtHoTen);
        txtSoDienThoai = findViewById(R.id.txtSoDienThoai);
        txtDiaChi = findViewById(R.id.txtDiaChi);
        groupVaiTro = findViewById(R.id.groupVaiTro);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnQuayLai = findViewById(R.id.btnQuayLai);

        // Bắt sự kiện click
        btnDangKy.setOnClickListener(v -> handleRegister());
        btnQuayLai.setOnClickListener(v -> finish());

        // === LOGIC MỚI: Ẩn/Hiện trường Địa Chỉ ===
        // Vì Bác sĩ không có địa chỉ, Bệnh nhân thì có
        groupVaiTro.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioBenhNhan) {
                txtDiaChi.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.radioBacSi) {
                txtDiaChi.setVisibility(View.GONE);
            }
        });
    }

    private void handleRegister() {
        // Lấy dữ liệu
        String tenDangNhap = txtTenDangNhap.getText().toString().trim();
        String matKhau = txtMatKhau.getText().toString().trim();
        String hoTen = txtHoTen.getText().toString().trim();
        String sdt = txtSoDienThoai.getText().toString().trim();
        String diaChi = txtDiaChi.getText().toString().trim(); // Chỉ dùng nếu là Bệnh nhân

        // 1. Lấy vai trò được chọn
        int selectedRoleId = groupVaiTro.getCheckedRadioButtonId();
        String vaiTro;

        if (selectedRoleId == R.id.radioBenhNhan) {
            vaiTro = "Bệnh nhân";
        } else if (selectedRoleId == R.id.radioBacSi) {
            vaiTro = "Bác sĩ";
        } else {
            Toast.makeText(this, "Vui lòng chọn vai trò!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Kiểm tra thông tin nhập (Validation)
        if (tenDangNhap.isEmpty() || matKhau.isEmpty() || hoTen.isEmpty() || sdt.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ Tên đăng nhập, Mật khẩu, Họ tên và SĐT!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validation riêng cho Bệnh nhân (bắt buộc nhập địa chỉ)
        if (vaiTro.equals("Bệnh nhân") && diaChi.isEmpty()) {
            Toast.makeText(this, "Bệnh nhân vui lòng nhập địa chỉ!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Kiểm tra tên đăng nhập đã tồn tại chưa
        repo.getByField(COLLECTION_TAIKHOAN, "tenDangNhap", tenDangNhap,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(this, "Tên đăng nhập đã được sử dụng!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Tên đăng nhập hợp lệ -> Tạo tài khoản
                        createNewAccount(tenDangNhap, matKhau, hoTen, sdt, diaChi, vaiTro);
                    }
                },
                e -> {
                    Toast.makeText(this, "Lỗi kiểm tra: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void createNewAccount(String tenDangNhap, String matKhau, String hoTen, String sdt, String diaChi, String vaiTro) {

        // === LOGIC MỚI: TẠO 2 MÃ UUID ===
        String maTaiKhoan = UUID.randomUUID().toString();
        String maProfile = UUID.randomUUID().toString(); // Đây là maBenhNhan hoặc maBacSi

        // 5. Tạo đối tượng TaiKhoan
        TaiKhoan newTaiKhoan = new TaiKhoan(maTaiKhoan, tenDangNhap, matKhau, vaiTro);

        // 6. Tạo đối tượng Profile (BenhNhan hoặc BacSi)
        Object userProfile;
        if ("Bệnh nhân".equals(vaiTro)) {
            userProfile = new BenhNhan(maProfile, maTaiKhoan, hoTen, sdt, diaChi);
        } else { // "Bác sĩ"
            userProfile = new BacSi(maProfile, maTaiKhoan, hoTen, sdt);
        }

        // 7. Gọi hàm registerNewUserBatch từ repository
        Task<Void> task = repo.registerNewUserBatch(newTaiKhoan, userProfile);

        if (task != null) {
            task.addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        finish(); // Đóng màn hình đăng ký
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RegisterActivity", "Lỗi khi ghi batch: ", e);
                        Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}