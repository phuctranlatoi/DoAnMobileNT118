// Script tạo thông báo mẫu để test hệ thống
// Chạy trong Firebase Console → Firestore Database → Console tab

// Thay đổi các mã này theo dữ liệu thực tế trong Firestore
const TEST_MA_BENH_NHAN = "BN001"; // Thay bằng mã bệnh nhân thực tế
const TEST_MA_BAC_SI = "BS001";    // Thay bằng mã bác sĩ thực tế

const testNotifications = [
  {
    maThongBao: "TB_TEST_001",
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Nhắc nhở uống thuốc",
    noiDung: "Đã đến giờ uống thuốc buổi sáng. Vui lòng uống Paracetamol 500mg theo đơn.",
    loaiThongBao: "NHAC_THUOC",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  },
  {
    maThongBao: "TB_TEST_002",
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Lịch hẹn khám sắp tới",
    noiDung: "Bạn có lịch hẹn khám với bác sĩ vào ngày mai lúc 9:00 AM. Vui lòng đến đúng giờ.",
    loaiThongBao: "LICH_HEN",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  },
  {
    maThongBao: "TB_TEST_003",
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Kết quả xét nghiệm",
    noiDung: "Kết quả xét nghiệm của bạn đã có. Vui lòng liên hệ bác sĩ để được tư vấn chi tiết.",
    loaiThongBao: "THONG_BAO_CHUNG",
    thoiGianGui: firebase.firestore.Timestamp.now(),
    daDoc: false
  },
  {
    maThongBao: "TB_TEST_004",
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Nhắc nhở uống thuốc buổi trưa",
    noiDung: "Đã đến giờ uống thuốc buổi trưa. Vui lòng uống Ibuprofen 400mg sau bữa ăn.",
    loaiThongBao: "NHAC_THUOC",
    thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 2 * 60 * 60 * 1000)), // 2 giờ trước
    daDoc: true // Đã đọc
  },
  {
    maThongBao: "TB_TEST_005",
    maBenhNhan: TEST_MA_BENH_NHAN,
    maBacSi: TEST_MA_BAC_SI,
    tieuDe: "Thay đổi lịch hẹn",
    noiDung: "Lịch hẹn khám của bạn đã được thay đổi từ 2:00 PM sang 3:00 PM cùng ngày.",
    loaiThongBao: "LICH_HEN",
    thoiGianGui: firebase.firestore.Timestamp.fromDate(new Date(Date.now() - 24 * 60 * 60 * 1000)), // 1 ngày trước
    daDoc: false
  }
];

// Hàm thêm thông báo
async function createTestNotifications() {
  console.log('Bắt đầu tạo thông báo test...');
  
  for (let i = 0; i < testNotifications.length; i++) {
    const tb = testNotifications[i];
    try {
      await firebase.firestore().collection('ThongBao').doc(tb.maThongBao).set(tb);
      console.log(`✓ Đã tạo thông báo ${i + 1}: ${tb.maThongBao} - ${tb.tieuDe}`);
    } catch (error) {
      console.error(`✗ Lỗi tạo thông báo ${tb.maThongBao}:`, error);
    }
  }
  
  console.log(`\n🎉 Hoàn thành! Đã tạo ${testNotifications.length} thông báo test.`);
  console.log('\n📱 Hướng dẫn test:');
  console.log('1. Mở app với tài khoản bệnh nhân có mã:', TEST_MA_BENH_NHAN);
  console.log('2. Click vào icon thông báo ở góc phải');
  console.log('3. Kiểm tra xem có hiển thị các thông báo không');
  console.log('4. Click vào từng thông báo để xem chi tiết');
}

// Chạy hàm
createTestNotifications();

// Hoặc chạy từng lệnh riêng lẻ:
/*
testNotifications.forEach(async (tb, index) => {
  await firebase.firestore().collection('ThongBao').doc(tb.maThongBao).set(tb);
  console.log(`Đã tạo thông báo ${index + 1}:`, tb.tieuDe);
});
*/