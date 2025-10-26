package com.example.doannt118.repository;

import android.util.Log;

import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.model.TaiKhoan;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";
    private static final String COLLECTION_BENHNHAN = "BenhNhan";
    private static final String COLLECTION_BACSI = "BacSi";
    private static final String COLLECTION_BENHAN = "BenhAn";
    private static final String COLLECTION_LICHSU = "LichSuHoatDong";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // === GET COLLECTION ===
    public CollectionReference getCollection(String collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        return db.collection(collection);
    }

    // === LẤY DỮ LIỆU THEO FIELD ===
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
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === LẤY TOÀN BỘ DỮ LIỆU ===
    public void getAll(String collection,
                       Consumer<QuerySnapshot> onSuccess,
                       Consumer<Exception> onFailure) {
        if (collection == null) {
            onFailure.accept(new IllegalArgumentException("Collection cannot be null"));
            return;
        }

        db.collection(collection)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === ĐĂNG KÝ NGƯỜI DÙNG MỚI THEO BATCH ===
    public void registerNewUserBatch(TaiKhoan taiKhoan, Object userProfile,
                                     Consumer<Void> onSuccess,
                                     Consumer<Exception> onFailure) {
        if (taiKhoan == null || userProfile == null) {
            onFailure.accept(new IllegalArgumentException("TaiKhoan or userProfile cannot be null"));
            return;
        }

        String profileCollectionName;
        String profileDocumentId;

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
            onFailure.accept(new IllegalArgumentException("Profile ID cannot be null or empty"));
            return;
        }

        String taiKhoanId = taiKhoan.getMaTaiKhoan();
        if (taiKhoanId == null || taiKhoanId.isEmpty()) {
            onFailure.accept(new IllegalArgumentException("TaiKhoan ID cannot be null or empty"));
            return;
        }

        DocumentReference taiKhoanRef = db.collection(COLLECTION_TAIKHOAN).document(taiKhoanId);
        DocumentReference profileRef = db.collection(profileCollectionName).document(profileDocumentId);

        WriteBatch batch = db.batch();
        batch.set(taiKhoanRef, taiKhoan);
        batch.set(profileRef, userProfile);

        batch.commit()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === GHI LOG HOẠT ĐỘNG ===
    public void logActivity(LichSuHoatDong lichSu) {
        db.collection(COLLECTION_LICHSU)
                .document(lichSu.getMaLichSu())
                .set(lichSu)
                .addOnFailureListener(e -> Log.e("FirestoreRepository", "Lỗi ghi log: ", e));
    }

    // === CẬP NHẬT MẬT KHẨU (trong collection TaiKhoan) ===
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

    // === GỬI EMAIL RESET PASSWORD ===
    public Task<Void> sendPasswordResetEmail(String email) {
        return auth.sendPasswordResetEmail(email);
    }

    // === THÊM MỚI DOCUMENT ===
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

    // === CẬP NHẬT DOCUMENT ===
    public void updateDocument(String collection, String documentId, Object data,
                               Consumer<Void> onSuccess,
                               Consumer<Exception> onFailure) {
        if (collection == null || documentId == null || data == null) {
            onFailure.accept(new IllegalArgumentException("Collection, documentId, or data cannot be null"));
            return;
        }

        Map<String, Object> updates;

        if (data instanceof Map) {
            updates = (Map<String, Object>) data;
        } else if (data instanceof BenhNhan) {
            updates = convertBenhNhanToMap((BenhNhan) data);
        } else if (data instanceof BacSi) {
            updates = convertBacSiToMap((BacSi) data);
        } else if (data instanceof BenhAn) {
            updates = convertBenhAnToMap((BenhAn) data);
        } else if (data instanceof LichLamViec) {
            updates = convertLichLamViecToMap((LichLamViec) data);
        } else {
            onFailure.accept(new IllegalArgumentException("Unsupported data type: " + data.getClass().getName()));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === XOÁ DOCUMENT ===
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

    // === TÌM KIẾM DOCUMENT ===
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
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === TRUY VẤN CÓ ĐIỀU KIỆN THEO NGÀY (CHUỖI) ===
    public void getByFieldWithDate(String collection, String field, String value,
                                   String dateField, String dateValue,
                                   Consumer<QuerySnapshot> onSuccess,
                                   Consumer<Exception> onFailure) {
        if (collection == null || field == null || value == null || dateField == null || dateValue == null) {
            onFailure.accept(new IllegalArgumentException("Invalid arguments for date filter"));
            return;
        }

        db.collection(collection)
                .whereEqualTo(field, value)
                .whereEqualTo(dateField, dateValue)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === TRUY VẤN THEO KHOẢNG NGÀY (KIỂU DATE) ===
    public void getByFieldAndDateRange(String collection, String field, String value,
                                       String dateField, Date startDate, Date endDate,
                                       Consumer<QuerySnapshot> onSuccess,
                                       Consumer<Exception> onFailure) {
        if (collection == null || field == null || value == null || dateField == null || startDate == null || endDate == null) {
            onFailure.accept(new IllegalArgumentException("Invalid arguments for date range query"));
            return;
        }

        db.collection(collection)
                .whereEqualTo(field, value)
                .whereGreaterThanOrEqualTo(dateField, startDate)
                .whereLessThanOrEqualTo(dateField, endDate)
                .get()
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    // === ĐẾM DOCUMENT THEO FIELD ===
    public void countByField(String collection, String field, String value,
                             Consumer<Long> onSuccess,
                             Consumer<Exception> onFailure) {
        db.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(query -> onSuccess.accept((long) query.size()))
                .addOnFailureListener(onFailure::accept);
    }

    // === CONVERT OBJECT SANG MAP ===
    private Map<String, Object> convertBenhNhanToMap(BenhNhan b) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBenhNhan", b.getMaBenhNhan());
        map.put("maTaiKhoan", b.getMaTaiKhoan());
        map.put("hoTen", b.getHoTen());
        map.put("soDienThoai", b.getSoDienThoai());
        map.put("diaChi", b.getDiaChi());
        return map;
    }

    private Map<String, Object> convertBacSiToMap(BacSi b) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBacSi", b.getMaBacSi());
        map.put("maTaiKhoan", b.getMaTaiKhoan());
        map.put("hoTen", b.getHoTen());
        return map;
    }

    private Map<String, Object> convertBenhAnToMap(BenhAn a) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBenhAn", a.getMaBenhAn());
        map.put("maLichKham", a.getMaLichKham());
        map.put("maBenhNhan", a.getMaBenhNhan());
        map.put("maBacSi", a.getMaBacSi());
        map.put("chanDoan", a.getChanDoan());
        map.put("ghiChu", a.getGhiChu());
        map.put("ngayKham", a.getNgayKham());
        return map;
    }

    private Map<String, Object> convertLichLamViecToMap(LichLamViec l) {
        Map<String, Object> map = new HashMap<>();
        map.put("maLichLamViec", l.getMaLichLamViec());
        map.put("maBacSi", l.getMaBacSi());
        map.put("ngayLamViec", l.getNgayLamViec());
        map.put("caLamViec", l.getCaLamViec());
        map.put("trangThai", l.getTrangThai());
        return map;
    }
}
