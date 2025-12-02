# 🔥 Firebase Structure cho Chatbot

## 📊 Collections cần có

### **1. ThongBao** (Đã có)
```javascript
ThongBao/{maThongBao}
{
  maThongBao: "TB001",
  maBenhNhan: "BN001",
  maBacSi: "BS001",
  tieuDe: "Nhắc nhở uống thuốc",
  noiDung: "Đã đến giờ uống thuốc...",
  loaiThongBao: "NHAC_THUOC", // LICH_HEN, NHAC_THUOC, THONG_BAO_CHUNG
  thoiGianGui: Timestamp,
  daDoc: false
}
```

### **2. BacSi** (Đã có - Cần đảm bảo có fields)
```javascript
BacSi/{maBacSi}
{
  maBacSi: "BS001",
  maTaiKhoan: "TK001",
  hoTen: "BS. Nguyễn Văn A",
  soDienThoai: "0123456789",
  chuyenKhoa: "Tim mạch",        // ✅ Cần có
  namKinhNghiem: 15,              // ✅ Cần có (int)
  bangCap: "Bác sĩ",
  hocVi: "Thạc sĩ",
  diaChi: "123 ABC",
  gioiThieu: "Chuyên về tim mạch...",
  avatarUrl: "https://...",
  trangThaiXacThuc: "Đã xác thực"
}
```

### **3. LichKham** (Đã có)
```javascript
LichKham/{maLichKham}
{
  maLichKham: "LK001",
  maBenhNhan: "BN001",
  maBacSi: "BS001",
  maLichLamViec: "LLV001",
  ngayKham: Timestamp,
  trangThai: "CHO_XAC_NHAN", // CHO_XAC_NHAN, DA_XAC_NHAN, HOAN_THANH, HUY
  soThuTu: 1,
  lyDoKham: "Khám tổng quát"     // Optional
}
```

### **4. LichLamViec** (Đã có)
```javascript
LichLamViec/{maLichLamViec}
{
  maLichLamViec: "LLV001",
  maBacSi: "BS001",
  ngayLamViec: Timestamp,
  gioVao: "08:00",
  gioRa: "12:00",
  trangThai: "HOAT_DONG"
}
```

### **5. DonThuoc** (Đã có)
```javascript
DonThuoc/{maDonThuoc}
{
  maDonThuoc: "DT001",
  maBenhAn: "BA001",
  ngayLap: Date
}
```

### **6. ChiTietDonThuoc** (Đã có)
```javascript
ChiTietDonThuoc/{maChiTiet}
{
  maChiTiet: "CTDT001",
  maDonThuoc: "DT001",
  tenThuoc: "Paracetamol 500mg",
  lieuDung: "2 viên/lần",
  soLan: "3 lần/ngày",
  cachDung: "Sau ăn 30 phút",
  soLuong: 30,
  donVi: "viên"
}
```

### **7. BenhAn** (Đã có)
```javascript
BenhAn/{maBenhAn}
{
  maBenhAn: "BA001",
  maLichKham: "LK001",
  maBenhNhan: "BN001",
  maBacSi: "BS001",
  chanDoan: "Viêm họng cấp",
  ghiChu: "Nghỉ ngơi 3 ngày",
  ngayKham: Timestamp
}
```

### **8. HoaDon** (Đã có)
```javascript
HoaDon/{maHoaDon}
{
  maHoaDon: "HD001",
  maBenhAn: "BA001",
  ngayLap: Date,
  tongTien: 500000
}
```

### **9. BenhNhan** (Đã có)
```javascript
BenhNhan/{maBenhNhan}
{
  maBenhNhan: "BN001",
  maTaiKhoan: "TK002",
  hoTen: "Nguyễn Văn B",
  soDienThoai: "0987654321",
  diaChi: "456 XYZ",
  ngaySinh: "01/01/1990",
  avatarUrl: "https://..."
}
```

---

## 🆕 Collections MỚI cho Chatbot (Optional - Nâng cao)

### **10. ChatHistory** (Lưu lịch sử chat)
```javascript
ChatHistory/{maChatHistory}
{
  maChatHistory: "CH001",
  maBenhNhan: "BN001",
  userMessage: "Đặt lịch khám",
  botResponse: "Bạn muốn khám ngày nào?",
  timestamp: Timestamp,
  intent: "DAT_LICH_KHAM",
  usedGemini: false  // true nếu dùng Gemini API
}
```

**Lợi ích:**
- Phân tích hành vi user
- Cải thiện chatbot
- Debug conversations
- Analytics

### **11. MedicationReminder** (Nhắc uống thuốc)
```javascript
MedicationReminder/{maReminder}
{
  maReminder: "MR001",
  maBenhNhan: "BN001",
  maDonThuoc: "DT001",
  tenThuoc: "Paracetamol",
  thoiGianNhac: ["08:00", "12:00", "18:00"],
  ngayBatDau: Timestamp,
  ngayKetThuc: Timestamp,
  trangThai: "ACTIVE", // ACTIVE, COMPLETED, CANCELLED
  lichSuUong: [
    {
      ngay: "2024-12-01",
      gio: "08:00",
      daUong: true
    }
  ]
}
```

**Lợi ích:**
- Chatbot nhắc uống thuốc tự động
- Tracking adherence
- Báo cáo cho bác sĩ

---

## 🔍 Indexes cần tạo (Firestore Console)

### **Composite Indexes:**

1. **LichKham - Query lịch hẹn của bệnh nhân**
   ```
   Collection: LichKham
   Fields: 
     - maBenhNhan (Ascending)
     - ngayKham (Descending)
   ```

2. **LichLamViec - Query lịch làm việc của bác sĩ**
   ```
   Collection: LichLamViec
   Fields:
     - maBacSi (Ascending)
     - ngayLamViec (Ascending)
   ```

3. **ThongBao - Query thông báo chưa đọc**
   ```
   Collection: ThongBao
   Fields:
     - maBenhNhan (Ascending)
     - daDoc (Ascending)
     - thoiGianGui (Descending)
   ```

4. **BacSi - Query bác sĩ theo chuyên khoa**
   ```
   Collection: BacSi
   Fields:
     - chuyenKhoa (Ascending)
     - namKinhNghiem (Descending)
   ```

---

## 📝 Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // BenhNhan chỉ đọc được data của mình
    match /LichKham/{lichKham} {
      allow read: if request.auth != null && 
                     resource.data.maBenhNhan == request.auth.uid;
      allow create: if request.auth != null;
    }
    
    match /ThongBao/{thongBao} {
      allow read: if request.auth != null && 
                     resource.data.maBenhNhan == request.auth.uid;
      allow update: if request.auth != null && 
                       resource.data.maBenhNhan == request.auth.uid;
    }
    
    match /DonThuoc/{donThuoc} {
      allow read: if request.auth != null;
    }
    
    match /BacSi/{bacSi} {
      allow read: if request.auth != null;
    }
    
    // ChatHistory - User chỉ đọc được chat của mình
    match /ChatHistory/{chatHistory} {
      allow read, write: if request.auth != null && 
                            resource.data.maBenhNhan == request.auth.uid;
    }
  }
}
```

---

## 🚀 Migration Script (Nếu cần update data)

### **Thêm field `namKinhNghiem` cho BacSi:**

```javascript
// Firebase Console > Firestore > BacSi
// Hoặc dùng script:

const admin = require('firebase-admin');
admin.initializeApp();
const db = admin.firestore();

async function addNamKinhNghiem() {
  const bacSiRef = db.collection('BacSi');
  const snapshot = await bacSiRef.get();
  
  const batch = db.batch();
  
  snapshot.forEach(doc => {
    // Nếu chưa có namKinhNghiem, set default = 0
    if (!doc.data().namKinhNghiem) {
      batch.update(doc.ref, { namKinhNghiem: 0 });
    }
  });
  
  await batch.commit();
  console.log('Updated all BacSi with namKinhNghiem');
}

addNamKinhNghiem();
```

---

## ✅ Checklist

### **Collections hiện có:**
- [x] BacSi (có `namKinhNghiem`, `chuyenKhoa`)
- [x] BenhNhan
- [x] LichKham
- [x] LichLamViec
- [x] DonThuoc
- [x] ChiTietDonThuoc
- [x] BenhAn
- [x] HoaDon
- [x] ThongBao

### **Collections mới (Optional):**
- [ ] ChatHistory (lưu lịch sử chat)
- [ ] MedicationReminder (nhắc uống thuốc)

### **Indexes:**
- [ ] LichKham (maBenhNhan + ngayKham)
- [ ] LichLamViec (maBacSi + ngayLamViec)
- [ ] ThongBao (maBenhNhan + daDoc + thoiGianGui)
- [ ] BacSi (chuyenKhoa + namKinhNghiem)

### **Security Rules:**
- [ ] Update rules cho chatbot
- [ ] Test permissions

---

## 📌 Notes

1. **Không cần tạo collection mới** - Chatbot dùng data hiện có
2. **Chỉ cần đảm bảo** BacSi có `namKinhNghiem` và `chuyenKhoa`
3. **Optional:** Tạo ChatHistory để analytics
4. **Optional:** Tạo MedicationReminder cho tính năng nhắc thuốc

---

**Tóm lại: Chatbot hoạt động với Firebase structure hiện tại, không cần thay đổi gì!** ✅
