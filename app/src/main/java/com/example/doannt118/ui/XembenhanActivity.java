package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.doannt118.R;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class XemBenhAnActivity extends AppCompatActivity {
    private RecyclerView rvBenhAn;
    private BenhAnAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private FirestoreRepository repository;
    private String maBenhNhan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xem_benh_an);

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadBenhNhanInfo();
    }

    private void initViews() {
        rvBenhAn = findViewById(R.id.rvBenhAn);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        repository = new FirestoreRepository();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new BenhAnAdapter(this);
        rvBenhAn.setLayoutManager(new LinearLayoutManager(this));
        rvBenhAn.setAdapter(adapter);
        
        swipeRefresh.setOnRefreshListener(() -> {
            if (maBenhNhan != null) {
                loadBenhAn();
            }
        });
    }

    private void loadBenhNhanInfo() {
        // Lấy maTaiKhoan từ Intent (đã được truyền từ MainBenhNhanActivity)
        String maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");
        if (maTaiKhoan == null || maTaiKhoan.isEmpty()) {
            showLoading(false);
            showEmpty(true);
            Toast.makeText(this, "Mã tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        repository.getByField("BenhNhan", "maTaiKhoan", maTaiKhoan,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    maBenhNhan = doc.getString("maBenhNhan");
                    loadBenhAn();
                } else {
                    showLoading(false);
                    showEmpty(true);
                    Toast.makeText(this, "Không tìm thấy thông tin bệnh nhân", Toast.LENGTH_SHORT).show();
                }
            },
            e -> {
                showLoading(false);
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadBenhAn() {
        showLoading(true);
        showEmpty(false);
        
        repository.getByField("BenhAn", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<BenhAn> list = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    BenhAn benhAn = doc.toObject(BenhAn.class);
                    if (benhAn != null) {
                        list.add(benhAn);
                    }
                }
                
                adapter.setData(list);
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                showEmpty(list.isEmpty());
            },
            e -> {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvBenhAn.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
