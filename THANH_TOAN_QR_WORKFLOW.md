# Workflow Thanh toán QR Code - Hệ thống Nhắn tin Bác sĩ

## 🔄 **Workflow hoàn chỉnh:**

### 1. **Chọn bác sĩ và gói tư vấn**
- Bệnh nhân chọn bác sĩ từ danh sách
- Xem thông tin chi tiết bác sĩ
- Chọn 1 trong 3 gói tư vấn (Cơ bản/Nâng cao/Cao cấp)
- Bấm "Đăng ký nhắn tin"

### 2. **Chọn phương thức thanh toán**
- Màn hình `ThanhToanActivity`
- Chọn: Ví điện tử / Thẻ tín dụng / Chuyển khoản
- Bấm "Thanh toán ngay"

### 3. **Thanh toán QR Code** ⭐
- Chuyển đến `ThanhToanQRActivity`
- Hiển thị:
  - Thông tin đơn hàng (Bác sĩ, Gói, Giá)
  - **Mã QR Code** (của bạn)
  - Hướng dẫn thanh toán
- **Tự động xử lý sau 5 giây:**
  - Giây 1-2: Hiển thị QR code
  - Giây 3-4: "Đang xử lý thanh toán..."
  - Giây 5: "Thanh toán thành công!"

### 4. **Tự động tạo cuộc trò chuyện**
- Cập nhật trạng thái đăng ký: "Đã thanh toán"
- **Tạo tin nhắn chào mừng từ bác sĩ:**
  ```
  "Chào bạn! Tôi là BS. [Tên]. Cảm ơn bạn đã đăng ký gói tư vấn. 
  Tôi sẵn sàng hỗ trợ và tư vấn cho bạn. Bạn có thể chia sẻ với tôi 
  về tình trạng sức khỏe hoặc những thắc mắc cần tư vấn."
  ```

### 5. **Chuyển đến màn hình chat**
- Tự động mở `NhanTinBacSiActivity`
- Hiển thị tin nhắn chào mừng từ bác sĩ
- Bệnh nhân có thể bắt đầu nhắn tin ngay

### 6. **Cuộc trò chuyện xuất hiện trong danh sách**
- Khi bệnh nhân vào "Tin nhắn" từ bottom nav
- Sẽ thấy cuộc trò chuyện với bác sĩ vừa đăng ký
- Tin nhắn cuối: Tin nhắn chào mừng từ bác sĩ

## 🎨 **UI/UX Features:**

### **Màn hình QR Code:**
- ✅ Thông tin đơn hàng rõ ràng
- ✅ QR Code giả lập (của bạn)
- ✅ Hướng dẫn thanh toán chi tiết
- ✅ Trạng thái xử lý real-time
- ✅ Nút hủy thanh toán

### **Tự động hóa:**
- ✅ Không cần bấm nút "Xác nhận thanh toán"
- ✅ Tự động chuyển màn hình sau khi "thanh toán"
- ✅ Tự động tạo tin nhắn đầu tiên
- ✅ Tự động mở chat

## 🔧 **Technical Implementation:**

### **Files mới:**
- `ThanhToanQRActivity.java` - Xử lý thanh toán QR
- `activity_thanh_toan_qr.xml` - UI thanh toán QR
- `qr_momo_sample.xml` - QR code vector drawable
- `qr_border.xml`, `cancel_button_background.xml` - UI components

### **Logic thanh toán:**
```java
// Tự động thanh toán sau 5 giây
handler.postDelayed(() -> {
    // 1. Cập nhật trạng thái đăng ký
    // 2. Tạo tin nhắn chào mừng
    // 3. Chuyển đến chat
}, 5000);
```

### **Tạo tin nhắn chào mừng:**
```java
Map<String, Object> tinNhanChaoMung = new HashMap<>();
tinNhanChaoMung.put("loaiTinNhan", "BAC_SI");
tinNhanChaoMung.put("noiDung", "Chào bạn! Tôi là BS. ...");
// Lưu vào collection TinNhanBacSi
```

## 🚀 **Demo Workflow:**
1. Chọn bác sĩ → Chọn gói → Chọn thanh toán
2. **Hiển thị QR của bạn** 📱
3. Đợi 5 giây (giả lập quét QR)
4. "Thanh toán thành công!" ✅
5. Tự động vào chat với tin nhắn chào mừng từ bác sĩ 💬

## 📱 **User Experience:**
- **Realistic:** Giống thanh toán thực tế với QR code
- **Automatic:** Không cần thao tác phức tạp
- **Seamless:** Chuyển đổi mượt mà giữa các màn hình
- **Engaging:** Có feedback và trạng thái rõ ràng

Perfect cho đồ án demo! 🎉