package com.example.doannt118.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import com.example.doannt118.R;
import com.example.doannt118.model.User;
import com.example.doannt118.repository.UserRepository;
import com.example.doannt118.utils.ToastUtils;

public class RegisterActivity extends AppCompatActivity {

    private EditText txtTenDangNhap, txtMatKhau, txtHoTen, txtSoDienThoai, txtDiaChi;
    private RadioButton radioBenhNhan, radioNhanVien;
    private Button btnDangKy, btnQuayLai;

    private UserRepository repository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ánh xạ view
        txtTenDangNhap = findViewById(R.id.txtTenDangNhap);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        txtHoTen = findViewById(R.id.txtHoTen);
        txtSoDienThoai = findViewById(R.id.txtSoDienThoai);
        txtDiaChi = findViewById(R.id.txtDiaChi);
        radioBenhNhan = findViewById(R.id.radioBenhNhan);
        radioNhanVien = findViewById(R.id.radioNhanVien);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnQuayLai = findViewById(R.id.btnQuayLai);

        btnDangKy.setOnClickListener(v -> handleDangKy());
        btnQuayLai.setOnClickListener(v -> finish());
    }

    private void handleDangKy() {
        String username = txtTenDangNhap.getText().toString().trim();
        String password = txtMatKhau.getText().toString().trim();
        String hoTen = txtHoTen.getText().toString().trim();
        String sdt = txtSoDienThoai.getText().toString().trim();
        String diaChi = txtDiaChi.getText().toString().trim();
        String vaiTro = radioBenhNhan.isChecked() ? "benhnhan" : "nhanvien";

        if (username.isEmpty() || password.isEmpty() || hoTen.isEmpty() ||
                sdt.isEmpty() || diaChi.isEmpty()) {
            ToastUtils.show(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        User user = new User(username, password, hoTen, sdt, diaChi, vaiTro);
        boolean success = repository.addUser(user);

        if (success) {
            ToastUtils.show(this, "Đăng ký thành công!");
            clearForm();
        } else {
            ToastUtils.show(this, "Tên đăng nhập đã tồn tại!");
        }
    }

    private void clearForm() {
        txtTenDangNhap.setText("");
        txtMatKhau.setText("");
        txtHoTen.setText("");
        txtSoDienThoai.setText("");
        txtDiaChi.setText("");
        radioBenhNhan.setChecked(true);
    }
}
