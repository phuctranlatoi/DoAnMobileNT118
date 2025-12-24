# DEMO CHATBOT BỆNH VIỆN - ROLE-BASED SYSTEM

## 🎯 TÍNH NĂNG ĐÃ IMPLEMENT

### 👥 ROLE-BASED SYSTEM
- **Bệnh nhân**: Đặt lịch, xem lịch, hủy lịch, tra cứu thông tin
- **Bác sĩ**: Xem lịch làm việc, danh sách bệnh nhân, thống kê, cập nhật lịch

### 🩺 CHỨC NĂNG BỆNH NHÂN
1. **Đặt lịch khám** (Flow hoàn chỉnh)
   - Chọn chuyên khoa → Chọn ngày → Chọn bác sĩ → Chọn giờ → Xác nhận
   - Hỗ trợ ngôn ngữ tự nhiên: "hôm nay", "ngày mai", "thứ 2 tuần sau"
   - Quick replies và buttons để dễ chọn

2. **Xem lịch khám cá nhân**
   - Hiển thị lịch sắp tới và lịch sử
   - Thông tin chi tiết: ngày, giờ, bác sĩ, trạng thái

3. **Hủy lịch khám**
   - Danh sách lịch có thể hủy
   - Xác nhận trước khi hủy
   - Cập nhật trạng thái trong database

4. **Tra cứu bác sĩ**
   - Tìm theo chuyên khoa
   - Hiển thị thông tin chi tiết bác sĩ

5. **Xem đơn thuốc**
   - Danh sách đơn thuốc
   - Hướng dẫn sử dụng

6. **Thông tin bệnh viện**
   - Giờ làm việc, bảng giá, địa chỉ, liên hệ

### 👨‍⚕️ CHỨC NĂNG BÁC SĨ
1. **Xem lịch làm việc**
   - Lịch theo ngày/tuần/tháng
   - Danh sách bệnh nhân trong ngày
   - Thống kê ca sáng/chiều

2. **Danh sách bệnh nhân hôm nay**
   - Sắp xếp theo giờ khám
   - Trạng thái từng bệnh nhân
   - Ghi chú đặc biệt

3. **Thống kê và báo cáo**
   - Số lượng bệnh nhân theo tháng
   - Doanh thu dự kiến
   - Tỷ lệ hoàn thành
   - Hiệu suất làm việc

4. **Cập nhật lịch làm việc**
   - Thêm ca làm việc mới
   - Đăng ký nghỉ phép
   - Chỉnh sửa lịch hiện tại

## 🚀 CÁCH SỬ DỤNG

### Khởi tạo Chatbot
```java
// Cho bệnh nhân
ChatbotEngine chatbot = new ChatbotEngine(context, "BN001", "benhnhan");

// Cho bác sĩ  
ChatbotEngine chatbot = new ChatbotEngine(context, "BS001", "bacsi");

// Hoặc để chatbot tự hỏi role
ChatbotEngine chatbot = new ChatbotEngine(context, "USER001", null);
```

### Xử lý tin nhắn
```java
chatbot.processMessage("Xin chào", new ChatbotEngine.ChatCallback() {
    @Override
    public void onResponse(ChatResponse response) {
        // Hiển thị response
        // Hiển thị quick replies/buttons nếu có
    }
    
    @Override
    public void onError(String error) {
        // Xử lý lỗi
    }
});
```

## 📱 VÍ DỤ CONVERSATION FLOWS

### Flow 1: Bệnh nhân đặt lịch khám
```
User: "Chào bạn"
Bot: "Xin chào! Bạn là Bệnh nhân hay Bác sĩ?"
[🏥 Tôi là Bệnh nhân] [👨‍⚕️ Tôi là Bác sĩ]

User: Clicks "🏥 Tôi là Bệnh nhân"
Bot: "Xin chào! Tôi có thể giúp bạn: đặt lịch khám, xem lịch..."
[📅 Đặt lịch khám] [👀 Xem lịch của tôi] [👨‍⚕️ Tìm bác sĩ] [🏥 Thông tin bệnh viện]

User: Clicks "📅 Đặt lịch khám"
Bot: "Bạn muốn khám chuyên khoa nào?"
[🫀 Nội khoa] [❤️ Tim mạch] [🩹 Da liễu] [👶 Nhi khoa]

User: Clicks "❤️ Tim mạch"
Bot: "Bạn muốn khám vào ngày nào?"
[Hôm nay] [Ngày mai] [Thứ 2 tuần sau] [Chọn ngày khác]

User: Clicks "Ngày mai"
Bot: "Danh sách bác sĩ Tim mạch có lịch ngày mai:"
[BS. Nguyễn Văn A] [BS. Trần Thị B] [BS. Lê Văn C]

User: Clicks "BS. Nguyễn Văn A"  
Bot: "Khung giờ trống của BS. Nguyễn Văn A:"
[8:00-8:30] [9:30-10:00] [14:00-14:30] [15:30-16:00]

User: Clicks "14:00-14:30"
Bot: "Xác nhận thông tin đặt lịch: Tim mạch - BS. Nguyễn Văn A - Ngày mai 14:00"
[✅ Xác nhận đặt lịch] [✏️ Chỉnh sửa] [❌ Hủy bỏ]

User: Clicks "✅ Xác nhận đặt lịch"
Bot: "🎉 Đặt lịch thành công! Mã lịch: LK240001..."
```

### Flow 2: Bác sĩ xem lịch làm việc
```
User: "Tôi là bác sĩ, xem lịch hôm nay"
Bot: "📅 LỊCH LÀM VIỆC HÔM NAY - 25/12/2024
👥 Tổng số bệnh nhân: 6
🌅 CA SÁNG: 3 bệnh nhân
🌆 CA CHIỀU: 3 bệnh nhân
💰 Doanh thu dự kiến: 1,200,000 VNĐ"
[📊 Xem thống kê] [📅 Xem ngày khác] [⚙️ Cập nhật lịch] [👥 Chi tiết bệnh nhân]
```

### Flow 3: Bệnh nhân hủy lịch
```
User: "Tôi muốn hủy lịch khám"
Bot: "Danh sách lịch khám có thể hủy:
1. 26/12/2024 - 14:00 (Đã xác nhận)
2. 28/12/2024 - 09:00 (Chờ xác nhận)"
[1. 26/12/2024] [2. 28/12/2024]

User: Clicks "1. 26/12/2024"
Bot: "⚠️ XÁC NHẬN HỦY LỊCH KHÁM
Ngày: 26/12/2024 - 14:00
Bạn có chắc chắn muốn hủy?"
[✅ Xác nhận hủy] [❌ Không hủy] [🔄 Chọn lịch khác]

User: Clicks "✅ Xác nhận hủy"
Bot: "✅ Đã hủy lịch khám thành công!"
```

## 🎨 UI COMPONENTS CẦN IMPLEMENT

### Quick Replies
```xml
<HorizontalScrollView>
    <LinearLayout android:orientation="horizontal">
        <Button android:text="Hôm nay" />
        <Button android:text="Ngày mai" />
        <Button android:text="Thứ 2" />
    </LinearLayout>
</HorizontalScrollView>
```

### Action Buttons
```xml
<LinearLayout android:orientation="vertical">
    <Button 
        android:text="📅 Đặt lịch khám"
        android:background="@color/primary"
        android:textColor="@color/white" />
    <Button 
        android:text="👀 Xem lịch của tôi"
        android:background="@color/secondary" />
</LinearLayout>
```

### Doctor Cards (RecyclerView)
```xml
<androidx.cardview.widget.CardView>
    <LinearLayout>
        <ImageView android:src="@drawable/doctor_avatar" />
        <TextView android:text="BS. Nguyễn Văn A" />
        <TextView android:text="Chuyên khoa Tim mạch" />
        <TextView android:text="15 năm kinh nghiệm" />
        <Button android:text="Đặt lịch" />
    </LinearLayout>
</androidx.cardview.widget.CardView>
```

## 🔧 CONFIGURATION

### Firestore Collections
- `LichKham`: Lịch khám bệnh
- `BacSi`: Thông tin bác sĩ  
- `BenhNhan`: Thông tin bệnh nhân
- `LichLamViec`: Lịch làm việc bác sĩ
- `DonThuoc`: Đơn thuốc
- `ChuyenKhoa`: Danh sách chuyên khoa

### Intent Keywords (IntentDetector)
- Đặt lịch: "đặt lịch", "book", "hẹn", "appointment"
- Xem lịch: "xem lịch", "lịch khám", "schedule"
- Hủy lịch: "hủy lịch", "cancel", "bỏ lịch"
- Bác sĩ: "bác sĩ", "doctor", "chuyên khoa"

## 🚨 LƯU Ý QUAN TRỌNG

1. **Role Management**: Luôn kiểm tra role trước khi thực hiện action
2. **State Management**: Sử dụng ConversationContext để track flow
3. **Error Handling**: Fallback graceful khi có lỗi
4. **Natural Language**: Hỗ trợ nhiều cách diễn đạt khác nhau
5. **User Experience**: Luôn có quick replies để dễ tương tác

## 🎯 NEXT STEPS

1. **Authentication**: Thêm xác thực OTP/mật khẩu
2. **Push Notifications**: Nhắc nhở lịch khám
3. **Payment Integration**: Thanh toán online
4. **Video Call**: Tích hợp khám online
5. **AI Enhancement**: Cải thiện NLU với ML models
6. **Multi-language**: Hỗ trợ tiếng Anh
7. **Voice Input**: Nhận diện giọng nói
8. **Analytics**: Theo dõi usage patterns