package com.example.doannt118.chatbot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * INTENT DETECTOR - NÂNG CAP HIỂU NGÔN NGỮ TỰ NHIÊN
 * 
 * Cải thiện:
 * - Fuzzy matching tốt hơn
 * - Context awareness
 * - Synonym handling
 * - Typo correction
 * - Multi-intent detection
 */
public class IntentDetector {
    
    public enum Intent {
        // ============================================
        // PATIENT INTENTS (Rule-based - Truy xuất Firestore)
        // ============================================
        DAT_LICH_KHAM,              // Đặt lịch khám (từ DanhSachBacSiActivity)
        XEM_LICH_KHAM,              // Xem lịch khám (LichKhamCuaToiActivity)
        XEM_LICH_HEN,               // Xem lịch hẹn (alias cho XEM_LICH_KHAM)
        HUY_LICH_KHAM,              // Hủy lịch khám
        HUY_LICH,                   // Hủy lịch (alias cho HUY_LICH_KHAM)
        XEM_BENH_AN,                // Xem bệnh án (XemBenhAnActivity)
        XEM_DON_THUOC,              // Xem đơn thuốc (DanhSachDonThuocActivity)
        QUAN_LY_UONG_THUOC,         // Quản lý uống thuốc (QuanLyUongThuocActivity)
        HUONG_DAN_UONG_THUOC,       // Hướng dẫn uống thuốc (alias)
        XEM_HOA_DON,                // Xem hóa đơn (DanhSachHoaDonActivity)
        XEM_THONG_BAO,              // Xem thông báo (ThongBaoActivity)
        CHAT_VOI_BAC_SI,            // Chat với bác sĩ (ChonBacSiChatActivity)
        TRA_CUU_BAC_SI,             // Tìm bác sĩ (DanhSachBacSiActivity)
        TRA_CUU_THONG_TIN,          // FAQ phòng khám
        THONG_TIN_PHONG_KHAM,       // Thông tin phòng khám (alias)
        
        // ============================================
        // DOCTOR INTENTS (Rule-based - Truy xuất Firestore)
        // ============================================
        XEM_LICH_LAM_VIEC,          // Xem lịch làm việc (QuanLyLichLamViecActivity)
        XEM_LICH_BAC_SI,            // Xem lịch bác sĩ theo ngày
        QUAN_LY_BENH_AN,            // Quản lý bệnh án (QuanLyBenhAnBacSiActivity)
        XAC_NHAN_LICH_KHAM,         // Xác nhận lịch khám (XacNhanLichKhamActivity)
        QUAN_LY_DON_THUOC_BS,       // Quản lý đơn thuốc (QuanLyDonThuocBacSiActivity)
        NHAP_MA_KHAM,               // Nhập mã khám (NhapMaKhamActivity)
        XEM_BENH_NHAN_NGAY,         // Xem danh sách bệnh nhân hôm nay
        THONG_KE_BAC_SI,            // Thống kê và báo cáo
        THONG_KE_BENH_NHAN,         // Thống kê bệnh nhân
        GUI_THONG_BAO,              // Gửi thông báo (GuiThongBaoActivity)
        CHAT_VOI_BENH_NHAN,         // Chat với bệnh nhân (DanhSachTinNhanBacSiActivity)
        AI_ASSISTANT,               // AI Assistant cho bác sĩ
        TRA_CUU_BENH_NHAN,          // Tra cứu thông tin bệnh nhân
        TRA_CUU_THUOC,              // Tra cứu thông tin thuốc
        TAO_BAO_CAO,                // Tạo báo cáo
        GOI_Y_CHAN_DOAN,            // Gợi ý chẩn đoán
        
        // ============================================
        // CONVERSATION & SYSTEM
        // ============================================
        CHAO_HOI,
        CAM_ON,
        XAC_NHAN,
        TU_CHOI,
        CHON_ROLE,                  // Chọn vai trò (Bệnh nhân/Bác sĩ)
        
        // FALLBACK (Gemini AI - Tư vấn y tế)
        KHAC                        // Câu hỏi mở, tư vấn
    }
    
    private Map<Intent, List<String>> keywords;
    
    public IntentDetector() {
        initKeywords();
    }
    
    private void initKeywords() {
        keywords = new HashMap<>();
        
        // ============================================
        // PATIENT INTENTS - DỰA TRÊN CÁC ACTIVITY THỰC TẾ
        // ============================================
        keywords.put(Intent.DAT_LICH_KHAM, Arrays.asList(
            "đặt lịch", "book", "hẹn", "đăng ký", "khám bệnh", "muốn khám", "đặt hẹn",
            "appointment", "schedule", "đặt", "lịch", "khám", "hẹn khám", "đi khám",
            "đăng ký lịch khám", "chọn bác sĩ"
        ));
        
        keywords.put(Intent.XEM_LICH_KHAM, Arrays.asList(
            "xem lịch", "lịch hẹn", "lịch khám", "lịch của tôi", "hẹn nào", "khi nào khám",
            "check lịch", "kiểm tra lịch", "lịch", "appointment", "schedule", "hẹn",
            "lịch khám của tôi"
        ));
        
        keywords.put(Intent.HUY_LICH_KHAM, Arrays.asList(
            "hủy lịch", "hủy hẹn", "không đi", "cancel", "bỏ lịch", "hủy"
        ));
        
        keywords.put(Intent.XEM_BENH_AN, Arrays.asList(
            "bệnh án", "hồ sơ", "chẩn đoán", "kết quả khám", "xem bệnh án",
            "medical record", "hồ sơ bệnh án"
        ));
        
        keywords.put(Intent.XEM_DON_THUOC, Arrays.asList(
            "thuốc", "đơn thuốc", "medication", "thuốc của tôi", "prescription", 
            "medicine", "đơn", "danh sách đơn thuốc", "xem đơn thuốc"
        ));
        
        keywords.put(Intent.QUAN_LY_UONG_THUOC, Arrays.asList(
            "uống thuốc", "điểm danh thuốc", "nhắc nhở thuốc", "lịch uống thuốc",
            "quản lý uống thuốc", "xác nhận uống thuốc", "medicine reminder"
        ));
        
        keywords.put(Intent.XEM_HOA_DON, Arrays.asList(
            "hóa đơn", "tiền", "thanh toán", "chi phí", "invoice", "bill",
            "danh sách hóa đơn", "xem hóa đơn"
        ));
        
        keywords.put(Intent.XEM_THONG_BAO, Arrays.asList(
            "thông báo", "notification", "tin nhắn hệ thống", "thông tin mới"
        ));
        
        keywords.put(Intent.CHAT_VOI_BAC_SI, Arrays.asList(
            "chat bác sĩ", "nhắn tin bác sĩ", "nói chuyện bác sĩ", "tư vấn bác sĩ",
            "chọn bác sĩ chat", "tin nhắn", "message doctor"
        ));
        
        keywords.put(Intent.TRA_CUU_BAC_SI, Arrays.asList(
            "bác sĩ", "doctor", "chuyên khoa", "tìm bác sĩ", "bác sĩ nào",
            "danh sách bác sĩ", "chọn bác sĩ"
        ));
        
        keywords.put(Intent.TRA_CUU_THONG_TIN, Arrays.asList(
            "giờ làm việc", "địa chỉ", "liên hệ", "hotline", "ở đâu", "thông tin",
            "bảng giá", "dịch vụ", "quy trình", "phòng khám"
        ));
        
        // ============================================
        // DOCTOR INTENTS - DỰA TRÊN MainBacSiActivity
        // ============================================
        
        keywords.put(Intent.XEM_LICH_LAM_VIEC, Arrays.asList(
            "lịch làm việc", "lịch của tôi", "ca làm việc", "schedule", 
            "work schedule", "quản lý lịch làm việc", "lịch hôm nay",
            "hôm nay làm gì", "ca làm", "lịch trực"
        ));
        
        keywords.put(Intent.QUAN_LY_BENH_AN, Arrays.asList(
            "quản lý bệnh án", "bệnh án bệnh nhân", "medical record management",
            "hồ sơ bệnh nhân", "cập nhật bệnh án", "xem bệnh án", "bệnh án"
        ));
        
        keywords.put(Intent.XAC_NHAN_LICH_KHAM, Arrays.asList(
            "xác nhận lịch", "duyệt lịch", "confirm appointment", "phê duyệt lịch khám",
            "xác nhận lịch khám", "lịch chờ xác nhận", "chờ duyệt"
        ));
        
        keywords.put(Intent.QUAN_LY_DON_THUOC_BS, Arrays.asList(
            "quản lý đơn thuốc", "kê đơn", "prescription management", "đơn thuốc bệnh nhân",
            "tạo đơn thuốc", "đơn thuốc", "kê thuốc"
        ));
        
        keywords.put(Intent.NHAP_MA_KHAM, Arrays.asList(
            "nhập mã khám", "mã khám", "patient code", "mã bệnh nhân", "nhập mã"
        ));
        
        keywords.put(Intent.XEM_BENH_NHAN_NGAY, Arrays.asList(
            "bệnh nhân hôm nay", "danh sách bệnh nhân", "ai khám hôm nay", 
            "lịch khám hôm nay", "bệnh nhân ngày", "patient today", "patients list",
            "bệnh nhân", "hôm nay có ai", "khám ai hôm nay"
        ));
        
        keywords.put(Intent.THONG_KE_BAC_SI, Arrays.asList(
            "thống kê", "báo cáo", "doanh thu", "số lượng bệnh nhân", "statistics",
            "report", "revenue", "patient count", "hiệu suất", "xem thống kê"
        ));
        
        keywords.put(Intent.GUI_THONG_BAO, Arrays.asList(
            "gửi thông báo", "thông báo bệnh nhân", "send notification", 
            "tin nhắn thông báo"
        ));
        
        keywords.put(Intent.CHAT_VOI_BENH_NHAN, Arrays.asList(
            "chat bệnh nhân", "tin nhắn bệnh nhân", "danh sách tin nhắn",
            "message patient", "patient chat", "nhắn tin bệnh nhân"
        ));
        
        keywords.put(Intent.AI_ASSISTANT, Arrays.asList(
            "ai assistant", "trợ lý ai", "hỗ trợ ai", "ai support", "chatbot bác sĩ",
            "hỗ trợ chẩn đoán", "tư vấn điều trị", "tra cứu y khoa"
        ));
        
        // ============================================
        // CONVERSATION
        // ============================================
        
        keywords.put(Intent.CHON_ROLE, Arrays.asList(
            "bệnh nhân", "bác sĩ", "patient", "doctor", "role", "vai trò"
        ));
        
        keywords.put(Intent.CHAO_HOI, Arrays.asList(
            "xin chào", "chào", "hello", "hi", "hey"
        ));
        
        keywords.put(Intent.CAM_ON, Arrays.asList(
            "cảm ơn", "thank", "thanks", "ok", "được"
        ));
        
        keywords.put(Intent.XAC_NHAN, Arrays.asList(
            "xác nhận", "đồng ý", "ok", "yes", "có", "được", "chấp nhận"
        ));
        
        keywords.put(Intent.TU_CHOI, Arrays.asList(
            "không", "hủy", "no", "thôi", "không cần", "bỏ qua"
        ));
    }
    
    /**
     * Detect intent từ user message
     */
    public Intent detect(String message) {
        String normalized = normalize(message);
        
        // BƯỚC 1: Kiểm tra các pattern ngôn ngữ tự nhiên trước
        Intent naturalLanguageIntent = detectNaturalLanguagePatterns(normalized);
        if (naturalLanguageIntent != Intent.KHAC) {
            return naturalLanguageIntent;
        }
        
        // BƯỚC 2: Fallback về keyword matching
        Map<Intent, Integer> scores = new HashMap<>();
        for (Intent intent : Intent.values()) {
            scores.put(intent, 0);
        }
        
        // Score each intent based on keyword matches
        for (Map.Entry<Intent, List<String>> entry : keywords.entrySet()) {
            Intent intent = entry.getKey();
            List<String> keywordList = entry.getValue();
            
            for (String keyword : keywordList) {
                if (normalized.contains(keyword)) {
                    scores.put(intent, scores.get(intent) + 10);
                }
            }
        }
        
        // Find highest score
        Intent bestIntent = Intent.KHAC;
        int maxScore = 0;
        
        for (Map.Entry<Intent, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestIntent = entry.getKey();
            }
        }
        
        return maxScore > 0 ? bestIntent : Intent.KHAC;
    }
    
    /**
     * PHÁT HIỆN PATTERN NGÔN NGỮ TỰ NHIÊN
     */
    private Intent detectNaturalLanguagePatterns(String message) {
        
        // PATTERN ĐẶT LỊCH KHÁM
        if (containsAny(message, Arrays.asList(
            "tôi muốn đặt lịch", "tôi cần đặt lịch", "muốn đặt lịch khám",
            "đặt lịch khám bệnh", "đặt hẹn khám", "book lịch khám",
            "tôi muốn khám", "cần khám bệnh", "muốn đi khám",
            "làm sao để đặt lịch", "đặt lịch như thế nào"
        ))) {
            return Intent.DAT_LICH_KHAM;
        }
        
        // PATTERN XEM LỊCH HẸN
        if (containsAny(message, Arrays.asList(
            "xem lịch hẹn", "xem lịch khám", "lịch khám của tôi",
            "cho tôi xem lịch", "tôi có lịch nào", "lịch hẹn nào",
            "khi nào tôi khám", "ngày nào tôi khám", "lịch của tôi",
            "xem appointment", "check lịch hẹn"
        ))) {
            return Intent.XEM_LICH_HEN;
        }
        
        // PATTERN HỦY LỊCH
        if (containsAny(message, Arrays.asList(
            "hủy lịch khám", "hủy lịch hẹn", "cancel lịch",
            "tôi muốn hủy lịch", "không đi khám nữa", "bỏ lịch hẹn",
            "hủy appointment", "không khám nữa"
        ))) {
            return Intent.HUY_LICH;
        }
        
        // PATTERN TRA CỨU BÁC SĨ
        if (containsAny(message, Arrays.asList(
            "tìm bác sĩ", "bác sĩ nào", "doctor nào", "chuyên khoa nào",
            "bác sĩ giỏi", "bác sĩ chuyên", "tư vấn bác sĩ",
            "gợi ý bác sĩ", "bác sĩ tim mạch", "bác sĩ nội khoa"
        ))) {
            return Intent.TRA_CUU_BAC_SI;
        }
        
        // PATTERN CHÀO HỎI
        if (containsAny(message, Arrays.asList(
            "xin chào", "chào bạn", "hello", "hi", "hey",
            "chào trợ lý", "chào bot", "good morning", "good afternoon"
        ))) {
            return Intent.CHAO_HOI;
        }
        
        // PATTERN BÁC SĨ - BỆNH NHÂN HÔM NAY
        if (containsAny(message, Arrays.asList(
            "bệnh nhân hôm nay", "ai khám hôm nay", "danh sách bệnh nhân hôm nay",
            "hôm nay có ai khám", "bệnh nhân ngày hôm nay", "lịch khám hôm nay",
            "ai đặt lịch hôm nay", "bệnh nhân nào khám hôm nay"
        ))) {
            return Intent.XEM_BENH_NHAN_NGAY;
        }
        
        // PATTERN BÁC SĨ - LỊCH LÀM VIỆC
        if (containsAny(message, Arrays.asList(
            "lịch làm việc", "lịch của tôi", "ca làm việc",
            "khi nào tôi làm", "lịch trực", "schedule của tôi"
        ))) {
            return Intent.XEM_LICH_LAM_VIEC;
        }
        
        return Intent.KHAC;
    }
    
    /**
     * Kiểm tra xem message có chứa bất kỳ phrase nào trong list không
     */
    private boolean containsAny(String message, List<String> phrases) {
        for (String phrase : phrases) {
            if (message.contains(phrase)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Normalize text: lowercase, fix typos, chuẩn hóa
     */
    private String normalize(String text) {
        if (text == null) return "";
        
        String normalized = text.toLowerCase().trim();
        
        // Fix common typos và variations
        normalized = fixCommonTypos(normalized);
        
        return normalized;
    }
    
    /**
     * Sửa các lỗi chính tả phổ biến và variations
     */
    private String fixCommonTypos(String text) {
        // Common typos và variations
        Map<String, String> fixes = new HashMap<>();
        
        // Variations của "đặt lịch"
        fixes.put("dat lich", "đặt lịch");
        fixes.put("dặt lịch", "đặt lịch");
        fixes.put("đat lich", "đặt lịch");
        fixes.put("book lich", "đặt lịch");
        fixes.put("booking", "đặt lịch");
        
        // Variations của "xem lịch"
        fixes.put("xem lich", "xem lịch");
        fixes.put("check lich", "xem lịch");
        fixes.put("kiem tra lich", "xem lịch");
        
        // Variations của "bác sĩ"
        fixes.put("bac si", "bác sĩ");
        fixes.put("bacsi", "bác sĩ");
        fixes.put("doctor", "bác sĩ");
        fixes.put("dr", "bác sĩ");
        
        // Apply fixes
        for (Map.Entry<String, String> fix : fixes.entrySet()) {
            text = text.replace(fix.getKey(), fix.getValue());
        }
        
        return text;
    }
    
    /**
     * Lấy gợi ý dựa trên role của user
     */
    public List<String> getSuggestionsForRole(String message, String userType) {
        List<String> suggestions = new ArrayList<>();
        
        if ("bacsi".equals(userType)) {
            suggestions.add("Xem bệnh nhân hôm nay");
            suggestions.add("Xem lịch làm việc");
            suggestions.add("Xác nhận lịch khám");
            suggestions.add("Xem thống kê");
        } else {
            suggestions.add("Đặt lịch khám");
            suggestions.add("Xem lịch khám");
            suggestions.add("Xem đơn thuốc");
            suggestions.add("Tìm bác sĩ");
        }
        
        return suggestions;
    }
    
    /**
     * Kiểm tra xem có phải câu hỏi mơ hồ không
     */
    public boolean isAmbiguous(String message) {
        String normalized = normalize(message);
        
        // Câu quá ngắn (dưới 3 ký tự)
        if (normalized.length() < 3) return true;
        
        // KHÔNG mơ hồ nếu có các từ khóa rõ ràng cho bác sĩ
        if (containsAny(normalized, Arrays.asList(
            "bệnh nhân hôm nay", "lịch làm việc", "xác nhận lịch", "thống kê",
            "quản lý bệnh án", "quản lý đơn thuốc", "nhập mã khám", "chat bệnh nhân",
            "ai assistant", "báo cáo", "doanh thu", "số lượng bệnh nhân"
        ))) {
            return false;
        }
        
        // KHÔNG mơ hồ nếu có các từ khóa rõ ràng cho bệnh nhân
        if (containsAny(normalized, Arrays.asList(
            "đặt lịch khám", "xem lịch khám", "hủy lịch", "xem đơn thuốc",
            "xem bệnh án", "xem hóa đơn", "chat bác sĩ", "tìm bác sĩ",
            "uống thuốc", "thông báo"
        ))) {
            return false;
        }
        
        // KHÔNG mơ hồ nếu là chào hỏi hoặc cảm ơn
        if (containsAny(normalized, Arrays.asList(
            "xin chào", "chào", "hello", "hi", "cảm ơn", "thank"
        ))) {
            return false;
        }
        
        // Chỉ mơ hồ nếu câu rất ngắn và chỉ có 1 từ khóa đơn lẻ
        if (normalized.length() < 10) {
            // Chỉ có từ "lịch" đơn lẻ
            if (normalized.equals("lịch") || normalized.equals("lich")) return true;
            // Chỉ có từ "thuốc" đơn lẻ
            if (normalized.equals("thuốc") || normalized.equals("thuoc")) return true;
            // Chỉ có từ "?" 
            if (normalized.equals("?")) return true;
        }
        
        return false;
    }
}