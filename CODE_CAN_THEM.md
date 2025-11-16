# Code cần thêm để hoàn thiện

## 1. Cập nhật QuanLyLichLamViecActivity.java

Thay đổi layout và logic:
```java
setContentView(R.layout.activity_quan_ly_lich_lam_viec_new);

// Init views
calendarView = findViewById(R.id.calendarView);
tvTongSoLich = findViewById(R.id.tvTongSoLich);
tvTongBenhNhan = findViewById(R.id.tvTongBenhNhan);
rvLichLamViec = findViewById(R.id.rvLichLamViec);
tvEmpty = findViewById(R.id.tvEmpty);

// Calendar listener
calendarView.setOnDateChangeListener((view, year, month, day) -> {
    Calendar cal = Calendar.getInstance();
    cal.set(year, month, day);
    selectedDate = cal.getTime();
    loadLichTheoNgay();
});

// Load lịch theo ngày
private void loadLichTheoNgay() {
    // Tạo lịch tự động theo quy tắc (giống ChiTietBacSiActivity)
    // Load số lượng bệnh nhân cho mỗi khung giờ
    // Cập nhật thống kê
}
```

## 2. Cập nhật item_lich_lam_viec_new.xml adapter

Tạo adapter mới hiển thị số bệnh nhân:
```java
public class LichLamViecNewAdapter extends RecyclerView.Adapter {
    // Hiển thị ca làm việc
    // Load và hiển thị số bệnh nhân (X/6)
    // Badge màu: Xanh (còn trống) / Đỏ (đầy)
    // Click → XemChiTietLichKhamActivity
}
```

## 3. Thêm menu "Xác nhận lịch khám" vào MainBacSiActivity

Trong layout activity_main_bacsi.xml, thêm card:
```xml
<androidx.cardview.widget.CardView
    android:id="@+id/cardXacNhanLichKham"
    ...>
    <TextView android:text="Xác nhận lịch khám" />
</androidx.cardview.widget.CardView>
```

Trong MainBacSiActivity.java:
```java
cardXacNhanLichKham.setOnClickListener(v -> {
    Intent intent = new Intent(this, XacNhanLichKhamActivity.class);
    intent.putExtra("MA_TAI_KHOAN", maTaiKhoan);
    intent.putExtra("MA_BAC_SI", maBacSi);
    startActivity(intent);
});
```

## 4. Tạo XacNhanLichKhamActivity.java

```java
public class XacNhanLichKhamActivity extends AppCompatActivity {
    // Load danh sách lịch khám của bác sĩ
    // Filter: CHO / XAC_NHAN
    // Adapter với nút Xác nhận/Hủy
}
```

## 5. Tạo XacNhanLichKhamAdapter.java

```java
public class XacNhanLichKhamAdapter extends RecyclerView.Adapter {
    // Hiển thị thông tin lịch khám
    // Nút Xác nhận → update trangThai = "XAC_NHAN"
    // Nút Hủy → update trangThai = "HUY"
}
```

## 6. Thêm vào AndroidManifest.xml

```xml
<activity android:name=".ui.XacNhanLichKhamActivity" />
```

## Các file đã tạo sẵn:
✅ activity_quan_ly_lich_lam_viec_new.xml (layout đẹp)
✅ item_lich_lam_viec_new.xml
✅ activity_xac_nhan_lich_kham.xml
✅ item_xac_nhan_lich_kham.xml
✅ circle_background.xml

## Bước tiếp theo:
1. Copy code từ QuanLyLichLamViecActivity cũ
2. Thay đổi layout và logic theo hướng dẫn trên
3. Tạo 2 Activity và 2 Adapter mới
4. Test chức năng
