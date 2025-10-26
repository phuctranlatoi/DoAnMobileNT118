package com.example.doannt118.repository;

import android.util.Log;

import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.model.TaiKhoan;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class FirestoreRepository {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";
    private static final String COLLECTION_BENHNHAN = "BenhNhan";
    private static final String COLLECTION_BACSI = "BacSi";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void getByField(String collection, String field, String value,
                           OnSuccessListener<QuerySnapshot> onSuccess,
                           OnFailureListener onFailure) {
        db.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public Task<Void> registerNewUserBatch(TaiKhoan taiKhoan, Object userProfile) {
        String profileCollectionName;
        String profileDocumentId;

        if (userProfile instanceof BenhNhan) {
            profileCollectionName = COLLECTION_BENHNHAN;
            profileDocumentId = ((BenhNhan) userProfile).getMaBenhNhan();
        } else if (userProfile instanceof BacSi) {
            profileCollectionName = COLLECTION_BACSI;
            profileDocumentId = ((BacSi) userProfile).getMaBacSi();
        } else {
            return null;
        }

        String taiKhoanId = taiKhoan.getMaTaiKhoan();
        DocumentReference taiKhoanRef = db.collection(COLLECTION_TAIKHOAN).document(taiKhoanId);
        DocumentReference profileRef = db.collection(profileCollectionName).document(profileDocumentId);

        WriteBatch batch = db.batch();
        batch.set(taiKhoanRef, taiKhoan);
        batch.set(profileRef, userProfile);

        return batch.commit();
    }

    public void logActivity(LichSuHoatDong lichSu) {
        db.collection("LichSuHoatDong").document(lichSu.getMaLichSu()).set(lichSu)
                .addOnFailureListener(e -> Log.e("FirestoreRepository", "Lỗi ghi log: ", e));
    }

    public Task<Void> updatePassword(String email, String newPassword) {
        return db.collection(COLLECTION_TAIKHOAN)
                .whereEqualTo("email", email)
                .get()
                .continueWithTask(task -> {
                    if (!task.isSuccessful() || task.getResult().isEmpty()) {
                        throw new Exception("Email không tồn tại");
                    }
                    DocumentReference taiKhoanRef = task.getResult().getDocuments().get(0).getReference();
                    return taiKhoanRef.update("matKhau", newPassword);
                });
    }

    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }
}