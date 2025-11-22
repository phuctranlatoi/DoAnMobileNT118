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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.doannt118.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivAvatar;
    private TextView tvUserName, tvUserPhone, tvMenuHoSoTitle;
    private View btnEditAvatar;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private SessionManager sessionManager;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private String maTaiKhoan;
    private String userType; // "benhnhan" hoặc "bacsi"
    private String currentAvatarUrl;
    private Uri selectedImageUri;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        userType = getIntent().getStringExtra("USER_TYPE"); // "benhnhan" hoặc "bacsi"

        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã tài khoản!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initActivityResultLaunchers();
        initViews();
        setupClickListeners();
        setupBottomNavigation();
        loadUserInfo();
    }
    
    private void initActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        Log.d("ProfileActivity", "Ảnh được chọn: " + selectedImageUri.toString());
                        uploadImageToFirebase(selectedImageUri);
                    } else {
                        Toast.makeText(this, "Không thể lấy ảnh", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
        
        // Camera launcher - Sửa lại để xử lý bitmap từ camera
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null && result.getData().getExtras() != null) {
                        // Lấy bitmap từ camera
                        android.graphics.Bitmap bitmap = (android.graphics.Bitmap) result.getData().getExtras().get("data");
                        if (bitmap != null) {
                            // Lưu bitmap vào file tạm và upload
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
            // Tạo file tạm trong cache
            java.io.File cacheDir = getCacheDir();
            java.io.File tempFile = new java.io.File(cacheDir, "temp_avatar_" + System.currentTimeMillis() + ".jpg");
            
            // Lưu bitmap vào file
            java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile);
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();
            
            // Tạo URI từ file
            selectedImageUri = Uri.fromFile(tempFile);
            uploadImageToFirebase(selectedImageUri);
            
        } catch (Exception e) {
            Log.e("ProfileActivity", "Lỗi lưu ảnh: ", e);
            Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.ivAvatar);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvMenuHoSoTitle = findViewById(R.id.tvMenuHoSoTitle);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
        
        // Cập nhật text dựa vào loại người dùng
        if (tvMenuHoSoTitle != null) {
            if ("bacsi".equals(userType)) {
                tvMenuHoSoTitle.setText("Hồ sơ cá nhân");
            } else {
                tvMenuHoSoTitle.setText("Hồ sơ y tế");
            }
        }
    }

    private void setupClickListeners() {
        // Nút chỉnh sửa avatar
        if (btnEditAvatar != null) {
            btnEditAvatar.setOnClickListener(v -> {
                Log.d("ProfileActivity", "Edit avatar button clicked");
                Toast.makeText(this, "Đang mở chọn ảnh...", Toast.LENGTH_SHORT).show();
                checkPermissionAndPickImage();
            });
        } else {
            Log.e("ProfileActivity", "btnEditAvatar is null!");
        }
        
        // Hồ sơ y tế / Hồ sơ cá nhân
        View menuHoSoYTe = findViewById(R.id.menuHoSoYTe);
        if (menuHoSoYTe != null) {
            menuHoSoYTe.setOnClickListener(v -> {
                Intent intent = new Intent(this, QuanLyHoSoCaNhan.class);
                intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
                intent.putExtra("USER_TYPE", userType);
                startActivity(intent);
            });
        }

        // Danh sách quan tâm
        View menuDanhSachQuanTam = findViewById(R.id.menuDanhSachQuanTam);
        if (menuDanhSachQuanTam != null) {
            menuDanhSachQuanTam.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Điều khoản
        View menuDieuKhoan = findViewById(R.id.menuDieuKhoan);
        if (menuDieuKhoan != null) {
            menuDieuKhoan.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Liên hệ
        View menuLienHe = findViewById(R.id.menuLienHe);
        if (menuLienHe != null) {
            menuLienHe.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng đang phát triển!", Toast.LENGTH_SHORT).show()
            );
        }

        // Cài đặt
        View menuCaiDat = findViewById(R.id.menuCaiDat);
        if (menuCaiDat != null) {
            menuCaiDat.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingActivity.class);
                intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
                startActivity(intent);
            });
        }

        // Đăng xuất
        View menuDangXuat = findViewById(R.id.menuDangXuat);
        if (menuDangXuat != null) {
            menuDangXuat.setOnClickListener(v -> handleDangXuat());
        }
    }

    private void loadUserInfo() {
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";
        
        repo.getByField(collection, "maTaiKhoan", maTaiKhoan,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        if ("bacsi".equals(userType)) {
                            // Load thông tin bác sĩ
                            var bacSi = querySnapshot.getDocuments().get(0);
                            String hoTen = bacSi.getString("hoTen");
                            String soDienThoai = bacSi.getString("soDienThoai");
                            currentAvatarUrl = bacSi.getString("avatarUrl");
                            
                            if (tvUserName != null) {
                                tvUserName.setText(hoTen != null ? hoTen : "Bác sĩ");
                            }
                            if (tvUserPhone != null) {
                                tvUserPhone.setText(soDienThoai != null ? soDienThoai : "");
                            }
                            loadAvatar(currentAvatarUrl);
                        } else {
                            // Load thông tin bệnh nhân
                            BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                            if (benhNhan != null) {
                                if (tvUserName != null) {
                                    tvUserName.setText(benhNhan.getHoTen() != null ? benhNhan.getHoTen() : "Người dùng");
                                }
                                if (tvUserPhone != null) {
                                    tvUserPhone.setText(benhNhan.getSoDienThoai() != null ? benhNhan.getSoDienThoai() : "");
                                }
                                currentAvatarUrl = benhNhan.getAvatarUrl();
                                loadAvatar(currentAvatarUrl);
                            }
                        }
                    }
                },
                e -> {
                    Log.e("ProfileActivity", "Lỗi tải thông tin: ", e);
                });
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
        Log.d("ProfileActivity", "checkPermissionAndPickImage called");
        
        // Với Android 13+ (API 33+), không cần quyền READ_EXTERNAL_STORAGE cho ACTION_PICK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Chỉ cần quyền READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) 
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("ProfileActivity", "Permission granted (Android 13+)");
                showImagePickerDialog();
            } else {
                Log.d("ProfileActivity", "Requesting permission (Android 13+)");
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 trở xuống
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED) {
                Log.d("ProfileActivity", "Permission granted (Android 12-)");
                showImagePickerDialog();
            } else {
                Log.d("ProfileActivity", "Requesting permission (Android 12-)");
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }
    
    private void showImagePickerDialog() {
        Log.d("ProfileActivity", "showImagePickerDialog called");
        String[] options = {"Chọn từ thư viện", "Chụp ảnh mới"};
        
        new AlertDialog.Builder(this)
            .setTitle("Chọn ảnh đại diện")
            .setItems(options, (dialog, which) -> {
                Log.d("ProfileActivity", "Option selected: " + which);
                if (which == 0) {
                    openGallery();
                } else {
                    openCamera();
                }
            })
            .setNegativeButton("Hủy", (dialog, which) -> {
                Log.d("ProfileActivity", "Dialog cancelled");
            })
            .show();
    }
    
    private void openGallery() {
        try {
            Log.d("ProfileActivity", "Opening gallery");
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            galleryLauncher.launch(intent);
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error opening gallery: ", e);
            Toast.makeText(this, "Lỗi mở thư viện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openCamera() {
        try {
            Log.d("ProfileActivity", "Opening camera");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Không tìm thấy ứng dụng camera", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ProfileActivity", "Error opening camera: ", e);
            Toast.makeText(this, "Lỗi mở camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri == null) {
            Toast.makeText(this, "Không có ảnh để tải lên", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Hiển thị progress
        Toast.makeText(this, "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();
        Log.d("ProfileActivity", "Bắt đầu upload ảnh: " + imageUri.toString());
        
        // Tạo reference đến Firebase Storage
        String fileName = "avatars/" + maTaiKhoan + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference().child(fileName);
        
        // Upload file
        storageRef.putFile(imageUri)
            .addOnProgressListener(taskSnapshot -> {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                Log.d("ProfileActivity", "Upload progress: " + progress + "%");
            })
            .addOnSuccessListener(taskSnapshot -> {
                Log.d("ProfileActivity", "Upload thành công!");
                // Lấy URL của ảnh đã upload
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    Log.d("ProfileActivity", "Download URL: " + downloadUrl);
                    updateAvatarUrlInFirestore(downloadUrl);
                }).addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Lỗi lấy download URL: ", e);
                    Toast.makeText(this, "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            })
            .addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Lỗi upload ảnh: ", e);
                Toast.makeText(this, "Lỗi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void updateAvatarUrlInFirestore(String avatarUrl) {
        String collection = "bacsi".equals(userType) ? "BacSi" : "BenhNhan";
        Log.d("ProfileActivity", "Cập nhật Firestore - Collection: " + collection + ", maTaiKhoan: " + maTaiKhoan);
        
        firestore.collection(collection)
            .whereEqualTo("maTaiKhoan", maTaiKhoan)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    String docId = querySnapshot.getDocuments().get(0).getId();
                    Log.d("ProfileActivity", "Tìm thấy document ID: " + docId);
                    
                    firestore.collection(collection)
                        .document(docId)
                        .update("avatarUrl", avatarUrl)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileActivity", "Cập nhật Firestore thành công!");
                            currentAvatarUrl = avatarUrl;
                            loadAvatar(avatarUrl);
                            Toast.makeText(this, "Cập nhật ảnh đại diện thành công!", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileActivity", "Lỗi cập nhật Firestore: ", e);
                            Toast.makeText(this, "Lỗi cập nhật ảnh đại diện: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                } else {
                    Log.e("ProfileActivity", "Không tìm thấy document với maTaiKhoan: " + maTaiKhoan);
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e("ProfileActivity", "Lỗi tìm document: ", e);
                Toast.makeText(this, "Lỗi tìm thông tin người dùng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        if (bottomNavigation != null) {
            // Set menu dựa vào loại người dùng
            if ("bacsi".equals(userType)) {
                bottomNavigation.inflateMenu(R.menu.bottom_nav_doctor);
            } else {
                bottomNavigation.inflateMenu(R.menu.bottom_nav_patient);
            }
            
            // Set selected item to profile
            bottomNavigation.setSelectedItemId(R.id.nav_profile);
            
            bottomNavigation.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    // Quay về trang chủ
                    finish();
                    return true;
                } else if (itemId == R.id.nav_messages) {
                    Toast.makeText(this, "Chức năng Tin nhắn đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_add) {
                    Toast.makeText(this, "Chức năng Đặt lịch đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_appointments) {
                    Toast.makeText(this, "Chức năng Xem lịch khám đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_notifications) {
                    Toast.makeText(this, "Chức năng Thông báo đang phát triển!", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // Đã ở trang profile
                    return true;
                }
                return false;
            });
        }
    }

    private void handleDangXuat() {
        // Xóa session
        sessionManager.logout();
        // Đăng xuất Firebase
        auth.signOut();
        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
