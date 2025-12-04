package com.example.doannt118.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import com.example.doannt118.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        // Kiểm tra user đã đăng nhập chưa
        checkUserLogin();
    }
    
    private void checkUserLogin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser != null) {
            // Đã đăng nhập, chuyển đến màn hình chính
            // TODO: Kiểm tra role và chuyển đến màn hình tương ứng
            finish();
        } else {
            // Chưa đăng nhập, chuyển đến màn hình login
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}