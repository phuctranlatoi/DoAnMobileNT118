// Script tạo thông báo test đơn giản
// Chạy trong Firebase Console → Firestore Database → Console tab

// Thay đổi các mã này theo dữ liệu thực tế trong app của bạn
const TEST_MA_BENH_NHAN = "BN001"; // Thay bằng mã bệnh nhân thực tế
const TEST_MA_BAC_SI = "BS001";    // Thay bằng mã bác sĩ thực tế

// Nếu không biết mã thực tế, chạy lệnh này trước:
/*
firebase.firestore().collection('BenhNhan').limit(1).get().then(snapshot => {
  if (!snapshot.empty) {
    console.log("Mã bệnh nhân:", snapshot.docs[0].data().maBenhNhan);
  }
});

firebase.firestore().collection('BacSi').limit(1).get().then(snapshot => {
  if (!snapshot.empty) {
    console.log("Mã bác sĩ:", snapshot.docs[0].data().maBacSi);
  }
});
*/

// Tạo thông báo test
const testNotifications = [
  {
    maThongBao: "TB_SIMPLE_001",
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Thông báo test 1",
    noiDung: "Đây là thông báo test đầu tiên. Nếu bạn thấy được tin nhắn này, hệ thống đang hoạt động.",
    loaiThongBao: "THONG_BAO_CHUNG",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  },
  {
    maThongBao: "TB_SIMPLE_002", 
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Nhắc nhở uống thuốc",
    noiDung: "Đã đến giờ uống thuốc buổi sáng.",
    loaiThongBao: "NHAC_THUOC",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  }
];

// Thêm thông báo
testNotifications.forEach(async (tb) => {
  try {
    await firebase.firestore().collection('ThongBao').doc(tb.maThongBao).set(tb);
    console.log('✅ Đã tạo thông báo:', tb.maThongBao, '-', tb.tieuDe);
  } catch (error) {
    console.error('❌ Lỗi tạo thông báo:', tb.maThongBao, error);
  }
});

console.log('🎉 Hoàn thành! Kiểm tra app để xem thông báo.');
console.log('📱 Lưu ý: Đảm bảo mã bệnh nhân và bác sĩ đúng với dữ liệu trong app.');