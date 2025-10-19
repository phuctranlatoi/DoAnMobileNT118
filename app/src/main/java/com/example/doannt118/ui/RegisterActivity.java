package com.example.doannt118.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;

import com.example.doannt118.R;
import com.example.doannt118.repository.UserRepository;
import com.example.doannt118.utils.ToastUtils;
import com.example.doannt118.model.TaiKhoan;

public class RegisterActivity extends AppCompatActivity {

    private EditText txtTenDangNhap, txtMatKhau;
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
        radioBenhNhan = findViewById(R.id.radioBenhNhan);
        radioNhanVien = findViewById(R.id.radioNhanVien);
        btnDangKy = findViewById(R.id.btnDangKy);
        btnQuayLai = findViewById(R.id.btnQuayLai);

        btnDangKy.setOnClickListener(v -> handleDangKy());
        btnQuayLai.setOnClickListener(v -> finish());
    }

    private void handleDangKy() {
        String maTaiKhoan = txtTenDangNhap.getText().toString().trim();
        String tenDangNhap = txtTenDangNhap.getText().toString().trim();
        String matKhau = txtMatKhau.getText().toString().trim();
        String vaiTro = radioBenhNhan.isChecked() ? "benhnhan" : "nhanvien";

        if (tenDangNhap.isEmpty() || matKhau.isEmpty()) {
            ToastUtils.show(this, "Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        TaiKhoan user = new TaiKhoan(maTaiKhoan, tenDangNhap, matKhau,vaiTro);
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
        radioBenhNhan.setChecked(true);
    }
}
