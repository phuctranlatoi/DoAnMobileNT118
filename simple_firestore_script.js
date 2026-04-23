// Script đơn giản để chạy trong Firebase Console
// QUAN TRỌNG: Phải vào đúng trang Firebase Console → Firestore Database → Data tab
// Sau đó mở Developer Tools (F12) → Console tab

const duocPhamData = [
  {maDuocPham: "DP005", tenDuocPham: "Ibuprofen 400mg", donViTinh: "Viên", giaBan: 3500},
  {maDuocPham: "DP006", tenDuocPham: "Paracetamol 500mg", donViTinh: "Viên", giaBan: 2000},
  {maDuocPham: "DP007", tenDuocPham: "Amoxicillin 250mg", donViTinh: "Viên", giaBan: 4500},
  {maDuocPham: "DP008", tenDuocPham: "Aspirin 100mg", donViTinh: "Viên", giaBan: 1500},
  {maDuocPham: "DP009", tenDuocPham: "Cetirizine 10mg", donViTinh: "Viên", giaBan: 3000},
  {maDuocPham: "DP010", tenDuocPham: "Omeprazole 20mg", donViTinh: "Viên", giaBan: 5500},
  {maDuocPham: "DP011", tenDuocPham: "Metformin 500mg", donViTinh: "Viên", giaBan: 4000},
  {maDuocPham: "DP012", tenDuocPham: "Simvastatin 20mg", donViTinh: "Viên", giaBan: 6000},
  {maDuocPham: "DP013", tenDuocPham: "Amlodipine 5mg", donViTinh: "Viên", giaBan: 3500},
  {maDuocPham: "DP014", tenDuocPham: "Losartan 50mg", donViTinh: "Viên", giaBan: 7000},
  {maDuocPham: "DP015", tenDuocPham: "Clopidogrel 75mg", donViTinh: "Viên", giaBan: 8500},
  {maDuocPham: "DP016", tenDuocPham: "Atorvastatin 20mg", donViTinh: "Viên", giaBan: 9000},
  {maDuocPham: "DP017", tenDuocPham: "Diclofenac 50mg", donViTinh: "Viên", giaBan: 2500},
  {maDuocPham: "DP018", tenDuocPham: "Prednisolone 5mg", donViTinh: "Viên", giaBan: 4500},
  {maDuocPham: "DP019", tenDuocPham: "Furosemide 40mg", donViTinh: "Viên", giaBan: 3000},
  {maDuocPham: "DP020", tenDuocPham: "Captopril 25mg", donViTinh: "Viên", giaBan: 2800}
];

// Cách 1: Sử dụng với Firebase v8 (cũ)
if (typeof firebase !== 'undefined' && firebase.firestore) {
  console.log('Sử dụng Firebase v8...');
  duocPhamData.forEach(async (duocPham, index) => {
    try {
      await firebase.firestore().collection('DuocPham').doc(duocPham.maDuocPham).set(duocPham);
      console.log(`✓ ${index + 1}. Đã thêm: ${duocPham.maDuocPham} - ${duocPham.tenDuocPham}`);
    } catch (error) {
      console.error(`✗ Lỗi ${duocPham.maDuocPham}:`, error);
    }
  });
}

// Cách 2: Sử dụng với Firebase v9+ (mới)
else if (typeof window !== 'undefined' && window.firebase) {
  console.log('Sử dụng Firebase v9+...');
  const { getFirestore, collection, doc, setDoc } = window.firebase;
  const db = getFirestore();
  
  duocPhamData.forEach(async (duocPham, index) => {
    try {
      await setDoc(doc(db, 'DuocPham', duocPham.maDuocPham), duocPham);
      console.log(`✓ ${index + 1}. Đã thêm: ${duocPham.maDuocPham} - ${duocPham.tenDuocPham}`);
    } catch (error) {
      console.error(`✗ Lỗi ${duocPham.maDuocPham}:`, error);
    }
  });
}

// Cách 3: Thông báo lỗi nếu không tìm thấy Firebase
else {
  console.error('❌ Không tìm thấy Firebase!');
  console.log('📋 Hướng dẫn:');
  console.log('1. Vào Firebase Console: https://console.firebase.google.com/');
  console.log('2. Chọn project của bạn');
  console.log('3. Vào Firestore Database → Data tab');
  console.log('4. Mở Developer Tools (F12) → Console tab');
  console.log('5. Paste script này vào console');
  console.log('\n🔄 Hoặc thêm thủ công:');
  duocPhamData.forEach((duocPham, index) => {
    console.log(`${index + 1}. Collection: DuocPham, Document ID: ${duocPham.maDuocPham}`);
    console.log(`   Data:`, duocPham);
  });
}