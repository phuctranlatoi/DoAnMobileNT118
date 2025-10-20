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

    public Task<List<TaiKhoan>> getUsers() {
        return db.collection("users")
                .get()
                .continueWith(task -> task.getResult().toObjects(TaiKhoan.class));
    }
}
