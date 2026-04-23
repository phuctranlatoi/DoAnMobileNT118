# Tính Năng Seen/Unseen Tin Nhắn

## Mô tả
Thêm tính năng hiển thị trạng thái đã xem/chưa xem tin nhắn như Messenger:
- **Trong chat**: Icon check bên cạnh tin nhắn gửi đi
- **Danh sách cuộc trò chuyện**: Icon seen cho tin nhắn cuối nếu là tin nhắn gửi đi

## Trạng thái tin nhắn
```java
public enum TrangThaiTinNhan {
    DA_GUI,    // Vừa gửi - không hiển thị icon
    DA_NHAN,   // Đã nhận - check xám
    DA_XEM     // Đã xem - check xanh
}
```

## Cải tiến thực hiện

### 1. Cập nhật Layout Tin Nhắn
**File**: `item_tin_nhan_benh_nhan.xml`
- Thêm `ImageView` cho icon seen bên cạnh thời gian
- Layout horizontal chứa thời gian + icon seen

### 2. Cập nhật Adapter Tin Nhắn
**File**: `TinNhanBacSiAdapter.java`
- Thêm `ivSeenStatus` vào `SentMessageViewHolder`
- Method `updateSeenStatus()` để cập nhật icon:
  - `DA_XEM`: Check xanh
  - `DA_NHAN`: Check xám  
  - `DA_GUI`: Ẩn icon

### 3. Cập nhật Layout Cuộc Trò Chuyện
**File**: `item_cuoc_tro_chuyen_bac_si.xml`
- Thêm `ivSeenStatusCuoi` bên cạnh tin nhắn cuối
- Chỉ hiển thị khi tin nhắn cuối là tin nhắn gửi đi

### 4. Cập nhật Adapter Cuộc Trò Chuyện
**File**: `CuocTroChuyenBacSiAdapter.java`
- Thêm `ivSeenStatusCuoi` vào ViewHolder
- Method `updateSeenStatusIcon()` để hiển thị trạng thái
- Chỉ hiển thị khi `isLaBacSiGuiCuoi() == true`

### 5. Cập nhật Models
**Files**: `CuocTroChuyenBacSi.java`, `CuocTroChuyenBenhNhan.java`
- Thêm field `trangThaiTinNhanCuoi`
- Getter/setter tương ứng

### 6. Cập nhật Logic Activities
**File**: `DanhSachTinNhanBacSiActivity.java`
- Set `trangThaiTinNhanCuoi` khi tạo cuộc trò chuyện
- Lấy từ `tinNhanCuoi.getTrangThai()`

**File**: `NhanTinBacSiActivity.java`
- Thêm `simulateMessageDelivery()` để cập nhật DA_GUI → DA_NHAN
- Gọi trong `guiTinNhan()` sau khi gửi thành công
- `markMessagesAsRead()` cập nhật DA_NHAN → DA_XEM khi vào chat

## Workflow Trạng Thái

### Khi gửi tin nhắn:
1. Tạo tin nhắn với `trangThai = DA_GUI`
2. Lưu vào Firestore
3. Sau 1 giây: Cập nhật thành `DA_NHAN` (giả lập)
4. Khi người nhận vào chat: Cập nhật thành `DA_XEM`

### Hiển thị trong chat:
- **Tin nhắn gửi đi**: Hiển thị icon seen dựa trên trạng thái
- **Tin nhắn nhận được**: Không hiển thị icon

### Hiển thị trong danh sách:
- **Tin nhắn cuối của mình**: Hiển thị icon seen
- **Tin nhắn cuối của người kia**: Không hiển thị icon

## Files Đã Cập Nhật
1. `app/src/main/res/layout/item_tin_nhan_benh_nhan.xml`
2. `app/src/main/res/layout/item_cuoc_tro_chuyen_bac_si.xml`
3. `app/src/main/java/com/example/doannt118/ui/TinNhanBacSiAdapter.java`
4. `app/src/main/java/com/example/doannt118/ui/CuocTroChuyenBacSiAdapter.java`
5. `app/src/main/java/com/example/doannt118/model/CuocTroChuyenBacSi.java`
6. `app/src/main/java/com/example/doannt118/model/CuocTroChuyenBenhNhan.java`
7. `app/src/main/java/com/example/doannt118/ui/DanhSachTinNhanBacSiActivity.java`
8. `app/src/main/java/com/example/doannt118/ui/NhanTinBacSiActivity.java`

## Test Cases
- [x] Icon seen hiển thị trong chat cho tin nhắn gửi đi
- [x] Icon seen thay đổi màu theo trạng thái (xám → xanh)
- [x] Icon seen hiển thị trong danh sách cuộc trò chuyện
- [x] Chỉ hiển thị icon cho tin nhắn gửi đi
- [x] Trạng thái cập nhật real-time qua Firestore listener
- [x] Logic tương tự cho cả bác sĩ và bệnh nhân