package com.example.doannt118.repository;

import android.util.Log;
import com.example.doannt118.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private static final String COLLECTION_DONTHUOC = "DonThuoc";
    private static final String COLLECTION_CHITIETDONTHUOC = "ChiTietDonThuoc";
    private static final String COLLECTION_DUOCPHAM = "DuocPham";
    private static final String COLLECTION_HOADON = "HoaDon";
    private static final String COLLECTION_CHITIETHOADON = "ChiTietHoaDon";

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

    // === LẤY DỮ LIỆU THEO FIELD (String) ===
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

    // === LẤY DỮ LIỆU THEO FIELD (Object - hỗ trợ Timestamp, Date, etc.) ===
    public void getByField(String collection, String field, Object value,
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
        } else if (data instanceof ThongBao) {
            updates = convertThongBaoToMap((ThongBao) data);
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

    // === CẬP NHẬT CÁC FIELD CỤ THỂ ===
    public void updateDocumentFields(String collection, String documentId, Map<String, Object> fields,
                                     Consumer<Void> onSuccess,
                                     Consumer<Exception> onFailure) {
        if (collection == null || documentId == null || fields == null || fields.isEmpty()) {
            onFailure.accept(new IllegalArgumentException("Collection, documentId, or fields cannot be null or empty"));
            return;
        }

        db.collection(collection)
                .document(documentId)
                .update(fields)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "updateDocumentFields success: collection=" + collection + ", documentId=" + documentId);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "updateDocumentFields failed: collection=" + collection + ", documentId=" + documentId, e);
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

    // === QUẢN LÝ ĐỐN THUỐC ===

    // Lấy danh sách đơn thuốc theo mã bệnh nhân
    public void getDonThuocByBenhNhan(String maBenhNhan,
                                      Consumer<QuerySnapshot> onSuccess,
                                      Consumer<Exception> onFailure) {
        // Lấy danh sách bệnh án của bệnh nhân
        db.collection(COLLECTION_BENHAN)
                .whereEqualTo("maBenhNhan", maBenhNhan)
                .get()
                .addOnSuccessListener(benhAnSnapshot -> {
                    if (benhAnSnapshot.isEmpty()) {
                        onSuccess.accept(benhAnSnapshot);
                        return;
                    }

                    // Lấy danh sách mã bệnh án
                    List<String> maBenhAnList = new ArrayList<>();
                    benhAnSnapshot.forEach(doc -> {
                        String maBenhAn = doc.getString("maBenhAn");
                        if (maBenhAn != null) {
                            maBenhAnList.add(maBenhAn);
                        }
                    });

                    // Lấy đơn thuốc theo danh sách mã bệnh án
                    if (!maBenhAnList.isEmpty()) {
                        db.collection(COLLECTION_DONTHUOC)
                                .whereIn("maBenhAn", maBenhAnList)
                                .get()
                                .addOnSuccessListener(donThuocSnapshot -> {
                                    Log.d("FirestoreRepository", "getDonThuocByBenhNhan success: " + donThuocSnapshot.size() + " records");
                                    onSuccess.accept(donThuocSnapshot);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("FirestoreRepository", "getDonThuocByBenhNhan failed", e);
                                    onFailure.accept(e);
                                });
                    } else {
                        onSuccess.accept(benhAnSnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getDonThuocByBenhNhan failed", e);
                    onFailure.accept(e);
                });
    }

    // Lấy chi tiết đơn thuốc
    public void getChiTietDonThuoc(String maDonThuoc,
                                   Consumer<QuerySnapshot> onSuccess,
                                   Consumer<Exception> onFailure) {
        db.collection(COLLECTION_CHITIETDONTHUOC)
                .whereEqualTo("maDonThuoc", maDonThuoc)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getChiTietDonThuoc success: " + querySnapshot.size() + " items");
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getChiTietDonThuoc failed", e);
                    onFailure.accept(e);
                });
    }

    // Thêm đơn thuốc mới
    public void addDonThuoc(String maDonThuoc, String maBenhAn, Date ngayLap,
                            Consumer<Void> onSuccess,
                            Consumer<Exception> onFailure) {
        Map<String, Object> donThuoc = new HashMap<>();
        donThuoc.put("maDonThuoc", maDonThuoc);
        donThuoc.put("maBenhAn", maBenhAn);
        donThuoc.put("ngayLap", ngayLap);

        db.collection(COLLECTION_DONTHUOC)
                .document(maDonThuoc)
                .set(donThuoc)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "addDonThuoc success: " + maDonThuoc);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "addDonThuoc failed", e);
                    onFailure.accept(e);
                });
    }

    // Thêm chi tiết đơn thuốc
    public void addChiTietDonThuoc(String maDonThuoc, String maDuocPham, int soLuong, String lieuDung,
                                   Consumer<Void> onSuccess,
                                   Consumer<Exception> onFailure) {
        Map<String, Object> chiTiet = new HashMap<>();
        chiTiet.put("maDonThuoc", maDonThuoc);
        chiTiet.put("maDuocPham", maDuocPham);
        chiTiet.put("soLuong", soLuong);
        chiTiet.put("lieuDung", lieuDung);

        db.collection(COLLECTION_CHITIETDONTHUOC)
                .add(chiTiet)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreRepository", "addChiTietDonThuoc success");
                    onSuccess.accept(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "addChiTietDonThuoc failed", e);
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
        map.put("avatarUrl", b.getAvatarUrl());
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
        map.put("avatarUrl", b.getAvatarUrl());
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
        map.put("soLuongToiDa", l.getSoLuongToiDa());
        map.put("loaiHinh", l.getLoaiHinh());
        return map;
    }

    private Map<String, Object> convertThongBaoToMap(ThongBao t) {
        Map<String, Object> map = new HashMap<>();
        map.put("maThongBao", t.getMaThongBao());
        map.put("maBenhNhan", t.getMaBenhNhan());
        map.put("maBacSi", t.getMaBacSi());
        map.put("tieuDe", t.getTieuDe());
        map.put("noiDung", t.getNoiDung());
        map.put("loaiThongBao", t.getLoaiThongBao());
        map.put("thoiGianGui", t.getThoiGianGui());
        map.put("daDoc", t.isDaDoc());
        return map;
    }

    // === QUẢN LÝ HÓA ĐƠN ===

    // Lấy danh sách hóa đơn theo mã bệnh nhân
    public void getHoaDonByBenhNhan(String maBenhNhan,
                                Consumer<QuerySnapshot> onSuccess,
                                Consumer<Exception> onFailure) {
    // Lấy danh sách bệnh án của bệnh nhân
    db.collection(COLLECTION_BENHAN)
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .get()
            .addOnSuccessListener(benhAnSnapshot -> {
                if (benhAnSnapshot.isEmpty()) {
                    onSuccess.accept(benhAnSnapshot);
                    return;
                }

                // Lấy danh sách mã bệnh án
                List<String> maBenhAnList = new ArrayList<>();
                benhAnSnapshot.forEach(doc -> {
                    String maBenhAn = doc.getString("maBenhAn");
                    if (maBenhAn != null) {
                        maBenhAnList.add(maBenhAn);
                    }
                });

                // Lấy hóa đơn theo danh sách mã bệnh án
                if (!maBenhAnList.isEmpty()) {
                    db.collection(COLLECTION_HOADON)
                            .whereIn("maBenhAn", maBenhAnList)
                            .get()
                            .addOnSuccessListener(hoaDonSnapshot -> {
                                Log.d("FirestoreRepository", "getHoaDonByBenhNhan success: " + hoaDonSnapshot.size() + " records");
                                onSuccess.accept(hoaDonSnapshot);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreRepository", "getHoaDonByBenhNhan failed", e);
                                onFailure.accept(e);
                            });
                } else {
                    onSuccess.accept(benhAnSnapshot);
                }
            })
            .addOnFailureListener(e -> {
                Log.e("FirestoreRepository", "getHoaDonByBenhNhan failed", e);
                onFailure.accept(e);
            });
}

    // Lấy chi tiết hóa đơn
    public void getChiTietHoaDon(String maHoaDon,
                             Consumer<QuerySnapshot> onSuccess,
                             Consumer<Exception> onFailure) {
    db.collection(COLLECTION_CHITIETHOADON)
            .whereEqualTo("maHoaDon", maHoaDon)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                Log.d("FirestoreRepository", "getChiTietHoaDon success: " + querySnapshot.size() + " items");
                onSuccess.accept(querySnapshot);
            })
            .addOnFailureListener(e -> {
                Log.e("FirestoreRepository", "getChiTietHoaDon failed", e);
                onFailure.accept(e);
            });
}

    // Thêm hóa đơn mới
    public void addHoaDon(String maHoaDon, String maBenhAn, Date ngayLap, double tongTien,
                      Consumer<Void> onSuccess,
                      Consumer<Exception> onFailure) {
    Map<String, Object> hoaDon = new HashMap<>();
    hoaDon.put("maHoaDon", maHoaDon);
    hoaDon.put("maBenhAn", maBenhAn);
    hoaDon.put("ngayLap", ngayLap);
    hoaDon.put("tongTien", tongTien);

    db.collection(COLLECTION_HOADON)
            .document(maHoaDon)
            .set(hoaDon)
            .addOnSuccessListener(aVoid -> {
                Log.d("FirestoreRepository", "addHoaDon success: " + maHoaDon);
                onSuccess.accept(aVoid);
            })
            .addOnFailureListener(e -> {
                Log.e("FirestoreRepository", "addHoaDon failed", e);
                onFailure.accept(e);
            });
}

    // Thêm chi tiết hóa đơn
    public void addChiTietHoaDon(String maHoaDon, String maDuocPham, int soLuong, double donGia,
                             Consumer<Void> onSuccess,
                             Consumer<Exception> onFailure) {
        Map<String, Object> chiTiet = new HashMap<>();
        chiTiet.put("maHoaDon", maHoaDon);
        chiTiet.put("maDuocPham", maDuocPham);
        chiTiet.put("soLuong", soLuong);
        chiTiet.put("donGia", donGia);

        db.collection(COLLECTION_CHITIETHOADON)
                .add(chiTiet)
                .addOnSuccessListener(documentReference -> {
                    Log.d("FirestoreRepository", "addChiTietHoaDon success");
                    onSuccess.accept(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "addChiTietHoaDon failed", e);
                    onFailure.accept(e);
                });
    }

    // === QUẢN LÝ LỊCH UỐNG THUỐC ===

    // Lấy lịch uống thuốc theo bệnh nhân và ngày
    public void getLichUongThuocByBenhNhanAndDate(String maBenhNhan, Date ngayUong,
                                                  Consumer<QuerySnapshot> onSuccess,
                                                  Consumer<Exception> onFailure) {
        db.collection("LichUongThuoc")
                .whereEqualTo("maBenhNhan", maBenhNhan)
                .whereEqualTo("ngayUong", ngayUong)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getLichUongThuocByBenhNhanAndDate success: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getLichUongThuocByBenhNhanAndDate failed", e);
                    onFailure.accept(e);
                });
    }

    // Lấy lịch uống thuốc chờ xác nhận
    public void getLichUongThuocChoXacNhan(String maBenhNhan,
                                          Consumer<QuerySnapshot> onSuccess,
                                          Consumer<Exception> onFailure) {
        db.collection("LichUongThuoc")
                .whereEqualTo("maBenhNhan", maBenhNhan)
                .whereEqualTo("trangThai", "CHO_XAC_NHAN")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getLichUongThuocChoXacNhan success: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getLichUongThuocChoXacNhan failed", e);
                    onFailure.accept(e);
                });
    }

    // Lấy thống kê xác nhận uống thuốc
    public void getThongKeXacNhanUongThuoc(String maBenhNhan, String maDonThuoc,
                                          Consumer<QuerySnapshot> onSuccess,
                                          Consumer<Exception> onFailure) {
        db.collection("XacNhanUongThuoc")
                .whereEqualTo("maBenhNhan", maBenhNhan)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getThongKeXacNhanUongThuoc success: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getThongKeXacNhanUongThuoc failed", e);
                    onFailure.accept(e);
                });
    }

    // Cập nhật trạng thái lịch uống thuốc
    public void capNhatTrangThaiLichUong(String maLichUong, String trangThai,
                                        Consumer<Void> onSuccess,
                                        Consumer<Exception> onFailure) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("trangThai", trangThai);
        updates.put("thoiGianXacNhan", com.google.firebase.Timestamp.now());

        db.collection("LichUongThuoc")
                .document(maLichUong)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "capNhatTrangThaiLichUong success: " + maLichUong);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "capNhatTrangThaiLichUong failed", e);
                    onFailure.accept(e);
                });
    }

    // Lấy chi tiết đơn thuốc theo ca uống
    public void getChiTietDonThuocTheoCa(String maDonThuoc, String caUong,
                                        Consumer<List<Map<String, Object>>> onSuccess,
                                        Consumer<Exception> onFailure) {
        db.collection(COLLECTION_CHITIETDONTHUOC)
                .whereEqualTo("maDonThuoc", maDonThuoc)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Map<String, Object>> result = new ArrayList<>();
                    for (var doc : querySnapshot.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        if (data != null) {
                            boolean canUong = false;
                            if ("SANG".equals(caUong) && Boolean.TRUE.equals(data.get("uongSang"))) {
                                canUong = true;
                            } else if ("TRUA".equals(caUong) && Boolean.TRUE.equals(data.get("uongTrua"))) {
                                canUong = true;
                            } else if ("TOI".equals(caUong) && Boolean.TRUE.equals(data.get("uongToi"))) {
                                canUong = true;
                            }
                            
                            if (canUong) {
                                result.add(data);
                            }
                        }
                    }
                    Log.d("FirestoreRepository", "getChiTietDonThuocTheoCa success: " + result.size());
                    onSuccess.accept(result);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getChiTietDonThuocTheoCa failed", e);
                    onFailure.accept(e);
                });
    }

    // Tính tỉ lệ tuân thủ uống thuốc
    public void tinhTiLeTuanThu(String maBenhNhan, String maDonThuoc,
                               Consumer<Map<String, Integer>> onSuccess,
                               Consumer<Exception> onFailure) {
        db.collection("LichUongThuoc")
                .whereEqualTo("maBenhNhan", maBenhNhan)
                .whereEqualTo("maDonThuoc", maDonThuoc)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int daUong = 0;
                    int boQua = 0;
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String trangThai = doc.getString("trangThai");
                        if ("DA_UONG".equals(trangThai)) {
                            daUong++;
                        } else if ("BO_QUA".equals(trangThai)) {
                            boQua++;
                        }
                    }
                    
                    Map<String, Integer> result = new HashMap<>();
                    result.put("total", total);
                    result.put("daUong", daUong);
                    result.put("boQua", boQua);
                    result.put("tiLe", total > 0 ? (daUong * 100 / total) : 0);
                    
                    Log.d("FirestoreRepository", "tinhTiLeTuanThu success: " + result);
                    onSuccess.accept(result);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "tinhTiLeTuanThu failed", e);
                    onFailure.accept(e);
                });
    }

    // === QUẢN LÝ BỆNH ÁN (BÁC SĨ) ===

    // Lấy danh sách bệnh án theo bác sĩ
    public void getBenhAnByBacSi(String maBacSi,
                                Consumer<QuerySnapshot> onSuccess,
                                Consumer<Exception> onFailure) {
        db.collection(COLLECTION_BENHAN)
                .whereEqualTo("maBacSi", maBacSi)
                .orderBy("ngayKham", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getBenhAnByBacSi success: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getBenhAnByBacSi failed", e);
                    onFailure.accept(e);
                });
    }

    // Tạo bệnh án mới
    public void taoBenhAn(String maBenhAn, String maLichKham, String maBenhNhan, 
                         String maBacSi, String chanDoan, String ghiChu,
                         Consumer<Void> onSuccess,
                         Consumer<Exception> onFailure) {
        Map<String, Object> benhAn = new HashMap<>();
        benhAn.put("maBenhAn", maBenhAn);
        benhAn.put("maLichKham", maLichKham);
        benhAn.put("maBenhNhan", maBenhNhan);
        benhAn.put("maBacSi", maBacSi);
        benhAn.put("chanDoan", chanDoan);
        benhAn.put("ghiChu", ghiChu);
        benhAn.put("ngayKham", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_BENHAN)
                .document(maBenhAn)
                .set(benhAn)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "taoBenhAn success: " + maBenhAn);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "taoBenhAn failed", e);
                    onFailure.accept(e);
                });
    }

    // Cập nhật chẩn đoán bệnh án
    public void capNhatChanDoan(String maBenhAn, String chanDoan, String ghiChu,
                               Consumer<Void> onSuccess,
                               Consumer<Exception> onFailure) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("chanDoan", chanDoan);
        updates.put("ghiChu", ghiChu);
        updates.put("ngayKham", com.google.firebase.Timestamp.now());

        db.collection(COLLECTION_BENHAN)
                .document(maBenhAn)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "capNhatChanDoan success: " + maBenhAn);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "capNhatChanDoan failed", e);
                    onFailure.accept(e);
                });
    }

    // Lấy bệnh án theo lịch khám
    public void getBenhAnByLichKham(String maLichKham,
                                   Consumer<QuerySnapshot> onSuccess,
                                   Consumer<Exception> onFailure) {
        db.collection(COLLECTION_BENHAN)
                .whereEqualTo("maLichKham", maLichKham)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getBenhAnByLichKham success");
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getBenhAnByLichKham failed", e);
                    onFailure.accept(e);
                });
    }

    // Thống kê bệnh án theo bác sĩ
    public void thongKeBenhAnBacSi(String maBacSi,
                                  Consumer<Map<String, Integer>> onSuccess,
                                  Consumer<Exception> onFailure) {
        db.collection(COLLECTION_BENHAN)
                .whereEqualTo("maBacSi", maBacSi)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int daKham = 0;
                    int choKham = 0;
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String chanDoan = doc.getString("chanDoan");
                        if (chanDoan != null && !chanDoan.isEmpty()) {
                            daKham++;
                        } else {
                            choKham++;
                        }
                    }
                    
                    Map<String, Integer> result = new HashMap<>();
                    result.put("total", total);
                    result.put("daKham", daKham);
                    result.put("choKham", choKham);
                    
                    Log.d("FirestoreRepository", "thongKeBenhAnBacSi success: " + result);
                    onSuccess.accept(result);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "thongKeBenhAnBacSi failed", e);
                    onFailure.accept(e);
                });
    }

    // === QUẢN LÝ ĐƠN THUỐC (BÁC SĨ) ===

    // Tạo đơn thuốc với chi tiết
    public void taoDonThuocVoiChiTiet(String maDonThuoc, String maBenhAn, String maBenhNhan,
                                     Date ngayLap, int soNgayUong, List<ChiTietDonThuoc> danhSachThuoc,
                                     Consumer<Void> onSuccess,
                                     Consumer<Exception> onFailure) {
        WriteBatch batch = db.batch();
        
        // Tạo đơn thuốc
        Map<String, Object> donThuoc = new HashMap<>();
        donThuoc.put("maDonThuoc", maDonThuoc);
        donThuoc.put("maBenhAn", maBenhAn);
        donThuoc.put("maBenhNhan", maBenhNhan);
        donThuoc.put("ngayLap", ngayLap);
        donThuoc.put("soNgayUong", soNgayUong);
        donThuoc.put("ngayBatDau", ngayLap);
        
        DocumentReference donThuocRef = db.collection(COLLECTION_DONTHUOC).document(maDonThuoc);
        batch.set(donThuocRef, donThuoc);
        
        // Thêm chi tiết đơn thuốc
        for (ChiTietDonThuoc chiTiet : danhSachThuoc) {
            chiTiet.setMaDonThuoc(maDonThuoc);
            DocumentReference chiTietRef = db.collection(COLLECTION_CHITIETDONTHUOC)
                .document(chiTiet.getMaChiTiet());
            batch.set(chiTietRef, chiTiet);
        }
        
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "taoDonThuocVoiChiTiet success: " + maDonThuoc);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "taoDonThuocVoiChiTiet failed", e);
                    onFailure.accept(e);
                });
    }

    // Cập nhật đơn thuốc
    public void capNhatDonThuoc(String maDonThuoc, int soNgayUong,
                               Consumer<Void> onSuccess,
                               Consumer<Exception> onFailure) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("soNgayUong", soNgayUong);
        
        db.collection(COLLECTION_DONTHUOC)
                .document(maDonThuoc)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "capNhatDonThuoc success: " + maDonThuoc);
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "capNhatDonThuoc failed", e);
                    onFailure.accept(e);
                });
    }

    // Xóa đơn thuốc (và chi tiết)
    public void xoaDonThuoc(String maDonThuoc,
                           Consumer<Void> onSuccess,
                           Consumer<Exception> onFailure) {
        // Xóa chi tiết trước
        db.collection(COLLECTION_CHITIETDONTHUOC)
                .whereEqualTo("maDonThuoc", maDonThuoc)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();
                    
                    // Xóa tất cả chi tiết
                    for (var doc : querySnapshot.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    
                    // Xóa đơn thuốc
                    batch.delete(db.collection(COLLECTION_DONTHUOC).document(maDonThuoc));
                    
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                Log.d("FirestoreRepository", "xoaDonThuoc success: " + maDonThuoc);
                                onSuccess.accept(aVoid);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("FirestoreRepository", "xoaDonThuoc failed", e);
                                onFailure.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "xoaDonThuoc query failed", e);
                    onFailure.accept(e);
                });
    }

    // Lấy đơn thuốc theo bệnh án
    public void getDonThuocByBenhAn(String maBenhAn,
                                   Consumer<QuerySnapshot> onSuccess,
                                   Consumer<Exception> onFailure) {
        db.collection(COLLECTION_DONTHUOC)
                .whereEqualTo("maBenhAn", maBenhAn)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Log.d("FirestoreRepository", "getDonThuocByBenhAn success: " + querySnapshot.size());
                    onSuccess.accept(querySnapshot);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "getDonThuocByBenhAn failed", e);
                    onFailure.accept(e);
                });
    }

    // Thêm chi tiết đơn thuốc
    public void themChiTietDonThuoc(ChiTietDonThuoc chiTiet,
                                   Consumer<Void> onSuccess,
                                   Consumer<Exception> onFailure) {
        db.collection(COLLECTION_CHITIETDONTHUOC)
                .document(chiTiet.getMaChiTiet())
                .set(chiTiet)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "themChiTietDonThuoc success");
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "themChiTietDonThuoc failed", e);
                    onFailure.accept(e);
                });
    }

    // Xóa chi tiết đơn thuốc
    public void xoaChiTietDonThuoc(String maChiTiet,
                                   Consumer<Void> onSuccess,
                                   Consumer<Exception> onFailure) {
        db.collection(COLLECTION_CHITIETDONTHUOC)
                .document(maChiTiet)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirestoreRepository", "xoaChiTietDonThuoc success");
                    onSuccess.accept(aVoid);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreRepository", "xoaChiTietDonThuoc failed", e);
                    onFailure.accept(e);
                });
    }
}
