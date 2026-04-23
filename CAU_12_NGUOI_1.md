# Câu 12: Sơ đồ tổng quan và tính năng đồ án - Người 1

## 🏥 SƠ ĐỒ TỔNG QUAN HỆ THỐNG

### Kiến trúc hệ thống:
```
┌─────────────────────────────────────────────────────────────┐
│                    HỆ THỐNG QUẢN LÝ BỆNH VIỆN                │
├─────────────────────────────────────────────────────────────┤
│  Frontend: Android App (Java)                              │
│  ├── UI Layer: Activities, Fragments, Adapters             │
│  ├── Business Logic: Models, Utils, Managers               │
│  └── Services: Firebase, Stringee, Notifications           │
├─────────────────────────────────────────────────────────────┤
│  Backend: Firebase Ecosystem                               │
│  ├── Authentication: Firebase Auth                         │
│  ├── Database: Cloud Firestore                            │
│  ├── Storage: Firebase Storage                             │
│  └── Messaging: Firebase Cloud Messaging                   │
├─────────────────────────────────────────────────────────────┤
│  Third-party Services:                                     │
│  ├── Stringee SDK: Voice/Video Calling                    │
│  ├── Gemini AI: Chatbot Intelligence                      │
│  └── QR Payment: Thanh toán trực tuyến                    │
└─────────────────────────────────────────────────────────────┘
```

### Phân quyền người dùng:
- **BỆNH NHÂN**: Đặt lịch, xem bệnh án, chat với bác sĩ, quản lý uống thuốc
- **BÁC SĨ**: Quản lý lịch làm việc, khám bệnh, kê đơn thuốc, chat với bệnh nhân
- **ADMIN**: Quản lý tài khoản, duyệt bác sĩ, thống kê hệ thống

## 🚀 DANH SÁCH TÍNH NĂNG ĐÃ THỰC HIỆN

### 👥 **QUẢN LÝ TÀI KHOẢN & XÁC THỰC**
1. **Đăng ký/Đăng nhập** - Firebase Authentication với mã hóa BCrypt
2. **Phân quyền 3 cấp** - Bệnh nhân, Bác sĩ, Admin với giao diện riêng biệt
3. **Quản lý profile** - Cập nhật thông tin, avatar, chuyên khoa bác sĩ
4. **Xác thực bác sĩ** - Admin duyệt hồ sơ bác sĩ trước khi hoạt động

### 📅 **HỆ THỐNG ĐẶT LỊCH THÔNG MINH**
5. **Smart Appointment Booking** - Tự động tạo slot 30 phút từ lịch làm việc
6. **Quản lý lịch làm việc** - Bác sĩ tự tạo/sửa lịch theo ca sáng/chiều
7. **Xác nhận lịch khám** - Workflow: Đặt → Chờ → Xác nhận → Hoàn thành
8. **Kiểm tra slot trống** - Real-time hiển thị "X/Y slots available"

### 🤖 **CHATBOT THÔNG MINH**
9. **Trợ lý ảo AI** - Tích hợp Gemini AI với 21 intents hỗ trợ
10. **Role-based Chatbot** - Phân biệt chức năng cho bệnh nhân/bác sĩ
11. **Natural Language Processing** - Hiểu tiếng Việt, xử lý lỗi chính tả
12. **Đặt lịch qua chatbot** - End-to-end booking workflow trong chat

### 💬 **HỆ THỐNG NHẮN TIN REAL-TIME**
13. **Chat bác sĩ - bệnh nhân** - Messenger-style với Firebase Realtime
14. **Push notifications** - Thông báo tin nhắn mới tức thời
15. **Payment integration** - Thanh toán QR code để mở khóa chat
16. **Unread messages** - Đếm tin nhắn chưa đọc, trạng thái online

### 📞 **TÍNH NĂNG GỌI ĐIỆN**
17. **Voice calling** - Stringee SDK app-to-app calling
18. **Video calling** - HD video call với camera controls
19. **Incoming call UI** - Giao diện nhận cuộc gọi như điện thoại
20. **Call management** - Quản lý trạng thái cuộc gọi, lịch sử

### 💊 **QUẢN LÝ THUỐC & ĐIỀU TRỊ**
21. **Kê đơn thuốc điện tử** - Bác sĩ kê đơn với liều lượng, cách dùng
22. **Nhắc nhở uống thuốc** - Alarm tự động theo ca sáng/trưa/chiều
23. **Điểm danh uống thuốc** - Bệnh nhân xác nhận đã uống, thống kê tuân thủ
24. **Lịch sử uống thuốc** - Theo dõi tỷ lệ tuân thủ điều trị

### 🏥 **QUẢN LÝ KHÁM BỆNH**
25. **Tạo bệnh án điện tử** - Ghi nhận triệu chứng, chẩn đoán, điều trị
26. **Nhập mã khám** - Bác sĩ nhập mã 6 số để truy cập hồ sơ bệnh nhân
27. **Quản lý hóa đơn** - Tính toán chi phí khám, dịch vụ, thuốc
28. **Thống kê báo cáo** - Dashboard cho bác sĩ và admin

### 🔔 **THÔNG BÁO & GIAO TIẾP**
29. **Firebase Cloud Messaging** - Push notification đa nền tảng
30. **Thông báo hệ thống** - Lịch khám, nhắc uống thuốc, tin nhắn mới
31. **Gửi thông báo tùy chỉnh** - Bác sĩ/Admin gửi thông báo cho bệnh nhân

## 🎯 **ĐÓNG GÓP QUAN TRỌNG NHẤT CỦA TÔI**

### **Hệ thống Chatbot AI thông minh**

**Tại sao quan trọng:**

1. **Tự động hóa quy trình** - Giảm 70% thời gian đặt lịch thủ công
2. **Trải nghiệm người dùng** - Interface như Shopee/Tiki, thân thiện với người Việt
3. **Tích hợp dữ liệu thực** - Kết nối trực tiếp Firestore, không cần mock data
4. **Xử lý ngôn ngữ tự nhiên** - Hiểu câu hỏi tiếng Việt, xử lý lỗi chính tả
5. **Phân quyền thông minh** - Tự động nhận diện vai trò và cung cấp chức năng phù hợp

**Technical Implementation:**
- **ChatbotEngine.java** (2,100+ lines): Core processing với 21 intents
- **IntentDetector.java**: NLP với fuzzy matching và pattern recognition  
- **Gemini AI Integration**: Fallback cho câu hỏi phức tạp
- **Real-time Firestore**: Đặt lịch end-to-end trong chatbot
- **Conversation Context**: Quản lý trạng thái đối thoại multi-step

**Impact:**
- Giảm tải cho nhân viên y tế
- Tăng accessibility cho người cao tuổi
- Cải thiện patient experience
- Tự động hóa quy trình bệnh viện

Chatbot không chỉ là tính năng phụ mà là **trung tâm tương tác** của toàn bộ hệ thống, giúp người dùng truy cập mọi chức năng một cách tự nhiên và hiệu quả.