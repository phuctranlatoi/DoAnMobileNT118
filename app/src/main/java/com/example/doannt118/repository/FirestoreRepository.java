package com.example.doannt118.repository;

import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.TaiKhoan;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirestoreRepository {
    private final FirebaseFirestore db;

    // Tên các collection
    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";
    private static final String COLLECTION_BENHNHAN = "BenhNhan";
    private static final String COLLECTION_BACSI = "BacSi";
    private static final String COLLECTION_BENHAN = "BenhAn";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Lấy dữ liệu từ một collection dựa trên một trường (field) và giá trị (value).
     * Dùng để kiểm tra sự tồn tại của tài khoản, bệnh nhân, bác sĩ, hoặc bệnh án.
     */
    public void getByField(String collection, String field, String value,
                           Consumer<QuerySnapshot> onSuccess,
                           Consumer<Exception> onFailure) {
        if (collection == null || field == null || value == null) {
            onFailure.accept(new IllegalArgumentException("Collection, field, or value cannot be null"));
            return;
        }

        db.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(querySnapshot -> onSuccess.accept(querySnapshot))
                .addOnFailureListener(e -> onFailure.accept(e));
    }

    /**
     * Đăng ký tài khoản mới bằng WriteBatch, ghi đồng thời vào collection TaiKhoan và profile
     * (BenhNhan hoặc BacSi).
     */
    public void registerNewUserBatch(TaiKhoan taiKhoan, Object userProfile,
                                     Consumer<Void> onSuccess,
                                     Consumer<Exception> onFailure) {
        if (taiKhoan == null || userProfile == null) {
            onFailure.accept(new IllegalArgumentException("TaiKhoan or userProfile cannot be null"));
            return;
        }

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
            onFailure.accept(new IllegalArgumentException("userProfile must be BenhNhan or BacSi"));
            return;
        }

        if (profileDocumentId == null || profileDocumentId.isEmpty()) {
            onFailure.accept(new IllegalArgumentException("Profile document ID cannot be null or empty"));
            return;
        }

        // Lấy ID tài khoản
        String taiKhoanId = taiKhoan.getMaTaiKhoan();
        if (taiKhoanId == null || taiKhoanId.isEmpty()) {
            onFailure.accept(new IllegalArgumentException("TaiKhoan ID cannot be null or empty"));
            return;
        }

        // Tham chiếu đến 2 document sẽ tạo
        DocumentReference taiKhoanRef = db.collection(COLLECTION_TAIKHOAN).document(taiKhoanId);
        DocumentReference profileRef = db.collection(profileCollectionName).document(profileDocumentId);

        // Bắt đầu một batch write
        WriteBatch batch = db.batch();
        batch.set(taiKhoanRef, taiKhoan); // Ghi tài khoản
        batch.set(profileRef, userProfile); // Ghi profile

        // Thực thi batch
        batch.commit()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    /**
     * Thêm một tài liệu mới vào collection.
     */
    public void addDocument(String collection, String documentId, Object data,
                            Consumer<Void> onSuccess,
                            Consumer<Exception> onFailure) {
        if (collection == null || documentId == null || data == null) {
            onFailure.accept(new IllegalArgumentException("Collection, documentId, or data cannot be null"));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .set(data)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    /**
     * Cập nhật một tài liệu trong collection.
     */
    public void updateDocument(String collection, String documentId, Object data,
                               Consumer<Void> onSuccess,
                               Consumer<Exception> onFailure) {
        if (collection == null || documentId == null || data == null) {
            onFailure.accept(new IllegalArgumentException("Collection, documentId, or data cannot be null"));
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        if (data instanceof BenhNhan) {
            BenhNhan benhNhan = (BenhNhan) data;
            updates.put("maBenhNhan", benhNhan.getMaBenhNhan());
            updates.put("maTaiKhoan", benhNhan.getMaTaiKhoan());
            updates.put("hoTen", benhNhan.getHoTen());
            updates.put("soDienThoai", benhNhan.getSoDienThoai());
            updates.put("diaChi", benhNhan.getDiaChi());
        } else if (data instanceof BacSi) {
            BacSi bacSi = (BacSi) data;
            updates.put("maBacSi", bacSi.getMaBacSi());
            updates.put("maTaiKhoan", bacSi.getMaTaiKhoan());
            updates.put("hoTen", bacSi.getHoTen());
        } else if (data instanceof BenhAn) {
            BenhAn benhAn = (BenhAn) data;
            updates.put("maBenhAn", benhAn.getMaBenhAn());
            updates.put("maLichKham", benhAn.getMaLichKham());
            updates.put("maBenhNhan", benhAn.getMaBenhNhan());
            updates.put("maBacSi", benhAn.getMaBacSi()); // Added maBacSi
            updates.put("chanDoan", benhAn.getChanDoan());
            updates.put("ghiChu", benhAn.getGhiChu());
            updates.put("ngayKham", benhAn.getNgayKham());
        } else {
            onFailure.accept(new IllegalArgumentException("Data must be BenhNhan, BacSi, or BenhAn"));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    /**
     * Xóa một tài liệu khỏi collection.
     */
    public void deleteDocument(String collection, String documentId,
                               Consumer<Void> onSuccess,
                               Consumer<Exception> onFailure) {
        if (collection == null || documentId == null) {
            onFailure.accept(new IllegalArgumentException("Collection or documentId cannot be null"));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .delete()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    /**
     * Optional: Tìm kiếm tài liệu trong collection với điều kiện linh hoạt hơn.
     * Ví dụ: Tìm kiếm bệnh án theo nhiều trường (maBenhAn, maBenhNhan).
     */
    public void searchDocuments(String collection, String field, String keyword,
                                Consumer<QuerySnapshot> onSuccess,
                                Consumer<Exception> onFailure) {
        if (collection == null || field == null || keyword == null) {
            onFailure.accept(new IllegalArgumentException("Collection, field, or keyword cannot be null"));
            return;
        }

        db.collection(collection)
                .whereEqualTo(field, keyword)
                .get()
                .addOnSuccessListener(querySnapshot -> onSuccess.accept(querySnapshot))
                .addOnFailureListener(e -> onFailure.accept(e));
    }
}