package com.example.doannt118.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.Admin;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.LichSuHoatDong;
import com.example.doannt118.model.TaiKhoan;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import org.mindrot.jbcrypt.BCrypt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MainAdminActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvUserName;
    private TabLayout tabLayout;
    private RecyclerView rvAccounts;
    private Button btnLogout;
    private FirestoreRepository repo;
    private FirebaseAuth auth;
    private String maTaiKhoanAdmin;
    private AccountAdapter adapter;
    private List<TaiKhoan> pendingAccounts;
    private List<TaiKhoan> allAccounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main_admin);

        repo = new FirestoreRepository();
        auth = FirebaseAuth.getInstance();
        maTaiKhoanAdmin = getIntent().getStringExtra("MA_TAI_KHOAN");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvUserName = findViewById(R.id.tvUserName);
        tabLayout = findViewById(R.id.tabLayout);
        rvAccounts = findViewById(R.id.rvAccounts);
        btnLogout = findViewById(R.id.btnLogout);
        rvAccounts.setLayoutManager(new LinearLayoutManager(this));

        pendingAccounts = new ArrayList<>();
        allAccounts = new ArrayList<>();

        tabLayout.addTab(tabLayout.newTab().setText("Chờ duyệt"));
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả tài khoản"));
        tabLayout.addTab(tabLayout.newTab().setText("Tạo tài khoản"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadPendingAccounts();
                        break;
                    case 1:
                        loadAllAccounts();
                        break;
                    case 2:
                        showCreateAccountDialog();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });

        btnLogout.setOnClickListener(v -> {
            String maLichSu = UUID.randomUUID().toString();
            LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoanAdmin, "Đăng xuất", new Date(), "Đăng xuất khỏi hệ thống");
            repo.logActivity(lichSu);
            auth.signOut();
            startActivity(new Intent(MainAdminActivity.this, LoginActivity.class));
            finish();
        });

        loadUserInfo();
        loadPendingAccounts(); // Mặc định hiển thị tab "Chờ duyệt"
    }

    private void loadUserInfo() {
        repo.getByField("Admin", "maTaiKhoan", maTaiKhoanAdmin,
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Admin admin = querySnapshot.getDocuments().get(0).toObject(Admin.class);
                        if (admin != null) {
                            tvUserName.setText(admin.getHoTen());
                        }
                    }
                },
                e -> Toast.makeText(this, "Lỗi tải thông tin admin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadPendingAccounts() {
        repo.getByField("TaiKhoan", "trangThai", "Chờ duyệt",
                querySnapshot -> {
                    pendingAccounts.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        TaiKhoan taiKhoan = doc.toObject(TaiKhoan.class);
                        if (taiKhoan != null) {
                            pendingAccounts.add(taiKhoan);
                        }
                    }
                    adapter = new AccountAdapter(pendingAccounts, true);
                    rvAccounts.setAdapter(adapter);
                },
                e -> Toast.makeText(this, "Lỗi tải danh sách tài khoản chờ duyệt: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadAllAccounts() {
        repo.getAll("TaiKhoan",
                querySnapshot -> {
                    allAccounts.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        TaiKhoan taiKhoan = doc.toObject(TaiKhoan.class);
                        if (taiKhoan != null) {
                            allAccounts.add(taiKhoan);
                        }
                    }
                    adapter = new AccountAdapter(allAccounts, false);
                    rvAccounts.setAdapter(adapter);
                },
                e -> Toast.makeText(this, "Lỗi tải danh sách tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showCreateAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_account, null);
        builder.setView(dialogView);

        EditText txtTenDangNhap = dialogView.findViewById(R.id.txtTenDangNhap);
        EditText txtMatKhau = dialogView.findViewById(R.id.txtMatKhau);
        EditText txtHoTen = dialogView.findViewById(R.id.txtHoTen);
        EditText txtSoDienThoai = dialogView.findViewById(R.id.txtSoDienThoai);
        EditText txtEmail = dialogView.findViewById(R.id.txtEmail);
        EditText txtBangCap = dialogView.findViewById(R.id.txtBangCap);
        EditText txtHocVi = dialogView.findViewById(R.id.txtHocVi);
        EditText txtChungChi = dialogView.findViewById(R.id.txtChungChi);
        Spinner spVaiTro = dialogView.findViewById(R.id.spVaiTro);
        Button btnDangKy = dialogView.findViewById(R.id.btnDangKy);
        Button btnQuayLai = dialogView.findViewById(R.id.btnQuayLai);

        ArrayAdapter<String> vaiTroAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Bác sĩ", "Admin"});
        vaiTroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVaiTro.setAdapter(vaiTroAdapter);

        AlertDialog dialog = builder.create();

        btnDangKy.setOnClickListener(v -> {
            String tenDangNhap = txtTenDangNhap.getText().toString().trim();
            String matKhau = txtMatKhau.getText().toString().trim();
            String hoTen = txtHoTen.getText().toString().trim();
            String sdt = txtSoDienThoai.getText().toString().trim();
            String email = txtEmail.getText().toString().trim();
            String bangCap = txtBangCap.getText().toString().trim();
            String hocVi = txtHocVi.getText().toString().trim();
            String chungChi = txtChungChi.getText().toString().trim();
            String vaiTro = spVaiTro.getSelectedItem().toString();

            if (tenDangNhap.isEmpty() || matKhau.isEmpty() || hoTen.isEmpty() || sdt.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ thông tin bắt buộc!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (vaiTro.equals("Bác sĩ") && (bangCap.isEmpty() || hocVi.isEmpty() || chungChi.isEmpty())) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ bằng cấp, học vị và chứng chỉ cho bác sĩ!", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.getByField("TaiKhoan", "tenDangNhap", tenDangNhap,
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
                        } else {
                            repo.getByField("TaiKhoan", "email", email,
                                    emailSnapshot -> {
                                        if (!emailSnapshot.isEmpty()) {
                                            Toast.makeText(this, "Email đã tồn tại!", Toast.LENGTH_SHORT).show();
                                        } else {
                                            createNewAccount(tenDangNhap, matKhau, hoTen, sdt, email, bangCap, hocVi, chungChi, vaiTro, dialog);
                                        }
                                    },
                                    e -> Toast.makeText(this, "Lỗi kiểm tra email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    },
                    e -> Toast.makeText(this, "Lỗi kiểm tra tên đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnQuayLai.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void createNewAccount(String tenDangNhap, String matKhau, String hoTen, String sdt, String email,
                                  String bangCap, String hocVi, String chungChi, String vaiTro, AlertDialog dialog) {
        String maTaiKhoan = UUID.randomUUID().toString();
        String maProfile = UUID.randomUUID().toString();
        String matKhauDaBam;
        try {
            matKhauDaBam = BCrypt.hashpw(matKhau, BCrypt.gensalt());
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi băm mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, matKhau)
                .addOnSuccessListener(authResult -> {
                    authResult.getUser().sendEmailVerification();
                    TaiKhoan newTaiKhoan = new TaiKhoan(maTaiKhoan, tenDangNhap, matKhauDaBam, vaiTro, email, "Chờ duyệt");
                    Object userProfile;

                    if (vaiTro.equals("Bác sĩ")) {
                        userProfile = new BacSi(maProfile, maTaiKhoan, hoTen, sdt, bangCap, hocVi, Arrays.asList(chungChi.split(",\\s*")), "Chờ xác thực");
                    } else {
                        userProfile = new Admin(maProfile, maTaiKhoan, hoTen, sdt);
                    }

                    repo.registerNewUserBatch(newTaiKhoan, userProfile,
                            v -> {
                                String maLichSu = UUID.randomUUID().toString();
                                LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoanAdmin, "Tạo tài khoản", new Date(), "Tạo tài khoản " + vaiTro + ": " + hoTen);
                                repo.logActivity(lichSu);
                                Toast.makeText(this, "Tạo tài khoản thành công! Chờ xác thực.", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                                tabLayout.getTabAt(0).select();
                            },
                            e -> {
                                if (authResult.getUser() != null) {
                                    authResult.getUser().delete();
                                }
                                Toast.makeText(this, "Tạo tài khoản thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạo tài khoản Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showEditAccountDialog(TaiKhoan taiKhoan) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_account, null);
        builder.setView(dialogView);

        EditText etHoTen = dialogView.findViewById(R.id.etHoTen);
        EditText etSoDienThoai = dialogView.findViewById(R.id.etSoDienThoai);
        EditText etBangCap = dialogView.findViewById(R.id.etBangCap);
        EditText etHocVi = dialogView.findViewById(R.id.etHocVi);
        EditText etChungChi = dialogView.findViewById(R.id.etChungChi);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        String collection = taiKhoan.getVaiTro().equals("Bác sĩ") ? "BacSi" : taiKhoan.getVaiTro().equals("Admin") ? "Admin" : "BenhNhan";
        repo.getByField(collection, "maTaiKhoan", taiKhoan.getMaTaiKhoan(),
                querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        if (taiKhoan.getVaiTro().equals("Bác sĩ")) {
                            BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                            if (bacSi != null) {
                                etHoTen.setText(bacSi.getHoTen());
                                etSoDienThoai.setText(bacSi.getSoDienThoai());
                                etBangCap.setText(bacSi.getBangCap() != null ? bacSi.getBangCap() : "");
                                etHocVi.setText(bacSi.getHocVi() != null ? bacSi.getHocVi() : "");
                                etChungChi.setText(bacSi.getChungChiHanhNghe() != null ? String.join(", ", bacSi.getChungChiHanhNghe()) : "");
                            }
                        } else if (taiKhoan.getVaiTro().equals("Admin")) {
                            Admin admin = querySnapshot.getDocuments().get(0).toObject(Admin.class);
                            if (admin != null) {
                                etHoTen.setText(admin.getHoTen());
                                etSoDienThoai.setText(admin.getSoDienThoai());
                                etBangCap.setVisibility(View.GONE);
                                etHocVi.setVisibility(View.GONE);
                                etChungChi.setVisibility(View.GONE);
                            }
                        } else {
                            BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                            if (benhNhan != null) {
                                etHoTen.setText(benhNhan.getHoTen());
                                etSoDienThoai.setText(benhNhan.getSoDienThoai());
                                etBangCap.setVisibility(View.GONE);
                                etHocVi.setVisibility(View.GONE);
                                etChungChi.setVisibility(View.GONE);
                            }
                        }
                    }
                },
                e -> Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String hoTen = etHoTen.getText().toString().trim();
            String sdt = etSoDienThoai.getText().toString().trim();
            String bangCap = etBangCap.getText().toString().trim();
            String hocVi = etHocVi.getText().toString().trim();
            String chungChi = etChungChi.getText().toString().trim();

            if (hoTen.isEmpty() || sdt.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ họ tên và số điện thoại!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (taiKhoan.getVaiTro().equals("Bác sĩ") && (bangCap.isEmpty() || hocVi.isEmpty() || chungChi.isEmpty())) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ bằng cấp, học vị và chứng chỉ cho bác sĩ!", Toast.LENGTH_SHORT).show();
                return;
            }

            repo.getByField(collection, "maTaiKhoan", taiKhoan.getMaTaiKhoan(),
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            Object userProfile;
                            String maProfile = querySnapshot.getDocuments().get(0).getId();
                            if (taiKhoan.getVaiTro().equals("Bác sĩ")) {
                                userProfile = new BacSi(maProfile, taiKhoan.getMaTaiKhoan(), hoTen, sdt, bangCap, hocVi, Arrays.asList(chungChi.split(",\\s*")), "Chờ xác thực");
                            } else if (taiKhoan.getVaiTro().equals("Admin")) {
                                userProfile = new Admin(maProfile, taiKhoan.getMaTaiKhoan(), hoTen, sdt);
                            } else {
                                userProfile = new BenhNhan(maProfile, taiKhoan.getMaTaiKhoan(), hoTen, sdt, "");
                            }

                            repo.updateDocument(collection, maProfile, userProfile,
                                    v2 -> {
                                        String maLichSu = UUID.randomUUID().toString();
                                        LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoanAdmin, "Sửa thông tin tài khoản", new Date(), "Sửa thông tin tài khoản " + taiKhoan.getVaiTro() + ": " + hoTen);
                                        repo.logActivity(lichSu);
                                        Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                                        loadAllAccounts();
                                        dialog.dismiss();
                                    },
                                    e -> Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    },
                    e -> Toast.makeText(this, "Lỗi tải thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {
        private List<TaiKhoan> list;
        private boolean isPendingMode;

        public AccountAdapter(List<TaiKhoan> list, boolean isPendingMode) {
            this.list = list;
            this.isPendingMode = isPendingMode;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_account, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            TaiKhoan taiKhoan = list.get(position);
            holder.tvTenDangNhap.setText(taiKhoan.getTenDangNhap());
            holder.tvEmail.setText(taiKhoan.getEmail());
            holder.tvVaiTro.setText(taiKhoan.getVaiTro());
            holder.tvTrangThai.setText(taiKhoan.getTrangThai());

            String collection = taiKhoan.getVaiTro().equals("Bác sĩ") ? "BacSi" : taiKhoan.getVaiTro().equals("Admin") ? "Admin" : "BenhNhan";
            repo.getByField(collection, "maTaiKhoan", taiKhoan.getMaTaiKhoan(),
                    querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            if (taiKhoan.getVaiTro().equals("Bác sĩ")) {
                                BacSi bacSi = querySnapshot.getDocuments().get(0).toObject(BacSi.class);
                                if (bacSi != null) {
                                    holder.tvHoTen.setText(bacSi.getHoTen());
                                    holder.tvBangCap.setText(bacSi.getBangCap() != null ? bacSi.getBangCap() : "N/A");
                                    holder.tvHocVi.setText(bacSi.getHocVi() != null ? bacSi.getHocVi() : "N/A");
                                    holder.tvChungChi.setText(bacSi.getChungChiHanhNghe() != null ? String.join(", ", bacSi.getChungChiHanhNghe()) : "N/A");
                                    holder.tvTrangThaiXacThuc.setText(bacSi.getTrangThaiXacThuc());
                                }
                            } else if (taiKhoan.getVaiTro().equals("Admin")) {
                                Admin admin = querySnapshot.getDocuments().get(0).toObject(Admin.class);
                                if (admin != null) {
                                    holder.tvHoTen.setText(admin.getHoTen());
                                    holder.tvBangCap.setVisibility(View.GONE);
                                    holder.tvHocVi.setVisibility(View.GONE);
                                    holder.tvChungChi.setVisibility(View.GONE);
                                    holder.tvTrangThaiXacThuc.setVisibility(View.GONE);
                                }
                            } else {
                                BenhNhan benhNhan = querySnapshot.getDocuments().get(0).toObject(BenhNhan.class);
                                if (benhNhan != null) {
                                    holder.tvHoTen.setText(benhNhan.getHoTen());
                                    holder.tvBangCap.setVisibility(View.GONE);
                                    holder.tvHocVi.setVisibility(View.GONE);
                                    holder.tvChungChi.setVisibility(View.GONE);
                                    holder.tvTrangThaiXacThuc.setVisibility(View.GONE);
                                }
                            }
                        }
                    },
                    e -> Toast.makeText(MainAdminActivity.this, "Lỗi tải thông tin chi tiết: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            if (isPendingMode) {
                holder.btnApprove.setVisibility(taiKhoan.getVaiTro().equals("Bác sĩ") ? View.VISIBLE : View.GONE);
                holder.btnReject.setVisibility(taiKhoan.getVaiTro().equals("Bác sĩ") ? View.VISIBLE : View.GONE);
                holder.btnEdit.setVisibility(View.GONE);
                holder.btnLock.setVisibility(View.GONE);

                if (taiKhoan.getVaiTro().equals("Bác sĩ")) {
                    holder.btnApprove.setOnClickListener(v -> {
                        repo.getByField("BacSi", "maTaiKhoan", taiKhoan.getMaTaiKhoan(),
                                querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        String maBacSi = querySnapshot.getDocuments().get(0).getId();
                                        repo.approveBacSi(maBacSi, "Đã xác thực", "Hoạt động", taiKhoan.getMaTaiKhoan(),
                                                v2 -> {
                                                    String maLichSu = UUID.randomUUID().toString();
                                                    LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoanAdmin, "Duyệt bác sĩ", new Date(), "Duyệt bác sĩ: " + taiKhoan.getTenDangNhap());
                                                    repo.logActivity(lichSu);
                                                    list.remove(position);
                                                    notifyDataSetChanged();
                                                    Toast.makeText(MainAdminActivity.this, "Đã duyệt tài khoản bác sĩ!", Toast.LENGTH_SHORT).show();
                                                },
                                                e -> Toast.makeText(MainAdminActivity.this, "Lỗi duyệt: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    }
                                },
                                e -> Toast.makeText(MainAdminActivity.this, "Lỗi tìm bác sĩ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });

                    holder.btnReject.setOnClickListener(v -> {
                        repo.getByField("BacSi", "maTaiKhoan", taiKhoan.getMaTaiKhoan(),
                                querySnapshot -> {
                                    if (!querySnapshot.isEmpty()) {
                                        String maBacSi = querySnapshot.getDocuments().get(0).getId();
                                        repo.approveBacSi(maBacSi, "Từ chối", "Bị khóa", taiKhoan.getMaTaiKhoan(),
                                                v2 -> {
                                                    String maLichSu = UUID.randomUUID().toString();
                                                    LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoanAdmin, "Từ chối bác sĩ", new Date(), "Từ chối bác sĩ: " + taiKhoan.getTenDangNhap());
                                                    repo.logActivity(lichSu);
                                                    list.remove(position);
                                                    notifyDataSetChanged();
                                                    Toast.makeText(MainAdminActivity.this, "Đã từ chối tài khoản bác sĩ!", Toast.LENGTH_SHORT).show();
                                                },
                                                e -> Toast.makeText(MainAdminActivity.this, "Lỗi từ chối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                    }
                                },
                                e -> Toast.makeText(MainAdminActivity.this, "Lỗi tìm bác sĩ: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
                }
            } else {
                holder.btnApprove.setVisibility(View.GONE);
                holder.btnReject.setVisibility(View.GONE);
                holder.btnEdit.setVisibility(View.VISIBLE);
                holder.btnLock.setVisibility(View.VISIBLE);

                holder.btnEdit.setOnClickListener(v -> showEditAccountDialog(taiKhoan));

                holder.btnLock.setText(taiKhoan.getTrangThai().equals("Hoạt động") ? "Khóa" : "Mở khóa");
                holder.btnLock.setOnClickListener(v -> {
                    String newStatus = taiKhoan.getTrangThai().equals("Hoạt động") ? "Bị khóa" : "Hoạt động";
                    repo.updateDocument("TaiKhoan", taiKhoan.getMaTaiKhoan(), new TaiKhoan(
                                    taiKhoan.getMaTaiKhoan(),
                                    taiKhoan.getTenDangNhap(),
                                    taiKhoan.getMatKhau(),
                                    taiKhoan.getVaiTro(),
                                    taiKhoan.getEmail(),
                                    newStatus),
                            v2 -> {
                                String maLichSu = UUID.randomUUID().toString();
                                LichSuHoatDong lichSu = new LichSuHoatDong(maLichSu, maTaiKhoanAdmin, newStatus.equals("Hoạt động") ? "Mở khóa tài khoản" : "Khóa tài khoản", new Date(), (newStatus.equals("Hoạt động") ? "Mở khóa" : "Khóa") + " tài khoản: " + taiKhoan.getTenDangNhap());
                                repo.logActivity(lichSu);
                                Toast.makeText(MainAdminActivity.this, (newStatus.equals("Hoạt động") ? "Mở khóa" : "Khóa") + " tài khoản thành công!", Toast.LENGTH_SHORT).show();
                                notifyDataSetChanged();
                            },
                            e -> Toast.makeText(MainAdminActivity.this, "Lỗi cập nhật trạng thái: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
            }
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvTenDangNhap, tvEmail, tvVaiTro, tvTrangThai, tvHoTen, tvBangCap, tvHocVi, tvChungChi, tvTrangThaiXacThuc;
            Button btnApprove, btnReject, btnEdit, btnLock;

            ViewHolder(View itemView) {
                super(itemView);
                tvTenDangNhap = itemView.findViewById(R.id.tvTenDangNhap);
                tvEmail = itemView.findViewById(R.id.tvEmail);
                tvVaiTro = itemView.findViewById(R.id.tvVaiTro);
                tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
                tvHoTen = itemView.findViewById(R.id.tvHoTen);
                tvBangCap = itemView.findViewById(R.id.tvBangCap);
                tvHocVi = itemView.findViewById(R.id.tvHocVi);
                tvChungChi = itemView.findViewById(R.id.tvChungChi);
                tvTrangThaiXacThuc = itemView.findViewById(R.id.tvTrangThaiXacThuc);
                btnApprove = itemView.findViewById(R.id.btnApprove);
                btnReject = itemView.findViewById(R.id.btnReject);
                btnEdit = itemView.findViewById(R.id.btnEdit);
                btnLock = itemView.findViewById(R.id.btnLock);
            }
        }
    }
}