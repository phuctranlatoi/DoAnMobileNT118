package com.example.doannt118.utils;

import android.util.Log;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;

/**
 * Utility class để load thông tin bác sĩ/bệnh nhân một cách nhất quán
 * Có log debug để dễ dàng troubleshoot
 */
public class UserInfoLoader {
    
    private static final String TAG = "UserInfoLoader";
    
    public interface BacSiCallback {
        void onSuccess(BacSi bacSi);
        void onError(String message);
    }
    
    public interface BenhNhanCallback {
        void onSuccess(BenhNhan benhNhan);
        void onError(String message);
    }
    
    /**
     * Load thông tin bác sĩ từ maTaiKhoan
     */
    public static void loadBacSi(String maTaiKhoan, FirestoreRepository repo, BacSiCallback callback) {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Log.e(TAG, "loadBacSi: maTaiKhoan is null or empty");
            callback.onError("Mã tài khoản không hợp lệ!");
            return;
        }
        
        Log.d(TAG, "loadBacSi: Loading for maTaiKhoan = " + maTaiKhoan);
        
        repo.getByField("BacSi", "maTaiKhoan", maTaiKhoan,
            querySnapshot -> {
                Log.d(TAG, "loadBacSi: Query returned " + querySnapshot.size() + " documents");
                
                if (querySnapshot.isEmpty()) {
                    Log.e(TAG, "loadBacSi: No documents found for maTaiKhoan = " + maTaiKhoan);
                    Log.e(TAG, "loadBacSi: Please check:");
                    Log.e(TAG, "  1. Firestore collection 'BacSi' exists");
                    Log.e(TAG, "  2. Document has field 'maTaiKhoan' = '" + maTaiKhoan + "'");
                    Log.e(TAG, "  3. Firestore rules allow read access");
                    callback.onError("Không tìm thấy thông tin bác sĩ!\nVui lòng kiểm tra Firestore.");
                    return;
                }
                
                try {
                    BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                    if (bacSi == null) {
                        Log.e(TAG, "loadBacSi: Failed to convert document to BacSi object");
                        callback.onError("Dữ liệu bác sĩ không hợp lệ!");
                        return;
                    }
                    
                    Log.d(TAG, "loadBacSi: Success! maBacSi = " + bacSi.getMaBacSi() + ", hoTen = " + bacSi.getHoTen());
                    callback.onSuccess(bacSi);
                    
                } catch (Exception e) {
                    Log.e(TAG, "loadBacSi: Exception converting document", e);
                    callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                }
            },
            e -> {
                Log.e(TAG, "loadBacSi: Query failed", e);
                Log.e(TAG, "loadBacSi: Error message: " + e.getMessage());
                callback.onError("Lỗi tải thông tin: " + e.getMessage());
            });
    }
    
    /**
     * Load thông tin bệnh nhân từ maTaiKhoan
     */
    public static void loadBenhNhan(String maTaiKhoan, FirestoreRepository repo, BenhNhanCallback callback) {
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Log.e(TAG, "loadBenhNhan: maTaiKhoan is null or empty");
            callback.onError("Mã tài khoản không hợp lệ!");
            return;
        }
        
        Log.d(TAG, "loadBenhNhan: Loading for maTaiKhoan = " + maTaiKhoan);
        
        repo.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
            querySnapshot -> {
                Log.d(TAG, "loadBenhNhan: Query returned " + querySnapshot.size() + " documents");
                
                if (querySnapshot.isEmpty()) {
                    Log.e(TAG, "loadBenhNhan: No documents found for maTaiKhoan = " + maTaiKhoan);
                    Log.e(TAG, "loadBenhNhan: Please check:");
                    Log.e(TAG, "  1. Firestore collection 'BenhNhan' exists");
                    Log.e(TAG, "  2. Document has field 'maTaiKhoan' = '" + maTaiKhoan + "'");
                    Log.e(TAG, "  3. Firestore rules allow read access");
                    callback.onError("Không tìm thấy thông tin bệnh nhân!\nVui lòng kiểm tra Firestore.");
                    return;
                }
                
                try {
                    BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                    if (benhNhan == null) {
                        Log.e(TAG, "loadBenhNhan: Failed to convert document to BenhNhan object");
                        callback.onError("Dữ liệu bệnh nhân không hợp lệ!");
                        return;
                    }
                    
                    Log.d(TAG, "loadBenhNhan: Success! maBenhNhan = " + benhNhan.getMaBenhNhan() + ", hoTen = " + benhNhan.getHoTen());
                    callback.onSuccess(benhNhan);
                    
                } catch (Exception e) {
                    Log.e(TAG, "loadBenhNhan: Exception converting document", e);
                    callback.onError("Lỗi xử lý dữ liệu: " + e.getMessage());
                }
            },
            e -> {
                Log.e(TAG, "loadBenhNhan: Query failed", e);
                Log.e(TAG, "loadBenhNhan: Error message: " + e.getMessage());
                callback.onError("Lỗi tải thông tin: " + e.getMessage());
            });
    }
}
