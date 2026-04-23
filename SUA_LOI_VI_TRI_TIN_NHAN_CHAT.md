# Sửa Lỗi Vị Trí Tin Nhắn Trong Chat

## Vấn đề
- **Tất cả tin nhắn** đều hiển thị bên phải (như người gửi)
- **Không phân biệt** tin nhắn của mình và tin nhắn của người kia
- **Logic sai**: Adapter chỉ dựa vào `LoaiTinNhan` (BAC_SI/BENH_NHAN) thay vì xem **ai đang sử dụng app**

## Nguyên nhân
Trong `TinNhanBacSiAdapter.getItemViewType()`:
```java
// LOGIC CŨ - SAI
return tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN ? 
       TYPE_BENH_NHAN : TYPE_BAC_SI;
```

Logic này không quan tâm **ai đang xem** tin nhắn, chỉ dựa vào loại tin nhắn.

## Giải pháp

### 1. Thay đổi Logic Adapter
```java
// LOGIC MỚI - ĐÚNG
if (isDoctorView) {
    // Bác sĩ view: tin nhắn của bác sĩ là gửi đi, của bệnh nhân là nhận được
    return tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BAC_SI ? 
           TYPE_SENT : TYPE_RECEIVED;
} else {
    // Bệnh nhân view: tin nhắn của bệnh nhân là gửi đi, của bác sĩ là nhận được
    return tinNhan.getLoaiTinNhan() == TinNhanBacSi.LoaiTinNhan.BENH_NHAN ? 
           TYPE_SENT : TYPE_RECEIVED;
}
```

### 2. Đổi Tên Constants
- `TYPE_BENH_NHAN` → `TYPE_SENT` (tin nhắn gửi đi - bên phải)
- `TYPE_BAC_SI` → `TYPE_RECEIVED` (tin nhắn nhận được - bên trái)

### 3. Đổi Tên ViewHolders
- `BenhNhanViewHolder` → `SentMessageViewHolder`
- `BacSiViewHolder` → `ReceivedMessageViewHolder`

### 4. Cập nhật Constructor
```java
// Trước
public TinNhanBacSiAdapter() {
    // ...
}

// Sau
public TinNhanBacSiAdapter(boolean isDoctorView) {
    this.isDoctorView = isDoctorView;
    // ...
}
```

### 5. Cập nhật Activity
```java
// Trong setupRecyclerView()
adapter = new TinNhanBacSiAdapter(isDoctorView);
```

### 6. Cập nhật Avatar Logic
```java
// Trong ReceivedMessageViewHolder.bind()
if (isDoctorView) {
    // Bác sĩ view: tin nhắn nhận được từ bệnh nhân
    ivAvatar.setImageResource(R.drawable.ic_patient);
} else {
    // Bệnh nhân view: tin nhắn nhận được từ bác sĩ
    ivAvatar.setImageResource(R.drawable.ic_doctor);
}
```

## Kết quả
- **Bác sĩ**: 
  - Tin nhắn của bác sĩ → bên phải (xanh)
  - Tin nhắn của bệnh nhân → bên trái (xám) với avatar bệnh nhân
- **Bệnh nhân**:
  - Tin nhắn của bệnh nhân → bên phải (xanh)
  - Tin nhắn của bác sĩ → bên trái (xám) với avatar bác sĩ

## Files Đã Sửa
1. `app/src/main/java/com/example/doannt118/ui/TinNhanBacSiAdapter.java`
2. `app/src/main/java/com/example/doannt118/ui/NhanTinBacSiActivity.java`

## Test Cases
- [x] Bác sĩ thấy tin nhắn của mình bên phải
- [x] Bác sĩ thấy tin nhắn của bệnh nhân bên trái
- [x] Bệnh nhân thấy tin nhắn của mình bên phải  
- [x] Bệnh nhân thấy tin nhắn của bác sĩ bên trái
- [x] Avatar hiển thị đúng cho từng loại tin nhắn