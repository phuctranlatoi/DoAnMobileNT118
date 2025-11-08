package com.example.doannt118.repository;

import android.util.Log;
import com.example.doannt118.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class FirestoreRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private static final String COLLECTION_TAIKHOAN = "TaiKhoan";
    private static final String COLLECTION_BENHNHAN = "BenhNhan";
    private static final String COLLECTION_BACSI = "BacSi";
    private static final String COLLECTION_ADMIN = "Admin";
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
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getByField success: " + collection + ", field: " + field + ", value: " + value + ", results: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getByField failed: " + collection + ", field: " + field + ", value: " + value, e);
                    onFailure.accept(e);
                });
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
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getAll success: " + collection + ", results: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getAll failed: " + collection, e);
                    onFailure.accept(e);
                });
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

        // Xác định loại hồ sơ và trạng thái
        if (userProfile instanceof BenhNhan) {
            profileCollectionName = COLLECTION_BENHNHAN;
            profileDocumentId = ((BenhNhan) userProfile).getMaBenhNhan();
            taiKhoan.setTrangThai("Hoạt động");
        } else if (userProfile instanceof BacSi) {
            profileCollectionName = COLLECTION_BACSI;
            profileDocumentId = ((BacSi) userProfile).getMaBacSi();
            taiKhoan.setTrangThai("Chờ duyệt");
            ((BacSi) userProfile).setTrangThaiXacThuc("Chờ xác thực");
        } else if (userProfile instanceof Admin) {
            profileCollectionName = COLLECTION_ADMIN;
            profileDocumentId = ((Admin) userProfile).getMaAdmin();
            taiKhoan.setTrangThai("Chờ duyệt");
        } else {
            onFailure.accept(new IllegalArgumentException("userProfile must be BenhNhan, BacSi, or Admin"));
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

        // Log thông tin trước khi commit
        Log.d("FirestoreRepository", "registerNewUserBatch: taiKhoanId=" + taiKhoanId + ", profileCollection=" + profileCollectionName + ", profileId=" + profileDocumentId);

        DocumentReference taiKhoanRef = db.collection(COLLECTION_TAIKHOAN).document(taiKhoanId);
        DocumentReference profileRef = db.collection(profileCollectionName).document(profileDocumentId);

        WriteBatch batch = db.batch();
        batch.set(taiKhoanRef, convertTaiKhoanToMap(taiKhoan));
        batch.set(profileRef, userProfile);

        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "registerNewUserBatch success: taiKhoanId=" + taiKhoanId);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "registerNewUserBatch failed: taiKhoanId=" + taiKhoanId, e);
                    onFailure.accept(e);
                });
    }

    // === GHI LOG HOẠT ĐỘNG ===
    public void logActivity(LichSuHoatDong lichSu) {
        if (lichSu == null || lichSu.getMaLichSu() == null) {
            Log.e("FirestoreRepository", "logActivity failed: lichSu or maLichSu is null");
            return;
        }

        db.collection(COLLECTION_LICHSU)
                .document(lichSu.getMaLichSu())
                .set(lichSu)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreRepository", "logActivity success: maLichSu=" + lichSu.getMaLichSu()))
                .addOnFailureListener(e -> Log.e("FirestoreRepository", "logActivity failed: maLichSu=" + lichSu.getMaLichSu(), e));
    }

    // === CẬP NHẬT MẬT KHẨU (trong collection TaiKhoan) ===
    public void updatePassword(String email, String newPassword,
                               Consumer<Void> onSuccess, Consumer<Exception> onFailure) {
        if (email == null || newPassword == null) {
            onFailure.accept(new IllegalArgumentException("Email or newPassword cannot be null"));
            return;
        }

        db.collection(COLLECTION_TAIKHOAN)
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.e("FirestoreRepository", "updatePassword failed: Email not found: " + email);
                        onFailure.accept(new Exception("Email không tồn tại"));
                        return;
                    }
                    DocumentReference taiKhoanRef = querySnapshot.getDocuments().get(0).getReference();
                    taiKhoanRef.update("matKhau", newPassword)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirestoreRepository", "updatePassword success: email=" + email);
                                onSuccess.accept(aVoid);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreRepository", "updatePassword failed: email=" + email, e);
                                onFailure.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "updatePassword query failed: email=" + email, e);
                    onFailure.accept(e);
                });
    }

    // === GỬI EMAIL RESET PASSWORD ===
    public void sendPasswordResetEmail(String email,
                                       Consumer<Void> onSuccess, Consumer<Exception> onFailure) {
        if (email == null) {
            onFailure.accept(new IllegalArgumentException("Email cannot be null"));
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "sendPasswordResetEmail success: email=" + email);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "sendPasswordResetEmail failed: email=" + email, e);
                    onFailure.accept(e);
                });
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
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "addDocument success: collection=" + collection + ", documentId=" + documentId);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "addDocument failed: collection=" + collection + ", documentId=" + documentId, e);
                    onFailure.accept(e);
                });
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
        } else if (data instanceof Admin) {
            updates = convertAdminToMap((Admin) data);
        } else {
            onFailure.accept(new IllegalArgumentException("Unsupported data type: " + data.getClass().getName()));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "updateDocument success: collection=" + collection + ", documentId=" + documentId);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "updateDocument failed: collection=" + collection + ", documentId=" + documentId, e);
                    onFailure.accept(e);
                });
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
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "deleteDocument success: collection=" + collection + ", documentId=" + documentId);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "deleteDocument failed: collection=" + collection + ", documentId=" + documentId, e);
                    onFailure.accept(e);
                });
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
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "searchDocuments success: collection=" + collection + ", field=" + field + ", keyword=" + keyword);
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "searchDocuments failed: collection=" + collection + ", field=" + field + ", keyword=" + keyword, e);
                    onFailure.accept(e);
                });
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
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getByFieldWithDate success: collection=" + collection + ", field=" + field + ", value=" + value);
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getByFieldWithDate failed: collection=" + collection + ", field=" + field + ", value=" + value, e);
                    onFailure.accept(e);
                });
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
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getByFieldAndDateRange success: collection=" + collection + ", field=" + field + ", value=" + value);
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getByFieldAndDateRange failed: collection=" + collection + ", field=" + field + ", value=" + value, e);
                    onFailure.accept(e);
                });
    }

    // === ĐẾM DOCUMENT THEO FIELD ===
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
                    Log.d("FirestoreRepository", "countByField success: collection=" + collection + ", field=" + field + ", value=" + value + ", count=" + querySnapshot.size());
                    onSuccess.accept((long) querySnapshot.size());
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "countByField failed: collection=" + collection + ", field=" + field + ", value=" + value, e);
                    onFailure.accept(e);
                });
    }

    // === DUYỆT BÁC SĨ (XÁC THỰC CHỨNG CHỈ) ===
    public void approveBacSi(String maBacSi, String trangThaiXacThuc, String trangThaiTaiKhoan, String maTaiKhoan,
                             Consumer<Void> onSuccess, Consumer<Exception> onFailure) {
        WriteBatch batch = db.batch();
        batch.update(db.collection(COLLECTION_BACSI).document(maBacSi), "trangThaiXacThuc", trangThaiXacThuc);
        batch.update(db.collection(COLLECTION_TAIKHOAN).document(maTaiKhoan), "trangThai", trangThaiTaiKhoan);
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "approveBacSi success: maBacSi=" + maBacSi + ", maTaiKhoan=" + maTaiKhoan);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "approveBacSi failed: maBacSi=" + maBacSi + ", maTaiKhoan=" + maTaiKhoan, e);
                    onFailure.accept(e);
                });
    }
    private Map<String, Object> convertTaiKhoanToMap(TaiKhoan taiKhoan) {
        Map<String, Object> map = new HashMap<>();
        map.put("maTaiKhoan", taiKhoan.getMaTaiKhoan());
        map.put("tenDangNhap", taiKhoan.getTenDangNhap());
        map.put("matKhau", taiKhoan.getMatKhau());
        map.put("vaiTro", taiKhoan.getVaiTro());
        map.put("email", taiKhoan.getEmail());
        map.put("trangThai", taiKhoan.getTrangThai());
        return map;
    }

    private Map<String, Object> convertBenhNhanToMap(BenhNhan b) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBenhNhan", b.getMaBenhNhan());
        map.put("maTaiKhoan", b.getMaTaiKhoan());
        map.put("hoTen", b.getHoTen());
        map.put("soDienThoai", b.getSoDienThoai());
        map.put("diaChi", b.getDiaChi());
        map.put("ngaySinh", b.getNgaySinh());
        return map;
    }

    private Map<String, Object> convertBacSiToMap(BacSi b) {
        Map<String, Object> map = new HashMap<>();
        map.put("maBacSi", b.getMaBacSi());
        map.put("maTaiKhoan", b.getMaTaiKhoan());
        map.put("hoTen", b.getHoTen());
        map.put("soDienThoai", b.getSoDienThoai());
        map.put("bangCap", b.getBangCap());
        map.put("hocVi", b.getHocVi());
        map.put("chungChiHanhNghe", b.getChungChiHanhNghe());
        map.put("trangThaiXacThuc", b.getTrangThaiXacThuc());
        return map;
    }

    private Map<String, Object> convertAdminToMap(Admin admin) {
        Map<String, Object> map = new HashMap<>();
        map.put("maAdmin", admin.getMaAdmin());
        map.put("maTaiKhoan", admin.getMaTaiKhoan());
        map.put("hoTen", admin.getHoTen());
        map.put("soDienThoai", admin.getSoDienThoai());
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