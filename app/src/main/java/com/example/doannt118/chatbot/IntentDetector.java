package com.example.doannt118.chatbot;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * INTENT DETECTOR - TỰ BUILD
 * 
 * Phân loại intent bằng keyword matching
 * Đơn giản, nhanh, không cần AI
 */
public class IntentDetector {
    
    public enum Intent {
        // DATA & ACTIONS (Rule-based - Truy xuất Firestore)
        DAT_LICH_KHAM,          // Đặt lịch khám
        XEM_LICH_HEN,           // Xem lịch hẹn
        HUY_LICH,               // Hủy lịch
        TRA_CUU_BAC_SI,         // Tìm bác sĩ
        HUONG_DAN_UONG_THUOC,   // Xem đơn thuốc
        XEM_BENH_AN,            // Xem bệnh án
        XEM_HOA_DON,            // Xem hóa đơn
        THONG_TIN_PHONG_KHAM,   // FAQ phòng khám
        
        // DOCTOR-SPECIFIC INTENTS
        THONG_KE_BENH_NHAN,     // Thống kê bệnh nhân
        XEM_LICH_LAM_VIEC,      // Xem lịch làm việc
        TRA_CUU_BENH_NHAN,      // Tra cứu thông tin bệnh nhân
        TRA_CUU_THUOC,          // Tra cứu thuốc và tương tác
        TAO_BAO_CAO,            // Tạo báo cáo
        GOI_Y_CHAN_DOAN,        // Gợi ý chẩn đoán
        
        // CONVERSATION
        CHAO_HOI,
        CAM_ON,
        XAC_NHAN,
        TU_CHOI,
        
        // FALLBACK (Gemini AI - Tư vấn y tế)
        KHAC                    // Câu hỏi mở, tư vấn
    }
    
    private Map<Intent, List<String>> keywords;
    
    public IntentDetector() {
        initKeywords();
    }
    
    private void initKeywords() {
        keywords = new HashMap<>();
        
        // ============================================
        // DATA & ACTIONS (Rule-based)
        // ============================================
        
        keywords.put(Intent.DAT_LICH_KHAM, Arrays.asList(
            "đặt lịch", "book", "hẹn", "đăng ký", "khám bệnh", "muốn khám", "đặt hẹn"
        ));
        
        keywords.put(Intent.XEM_LICH_HEN, Arrays.asList(
            "xem lịch", "lịch hẹn", "lịch khám", "lịch của tôi", "hẹn nào", "khi nào khám"
        ));
        
        keywords.put(Intent.HUY_LICH, Arrays.asList(
            "hủy lịch", "hủy hẹn", "không đi", "cancel", "bỏ lịch"
        ));
        
        keywords.put(Intent.TRA_CUU_BAC_SI, Arrays.asList(
            "bác sĩ", "doctor", "chuyên khoa", "tìm bác sĩ", "bác sĩ nào"
        ));
        
        keywords.put(Intent.HUONG_DAN_UONG_THUOC, Arrays.asList(
            "thuốc", "uống thuốc", "đơn thuốc", "medication", "thuốc của tôi"
        ));
        
        keywords.put(Intent.XEM_BENH_AN, Arrays.asList(
            "bệnh án", "hồ sơ", "chẩn đoán", "kết quả khám"
        ));
        
        keywords.put(Intent.XEM_HOA_DON, Arrays.asList(
            "hóa đơn", "tiền", "thanh toán", "chi phí", "invoice"
        ));
        
        keywords.put(Intent.THONG_TIN_PHONG_KHAM, Arrays.asList(
            "giờ làm việc", "địa chỉ", "liên hệ", "hotline", "ở đâu"
        ));
        
        // ============================================
        // DOCTOR-SPECIFIC INTENTS
        // ============================================
        
        keywords.put(Intent.THONG_KE_BENH_NHAN, Arrays.asList(
            "thống kê", "số lượng bệnh nhân", "bao nhiêu bệnh nhân", "tổng số", "báo cáo thống kê"
        ));
        
        keywords.put(Intent.XEM_LICH_LAM_VIEC, Arrays.asList(
            "lịch làm việc", "lịch của tôi", "ca làm", "lịch trực", "khi nào làm việc"
        ));
        
        keywords.put(Intent.TRA_CUU_BENH_NHAN, Arrays.asList(
            "thông tin bệnh nhân", "tra cứu bệnh nhân", "tìm bệnh nhân", "bệnh nhân nào", "hồ sơ bệnh nhân"
        ));
        
        keywords.put(Intent.TRA_CUU_THUOC, Arrays.asList(
            "tra cứu thuốc", "thuốc gì", "tương tác thuốc", "thông tin thuốc", "liều dùng"
        ));
        
        keywords.put(Intent.TAO_BAO_CAO, Arrays.asList(
            "tạo báo cáo", "báo cáo", "xuất báo cáo", "report", "thống kê báo cáo"
        ));
        
        keywords.put(Intent.GOI_Y_CHAN_DOAN, Arrays.asList(
            "gợi ý chẩn đoán", "chẩn đoán", "triệu chứng", "bệnh gì", "có thể là"
        ));
        
        // ============================================
        // CONVERSATION
        // ============================================
        
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
        
        // Nếu không match gì → KHAC (Gemini sẽ xử lý)
        return maxScore > 0 ? bestIntent : Intent.KHAC;
    }
    
    /**
     * Normalize text: CHỈ lowercase, GIỮ NGUYÊN dấu tiếng Việt
     */
    private String normalize(String text) {
        if (text == null) return "";
        
        // CHỈ lowercase và trim, KHÔNG bỏ dấu
        return text.toLowerCase().trim();
    }
}
