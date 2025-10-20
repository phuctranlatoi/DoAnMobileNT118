package com.example.doannt118.repository;

import com.example.doannt118.model.TaiKhoan;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;
import java.util.List;

public class FirestoreRepository {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public Task<Void> addUser(TaiKhoan user) {
        return db.collection("users")
                .document(user.getMaTaiKhoan())
                .set(user);
    }
}
