// Script để đổ dữ liệu vào collection DuocPham
// Chạy với Node.js và Firebase Admin SDK

const admin = require('firebase-admin');

// Khởi tạo Firebase Admin SDK với service account key
// Bạn cần tải service account key từ Firebase Console
try {
  const serviceAccount = require('./firebase-service-account.json');
  
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  
  console.log('Firebase Admin SDK đã được khởi tạo thành công');
} catch (error) {
  console.error('Lỗi khởi tạo Firebase:', error.message);
  console.log('\nHướng dẫn:');
  console.log('1. Vào Firebase Console → Project Settings → Service accounts');
  console.log('2. Click "Generate new private key"');
  console.log('3. Lưu file JSON với tên "firebase-service-account.json" trong thư mục này');
  process.exit(1);
}

const db = admin.firestore();

const duocPhamData = [
  {
    maDuocPham: "DP005",
    tenDuocPham: "Ibuprofen 400mg",
    donViTinh: "Viên",
    giaBan: 3500
  },
  {
    maDuocPham: "DP006",
    tenDuocPham: "Paracetamol 500mg",
    donViTinh: "Viên",
    giaBan: 2000
  },
  {
    maDuocPham: "DP007",
    tenDuocPham: "Amoxicillin 250mg",
    donViTinh: "Viên",
    giaBan: 4500
  },
  {
    maDuocPham: "DP008",
    tenDuocPham: "Aspirin 100mg",
    donViTinh: "Viên",
    giaBan: 1500
  },
  {
    maDuocPham: "DP009",
    tenDuocPham: "Cetirizine 10mg",
    donViTinh: "Viên",
    giaBan: 3000
  },
  {
    maDuocPham: "DP010",
    tenDuocPham: "Omeprazole 20mg",
    donViTinh: "Viên",
    giaBan: 5500
  },
  {
    maDuocPham: "DP011",
    tenDuocPham: "Metformin 500mg",
    donViTinh: "Viên",
    giaBan: 4000
  },
  {
    maDuocPham: "DP012",
    tenDuocPham: "Simvastatin 20mg",
    donViTinh: "Viên",
    giaBan: 6000
  },
  {
    maDuocPham: "DP013",
    tenDuocPham: "Amlodipine 5mg",
    donViTinh: "Viên",
    giaBan: 3500
  },
  {
    maDuocPham: "DP014",
    tenDuocPham: "Losartan 50mg",
    donViTinh: "Viên",
    giaBan: 7000
  },
  {
    maDuocPham: "DP015",
    tenDuocPham: "Clopidogrel 75mg",
    donViTinh: "Viên",
    giaBan: 8500
  },
  {
    maDuocPham: "DP016",
    tenDuocPham: "Atorvastatin 20mg",
    donViTinh: "Viên",
    giaBan: 9000
  },
  {
    maDuocPham: "DP017",
    tenDuocPham: "Diclofenac 50mg",
    donViTinh: "Viên",
    giaBan: 2500
  },
  {
    maDuocPham: "DP018",
    tenDuocPham: "Prednisolone 5mg",
    donViTinh: "Viên",
    giaBan: 4500
  },
  {
    maDuocPham: "DP019",
    tenDuocPham: "Furosemide 40mg",
    donViTinh: "Viên",
    giaBan: 3000
  },
  {
    maDuocPham: "DP020",
    tenDuocPham: "Captopril 25mg",
    donViTinh: "Viên",
    giaBan: 2800
  },
  {
    maDuocPham: "DP021",
    tenDuocPham: "Glibenclamide 5mg",
    donViTinh: "Viên",
    giaBan: 3200
  },
  {
    maDuocPham: "DP022",
    tenDuocPham: "Ranitidine 150mg",
    donViTinh: "Viên",
    giaBan: 2200
  },
  {
    maDuocPham: "DP023",
    tenDuocPham: "Ciprofloxacin 500mg",
    donViTinh: "Viên",
    giaBan: 5000
  },
  {
    maDuocPham: "DP024",
    tenDuocPham: "Doxycycline 100mg",
    donViTinh: "Viên",
    giaBan: 4200
  }
];

// Hàm để thêm dữ liệu vào Firestore
async function insertDuocPhamData() {
  try {
    const batch = db.batch();
    
    duocPhamData.forEach((duocPham) => {
      const docRef = db.collection('DuocPham').doc(duocPham.maDuocPham);
      batch.set(docRef, duocPham);
    });
    
    await batch.commit();
    console.log(`Đã thêm thành công ${duocPhamData.length} dược phẩm vào collection DuocPham`);
    
    // In ra danh sách đã thêm
    duocPhamData.forEach((duocPham, index) => {
      console.log(`${index + 1}. ${duocPham.maDuocPham} - ${duocPham.tenDuocPham} - ${duocPham.giaBan}đ/${duocPham.donViTinh}`);
    });
    
  } catch (error) {
    console.error('Lỗi khi thêm dữ liệu:', error);
  }
}

// Gọi hàm để thực thi
insertDuocPhamData();

// Nếu chạy trong Firebase Console, sử dụng code này:
/*
duocPhamData.forEach(async (duocPham) => {
  await db.collection('DuocPham').doc(duocPham.maDuocPham).set(duocPham);
  console.log(`Đã thêm: ${duocPham.maDuocPham} - ${duocPham.tenDuocPham}`);
});
*/