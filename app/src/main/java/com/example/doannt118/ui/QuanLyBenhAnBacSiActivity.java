package com.example.doannt118.ui;

import android.content.Intent;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class QuanLyBenhAnBacSiActivity extends AppCompatActivity {
    private RecyclerView rvBenhAn;
    private BenhAnBacSiAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private TabLayout tabLayout;
    private FloatingActionButton fabTaoBenhAn;
    
    private FirestoreRepository repository;
    private String maBacSi;
    private String currentTab = "CHO_KHAM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_benh_an_bac_si);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupTabs();
        loadBacSiInfo();
    }

    private void initViews() {
        rvBenhAn = findViewById(R.id.rvBenhAn);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tabLayout = findViewById(R.id.tabLayout);
        fabTaoBenhAn = findViewById(R.id.fabTaoBenhAn);
        
        repository = new FirestoreRepository();
        
        swipeRefresh.setOnRefreshListener(() -> loadBenhAn());
        fabTaoBenhAn.setOnClickListener(v -> openTaoBenhAn());
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
        adapter = new BenhAnBacSiAdapter(this);
        rvBenhAn.setLayoutManager(new LinearLayoutManager(this));
        rvBenhAn.setAdapter(adapter);
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition() == 0 ? "CHO_KHAM" : "DA_KHAM";
                loadBenhAn();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadBacSiInfo() {
        // Ưu tiên lấy từ Intent trước
        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        
        if (maBacSi != null && !maBacSi.isEmpty()) {
            loadBenhAn();
            return;
        }
        
        // Nếu không có trong Intent, load từ Firebase Auth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        showLoading(true);
        
        repository.getByField("BacSi", "maTaiKhoan", userId,
            querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                    maBacSi = doc.getString("maBacSi");
                    loadBenhAn();
                } else {
                    showLoading(false);
                    Toast.makeText(this, "Không tìm thấy thông tin bác sĩ", Toast.LENGTH_SHORT).show();
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
        
        repository.getByField("BenhAn", "maBacSi", maBacSi,
            querySnapshot -> {
                List<BenhAn> list = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    BenhAn benhAn = doc.toObject(BenhAn.class);
                    if (benhAn != null) {
                        // Lọc theo tab
                        boolean isCompleted = benhAn.getChanDoan() != null && 
                                            !benhAn.getChanDoan().isEmpty();
                        if (currentTab.equals("CHO_KHAM") && !isCompleted) {
                            list.add(benhAn);
                        } else if (currentTab.equals("DA_KHAM") && isCompleted) {
                            list.add(benhAn);
                        }
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
                Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void openTaoBenhAn() {
        Intent intent = new Intent(this, TaoBenhAnActivity.class);
        intent.putExtra("maBacSi", maBacSi);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (maBacSi != null) {
            loadBenhAn();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmpty(boolean show) {
        tvEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        rvBenhAn.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
