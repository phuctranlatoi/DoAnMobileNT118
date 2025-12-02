package com.example.doannt118.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.UUID;

public class QuanLyHoSoCaNhan extends AppCompatActivity {

    private EditText etHoTen, etSoDienThoai, etDiaChi, etNgaySinh;
    private ImageView btnEdit, btnBack, ivAvatar;
    private Button btnConfirm, btnCancel;
    private TextView tvMessage, tvChangeAvatar;
    private LinearLayout editButtonLayout;
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private String maBenhNhan; // maProfile hoặc maBacSi
    private String userType; // "benhnhan" hoặc "bacsi"
    private boolean isEditing = false;
    private String currentAvatarUrl;
    private Uri selectedImageUri;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quanlyhosocanhan);

        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        userType = getIntent().getStringExtra("USER_TYPE");
        
        // Mặc định là bệnh nhân nếu không có userType
        if (userType == null || userType.isEmpty()) {
            userType = "benhnhan";
        }

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showError("Không tìm thấy thông tin tài khoản!");
            finish();
            return;
        }

        initActivityResultLaunchers();
        initViews();
        setupClickListeners();
        loadUserData();
    }
    
    private void initActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Log.d("QuanLyHoSo", "Ảnh được chọn: " + selectedImageUri.toString());
                        uploadImageToFirebase(selectedImageUri);
                    } else {
                        Toast.makeText(this, "Không thể lấy ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        
        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null && result.getData().getExtras() != null) {
                        android.graphics.Bitmap bitmap = (android.graphics.Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) {
                            saveBitmapAndUpload(bitmap);
                        } else {
                            Toast.makeText(this, "Không thể lấy ảnh từ camera", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
        
        // Permission launcher
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    showImagePickerDialog();
                } else {
                    Toast.makeText(this, "Cần cấp quyền để chọn ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    
    private void saveBitmapAndUpload(android.graphics.Bitmap bitmap) {
        try {
            java.io.File cacheDir = getCacheDir();
            java.io.File tempFile = new java.io.File(cacheDir, "temp_avatar_" + System.currentTimeMillis() + ".jpg");
            
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            selectedImageUri = Uri.fromFile(tempFile);
            uploadImageToFirebase(selectedImageUri);
            
        } catch (Exception e) {
            Log.e("QuanLyHoSo", "Lỗi lưu ảnh: ", e);
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        etHoTen = findViewById(R.id.etHoTen);
        etSoDienThoai = findViewById(R.id.etSoDienThoai);
        etDiaChi = findViewById(R.id.etDiaChi);
        etNgaySinh = findViewById(R.id.etNgaySinh);
        btnEdit = findViewById(R.id.btnEdit);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        tvMessage = findViewById(R.id.tvMessage);
        editButtonLayout = findViewById(R.id.editButtonLayout);
        ivAvatar = findViewById(R.id.ivAvatar);
        tvChangeAvatar = findViewById(R.id.tvChangeAvatar);

        // Ẩn thông báo lỗi ban đầu
        if (tvMessage != null) tvMessage.setVisibility(View.GONE);

        // Vô hiệu hóa input
        setEditMode(false);
    }

    private void setupClickListeners() {
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> toggleEditMode(true));
        }
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> saveChanges());
        }
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> toggleEditMode(false));
        }
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        if (tvChangeAvatar != null) {
            tvChangeAvatar.setOnClickListener(v -> {
                Log.d("QuanLyHoSo", "Change avatar clicked");
                checkPermissionAndPickImage();
            });
        }
    }

    private void loadUserData() {
        showLoading("Đang tải thông tin...");
        
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";
        String userLabel = "bacsi".equals(userType) ? "bác sĩ" : "bệnh nhân";

        repo.getByField(collection, "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    hideLoading();
                    if (querySnapshot.isEmpty()) {
                        showError("Không tìm thấy thông tin " + userLabel + "!");
                        return;
                    }

                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    
                    if ("bacsi".equals(userType)) {
                        // Load thông tin bác sĩ
                        String maBacSi = doc.getString("maBacSi");
                        String hoTen = doc.getString("hoTen");
                        String soDienThoai = doc.getString("soDienThoai");
                        String diaChi = doc.getString("diaChi");
                        String ngaySinh = doc.getString("ngaySinh");
                        
                        if (maBacSi == null || maBacSi.isEmpty()) {
                            showError("Mã bác sĩ trống!");
                            return;
                        }
                        
                        maBenhNhan = maBacSi; // Dùng chung biến để lưu ID
                        
                        // Hiển thị dữ liệu
                        if (etHoTen != null) etHoTen.setText(safeString(hoTen));
                        if (etSoDienThoai != null) etSoDienThoai.setText(safeString(soDienThoai));
                        if (etDiaChi != null) {
                            etDiaChi.setText(safeString(diaChi));
                            etDiaChi.setVisibility(View.VISIBLE);
                        }
                        if (etNgaySinh != null) etNgaySinh.setText(safeString(ngaySinh));
                        
                        // Load avatar
                        currentAvatarUrl = doc.getString("avatarUrl");
                        loadAvatar(currentAvatarUrl);
                    } else {
                        // Load thông tin bệnh nhân
                        BenhNhan benhNhan = doc.toObject(BenhNhan.class);
                        if (benhNhan == null) {
                            showError("Dữ liệu không hợp lệ!");
                            return;
                        }

                        maBenhNhan = benhNhan.getMaBenhNhan();
                        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
                            showError("Mã bệnh nhân trống!");
                            return;
                        }

                        // Hiển thị dữ liệu
                        if (etHoTen != null) etHoTen.setText(safeString(benhNhan.getHoTen()));
                        if (etSoDienThoai != null) etSoDienThoai.setText(safeString(benhNhan.getSoDienThoai()));
                        if (etDiaChi != null) {
                            etDiaChi.setText(safeString(benhNhan.getDiaChi()));
                            etDiaChi.setVisibility(View.VISIBLE);
                        }
                        if (etNgaySinh != null) etNgaySinh.setText(safeString(benhNhan.getNgaySinh()));
                        
                        // Load avatar
                        currentAvatarUrl = benhNhan.getAvatarUrl();
                        loadAvatar(currentAvatarUrl);
                    }

                    logActivity("Xem hồ sơ cá nhân");
                },
                e -> {
                    hideLoading();
                    Log.e("QuanLyHoSo", "Lỗi Firestore: ", e);
                    showError("Lỗi kết nối: " + e.getMessage());
                });
    }

    private void toggleEditMode(boolean enable) {
        isEditing = enable;
        setEditMode(enable);

        if (editButtonLayout != null) {
            editButtonLayout.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
        if (tvChangeAvatar != null) {
            tvChangeAvatar.setVisibility(enable ? View.VISIBLE : View.GONE);
        }

        // Nếu hủy → reload dữ liệu từ Firestore (an toàn nhất)
        if (!enable) {
            loadUserData();
        }
    }

    private void setEditMode(boolean enable) {
        if (etHoTen != null) etHoTen.setEnabled(enable);
        if (etSoDienThoai != null) etSoDienThoai.setEnabled(enable);
        if (etDiaChi != null) etDiaChi.setEnabled(enable);
        if (etNgaySinh != null) etNgaySinh.setEnabled(enable);
    }

    private void saveChanges() {
        String hoTen = safeTrim(etHoTen);
        String soDienThoai = safeTrim(etSoDienThoai);
        String diaChi = safeTrim(etDiaChi);
        String ngaySinh = safeTrim(etNgaySinh);

        if (hoTen.isEmpty() || soDienThoai.isEmpty() || diaChi.isEmpty() || ngaySinh.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!soDienThoai.matches("\\d{10,11}")) {
            showError("Số điện thoại phải 10-11 số!");
            return;
        }

        if (maBenhNhan == null) {
            showError("Lỗi: Không có mã người dùng!");
            return;
        }

        showLoading("Đang lưu...");
        
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";

        if ("bacsi".equals(userType)) {
            // Cập nhật thông tin bác sĩ
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("maBacSi", maBenhNhan);
            updates.put("maTaiKhoan", maTaiKhoan);
            updates.put("hoTen", hoTen);
            updates.put("soDienThoai", soDienThoai);
            updates.put("diaChi", diaChi);
            updates.put("ngaySinh", ngaySinh);
            
            repo.updateDocumentFields(collection, maBenhNhan, updates,
                    aVoid -> {
                        hideLoading();
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        logActivity("Cập nhật hồ sơ cá nhân");
                        toggleEditMode(false);
                    },
                    e -> {
                        hideLoading();
                        Log.e("QuanLyHoSo", "Lỗi cập nhật: ", e);
                        showError("Lỗi lưu: " + e.getMessage());
                    });
        } else {
            // Cập nhật thông tin bệnh nhân
            BenhNhan updated = new BenhNhan();
            updated.setMaBenhNhan(maBenhNhan);
            updated.setMaTaiKhoan(maTaiKhoan);
            updated.setHoTen(hoTen);
            updated.setSoDienThoai(soDienThoai);
            updated.setDiaChi(diaChi);
            updated.setNgaySinh(ngaySinh);

            repo.updateDocument(collection, maBenhNhan, updated,
                    aVoid -> {
                        hideLoading();
                        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        logActivity("Cập nhật hồ sơ cá nhân");
                        toggleEditMode(false);
                    },
                    e -> {
                        hideLoading();
                        Log.e("QuanLyHoSo", "Lỗi cập nhật: ", e);
                        showError("Lỗi lưu: " + e.getMessage());
                    });
        }
    }

    // === HÀM HỖ TRỢ ===
    private String safeTrim(EditText et) {
        return et != null ? et.getText().toString().trim() : "";
    }

    private String safeString(String s) {
        return s != null ? s : "";
    }

    private void showError(String msg) {
        if (tvMessage != null) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
            tvMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showLoading(String msg) {
        if (tvMessage != null) {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(msg);
            tvMessage.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void hideLoading() {
        if (tvMessage != null && !tvMessage.getText().toString().contains("lỗi")) {
            tvMessage.setVisibility(View.GONE);
        }
    }

    private void logActivity(String tenHoatDong) {
        String maLichSu = UUID.randomUUID().toString();
        LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoan, tenHoatDong, new Date(), "Bệnh nhân: " + tenHoatDong);
        repo.logActivity(lichSu);
    }
    
    private void loadAvatar(String avatarUrl) {
        if (avatarUrl != null && !avatarUrl.isEmpty() && ivAvatar != null) {
            Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .circleCrop()
                .into(ivAvatar);
        }
    }
    
    private void checkPermissionAndPickImage() {
        Log.d("QuanLyHoSo", "checkPermissionAndPickImage called");
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                showImagePickerDialog();
            } else {
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }
    
    private void showImagePickerDialog() {
        Log.d("QuanLyHoSo", "showImagePickerDialog called");
        String[] options = {"Chọn từ thư viện", "Chụp ảnh mới"};
        
        new AlertDialog.Builder(this)
            .setTitle("Chọn ảnh đại diện")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    openGallery();
                } else {
                    openCamera();
                }
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Log.e("QuanLyHoSo", "Error opening gallery: ", e);
            Toast.makeText(this, "Lỗi mở thư viện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("QuanLyHoSo", "Error opening camera: ", e);
            Toast.makeText(this, "Lỗi mở camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Không có ảnh để tải lên", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Lỗi: Chưa đăng nhập Firebase Auth!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String firebaseUid = auth.getCurrentUser().getUid();
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        
        try {
            String storageBucket = "qlykhambenh.firebasestorage.app";
            FirebaseStorage storageInstance = FirebaseStorage.getInstance("gs://" + storageBucket);
            String fileName = "avatars/" + firebaseUid + "/avatar_" + System.currentTimeMillis() + ".jpg";
            StorageReference storageRef = storageInstance.getReference().child(fileName);
            
            storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateAvatarUrlInFirestore(downloadUrl);
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("QuanLyHoSo", "Lỗi upload ảnh: ", e);
                    if (e.getMessage() != null && e.getMessage().contains("Permission denied")) {
                        Toast.makeText(this, "Lỗi: Không có quyền upload. Vui lòng kiểm tra Storage Rules.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lỗi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo Storage: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void updateAvatarUrlInFirestore(String avatarUrl) {
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";
        
        firestore.collection(collection)
            .whereEqualTo("maTaiKhoan", maTaiKhoan)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String docId = querySnapshot.getDocuments().get(0).getId();
                    
                    firestore.collection(collection)
                        .document(docId)
                        .update("avatarUrl", avatarUrl)
                        .addOnSuccessListener(aVoid -> {
                            currentAvatarUrl = avatarUrl;
                            loadAvatar(avatarUrl);
                            Toast.makeText(this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Lỗi cập nhật ảnh đại diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tìm thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
}