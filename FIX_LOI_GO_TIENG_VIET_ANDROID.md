# 🔧 Fix Lỗi Gõ Tiếng Việt trên Android

## ❌ **Vấn đề:**

Khi gõ tiếng Việt trên Android với bàn phím như Gboard, SwiftKey:
- Gõ "toi" → bàn phím tự động sửa thành "tôi"
- Nhưng EditText có thể nhận text chưa hoàn chỉnh
- Dẫn đến lỗi hiển thị hoặc xử lý sai

## ✅ **Giải pháp đã áp dụng:**

### **1. Thêm `imeOptions` trong layout:**

```xml
<!-- activity_chat.xml -->
<Edi