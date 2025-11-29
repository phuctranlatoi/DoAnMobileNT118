package com.example.doannt118.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doannt118.R;
import com.example.doannt118.model.BenhNhan;
import com.example.doannt118.model.ThongBao;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class GuiThongBaoActivity extends AppCompatActivity {
    private RecyclerView rvBenhNhan;
    private BenhNhanSelectAdapter adapter;
    private List<BenhNhan> benhNhanList;
    private Spinner spinnerLoaiThongBao;
    private EditText edtTieuDe, edtNoiDung;
    private Button btnGuiThongBao;
    private FirestoreRepository repo;
    private String maBacSi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui_thong_bao);

        maBacSi = getIntent().getStringExtra("MA_BAC_SI");
        repo = new FirestoreRepository();

        setupToolbar();
        initViews();
        setupSpinner();
        loadBenhNhan();
        setupListeners();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gửi thông báo");
        }
    }

    private void initViews() {
        rvBenhNhan = findViewById(R.id.rvBenhNhan);
        spinnerLoaiThongBao = findViewById(R.id.spinnerLoaiThongBao);
        edtTieuDe = findViewById(R.id.edtTieuDe);
        edtNoiDung = findViewById(R.id.edtNoiDung);
        btnGuiThongBao = findViewById(R.id.btnGuiThongBao);

        benhNhanList = new ArrayList<>();
        adapter = new BenhNhanSelectAdapter(benhNhanList);
        rvBenhNhan.setLayoutManager(new LinearLayoutManager(this));
        rvBenhNhan.setAdapter(adapter);
    }

    private void setupSpinner() {
        String[] loaiThongBao = {"Lịch hẹn sắp tới", "Nhắc nhở uống thuốc", "Thông báo chung"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, loaiThongBao);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLoaiThongBao.setAdapter(spinnerAdapter);
    }

    private void loadBenhNhan() {
        repo.getAll("BenhNhan",
                querySnapshot -> {
                    benhNhanList.clear();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        BenhNhan bn = doc.toObject(BenhNhan.class);
                        if (bn != null) {
                            benhNhanList.add(bn);
                        }
                    }
                    adapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupListeners() {
        btnGuiThongBao.setOnClickListener(v -> guiThongBao());
    }

    private void guiThongBao() {
        String tieuDe = edtTieuDe.getText().toString().trim();
        String noiDung = edtNoiDung.getText().toString().trim();

        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<BenhNhan> selectedBenhNhan = adapter.getSelectedBenhNhan();
        if (selectedBenhNhan.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 bệnh nhân!", Toast.LENGTH_SHORT).show();
            return;
        }

        String loaiThongBao = getLoaiThongBao();
        int count = 0;

        for (BenhNhan bn : selectedBenhNhan) {
            String maThongBao = "TB" + System.currentTimeMillis() + "_" + bn.getMaBenhNhan();
            ThongBao thongBao = new ThongBao(
                maThongBao,
                bn.getMaBenhNhan(),
                maBacSi,
                tieuDe,
                noiDung,
                loaiThongBao,
                Timestamp.now(),
                false
            );

            repo.addDocument("ThongBao", maThongBao, thongBao,
                    aVoid -> {},
                    e -> Toast.makeText(this, "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            count++;
        }

        Toast.makeText(this, "Đã gửi " + count + " thông báo!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getLoaiThongBao() {
        int position = spinnerLoaiThongBao.getSelectedItemPosition();
        switch (position) {
            case 0: return "LICH_HEN";
            case 1: return "NHAC_THUOC";
            default: return "THONG_BAO_CHUNG";
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
