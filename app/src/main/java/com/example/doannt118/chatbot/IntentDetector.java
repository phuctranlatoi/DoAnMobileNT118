package com.example.doannt118.chatbot;

import java.text.Normalizer;
import java.util.ArrayList;
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
        // DATA & ACTIONS (Rule-based)
        // ============================================
        
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
            "work schedule", "quản lý lịch làm việc"
        ));
        
        keywords.put(Intent.QUAN_LY_BENH_AN, Arrays.asList(
            "quản lý bệnh án", "bệnh án bệnh nhân", "medical record management",
            "hồ sơ bệnh nhân", "cập nhật bệnh án"
        ));
        
        keywords.put(Intent.XAC_NHAN_LICH_KHAM, Arrays.asList(
            "xác nhận lịch", "duyệt lịch", "confirm appointment", "phê duyệt lịch khám",
            "xác nhận lịch khám"
        ));
        
        keywords.put(Intent.QUAN_LY_DON_THUOC_BS, Arrays.asList(
            "quản lý đơn thuốc", "kê đơn", "prescription management", "đơn thuốc bệnh nhân",
            "tạo đơn thuốc"
        ));
        
        keywords.put(Intent.NHAP_MA_KHAM, Arrays.asList(
            "nhập mã khám", "mã khám", "patient code", "mã bệnh nhân"
        ));
        
        keywords.put(Intent.XEM_BENH_NHAN_NGAY, Arrays.asList(
            "bệnh nhân hôm nay", "danh sách bệnh nhân", "ai khám hôm nay", 
            "lịch khám hôm nay", "bệnh nhân ngày", "patient today", "patients list"
        ));
        
        keywords.put(Intent.THONG_KE_BAC_SI, Arrays.asList(
            "thống kê", "báo cáo", "doanh thu", "số lượng bệnh nhân", "statistics",
            "report", "revenue", "patient count", "hiệu suất"
        ));
        
        keywords.put(Intent.GUI_THONG_BAO, Arrays.asList(
            "gửi thông báo", "thông báo bệnh nhân", "send notification", 
            "tin nhắn thông báo"
        ));
        
        keywords.put(Intent.CHAT_VOI_BENH_NHAN, Arrays.asList(
            "chat bệnh nhân", "tin nhắn bệnh nhân", "danh sách tin nhắn",
            "message patient", "patient chat"
        ));
        
        keywords.put(Intent.AI_ASSISTANT, Arrays.asList(
            "ai assistant", "trợ lý ai", "hỗ trợ ai", "ai support", "chatbot bác sĩ"
        ));
        
        // ============================================
        // CONVERSATION
        // ============================================
        
        keywords.put(Intent.CHON_ROLE, Arrays.asList(
            "bệnh nhân", "bác sĩ", "patient", "doctor", "role", "vai trò"
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
     * Detect intent từ user message - NÂNG CAP HIỂU NGÔN NGỮ TỰ NHIÊN
     */
    public Intent detect(String message) {
        String normalized = normalize(message);
        
        // BƯỚC 1: Kiểm tra các pattern ngôn ngữ tự nhiên trước
        Intent naturalLanguageIntent = detectNaturalLanguagePatterns(normalized);
        if (naturalLanguageIntent != Intent.KHAC) {
            return naturalLanguageIntent;
        }
        
        // BƯỚC 2: Fallback về keyword matching cũ
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
     * PHÁT HIỆN PATTERN NGÔN NGỮ TỰ NHIÊN
     * Hiểu các câu như "tôi muốn...", "cho tôi xem...", "làm sao để..."
     */
    private Intent detectNaturalLanguagePatterns(String message) {
        
        // ============================================
        // PATTERN ĐẶT LỊCH KHÁM
        // ============================================
        if (containsAny(message, Arrays.asList(
            "tôi muốn đặt lịch", "tôi cần đặt lịch", "muốn đặt lịch khám",
            "đặt lịch khám bệnh", "đặt hẹn khám", "book lịch khám",
            "tôi muốn khám", "cần khám bệnh", "muốn đi khám",
            "làm sao để đặt lịch", "đặt lịch như thế nào"
        ))) {
            return Intent.DAT_LICH_KHAM;
        }
        
        // ============================================
        // PATTERN XEM LỊCH HẸN
        // ============================================
        if (containsAny(message, Arrays.asList(
            "xem lịch hẹn", "xem lịch khám", "lịch khám của tôi",
            "cho tôi xem lịch", "tôi có lịch nào", "lịch hẹn nào",
            "khi nào tôi khám", "ngày nào tôi khám", "lịch của tôi",
            "xem appointment", "check lịch hẹn"
        ))) {
            return Intent.XEM_LICH_HEN;
        }
        
        // ============================================
        // PATTERN HỦY LỊCH
        // ============================================
        if (containsAny(message, Arrays.asList(
            "hủy lịch khám", "hủy lịch hẹn", "cancel lịch",
            "tôi muốn hủy lịch", "không đi khám nữa", "bỏ lịch hẹn",
            "hủy appointment", "không khám nữa"
        ))) {
            return Intent.HUY_LICH;
        }
        
        // ============================================
        // PATTERN TRA CỨU BÁC SĨ
        // ============================================
        if (containsAny(message, Arrays.asList(
            "tìm bác sĩ", "bác sĩ nào", "doctor nào", "chuyên khoa nào",
            "bác sĩ giỏi", "bác sĩ chuyên", "tư vấn bác sĩ",
            "gợi ý bác sĩ", "bác sĩ tim mạch", "bác sĩ nội khoa"
        ))) {
            return Intent.TRA_CUU_BAC_SI;
        }
        
        // ============================================
        // PATTERN XEM LỊCH BÁC SĨ THEO NGÀY
        // ============================================
        if (containsAny(message, Arrays.asList(
            "lịch bác sĩ", "bác sĩ làm việc ngày", "bác sĩ có lịch ngày",
            "ngày nào có bác sĩ", "bác sĩ khám ngày", "lịch khám bác sĩ",
            "bác sĩ rảnh ngày", "xem lịch bác sĩ", "bác sĩ nào làm việc",
            "hôm nay có bác sĩ nào", "ngày mai có bác sĩ nào",
            "bác sĩ nào khám hôm nay", "bác sĩ nào khám ngày mai"
        ))) {
            return Intent.XEM_LICH_BAC_SI;
        }
        
        // ============================================
        // PATTERN THUỐC
        // ============================================
        if (containsAny(message, Arrays.asList(
            "xem đơn thuốc", "thuốc của tôi", "đơn thuốc nào",
            "tôi uống thuốc gì", "medication của tôi", "prescription",
            "hướng dẫn uống thuốc", "cách uống thuốc", "lịch uống thuốc"
        ))) {
            return Intent.HUONG_DAN_UONG_THUOC;
        }
        
        // ============================================
        // PATTERN BỆNH ÁN
        // ============================================
        if (containsAny(message, Arrays.asList(
            "xem bệnh án", "hồ sơ bệnh án", "kết quả khám",
            "chẩn đoán của tôi", "bệnh án của tôi", "medical record",
            "lịch sử khám bệnh", "kết quả xét nghiệm"
        ))) {
            return Intent.XEM_BENH_AN;
        }
        
        // ============================================
        // PATTERN HÓA ĐƠN
        // ============================================
        if (containsAny(message, Arrays.asList(
            "xem hóa đơn", "chi phí khám", "tiền khám", "invoice",
            "thanh toán", "bill", "phí khám bệnh", "giá khám"
        ))) {
            return Intent.XEM_HOA_DON;
        }
        
        // ============================================
        // PATTERN THÔNG TIN PHÒNG KHÁM
        // ============================================
        if (containsAny(message, Arrays.asList(
            "phòng khám ở đâu", "địa chỉ phòng khám", "giờ làm việc",
            "liên hệ phòng khám", "hotline", "số điện thoại",
            "phòng khám mở cửa", "đóng cửa lúc nào"
        ))) {
            return Intent.THONG_TIN_PHONG_KHAM;
        }
        
        // ============================================
        // PATTERN CHÀO HỎI
        // ============================================
        if (containsAny(message, Arrays.asList(
            "xin chào", "chào bạn", "hello", "hi", "hey",
            "chào trợ lý", "chào bot", "good morning", "good afternoon"
        ))) {
            return Intent.CHAO_HOI;
        }
        
        // ============================================
        // PATTERN CẢM ƠN
        // ============================================
        if (containsAny(message, Arrays.asList(
            "cảm ơn", "thank you", "thanks", "cám ơn",
            "ok cảm ơn", "được rồi", "ok", "oke"
        ))) {
            return Intent.CAM_ON;
        }
        
        // ============================================
        // PATTERN XÁC NHẬN
        // ============================================
        if (containsAny(message, Arrays.asList(
            "xác nhận", "đồng ý", "ok", "yes", "có", "được",
            "chấp nhận", "đúng rồi", "đúng vậy", "tôi đồng ý"
        ))) {
            return Intent.XAC_NHAN;
        }
        
        // ============================================
        // PATTERN TỪ CHỐI
        // ============================================
        if (containsAny(message, Arrays.asList(
            "không", "hủy", "no", "thôi", "không cần",
            "bỏ qua", "không muốn", "từ chối", "cancel"
        ))) {
            return Intent.TU_CHOI;
        }
        
        // ============================================
        // PATTERN CHO BÁC SĨ
        // ============================================
        if (containsAny(message, Arrays.asList(
            "thống kê bệnh nhân", "số lượng bệnh nhân", "bao nhiêu bệnh nhân",
            "thống kê hôm nay", "báo cáo ngày", "tổng số bệnh nhân"
        ))) {
            return Intent.THONG_KE_BENH_NHAN;
        }
        
        if (containsAny(message, Arrays.asList(
            "lịch làm việc", "lịch của tôi", "ca làm việc",
            "khi nào tôi làm", "lịch trực", "schedule của tôi"
        ))) {
            return Intent.XEM_LICH_LAM_VIEC;
        }
        
        // Không match pattern nào
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
        
        // Variations của "thuốc"
        fixes.put("thuoc", "thuốc");
        fixes.put("medication", "thuốc");
        fixes.put("medicine", "thuốc");
        fixes.put("don thuoc", "đơn thuốc");
        
        // Variations của "hủy"
        fixes.put("huy", "hủy");
        fixes.put("cancel", "hủy");
        fixes.put("hủy bỏ", "hủy");
        
        // Variations của "xác nhận"
        fixes.put("xac nhan", "xác nhận");
        fixes.put("dong y", "đồng ý");
        fixes.put("ok", "đồng ý");
        fixes.put("yes", "đồng ý");
        
        // Apply fixes
        for (Map.Entry<String, String> fix : fixes.entrySet()) {
            text = text.replace(fix.getKey(), fix.getValue());
        }
        
        return text;
    }
    
    /**
     * Fuzzy matching - tìm similarity giữa 2 string
     */
    private boolean fuzzyMatch(String text, String pattern, double threshold) {
        if (text.contains(pattern)) return true;
        
        // Simple Levenshtein distance based similarity
        int distance = levenshteinDistance(text, pattern);
        double similarity = 1.0 - (double) distance / Math.max(text.length(), pattern.length());
        
        return similarity >= threshold;
    }
    
    /**
     * Tính Levenshtein distance
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    /**
     * Xử lý các câu hỏi mơ hồ bằng cách gợi ý
     */
    public List<String> getSuggestions(String message) {
        List<String> suggestions = new ArrayList<>();
        String normalized = normalize(message);
        
        // Nếu chỉ nói "lịch" → gợi ý cụ thể
        if (normalized.contains("lịch") && !normalized.contains("đặt") && !normalized.contains("xem")) {
            suggestions.add("Đặt lịch khám");
            suggestions.add("Xem lịch khám");
            suggestions.add("Hủy lịch khám");
        }
        
        // Nếu chỉ nói "thuốc" → gợi ý cụ thể
        if (normalized.contains("thuốc") && !normalized.contains("xem") && !normalized.contains("đơn")) {
            suggestions.add("Xem đơn thuốc");
            suggestions.add("Hướng dẫn uống thuốc");
            suggestions.add("Tra cứu thuốc");
        }
        
        // Nếu chỉ nói "bác sĩ" → gợi ý cụ thể
        if (normalized.contains("bác sĩ") && !normalized.contains("tìm") && !normalized.contains("chuyên khoa")) {
            suggestions.add("Tìm bác sĩ");
            suggestions.add("Bác sĩ chuyên khoa");
            suggestions.add("Đặt lịch với bác sĩ");
        }
        
        // Câu hỏi chung → gợi ý menu
        if (normalized.length() < 10 || containsAny(normalized, Arrays.asList("giúp", "help", "làm gì", "gì"))) {
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
        
        // Câu quá ngắn
        if (normalized.length() < 5) return true;
        
        // Chỉ có 1 từ khóa mà không rõ ý định
        if ((normalized.contains("lịch") && !normalized.contains("đặt") && !normalized.contains("xem")) ||
            (normalized.contains("thuốc") && !normalized.contains("xem") && !normalized.contains("đơn")) ||
            (normalized.contains("bác sĩ") && !normalized.contains("tìm"))) {
            return true;
        }
        
        // Câu hỏi chung chung
        return containsAny(normalized, Arrays.asList("giúp", "help", "làm gì", "gì", "?"));
    }
}