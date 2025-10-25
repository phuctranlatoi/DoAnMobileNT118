// Đường dẫn: app/src/main/java/com/example/doannt118/repository/FirestoreRepository.java
package com.example.doannt118.repository;

import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.TaiKhoan;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

public class FirestoreRepository {
    private FirebaseFirestore db;

    // Tên các collection
    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";
    private static final String COLLECTION_BENHNHAN = "BenhNhan";
    private static final String COLLECTION_BACSI = "BacSi"; // Đổi từ NhanVien

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Hàm này lấy tài khoản dựa trên một trường (field) và giá trị (value)
     * Dùng để kiểm tra tenDangNhap có tồn tại không
     */
    public void getByField(String collection, String field, String value,
                           OnSuccessListener<QuerySnapshot> onSuccess,
                           OnFailureListener onFailure) {

        db.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * HÀM ĐÃ CẬP NHẬT: Dùng WriteBatch để đăng ký tài khoản
     * Ghi đồng thời vào collection TaiKhoan và collection (BenhNhan hoặc BacSi)
     */
    public Task<Void> registerNewUserBatch(TaiKhoan taiKhoan, Object userProfile) {

        String profileCollectionName;
        String profileDocumentId;

        // Xác định collection và ID để lưu profile
        if (userProfile instanceof BenhNhan) {
            profileCollectionName = COLLECTION_BENHNHAN;
            profileDocumentId = ((BenhNhan) userProfile).getMaBenhNhan();
        } else if (userProfile instanceof BacSi) {
            profileCollectionName = COLLECTION_BACSI;
            profileDocumentId = ((BacSi) userProfile).getMaBacSi();
        } else {
            return null; // Lỗi không xác định
        }

        // Lấy ID tài khoản
        String taiKhoanId = taiKhoan.getMaTaiKhoan();

        // Tham chiếu đến 2 document sẽ tạo
        DocumentReference taiKhoanRef = db.collection(COLLECTION_TAIKHOAN).document(taiKhoanId);
        DocumentReference profileRef = db.collection(profileCollectionName).document(profileDocumentId);

        // Bắt đầu một batch write
        WriteBatch batch = db.batch();
        batch.set(taiKhoanRef, taiKhoan); // Ghi tài khoản
        batch.set(profileRef, userProfile); // Ghi profile

        // Thực thi batch
        return batch.commit();
    }
}