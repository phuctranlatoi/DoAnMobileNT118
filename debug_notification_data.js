// Script debug để kiểm tra dữ liệu thông báo
// Chạy trong Firebase Console → Firestore Database → Console tab

// Bước 1: Kiểm tra dữ liệu BenhNhan
console.log("=== KIỂM TRA DỮ LIỆU BỆNH NHÂN ===");
firebase.firestore().collection('BenhNhan').limit(5).get().then(snapshot => {
  console.log("Số lượng bệnh nhân:", snapshot.size);
  snapshot.forEach(doc => {
    const data = doc.data();
    console.log("Bệnh nhân:", data.maBenhNhan, "-", data.hoTen);
  });
});

// Bước 2: Kiểm tra dữ liệu BacSi
console.log("\n=== KIỂM TRA DỮ LIỆU BÁC SĨ ===");
firebase.firestore().collection('BacSi').limit(5).get().then(snapshot => {
  console.log("Số lượng bác sĩ:", snapshot.size);
  snapshot.forEach(doc => {
    const data = doc.data();
    console.log("Bác sĩ:", data.maBacSi, "-", data.hoTen);
  });
});

// Bước 3: Kiểm tra dữ liệu ThongBao hiện tại
console.log("\n=== KIỂM TRA DỮ LIỆU THÔNG BÁO ===");
firebase.firestore().collection('ThongBao').get().then(snapshot => {
  console.log("Số lượng thông báo:", snapshot.size);
  snapshot.forEach(doc => {
    const data = doc.data();
    console.log("Thông báo:", data.maThongBao, "-", data.tieuDe);
    console.log("  - Bệnh nhân:", data.maBenhNhan);
    console.log("  - Bác sĩ:", data.maBacSi);
    console.log("  - Loại:", data.loaiThongBao);
    console.log("  - Đã đọc:", data.daDoc);
  });
});

// Bước 4: Tạo thông báo test với dữ liệu thực tế
async function createTestNotificationWithRealData() {
  console.log("\n=== TẠO THÔNG BÁO TEST ===");
  
  // Lấy bệnh nhân đầu tiên
  const benhNhanSnapshot = await firebase.firestore().collection('BenhNhan').limit(1).get();
  if (benhNhanSnapshot.empty) {
    console.log("❌ Không có dữ liệu bệnh nhân!");
    return;
  }
  
  // Lấy bác sĩ đầu tiên
  const bacSiSnapshot = await firebase.firestore().collection('BacSi').limit(1).get();
  if (bacSiSnapshot.empty) {
    console.log("❌ Không có dữ liệu bác sĩ!");
    return;
  }
  
  const benhNhan = benhNhanSnapshot.docs[0].data();
  const bacSi = bacSiSnapshot.docs[0].data();
  
  console.log("✓ Sử dụng bệnh nhân:", benhNhan.maBenhNhan, "-", benhNhan.hoTen);
  console.log("✓ Sử dụng bác sĩ:", bacSi.maBacSi, "-", bacSi.hoTen);
  
  // Tạo thông báo test
  const testNotifications = [
    {
      maThongBao: "TB_DEBUG_001",
      maBenhNhan: benhNhan.maBenhNhan,
      maBacSi: bacSi.maBacSi,
      tieuDe: "🔔 Test Thông báo 1",
      noiDung: "Đây là thông báo test để kiểm tra hệ thống. Nếu bạn thấy được tin nhắn này, hệ thống đang hoạt động bình thường.",
      loaiThongBao: "THONG_BAO_CHUNG",
      thoiGianGui: firebase.firestore.Timestamp.now(),
      daDoc: false
    },
    {
      maThongBao: "TB_DEBUG_002",
      maBenhNhan: benhNhan.maBenhNhan,
      maBacSi: bacSi.maBacSi,
      tieuDe: "💊 Nhắc nhở uống thuốc",
      noiDung: "Đã đến giờ uống thuốc buổi sáng. Vui lòng uống theo đơn thuốc đã kê.",
      loaiThongBao: "NHAC_THUOC",
      thoiGianGui: firebase.firestore.Timestamp.now(),
      daDoc: false
    },
    {
      maThongBao: "TB_DEBUG_003",
      maBenhNhan: benhNhan.maBenhNhan,
      maBacSi: bacSi.maBacSi,
      tieuDe: "📅 Lịch hẹn khám",
      noiDung: "Bạn có lịch hẹn khám với bác sĩ vào ngày mai lúc 9:00 AM. Vui lòng đến đúng giờ.",
      loaiThongBao: "LICH_HEN",
      thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 60 * 60 * 1000)), // 1 giờ trước
      daDoc: true // Đã đọc
    }
  ];
  
  // Thêm từng thông báo
  for (const tb of testNotifications) {
    try {
      await firebase.firestore().collection('ThongBao').doc(tb.maThongBao).set(tb);
      console.log("✅ Đã tạo:", tb.maThongBao, "-", tb.tieuDe);
    } catch (error) {
      console.error("❌ Lỗi tạo thông báo:", tb.maThongBao, error);
    }
  }
  
  console.log("\n🎉 Hoàn thành! Hãy mở app và kiểm tra thông báo.");
  console.log("📱 Hướng dẫn test:");
  console.log("1. Đăng nhập với tài khoản bệnh nhân:", benhNhan.maBenhNhan);
  console.log("2. Click vào icon thông báo");
  console.log("3. Kiểm tra xem có hiển thị 3 thông báo test không");
}

// Chạy hàm tạo thông báo test
createTestNotificationWithRealData();

// Hoặc chạy từng bước riêng lẻ để debug
/*
// Chỉ kiểm tra dữ liệu
firebase.firestore().collection('BenhNhan').limit(1).get().then(snapshot => {
  if (!snapshot.empty) {
    console.log("Bệnh nhân đầu tiên:", snapshot.docs[0].data());
  }
});

firebase.firestore().collection('BacSi').limit(1).get().then(snapshot => {
  if (!snapshot.empty) {
    console.log("Bác sĩ đầu tiên:", snapshot.docs[0].data());
  }
});
*/