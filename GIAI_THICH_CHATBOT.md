# 📚 Giải thích Kiến trúc Chatbot Hybrid

## 🎯 **Ý tưởng chính**

```
90% TỰ BUILD (Rule-based) + 10% Gemini (Fallback)
```

**Tại sao?**
- ✅ Cô thích tự build → Phần lớn logic là của bạn
- ✅ Tiết kiệm API calls → Chỉ gọi Gemini khi cần
- ✅ Kiểm soát responses → Không phụ thuộc AI
- ✅ Nhanh hơn → Rule-based instant, không chờ API
- ✅ Offline-friendly → Phần lớn chạy được offline

---

## 🏗️ **Kiến trúc 3 tầng**

### **Tầng 1: Intent Detection (TỰ BUILD)**

```java
// IntentDetector.java
Intent detect(String message) {
    // Keyword matching đơn giản
    if (message.contains("đặt lịch")) 
        return DAT_LICH_KHAM;
    if (message.contains("bác sĩ"))
        return TRA_CUU_BAC_SI;
    // ...
    return KHAC; // Không match → Dùng Gemini
}
```

**Giải thích:**
- Dùng keyword matching (contains, regex)
- Nhanh, đơn giản, không cần AI
- Match được 80-90% cases thông thường

---

### **Tầng 2: Business Logic (TỰ BUILD)**

```java
// ChatbotEngine.java
void handleBookingIntent() {
    // 1. Hỏi ngày
    conversationContext.setState(WAITING_DATE);
    response = "Bạn muốn khám ngày nào?";
    
    // 2. User trả lời → Parse date
    String date = parseDateFromText(userInput);
    
    // 3. Query Firestore
    List<BacSi> doctors = queryDoctorsByDate(date);
    
    // 4. Hiển thị danh sách
    response = "Các bác sĩ có lịch: ...";
    
    // 5. User chọn → Xác nhận
    // 6. Tạo LichKham trong Firestore
}
```

**Giải thích:**
- State machine: Track conversation flow
- Query Firestore: Lấy data thực từ DB
- Template responses: Hardcoded, nhất quán
- Actions: Tạo/update data trong Firestore

**Ưu điểm:**
- ✅ Bạn kiểm soát 100% logic
- ✅ Responses nhất quán, không ngẫu nhiên
- ✅ Không tốn API calls
- ✅ Chạy nhanh

---

### **Tầng 3: Gemini Fallback (CHỈ KHI CẦN)**

```java
// ChatbotEngine.java
void handleNewIntent(Intent intent, String message) {
    if (intent == KHAC) {
        // Không match intent nào
        // → Dùng Gemini làm fallback
        handleWithGemini(message);
    } else {
        // Match intent → Dùng logic tự build
        handleBookingIntent();
    }
}
```

**Khi nào dùng Gemini?**
1. ❌ Không match intent nào
2. ❌ Câu hỏi phức tạp, không parse được
3. ❌ User hỏi về kiến thức y tế chung

**VD:**
```
User: "Tôi bị đau đầu và sốt, có nguy hiểm không?"
→ Không match intent cụ thể
→ Gọi Gemini để tư vấn sơ bộ
```

**Ưu điểm:**
- ✅ Handle edge cases
- ✅ Flexible với câu hỏi tự do
- ✅ Vẫn tiết kiệm API (chỉ 10% cases)

---

## 📊 **Flow thực tế**

### **Case 1: Đặt lịch khám (90% TỰ BUILD)**

```
User: "Đặt lịch khám"
  ↓
IntentDetector: DAT_LICH_KHAM ✅
  ↓
ChatbotEngine.handleBookingIntent()
  ↓
State: IDLE → WAITING_DATE
  ↓
Response: "Bạn muốn khám ngày nào?" (Template)
  ↓
User: "15/12"
  ↓
parseDateFromText("15/12") → "15/12/2024" ✅
  ↓
queryDoctorsByDate("15/12/2024") → [BS A, BS B]
  ↓
State: WAITING_DATE → WAITING_DOCTOR
  ↓
Response: "Các bác sĩ: BS A, BS B" (Template)
  ↓
User: "Bác sĩ A"
  ↓
findDoctorFromInput("Bác sĩ A") → BacSi A ✅
  ↓
State: WAITING_DOCTOR → WAITING_CONFIRMATION
  ↓
Response: "Xác nhận: BS A, 15/12..." (Template)
  ↓
User: "Xác nhận"
  ↓
createBooking() → Firestore.add(LichKham)
  ↓
Response: "✅ Đặt lịch thành công!" (Template)
```

**Tổng API calls: 0** ✅

---

### **Case 2: Câu hỏi phức tạp (10% GEMINI)**

```
User: "Tôi bị đau đầu và sốt 38 độ, có nên uống aspirin không?"
  ↓
IntentDetector: KHAC ❌ (Không match)
  ↓
ChatbotEngine.handleWithGemini()
  ↓
Build context: "User có đơn thuốc: Paracetamol..."
  ↓
GeminiAssistant.ask(question, context)
  ↓
Gemini API call → Response
  ↓
Response: "Với triệu chứng sốt 38°C, bạn nên:
          1. Nghỉ ngơi, uống nhiều nước
          2. Có thể dùng Paracetamol (đã có trong đơn)
          3. KHÔNG nên tự ý dùng Aspirin
          4. Nếu sốt >3 ngày, đến khám ngay
          
          ⚠️ Đây chỉ là tư vấn sơ bộ, không thay thế bác sĩ."
```

**Tổng API calls: 1** (Chỉ khi cần)

---

## 💡 **Tại sao kiến trúc này tốt?**

### **1. Tiết kiệm API quota**
```
1000 messages/ngày:
- 900 messages: Rule-based (0 API calls)
- 100 messages: Gemini fallback (100 API calls)

Gemini free tier: 60 requests/phút = 86,400/ngày
→ Đủ dư!
```

### **2. Responses nhất quán**
```
Rule-based:
"Bạn muốn khám ngày nào?" ← Luôn giống nhau

Gemini:
"Bạn muốn đặt lịch vào ngày nào?" ← Có thể khác
"Hãy cho tôi biết ngày bạn muốn khám" ← Mỗi lần khác
```

### **3. Tốc độ nhanh**
```
Rule-based: <50ms (instant)
Gemini API: 1-3 giây (chờ network)
```

### **4. Offline-friendly**
```
Rule-based: Chạy được offline (sau khi load Firestore data)
Gemini: Cần internet
```

### **5. Dễ debug**
```
Rule-based: Biết chính xác logic flow
Gemini: Black box, khó debug
```

---

## 🎓 **Khi demo với cô**

### **Nhấn mạnh:**

1. **"Em tự build 90% logic"**
   - Intent detection: Keyword matching
   - Conversation flow: State machine
   - Data queries: Firestore
   - Responses: Templates

2. **"Gemini chỉ là trợ lý"**
   - Chỉ dùng khi không match intent
   - Fallback cho edge cases
   - Tư vấn kiến thức y tế chung

3. **"Tiết kiệm API calls"**
   - Chỉ 10% cases dùng Gemini
   - 90% chạy bằng logic tự build

4. **"Có thể tắt Gemini"**
   - Nếu không có API key
   - Vẫn chạy được 90% tính năng
   - Chỉ mất phần tư vấn phức tạp

---

## 📝 **Các tính năng TỰ BUILD**

### ✅ **Hoàn toàn tự build (0% Gemini):**
1. Đặt lịch khám (conversational)
2. Xem lịch hẹn
3. Xem đơn thuốc
4. Hướng dẫn uống thuốc
5. Tra cứu bác sĩ theo chuyên khoa
6. Thông tin phòng khám (FAQ)
7. Nhắc uống thuốc
8. Tracking uống thuốc

### 🤝 **Hybrid (Rule-based + Gemini fallback):**
9. Tư vấn triệu chứng
10. Câu hỏi tự do về y tế
11. Giải thích thuốc
12. Gợi ý chuyên khoa

---

## 🚀 **Roadmap Implementation**

### **Week 1: Core (TỰ BUILD)**
- ✅ IntentDetector
- ✅ ConversationContext
- ✅ ChatbotEngine (booking flow)
- ✅ Template responses

### **Week 2: Integration (TỰ BUILD)**
- ✅ Connect Firestore
- ✅ Query doctors, appointments
- ✅ Create bookings
- ✅ UI (ChatActivity)

### **Week 3: Advanced (TỰ BUILD)**
- ✅ Medication guide
- ✅ View medical records
- ✅ Doctor search
- ✅ FAQ

### **Week 4: Gemini (10%)**
- ✅ GeminiAssistant
- ✅ Fallback logic
- ✅ Symptom consultation
- ✅ Polish & test

---

## 🎯 **Kết luận**

**Chatbot này là:**
- 90% công sức của bạn (Rule-based)
- 10% Gemini hỗ trợ (Fallback)

**Cô sẽ thấy:**
- Bạn hiểu rõ logic
- Bạn tự build phần lớn
- Gemini chỉ là công cụ hỗ trợ
- Tiết kiệm, thông minh

**Perfect cho đồ án!** 🎓
