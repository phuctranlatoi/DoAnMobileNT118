// Script để chạy trực tiếp trong Firebase Console
// Vào Firebase Console → Firestore → Rules → Console tab
// Copy và paste code này vào console

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
  }
];

// Chạy từng dòng này trong Firebase Console:

// Bước 1: Khởi tạo batch
const batch = firebase.firestore().batch();

// Bước 2: Thêm từng document vào batch
duocPhamData.forEach((duocPham) => {
  const docRef = firebase.firestore().collection('DuocPham').doc(duocPham.maDuocPham);
  batch.set(docRef, duocPham);
});

// Bước 3: Commit batch
batch.commit().then(() => {
  console.log('Đã thêm thành công tất cả dược phẩm!');
  duocPhamData.forEach((duocPham, index) => {
    console.log(`${index + 1}. ${duocPham.maDuocPham} - ${duocPham.tenDuocPham} - ${duocPham.giaBan}đ/${duocPham.donViTinh}`);
  });
}).catch((error) => {
  console.error('Lỗi khi thêm dữ liệu:', error);
});