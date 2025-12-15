# 📞 Tính năng Gọi điện và Video Call với Stringee

## 🎯 **Tính năng đã implement:**

### ✅ **Voice Call (Gọi điện thoại)**
- **Giao diện giống Messenger**: Nút gọi điện ở góc trên màn hình chat
- **Full-screen call interface**: Màn hình gọi toàn màn hình với avatar và thông tin
- **Call controls**: Nút tắt tiếng, loa ngoài, kết thúc cuộc gọi
- **Call duration timer**: Hiển thị thời gian cuộc gọi
- **Call status**: Hiển thị trạng thái cuộc gọi (đang gọi, đổ chuông, kết nối...)

### ✅ **Video Call (Gọi video)**
- **Video interface**: Màn hình video với local và remote view
- **Video controls**: Nút tắt camera, chuyển camera, tắt tiếng
- **Overlay UI**: Thông tin cuộc gọi overlay có thể ẩn/hiện
- **Picture-in-picture**: Local video hiển thị ở góc màn hình

### ✅ **Stringee Integration**
- **JWT Token Generation**: Tự động tạo access token
- **Connection Management**: Quản lý kết nối Stringee
- **Call Management**: Xử lý incoming/outgoing calls
- **Error Handling**: Xử lý lỗi kết nối và cuộc gọi

## 🔧 **Cấu trúc code:**

### **1. StringeeManager.java**
- Singleton class quản lý Stringee SDK
- Xử lý kết nối và authentication
- Quản lý voice call và video call
- Callback interfaces cho connection và call events

### **2. StringeeTokenGenerator.java**
- Generate JWT access token cho Stringee
- Sử dụng SID key và Secret key
- Validate token expiration
- HMAC SHA256 signature

### **3. VoiceCallActivity.java**
- Giao diện cuộc gọi thoại
- Call controls (mute, speaker, end call)
- Call duration timer
- Call status management

### **4. VideoCallActivity.java**
- Giao diện cuộc gọi video
- Video rendering (local + remote)
- Video controls (camera, switch camera)
- Overlay UI management

### **5. MyApplication.java**
- Initialize Stringee khi app khởi động
- Setup connection callbacks
- Global Stringee management

## 📱 **Giao diện người dùng:**

### **Chat Screen (Messenger-style)**
```
[Back] BS. Nguyễn Văn A    [📞] [📹] [Avatar]
       Đang hoạt động
```

### **Voice Call Screen**
```
        [Avatar lớn]
      BS. Nguyễn Văn A
       Đang gọi...
         00:45

    [🔇]    [📞]    [🔊]
   Mute   End Call  Speaker
```

### **Video Call Screen**
```
[Remote Video - Full Screen]
                    [Local Video]
                    [Small overlay]

[Info Overlay - Auto hide]
BS. Nguyễn Văn A
Đang trò chuyện video
00:45

[🔇] [📹] [📞] [🔄]
Mute Camera End Switch
```

## 🔑 **Stringee Configuration:**

### **API Keys:**
- **SID Key**: `SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbsoapi`
- **Secret Key**: `TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY=`

### **User ID Format:**
- **Bệnh nhân**: `patient_{maBenhNhan}`
- **Bác sĩ**: `doctor_{maBacSi}`
- **Fallback**: `user_{timestamp}`

### **Token Generation:**
- **Algorithm**: HMAC SHA256
- **Expiration**: 1 hour
- **Format**: JWT (Header.Payload.Signature)

## 🚀 **Cách sử dụng:**

### **1. Từ màn hình chat:**
- Bấm nút 📞 để gọi điện thoại
- Bấm nút 📹 để gọi video
- Tự động chuyển đến màn hình call

### **2. Trong cuộc gọi:**
- **Voice Call**: Mute, Speaker, End Call
- **Video Call**: Mute, Camera On/Off, Switch Camera, End Call
- **Tap màn hình**: Ẩn/hiện controls (video call)

### **3. Kết thúc cuộc gọi:**
- Bấm nút End Call (đỏ)
- Tự động quay về màn hình chat

## 📁 **Files đã tạo:**

### **Java Classes:**
- `StringeeManager.java` - Quản lý Stringee SDK
- `StringeeTokenGenerator.java` - Generate JWT tokens
- `VoiceCallActivity.java` - Màn hình gọi thoại
- `VideoCallActivity.java` - Màn hình gọi video
- `MyApplication.java` - Initialize app

### **Layouts:**
- `activity_voice_call.xml` - Giao diện gọi thoại
- `activity_video_call.xml` - Giao diện gọi video

### **Drawables:**
- `ic_call.xml`, `ic_video_call.xml` - Icons chính
- `ic_call_end.xml`, `ic_mic_off.xml`, `ic_volume_up.xml` - Control icons
- `ic_videocam_off.xml`, `ic_switch_camera.xml` - Video icons
- Background gradients và button styles

### **Dependencies:**
- `stringee-android-sdk:1.9.3`
- `google-webrtc:1.0.32006`

## ⚠️ **Lưu ý quan trọng:**

### **1. Permissions:**
- RECORD_AUDIO, CAMERA - Bắt buộc cho call
- MODIFY_AUDIO_SETTINGS - Điều khiển audio
- BLUETOOTH - Hỗ trợ tai nghe Bluetooth

### **2. Production Deployment:**
- JWT token generation nên được thực hiện trên server
- Không hardcode API keys trong app
- Implement proper user authentication

### **3. Network Requirements:**
- Cần kết nối internet ổn định
- WebRTC yêu cầu UDP ports
- Firewall có thể block một số connections

## 🎉 **Kết quả:**

Hệ thống calling đã hoàn thành với:
- ✅ Voice call với giao diện đẹp
- ✅ Video call với full controls
- ✅ Messenger-style integration
- ✅ Stringee SDK integration
- ✅ JWT token authentication
- ✅ Professional UI/UX
- ✅ Error handling và connection management

**Tính năng gọi điện và video call đã sẵn sàng sử dụng!** 📞📹