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
    private GeminiAssistant geminiAssistant;
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
        this.conversationContext.setData("userType", userType);
        this.intentDetector = new IntentDetector();
        this.geminiAssistant = new GeminiAssistant(context);
    }
    
    /**
     * MAIN PROCESSING - TỰ BUILD LOGIC với NLU cải tiến
     */
    public void processMessage(String userMessage, ChatCallback callback) {
        
        // Step 1: Kiểm tra câu hỏi mơ hồ trước
        if (intentDetector.isAmbiguous(userMessage)) {
            handleAmbiguousMessage(userMessage, callback);
            return;
        }
        
        // Step 2: Detect intent (RULE-BASED với NLU)
        IntentDetector.Intent intent = intentDetector.detect(userMessage);
        
        // Step 3: Check conversation state
        ConversationContext.ConversationState state = conversationContext.getState();
        
        // Step 4: Route to appropriate handler (TỰ BUILD)
        if (state != ConversationContext.ConversationState.IDLE) {
            // Đang trong conversation flow
            handleConversationFlow(userMessage, state, callback);
        } else {
            // New intent
            handleNewIntent(intent, userMessage, callback);
        }
    }
    
    /**
     * Xử lý câu hỏi mơ hồ bằng cách gợi ý
     */
    private void handleAmbiguousMessage(String userMessage, ChatCallback callback) {
        List<String> suggestions = intentDetector.getSuggestions(userMessage);
        
        String responseMessage = "Tôi hiểu bạn đang hỏi về \"" + userMessage + "\" 🤔\n\n" +
                               "Bạn có thể muốn:\n\n";
        
        for (int i = 0; i < suggestions.size() && i < 4; i++) {
            responseMessage += "• " + suggestions.get(i) + "\n";
        }
        
        responseMessage += "\nVui lòng nói rõ hơn để tôi có thể giúp bạn tốt nhất! 😊";
        
        ChatResponse response = new ChatResponse(responseMessage, ChatResponse.ResponseType.QUICK_REPLY);
        response.setQuickReplies(suggestions.subList(0, Math.min(suggestions.size(), 4)));
        
        callback.onResponse(response);
    }
    
    /**
     * XỬ LÝ CONVERSATION FLOW - TỰ BUILD
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
                
            case WAITING_TIME_SELECTION:
                handleTimeSelection(userMessage, callback);
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
     */
    private void handleNewIntent(IntentDetector.Intent intent, 
                                 String userMessage, 
                                 ChatCallback callback) {
        
        switch (intent) {
            case DAT_LICH_KHAM:
                handleBookingIntent(callback);
                break;
                
            case XEM_LICH_HEN:
                handleViewAppointments(callback);
                break;
                
            case XEM_LICH_BAC_SI:
                handleViewDoctorSchedule(userMessage, callback);
                break;
                
            case HUONG_DAN_UONG_THUOC:
                handleMedicationGuide(callback);
                break;
                
            case TRA_CUU_BAC_SI:
                handleDoctorQuery(userMessage, callback);
                break;
                
            case CHAO_HOI:
                handleGreeting(callback);
                break;
                
            case CAM_ON:
                handleThanks(callback);
                break;
                
            case KHAC:
                handleUnknownIntent(userMessage, callback);
                break;
                
            default:
                handleUnknownIntent(userMessage, callback);
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
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Hôm nay");
        quickReplies.add("Ngày mai");
        quickReplies.add("Thứ 2 tuần sau");
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    // ============================================
    // XEM LỊCH BÁC SĨ THEO NGÀY
    // ============================================
    
    private void handleViewDoctorSchedule(String userMessage, ChatCallback callback) {
        // Parse ngày từ message
        java.util.Date targetDate = parseDateFromText(userMessage);
        
        // Nếu không có ngày cụ thể, mặc định là hôm nay
        if (targetDate == null) {
            targetDate = new java.util.Date();
        }
        
        final java.util.Date queryDate = targetDate;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Query lịch làm việc của bác sĩ theo ngày
        // Tạo timestamp cho đầu ngày
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(queryDate);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        
        repo.getCollection("LichLamViec")
            .whereEqualTo("ngayLamViec", new com.google.firebase.Timestamp(cal.getTime()))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "📅 Không có bác sĩ nào có lịch làm việc vào ngày " + 
                        dateFormat.format(queryDate) + "\n\n" +
                        "Bạn có muốn xem ngày khác không?",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("Ngày mai");
                    quickReplies.add("Thứ 2 tuần sau");
                    quickReplies.add("Đặt lịch khám");
                    response.setQuickReplies(quickReplies);
                    
                    callback.onResponse(response);
                    return;
                }
                
                // Lấy danh sách mã bác sĩ và ca làm việc
                List<String> doctorIds = new ArrayList<>();
                Map<String, String> doctorSchedules = new HashMap<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    String maBacSi = doc.getString("maBacSi");
                    String caLamViec = doc.getString("caLamViec");
                    if (maBacSi != null) {
                        doctorIds.add(maBacSi);
                        doctorSchedules.put(maBacSi, caLamViec);
                    }
                }
                
                // Query thông tin chi tiết bác sĩ
                repo.getCollection("BacSi")
                    .whereIn("maBacSi", doctorIds)
                    .get()
                    .addOnSuccessListener(doctorSnapshot -> {
                        List<BacSi> availableDoctors = new ArrayList<>();
                        
                        for (var doctorDoc : doctorSnapshot.getDocuments()) {
                            BacSi doctor = doctorDoc.toObject(BacSi.class);
                            if (doctor != null) {
                                String schedule = doctorSchedules.get(doctor.getMaBacSi());
                                doctor.setCaLamViec(schedule);
                                availableDoctors.add(doctor);
                            }
                        }
                        
                        if (availableDoctors.isEmpty()) {
                            callback.onResponse(new ChatResponse(
                                "Không tìm thấy thông tin bác sĩ.",
                                ChatResponse.ResponseType.TEXT
                            ));
                            return;
                        }
                        
                        // Tạo response với doctor cards
                        String message = "👨‍⚕️ Danh sách bác sĩ có lịch ngày " + 
                                        dateFormat.format(queryDate) + ":\n\n" +
                                        "Vuốt sang trái để xem thêm bác sĩ. Nhấn \"Đặt lịch khám\" để đặt lịch.";
                        
                        ChatResponse response = new ChatResponse(message, ChatResponse.ResponseType.DOCTOR_CARDS);
                        response.setDoctorCards(availableDoctors);
                        
                        callback.onResponse(response);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Lỗi khi tải thông tin bác sĩ: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tìm lịch làm việc: " + e.getMessage());
            });
    }
    
    private void handleDateInput(String userMessage, ChatCallback callback) {
        java.util.Date selectedDate = parseDateFromText(userMessage);
        
        if (selectedDate == null) {
            callback.onResponse(new ChatResponse(
                "Xin lỗi, tôi không hiểu ngày bạn muốn khám.\n" +
                "Vui lòng nhập lại (VD: 15/12, ngày mai, thứ 2 tuần sau)",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        // Kiểm tra ngày hợp lệ
        java.util.Calendar today = java.util.Calendar.getInstance();
        today.set(java.util.Calendar.HOUR_OF_DAY, 0);
        today.set(java.util.Calendar.MINUTE, 0);
        today.set(java.util.Calendar.SECOND, 0);
        today.set(java.util.Calendar.MILLISECOND, 0);
        
        if (selectedDate.before(today.getTime())) {
            callback.onResponse(new ChatResponse(
                "Không thể đặt lịch cho ngày trong quá khứ.\n" +
                "Vui lòng chọn ngày từ hôm nay trở đi.",
                ChatResponse.ResponseType.TEXT
            ));
            return;
        }
        
        conversationContext.setData("selectedDate", selectedDate);
        queryAvailableDoctors(selectedDate, callback);
    }
    
    private void queryAvailableDoctors(java.util.Date selectedDate, ChatCallback callback) {
        // Query bác sĩ có lịch làm việc ngày này
        repo.getCollection("LichLamViec")
            .whereEqualTo("ngayLamViec", new com.google.firebase.Timestamp(selectedDate))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Xin lỗi, không có bác sĩ nào có lịch làm việc vào ngày " + 
                        new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate) + "\n\n" +
                        "Bạn có muốn chọn ngày khác không?",
                        ChatResponse.ResponseType.TEXT
                    ));
                    conversationContext.setState(ConversationContext.ConversationState.WAITING_DATE);
                    return;
                }
                
                List<String> doctorIds = new ArrayList<>();
                Map<String, String> doctorSchedules = new HashMap<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    String maBacSi = doc.getString("maBacSi");
                    String caLamViec = doc.getString("caLamViec");
                    if (maBacSi != null) {
                        doctorIds.add(maBacSi);
                        doctorSchedules.put(maBacSi, caLamViec);
                    }
                }
                
                if (doctorIds.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Không tìm thấy bác sĩ có lịch làm việc ngày này.\n" +
                        "Vui lòng chọn ngày khác.",
                        ChatResponse.ResponseType.TEXT
                    ));
                    conversationContext.setState(ConversationContext.ConversationState.WAITING_DATE);
                    return;
                }
                
                // Query thông tin chi tiết bác sĩ
                repo.getCollection("BacSi")
                    .whereIn("maBacSi", doctorIds)
                    .get()
                    .addOnSuccessListener(doctorSnapshot -> {
                        List<BacSi> availableDoctors = new ArrayList<>();
                        
                        for (var doctorDoc : doctorSnapshot.getDocuments()) {
                            BacSi doctor = doctorDoc.toObject(BacSi.class);
                            if (doctor != null) {
                                String schedule = doctorSchedules.get(doctor.getMaBacSi());
                                doctor.setCaLamViec(schedule);
                                availableDoctors.add(doctor);
                            }
                        }
                        
                        if (availableDoctors.isEmpty()) {
                            callback.onResponse(new ChatResponse(
                                "Không tìm thấy thông tin bác sĩ.\n" +
                                "Vui lòng thử lại sau.",
                                ChatResponse.ResponseType.TEXT
                            ));
                            conversationContext.reset();
                            return;
                        }
                        
                        StringBuilder message = new StringBuilder();
                        message.append("🏥 Các bác sĩ có lịch ngày ")
                               .append(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate))
                               .append(":\n\n");
                        
                        for (int i = 0; i < availableDoctors.size(); i++) {
                            BacSi doctor = availableDoctors.get(i);
                            message.append(i + 1).append(". 👨‍⚕️ **").append(doctor.getHoTen()).append("**\n");
                            message.append("   📋 Chuyên khoa: ").append(doctor.getChuyenKhoa()).append("\n");
                            message.append("   ⭐ Kinh nghiệm: ").append(doctor.getNamKinhNghiem()).append(" năm\n");
                            message.append("   ⏰ Ca làm việc: ").append(doctor.getCaLamViec()).append("\n\n");
                        }
                        
                        message.append("Vui lòng chọn bác sĩ (VD: Bác sĩ 1, BS 2):");
                        
                        conversationContext.setData("availableDoctors", availableDoctors);
                        conversationContext.setState(ConversationContext.ConversationState.WAITING_DOCTOR_SELECTION);
                        
                        callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
                    })
                    .addOnFailureListener(e -> {
                        callback.onError("Lỗi khi tải thông tin bác sĩ: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tìm lịch làm việc: " + e.getMessage());
            });
    }
    
    private void handleDoctorSelection(String userMessage, ChatCallback callback) {
        @SuppressWarnings("unchecked")
        List<BacSi> availableDoctors = (List<BacSi>) conversationContext.getData("availableDoctors");
        
        if (availableDoctors == null || availableDoctors.isEmpty()) {
            callback.onError("Không tìm thấy danh sách bác sĩ");
            return;
        }
        
        BacSi selectedDoctor = findDoctorFromInput(userMessage, availableDoctors);
        
        if (selectedDoctor == null) {
            StringBuilder doctorList = new StringBuilder("Vui lòng chọn một trong các bác sĩ sau:\n\n");
            for (int i = 0; i < availableDoctors.size(); i++) {
                doctorList.append(i + 1).append(". ").append(availableDoctors.get(i).getHoTen()).append("\n");
            }
            doctorList.append("\nVD: Bác sĩ 1, BS 2, hoặc tên bác sĩ");
            
            callback.onResponse(new ChatResponse(doctorList.toString(), ChatResponse.ResponseType.TEXT));
            return;
        }
        
        conversationContext.setData("selectedDoctor", selectedDoctor);
        queryAvailableTimeSlots(selectedDoctor, callback);
    }
    
    private void queryAvailableTimeSlots(BacSi selectedDoctor, ChatCallback callback) {
        java.util.Date selectedDate = (java.util.Date) conversationContext.getData("selectedDate");
        
        repo.getCollection("LichKham")
            .whereEqualTo("maBacSi", selectedDoctor.getMaBacSi())
            .whereEqualTo("ngayKham", new com.google.firebase.Timestamp(selectedDate))
            .whereIn("trangThai", java.util.Arrays.asList("CHO", "XAC_NHAN"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<String> bookedTimes = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    String gioKham = doc.getString("gioKham");
                    if (gioKham != null) {
                        bookedTimes.add(gioKham);
                    }
                }
                
                List<String> availableTimes = generateAvailableTimeSlots(selectedDoctor.getCaLamViec(), bookedTimes);
                
                if (availableTimes.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Xin lỗi, bác sĩ " + selectedDoctor.getHoTen() + 
                        " đã hết lịch trong ngày này.\n\n" +
                        "Bạn có muốn:\n" +
                        "1. Chọn bác sĩ khác\n" +
                        "2. Chọn ngày khác",
                        ChatResponse.ResponseType.TEXT
                    ));
                    conversationContext.setState(ConversationContext.ConversationState.WAITING_DOCTOR_SELECTION);
                    return;
                }
                
                StringBuilder message = new StringBuilder();
                message.append("⏰ Khung giờ có sẵn của bác sĩ ")
                       .append(selectedDoctor.getHoTen())
                       .append(":\n\n");
                
                for (int i = 0; i < availableTimes.size(); i++) {
                    message.append(i + 1).append(". ").append(availableTimes.get(i)).append("\n");
                }
                
                message.append("\nVui lòng chọn giờ khám (VD: 1, 8:00, khung 2):");
                
                conversationContext.setData("availableTimes", availableTimes);
                conversationContext.setState(ConversationContext.ConversationState.WAITING_TIME_SELECTION);
                
                callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi kiểm tra lịch khám: " + e.getMessage());
            });
    }
    
    private void handleTimeSelection(String userMessage, ChatCallback callback) {
        @SuppressWarnings("unchecked")
        List<String> availableTimes = (List<String>) conversationContext.getData("availableTimes");
        
        if (availableTimes == null || availableTimes.isEmpty()) {
            callback.onError("Không tìm thấy danh sách khung giờ");
            return;
        }
        
        String selectedTime = findTimeFromInput(userMessage, availableTimes);
        
        if (selectedTime == null) {
            StringBuilder timeList = new StringBuilder("Vui lòng chọn một trong các khung giờ sau:\n\n");
            for (int i = 0; i < availableTimes.size(); i++) {
                timeList.append(i + 1).append(". ").append(availableTimes.get(i)).append("\n");
            }
            timeList.append("\nVD: 1, 8:00, khung 2");
            
            callback.onResponse(new ChatResponse(timeList.toString(), ChatResponse.ResponseType.TEXT));
            return;
        }
        
        conversationContext.setData("selectedTime", selectedTime);
        showFinalBookingConfirmation(callback);
    }
    
    private void showFinalBookingConfirmation(ChatCallback callback) {
        java.util.Date selectedDate = (java.util.Date) conversationContext.getData("selectedDate");
        BacSi selectedDoctor = (BacSi) conversationContext.getData("selectedDoctor");
        String selectedTime = (String) conversationContext.getData("selectedTime");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        String message = "📋 **XÁC NHẬN THÔNG TIN ĐẶT LỊCH**\n\n" +
                        "👤 **Bệnh nhân:** " + getUserName() + "\n" +
                        "👨‍⚕️ **Bác sĩ:** " + selectedDoctor.getHoTen() + "\n" +
                        "🏥 **Chuyên khoa:** " + selectedDoctor.getChuyenKhoa() + "\n" +
                        "📅 **Ngày khám:** " + dateFormat.format(selectedDate) + "\n" +
                        "⏰ **Giờ khám:** " + selectedTime + "\n" +
                        "💊 **Loại khám:** Khám tổng quát\n" +
                        "💰 **Chi phí:** 200.000đ\n\n" +
                        "✅ **Xác nhận đặt lịch?**\n" +
                        "Trả lời: 'Có' hoặc 'Xác nhận' để đặt lịch\n" +
                        "Trả lời: 'Không' hoặc 'Hủy' để hủy bỏ";
        
        conversationContext.setState(ConversationContext.ConversationState.WAITING_CONFIRMATION);
        
        ChatResponse response = new ChatResponse(message, ChatResponse.ResponseType.CONFIRMATION);
        callback.onResponse(response);
    }
    
    private void handleConfirmation(String userMessage, ChatCallback callback) {
        IntentDetector.Intent intent = intentDetector.detect(userMessage);
        
        if (intent == IntentDetector.Intent.XAC_NHAN) {
            createBooking(callback);
        } else {
            conversationContext.reset();
            callback.onResponse(new ChatResponse(
                "Đã hủy đặt lịch. Bạn cần giúp gì khác không?",
                ChatResponse.ResponseType.TEXT
            ));
        }
    }
    
    private void createBooking(ChatCallback callback) {
        java.util.Date selectedDate = (java.util.Date) conversationContext.getData("selectedDate");
        BacSi selectedDoctor = (BacSi) conversationContext.getData("selectedDoctor");
        String selectedTime = (String) conversationContext.getData("selectedTime");
        
        String maLichKham = "LK" + System.currentTimeMillis();
        
        LichKham lichKham = new LichKham();
        lichKham.setMaLichKham(maLichKham);
        lichKham.setMaBenhNhan(maBenhNhan);
        lichKham.setMaBacSi(selectedDoctor.getMaBacSi());
        lichKham.setNgayKham(new com.google.firebase.Timestamp(selectedDate));
        lichKham.setGioKham(selectedTime);
        lichKham.setTrangThai("CHO");
        lichKham.setLoaiKham("Khám tổng quát");
        lichKham.setChiPhi(200000.0);
        lichKham.setNgayTao(com.google.firebase.Timestamp.now());
        lichKham.setGhiChu("Đặt lịch qua trợ lý AI");
        
        repo.addDocument("LichKham", maLichKham, lichKham,
            aVoid -> {
                conversationContext.reset();
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                
                String successMessage = "🎉 **ĐẶT LỊCH THÀNH CÔNG!**\n\n" +
                                      "📋 **Mã lịch khám:** " + maLichKham + "\n" +
                                      "👨‍⚕️ **Bác sĩ:** " + selectedDoctor.getHoTen() + "\n" +
                                      "📅 **Ngày:** " + dateFormat.format(selectedDate) + "\n" +
                                      "⏰ **Giờ:** " + selectedTime + "\n" +
                                      "💰 **Chi phí:** 200.000đ\n\n" +
                                      "📝 **Lưu ý quan trọng:**\n" +
                                      "• Vui lòng đến trước 15 phút\n" +
                                      "• Mang theo CMND/CCCD\n" +
                                      "• Chuẩn bị tiền khám\n\n" +
                                      "📱 Bạn có thể xem lịch trong mục **'Lịch khám của tôi'**\n\n" +
                                      "Cần hỗ trợ gì thêm không? 😊";
                
                callback.onResponse(new ChatResponse(successMessage, ChatResponse.ResponseType.TEXT));
            },
            e -> {
                conversationContext.reset();
                callback.onError("Lỗi khi đặt lịch: " + e.getMessage() + 
                               "\nVui lòng thử lại hoặc liên hệ trực tiếp phòng khám.");
            }
        );
    }
    
    // ============================================
    // OTHER INTENTS - TỰ BUILD
    // ============================================
    
    private void handleViewAppointments(ChatCallback callback) {
        repo.getCollection("LichKham")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .orderBy("ngayKham", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "📅 **LỊCH KHÁM CỦA BẠN**\n\n" +
                        "Bạn chưa có lịch hẹn nào.\n\n" +
                        "💡 Bạn có muốn đặt lịch khám mới không?\n" +
                        "Chỉ cần nói: *'Tôi muốn đặt lịch khám'*",
                        ChatResponse.ResponseType.TEXT
                    ));
                } else {
                    StringBuilder message = new StringBuilder("📅 **LỊCH KHÁM CỦA BẠN**\n\n");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    int count = 0;
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        LichKham lk = doc.toObject(LichKham.class);
                        if (lk != null) {
                            count++;
                            String trangThaiIcon = getTrangThaiIcon(lk.getTrangThai());
                            String trangThaiText = getTrangThaiText(lk.getTrangThai());
                            
                            message.append("**").append(count).append(". ").append(lk.getMaLichKham()).append("**\n");
                            message.append("📅 Ngày: ").append(dateFormat.format(lk.getNgayKham().toDate())).append("\n");
                            message.append("⏰ Giờ: ").append(lk.getGioKham()).append("\n");
                            message.append("👨‍⚕️ Bác sĩ: ").append(getBacSiName(lk.getMaBacSi())).append("\n");
                            message.append(trangThaiIcon).append(" Trạng thái: ").append(trangThaiText).append("\n");
                            message.append("💰 Chi phí: ").append(formatCurrency(lk.getChiPhi())).append("\n\n");
                        }
                    }
                    
                    message.append("💡 **Cần hỗ trợ gì thêm?**\n");
                    message.append("• Đặt lịch mới: *'Đặt lịch khám'*\n");
                    message.append("• Hủy lịch: *'Hủy lịch [mã lịch]'*\n");
                    message.append("• Xem chi tiết: *'Chi tiết lịch [mã lịch]'*");
                    
                    callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
                }
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tải lịch khám: " + e.getMessage());
            });
    }
    
    private void handleMedicationGuide(ChatCallback callback) {
        repo.getCollection("DonThuoc")
            .whereEqualTo("maBenhNhan", maBenhNhan)
            .whereIn("trangThai", java.util.Arrays.asList("DANG_DUNG", null))
            .orderBy("ngayKeDon", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "💊 **ĐƠN THUỐC CỦA BẠN**\n\n" +
                        "Bạn hiện không có đơn thuốc nào đang sử dụng.\n\n" +
                        "💡 Nếu bạn có thắc mắc về thuốc, hãy hỏi tôi:\n" +
                        "*'Thuốc [tên thuốc] có tác dụng gì?'*",
                        ChatResponse.ResponseType.TEXT
                    ));
                } else {
                    StringBuilder message = new StringBuilder("💊 **ĐƠN THUỐC CỦA BẠN**\n\n");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        com.example.doannt118.model.DonThuoc donThuoc = doc.toObject(com.example.doannt118.model.DonThuoc.class);
                        if (donThuoc != null) {
                            message.append("📋 **").append(donThuoc.getMaDonThuoc()).append("**\n");
                            message.append("📅 Ngày kê: ").append(dateFormat.format(donThuoc.getNgayKeDon().toDate())).append("\n");
                            message.append("👨‍⚕️ Bác sĩ: ").append(getBacSiName(donThuoc.getMaBacSi())).append("\n\n");
                            
                            queryMedicationDetails(donThuoc.getMaDonThuoc(), message, callback);
                            return;
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tải đơn thuốc: " + e.getMessage());
            });
    }
    
    private void queryMedicationDetails(String maDonThuoc, StringBuilder message, ChatCallback callback) {
        repo.getCollection("ChiTietDonThuoc")
            .whereEqualTo("maDonThuoc", maDonThuoc)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    message.append("**CHI TIẾT THUỐC:**\n\n");
                    
                    int count = 0;
                    for (var doc : querySnapshot.getDocuments()) {
                        com.example.doannt118.model.ChiTietDonThuoc chiTiet = doc.toObject(com.example.doannt118.model.ChiTietDonThuoc.class);
                        if (chiTiet != null) {
                            count++;
                            message.append(count).append(". **").append(chiTiet.getTenThuoc()).append("**\n");
                            message.append("   📊 Liều dùng: ").append(chiTiet.getLieuDungDayDu()).append("\n");
                            message.append("   🍽️ Cách dùng: ").append(chiTiet.getCachDung()).append("\n");
                            
                            StringBuilder schedule = new StringBuilder("   ⏰ Lịch uống: ");
                            if (chiTiet.isUongSang()) schedule.append("Sáng ");
                            if (chiTiet.isUongTrua()) schedule.append("Trưa ");
                            if (chiTiet.isUongChieu()) schedule.append("Chiều ");
                            if (chiTiet.isUongToi()) schedule.append("Tối ");
                            message.append(schedule.toString()).append("\n\n");
                        }
                    }
                }
                
                message.append("💡 **Lưu ý quan trọng:**\n");
                message.append("• Uống đúng giờ, đúng liều\n");
                message.append("• Không tự ý ngừng thuốc\n");
                message.append("• Báo bác sĩ nếu có tác dụng phụ\n\n");
                message.append("🔔 Bạn có thể xem lịch uống thuốc trong mục **'Quản lý uống thuốc'**");
                
                callback.onResponse(new ChatResponse(message.toString(), ChatResponse.ResponseType.TEXT));
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tải chi tiết thuốc: " + e.getMessage());
            });
    }
    
    private void handleDoctorQuery(String userMessage, ChatCallback callback) {
        String chuyenKhoa = extractChuyenKhoa(userMessage);
        
        if (chuyenKhoa != null) {
            queryDoctorsBySpecialty(chuyenKhoa, callback);
        } else {
            // Hiển thị tất cả bác sĩ
            queryAllDoctors(callback);
        }
    }
    
    private void queryAllDoctors(ChatCallback callback) {
        repo.getCollection("BacSi")
            .limit(10)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Không tìm thấy bác sĩ nào trong hệ thống.",
                        ChatResponse.ResponseType.TEXT
                    ));
                    return;
                }
                
                List<BacSi> doctors = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    BacSi doctor = doc.toObject(BacSi.class);
                    if (doctor != null) {
                        doctors.add(doctor);
                    }
                }
                
                String message = "👨‍⚕️ Danh sách bác sĩ của phòng khám:\n\n" +
                                "Vuốt sang trái để xem thêm. Nhấn \"Đặt lịch khám\" để đặt lịch với bác sĩ.";
                
                ChatResponse response = new ChatResponse(message, ChatResponse.ResponseType.DOCTOR_CARDS);
                response.setDoctorCards(doctors);
                callback.onResponse(response);
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tải danh sách bác sĩ: " + e.getMessage());
            });
    }
    
    private void handleGreeting(ChatCallback callback) {
        String welcomeMessage;
        
        if ("bacsi".equals(userType)) {
            welcomeMessage = "Xin chào Bác sĩ! 👨‍⚕️\n\n" +
                           "Tôi có thể hỗ trợ bạn:\n\n" +
                           "📊 Xem thống kê bệnh nhân\n" +
                           "📅 Quản lý lịch làm việc\n" +
                           "🔍 Tra cứu thông tin\n" +
                           "💊 Tra cứu thuốc\n\n" +
                           "Bạn cần hỗ trợ gì?";
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("Thống kê hôm nay");
            quickReplies.add("Lịch làm việc");
            quickReplies.add("Tra cứu thuốc");
            
            ChatResponse response = new ChatResponse(welcomeMessage, ChatResponse.ResponseType.QUICK_REPLY);
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
        } else {
            welcomeMessage = "Xin chào! 👋\n\n" +
                           "Tôi là trợ lý ảo của phòng khám. Tôi có thể giúp bạn nhiều việc!\n\n" +
                           "Hãy chọn một trong các tùy chọn bên dưới:";
            
            // Tạo action buttons cho bệnh nhân
            List<com.example.doannt118.model.ChatMessage.ActionButton> actions = new ArrayList<>();
            actions.add(new com.example.doannt118.model.ChatMessage.ActionButton(
                "📅 Đặt lịch khám", 
                com.example.doannt118.model.ChatMessage.ActionType.BOOK_APPOINTMENT).setPrimary(true));
            actions.add(new com.example.doannt118.model.ChatMessage.ActionButton(
                "🏥 Xem lịch khám của tôi", 
                com.example.doannt118.model.ChatMessage.ActionType.VIEW_APPOINTMENTS));
            actions.add(new com.example.doannt118.model.ChatMessage.ActionButton(
                "👨‍⚕️ Xem lịch bác sĩ hôm nay", 
                com.example.doannt118.model.ChatMessage.ActionType.VIEW_DOCTOR_SCHEDULE));
            actions.add(new com.example.doannt118.model.ChatMessage.ActionButton(
                "💊 Xem đơn thuốc", 
                com.example.doannt118.model.ChatMessage.ActionType.VIEW_PRESCRIPTIONS));
            actions.add(new com.example.doannt118.model.ChatMessage.ActionButton(
                "🔍 Tìm bác sĩ", 
                com.example.doannt118.model.ChatMessage.ActionType.FIND_DOCTOR));
            
            ChatResponse response = new ChatResponse(welcomeMessage, ChatResponse.ResponseType.ACTION_BUTTONS);
            response.setActionButtons(actions);
            callback.onResponse(response);
        }
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
     * Xử lý intent không rõ - thông minh hơn
     */
    private void handleUnknownIntent(String userMessage, ChatCallback callback) {
        // Kiểm tra xem có phải câu hỏi y tế không
        if (isMedicalAdvice(userMessage)) {
            handleWithGemini(userMessage, callback);
            return;
        }
        
        // Gợi ý dựa trên từ khóa trong câu
        List<String> suggestions = intentDetector.getSuggestions(userMessage);
        
        String responseMessage = "Xin lỗi, tôi không hiểu rõ ý bạn 😅\n\n";
        
        if (!suggestions.isEmpty()) {
            responseMessage += "Có phải bạn muốn:\n\n";
            for (int i = 0; i < suggestions.size() && i < 3; i++) {
                responseMessage += "• " + suggestions.get(i) + "\n";
            }
            responseMessage += "\n";
        }
        
        responseMessage += "Hoặc bạn có thể hỏi tôi về:\n" +
                          "📅 Đặt lịch khám\n" +
                          "💊 Xem đơn thuốc\n" +
                          "🏥 Xem lịch khám\n" +
                          "👨‍⚕️ Tìm bác sĩ\n\n" +
                          "Hãy nói rõ hơn để tôi giúp bạn nhé! 😊";
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Đặt lịch khám");
        quickReplies.add("Xem lịch khám");
        quickReplies.add("Xem đơn thuốc");
        quickReplies.add("Tìm bác sĩ");
        
        ChatResponse response = new ChatResponse(responseMessage, ChatResponse.ResponseType.QUICK_REPLY);
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    private void handleWithGemini(String userMessage, ChatCallback callback) {
        String userContext = buildUserContext();
        
        geminiAssistant.ask(userMessage, userContext, new GeminiAssistant.GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                if (isMedicalAdvice(userMessage)) {
                    response += "\n\n⚠️ Lưu ý: Đây chỉ là tư vấn sơ bộ. " +
                               "Vui lòng gặp bác sĩ để được chẩn đoán chính xác.";
                }
                
                callback.onResponse(new ChatResponse(response, ChatResponse.ResponseType.TEXT));
            }
            
            @Override
            public void onError(String error) {
                // Fallback khi Gemini lỗi
                handleUnknownIntent(userMessage, callback);
            }
        });
    }
    
    private String buildUserContext() {
        StringBuilder context = new StringBuilder();
        context.append("Thông tin bệnh nhân:\n");
        context.append("- Mã BN: ").append(maBenhNhan).append("\n");
        return context.toString();
    }
    
    private boolean isMedicalAdvice(String message) {
        String lower = message.toLowerCase();
        return lower.contains("bị") || 
               lower.contains("đau") || 
               lower.contains("sốt") ||
               lower.contains("nên làm gì") ||
               lower.contains("triệu chứng");
    }
    
    // ============================================
    // HELPER METHODS
    // ============================================
    
    private java.util.Date parseDateFromText(String text) {
        text = text.toLowerCase().trim();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        
        if (text.contains("hôm nay") || text.contains("hom nay") || text.equals("hôm nay")) {
            return cal.getTime();
        }
        
        if (text.contains("ngày mai") || text.contains("ngay mai") || text.equals("mai")) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }
        
        if (text.contains("ngày mốt") || text.contains("ngay mot") || text.equals("mốt")) {
            cal.add(java.util.Calendar.DAY_OF_MONTH, 2);
            return cal.getTime();
        }
        
        if (text.contains("thứ")) {
            return parseDayOfWeek(text, cal);
        }
        
        if (text.matches("\\d{1,2}/\\d{1,2}(/\\d{4})?")) {
            return parseDateFormat(text, cal);
        }
        
        return null;
    }
    
    private java.util.Date parseDayOfWeek(String text, java.util.Calendar cal) {
        int targetDay = -1;
        
        if (text.contains("thứ 2") || text.contains("thứ hai")) targetDay = java.util.Calendar.MONDAY;
        else if (text.contains("thứ 3") || text.contains("thứ ba")) targetDay = java.util.Calendar.TUESDAY;
        else if (text.contains("thứ 4") || text.contains("thứ tư")) targetDay = java.util.Calendar.WEDNESDAY;
        else if (text.contains("thứ 5") || text.contains("thứ năm")) targetDay = java.util.Calendar.THURSDAY;
        else if (text.contains("thứ 6") || text.contains("thứ sáu")) targetDay = java.util.Calendar.FRIDAY;
        else if (text.contains("thứ 7") || text.contains("thứ bảy")) targetDay = java.util.Calendar.SATURDAY;
        else if (text.contains("chủ nhật") || text.contains("cn")) targetDay = java.util.Calendar.SUNDAY;
        
        if (targetDay == -1) return null;
        
        int currentDay = cal.get(java.util.Calendar.DAY_OF_WEEK);
        int daysToAdd = targetDay - currentDay;
        
        if (daysToAdd <= 0) {
            daysToAdd += 7;
        }
        
        if (text.contains("tuần sau")) {
            daysToAdd += 7;
        }
        
        cal.add(java.util.Calendar.DAY_OF_MONTH, daysToAdd);
        return cal.getTime();
    }
    
    private java.util.Date parseDateFormat(String text, java.util.Calendar cal) {
        try {
            String[] parts = text.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = cal.get(java.util.Calendar.YEAR);
            
            if (parts.length == 3) {
                year = Integer.parseInt(parts[2]);
            }
            
            cal.set(year, month - 1, day);
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }
    
    private BacSi findDoctorFromInput(String input, List<BacSi> doctors) {
        input = input.toLowerCase().trim();
        
        for (int i = 1; i <= doctors.size(); i++) {
            if (input.equals(String.valueOf(i)) ||
                input.contains("bác sĩ " + i) ||
                input.contains("bs " + i) ||
                input.contains("số " + i) ||
                input.contains("bác si " + i)) {
                return doctors.get(i - 1);
            }
        }
        
        for (BacSi doctor : doctors) {
            String doctorName = doctor.getHoTen().toLowerCase();
            if (input.contains(doctorName) || doctorName.contains(input)) {
                return doctor;
            }
        }
        
        return null;
    }
    
    private String findTimeFromInput(String input, List<String> availableTimes) {
        input = input.toLowerCase().trim();
        
        for (int i = 1; i <= availableTimes.size(); i++) {
            if (input.equals(String.valueOf(i)) ||
                input.contains("khung " + i) ||
                input.contains("giờ " + i) ||
                input.contains("số " + i)) {
                return availableTimes.get(i - 1);
            }
        }
        
        for (String time : availableTimes) {
            if (input.contains(time.toLowerCase()) ||
                time.toLowerCase().contains(input)) {
                return time;
            }
        }
        
        return null;
    }
    
    private List<String> generateAvailableTimeSlots(String caLamViec, List<String> bookedTimes) {
        List<String> allSlots = new ArrayList<>();
        
        if ("SANG".equals(caLamViec)) {
            allSlots.add("08:00");
            allSlots.add("08:30");
            allSlots.add("09:00");
            allSlots.add("09:30");
            allSlots.add("10:00");
            allSlots.add("10:30");
            allSlots.add("11:00");
        } else if ("CHIEU".equals(caLamViec)) {
            allSlots.add("14:00");
            allSlots.add("14:30");
            allSlots.add("15:00");
            allSlots.add("15:30");
            allSlots.add("16:00");
            allSlots.add("16:30");
            allSlots.add("17:00");
        } else {
            allSlots.add("08:00");
            allSlots.add("08:30");
            allSlots.add("09:00");
            allSlots.add("09:30");
            allSlots.add("10:00");
            allSlots.add("10:30");
            allSlots.add("11:00");
            allSlots.add("14:00");
            allSlots.add("14:30");
            allSlots.add("15:00");
            allSlots.add("15:30");
            allSlots.add("16:00");
            allSlots.add("16:30");
            allSlots.add("17:00");
        }
        
        List<String> availableSlots = new ArrayList<>();
        for (String slot : allSlots) {
            if (!bookedTimes.contains(slot)) {
                availableSlots.add(slot);
            }
        }
        
        return availableSlots;
    }
    
    private String getUserName() {
        return "Bệnh nhân";
    }
    
    private String extractChuyenKhoa(String message) {
        message = message.toLowerCase();
        if (message.contains("tim") || message.contains("tim mạch")) return "Tim mạch";
        if (message.contains("nội") || message.contains("nội khoa")) return "Nội khoa";
        if (message.contains("ngoại") || message.contains("ngoại khoa")) return "Ngoại khoa";
        if (message.contains("da liễu") || message.contains("da")) return "Da liễu";
        if (message.contains("mắt") || message.contains("nhãn khoa")) return "Nhãn khoa";
        if (message.contains("tai mũi họng") || message.contains("tmh")) return "Tai mũi họng";
        if (message.contains("răng") || message.contains("nha khoa")) return "Nha khoa";
        if (message.contains("sản") || message.contains("phụ khoa")) return "Sản phụ khoa";
        if (message.contains("nhi") || message.contains("trẻ em")) return "Nhi khoa";
        if (message.contains("thần kinh")) return "Thần kinh";
        return null;
    }
    
    private void queryDoctorsBySpecialty(String chuyenKhoa, ChatCallback callback) {
        repo.getCollection("BacSi")
            .whereEqualTo("chuyenKhoa", chuyenKhoa)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onResponse(new ChatResponse(
                        "Không tìm thấy bác sĩ chuyên khoa " + chuyenKhoa + ".\n\n" +
                        "Bạn có muốn xem tất cả bác sĩ không?",
                        ChatResponse.ResponseType.QUICK_REPLY
                    ));
                    return;
                }
                
                List<BacSi> doctors = new ArrayList<>();
                for (var doc : querySnapshot.getDocuments()) {
                    BacSi doctor = doc.toObject(BacSi.class);
                    if (doctor != null) {
                        doctors.add(doctor);
                    }
                }
                
                String message = "👨‍⚕️ Bác sĩ chuyên khoa " + chuyenKhoa + ":\n\n" +
                                "Vuốt sang trái để xem thêm. Nhấn \"Đặt lịch khám\" để đặt lịch.";
                
                ChatResponse response = new ChatResponse(message, ChatResponse.ResponseType.DOCTOR_CARDS);
                response.setDoctorCards(doctors);
                callback.onResponse(response);
            })
            .addOnFailureListener(e -> {
                callback.onError("Lỗi khi tìm bác sĩ: " + e.getMessage());
            });
    }
    
    private String getTrangThaiIcon(String trangThai) {
        switch (trangThai) {
            case "CHO": return "⏳";
            case "XAC_NHAN": return "✅";
            case "HOAN_THANH": return "🎉";
            case "HUY": return "❌";
            default: return "📋";
        }
    }
    
    private String getTrangThaiText(String trangThai) {
        switch (trangThai) {
            case "CHO": return "Chờ xác nhận";
            case "XAC_NHAN": return "Đã xác nhận";
            case "HOAN_THANH": return "Hoàn thành";
            case "HUY": return "Đã hủy";
            default: return "Không xác định";
        }
    }
    
    private String getBacSiName(String maBacSi) {
        return "Đang tải...";
    }
    
    private String formatCurrency(Double amount) {
        if (amount == null) return "0đ";
        return String.format("%,.0fđ", amount);
    }
}