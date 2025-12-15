package com.example.doannt118.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.doannt118.R;
import com.example.doannt118.repository.FirestoreRepository;
import com.example.doannt118.ui.fragment.DiemDanhUongThuocFragment;
import com.example.doannt118.ui.fragment.LichSuUongThuocFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class QuanLyUongThuocActivity extends AppCompatActivity {
    
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    private String maBenhNhan;

    private FirestoreRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quan_ly_uong_thuoc);

        // Lấy mã bệnh nhân từ Intent
        maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
        
        initViews();
        setupToolbar();
        setupViewPager();
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        repository = new FirestoreRepository();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý uống thuốc");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Điểm danh");
                    tab.setIcon(R.drawable.ic_check_circle);
                    break;
                case 1:
                    tab.setText("Lịch sử");
                    tab.setIcon(R.drawable.ic_analytics);
                    break;
            }
        }).attach();
    }
    


    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return DiemDanhUongThuocFragment.newInstance(maBenhNhan);
                case 1:
                    return LichSuUongThuocFragment.newInstance(maBenhNhan);
                default:
                    return DiemDanhUongThuocFragment.newInstance(maBenhNhan);
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}