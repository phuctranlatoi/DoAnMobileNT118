# 🏥 SƠ ĐỒ TỔNG QUAN DỰ ÁN HỆ THỐNG QUẢN LÝ BỆNH VIỆN

## 📋 TỔNG QUAN CÁC MODULE VÀ API CONNECTIONS

```
                    🏥 HỆ THỐNG QUẢN LÝ BỆNH VIỆN
                         (Android Application)
                                   │
        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
        ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  👤 USER MODULE │    │ 🤖 AI MODULE    │    │ 📞 CALL MODULE  │
│                 │    │                 │    │                 │
│ • Authentication│    │ • Chatbot       │    │ • Voice Call    │
│ • Role Management│   │ • NLP Engine    │    │ • Video Call    │
│ • Profile Mgmt  │    │ • Intent Detect │    │ • P2P Connect   │
│ • Session Mgmt  │    │ • Gemini AI     │    │ • Call Control  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                          │                          │
        │ HTTPS                    │ HTTPS                    │ WSS/WebRTC
        ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 🔐 Firebase     │    │ 🧠 Google       │    │ 📞 Stringee     │
│    Auth API     │    │    Gemini AI    │    │    SDK API      │
│                 │    │                 │    │                 │
│ • Login/Logout  │    │ • NLP Processing│    │ • Voice/Video   │
│ • Registration  │    │ • Intent Detect │    │ • P2P Connection│
│ • Token Mgmt    │    │ • Response Gen  │    │ • Call Control  │
└─────────────────┘    └─────────────────┘    └─────────────────┘

        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
        ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 📅 SCHEDULE     │    │ 💬 MESSAGING    │    │ 💊 MEDICINE     │
│    MODULE       │    │    MODULE       │    │    MODULE       │
│                 │    │                 │    │                 │
│ • Appointment   │    │ • 1-1 Chat Only │    │ • Prescription  │
│ • Time Slots    │    │ • Doctor-Patient│    │ • Reminder      │
│ • Work Schedule │    │ • Payment Req'd │    │ • Tracking      │
│ • Smart Booking │    │ • Real-time Sync│    │ • Adherence     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                          │                          │
        │ HTTPS/WSS                │ HTTPS/WSS                │ Local Alarms
        ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 🗄️ Firestore    │    │ 🔔 Firebase     │    │ ⏰ Android      │
│    Database     │    │    FCM API      │    │    AlarmManager │
│                 │    │                 │    │                 │
│ • Real-time DB  │    │ • Push Messages │    │ • Scheduled     │
│ • Collections   │    │ • Topics        │    │ • Notifications │
│ • Queries       │    │ • Targeting     │    │ • Reminders     │
└─────────────────┘    └─────────────────┘    └─────────────────┘

        ┌──────────────────────────┼──────────────────────────┐
        │                          │                          │
        ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 🏥 MEDICAL      │    │ 💳 PAYMENT      │    │ 🔔 NOTIFICATION │
│    MODULE       │    │    MODULE       │    │    MODULE       │
│                 │    │                 │    │                 │
│ • Medical Record│    │ • QR Payment    │    │ • FCM Service   │
│ • Diagnosis     │    │ • Billing       │    │ • Push Messages │
│ • Treatment     │    │ • Invoice       │    │ • Reminders     │
│ • Doctor Notes  │    │ • Transaction   │    │ • System Alerts │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                          │                          │
        │ HTTPS                    │ HTTPS                    │ HTTPS/WSS
        ▼                          ▼                          ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 📁 Firebase     │    │ 💰 QR Code      │    │ 🔔 Firebase     │
│    Storage      │    │    Generator    │    │    FCM          │
│                 │    │                 │    │                 │
│ • File Upload   │    │ • MoMo API      │    │ • Push Service  │
│ • Image Storage │    │ • VNPay API     │    │ • Real-time     │
│ • Documents     │    │ • Bank Transfer │    │ • Broadcast     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                   │
                                   ▼
                    ┌─────────────────────────────┐
                    │     🗄️ DATA MODULE         │
                    │                             │
                    │ • Firebase Firestore       │
                    │ • Firebase Storage          │
                    │ • Repository Pattern        │
                    │ • Real-time Sync (WSS)     │
                    └─────────────────────────────┘
```

## 🌐 API CONNECTIONS & PROTOCOLS

### **HTTPS Connections:**
```
📱 Android App ──HTTPS──► 🔐 Firebase Auth API
                         ├── POST /auth/signIn
                         ├── POST /auth/register  
                         ├── POST /auth/verify
                         └── GET /auth/user

📱 Android App ──HTTPS──► 🗄️ Firestore REST API
                         ├── GET /collections/{collection}
                         ├── POST /documents/{path}
                         ├── PUT /documents/{path}
                         └── DELETE /documents/{path}

📱 Android App ──HTTPS──► 🧠 Google Gemini AI API
                         ├── POST /generateContent
                         ├── Content-Type: application/json
                         └── Authorization: Bearer {API_KEY}

📱 Android App ──HTTPS──► 📁 Firebase Storage API
                         ├── POST /upload (multipart/form-data)
                         ├── GET /download/{file}
                         └── DELETE /files/{path}

📱 Android App ──HTTPS──► 🔔 Firebase FCM API
                         ├── POST /fcm/send
                         ├── POST /fcm/subscribe
                         └── GET /fcm/tokens
```

### **WebSocket/Real-time Connections:**
```
📱 Android App ──WSS───► 🗄️ Firestore Real-time
                        ├── addSnapshotListener()
                        ├── Real-time document updates
                        └── Collection change streams

📱 Android App ──WebRTC─► 📞 Stringee P2P Connection
                         ├── Voice call streams
                         ├── Video call streams
                         ├── STUN/TURN servers
                         └── ICE candidates exchange
```

## 📚 THỦ VIỆN VÀ DEPENDENCIES

### **Firebase Ecosystem:**
```gradle
implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
implementation("com.google.firebase:firebase-firestore")     // Database
implementation("com.google.firebase:firebase-auth")         // Authentication  
implementation("com.google.firebase:firebase-storage")      // File Storage
implementation("com.google.firebase:firebase-messaging")    // Push Notifications
implementation("com.google.firebase:firebase-analytics")    // Analytics
implementation("com.google.firebase:firebase-ai")           // Gemini AI
```

### **UI & Material Design:**
```gradle
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("com.google.android.material:material:1.11.0")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
implementation("androidx.gridlayout:gridlayout:1.0.0")
```

### **Communication & Calling:**
```gradle
implementation("com.stringee.sdk.android:stringee-android-sdk:2.1.5")  // Voice/Video
implementation("io.github.webrtc-sdk:android:137.7151.03")             // WebRTC
implementation("com.android.volley:volley:1.2.1")                      // HTTP Client
```

### **Image & Media Processing:**
```gradle
implementation("com.github.bumptech.glide:glide:4.16.0")        // Image Loading
implementation("de.hdodenhof:circleimageview:3.1.0")            // Circle Avatar
```

### **Security & Encryption:**
```gradle
implementation("org.mindrot:jbcrypt:0.4")                       // Password Hashing
```

### **Email & Messaging:**
```gradle
implementation("com.sun.mail:javax.mail:1.6.2")                // Email Service
```

## 🔐 ANDROID PERMISSIONS

### **System Permissions:**
```xml
<!-- Network & Internet -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />

<!-- Storage & Media -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" 
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.CAMERA" />

<!-- Notifications & Alarms -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Audio & Video Calling -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
```

### **Hardware Features:**
```xml
<!-- Camera Features -->
<uses-feature android:name="android.hardware.camera" android:required="true" />
<uses-feature android:name="android.hardware.camera.autofocus" android:required="false" />

<!-- Bluetooth Features -->
<uses-feature android:name="android.hardware.bluetooth" android:required="false" />
<uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />

<!-- OpenGL ES -->
<uses-feature android:glEsVersion="0x00020000" android:required="false" />
```

### **Permission Usage Mapping:**
```
📱 USER MODULE:
   ├── CAMERA → Profile avatar, medical photos
   ├── STORAGE → Save/load user data
   └── INTERNET → Authentication, profile sync

🤖 AI MODULE:
   ├── INTERNET → Gemini AI API calls
   ├── RECORD_AUDIO → Voice input (future)
   └── MICROPHONE → Speech recognition

📞 CALL MODULE:
   ├── RECORD_AUDIO → Voice calling
   ├── CAMERA → Video calling
   ├── BLUETOOTH → Headset support
   ├── MODIFY_AUDIO → Call audio routing
   └── INTERNET → Stringee connection

💬 MESSAGING MODULE:
   ├── INTERNET → Real-time chat
   ├── POST_NOTIFICATIONS → Message alerts
   ├── VIBRATE → Notification feedback
   └── WAKE_LOCK → Background messaging

💊 MEDICINE MODULE:
   ├── SCHEDULE_EXACT_ALARM → Medicine reminders
   ├── USE_EXACT_ALARM → Precise timing
   ├── RECEIVE_BOOT_COMPLETED → Restore alarms
   ├── POST_NOTIFICATIONS → Reminder alerts
   └── VIBRATE → Alarm feedback

🏥 MEDICAL MODULE:
   ├── CAMERA → Scan prescriptions, documents
   ├── STORAGE → Save medical files
   └── INTERNET → Upload to Firebase Storage

💳 PAYMENT MODULE:
   ├── CAMERA → QR code scanning
   ├── INTERNET → Payment API calls
   └── STORAGE → Receipt storage

🔔 NOTIFICATION MODULE:
   ├── POST_NOTIFICATIONS → All app notifications
   ├── INTERNET → FCM connection
   ├── VIBRATE → Alert feedback
   └── WAKE_LOCK → Background processing
```

## 🛡️ SECURITY & PRIVACY

### **Data Protection:**
- **BCrypt Hashing**: Password encryption
- **Firebase Security Rules**: Database access control
- **HTTPS Only**: All API communications encrypted
- **Token-based Auth**: Secure session management
- **Role-based Access**: Permission-based features

### **Medical Data Compliance:**
- **HIPAA-ready**: Secure medical data handling
- **Data Encryption**: At rest and in transit
- **Access Logging**: User activity tracking
- **Consent Management**: Permission-based data access
- **Data Retention**: Configurable retention policies

### 1. 👤 **USER MODULE** - Quản lý người dùng
```
📁 Package: com.example.doannt118.ui
├── LoginActivity.java          - Đăng nhập
├── RegisterActivity.java       - Đăng ký
├── MainBenhNhanActivity.java   - Giao diện bệnh nhân
├── MainBacSiActivity.java      - Giao diện bác sĩ
├── MainAdminActivity.java      - Giao diện admin
├── ProfileActivity.java        - Quản lý profile
└── utils/SessionManager.java   - Quản lý phiên đăng nhập
```

### 2. 🤖 **AI MODULE** - Trợ lý thông minh
```
📁 Package: com.example.doannt118.chatbot
├── ChatbotEngine.java          - Engine xử lý chatbot
├── IntentDetector.java         - Phát hiện ý định
├── ChatResponse.java           - Tạo phản hồi
├── ChatActivity.java           - Giao diện chat
└── ChatAdapter.java            - Hiển thị tin nhắn
```

### 3. 📞 **CALL MODULE** - Gọi điện thoại
```
📁 Package: com.example.doannt118.stringee
├── StringeeManager.java        - Quản lý Stringee SDK
├── StringeeTokenGenerator.java - Tạo token
├── VoiceCallActivity.java      - Gọi thoại
├── VideoCallActivity.java      - Gọi video
└── IncomingCallActivity.java   - Nhận cuộc gọi
```

### 4. 📅 **SCHEDULE MODULE** - Quản lý lịch
```
📁 Package: com.example.doannt118.ui
├── DangKyLichKhamActivity.java     - Đặt lịch khám
├── QuanLyLichLamViecActivity.java  - Quản lý lịch làm việc
├── XacNhanLichKhamActivity.java    - Xác nhận lịch
├── ChiTietBacSiActivity.java       - Chi tiết bác sĩ
├── TimeSlotAdapter.java            - Hiển thị slot thời gian
└── model/TimeSlot.java             - Model slot thời gian
```

### 5. 💬 **MESSAGING MODULE** - Nhắn tin 1-1
```
📁 Package: com.example.doannt118.ui
├── NhanTinBacSiActivity.java           - Chat 1-1 giữa bác sĩ và bệnh nhân
├── DanhSachTinNhanBacSiActivity.java   - DS cuộc trò chuyện (view bác sĩ)
├── DanhSachCuocTroChuyenBenhNhanActivity.java - DS cuộc trò chuyện (view bệnh nhân)
├── ThongTinBacSiActivity.java          - Chọn bác sĩ và gói tư vấn
├── ThanhToanQRActivity.java            - Thanh toán để mở khóa chat
├── TinNhanBacSiAdapter.java            - Adapter hiển thị tin nhắn
└── model/TinNhanBacSi.java             - Model: maBacSi + maBenhNhan + nội dung
```

**Đặc điểm:**
- **1-1 Chat**: Mỗi cuộc trò chuyện chỉ giữa 1 bác sĩ và 1 bệnh nhân
- **Unique Pair**: Mỗi tin nhắn có maBacSi + maBenhNhan duy nhất
- **Payment Required**: Bệnh nhân phải thanh toán QR để chat
- **Real-time**: Firebase Firestore addSnapshotListener
- **Push Notifications**: FCM cho cả 2 bên khi có tin nhắn mới

### 6. 💊 **MEDICINE MODULE** - Quản lý thuốc
```
📁 Package: com.example.doannt118.ui
├── QuanLyUongThuocActivity.java        - Quản lý uống thuốc
├── DiemDanhUongThuocActivity.java      - Điểm danh uống thuốc
├── XacNhanUongThuocActivity.java       - Xác nhận uống thuốc
├── LichSuUongThuocActivity.java        - Lịch sử uống thuốc
├── utils/MedicineReminderManager.java  - Quản lý nhắc nhở
└── receiver/MedicineReminderReceiver.java - Nhận alarm nhắc nhở
```

### 7. 🏥 **MEDICAL MODULE** - Hồ sơ y tế
```
📁 Package: com.example.doannt118.ui
├── TaoBenhAnActivity.java              - Tạo bệnh án
├── XemBenhAnActivity.java              - Xem bệnh án
├── KeDonThuocActivity.java             - Kê đơn thuốc
├── QuanLyDonThuocBacSiActivity.java    - Quản lý đơn thuốc
├── TaoHoaDonActivity.java              - Tạo hóa đơn
└── NhapMaKhamActivity.java             - Nhập mã khám
```

### 8. 💳 **PAYMENT MODULE** - Thanh toán
```
📁 Package: com.example.doannt118.ui
├── ThanhToanActivity.java              - Chọn phương thức thanh toán
├── ThanhToanQRActivity.java            - Thanh toán QR
├── DanhSachHoaDonActivity.java         - Danh sách hóa đơn
├── ChiTietHoaDonActivity.java          - Chi tiết hóa đơn
└── model/GoiNhanTin.java               - Model gói nhắn tin
```

### 9. 🔔 **NOTIFICATION MODULE** - Thông báo
```
📁 Package: com.example.doannt118.service
├── MyFirebaseMessagingService.java     - FCM Service
├── utils/NotificationHelper.java       - Helper thông báo
├── ThongBaoActivity.java               - Xem thông báo
├── GuiThongBaoActivity.java            - Gửi thông báo
└── receiver/BootReceiver.java          - Khởi động lại alarm
```

### 10. 🗄️ **DATA MODULE** - Quản lý dữ liệu
```
📁 Package: com.example.doannt118.repository
├── FirestoreRepository.java            - Repository chính
├── utils/UserInfoLoader.java           - Load thông tin user
├── model/ (15+ models)                 - Các model dữ liệu
│   ├── BenhNhan.java
│   ├── BacSi.java
│   ├── LichKham.java
│   ├── TinNhanBacSi.java
│   └── ...
└── MyApplication.java                  - Application class
```

## 🔄 TƯƠNG TÁC GIỮA CÁC MODULE

```
USER MODULE ←→ AI MODULE (Chatbot cho từng role)
     ↓              ↓
SCHEDULE MODULE ←→ MESSAGING MODULE (Đặt lịch qua chat)
     ↓              ↓
MEDICAL MODULE ←→ MEDICINE MODULE (Kê đơn → Nhắc uống)
     ↓              ↓
PAYMENT MODULE ←→ NOTIFICATION MODULE (Thanh toán → Thông báo)
     ↓              ↓
     DATA MODULE (Lưu trữ tất cả dữ liệu)
```

## 🛠️ CÔNG NGHỆ SỬ DỤNG

- **Frontend**: Android Java, Material Design
- **Backend**: Firebase (Auth, Firestore, Storage, FCM)
- **AI**: Google Gemini API
- **Communication**: Stringee SDK (Voice/Video)
- **Payment**: QR Code, MoMo/VNPay simulation
- **Architecture**: Repository Pattern, MVP, Observer Pattern