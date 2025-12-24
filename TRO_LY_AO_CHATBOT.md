# Trợ Lý Ảo Chatbot - Giống Tép Shopee

## Tổng quan

Đã nâng cấp hệ thống chatbot để hoạt động giống như "Tép" của Shopee với các tính năng:

### 1. Action Buttons
- Hiển thị các nút bấm để người dùng chọn nhanh
- Nút primary được highlight màu chính
- Hỗ trợ icon cho mỗi nút

### 2. Doctor Cards Carousel
- Hiển thị danh sách bác sĩ dạng card có thể vuốt ngang
- Mỗi card hiển thị: tên, chuyên khoa, kinh nghiệm, ca làm việc
- Nút "Đặt lịch khám" trên mỗi card

### 3. Quick Replies
- Các nút gợi ý nhanh ở cuối màn hình
- Tự động ẩn sau khi chọn

## Các Intent được hỗ trợ

### Cho Bệnh Nhân:
1. **Đặt lịch khám** - "Tôi muốn đặt lịch khám"
2. **Xem lịch khám** - "Xem lịch khám của tôi"
3. **Xem lịch bác sĩ** - "Hôm nay có bác sĩ nào?", "Xem lịch bác sĩ ngày mai"
4. **Tìm bác sĩ** - "Tìm bác sĩ tim mạch", "Bác sĩ nội khoa"
5. **Xem đơn thuốc** - "Xem đơn thuốc của tôi"

### Cho Bác Sĩ:
1. **Thống kê bệnh nhân**
2. **Xem lịch làm việc**
3. **Tra cứu thuốc**

## Files đã tạo/sửa

### Java Files:
- `ChatMessage.java` - Thêm ActionButton, MessageType mới
- `ChatResponse.java` - Thêm hỗ trợ action buttons, doctor cards
- `ChatAdapter.java` - Thêm ViewHolder cho action buttons và doctor carousel
- `ChatActivity.java` - Xử lý action click và doctor select
- `ChatbotEngine.java` - Thêm handleViewDoctorSchedule, cập nhật handleGreeting
- `IntentDetector.java` - Thêm intent XEM_LICH_BAC_SI

### Layout Files:
- `item_chat_bot_actions.xml` - Layout tin nhắn bot với action buttons
- `item_action_button.xml` - Layout cho mỗi action button
- `item_doctor_card.xml` - Layout card bác sĩ
- `item_chat_doctor_carousel.xml` - Layout carousel bác sĩ

### Drawable Files:
- `ic_calendar.xml` - Icon lịch
- `ic_list.xml` - Icon danh sách
- `ic_search.xml` - Icon tìm kiếm
- `ic_schedule.xml` - Icon thời gian
- `ic_star.xml` - Icon sao
- `bg_schedule_info.xml` - Background thông tin lịch
- `bg_circle_primary.xml` - Background tròn

## Cách sử dụng

### Mở Chatbot từ MainBenhNhanActivity:
```java
Intent intent = new Intent(this, ChatActivity.class);
intent.putExtra("MA_BENH_NHAN", maBenhNhan);
startActivity(intent);
```

### Flow đặt lịch:
1. User chọn "Đặt lịch khám"
2. Bot hỏi ngày khám
3. User chọn ngày (hôm nay, ngày mai, thứ X)
4. Bot hiển thị danh sách bác sĩ có lịch (dạng cards)
5. User chọn bác sĩ
6. Bot hiển thị khung giờ trống
7. User chọn giờ
8. Bot hiển thị xác nhận
9. User xác nhận → Đặt lịch thành công

### Flow xem lịch bác sĩ:
1. User hỏi "Hôm nay có bác sĩ nào?"
2. Bot query LichLamViec theo ngày
3. Bot hiển thị danh sách bác sĩ dạng cards với ca làm việc
4. User có thể đặt lịch trực tiếp từ card
