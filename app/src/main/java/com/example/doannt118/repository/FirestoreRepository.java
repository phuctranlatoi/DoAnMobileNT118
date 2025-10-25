package com.example.doannt118.repository;

import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.model.TaiKhoan;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.Date; // <-- Import này cần thiết
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class FirestoreRepository {
    private final FirebaseFirestore db;

    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";
    private static final String COLLECTION_BENHNHAN = "BenhNhan";
    private static final String COLLECTION_BACSI = "BacSi";
    private static final String COLLECTION_BENHAN = "BenhAn";

    public FirestoreRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // --- Phương thức để lấy CollectionReference (dùng cho query thủ công nếu cần) ---
    public CollectionReference getCollection(String collection) {
        if (collection == null) {
            throw new IllegalArgumentException("Collection cannot be null");
        }
        return db.collection(collection);
    }

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

    public void getAll(String collection,
                       Consumer<QuerySnapshot> onSuccess,
                       Consumer<Exception> onFailure) {
        if (collection == null) {
            onFailure.accept(new IllegalArgumentException("Collection cannot be null"));
            return;
        }

        db.collection(collection)
                .get()
                .addOnSuccessListener(querySnapshot -> onSuccess.accept(querySnapshot))
                .addOnFailureListener(e -> onFailure.accept(e));
    }

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
            onFailure.accept(new IllegalArgumentException("Profile document ID cannot be null or empty"));
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

    public void addDocument(String collection, String documentId, Object data,
                            Consumer<Void> onSuccess,
                            Consumer<Exception> onFailure) {
        if (collection == null || documentId == null || data == null) {
            onFailure.accept(new IllegalArgumentException("Collection, documentId, or data cannot be null"));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .set(data) // Firestore tự chuyển đổi POJO thành Map
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

    public void updateDocument(String collection, String documentId, Object data,
                               Consumer<Void> onSuccess,
                               Consumer<Exception> onFailure) {
        if (collection == null || documentId == null || data == null) {
            onFailure.accept(new IllegalArgumentException("Collection, documentId, or data cannot be null"));
            return;
        }

        // Nếu data là Map, cập nhật trực tiếp
        if (data instanceof Map) {
            db.collection(collection)
                    .document(documentId)
                    .update((Map<String, Object>) data)
                    .addOnSuccessListener(onSuccess::accept)
                    .addOnFailureListener(onFailure::accept);
            return;
        }

        // Nếu không phải Map, cố gắng convert sang Map (nên dùng toMap() trong model)
        Map<String, Object> updates;
        if (data instanceof BenhNhan) {
            // Nên có hàm toMap() trong BenhNhan
            updates = convertBenhNhanToMap((BenhNhan) data);
        } else if (data instanceof BacSi) {
            // Nên có hàm toMap() trong BacSi
            updates = convertBacSiToMap((BacSi) data);
        } else if (data instanceof BenhAn) {
            // Nên có hàm toMap() trong BenhAn
            updates = convertBenhAnToMap((BenhAn) data);
        } else if (data instanceof LichLamViec) {
            // Nên có hàm toMap() trong LichLamViec
            updates = convertLichLamViecToMap((LichLamViec) data);
        }
        else {
            onFailure.accept(new IllegalArgumentException("Data type not supported for automatic conversion to Map: " + data.getClass().getName()));
            return;
        }

        if (updates == null || updates.isEmpty()) {
            onFailure.accept(new IllegalArgumentException("Failed to convert object to Map for update"));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(onSuccess::accept)
                .addOnFailureListener(onFailure::accept);
    }

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

    // Phương thức lọc theo ngày dạng String (yyyy-MM-dd)
    public void getByFieldWithDate(String collection, String field, String value, String dateField, String dateValue,
                                   Consumer<QuerySnapshot> onSuccess,
                                   Consumer<Exception> onFailure) {
        if (collection == null || field == null || value == null || dateField == null || dateValue == null) {
            onFailure.accept(new IllegalArgumentException("Collection, field, value, dateField, or dateValue cannot be null"));
            return;
        }

        // Lưu ý: Cách này chỉ hoạt động nếu bạn lưu ngày dạng String "yyyy-MM-dd" trong Firestore
        db.collection(collection)
                .whereEqualTo(field, value)
                .whereEqualTo(dateField, dateValue)
                .get()
                .addOnSuccessListener(querySnapshot -> onSuccess.accept(querySnapshot))
                .addOnFailureListener(e -> onFailure.accept(e));
    }

    // === HÀM MỚI ĐỂ LỌC THEO NGÀY (KIỂU DATE) ===
    public void getByFieldAndDateRange(String collection, String field, String value, String dateField, Date startDate, Date endDate,
                                       Consumer<QuerySnapshot> onSuccess,
                                       Consumer<Exception> onFailure) {
        if (collection == null || field == null || value == null || dateField == null || startDate == null || endDate == null) {
            onFailure.accept(new IllegalArgumentException("Arguments cannot be null for date range query"));
            return;
        }

        db.collection(collection)
                .whereEqualTo(field, value)
                .whereGreaterThanOrEqualTo(dateField, startDate) // Lớn hơn hoặc bằng ngày bắt đầu (00:00:00)
                .whereLessThanOrEqualTo(dateField, endDate)     // Nhỏ hơn hoặc bằng ngày kết thúc (23:59:59)
                // Bạn có thể thêm orderBy ở đây nếu muốn Firestore tự sắp xếp
                // .orderBy(dateField, Query.Direction.ASCENDING)
                // .orderBy("caLamViec", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> onSuccess.accept(querySnapshot))
                .addOnFailureListener(e -> onFailure.accept(e));
    }
    // === KẾT THÚC HÀM MỚI ===

    public void countByField(String collection, String field, String value,
                             Consumer<Long> onSuccess,
                             Consumer<Exception> onFailure) {
        if (collection == null || field == null || value == null) {
            onFailure.accept(new IllegalArgumentException("Collection, field, or value cannot be null"));
            return;
        }

        db.collection(collection)
                .whereEqualTo(field, value)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long count = querySnapshot.size();
                    onSuccess.accept(count);
                })
                .addOnFailureListener(e -> onFailure.accept(e));
    }

    // --- Các hàm helper để convert object sang Map (Nên đặt trong Model) ---
    private Map<String, Object> convertBenhNhanToMap(BenhNhan benhNhan) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBenhNhan", benhNhan.getMaBenhNhan());
        map.put("maTaiKhoan", benhNhan.getMaTaiKhoan());
        map.put("hoTen", benhNhan.getHoTen());
        map.put("soDienThoai", benhNhan.getSoDienThoai());
        map.put("diaChi", benhNhan.getDiaChi());
        // Thêm các trường khác nếu có
        return map;
    }

    private Map<String, Object> convertBacSiToMap(BacSi bacSi) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBacSi", bacSi.getMaBacSi());
        map.put("maTaiKhoan", bacSi.getMaTaiKhoan());
        map.put("hoTen", bacSi.getHoTen());
        // Thêm các trường khác nếu có
        return map;
    }

    private Map<String, Object> convertBenhAnToMap(BenhAn benhAn) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBenhAn", benhAn.getMaBenhAn());
        map.put("maLichKham", benhAn.getMaLichKham());
        map.put("maBenhNhan", benhAn.getMaBenhNhan());
        map.put("maBacSi", benhAn.getMaBacSi());
        map.put("chanDoan", benhAn.getChanDoan());
        map.put("ghiChu", benhAn.getGhiChu());
        map.put("ngayKham", benhAn.getNgayKham());
        // Thêm các trường khác nếu có
        return map;
    }

    private Map<String, Object> convertLichLamViecToMap(LichLamViec lich) {
        Map<String, Object> map = new HashMap<>();
        map.put("maLichLamViec", lich.getMaLichLamViec());
        map.put("maBacSi", lich.getMaBacSi());
        map.put("ngayLamViec", lich.getNgayLamViec()); // Lưu kiểu Timestamp/Date
        map.put("caLamViec", lich.getCaLamViec());
        map.put("trangThai", lich.getTrangThai());
        // Thêm các trường khác nếu có
        return map;
    }
}