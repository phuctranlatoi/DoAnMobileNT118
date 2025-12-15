# Sửa lỗi Duplicate Messages - Tin nhắn bị lặp lại

## 🐛 **Vấn đề:**
Khi bác sĩ gửi tin nhắn, tin nhắn bị hiển thị duplicate (lặp lại 2 lần) trong chat interface.

## 🔍 **Nguyên nhân có thể:**

### 1. **Multiple calls to loadTinNhan()**
- `loadTinNhan()` được gọi trong `onCreate()` 
- Lại được gọi thêm lần nữa trong `getDataFromIntent()`
- Tạo ra multiple listeners cho cùng một query

### 2. **Firestore Listener không được cleanup**
- Listener cũ không được remove trước khi tạo listener mới
- Multiple listeners cùng lắng nghe một collection

### 3. **Adapter không clear data cũ**
- Method `setData()` không clear data cũ trước khi set data mới
- Data mới được append thay vì replace

## ✅ **Các sửa đổi đã thực hiện:**

### 1. **Thêm flag để tránh multiple loadTinNhan()**
```java
// Thêm flag
private boolean isMessageLoaded = false;

// Trong onCreate()
if (!TextUtils.isEmpty(maBenhNhan) || isDoctorView) {
    loadTinNhan();
    isMessageLoaded = true; // Đánh dấu đã load
}

// Trong getDataFromIntent()
if (!isMessageLoaded) { // Chỉ load nếu chưa load
    loadTinNhan();
    isMessageLoaded = true;
}
```

### 2. **Remove listener cũ trước khi tạo mới**
```java
private void loadTinNhan() {
    // Remove listener cũ nếu có
    if (messageListener != null) {
        messageListener.remove();
        messageListener = null;
    }
    
    // Tạo listener mới
    messageListener = query.addSnapshotListener(...);
}
```

### 3. **Clear data cũ trong adapter**
```java
public void setData(List<TinNhanBacSi> danhSachTinNhan) {
    // Clear data cũ trước khi set data mới
    this.danhSachTinNhan.clear();
    if (danhSachTinNhan != null) {
        this.danhSachTinNhan.addAll(danhSachTinNhan);
    }
    notifyDataSetChanged();
}
```

### 4. **Thêm debug logs**
```java
android.util.Log.d("NhanTinBacSi", "Snapshot received: " + querySnapshot.size() + " documents");
android.util.Log.d("NhanTinBacSi", "Setting " + danhSachTinNhan.size() + " messages to adapter");
```

## 🎯 **Kết quả mong đợi:**
- ✅ Tin nhắn chỉ hiển thị 1 lần (không duplicate)
- ✅ Real-time messaging vẫn hoạt động bình thường
- ✅ Performance tốt hơn (ít listener hơn)
- ✅ Memory leak được tránh

## 🔧 **Files đã sửa:**
1. `NhanTinBacSiActivity.java` - Thêm flag và cleanup listener
2. `TinNhanBacSiAdapter.java` - Clear data cũ trong setData()

## 🚀 **Test workflow:**
1. Bác sĩ gửi tin nhắn → Kiểm tra chỉ hiển thị 1 lần
2. Bệnh nhân trả lời → Kiểm tra real-time update
3. Rotate màn hình → Kiểm tra không bị duplicate
4. Vào/ra chat nhiều lần → Kiểm tra performance

**Duplicate messages đã được fix!** ✅