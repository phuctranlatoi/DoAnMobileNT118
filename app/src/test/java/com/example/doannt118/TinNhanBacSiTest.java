package com.example.doannt118;

import com.example.doannt118.model.TinNhanBacSi;
import com.google.firebase.Timestamp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;

/**
 * Unit tests for TinNhanBacSi model
 * Tests message model functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class TinNhanBacSiTest {
    
    private TinNhanBacSi tinNhan;
    
    @Before
    public void setUp() {
        tinNhan = new TinNhanBacSi();
    }
    
    @Test
    public void testDefaultConstructor_CreatesEmptyMessage() {
        // Act
        TinNhanBacSi message = new TinNhanBacSi();
        
        // Assert
        assertThat(message).isNotNull();
        assertThat(message.getNoiDung()).isNull();
        assertThat(message.getMaBenhNhan()).isNull();
        assertThat(message.getMaBacSi()).isNull();
        assertThat(message.getThoiGianGui()).isNull();
    }
    
    @Test
    public void testParameterizedConstructor_SetsAllFields() {
        // Arrange
        String noiDung = "Xin chào bác sĩ";
        String maBenhNhan = "BN123";
        String maBacSi = "BS456";
        TinNhanBacSi.LoaiTinNhan loaiTinNhan = TinNhanBacSi.LoaiTinNhan.BENH_NHAN;
        String tenNguoiGui = "Nguyen Van A";
        
        // Act
        TinNhanBacSi message = new TinNhanBacSi(noiDung, maBenhNhan, maBacSi, loaiTinNhan, tenNguoiGui);
        
        // Assert
        assertThat(message.getNoiDung()).isEqualTo(noiDung);
        assertThat(message.getMaBenhNhan()).isEqualTo(maBenhNhan);
        assertThat(message.getMaBacSi()).isEqualTo(maBacSi);
        assertThat(message.getLoaiTinNhan()).isEqualTo(loaiTinNhan);
        assertThat(message.getTenNguoiGui()).isEqualTo(tenNguoiGui);
        assertThat(message.getTrangThai()).isEqualTo(TinNhanBacSi.TrangThaiTinNhan.DA_GUI);
        assertThat(message.getThoiGianGui()).isNotNull();
    }
    
    @Test
    public void testSetNoiDung_ValidContent_SetsCorrectly() {
        // Arrange
        String noiDung = "Tôi cần tư vấn về bệnh";
        
        // Act
        tinNhan.setNoiDung(noiDung);
        
        // Assert
        assertThat(tinNhan.getNoiDung()).isEqualTo(noiDung);
    }
    
    @Test
    public void testSetMaBenhNhan_ValidPatientId_SetsCorrectly() {
        // Arrange
        String maBenhNhan = "BN789";
        
        // Act
        tinNhan.setMaBenhNhan(maBenhNhan);
        
        // Assert
        assertThat(tinNhan.getMaBenhNhan()).isEqualTo(maBenhNhan);
    }
    
    @Test
    public void testSetMaBacSi_ValidDoctorId_SetsCorrectly() {
        // Arrange
        String maBacSi = "BS101";
        
        // Act
        tinNhan.setMaBacSi(maBacSi);
        
        // Assert
        assertThat(tinNhan.getMaBacSi()).isEqualTo(maBacSi);
    }
    
    @Test
    public void testSetThoiGianGui_ValidTimestamp_SetsCorrectly() {
        // Arrange
        Timestamp thoiGian = Timestamp.now();
        
        // Act
        tinNhan.setThoiGianGui(thoiGian);
        
        // Assert
        assertThat(tinNhan.getThoiGianGui()).isEqualTo(thoiGian);
    }
    
    @Test
    public void testSetters_NullValues_AcceptsNull() {
        // Act
        tinNhan.setNoiDung(null);
        tinNhan.setMaBenhNhan(null);
        tinNhan.setMaBacSi(null);
        tinNhan.setThoiGianGui(null);
        
        // Assert
        assertThat(tinNhan.getNoiDung()).isNull();
        assertThat(tinNhan.getMaBenhNhan()).isNull();
        assertThat(tinNhan.getMaBacSi()).isNull();
        assertThat(tinNhan.getThoiGianGui()).isNull();
    }
    
    @Test
    public void testSetters_EmptyStrings_AcceptsEmptyStrings() {
        // Act
        tinNhan.setNoiDung("");
        tinNhan.setMaBenhNhan("");
        tinNhan.setMaBacSi("");
        
        // Assert
        assertThat(tinNhan.getNoiDung()).isEmpty();
        assertThat(tinNhan.getMaBenhNhan()).isEmpty();
        assertThat(tinNhan.getMaBacSi()).isEmpty();
    }
    
    @Test
    public void testMessageFlow_PatientToDoctor() {
        // Arrange
        String noiDung = "Tôi bị đau đầu";
        String maBenhNhan = "BN123";
        String maBacSi = "BS456";
        TinNhanBacSi.LoaiTinNhan loaiTinNhan = TinNhanBacSi.LoaiTinNhan.BENH_NHAN;
        String tenNguoiGui = "Nguyen Van A";
        
        // Act
        TinNhanBacSi message = new TinNhanBacSi(noiDung, maBenhNhan, maBacSi, loaiTinNhan, tenNguoiGui);
        
        // Assert
        assertThat(message.getNoiDung()).isEqualTo(noiDung);
        assertThat(message.getMaBenhNhan()).isEqualTo(maBenhNhan);
        assertThat(message.getMaBacSi()).isEqualTo(maBacSi);
        assertThat(message.getLoaiTinNhan()).isEqualTo(TinNhanBacSi.LoaiTinNhan.BENH_NHAN);
        assertThat(message.getTenNguoiGui()).isEqualTo(tenNguoiGui);
    }
    
    @Test
    public void testMessageFlow_DoctorToPatient() {
        // Arrange
        String noiDung = "Bạn nên uống nhiều nước và nghỉ ngơi";
        String maBenhNhan = "BN123";
        String maBacSi = "BS456";
        TinNhanBacSi.LoaiTinNhan loaiTinNhan = TinNhanBacSi.LoaiTinNhan.BAC_SI;
        String tenNguoiGui = "Dr. Tran Van B";
        
        // Act
        TinNhanBacSi message = new TinNhanBacSi(noiDung, maBenhNhan, maBacSi, loaiTinNhan, tenNguoiGui);
        
        // Assert
        assertThat(message.getNoiDung()).isEqualTo(noiDung);
        assertThat(message.getMaBenhNhan()).isEqualTo(maBenhNhan);
        assertThat(message.getMaBacSi()).isEqualTo(maBacSi);
        assertThat(message.getLoaiTinNhan()).isEqualTo(TinNhanBacSi.LoaiTinNhan.BAC_SI);
        assertThat(message.getTenNguoiGui()).isEqualTo(tenNguoiGui);
    }
    
    @Test
    public void testLoaiTinNhan_Enum_WorksCorrectly() {
        // Test enum values
        assertThat(TinNhanBacSi.LoaiTinNhan.BENH_NHAN).isNotNull();
        assertThat(TinNhanBacSi.LoaiTinNhan.BAC_SI).isNotNull();
        
        // Test setting enum
        tinNhan.setLoaiTinNhan(TinNhanBacSi.LoaiTinNhan.BENH_NHAN);
        assertThat(tinNhan.getLoaiTinNhan()).isEqualTo(TinNhanBacSi.LoaiTinNhan.BENH_NHAN);
        
        tinNhan.setLoaiTinNhan(TinNhanBacSi.LoaiTinNhan.BAC_SI);
        assertThat(tinNhan.getLoaiTinNhan()).isEqualTo(TinNhanBacSi.LoaiTinNhan.BAC_SI);
    }
    
    @Test
    public void testTrangThaiTinNhan_Enum_WorksCorrectly() {
        // Test enum values
        assertThat(TinNhanBacSi.TrangThaiTinNhan.DA_GUI).isNotNull();
        assertThat(TinNhanBacSi.TrangThaiTinNhan.DA_NHAN).isNotNull();
        assertThat(TinNhanBacSi.TrangThaiTinNhan.DA_XEM).isNotNull();
        
        // Test setting enum
        tinNhan.setTrangThai(TinNhanBacSi.TrangThaiTinNhan.DA_GUI);
        assertThat(tinNhan.getTrangThai()).isEqualTo(TinNhanBacSi.TrangThaiTinNhan.DA_GUI);
        
        tinNhan.setTrangThai(TinNhanBacSi.TrangThaiTinNhan.DA_NHAN);
        assertThat(tinNhan.getTrangThai()).isEqualTo(TinNhanBacSi.TrangThaiTinNhan.DA_NHAN);
        
        tinNhan.setTrangThai(TinNhanBacSi.TrangThaiTinNhan.DA_XEM);
        assertThat(tinNhan.getTrangThai()).isEqualTo(TinNhanBacSi.TrangThaiTinNhan.DA_XEM);
    }
}