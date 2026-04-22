// Script tự động tạo thông báo test
// Chạy trong Firebase Console → Firestore Database → Console tab

async function autoCreateNotifications() {
  console.log("🔍 Đang kiểm tra dữ liệu...");
  
  try {
    // Lấy bệnh nhân đầu tiên
    const benhNhanSnapshot = await firebase.firestore().collection('BenhNhan').limit(1).get();
    let maBenhNhan = "BN001"; // Mặc định
    
    if (!benhNhanSnapshot.empty) {
      maBenhNhan = benhNhanSnapshot.docs[0].data().maBenhNhan;
      console.log("✅ Tìm thấy bệnh nhân:", maBenhNhan);
    } else {
      console.log("⚠️ Không tìm thấy bệnh nhân, sử dụng mã mặc định:", maBenhNhan);
    }
    
    // Lấy bác sĩ đầu tiên
    const bacSiSnapshot = await firebase.firestore().collection('BacSi').limit(1).get();
    let maBacSi = "BS001"; // Mặc định
    
    if (!bacSiSnapshot.empty) {
      maBacSi = bacSiSnapshot.docs[0].data().maBacSi;
      console.log("✅ Tìm thấy bác sĩ:", maBacSi);
    } else {
      console.log("⚠️ Không tìm thấy bác sĩ, sử dụng mã mặc định:", maBacSi);
    }
    
    // Xóa thông báo test cũ
    console.log("🗑️ Xóa thông báo test cũ...");
    const oldNotifications = await firebase.firestore().collection('ThongBao')
      .where('maThongBao', '>=', 'TB_AUTO_')
      .where('maThongBao', '<', 'TB_AUTO_Z')
      .get();
    
    const deletePromises = oldNotifications.docs.map(doc => doc.ref.delete());
    await Promise.all(deletePromises);
    console.log(`🗑️ Đã xóa ${oldNotifications.size} thông báo test cũ`);
    
    // Tạo thông báo mới
    console.log("📝 Tạo thông báo mới...");
    
    const notifications = [
      {
        maThongBao: "TB_AUTO_001",
        maBenhNhan: maBenhNhan,
        maBacSi: maBacSi,
        tieuDe: "🔔 Thông báo hệ thống",
        noiDung: "Chào mừng bạn đến với hệ thống thông báo! Đây là thông báo test để kiểm tra tính năng.",
        loaiThongBao: "THONG_BAO_CHUNG",
        thoiGianGui: firebase.firestore.Timestamp.now(),
        daDoc: false
      },
      {
        maThongBao: "TB_AUTO_002",
        maBenhNhan: maBenhNhan,
        maBacSi: maBacSi,
        tieuDe: "💊 Nhắc nhở uống thuốc",
        noiDung: "Đã đến giờ uống thuốc buổi sáng. Vui lòng uống Paracetamol 500mg theo đơn thuốc.",
        loaiThongBao: "NHAC_THUOC",
        thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 30 * 60 * 1000)), // 30 phút trước
        daDoc: false
      },
      {
        maThongBao: "TB_AUTO_003",
        maBenhNhan: maBenhNhan,
        maBacSi: maBacSi,
        tieuDe: "📅 Lịch hẹn khám",
        noiDung: "Bạn có lịch hẹn khám với bác sĩ vào ngày mai lúc 9:00 AM. Vui lòng đến đúng giờ và mang theo giấy tờ cần thiết.",
        loaiThongBao: "LICH_HEN",
        thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 2 * 60 * 60 * 1000)), // 2 giờ trước
        daDoc: false
      },
      {
        maThongBao: "TB_AUTO_004",
        maBenhNhan: maBenhNhan,
        maBacSi: maBacSi,
        tieuDe: "✅ Kết quả xét nghiệm",
        noiDung: "Kết quả xét nghiệm máu của bạn đã có. Các chỉ số đều trong giới hạn bình thường. Vui lòng liên hệ bác sĩ nếu có thắc mắc.",
        loaiThongBao: "THONG_BAO_CHUNG",
        thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 24 * 60 * 60 * 1000)), // 1 ngày trước
        daDoc: true // Đã đọc
      },
      {
        maThongBao: "TB_AUTO_005",
        maBenhNhan: maBenhNhan,
        maBacSi: maBacSi,
        tieuDe: "💊 Nhắc nhở uống thuốc buổi trưa",
        noiDung: "Đã đến giờ uống thuốc buổi trưa. Vui lòng uống Ibuprofen 400mg sau bữa ăn.",
        loaiThongBao: "NHAC_THUOC",
        thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 4 * 60 * 60 * 1000)), // 4 giờ trước
        daDoc: true // Đã đọc
      }
    ];
    
    // Thêm từng thông báo
    for (const notification of notifications) {
      await firebase.firestore().collection('ThongBao').doc(notification.maThongBao).set(notification);
      console.log(`✅ Đã tạo: ${notification.maThongBao} - ${notification.tieuDe}`);
    }
    
    console.log("\n🎉 HOÀN THÀNH!");
    console.log(`📊 Đã tạo ${notifications.length} thông báo test`);
    console.log(`👤 Bệnh nhân: ${maBenhNhan}`);
    console.log(`👨‍⚕️ Bác sĩ: ${maBacSi}`);
    
    console.log("\n📱 HƯỚNG DẪN TEST:");
    console.log("1. Mở app và đăng nhập với tài khoản bệnh nhân");
    console.log("2. Click vào icon thông báo (🔔) ở góc phải toolbar");
    console.log("3. Kiểm tra xem có hiển thị 5 thông báo không");
    console.log("4. Click vào từng thông báo để xem chi tiết");
    
    console.log("\n🔍 KIỂM TRA LOG:");
    console.log("- Mở Android Studio → Logcat");
    console.log("- Filter: ThongBaoActivity");
    console.log("- Xem log khi click nút thông báo");
    
  } catch (error) {
    console.error("❌ Lỗi:", error);
  }
}

// Chạy script
autoCreateNotifications();