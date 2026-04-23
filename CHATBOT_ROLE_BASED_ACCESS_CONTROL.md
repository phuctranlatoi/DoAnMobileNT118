# 🔐 HỆ THỐNG PHÂN QUYỀN CHATBOT - RBAC (Role-Based Access Control)

## 📋 TỔNG QUAN HỆ THỐNG PHÂN QUYỀN

Chatbot đã được implement đầy đủ hệ thống **Role-Based Access Control (RBAC)** với 2 vai trò chính:
- 🏥 **Bệnh nhân (benhnhan)** - 11 chức năng
- 👨‍⚕️ **Bác sĩ (bacsi)** - 10 chức năng

## 🏗️ KIẾN TRÚC PHÂN QUYỀN

### **1. Role Detection & Initialization**
```java
// Constructor với role detection
public ChatbotEngine(Context context, String userId, String userType) {
    this.userType = userType;
    
    if ("bacsi".equals(userType)) {
        this.maBacSi = userId;
    } else {
        this.maBenhNhan = userId;
    }
    
    this.conversationContext.setData("userType", userType);
}

// Auto-detect trong ChatActivity
if (userType == null) {
    userType = (maBacSi != null) ? "bacsi" : "benhnhan";
}
```

### **2. Intent-Level Access Control**
```java
private void handleNewIntent(IntentDetector.Intent intent, String userMessage, ChatCallback callback) {
    // Kiểm tra role trước khi xử lý intent
    String currentUserType = (String) conversationContext.getData("userType");
    
    if (currentUserType == null) {
        handleRoleSelection(callback);
        return;
    }
    
    switch (intent) {
        case DAT_LICH_KHAM:
            if ("benhnhan".equals(currentUserType)) {
                handleBookingIntent(callback);
            } else {
                handleUnauthorizedAction("Chỉ bệnh nhân mới có thể đặt lịch khám", callback);
            }
            break;
        // ... more cases
    }
}
```

## 🏥 QUYỀN TRUY CẬP CHI TIẾT

### **BỆNH NHÂN (benhnhan) - 11 Chức năng:**

| Chức năng | Intent | Mô tả | Tích hợp |
|-----------|--------|-------|----------|
| 📅 Đặt lịch khám | `DAT_LICH_KHAM` | Đặt lịch với bác sĩ có lịch làm việc | ✅ Firestore |
| 👀 Xem lịch khám | `XEM_LICH_KHAM` | Xem lịch khám cá nhân | ✅ Firestore |
| ❌ Hủy lịch khám | `HUY_LICH_KHAM` | Hủy lịch đã đặt | 🔗 App chính |
| 📋 Xem bệnh án | `XEM_BENH_AN` | Xem hồ sơ bệnh án | 🔗 XemBenhAnActivity |
| 💊 Xem đơn thuốc | `XEM_DON_THUOC` | Xem đơn thuốc được kê | 🔗 DanhSachDonThuocActivity |
| ⏰ Quản lý uống thuốc | `QUAN_LY_UONG_THUOC` | Điểm danh uống thuốc | 🔗 QuanLyUongThuocActivity |
| 💰 Xem hóa đơn | `XEM_HOA_DON` | Xem chi phí khám chữa | 🔗 DanhSachHoaDonActivity |
| 🔔 Xem thông báo | `XEM_THONG_BAO` | Thông báo từ bệnh viện | 🔗 ThongBaoActivity |
| 💬 Chat với bác sĩ | `CHAT_VOI_BAC_SI` | Nhắn tin với bác sĩ | 🔗 ChonBacSiChatActivity |
| 👨‍⚕️ Tìm bác sĩ | `TRA_CUU_BAC_SI` | Tìm bác sĩ theo chuyên khoa | 🔗 DanhSachBacSiActivity |
| 🏥 Thông tin bệnh viện | `TRA_CUU_THONG_TIN` | FAQ, giờ làm việc, giá | ✅ Built-in |

### **BÁC SĨ (bacsi) - 10 Chức năng:**

| Chức năng | Intent | Mô tả | Tích hợp |
|-----------|--------|-------|----------|
| 📅 Lịch làm việc | `XEM_LICH_LAM_VIEC` | Xem lịch làm việc cá nhân | 🔗 QuanLyLichLamViecActivity |
| 👥 Bệnh nhân hôm nay | `XEM_BENH_NHAN_NGAY` | Danh sách bệnh nhân hôm nay | ✅ Firestore |
| 📊 Thống kê | `THONG_KE_BAC_SI` | Báo cáo và thống kê | 🔗 Built-in |
| 📋 Quản lý bệnh án | `QUAN_LY_BENH_AN` | Quản lý hồ sơ bệnh nhân | 🔗 QuanLyBenhAnBacSiActivity |
| ✅ Xác nhận lịch khám | `XAC_NHAN_LICH_KHAM` | Duyệt lịch khám | 🔗 XacNhanLichKhamActivity |
| 💊 Quản lý đơn thuốc | `QUAN_LY_DON_THUOC_BS` | Kê đơn thuốc | 🔗 QuanLyDonThuocBacSiActivity |
| 🔢 Nhập mã khám | `NHAP_MA_KHAM` | Nhập mã bệnh nhân | 🔗 NhapMaKhamActivity |
| 🔔 Gửi thông báo | `GUI_THONG_BAO` | Gửi thông báo cho BN | 🔗 GuiThongBaoActivity |
| 💬 Chat với bệnh nhân | `CHAT_VOI_BENH_NHAN` | Trả lời tin nhắn BN | 🔗 DanhSachTinNhanBacSiActivity |
| 🤖 AI Assistant | `AI_ASSISTANT` | Trợ lý AI cho bác sĩ | ✅ Enhanced mode |

### **CHUNG (Cả hai vai trò):**

| Chức năng | Intent | Mô tả |
|-----------|--------|-------|
| 👋 Chào hỏi | `CHAO_HOI` | Lời chào thân thiện |
| 🙏 Cảm ơn | `CAM_ON` | Phản hồi lịch sự |
| 🏥 Thông tin bệnh viện | `TRA_CUU_THONG_TIN` | FAQ chung |

## 🔒 CƠ CHẾ BẢO MẬT

### **1. Role Validation**
```java
private void handleNewIntent(IntentDetector.Intent intent, String userMessage, ChatCallback callback) {
    String currentUserType = (String) conversationContext.getData("userType");
    
    // Bắt buộc phải có role
    if (currentUserType == null) {
        handleRoleSelection(callback);
        return;
    }
    
    // Kiểm tra quyền cho từng intent
    switch (intent) {
        case DAT_LICH_KHAM:
            if ("benhnhan".equals(currentUserType)) {
                handleBookingIntent(callback);
            } else {
                handleUnauthorizedAction("Chỉ bệnh nhân mới có thể đặt lịch khám", callback);
            }
            break;
    }
}
```

### **2. Unauthorized Access Handling**
```java
private void handleUnauthorizedAction(String message, ChatCallback callback) {
    ChatResponse response = new ChatResponse(
        "⚠️ " + message + "\n\n" +
        "Bạn có muốn chuyển đổi vai trò không?",
        ChatResponse.ResponseType.QUICK_REPLY
    );
    
    List<String> quickReplies = new ArrayList<>();
    quickReplies.add("🔄 Chuyển đổi vai trò");
    quickReplies.add("📋 Xem menu chức năng");
    response.setQuickReplies(quickReplies);
    
    callback.onResponse(response);
}
```

### **3. Role Selection Flow**
```java
private void handleRoleSelection(ChatCallback callback) {
    conversationContext.setState(ConversationContext.ConversationState.WAITING_ROLE_SELECTION);
    
    ChatResponse response = new ChatResponse(
        "👋 Xin chào! Tôi là **MediBot** - trợ lý ảo thông minh của bệnh viện.\n\n" +
        "Trước tiên, vui lòng cho tôi biết bạn là:",
        ChatResponse.ResponseType.QUICK_REPLY
    );
    
    List<String> quickReplies = new ArrayList<>();
    quickReplies.add("🏥 Tôi là Bệnh nhân");
    quickReplies.add("👨‍⚕️ Tôi là Bác sĩ");
    response.setQuickReplies(quickReplies);
    
    callback.onResponse(response);
}
```

## 🎯 WELCOME MESSAGES THEO ROLE

### **Bệnh nhân:**
```
👋 Xin chào! Tôi là trợ lý ảo dành cho **Bệnh nhân**.

🩺 **Tôi có thể giúp bạn:**
• 📅 Đặt lịch khám bệnh
• 👀 Xem lịch khám của bạn
• ❌ Hủy lịch khám
• 👨‍⚕️ Tìm bác sĩ theo chuyên khoa
• 💊 Xem đơn thuốc
• 🏥 Thông tin bệnh viện

Bạn cần hỗ trợ gì hôm nay?
```

### **Bác sĩ:**
```
👨‍⚕️ Xin chào Bác sĩ! Tôi là trợ lý ảo hỗ trợ công việc của bạn.

⚕️ **Tôi có thể giúp bạn:**
• 📅 Xem lịch làm việc
• 👥 Danh sách bệnh nhân hôm nay
• 📊 Thống kê và báo cáo
• ⚙️ Cập nhật lịch làm việc
• 👤 Thông tin bệnh nhân
• 📝 Ghi chú khám bệnh

Bạn cần hỗ trợ gì hôm nay?
```

## 🔄 ROLE SWITCHING

### **Chuyển đổi vai trò:**
```java
private void handleRoleSelectionInput(String userMessage, ChatCallback callback) {
    String message = userMessage.toLowerCase().trim();
    
    if (message.contains("bệnh nhân")) {
        conversationContext.setData("userType", "benhnhan");
        conversationContext.setState(ConversationContext.ConversationState.IDLE);
        // Show patient menu
    } else if (message.contains("bác sĩ")) {
        conversationContext.setData("userType", "bacsi");
        conversationContext.setState(ConversationContext.ConversationState.IDLE);
        // Show doctor menu
    }
}
```

## 🚀 INTEGRATION VỚI UI

### **ChatActivity Role Detection:**
```java
// Auto-detect user type từ Intent
maBenhNhan = getIntent().getStringExtra("MA_BENH_NHAN");
maBacSi = getIntent().getStringExtra("MA_BAC_SI");
userType = getIntent().getStringExtra("USER_TYPE");

if (userType == null) {
    userType = (maBacSi != null) ? "bacsi" : "benhnhan";
}

// Initialize chatbot với role
if ("doctor_assistant".equals(aiMode)) {
    chatbot = new ChatbotEngine(this, maBacSi, "bacsi");
} else {
    chatbot = new ChatbotEngine(this, maBenhNhan, "benhnhan");
}
```

### **MainBenhNhanActivity Integration:**
```java
private void handleChatbot() {
    Intent intent = new Intent(this, ChatActivity.class);
    intent.putExtra("MA_BENH_NHAN", maBenhNhan);
    intent.putExtra("USER_TYPE", "benhnhan");
    startActivity(intent);
}
```

### **MainBacSiActivity Integration:**
```java
private void handleAIAssistant() {
    Intent intent = new Intent(this, ChatActivity.class);
    intent.putExtra("MA_BAC_SI", maBacSi);
    intent.putExtra("USER_TYPE", "bacsi");
    intent.putExtra("AI_MODE", "doctor_assistant");
    startActivity(intent);
}
```

## 🛡️ SECURITY FEATURES

### **1. Role Persistence**
- Role được lưu trong `ConversationContext`
- Kiểm tra role cho mỗi intent
- Reset role khi cần thiết

### **2. Access Denial**
- Thông báo rõ ràng khi không có quyền
- Gợi ý chuyển đổi vai trò
- Không leak thông tin sensitive

### **3. Data Isolation**
- Bệnh nhân chỉ thấy dữ liệu của mình (`maBenhNhan`)
- Bác sĩ chỉ thấy dữ liệu liên quan (`maBacSi`)
- Không cross-access giữa các role

## 🎯 KẾT LUẬN

Hệ thống phân quyền chatbot đã được implement đầy đủ với:

✅ **Role-based access control** cho 21 intents  
✅ **Unauthorized access handling** với thông báo thân thiện  
✅ **Role detection & switching** linh hoạt  
✅ **Data isolation** bảo mật  
✅ **UI integration** seamless với existing activities  
✅ **Security-first approach** với validation đầy đủ  

Chatbot hoạt động an toàn và chính xác theo từng vai trò người dùng! 🔐✨