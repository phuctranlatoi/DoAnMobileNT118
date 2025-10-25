package com.example.doannt118.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan; // <-- 1. THÊM VÀO
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp; // <-- Thêm để init Firebase

import org.mindrot.jbcrypt.BCrypt; // <-- 3. THÊM VÀO
import java.util.UUID; // <-- 4. THÊM VÀO

public class MainActivity extends AppCompatActivity {
    FirestoreRepository repo = new FirestoreRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);  // Thêm để init Firebase nếu cần
        setContentView(R.layout.activity_main);

        // === ĐÂY LÀ CODE ĐÃ SỬA ĐỂ TEST ===
        // Code này sẽ tạo một tài khoản Bệnh nhân mới với mật khẩu đã băm
        // Comment out để tránh chạy tự động ở startup, giảm load gây crash (chạy thủ công nếu cần)

        /*try {
            // Chuẩn bị thông tin
            String maTaiKhoanMoi = UUID.randomUUID().toString();
            String maBenhNhanMoi = UUID.randomUUID().toString();
            String matKhauThuan = "123456";
            String tenDangNhap = "PhucTran";

            // Băm mật khẩu
            String matKhauDaBam = BCrypt.hashpw(matKhauThuan, BCrypt.gensalt());

            // 6. Tạo 2 đối tượng theo đúng logic
            TaiKhoan userAccount = new TaiKhoan(maTaiKhoanMoi, tenDangNhap, matKhauDaBam, "Bệnh nhân");
            BenhNhan userProfile = new BenhNhan(maBenhNhanMoi, maTaiKhoanMoi, "Phuc Tran Test", "0987654321", "TP.HCM");

            // 7. Gọi hàm đúng: "registerNewUserBatch"
            repo.registerNewUserBatch(userAccount, userProfile)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã thêm Bệnh nhân: " + tenDangNhap, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Log.e("MainActivity", "Lỗi khi thêm user: ", e);
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi ngoài lề: ", e);
            Toast.makeText(this, "Lỗi Băm mật khẩu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }*/
    }
}