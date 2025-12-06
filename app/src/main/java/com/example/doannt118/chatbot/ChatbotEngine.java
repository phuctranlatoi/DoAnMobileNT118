package com.example.doannt118.chatbot;

import android.content.Context;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * CHATBOT ENGINE - TỰ BUILD
 * 
 * Xử lý 90% cases bằng rule-based logic
 * Chỉ gọi Gemini khi thực sự cần (10% cases)
 */
public class ChatbotEngine {
    
    private Context context;
    private FirestoreRepository repo;
    private ConversationContext conversationContext;
    private IntentDetector intentDetector;
    private GeminiAssistant geminiAssistant; // Chỉ dùng khi cần
    private String maBenhNhan;
    private String maBacSi;
    private String userType; // "benhnhan" hoặc "bacsi"
    
    public interface ChatCallback {
        void onResponse(ChatResponse response);
        void onError(String error);
    }
    
    // Constructor cũ - backward compatibility
    public ChatbotEngine(Context context, String maBenhNhan) {
        this(context, maBenhNhan, "benhnhan");
    }
    
    // Constructor mới - hỗ trợ cả bác sĩ và bệnh nhân
    public ChatbotEngine(Context context, String userId, String userType) {
        this.context = context;
        this.userType = userType;
        
        if ("bacsi".equals(userType)) {
            this.maBacSi = userId;
        } else {
            this.maBenhNhan = userId;
        }
        
        this.repo = new FirestoreRepository();
        this.conversationContext = new ConversationContext();
        this.conversationContext.setData("userType", userType); // Lưu userType vào context
        this.intentDetector = new IntentDetector();
        this.geminiAssistant = new GeminiAssistant(context); // Backup only
    }
    
    /**
     * MAIN PROCESSING - TỰ BUILD LOGIC
     */
    public void processMessage(String userMessage, ChatCallback callback) {
        
        // Step 1: Detect intent (RULE-BASED)
        IntentDetector.Intent intent = intentDetector.detect(userMessage);
        
        // Step 2: Check conversation state
        ConversationContext.ConversationState state = conversationContext.getState();
        
        // Step 3: Route to appropriate handler (TỰ BUILD)
        if (state != ConversationContext.ConversationState.IDLE) {
            // Đang trong conversation flow
            handleConversationFlow(userMessage, state, callback);
        } else {
            // New intent
            handleNewIntent(intent, userMessage, callback);
        }
    }
    
    /**
     * XỬ LÝ CONVERSATION FLOW - TỰ BUILD
     * VD: Đang đặt lịch, đang chờ user nhập ngày...
     */
    private void handleConversationFlow(String userMessage, 
                                       ConversationContext.ConversationState state, 
                                       ChatCallback callback) {
        
        switch (state) {
            case WAITING_DATE:
                handleDateInput(userMessage, callback);
                break;
                
            case WAITING_DOCTOR_SELECTION:
                handleDoctorSelection(userMessage, callback);
                break;
                
            case WAITING_CONFIRMATION:
                handleConfirmation(userMessage, callback);
                break;
                
            default:
                callback.onError("Unknown state");
        }
    }
    
    /**
     * XỬ LÝ INTENT MỚI
     * 
     * PHÂN LOẠI:
     * - Data/Action intents → Rule-based (TỰ BUILD)
     * - Knowledge/Advisory intents → Gemini AI
     */
    private void handleNewIntent(IntentDetector.Intent intent, 
                                 String userMessage, 
                                 ChatCallback callback) {
        
        // ============================================
        // RULE-BASED: Truy xuất data & Actions
        // ============================================
        switch (intent) {
            case DAT_LICH_KHAM:
                // Action: Đặt lịch khám
                handleBookingIntent(callback);
                break;
                
            case XEM_LICH_HEN:
                // Data: Query lịch hẹn từ Firestore
                handleViewAppointments(callback);
                break;
                
            case HUONG_DAN_UONG_THUOC:
                // Data: Query đơn thuốc từ Firestore
                handleMedicationGuide(callback);
                break;
                
            case TRA_CUU_BAC_SI:
                // Data: Query bác sĩ từ Firestore
                handleDoctorQuery(userMessage, callback);
                break;
                
            case XEM_BENH_AN:
                // Data: Query bệnh án từ Firestore
                handleViewMedicalRecords(callback);
                break;
                
            case XEM_HOA_DON:
                // Data: Query hóa đơn từ Firestore
                handleViewInvoices(callback);
                break;
                
            case HUY_LICH:
                // Action: Hủy lịch hẹn
                handleCancelAppointment(userMessage, callback);
                break;
                
            case THONG_TIN_PHONG_KHAM:
                // Static info: FAQ
                handleClinicInfo(callback);
                break;
                
            case CHAO_HOI:
                handleGreeting(callback);
                break;
                
            case CAM_ON:
                handleThanks(callback);
                break;
                
            // ============================================
            // DOCTOR-SPECIFIC INTENTS
            // ============================================
            case THONG_KE_BENH_NHAN:
                handlePatientStatistics(callback);
                break;
                
            case XEM_LICH_LAM_VIEC:
                handleDoctorSchedule(callback);
                break;
                
            case TRA_CUU_BENH_NHAN:
                handlePatientLookup(userMessage, callback);
                break;
                
            case TRA_CUU_THUOC:
                handleMedicationLookup(userMessage, callback);
                break;
                
            case TAO_BAO_CAO:
                handleCreateReport(callback);
                break;
                
            case GOI_Y_CHAN_DOAN:
                handleDiagnosisSuggestion(userMessage, callback);
                break;
                
            // ============================================
            // GEMINI AI: Tư vấn & Kiến thức y tế
            // ============================================
            case KHAC:
                // Câu hỏi mở, tư vấn y tế
                // VD: "Tôi bị đau đầu nên làm gì?"
                //     "Trước khi khám nên chuẩn bị gì?"
                handleWithGemini(userMessage, callback);
                break;
        }
    }
    
    // ============================================
    // BOOKING FLOW - TỰ BUILD
    // ============================================
    
    private void handleBookingIntent(ChatCallback callback) {
        conversationContext.setState(ConversationContext.ConversationState.WAITING_DATE);
        
        ChatResponse response = new ChatResponse(
            "Tôi sẽ giúp bạn đặt lịch khám! 📅\n\n" +
            "Bạn muốn khám vào ngày nào?\n" +
            "(VD: 15/12, thứ 3 tuần này, ngày mai)",
            ChatResponse.ResponseType.TEXT
        );
        
        // Add quick replies
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Hôm nay");
        quickReplies.add("Ngày mai");
        quickReplies.add("Thứ 2 tuần sau");
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    private void handleDateInput(String userMessage, ChatCallback callback) {
        // Parse date từ user input (TỰ BUILD)
        String date = parseDateFromText(userMessage);
        
        if (date == null) {
            callback.onResponse(new ChatResponse(
                "Xin lỗi, tôi không hiểu ngày bạn muốn khám.\n" +
                "Vui lòng nhập lại (VD: 15/12, ngày mai)",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        // Lưu date vào context
        conversationContext.setData("date", date);
        
        // Query bác sĩ có lịch ngày đó (TỰ BUILD - Query Firestore)
        queryDoctorsByDate(date, callback);
    }
    
    private void queryDoctorsByDate(String date, ChatCallback callback) {
        // TODO: Query Firestore để lấy bác sĩ có lịch ngày này
        // Giả sử có 2 bác sĩ:
        
        List<BacSi> doctors = new ArrayList<>();
        // Mock data - Thực tế query từ Firestore
        
        if (doctors.isEmpty()) {
            callback.onResponse(new ChatResponse(
                "Xin lỗi, không có bác sĩ nào có lịch vào ngày " + date + "\n\n" +
                "Bạn có muốn chọn ngày khác không?",
                ChatResponse.ResponseType.TEXT
            ));
            conversationContext.setState(ConversationContext.ConversationState.WAITING_DATE);
        } else {
            // Hiển thị danh sách bác sĩ
            StringBuilder message = new StringBuilder();
            message.append("Các bác sĩ có lịch ngày ").append(date).append(":\n\n");
            
            for (int i = 0; i < doctors.size(); i++) {
                BacSi bs = doctors.get(i);
                message.append(i + 1).append(". 👨‍⚕️ ").append(bs.getHoTen()).append("\n");
                message.append("   Chuyên khoa: ").append(bs.getChuyenKhoa()).append("\n");
                message.append("   Kinh nghiệm: ").append(bs.getNamKinhNghiem()).append(" năm\n\n");
            }
            
            message.append("Bạn muốn đặt lịch với bác sĩ nào?");
            
            conversationContext.setData("doctors", doctors);
            conversationContext.setState(ConversationContext.ConversationState.WAITING_DOCTOR_SELECTION);
            
            callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
        }
    }
    
    private void handleDoctorSelection(String userMessage, ChatCallback callback) {
        // Parse doctor selection (TỰ BUILD)
        // VD: "bác sĩ 1", "bác sĩ A", "số 2"
        
        @SuppressWarnings("unchecked")
        List<BacSi> doctors = (List<BacSi>) conversationContext.getData("doctors");
        
        BacSi selectedDoctor = findDoctorFromInput(userMessage, doctors);
        
        if (selectedDoctor == null) {
            callback.onResponse(new ChatResponse(
                "Xin lỗi, tôi không tìm thấy bác sĩ bạn chọn.\n" +
                "Vui lòng nhập lại (VD: Bác sĩ 1, Bác sĩ Nguyễn Văn A)",
                ChatResponse.ResponseType.TEXT
            ));

            return;
        }
        
        // Lưu doctor vào context
        conversationContext.setData("doctor", selectedDoctor);
        
        // Hiển thị xác nhận
        showBookingConfirmation(callback);
    }
    
    private void showBookingConfirmation(ChatCallback callback) {
        String date = conversationContext.getString("date");
        BacSi doctor = (BacSi) conversationContext.getData("doctor");
        
        String message = "📋 Xác nhận thông tin đặt lịch:\n\n" +
                        "👤 Bệnh nhân: [Tên bạn]\n" +
                        "👨‍⚕️ Bác sĩ: " + doctor.getHoTen() + "\n" +
                        "📅 Ngày: " + date + "\n" +
                        "⏰ Giờ: 8:00\n" +
                        "💊 Loại: Khám tổng quát\n" +
                        "💰 Chi phí: 200.000đ\n\n" +
                        "Xác nhận đặt lịch?";
        
        conversationContext.setState(ConversationContext.ConversationState.WAITING_CONFIRMATION);
        
        ChatResponse response = new ChatResponse(message, ChatResponse.ResponseType.CONFIRMATION);
        callback.onResponse(response);
    }
    
    private void handleConfirmation(String userMessage, ChatCallback callback) {
        IntentDetector.Intent intent = intentDetector.detect(userMessage);
        
        if (intent == IntentDetector.Intent.XAC_NHAN) {
            // Tạo lịch khám trong Firestore (TỰ BUILD)
            createBooking(callback);
        } else {
            // Hủy
            conversationContext.reset();
            callback.onResponse(new ChatResponse(
                "Đã hủy đặt lịch. Bạn cần giúp gì khác không?",
                ChatResponse.ResponseType.TEXT
            ));
        }
    }
    
    private void createBooking(ChatCallback callback) {
        // TODO: Tạo LichKham trong Firestore
        
        conversationContext.reset();
        
        callback.onResponse(new ChatResponse(
            "✅ Đặt lịch thành công!\n\n" +
            "Mã lịch khám: LK" + System.currentTimeMillis() + "\n\n" +
            "Vui lòng đến trước 15 phút.\n" +
            "Bạn có thể xem lịch trong mục 'Lịch khám của tôi'.",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    // ============================================
    // OTHER INTENTS - TỰ BUILD
    // ============================================
    
    private void handleViewAppointments(ChatCallback callback) {
        // Query lịch hẹn từ Firestore (TỰ BUILD)
        repo.getByField("LichKham", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Bạn chưa có lịch hẹn nào.",
                        ChatResponse.ResponseType.TEXT
                    ));
                } else {
                    StringBuilder message = new StringBuilder("📅 Lịch hẹn của bạn:\n\n");
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        LichKham lk = doc.toObject(LichKham.class);
                        if (lk != null) {
                            message.append("📌 ").append(sdf.format(lk.getNgayKham().toDate())).append("\n");
                            message.append("   Trạng thái: ").append(lk.getTrangThai()).append("\n\n");
                        }
                    }
                    
                    callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
                }
            },
            e -> callback.onError(e.getMessage())
        );
    }
    
    private void handleMedicationGuide(ChatCallback callback) {
        // Query đơn thuốc từ Firestore (TỰ BUILD)
        callback.onResponse(new ChatResponse(
            "💊 Hướng dẫn uống thuốc:\n\n" +
            "Tôi đang kiểm tra đơn thuốc của bạn...",
            ChatResponse.ResponseType.TEXT
        ));
        
        // TODO: Query DonThuoc từ Firestore
    }
    
    private void handleDoctorQuery(String userMessage, ChatCallback callback) {
        // Extract chuyên khoa từ message (TỰ BUILD)
        String chuyenKhoa = extractChuyenKhoa(userMessage);
        
        if (chuyenKhoa != null) {
            // Query bác sĩ theo chuyên khoa
            queryDoctorsBySpecialty(chuyenKhoa, callback);
        } else {
            // Không rõ chuyên khoa → Dùng Gemini gợi ý
            handleWithGemini(userMessage, callback);
        }
    }
    
    private void handleClinicInfo(ChatCallback callback) {
        // Hardcoded info (TỰ BUILD)
        callback.onResponse(new ChatResponse(
            "🏥 Thông tin phòng khám:\n\n" +
            "📍 Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM\n" +
            "📞 Hotline: 1900-xxxx\n" +
            "⏰ Giờ làm việc:\n" +
            "   - Thứ 2-6: 8:00 - 17:00\n" +
            "   - Thứ 7: 8:00 - 12:00\n" +
            "   - Chủ nhật: Nghỉ",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    private void handleGreeting(ChatCallback callback) {
        callback.onResponse(new ChatResponse(
            "Xin chào! 👋\n\n" +
            "Tôi là trợ lý ảo của phòng khám. Tôi có thể giúp bạn:\n\n" +
            "📅 Đặt lịch khám\n" +
            "💊 Xem đơn thuốc\n" +
            "🏥 Xem bệnh án\n" +
            "👨‍⚕️ Tìm bác sĩ\n\n" +
            "Bạn cần giúp gì?",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    private void handleThanks(ChatCallback callback) {
        callback.onResponse(new ChatResponse(
            "Rất vui được giúp bạn! 😊\n" +
            "Nếu cần gì thêm, cứ hỏi tôi nhé!",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    // ============================================
    // GEMINI AI - TƯ VẤN & KIẾN THỨC Y TẾ
    // ============================================
    
    /**
     * Dùng Gemini cho:
     * - Tư vấn triệu chứng: "Tôi bị đau đầu nên làm gì?"
     * - Hướng dẫn chuẩn bị: "Trước khi khám nên chuẩn bị gì?"
     * - Kiến thức y tế: "Thuốc X có tác dụng gì?"
     * - Lời khuyên sức khỏe: "Ăn gì tốt cho tim?"
     */
    private void handleWithGemini(String userMessage, ChatCallback callback) {
        
        // Build context từ user data (nếu cần)
        String userContext = buildUserContext();
        
        geminiAssistant.ask(userMessage, userContext, new GeminiAssistant.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                // Thêm disclaimer nếu là tư vấn y tế
                if (isMedicalAdvice(userMessage)) {
                    response += "\n\n⚠️ Lưu ý: Đây chỉ là tư vấn sơ bộ. " +
                               "Vui lòng gặp bác sĩ để được chẩn đoán chính xác.";
                }
                
                callback.onResponse(new ChatResponse(response, ChatResponse.ResponseType.TEXT));
            }
            
            @Override
            public void onError(String error) {
                // Gemini fail → Fallback
                callback.onResponse(new ChatResponse(
                    "Xin lỗi, tôi không thể trả lời câu hỏi này lúc này.\n\n" +
                    "Bạn có thể:\n" +
                    "- Đặt lịch khám để được bác sĩ tư vấn trực tiếp\n" +
                    "- Hỏi về các tính năng khác của hệ thống\n\n" +
                    "[Đặt lịch khám]",
                    ChatResponse.ResponseType.TEXT
                ));
            }
        });
    }
    
    /**
     * Build context về user để Gemini trả lời chính xác hơn
     */
    private String buildUserContext() {
        StringBuilder context = new StringBuilder();
        context.append("Thông tin bệnh nhân:\n");
        context.append("- Mã BN: ").append(maBenhNhan).append("\n");
        
        // TODO: Thêm context nếu cần:
        // - Có đơn thuốc đang dùng
        // - Có bệnh án gần đây
        // - Có lịch hẹn sắp tới
        
        return context.toString();
    }
    
    /**
     * Check xem có phải câu hỏi tư vấn y tế không
     */
    private boolean isMedicalAdvice(String message) {
        String lower = message.toLowerCase();
        return lower.contains("bị") || 
               lower.contains("đau") || 
               lower.contains("sốt") ||
               lower.contains("nên làm gì") ||
               lower.contains("triệu chứng");
    }
    
    // ============================================
    // MORE DATA HANDLERS - TỰ BUILD
    // ============================================
    
    private void handleViewMedicalRecords(ChatCallback callback) {
        // TODO: Query BenhAn từ Firestore
        callback.onResponse(new ChatResponse(
            "📋 Đang tải bệnh án của bạn...",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    private void handleViewInvoices(ChatCallback callback) {
        // TODO: Query HoaDon từ Firestore
        callback.onResponse(new ChatResponse(
            "💰 Đang tải hóa đơn của bạn...",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    private void handleCancelAppointment(String userMessage, ChatCallback callback) {
        // TODO: Parse lịch cần hủy và update Firestore
        callback.onResponse(new ChatResponse(
            "Bạn muốn hủy lịch hẹn nào?\n" +
            "Vui lòng cho tôi biết ngày hoặc mã lịch khám.",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    // ============================================
    // HELPER METHODS - TỰ BUILD
    // ============================================
    
    private String parseDateFromText(String text) {
        // Simple date parsing (TỰ BUILD)
        if (text.contains("hôm nay") || text.contains("hom nay")) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return sdf.format(new java.util.Date());
        }
        // TODO: Parse "ngày mai", "thứ 3", "15/12"...
        return null;
    }
    
    private BacSi findDoctorFromInput(String input, List<BacSi> doctors) {
        // Parse doctor selection (TỰ BUILD)
        // TODO: Match "bác sĩ 1", "số 2", "bác sĩ A"
        return null;
    }
    
    private String extractChuyenKhoa(String message) {
        // Extract specialty from message (TỰ BUILD)
        if (message.contains("tim")) return "Tim mạch";
        if (message.contains("nội")) return "Nội khoa";
        // TODO: More specialties
        return null;
    }
    
    private void queryDoctorsBySpecialty(String chuyenKhoa, ChatCallback callback) {
        // Query Firestore (TỰ BUILD)
        repo.getByField("BacSi", "chuyenKhoa", chuyenKhoa,
            querySnapshot -> {
                // Build response
            },
            e -> callback.onError(e.getMessage())
        );
    }
    
    // ============================================
    // DOCTOR-SPECIFIC HANDLERS
    // ============================================
    
    private void handlePatientStatistics(ChatCallback callback) {
        if (maBacSi == null) {
            callback.onResponse(new ChatResponse(
                "Chức năng này chỉ dành cho bác sĩ.",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        // Query thống kê từ Firestore
        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                int totalAppointments = querySnapshot.size();
                int todayAppointments = 0;
                int pendingAppointments = 0;
                
                java.util.Calendar today = java.util.Calendar.getInstance();
                today.set(java.util.Calendar.HOUR_OF_DAY, 0);
                today.set(java.util.Calendar.MINUTE, 0);
                today.set(java.util.Calendar.SECOND, 0);
                
                for (var doc : querySnapshot.getDocuments()) {
                    LichKham lk = doc.toObject(LichKham.class);
                    if (lk != null) {
                        if ("CHO".equals(lk.getTrangThai())) {
                            pendingAppointments++;
                        }
                        
                        if (lk.getNgayKham() != null) {
                            java.util.Calendar appointmentDate = java.util.Calendar.getInstance();
                            appointmentDate.setTime(lk.getNgayKham().toDate());
                            appointmentDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
                            appointmentDate.set(java.util.Calendar.MINUTE, 0);
                            appointmentDate.set(java.util.Calendar.SECOND, 0);
                            
                            if (appointmentDate.equals(today)) {
                                todayAppointments++;
                            }
                        }
                    }
                }
                
                String message = "📊 Thống kê bệnh nhân:\n\n" +
                               "📅 Hôm nay: " + todayAppointments + " lịch khám\n" +
                               "⏳ Chờ xác nhận: " + pendingAppointments + " lịch\n" +
                               "📋 Tổng số lịch: " + totalAppointments + "\n\n" +
                               "Bạn cần xem chi tiết gì không?";
                
                callback.onResponse(new ChatResponse(message, ChatResponse.ResponseType.TEXT));
            },
            e -> callback.onError(e.getMessage())
        );
    }
    
    private void handleDoctorSchedule(ChatCallback callback) {
        if (maBacSi == null) {
            callback.onResponse(new ChatResponse(
                "Chức năng này chỉ dành cho bác sĩ.",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        // Query lịch làm việc từ Firestore
        repo.getByField("LichLamViec", "maBacSi", maBacSi,
            querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Bạn chưa có lịch làm việc nào được đăng ký.",
                        ChatResponse.ResponseType.TEXT
                    ));
                } else {
                    StringBuilder message = new StringBuilder("📅 Lịch làm việc của bạn:\n\n");
                    
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        String caLamViec = doc.getString("caLamViec");
                        com.google.firebase.Timestamp ngayLamViec = doc.getTimestamp("ngayLamViec");
                        
                        if (ngayLamViec != null) {
                            message.append("📌 ").append(sdf.format(ngayLamViec.toDate()));
                            message.append(" - ").append(caLamViec).append("\n");
                        }
                    }
                    
                    callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
                }
            },
            e -> callback.onError(e.getMessage())
        );
    }
    
    private void handlePatientLookup(String userMessage, ChatCallback callback) {
        if (maBacSi == null) {
            callback.onResponse(new ChatResponse(
                "Chức năng này chỉ dành cho bác sĩ.",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        callback.onResponse(new ChatResponse(
            "🔍 Tra cứu bệnh nhân\n\n" +
            "Vui lòng cung cấp:\n" +
            "- Tên bệnh nhân\n" +
            "- Mã bệnh nhân\n" +
            "- Số điện thoại\n\n" +
            "Để tôi tìm kiếm thông tin.",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    private void handleMedicationLookup(String userMessage, ChatCallback callback) {
        // Tra cứu thuốc - có thể dùng Gemini để tra cứu thông tin thuốc
        String prompt = "Tra cứu thông tin về thuốc: " + userMessage + 
                       "\n\nVui lòng cung cấp:\n" +
                       "- Thành phần\n" +
                       "- Công dụng\n" +
                       "- Liều dùng\n" +
                       "- Tương tác thuốc\n" +
                       "- Chống chỉ định";
        
        handleWithGemini(prompt, callback);
    }
    
    private void handleCreateReport(ChatCallback callback) {
        if (maBacSi == null) {
            callback.onResponse(new ChatResponse(
                "Chức năng này chỉ dành cho bác sĩ.",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        callback.onResponse(new ChatResponse(
            "📋 Tạo báo cáo\n\n" +
            "Bạn muốn tạo báo cáo gì?\n\n" +
            "1. Báo cáo bệnh nhân theo ngày\n" +
            "2. Báo cáo doanh thu\n" +
            "3. Báo cáo thuốc kê đơn\n" +
            "4. Báo cáo tổng hợp\n\n" +
            "Vui lòng chọn loại báo cáo.",
            ChatResponse.ResponseType.TEXT
        ));
    }
    
    private void handleDiagnosisSuggestion(String userMessage, ChatCallback callback) {
        // Gợi ý chẩn đoán - dùng Gemini với context y tế
        String prompt = "Với vai trò là trợ lý y tế, hãy gợi ý các chẩn đoán có thể dựa trên triệu chứng sau:\n\n" +
                       userMessage + 
                       "\n\nVui lòng liệt kê:\n" +
                       "1. Các chẩn đoán có thể\n" +
                       "2. Xét nghiệm cần làm\n" +
                       "3. Điều trị ban đầu\n\n" +
                       "Lưu ý: Đây chỉ là gợi ý, cần thăm khám trực tiếp để chẩn đoán chính xác.";
        
        handleWithGemini(prompt, callback);
    }
}
