package com.example.doannt118.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;
import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    FirestoreRepository repo = new FirestoreRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        // Thử thêm 1 user vào Firestore
        User user = new User("u01", "Phúc", 20);
        repo.addUser(user)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã thêm user", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
