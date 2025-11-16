package com.example.doannt118.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.doannt118.R;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.repository.FirestoreRepository;
import java.util.ArrayList;
import java.util.List;

public class DanhSachBacSiActivity extends AppCompatActivity {

    private static final String TAG = "DanhSachBacSi";
    
    private ImageView btnBack;
    private EditText etSearch;
    private RecyclerView rvBacSi;
    
    private FirestoreRepository repo;
    private String maTaiKhoan;
    private List<BacSi> allBacSiList = new ArrayList<>();
    private List<BacSi> filteredList = new ArrayList<>();
    private BacSiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_sach_bac_si);

        repo = new FirestoreRepository();
        maTaiKhoan = getIntent().getStringExtra("MA_TAI_KHOAN");

        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadBacSi();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSearch = findViewById(R.id.etSearch);
        rvBacSi = findViewById(R.id.rvBacSi);
    }

    private void setupRecyclerView() {
        rvBacSi.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BacSiAdapter(filteredList, bacSi -> {
            // Mở màn hình chi tiết bác sĩ
            Intent intent = new Intent(this, ChiTietBacSiActivity.class);
            intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
            intent.putExtra("MA_BAC_SI", bacSi.getMaBacSi());
            startActivity(intent);
        });
        rvBacSi.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Search
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBacSi(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadBacSi() {
        repo.getAll("BacSi",
                querySnapshot -> {
                    allBacSiList.clear();
                    for (var doc : querySnapshot.getDocuments()) {
                        BacSi bacSi = doc.toObject(BacSi.class);
                        if (bacSi != null && "Đã xác thực".equals(bacSi.getTrangThaiXacThuc())) {
                            allBacSiList.add(bacSi);
                        }
                    }
                    filterBacSi("");
                    Log.d(TAG, "Loaded " + allBacSiList.size() + " bác sĩ");
                },
                e -> {
                    Log.e(TAG, "Error loading bác sĩ: ", e);
                    Toast.makeText(this, "Lỗi tải danh sách bác sĩ!", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterBacSi(String searchText) {
        filteredList.clear();
        
        for (BacSi bacSi : allBacSiList) {
            boolean matchSearch = searchText.isEmpty() ||
                                 (bacSi.getHoTen() != null && bacSi.getHoTen().toLowerCase().contains(searchText.toLowerCase())) ||
                                 (bacSi.getChuyenKhoa() != null && bacSi.getChuyenKhoa().toLowerCase().contains(searchText.toLowerCase()));
            
            if (matchSearch) {
                filteredList.add(bacSi);
            }
        }
        
        adapter.notifyDataSetChanged();
    }
}
