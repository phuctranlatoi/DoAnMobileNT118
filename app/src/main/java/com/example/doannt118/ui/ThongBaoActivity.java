package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.ThongBao;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ThongBaoActivity extends AppCompatActivity {
    private RecyclerView rvThongBao;
    private ThongBaoAdapter adapter;
    private List<ThongBao> thongBaoList;
    private FirestoreRepository repo;
    private String maBenhNhan;
    private ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);

        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        repo = new FirestoreRepository();

        setupToolbar();
        initViews();
        listenToThongBao();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thông báo");
        }
    }

    private void initViews() {
        rvThongBao = findViewById(R.id.rvThongBao);
        thongBaoList = new ArrayList<>();
        adapter = new ThongBaoAdapter(thongBaoList, this::markAsRead);
        rvThongBao.setLayoutManager(new LinearLayoutManager(this));
        rvThongBao.setAdapter(adapter);
    }

    private void listenToThongBao() {
        listenerRegistration = repo.getCollection("ThongBao")
                .whereEqualTo("maBenhNhan", maBenhNhan)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        thongBaoList.clear();
                        List<ThongBao> tempList = snapshots.toObjects(ThongBao.class);
                        
                        // Sắp xếp theo thời gian trong code thay vì query
                        tempList.sort((t1, t2) -> {
                            if (t1.getThoiGianGui() == null) return 1;
                            if (t2.getThoiGianGui() == null) return -1;
                            return t2.getThoiGianGui().compareTo(t1.getThoiGianGui());
                        });
                        
                        thongBaoList.addAll(tempList);
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void markAsRead(ThongBao thongBao) {
        // Mở chi tiết thông báo
        android.content.Intent intent = new android.content.Intent(this, ChiTietThongBaoActivity.class);
        intent.putExtra("maThongBao", thongBao.getMaThongBao());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
