package com.example.doannt118.chatbot;

import android.content.Context;
import com.example.doannt118.model.BacSi;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.repository.FirestoreRepository;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.util.Log;

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
    
    public ChatbotEngine(Context context, String maBenhNhan) {
        this(context, maBenhNhan, "benhnhan");
    }
    
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
    public void processMessage(String userMessage, ChatCallback callback) {
        
        if (intentDetector.isAmbiguous(userMessage)) {
            handleAmbiguousMessage(userMessage, callback);
            return;
        }
        
        IntentDetector.Intent intent = intentDetector.detect(userMessage);
        
        ConversationContext.ConversationState state = conversationContext.getState();
        
        if (state != ConversationContext.ConversationState.IDLE) {
            handleConversationFlow(userMessage, state, callback);
        } else {
            handleNewIntent(intent, userMessage, callback);
        }
    }
    
    private void handleAmbiguousMessage(String userMessage, ChatCallback callback) {
        String currentUserType = (String) conversationContext.getData("userType");
        List<String> suggestions;
        
        // Lấy gợi ý dựa trên role
        if ("bacsi".equals(currentUserType)) {
            suggestions = new ArrayList<>();
            suggestions.add("👥 Xem bệnh nhân hôm nay");
            suggestions.add("📅 Xem lịch làm việc");
            suggestions.add("✅ Xác nhận lịch khám");
            suggestions.add("📊 Xem thống kê");
        } else if ("benhnhan".equals(currentUserType)) {
            suggestions = new ArrayList<>();
            suggestions.add("📅 Đặt lịch khám");
            suggestions.add("👀 Xem lịch khám");
            suggestions.add("💊 Xem đơn thuốc");
            suggestions.add("👨‍⚕️ Tìm bác sĩ");
        } else {
            // Chưa chọn role
            handleRoleSelection(callback);
            return;
        }
        
        String responseMessage = "🤔 Tôi chưa hiểu rõ câu hỏi \"" + userMessage + "\"\n\n" +
                               "Bạn có thể muốn:\n\n";
        
        for (int i = 0; i < suggestions.size(); i++) {
            responseMessage += "• " + suggestions.get(i) + "\n";
        }
        
        responseMessage += "\nVui lòng chọn hoặc nói rõ hơn để tôi hỗ trợ bạn! 😊";
        
        ChatResponse response = new ChatResponse(responseMessage, ChatResponse.ResponseType.QUICK_REPLY);
        response.setQuickReplies(suggestions);
        
        callback.onResponse(response);
    }
    
    /**
     * XỬ LÝ CONVERSATION FLOW
     */
    private void handleConversationFlow(String userMessage,
                                       ConversationContext.ConversationState state,
                                       ChatCallback callback) {

        switch (state) {
            case WAITING_ROLE_SELECTION:
                handleRoleSelectionInput(userMessage, callback);
                break;

            case WAITING_DATE:
                handleDateInput(userMessage, callback);
                break;

            case WAITING_SPECIALTY_SELECTION:
                handleSpecialtySelection(userMessage, callback);
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

            case WAITING_CANCEL_SELECTION:
                handleCancelSelection(userMessage, callback);
                break;

            case WAITING_SCHEDULE_ACTION:
                handleScheduleActionSelection(userMessage, callback);
                break;

            default:
                callback.onError("Unknown state: " + state);
        }
    }

    /**
     * XỬ LÝ INTENT MỚI - ROLE-BASED SYSTEM
     */
    private void handleNewIntent(IntentDetector.Intent intent,
                                 String userMessage,
                                 ChatCallback callback) {

        // Kiểm tra role trước khi xử lý intent
        String currentUserType = (String) conversationContext.getData("userType");
        if (currentUserType == null) {
            handleRoleSelection(callback);
            return;
        }

        switch (intent) {
            // ============================================
            // PATIENT INTENTS
            // ============================================
            case DAT_LICH_KHAM:
                if ("benhnhan".equals(currentUserType)) {
                    handleBookingIntent(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân mới có thể đặt lịch khám", callback);
                }
                break;

            case XEM_LICH_KHAM:
                if ("benhnhan".equals(currentUserType)) {
                    handleViewPatientAppointments(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân mới có thể xem lịch khám cá nhân", callback);
                }
                break;

            case HUY_LICH_KHAM:
                if ("benhnhan".equals(currentUserType)) {
                    handleCancelAppointment(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân mới có thể hủy lịch khám", callback);
                }
                break;

            case XEM_BENH_AN:
                if ("benhnhan".equals(currentUserType)) {
                    handleViewMedicalRecord(callback);
                } else if ("bacsi".equals(currentUserType)) {
                    handleViewMedicalRecordForDoctor(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân và bác sĩ mới có thể xem bệnh án", callback);
                }
                break;

            case XEM_DON_THUOC:
                if ("benhnhan".equals(currentUserType)) {
                    handleViewPrescriptions(callback);
                } else if ("bacsi".equals(currentUserType)) {
                    handleViewPrescriptionsForDoctor(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân và bác sĩ mới có thể xem đơn thuốc", callback);
                }
                break;

            case XEM_HOA_DON:
                if ("benhnhan".equals(currentUserType)) {
                    handleViewInvoices(callback);
                } else if ("bacsi".equals(currentUserType)) {
                    handleViewInvoicesForDoctor(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân và bác sĩ mới có thể xem hóa đơn", callback);
                }
                break;

            case QUAN_LY_UONG_THUOC:
                if ("benhnhan".equals(currentUserType)) {
                    handleMedicineManagement(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân mới có thể quản lý uống thuốc", callback);
                }
                break;

            case XEM_THONG_BAO:
                if ("benhnhan".equals(currentUserType)) {
                    handleViewNotifications(callback);
                } else if ("bacsi".equals(currentUserType)) {
                    handleViewNotificationsForDoctor(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân và bác sĩ mới có thể xem thông báo", callback);
                }
                break;

            case CHAT_VOI_BAC_SI:
                if ("benhnhan".equals(currentUserType)) {
                    handleChatWithDoctor(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bệnh nhân mới có thể chat với bác sĩ", callback);
                }
                break;

            // ============================================
            // DOCTOR INTENTS
            // ============================================
            case XEM_LICH_LAM_VIEC:
                if ("bacsi".equals(currentUserType)) {
                    handleDoctorSchedule(userMessage, callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể xem lịch làm việc", callback);
                }
                break;

            case XEM_BENH_NHAN_NGAY:
                if ("bacsi".equals(currentUserType)) {
                    handleViewTodayPatients(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể xem danh sách bệnh nhân", callback);
                }
                break;

            case QUAN_LY_BENH_AN:
                if ("bacsi".equals(currentUserType)) {
                    handleManageMedicalRecords(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể quản lý bệnh án", callback);
                }
                break;

            case XAC_NHAN_LICH_KHAM:
                if ("bacsi".equals(currentUserType)) {
                    handleConfirmAppointments(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể xác nhận lịch khám", callback);
                }
                break;

            case QUAN_LY_DON_THUOC_BS:
                if ("bacsi".equals(currentUserType)) {
                    handleManagePrescriptions(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể quản lý đơn thuốc", callback);
                }
                break;

            case NHAP_MA_KHAM:
                if ("bacsi".equals(currentUserType)) {
                    handleEnterPatientCode(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể nhập mã khám", callback);
                }
                break;

            case AI_ASSISTANT:
                if ("bacsi".equals(currentUserType)) {
                    handleAIAssistant(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể sử dụng AI Assistant", callback);
                }
                break;

            case CHAT_VOI_BENH_NHAN:
                if ("bacsi".equals(currentUserType)) {
                    handleChatWithPatients(callback);
                } else {
                    handleUnauthorizedAction("Chỉ bác sĩ mới có thể chat với bệnh nhân", callback);
                }
                break;

            // ============================================
            // COMMON INTENTS
            // ============================================
            case TRA_CUU_BAC_SI:
                handleDoctorSearch(callback);
                break;

            case TRA_CUU_THONG_TIN:
                handleHospitalInfo(userMessage, callback);
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

    /**
     * XỬ LÝ CHỌN ROLE - BỆNH NHÂN HAY BÁC SĨ
     */
    private void handleRoleSelection(ChatCallback callback) {
        conversationContext.setState(ConversationContext.ConversationState.WAITING_ROLE_SELECTION);

        ChatResponse response = new ChatResponse(
            "👋 Xin chào! Tôi là **MediBot** - trợ lý ảo thông minh của bệnh viện.\n\n" +
            "Tôi có thể hỗ trợ bạn với nhiều chức năng khác nhau. " +
            "Trước tiên, vui lòng cho tôi biết bạn là:",
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("🏥 Tôi là Bệnh nhân");
        quickReplies.add("👨‍⚕️ Tôi là Bác sĩ");
        response.setQuickReplies(quickReplies);

        callback.onResponse(response);
    }

    /**
     * XỬ LÝ HÀNH ĐỘNG KHÔNG ĐƯỢC PHÉP
     */
    private void handleUnauthorizedAction(String message, ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "⚠️ " + message + "\n\n" +
            "Bạn có muốn chuyển đổi vai trò không?",
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("🔄 Chuyển đổi vai trò");
        quickReplies.add("📋 Xem menu chức năng");
        response.setQuickReplies(quickReplies);

        callback.onResponse(response);
    }

    // ============================================
    // BOOKING FLOW - TÍCH HỢP THỰC TẾ VỚI FIRESTORE
    // ============================================

    private void handleBookingIntent(ChatCallback callback) {
        conversationContext.setState(ConversationContext.ConversationState.WAITING_SPECIALTY_SELECTION);

        // Lấy danh sách chuyên khoa thực tế từ Firestore
        repo.getAll("BacSi", querySnapshot -> {
            List<String> chuyenKhoaList = new ArrayList<>();
            for (var doc : querySnapshot.getDocuments()) {
                com.example.doannt118.model.BacSi bacSi = doc.toObject(com.example.doannt118.model.BacSi.class);
                if (bacSi != null && bacSi.getChuyenKhoa() != null &&
                    "Đã xác thực".equals(bacSi.getTrangThaiXacThuc())) {
                    String chuyenKhoa = bacSi.getChuyenKhoa();
                    if (!chuyenKhoaList.contains(chuyenKhoa)) {
                        chuyenKhoaList.add(chuyenKhoa);
                    }
                }
            }

            if (chuyenKhoaList.isEmpty()) {
                ChatResponse response = new ChatResponse(
                    "❌ Hiện tại chưa có bác sĩ nào trong hệ thống.\n\n" +
                    "Vui lòng liên hệ lễ tân để được hỗ trợ: 1900-1234",
                    ChatResponse.ResponseType.TEXT
                );
                callback.onResponse(response);
                return;
            }

            ChatResponse response = new ChatResponse(
                "🩺 **ĐẶT LỊCH KHÁM BỆNH**\n\n" +
                "Tôi sẽ giúp bạn đặt lịch khám một cách nhanh chóng! 📅\n\n" +
                "Vui lòng chọn chuyên khoa bạn muốn khám:",
                ChatResponse.ResponseType.QUICK_REPLY
            );

            // Giới hạn tối đa 4 chuyên khoa để hiển thị gọn
            List<String> quickReplies = new ArrayList<>();
            for (int i = 0; i < Math.min(chuyenKhoaList.size(), 4); i++) {
                quickReplies.add(chuyenKhoaList.get(i));
            }

            // Nếu có nhiều hơn 4 chuyên khoa, thêm nút "Xem thêm"
            if (chuyenKhoaList.size() > 4) {
                quickReplies.add("📋 Xem tất cả chuyên khoa");
            }

            response.setQuickReplies(quickReplies);

            // Lưu danh sách chuyên khoa vào context
            conversationContext.setData("chuyenKhoaList", chuyenKhoaList);

            callback.onResponse(response);

        }, e -> {
            ChatResponse response = new ChatResponse(
                "❌ Lỗi khi tải danh sách chuyên khoa. Vui lòng thử lại sau!",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
        });
    }

    // ============================================
    // PATIENT HANDLERS - SIMPLIFIED
    // ============================================

    private void handleViewPatientAppointments(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy lịch khám thực tế từ Firestore
        repo.getByField("LichKham", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.LichKham> lichKhamList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.LichKham lichKham =
                        doc.toObject(com.example.doannt118.model.LichKham.class);
                    if (lichKham != null) {
                        lichKhamList.add(lichKham);
                    }
                }

                if (lichKhamList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "📅 LỊCH KHÁM CỦA BẠN\n\n" +
                        "🔍 Bạn chưa có lịch khám nào.\n\n" +
                        "Bạn có muốn đặt lịch khám mới không?",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("📅 Đặt lịch khám mới");
                    quickReplies.add("👨‍⚕️ Tìm bác sĩ");
                    quickReplies.add("🏥 Thông tin bệnh viện");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày khám (mới nhất trước)
                lichKhamList.sort((a, b) -> {
                    if (a.getNgayKham() == null) return 1;
                    if (b.getNgayKham() == null) return -1;
                    return b.getNgayKham().compareTo(a.getNgayKham());
                });

                // Tạo response với thông tin lịch khám
                StringBuilder responseText = new StringBuilder();
                responseText.append("📅 LỊCH KHÁM CỦA BẠN\n\n");
                responseText.append("📊 Tổng cộng: ").append(lichKhamList.size()).append(" lịch khám\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));

                int count = 0;
                for (com.example.doannt118.model.LichKham lichKham : lichKhamList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 lịch gần nhất

                    String statusIcon = getStatusIcon(lichKham.getTrangThai());
                    String statusText = getStatusText(lichKham.getTrangThai());

                    responseText.append(statusIcon).append(" ").append(statusText).append("\n");

                    if (lichKham.getNgayKham() != null) {
                        Date ngayKham = lichKham.getNgayKham().toDate();
                        responseText.append("📅 ").append(dayFormat.format(ngayKham))
                                   .append(", ").append(dateFormat.format(ngayKham)).append("\n");
                    }

                    if (lichKham.getGioKham() != null) {
                        responseText.append("⏰ ").append(lichKham.getGioKham()).append("\n");
                    }

                    // Lấy tên bác sĩ (cần query thêm)
                    responseText.append("👨‍⚕️ Đang tải thông tin bác sĩ...\n");
                    responseText.append("🆔 ").append(lichKham.getMaLichKham().substring(0, 8).toUpperCase()).append("\n\n");

                    count++;
                }

                if (lichKhamList.size() > 3) {
                    responseText.append("📋 Và ").append(lichKhamList.size() - 3).append(" lịch khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("📅 Đặt lịch mới");
                quickReplies.add("❌ Hủy lịch khám");
                quickReplies.add("👨‍⚕️ Tìm bác sĩ");
                quickReplies.add("🔄 Làm mới danh sách");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading appointments: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải dữ liệu\n\n" +
                    "Không thể tải danh sách lịch khám. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("📅 Đặt lịch mới");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private String getStatusIcon(String trangThai) {
        if (trangThai == null) return "❓";
        switch (trangThai) {
            case "CHO": return "⏳";
            case "XAC_NHAN": return "✅";
            case "HOAN_THANH": return "🎉";
            case "HUY": return "❌";
            default: return "❓";
        }
    }

    private String getStatusText(String trangThai) {
        if (trangThai == null) return "Không rõ";
        switch (trangThai) {
            case "CHO": return "Chờ xác nhận";
            case "XAC_NHAN": return "Đã xác nhận";
            case "HOAN_THANH": return "Hoàn thành";
            case "HUY": return "Đã hủy";
            default: return "Không rõ";
        }
    }

    private void handleViewMedicalRecord(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách bệnh án thực tế từ Firestore
        repo.getByField("BenhAn", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.BenhAn> benhAnList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.BenhAn benhAn =
                        doc.toObject(com.example.doannt118.model.BenhAn.class);
                    if (benhAn != null) {
                        benhAnList.add(benhAn);
                    }
                }

                if (benhAnList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "📋 BỆNH ÁN CỦA BẠN\n\n" +
                        "📋 Bạn chưa có bệnh án nào.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("📅 Đặt lịch khám");
                    quickReplies.add("👨‍⚕️ Tìm bác sĩ");
                    quickReplies.add("🏥 Thông tin bệnh viện");
                    quickReplies.add("📞 Liên hệ hỗ trợ");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày khám (mới nhất trước)
                benhAnList.sort((a, b) -> {
                    com.google.firebase.Timestamp tsA = a.getNgayKhamAsTimestamp();
                    com.google.firebase.Timestamp tsB = b.getNgayKhamAsTimestamp();
                    if (tsA == null) return 1;
                    if (tsB == null) return -1;
                    return tsB.compareTo(tsA);
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("📋 BỆNH ÁN CỦA BẠN\n\n");
                responseText.append("📊 Tổng cộng: ").append(benhAnList.size()).append(" bệnh án\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                int count = 0;
                for (com.example.doannt118.model.BenhAn benhAn : benhAnList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 bệnh án gần nhất

                    responseText.append("🏥 LẦN KHÁM ").append(count + 1).append("\n");
                    responseText.append("🆔 Mã BA: ").append(benhAn.getMaBenhAn().substring(0, 8).toUpperCase()).append("\n");

                    com.google.firebase.Timestamp ngayKham = benhAn.getNgayKhamAsTimestamp();
                    if (ngayKham != null) {
                        responseText.append("📅 Ngày khám: ").append(dateFormat.format(ngayKham.toDate())).append("\n");
                    }

                    if (benhAn.getChanDoan() != null && !benhAn.getChanDoan().isEmpty()) {
                        String chanDoan = benhAn.getChanDoan();
                        if (chanDoan.length() > 50) {
                            chanDoan = chanDoan.substring(0, 50) + "...";
                        }
                        responseText.append("🩺 Chẩn đoán: ").append(chanDoan).append("\n");
                    }

                    if (benhAn.getLoaiKham() != null && !benhAn.getLoaiKham().isEmpty()) {
                        responseText.append("🔍 Loại khám: ").append(benhAn.getLoaiKham()).append("\n");
                    }

                    if (benhAn.getPhiKham() > 0) {
                        responseText.append("💰 Phí khám: ").append(String.format("%,d", benhAn.getPhiKham())).append(" VNĐ\n");
                    }

                    responseText.append("\n");
                    count++;
                }

                if (benhAnList.size() > 3) {
                    responseText.append("📋 Và ").append(benhAnList.size() - 3).append(" bệnh án khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết đầy đủ, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("💊 Xem đơn thuốc");
                quickReplies.add("📅 Đặt lịch tái khám");
                quickReplies.add("👨‍⚕️ Chat với bác sĩ");
                quickReplies.add("💰 Xem hóa đơn");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading medical records: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải bệnh án\n\n" +
                    "Không thể tải danh sách bệnh án. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("📅 Đặt lịch khám");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    /**
     * XỬ LÝ XEM BỆNH ÁN CHO BÁC SĨ - Xem bệnh án của bệnh nhân mà bác sĩ phụ trách
     */
    private void handleViewMedicalRecordForDoctor(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách bệnh án do bác sĩ này tạo từ Firestore
        repo.getByField("BenhAn", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.BenhAn> benhAnList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.BenhAn benhAn =
                        doc.toObject(com.example.doannt118.model.BenhAn.class);
                    if (benhAn != null) {
                        benhAnList.add(benhAn);
                    }
                }

                if (benhAnList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "📋 BỆNH ÁN BỆNH NHÂN\n\n" +
                        "📋 Bạn chưa tạo bệnh án nào.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("👥 Bệnh nhân hôm nay");
                    quickReplies.add("✅ Xác nhận lịch khám");
                    quickReplies.add("💊 Quản lý đơn thuốc");
                    quickReplies.add("📊 Thống kê");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày khám (mới nhất trước)
                benhAnList.sort((a, b) -> {
                    com.google.firebase.Timestamp tsA = a.getNgayKhamAsTimestamp();
                    com.google.firebase.Timestamp tsB = b.getNgayKhamAsTimestamp();
                    if (tsA == null) return 1;
                    if (tsB == null) return -1;
                    return tsB.compareTo(tsA);
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("📋 BỆNH ÁN BỆNH NHÂN\n\n");
                responseText.append("📊 Tổng cộng: ").append(benhAnList.size()).append(" bệnh án\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                int count = 0;
                for (com.example.doannt118.model.BenhAn benhAn : benhAnList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 bệnh án gần nhất

                    responseText.append("🏥 BỆNH ÁN ").append(count + 1).append("\n");
                    responseText.append("🆔 Mã BA: ").append(benhAn.getMaBenhAn().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("🆔 Mã BN: ").append(benhAn.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");

                    com.google.firebase.Timestamp ngayKham = benhAn.getNgayKhamAsTimestamp();
                    if (ngayKham != null) {
                        responseText.append("📅 Ngày khám: ").append(dateFormat.format(ngayKham.toDate())).append("\n");
                    }

                    if (benhAn.getChanDoan() != null && !benhAn.getChanDoan().isEmpty()) {
                        String chanDoan = benhAn.getChanDoan();
                        if (chanDoan.length() > 50) {
                            chanDoan = chanDoan.substring(0, 50) + "...";
                        }
                        responseText.append("🩺 Chẩn đoán: ").append(chanDoan).append("\n");
                    }

                    if (benhAn.getLoaiKham() != null && !benhAn.getLoaiKham().isEmpty()) {
                        responseText.append("🔍 Loại khám: ").append(benhAn.getLoaiKham()).append("\n");
                    }

                    if (benhAn.getPhiKham() > 0) {
                        responseText.append("💰 Phí khám: ").append(String.format("%,d", benhAn.getPhiKham())).append(" VNĐ\n");
                    }

                    responseText.append("\n");
                    count++;
                }

                if (benhAnList.size() > 3) {
                    responseText.append("📋 Và ").append(benhAnList.size() - 3).append(" bệnh án khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết đầy đủ, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("💊 Quản lý đơn thuốc");
                quickReplies.add("📊 Thống kê");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading medical records for doctor: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải bệnh án\n\n" +
                    "Không thể tải danh sách bệnh án. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void handleViewPrescriptions(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách đơn thuốc thực tế từ Firestore
        repo.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.DonThuoc> donThuocList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.DonThuoc donThuoc =
                        doc.toObject(com.example.doannt118.model.DonThuoc.class);
                    if (donThuoc != null) {
                        donThuocList.add(donThuoc);
                    }
                }

                if (donThuocList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "💊 ĐỚN THUỐC CỦA BẠN\n\n" +
                        "📋 Bạn chưa có đơn thuốc nào.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("📅 Đặt lịch khám");
                    quickReplies.add("📋 Xem bệnh án");
                    quickReplies.add("👨‍⚕️ Tìm bác sĩ");
                    quickReplies.add("🏥 Thông tin bệnh viện");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày lập (mới nhất trước)
                donThuocList.sort((a, b) -> {
                    if (a.getNgayLap() == null) return 1;
                    if (b.getNgayLap() == null) return -1;
                    return b.getNgayLap().compareTo(a.getNgayLap());
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("💊 ĐỚN THUỐC CỦA BẠN\n\n");
                responseText.append("📊 Tổng cộng: ").append(donThuocList.size()).append(" đơn thuốc\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                int count = 0;
                for (com.example.doannt118.model.DonThuoc donThuoc : donThuocList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 đơn gần nhất

                    String statusIcon = getPrescriptionStatusIcon(donThuoc.getTrangThai());
                    String statusText = getPrescriptionStatusText(donThuoc.getTrangThai());

                    responseText.append(statusIcon).append(" ").append(statusText).append("\n");
                    responseText.append("🆔 Mã đơn: ").append(donThuoc.getMaDonThuoc().substring(0, 8).toUpperCase()).append("\n");

                    if (donThuoc.getNgayLap() != null) {
                        responseText.append("📅 Ngày kê: ").append(dateFormat.format(donThuoc.getNgayLap())).append("\n");
                    }

                    if (donThuoc.getSoNgayUong() > 0) {
                        responseText.append("⏰ Thời gian: ").append(donThuoc.getSoNgayUong()).append(" ngày\n");
                    }

                    // Tính ngày hết thuốc
                    if (donThuoc.getNgayBatDau() != null && donThuoc.getSoNgayUong() > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(donThuoc.getNgayBatDau());
                        cal.add(Calendar.DAY_OF_MONTH, donThuoc.getSoNgayUong());
                        responseText.append("📅 Hết thuốc: ").append(dateFormat.format(cal.getTime())).append("\n");
                    }

                    responseText.append("\n");
                    count++;
                }

                if (donThuocList.size() > 3) {
                    responseText.append("📋 Và ").append(donThuocList.size() - 3).append(" đơn thuốc khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết từng loại thuốc, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("⏰ Quản lý uống thuốc");
                quickReplies.add("📋 Xem bệnh án");
                quickReplies.add("👨‍⚕️ Chat với bác sĩ");
                quickReplies.add("📅 Đặt lịch tái khám");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading prescriptions: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải đơn thuốc\n\n" +
                    "Không thể tải danh sách đơn thuốc. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("📅 Đặt lịch khám");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    /**
     * XỬ LÝ XEM ĐƠN THUỐC CHO BÁC SĨ - Xem đơn thuốc mà bác sĩ đã kê
     */
    private void handleViewPrescriptionsForDoctor(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách đơn thuốc do bác sĩ này kê từ Firestore
        repo.getByField("DonThuoc", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.DonThuoc> donThuocList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.DonThuoc donThuoc =
                        doc.toObject(com.example.doannt118.model.DonThuoc.class);
                    if (donThuoc != null) {
                        donThuocList.add(donThuoc);
                    }
                }

                if (donThuocList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "💊 ĐƠN THUỐC ĐÃ KÊ\n\n" +
                        "📋 Bạn chưa kê đơn thuốc nào.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("👥 Bệnh nhân hôm nay");
                    quickReplies.add("📋 Quản lý bệnh án");
                    quickReplies.add("✅ Xác nhận lịch khám");
                    quickReplies.add("📊 Thống kê");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày lập (mới nhất trước)
                donThuocList.sort((a, b) -> {
                    if (a.getNgayLap() == null) return 1;
                    if (b.getNgayLap() == null) return -1;
                    return b.getNgayLap().compareTo(a.getNgayLap());
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("💊 ĐƠN THUỐC ĐÃ KÊ\n\n");
                responseText.append("📊 Tổng cộng: ").append(donThuocList.size()).append(" đơn thuốc\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                int count = 0;
                for (com.example.doannt118.model.DonThuoc donThuoc : donThuocList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 đơn gần nhất

                    String statusIcon = getPrescriptionStatusIcon(donThuoc.getTrangThai());
                    String statusText = getPrescriptionStatusText(donThuoc.getTrangThai());

                    responseText.append(statusIcon).append(" ").append(statusText).append("\n");
                    responseText.append("🆔 Mã đơn: ").append(donThuoc.getMaDonThuoc().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("🆔 Mã BN: ").append(donThuoc.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");

                    if (donThuoc.getNgayLap() != null) {
                        responseText.append("📅 Ngày kê: ").append(dateFormat.format(donThuoc.getNgayLap())).append("\n");
                    }

                    if (donThuoc.getSoNgayUong() > 0) {
                        responseText.append("⏰ Thời gian: ").append(donThuoc.getSoNgayUong()).append(" ngày\n");
                    }

                    // Tính ngày hết thuốc
                    if (donThuoc.getNgayBatDau() != null && donThuoc.getSoNgayUong() > 0) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(donThuoc.getNgayBatDau());
                        cal.add(Calendar.DAY_OF_MONTH, donThuoc.getSoNgayUong());
                        responseText.append("📅 Hết thuốc: ").append(dateFormat.format(cal.getTime())).append("\n");
                    }

                    responseText.append("\n");
                    count++;
                }

                if (donThuocList.size() > 3) {
                    responseText.append("📋 Và ").append(donThuocList.size() - 3).append(" đơn thuốc khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết từng loại thuốc, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📋 Quản lý bệnh án");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("📊 Thống kê");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading prescriptions for doctor: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải đơn thuốc\n\n" +
                    "Không thể tải danh sách đơn thuốc. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private String getPrescriptionStatusIcon(String trangThai) {
        if (trangThai == null) return "❓";
        switch (trangThai) {
            case "DANG_DUNG": return "💊";
            case "DA_HET": return "✅";
            case "DA_HUY": return "❌";
            default: return "❓";
        }
    }

    private String getPrescriptionStatusText(String trangThai) {
        if (trangThai == null) return "Không rõ";
        switch (trangThai) {
            case "DANG_DUNG": return "Đang sử dụng";
            case "DA_HET": return "Đã hết thuốc";
            case "DA_HUY": return "Đã hủy";
            default: return "Không rõ";
        }
    }

    private void handleCancelAppointment(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "❌ HỦY LỊCH KHÁM\n\n" +
            "Để hủy lịch khám, vui lòng sử dụng ứng dụng chính.\n\n" +
            "Hoặc liên hệ hotline: 1900-1234",
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("📅 Xem lịch khám");
        quickReplies.add("📞 Liên hệ hỗ trợ");
        response.setQuickReplies(quickReplies);

        callback.onResponse(response);
    }

    private void handleViewInvoices(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách hóa đơn thực tế từ Firestore
        repo.getByField("HoaDon", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.HoaDon> hoaDonList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.HoaDon hoaDon =
                        doc.toObject(com.example.doannt118.model.HoaDon.class);
                    if (hoaDon != null) {
                        hoaDonList.add(hoaDon);
                    }
                }

                if (hoaDonList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "💰 HÓA ĐƠN CỦA BẠN\n\n" +
                        "📋 Bạn chưa có hóa đơn nào.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("📅 Đặt lịch khám");
                    quickReplies.add("📋 Xem bệnh án");
                    quickReplies.add("🏥 Bảng giá dịch vụ");
                    quickReplies.add("📞 Liên hệ hỗ trợ");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày lập (mới nhất trước)
                hoaDonList.sort((a, b) -> {
                    if (a.getNgayLap() == null) return 1;
                    if (b.getNgayLap() == null) return -1;
                    return b.getNgayLap().compareTo(a.getNgayLap());
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("💰 HÓA ĐƠN CỦA BẠN\n\n");
                responseText.append("📊 Tổng cộng: ").append(hoaDonList.size()).append(" hóa đơn\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                // Tính tổng tiền và thống kê
                long tongTienTatCa = 0;
                int daThanhtoan = 0;
                int chuaThanhToan = 0;

                for (com.example.doannt118.model.HoaDon hoaDon : hoaDonList) {
                    tongTienTatCa += hoaDon.getTongTienLong();
                    if ("DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
                        daThanhtoan++;
                    } else {
                        chuaThanhToan++;
                    }
                }

                responseText.append("💵 Tổng tiền: ").append(String.format("%,d", tongTienTatCa)).append(" VNĐ\n");
                responseText.append("✅ Đã thanh toán: ").append(daThanhtoan).append(" hóa đơn\n");
                responseText.append("⏳ Chưa thanh toán: ").append(chuaThanhToan).append(" hóa đơn\n\n");

                // Hiển thị chi tiết một số hóa đơn gần nhất
                responseText.append("📋 HÓA ĐƠN GẦN NHẤT:\n\n");

                int count = 0;
                for (com.example.doannt118.model.HoaDon hoaDon : hoaDonList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 hóa đơn gần nhất

                    String statusIcon = getInvoiceStatusIcon(hoaDon.getTrangThai());
                    String statusText = getInvoiceStatusText(hoaDon.getTrangThai());

                    responseText.append(statusIcon).append(" ").append(statusText).append("\n");
                    responseText.append("🆔 Mã HĐ: ").append(hoaDon.getMaHoaDon().substring(0, 8).toUpperCase()).append("\n");

                    if (hoaDon.getNgayLap() != null) {
                        responseText.append("📅 Ngày lập: ").append(dateFormat.format(hoaDon.getNgayLap())).append("\n");
                    }

                    // Chi tiết chi phí
                    if (hoaDon.getPhiKham() > 0) {
                        responseText.append("🩺 Phí khám: ").append(String.format("%,d", hoaDon.getPhiKham())).append(" VNĐ\n");
                    }
                    if (hoaDon.getPhiThuoc() > 0) {
                        responseText.append("💊 Phí thuốc: ").append(String.format("%,d", hoaDon.getPhiThuoc())).append(" VNĐ\n");
                    }
                    if (hoaDon.getPhiDichVu() > 0) {
                        responseText.append("🔬 Phí dịch vụ: ").append(String.format("%,d", hoaDon.getPhiDichVu())).append(" VNĐ\n");
                    }

                    long tongTien = hoaDon.getTongTienLong();
                    if (tongTien > 0) {
                        responseText.append("💰 Tổng tiền: ").append(String.format("%,d", tongTien)).append(" VNĐ\n");
                    }

                    if (hoaDon.getNgayThanhToan() != null) {
                        responseText.append("✅ Đã thanh toán: ").append(dateFormat.format(hoaDon.getNgayThanhToan())).append("\n");
                    }

                    responseText.append("\n");
                    count++;
                }

                if (hoaDonList.size() > 3) {
                    responseText.append("📋 Và ").append(hoaDonList.size() - 3).append(" hóa đơn khác...\n\n");
                }

                responseText.append("💡 *Để thanh toán trực tuyến, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("📋 Xem bệnh án");
                quickReplies.add("💊 Xem đơn thuốc");
                quickReplies.add("🏥 Bảng giá dịch vụ");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading invoices: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải hóa đơn\n\n" +
                    "Không thể tải danh sách hóa đơn. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("📅 Đặt lịch khám");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    /**
     * XỬ LÝ XEM HÓA ĐƠN CHO BÁC SĨ - Xem hóa đơn liên quan đến bác sĩ
     */
    private void handleViewInvoicesForDoctor(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách hóa đơn liên quan đến bác sĩ từ Firestore
        repo.getByField("HoaDon", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.HoaDon> hoaDonList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.HoaDon hoaDon =
                        doc.toObject(com.example.doannt118.model.HoaDon.class);
                    if (hoaDon != null) {
                        hoaDonList.add(hoaDon);
                    }
                }

                if (hoaDonList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "💰 HÓA ĐƠN LIÊN QUAN\n\n" +
                        "📋 Chưa có hóa đơn nào liên quan đến bạn.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("👥 Bệnh nhân hôm nay");
                    quickReplies.add("📋 Quản lý bệnh án");
                    quickReplies.add("💊 Quản lý đơn thuốc");
                    quickReplies.add("📊 Thống kê");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo ngày lập (mới nhất trước)
                hoaDonList.sort((a, b) -> {
                    if (a.getNgayLap() == null) return 1;
                    if (b.getNgayLap() == null) return -1;
                    return b.getNgayLap().compareTo(a.getNgayLap());
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("💰 HÓA ĐƠN LIÊN QUAN\n\n");
                responseText.append("📊 Tổng cộng: ").append(hoaDonList.size()).append(" hóa đơn\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                // Tính tổng tiền và thống kê
                long tongTienTatCa = 0;
                int daThanhtoan = 0;
                int chuaThanhToan = 0;

                for (com.example.doannt118.model.HoaDon hoaDon : hoaDonList) {
                    tongTienTatCa += hoaDon.getTongTienLong();
                    if ("DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
                        daThanhtoan++;
                    } else {
                        chuaThanhToan++;
                    }
                }

                responseText.append("💵 Tổng doanh thu: ").append(String.format("%,d", tongTienTatCa)).append(" VNĐ\n");
                responseText.append("✅ Đã thanh toán: ").append(daThanhtoan).append(" hóa đơn\n");
                responseText.append("⏳ Chưa thanh toán: ").append(chuaThanhToan).append(" hóa đơn\n\n");

                // Hiển thị chi tiết một số hóa đơn gần nhất
                responseText.append("📋 HÓA ĐƠN GẦN NHẤT:\n\n");

                int count = 0;
                for (com.example.doannt118.model.HoaDon hoaDon : hoaDonList) {
                    if (count >= 3) break; // Chỉ hiển thị 3 hóa đơn gần nhất

                    String statusIcon = getInvoiceStatusIcon(hoaDon.getTrangThai());
                    String statusText = getInvoiceStatusText(hoaDon.getTrangThai());

                    responseText.append(statusIcon).append(" ").append(statusText).append("\n");
                    responseText.append("🆔 Mã HĐ: ").append(hoaDon.getMaHoaDon().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("🆔 Mã BN: ").append(hoaDon.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");

                    if (hoaDon.getNgayLap() != null) {
                        responseText.append("📅 Ngày lập: ").append(dateFormat.format(hoaDon.getNgayLap())).append("\n");
                    }

                    // Chi tiết chi phí
                    if (hoaDon.getPhiKham() > 0) {
                        responseText.append("🩺 Phí khám: ").append(String.format("%,d", hoaDon.getPhiKham())).append(" VNĐ\n");
                    }
                    if (hoaDon.getPhiThuoc() > 0) {
                        responseText.append("💊 Phí thuốc: ").append(String.format("%,d", hoaDon.getPhiThuoc())).append(" VNĐ\n");
                    }
                    if (hoaDon.getPhiDichVu() > 0) {
                        responseText.append("🔬 Phí dịch vụ: ").append(String.format("%,d", hoaDon.getPhiDichVu())).append(" VNĐ\n");
                    }

                    long tongTien = hoaDon.getTongTienLong();
                    if (tongTien > 0) {
                        responseText.append("💰 Tổng tiền: ").append(String.format("%,d", tongTien)).append(" VNĐ\n");
                    }

                    if (hoaDon.getNgayThanhToan() != null) {
                        responseText.append("✅ Đã thanh toán: ").append(dateFormat.format(hoaDon.getNgayThanhToan())).append("\n");
                    }

                    responseText.append("\n");
                    count++;
                }

                if (hoaDonList.size() > 3) {
                    responseText.append("📋 Và ").append(hoaDonList.size() - 3).append(" hóa đơn khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết đầy đủ, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📋 Quản lý bệnh án");
                quickReplies.add("💊 Quản lý đơn thuốc");
                quickReplies.add("📊 Thống kê");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading invoices for doctor: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải hóa đơn\n\n" +
                    "Không thể tải danh sách hóa đơn. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private String getInvoiceStatusIcon(String trangThai) {
        if (trangThai == null) return "❓";
        switch (trangThai) {
            case "CHUA_THANH_TOAN": return "⏳";
            case "DA_THANH_TOAN": return "✅";
            case "DA_HUY": return "❌";
            case "HOAN_TIEN": return "🔄";
            default: return "❓";
        }
    }

    private String getInvoiceStatusText(String trangThai) {
        if (trangThai == null) return "Không rõ";
        switch (trangThai) {
            case "CHUA_THANH_TOAN": return "Chưa thanh toán";
            case "DA_THANH_TOAN": return "Đã thanh toán";
            case "DA_HUY": return "Đã hủy";
            case "HOAN_TIEN": return "Hoàn tiền";
            default: return "Không rõ";
        }
    }

    // Thêm method xử lý quản lý uống thuốc - tích hợp dữ liệu thực
    private void handleMedicineManagement(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy đơn thuốc đang sử dụng từ Firestore
        repo.getByField("DonThuoc", "maBenhNhan", maBenhNhan,
            querySnapshot -> {
                List<com.example.doannt118.model.DonThuoc> donThuocDangDung = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.DonThuoc donThuoc =
                        doc.toObject(com.example.doannt118.model.DonThuoc.class);
                    if (donThuoc != null && "DANG_DUNG".equals(donThuoc.getTrangThai())) {
                        donThuocDangDung.add(donThuoc);
                    }
                }

                if (donThuocDangDung.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "⏰ QUẢN LÝ UỐNG THUỐC\n\n" +
                        "💊 Hiện tại bạn không có đơn thuốc nào đang sử dụng.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("💊 Xem tất cả đơn thuốc");
                    quickReplies.add("📅 Đặt lịch khám");
                    quickReplies.add("👨‍⚕️ Chat với bác sĩ");
                    quickReplies.add("📞 Liên hệ hỗ trợ");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                StringBuilder responseText = new StringBuilder();
                responseText.append("⏰ QUẢN LÝ UỐNG THUỐC\n\n");
                responseText.append("💊 Đang có ").append(donThuocDangDung.size()).append(" đơn thuốc cần uống:\n\n");

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Calendar today = Calendar.getInstance();

                for (com.example.doannt118.model.DonThuoc donThuoc : donThuocDangDung) {
                    responseText.append("📋 Đơn thuốc ").append(donThuoc.getMaDonThuoc().substring(0, 6).toUpperCase()).append("\n");

                    if (donThuoc.getNgayLap() != null) {
                        responseText.append("📅 Ngày kê: ").append(dateFormat.format(donThuoc.getNgayLap())).append("\n");
                    }

                    if (donThuoc.getSoNgayUong() > 0) {
                        responseText.append("⏰ Thời gian: ").append(donThuoc.getSoNgayUong()).append(" ngày\n");

                        // Tính ngày còn lại
                        if (donThuoc.getNgayBatDau() != null) {
                            Calendar startCal = Calendar.getInstance();
                            startCal.setTime(donThuoc.getNgayBatDau());
                            startCal.add(Calendar.DAY_OF_MONTH, donThuoc.getSoNgayUong());

                            long daysLeft = (startCal.getTimeInMillis() - today.getTimeInMillis()) / (24 * 60 * 60 * 1000);
                            if (daysLeft > 0) {
                                responseText.append("📅 Còn lại: ").append(daysLeft).append(" ngày\n");
                            } else if (daysLeft == 0) {
                                responseText.append("⚠️ Hết thuốc hôm nay\n");
                            } else {
                                responseText.append("❌ Đã hết thuốc\n");
                            }
                        }
                    }

                    responseText.append("\n");
                }

                // Lấy chi tiết thuốc từ collection ChiTietDonThuoc
                loadMedicineDetails(donThuocDangDung, responseText, callback);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading medicine management: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải thông tin thuốc\n\n" +
                    "Không thể tải thông tin quản lý uống thuốc. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("💊 Xem đơn thuốc");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void loadMedicineDetails(List<com.example.doannt118.model.DonThuoc> donThuocList,
                                   StringBuilder responseText, ChatCallback callback) {
        if (donThuocList.isEmpty()) {
            finalizeMedicineManagementResponse(responseText, callback);
            return;
        }

        // Lấy chi tiết thuốc từ đơn đầu tiên
        String maDonThuoc = donThuocList.get(0).getMaDonThuoc();

        repo.getByField("ChiTietDonThuoc", "maDonThuoc", maDonThuoc,
            querySnapshot -> {
                responseText.append("💊 CHI TIẾT THUỐC CẦN UỐNG:**\n\n");

                int count = 0;
                for (var doc : querySnapshot.getDocuments()) {
                    if (count >= 3) break; // Chỉ hiển thị 3 loại thuốc đầu tiên

                    com.example.doannt118.model.ChiTietDonThuoc chiTiet =
                        doc.toObject(com.example.doannt118.model.ChiTietDonThuoc.class);
                    if (chiTiet != null) {
                        responseText.append("💊 ").append(chiTiet.getTenThuoc()).append("\n");
                        responseText.append("📝 ").append(chiTiet.getLieuDungDayDu()).append("\n");

                        // Hiển thị ca uống
                        StringBuilder caUong = new StringBuilder();
                        if (chiTiet.isUongSang()) caUong.append("🌅 Sáng ");
                        if (chiTiet.isUongTrua()) caUong.append("☀️ Trưa ");
                        if (chiTiet.isUongChieu()) caUong.append("🌆 Chiều ");
                        if (chiTiet.isUongToi()) caUong.append("🌙 Tối ");

                        if (caUong.length() > 0) {
                            responseText.append("⏰ Ca uống: ").append(caUong.toString()).append("\n");
                        }

                        if (chiTiet.getCachDung() != null && !chiTiet.getCachDung().isEmpty()) {
                            responseText.append("📋 ").append(chiTiet.getCachDung()).append("\n");
                        }

                        responseText.append("\n");
                        count++;
                    }
                }

                if (querySnapshot.size() > 3) {
                    responseText.append("💊 Và ").append(querySnapshot.size() - 3).append(" loại thuốc khác...\n\n");
                }

                finalizeMedicineManagementResponse(responseText, callback);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading medicine details: ", e);
                finalizeMedicineManagementResponse(responseText, callback);
            });
    }

    private void finalizeMedicineManagementResponse(StringBuilder responseText, ChatCallback callback) {
        responseText.append("🔔 NHẮC NHỞ QUAN TRỌNG:\n");
        responseText.append("• ⏰ Uống thuốc đúng giờ theo chỉ định\n");
        responseText.append("• 📱 Sử dụng app để điểm danh uống thuốc\n");
        responseText.append("• 👨‍⚕️ Liên hệ bác sĩ nếu có tác dụng phụ\n\n");
        responseText.append("💡 *Để điểm danh chi tiết, vui lòng sử dụng ứng dụng chính.*");

        ChatResponse response = new ChatResponse(
            responseText.toString(),
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("💊 Xem đơn thuốc");
        quickReplies.add("📋 Xem bệnh án");
        quickReplies.add("👨‍⚕️ Chat với bác sĩ");
        quickReplies.add("📞 Liên hệ hỗ trợ");

        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }

    private String getMedicineStatusIcon(String trangThai) {
        if (trangThai == null) return "❓";
        switch (trangThai) {
            case "CHUA_UONG": return "⏰";
            case "DA_UONG": return "✅";
            case "BO_QUA": return "⏭️";
            case "TRE_GIO": return "⚠️";
            default: return "❓";
        }
    }

    // Thêm method xử lý xem thông báo - đơn giản hóa
    private void handleViewNotifications(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        ChatResponse response = new ChatResponse(
            "🔔 THÔNG BÁO\n\n" +
            "Để xem danh sách thông báo chi tiết, vui lòng sử dụng ứng dụng chính.\n\n" +
            "📋 Các loại thông báo:\n" +
            "• 📅 Lịch khám được xác nhận/thay đổi\n" +
            "• 📋 Kết quả xét nghiệm có sẵn\n" +
            "• 💊 Nhắc nhở uống thuốc\n" +
            "• 💰 Thông báo thanh toán\n" +
            "• ⚙️ Cập nhật hệ thống\n\n" +
            "🔔 Bạn sẽ nhận được thông báo push khi có tin mới.",
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("📅 Xem lịch khám");
        quickReplies.add("💊 Xem đơn thuốc");
        quickReplies.add("👨‍⚕️ Chat với bác sĩ");
        quickReplies.add("📞 Liên hệ hỗ trợ");

        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }

    /**
     * XỬ LÝ XEM THÔNG BÁO CHO BÁC SĨ
     */
    private void handleViewNotificationsForDoctor(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        ChatResponse response = new ChatResponse(
            "🔔 THÔNG BÁO BÁC SĨ\n\n" +
            "Để xem danh sách thông báo chi tiết, vui lòng sử dụng ứng dụng chính.\n\n" +
            "📋 Các loại thông báo:\n" +
            "• 📅 Lịch khám mới cần xác nhận\n" +
            "• 👥 Bệnh nhân hủy lịch khám\n" +
            "• 💬 Tin nhắn từ bệnh nhân\n" +
            "• 📊 Báo cáo thống kê\n" +
            "• ⚙️ Cập nhật hệ thống\n\n" +
            "🔔 Bạn sẽ nhận được thông báo push khi có tin mới.",
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("👥 Bệnh nhân hôm nay");
        quickReplies.add("✅ Xác nhận lịch khám");
        quickReplies.add("💬 Tin nhắn bệnh nhân");
        quickReplies.add("📊 Thống kê");

        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }

    private String getNotificationStatusIcon(String trangThai) {
        if (trangThai == null) return "📩";
        switch (trangThai) {
            case "CHUA_DOC": return "🔴";
            case "DA_DOC": return "✅";
            default: return "📩";
        }
    }

    // Thêm method xử lý chat với bác sĩ
    private void handleChatWithDoctor(ChatCallback callback) {
        if (maBenhNhan == null || maBenhNhan.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bệnh nhân. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        ChatResponse response = new ChatResponse(
            "💬 CHAT VỚI BÁC SĨ\n\n" +
            "Bạn có thể nhắn tin trực tiếp với bác sĩ để:\n" +
            "• 🤔 Hỏi về tình trạng sức khỏe\n" +
            "• 💊 Tư vấn về thuốc đang sử dụng\n" +
            "• 📋 Thắc mắc về kết quả khám\n" +
            "• 📅 Hỏi về lịch tái khám\n\n" +
            "Vui lòng sử dụng ứng dụng chính để chat trực tiếp với bác sĩ.",
            ChatResponse.ResponseType.QUICK_REPLY
        );

        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("👨‍⚕️ Chọn bác sĩ để chat");
        quickReplies.add("📋 Xem bệnh án");
        quickReplies.add("💊 Xem đơn thuốc");
        quickReplies.add("📅 Đặt lịch khám");

        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }

    // Thêm method xử lý tìm bác sĩ
    private void handleDoctorSearch(ChatCallback callback) {
        // Lấy danh sách bác sĩ từ Firestore
        repo.getAll("BacSi", querySnapshot -> {
            List<com.example.doannt118.model.BacSi> bacSiList = new ArrayList<>();
            Map<String, Integer> chuyenKhoaCount = new HashMap<>();

            for (var doc : querySnapshot.getDocuments()) {
                com.example.doannt118.model.BacSi bacSi =
                    doc.toObject(com.example.doannt118.model.BacSi.class);
                if (bacSi != null && "Đã xác thực".equals(bacSi.getTrangThaiXacThuc())) {
                    bacSiList.add(bacSi);

                    // Đếm số bác sĩ theo chuyên khoa
                    String chuyenKhoa = bacSi.getChuyenKhoa();
                    if (chuyenKhoa != null) {
                        chuyenKhoaCount.put(chuyenKhoa,
                            chuyenKhoaCount.getOrDefault(chuyenKhoa, 0) + 1);
                    }
                }
            }

            if (bacSiList.isEmpty()) {
                ChatResponse response = new ChatResponse(
                    "👨‍⚕️ TÌM BÁC SĨ\n\n" +
                    "❌ Hiện tại chưa có bác sĩ nào trong hệ thống.\n\n" +
                    "Vui lòng liên hệ lễ tân để được hỗ trợ: 1900-1234",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("📞 Liên hệ lễ tân");
                quickReplies.add("🏥 Thông tin bệnh viện");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                return;
            }

            // Tạo response với thông tin bác sĩ
            StringBuilder responseText = new StringBuilder();
            responseText.append("👨‍⚕️ TÌM BÁC SĨ\n\n");
            responseText.append("📊 Tổng cộng: ").append(bacSiList.size()).append(" bác sĩ\n\n");

            // Hiển thị theo chuyên khoa
            responseText.append("🩺 THEO CHUYÊN KHOA:\n");
            for (Map.Entry<String, Integer> entry : chuyenKhoaCount.entrySet()) {
                responseText.append("• ").append(entry.getKey())
                           .append(": ").append(entry.getValue()).append(" bác sĩ\n");
            }
            responseText.append("\n");

            // Hiển thị một vài bác sĩ nổi bật
            responseText.append("⭐ BÁC SĨ NỔI BẬT:\n");

            // Sắp xếp theo kinh nghiệm
            bacSiList.sort((a, b) -> Integer.compare(b.getNamKinhNghiem(), a.getNamKinhNghiem()));

            int count = 0;
            for (com.example.doannt118.model.BacSi bacSi : bacSiList) {
                if (count >= 3) break; // Chỉ hiển thị 3 bác sĩ nổi bật

                responseText.append("👨‍⚕️ ").append(bacSi.getHoTen()).append("\n");

                if (bacSi.getChuyenKhoa() != null) {
                    responseText.append("🩺 ").append(bacSi.getChuyenKhoa()).append("\n");
                }

                if (bacSi.getNamKinhNghiem() > 0) {
                    responseText.append("📚 ").append(bacSi.getNamKinhNghiem()).append(" năm kinh nghiệm\n");
                }

                if (bacSi.getGioiThieu() != null && !bacSi.getGioiThieu().isEmpty()) {
                    String gioiThieu = bacSi.getGioiThieu();
                    if (gioiThieu.length() > 50) {
                        gioiThieu = gioiThieu.substring(0, 50) + "...";
                    }
                    responseText.append("📝 ").append(gioiThieu).append("\n");
                }

                responseText.append("\n");
                count++;
            }

            responseText.append("💡 *Để xem danh sách đầy đủ và đặt lịch, vui lòng sử dụng ứng dụng chính.*");

            ChatResponse response = new ChatResponse(
                responseText.toString(),
                ChatResponse.ResponseType.QUICK_REPLY
            );

            List<String> quickReplies = new ArrayList<>();

            // Thêm các chuyên khoa phổ biến
            List<String> topChuyenKhoa = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : chuyenKhoaCount.entrySet()) {
                topChuyenKhoa.add(entry.getKey());
                if (topChuyenKhoa.size() >= 3) break;
            }

            for (String chuyenKhoa : topChuyenKhoa) {
                quickReplies.add("🩺 " + chuyenKhoa);
            }

            quickReplies.add("📅 Đặt lịch khám");

            response.setQuickReplies(quickReplies);
            callback.onResponse(response);

        }, e -> {
            Log.e("ChatbotEngine", "Error loading doctors: ", e);

            ChatResponse response = new ChatResponse(
                "❌ Lỗi tải dữ liệu\n\n" +
                "Không thể tải danh sách bác sĩ. Vui lòng thử lại sau.",
                ChatResponse.ResponseType.QUICK_REPLY
            );

            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("🔄 Thử lại");
            quickReplies.add("📞 Liên hệ hỗ trợ");

            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
        });
    }

    // ============================================
    // DOCTOR HANDLERS - TÍCH HỢP THỰC TẾ VỚI FIRESTORE
    // ============================================

    private void handleDoctorSchedule(String userMessage, ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy lịch làm việc thực tế từ Firestore
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        Date startOfDay = today.getTime();

        today.set(Calendar.HOUR_OF_DAY, 23);
        today.set(Calendar.MINUTE, 59);
        today.set(Calendar.SECOND, 59);
        today.set(Calendar.MILLISECOND, 999);
        Date endOfDay = today.getTime();

        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startOfDay, endOfDay,
            querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "📅 LỊCH LÀM VIỆC HÔM NAY\n\n" +
                        "🏠 Hôm nay bạn không có lịch làm việc.\n\n" +
                        "Bạn có muốn:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("📅 Xem lịch tuần này");
                    quickReplies.add("⚙️ Quản lý lịch làm việc");
                    quickReplies.add("👥 Bệnh nhân hôm nay");
                    quickReplies.add("📊 Thống kê");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                StringBuilder responseText = new StringBuilder();
                responseText.append("📅 LỊCH LÀM VIỆC HÔM NAY\n\n");

                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                responseText.append("📆 Ngày: ").append(dateFormat.format(new Date())).append("\n\n");

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.LichLamViec lichLamViec =
                        doc.toObject(com.example.doannt118.model.LichLamViec.class);
                    if (lichLamViec != null) {
                        responseText.append("⏰ Ca làm việc: ").append(lichLamViec.getCaLamViec()).append("\n");
                        if (lichLamViec.getLoaiHinh() != null) {
                            responseText.append("🏥 Loại hình: ").append(lichLamViec.getLoaiHinh()).append("\n");
                        }
                        if (lichLamViec.getGhiChu() != null && !lichLamViec.getGhiChu().isEmpty()) {
                            responseText.append("📝 Ghi chú: ").append(lichLamViec.getGhiChu()).append("\n");
                        }
                        responseText.append("\n");
                    }
                }

                responseText.append("💡 *Để quản lý lịch chi tiết, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("📊 Thống kê");
                quickReplies.add("⚙️ Quản lý lịch làm việc");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading doctor schedule: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải lịch làm việc\n\n" +
                    "Không thể tải lịch làm việc. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void handleViewTodayPatients(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách bệnh nhân hôm nay từ Firestore (giống logic trong MainBacSiActivity)
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        calStart.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calStart.getTime();

        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        calEnd.set(Calendar.MILLISECOND, 999);
        Date endOfDay = calEnd.getTime();

        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.LichKham> lichKhamList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    try {
                        String trangThai = doc.getString("trangThai");
                        if (!"XAC_NHAN".equals(trangThai)) continue;

                        // Kiểm tra ngày khám
                        com.google.firebase.Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                        if (ngayKhamTs == null) continue;

                        Date ngayKham = ngayKhamTs.toDate();
                        if (ngayKham.before(startOfDay) || ngayKham.after(endOfDay)) continue;

                        com.example.doannt118.model.LichKham lichKham =
                            doc.toObject(com.example.doannt118.model.LichKham.class);
                        if (lichKham != null) {
                            lichKhamList.add(lichKham);
                        }
                    } catch (Exception e) {
                        Log.e("ChatbotEngine", "Error parsing LichKham", e);
                    }
                }

                if (lichKhamList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "👥 BỆNH NHÂN HÔM NAY\n\n" +
                        "🏠 Hôm nay bạn không có bệnh nhân nào.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("📅 Xem lịch làm việc");
                    quickReplies.add("✅ Xác nhận lịch khám");
                    quickReplies.add("📊 Thống kê");
                    quickReplies.add("⚙️ Quản lý lịch");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                // Sắp xếp theo giờ khám
                lichKhamList.sort((a, b) -> {
                    String gioA = a.getGioKham() != null ? a.getGioKham() : "";
                    String gioB = b.getGioKham() != null ? b.getGioKham() : "";
                    return gioA.compareTo(gioB);
                });

                StringBuilder responseText = new StringBuilder();
                responseText.append("👥 BỆNH NHÂN HÔM NAY\n\n");
                responseText.append("📊 Tổng cộng: ").append(lichKhamList.size()).append(" bệnh nhân\n\n");

                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                responseText.append("📅 Ngày: ").append(dateFormat.format(new Date())).append("\n\n");

                int count = 0;
                for (com.example.doannt118.model.LichKham lichKham : lichKhamList) {
                    if (count >= 5) break; // Chỉ hiển thị 5 bệnh nhân đầu tiên

                    responseText.append("⏰ ").append(lichKham.getGioKham()).append("\n");
                    responseText.append("🆔 Mã BN: ").append(lichKham.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("🆔 Mã LK: ").append(lichKham.getMaLichKham().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("✅ Trạng thái: Đã xác nhận\n\n");

                    count++;
                }

                if (lichKhamList.size() > 5) {
                    responseText.append("📋 Và ").append(lichKhamList.size() - 5).append(" bệnh nhân khác...\n\n");
                }

                responseText.append("💡 *Để xem chi tiết và quản lý, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("📅 Lịch làm việc");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("📋 Quản lý bệnh án");
                quickReplies.add("💊 Quản lý đơn thuốc");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading today patients: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải danh sách bệnh nhân\n\n" +
                    "Không thể tải danh sách bệnh nhân hôm nay. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("📅 Lịch làm việc");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void handleDoctorStatistics(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Tính thống kê từ Firestore
        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                int totalAppointments = 0;
                int confirmedAppointments = 0;
                int completedAppointments = 0;
                int cancelledAppointments = 0;
                int todayAppointments = 0;

                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                Date startOfDay = today.getTime();

                today.set(Calendar.HOUR_OF_DAY, 23);
                today.set(Calendar.MINUTE, 59);
                today.set(Calendar.SECOND, 59);
                today.set(Calendar.MILLISECOND, 999);
                Date endOfDay = today.getTime();

                for (var doc : querySnapshot.getDocuments()) {
                    totalAppointments++;

                    String trangThai = doc.getString("trangThai");
                    if ("XAC_NHAN".equals(trangThai)) {
                        confirmedAppointments++;
                    } else if ("HOAN_THANH".equals(trangThai)) {
                        completedAppointments++;
                    } else if ("HUY".equals(trangThai)) {
                        cancelledAppointments++;
                    }

                    // Kiểm tra lịch hôm nay
                    com.google.firebase.Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                    if (ngayKhamTs != null) {
                        Date ngayKham = ngayKhamTs.toDate();
                        if (ngayKham.getTime() >= startOfDay.getTime() &&
                            ngayKham.getTime() <= endOfDay.getTime() &&
                            "XAC_NHAN".equals(trangThai)) {
                            todayAppointments++;
                        }
                    }
                }

                StringBuilder responseText = new StringBuilder();
                responseText.append("📊 THỐNG KÊ CỦA BẠN\n\n");

                responseText.append("📈 TỔNG QUAN:\n");
                responseText.append("• 📋 Tổng lịch khám: ").append(totalAppointments).append("\n");
                responseText.append("• ✅ Đã xác nhận: ").append(confirmedAppointments).append("\n");
                responseText.append("• 🎉 Hoàn thành: ").append(completedAppointments).append("\n");
                responseText.append("• ❌ Đã hủy: ").append(cancelledAppointments).append("\n\n");

                responseText.append("📅 HÔM NAY:\n");
                responseText.append("• 👥 Bệnh nhân hôm nay: ").append(todayAppointments).append("\n\n");

                // Tính tỷ lệ hoàn thành
                if (totalAppointments > 0) {
                    double completionRate = (double) completedAppointments / totalAppointments * 100;
                    responseText.append("📈 Tỷ lệ hoàn thành: ").append(String.format("%.1f%%", completionRate)).append("\n\n");
                }

                responseText.append("💡 *Để xem báo cáo chi tiết, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📅 Lịch làm việc");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("📋 Quản lý bệnh án");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading statistics: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải thống kê\n\n" +
                    "Không thể tải thống kê. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    // ============================================
    // ADDITIONAL DOCTOR HANDLERS - DỰA TRÊN MainBacSiActivity
    // ============================================

    private void handleManageMedicalRecords(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách bệnh án do bác sĩ này tạo từ Firestore
        repo.getByField("BenhAn", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.BenhAn> benhAnList = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.BenhAn benhAn =
                        doc.toObject(com.example.doannt118.model.BenhAn.class);
                    if (benhAn != null) {
                        benhAnList.add(benhAn);
                    }
                }

                StringBuilder responseText = new StringBuilder();
                responseText.append("📋 QUẢN LÝ BỆNH ÁN\n\n");

                if (benhAnList.isEmpty()) {
                    responseText.append("📋 Bạn chưa tạo bệnh án nào.\n\n");
                } else {
                    responseText.append("📊 THỐNG KÊ:\n");
                    responseText.append("• 📋 Tổng số bệnh án: ").append(benhAnList.size()).append("\n\n");

                    // Sắp xếp theo ngày khám (mới nhất trước)
                    benhAnList.sort((a, b) -> {
                        com.google.firebase.Timestamp tsA = a.getNgayKhamAsTimestamp();
                        com.google.firebase.Timestamp tsB = b.getNgayKhamAsTimestamp();
                        if (tsA == null) return 1;
                        if (tsB == null) return -1;
                        return tsB.compareTo(tsA);
                    });

                    responseText.append("📋 BỆNH ÁN GẦN NHẤT:\n\n");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    int count = 0;
                    for (com.example.doannt118.model.BenhAn benhAn : benhAnList) {
                        if (count >= 3) break;

                        responseText.append("🏥 Bệnh án ").append(benhAn.getMaBenhAn().substring(0, 6).toUpperCase()).append("\n");
                        responseText.append("🆔 BN: ").append(benhAn.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");

                        com.google.firebase.Timestamp ngayKham = benhAn.getNgayKhamAsTimestamp();
                        if (ngayKham != null) {
                            responseText.append("📅 Ngày khám: ").append(dateFormat.format(ngayKham.toDate())).append("\n");
                        }

                        if (benhAn.getChanDoan() != null && !benhAn.getChanDoan().isEmpty()) {
                            String chanDoan = benhAn.getChanDoan();
                            if (chanDoan.length() > 40) {
                                chanDoan = chanDoan.substring(0, 40) + "...";
                            }
                            responseText.append("🩺 Chẩn đoán: ").append(chanDoan).append("\n");
                        }

                        if (benhAn.getLoaiKham() != null && !benhAn.getLoaiKham().isEmpty()) {
                            responseText.append("🔍 Loại khám: ").append(benhAn.getLoaiKham()).append("\n");
                        }

                        responseText.append("\n");
                        count++;
                    }

                    if (benhAnList.size() > 3) {
                        responseText.append("📋 Và ").append(benhAnList.size() - 3).append(" bệnh án khác...\n\n");
                    }
                }

                responseText.append("💡 *Để tạo hoặc chỉnh sửa bệnh án, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("💊 Quản lý đơn thuốc");
                quickReplies.add("📊 Thống kê");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading medical records for doctor: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải bệnh án\n\n" +
                    "Không thể tải danh sách bệnh án. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void handleConfirmAppointments(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách lịch khám chờ xác nhận
        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.LichKham> pendingAppointments = new ArrayList<>();

                for (var doc : querySnapshot.getDocuments()) {
                    String trangThai = doc.getString("trangThai");
                    if ("CHO".equals(trangThai)) { // Chờ xác nhận
                        com.example.doannt118.model.LichKham lichKham =
                            doc.toObject(com.example.doannt118.model.LichKham.class);
                        if (lichKham != null) {
                            pendingAppointments.add(lichKham);
                        }
                    }
                }

                if (pendingAppointments.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "✅ XÁC NHẬN LỊCH KHÁM\n\n" +
                        "🎉 Tuyệt vời! Hiện tại không có lịch khám nào cần xác nhận.\n\n" +
                        "Bạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );

                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("👥 Bệnh nhân hôm nay");
                    quickReplies.add("📅 Lịch làm việc");
                    quickReplies.add("📊 Thống kê");
                    quickReplies.add("📋 Quản lý bệnh án");

                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }

                StringBuilder responseText = new StringBuilder();
                responseText.append("✅ XÁC NHẬN LỊCH KHÁM\n\n");
                responseText.append("📋 Có ").append(pendingAppointments.size()).append(" lịch khám cần xác nhận:\n\n");

                // Sắp xếp theo ngày khám
                pendingAppointments.sort((a, b) -> {
                    if (a.getNgayKham() == null) return 1;
                    if (b.getNgayKham() == null) return -1;
                    return a.getNgayKham().compareTo(b.getNgayKham());
                });

                java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());

                int count = 0;
                for (com.example.doannt118.model.LichKham lichKham : pendingAppointments) {
                    if (count >= 3) break; // Chỉ hiển thị 3 lịch đầu tiên

                    responseText.append("⏰ ").append(lichKham.getGioKham()).append("\n");
                    if (lichKham.getNgayKham() != null) {
                        responseText.append("📅 ").append(dateFormat.format(lichKham.getNgayKham().toDate())).append("\n");
                    }
                    responseText.append("🆔 Mã BN: ").append(lichKham.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("🆔 Mã LK: ").append(lichKham.getMaLichKham().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("⏳ Trạng thái: Chờ xác nhận\n\n");

                    count++;
                }

                if (pendingAppointments.size() > 3) {
                    responseText.append("📋 Và ").append(pendingAppointments.size() - 3).append(" lịch khác...\n\n");
                }

                responseText.append("💡 *Để xác nhận chi tiết, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📅 Lịch làm việc");
                quickReplies.add("📋 Quản lý bệnh án");
                quickReplies.add("📊 Thống kê");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading pending appointments: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải danh sách lịch khám\n\n" +
                    "Không thể tải danh sách lịch khám cần xác nhận. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void handleManagePrescriptions(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }

        // Lấy danh sách đơn thuốc do bác sĩ này kê từ Firestore
        repo.getByField("DonThuoc", "maBacSi", maBacSi,
            querySnapshot -> {
                List<com.example.doannt118.model.DonThuoc> donThuocList = new ArrayList<>();
                int dangDung = 0;
                int daHet = 0;

                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.DonThuoc donThuoc =
                        doc.toObject(com.example.doannt118.model.DonThuoc.class);
                    if (donThuoc != null) {
                        donThuocList.add(donThuoc);
                        if ("DANG_DUNG".equals(donThuoc.getTrangThai())) {
                            dangDung++;
                        } else if ("DA_HET".equals(donThuoc.getTrangThai())) {
                            daHet++;
                        }
                    }
                }

                StringBuilder responseText = new StringBuilder();
                responseText.append("💊 QUẢN LÝ ĐƠN THUỐC\n\n");

                if (donThuocList.isEmpty()) {
                    responseText.append("📋 Bạn chưa kê đơn thuốc nào.\n\n");
                } else {
                    responseText.append("📊 THỐNG KÊ:\n");
                    responseText.append("• 📋 Tổng số đơn: ").append(donThuocList.size()).append("\n");
                    responseText.append("• 💊 Đang sử dụng: ").append(dangDung).append("\n");
                    responseText.append("• ✅ Đã hoàn thành: ").append(daHet).append("\n\n");

                    // Sắp xếp theo ngày lập (mới nhất trước)
                    donThuocList.sort((a, b) -> {
                        if (a.getNgayLap() == null) return 1;
                        if (b.getNgayLap() == null) return -1;
                        return b.getNgayLap().compareTo(a.getNgayLap());
                    });

                    responseText.append("📋 ĐƠN THUỐC GẦN NHẤT:\n\n");

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    int count = 0;
                    for (com.example.doannt118.model.DonThuoc donThuoc : donThuocList) {
                        if (count >= 3) break;

                        String statusIcon = getPrescriptionStatusIcon(donThuoc.getTrangThai());
                        responseText.append(statusIcon).append(" Đơn ").append(donThuoc.getMaDonThuoc().substring(0, 6).toUpperCase()).append("\n");
                        responseText.append("🆔 BN: ").append(donThuoc.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");

                        if (donThuoc.getNgayLap() != null) {
                            responseText.append("📅 Ngày kê: ").append(dateFormat.format(donThuoc.getNgayLap())).append("\n");
                        }

                        if (donThuoc.getSoNgayUong() > 0) {
                            responseText.append("⏰ Thời gian: ").append(donThuoc.getSoNgayUong()).append(" ngày\n");
                        }

                        responseText.append("\n");
                        count++;
                    }

                    if (donThuocList.size() > 3) {
                        responseText.append("📋 Và ").append(donThuocList.size() - 3).append(" đơn thuốc khác...\n\n");
                    }
                }

                responseText.append("💡 *Để kê đơn mới hoặc chỉnh sửa, vui lòng sử dụng ứng dụng chính.*");

                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📋 Quản lý bệnh án");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("📊 Thống kê");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);

            }, e -> {
                Log.e("ChatbotEngine", "Error loading prescriptions for doctor: ", e);

                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải đơn thuốc\n\n" +
                    "Không thể tải danh sách đơn thuốc. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );

                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");

                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }

    private void handleEnterPatientCode(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "🔢 NHẬP MÃ KHÁM\n\n" +
            "Để nhập mã khám và tra cứu thông tin bệnh nhân, vui lòng sử dụng ứng dụng chính.\n\n" +
            "🔍 Chức năng bao gồm:\n" +
            "• 🔢 Nhập mã khám bệnh nhân\n" +
            "• 📋 Xem thông tin bệnh nhân\n" +
            "• 📝 Ghi nhận kết quả khám\n" +
            "• 💊 Kê đơn thuốc\n" +
            "• 📊 Cập nhật bệnh án\n\n" +
            "Bạn cần hỗ trợ gì khác?",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("👥 Bệnh nhân hôm nay");
        quickReplies.add("📋 Quản lý bệnh án");
        quickReplies.add("💊 Quản lý đơn thuốc");
        quickReplies.add("📊 Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    private void handleAIAssistant(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        // Lấy thông tin bác sĩ để cá nhân hóa
        repo.getCollection("BacSi").document(maBacSi).get()
            .addOnSuccessListener(documentSnapshot -> {
                String tenBacSi = "Bác sĩ";
                String chuyenKhoa = "";
                
                if (documentSnapshot.exists()) {
                    com.example.doannt118.model.BacSi bacSi = 
                        documentSnapshot.toObject(com.example.doannt118.model.BacSi.class);
                    if (bacSi != null) {
                        tenBacSi = "BS. " + bacSi.getHoTen();
                        chuyenKhoa = bacSi.getChuyenKhoa() != null ? bacSi.getChuyenKhoa() : "";
                    }
                }
                
                StringBuilder responseText = new StringBuilder();
                responseText.append("🤖 AI ASSISTANT\n\n");
                responseText.append("Xin chào ").append(tenBacSi).append("! 👋\n\n");
                
                if (!chuyenKhoa.isEmpty()) {
                    responseText.append("Chuyên khoa: ").append(chuyenKhoa).append("\n\n");
                }
                
                responseText.append("Tôi có thể hỗ trợ bạn với:\n\n");
                responseText.append("🩺 Hỗ trợ chẩn đoán:\n");
                responseText.append("• Phân tích triệu chứng\n");
                responseText.append("• Gợi ý chẩn đoán phân biệt\n");
                responseText.append("• Tư vấn xét nghiệm cần thiết\n\n");
                
                responseText.append("💊 Hỗ trợ điều trị:\n");
                responseText.append("• Gợi ý phác đồ điều trị\n");
                responseText.append("• Kiểm tra tương tác thuốc\n");
                responseText.append("• Tính liều dùng thuốc\n\n");
                
                responseText.append("📚 Tra cứu y khoa:\n");
                responseText.append("• Thông tin bệnh lý\n");
                responseText.append("• Hướng dẫn điều trị\n\n");
                
                responseText.append("💡 *Hãy hỏi tôi bất cứ điều gì về y khoa!*");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🩺 Hỗ trợ chẩn đoán");
                quickReplies.add("💊 Tư vấn điều trị");
                quickReplies.add("📚 Tra cứu y khoa");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            })
            .addOnFailureListener(e -> {
                Log.e("ChatbotEngine", "Error loading doctor info: ", e);
                
                // Fallback nếu không lấy được thông tin bác sĩ
                ChatResponse response = new ChatResponse(
                    "🤖 AI ASSISTANT\n\n" +
                    "Tôi có thể hỗ trợ bạn với:\n\n" +
                    "🩺 Hỗ trợ chẩn đoán - Phân tích triệu chứng, gợi ý chẩn đoán\n" +
                    "💊 Hỗ trợ điều trị - Phác đồ, tương tác thuốc, liều dùng\n" +
                    "📚 Tra cứu y khoa - Thông tin bệnh lý, hướng dẫn điều trị\n\n" +
                    "💡 *Hãy hỏi tôi bất cứ điều gì về y khoa!*",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🩺 Hỗ trợ chẩn đoán");
                quickReplies.add("💊 Tư vấn điều trị");
                quickReplies.add("📚 Tra cứu y khoa");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    private void handleChatWithPatients(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "❌ Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        // Lấy danh sách tin nhắn của bác sĩ từ Firestore
        repo.getByField("TinNhanBacSi", "maBacSi", maBacSi,
            querySnapshot -> {
                // Đếm số cuộc trò chuyện và tin nhắn chưa đọc
                Map<String, Integer> conversationCount = new HashMap<>();
                int unreadCount = 0;
                
                for (var doc : querySnapshot.getDocuments()) {
                    String maBenhNhanChat = doc.getString("maBenhNhan");
                    Boolean daDoc = doc.getBoolean("daDoc");
                    String nguoiGui = doc.getString("nguoiGui");
                    
                    if (maBenhNhanChat != null) {
                        conversationCount.put(maBenhNhanChat, 
                            conversationCount.getOrDefault(maBenhNhanChat, 0) + 1);
                    }
                    
                    // Đếm tin nhắn chưa đọc (từ bệnh nhân gửi)
                    if (daDoc != null && !daDoc && "benhnhan".equals(nguoiGui)) {
                        unreadCount++;
                    }
                }
                
                StringBuilder responseText = new StringBuilder();
                responseText.append("💬 CHAT VỚI BỆNH NHÂN\n\n");
                
                if (conversationCount.isEmpty()) {
                    responseText.append("📭 Bạn chưa có cuộc trò chuyện nào với bệnh nhân.\n\n");
                } else {
                    responseText.append("📊 THỐNG KÊ:\n");
                    responseText.append("• 💬 Số cuộc trò chuyện: ").append(conversationCount.size()).append("\n");
                    responseText.append("• 📩 Tin nhắn chưa đọc: ").append(unreadCount).append("\n\n");
                    
                    if (unreadCount > 0) {
                        responseText.append("🔔 Bạn có ").append(unreadCount).append(" tin nhắn mới cần trả lời!\n\n");
                    }
                }
                
                responseText.append("💡 *Để chat trực tiếp với bệnh nhân, vui lòng sử dụng ứng dụng chính.*");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📋 Quản lý bệnh án");
                quickReplies.add("✅ Xác nhận lịch khám");
                quickReplies.add("📊 Thống kê");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            }, e -> {
                Log.e("ChatbotEngine", "Error loading chat messages: ", e);
                
                ChatResponse response = new ChatResponse(
                    "❌ Lỗi tải tin nhắn\n\n" +
                    "Không thể tải danh sách tin nhắn. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("👥 Bệnh nhân hôm nay");
                quickReplies.add("📞 Liên hệ hỗ trợ");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    // ============================================
    // CONVERSATION FLOW HANDLERS
    // ============================================
    
    private void handleRoleSelectionInput(String userMessage, ChatCallback callback) {
        String message = userMessage.toLowerCase().trim();
        
        if (message.contains("bệnh nhân") || message.contains("benh nhan") || message.contains("patient")) {
            conversationContext.setData("userType", "benhnhan");
            conversationContext.setState(ConversationContext.ConversationState.IDLE);
            
            ChatResponse response = new ChatResponse(
                "👋 Xin chào! Tôi là trợ lý ảo dành cho Bệnh nhân.\n\n" +
                "🩺 Tôi có thể giúp bạn:\n" +
                "• 📅 Đặt lịch khám bệnh\n" +
                "• 👀 Xem lịch khám của bạn\n" +
                "• ❌ Hủy lịch khám\n" +
                "• 👨‍⚕️ Tìm bác sĩ theo chuyên khoa\n" +
                "• 💊 Xem đơn thuốc\n" +
                "• 🏥 Thông tin bệnh viện\n\n" +
                "Bạn cần hỗ trợ gì hôm nay?",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("📅 Đặt lịch khám");
            quickReplies.add("👀 Xem lịch của tôi");
            quickReplies.add("👨‍⚕️ Tìm bác sĩ");
            quickReplies.add("🏥 Thông tin bệnh viện");
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
            
        } else if (message.contains("bác sĩ") || message.contains("bac si") || message.contains("doctor")) {
            conversationContext.setData("userType", "bacsi");
            conversationContext.setState(ConversationContext.ConversationState.IDLE);
            
            ChatResponse response = new ChatResponse(
                "👨‍⚕️ Xin chào Bác sĩ! Tôi là trợ lý ảo hỗ trợ công việc của bạn.\n\n" +
                "⚕️ Tôi có thể giúp bạn:\n" +
                "• 📅 Xem lịch làm việc hôm nay\n" +
                "• 👥 Danh sách bệnh nhân hôm nay\n" +
                "• ✅ Xác nhận lịch khám\n" +
                "• 📋 Quản lý bệnh án\n" +
                "• 💊 Quản lý đơn thuốc\n" +
                "• 🔢 Nhập mã khám\n" +
                "• 📊 Thống kê và báo cáo\n" +
                "• 🤖 AI Assistant\n" +
                "• 💬 Chat với bệnh nhân\n\n" +
                "Bạn cần hỗ trợ gì hôm nay?",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("📅 Lịch làm việc hôm nay");
            quickReplies.add("👥 Bệnh nhân hôm nay");
            quickReplies.add("✅ Xác nhận lịch khám");
            quickReplies.add("📊 Xem thống kê");
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
            
        } else {
            ChatResponse response = new ChatResponse(
                "🤔 Xin lỗi, tôi chưa hiểu. Vui lòng chọn một trong hai tùy chọn:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("🏥 Tôi là Bệnh nhân");
            quickReplies.add("👨‍⚕️ Tôi là Bác sĩ");
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
        }
    }
    
    private void handleSpecialtySelection(String userMessage, ChatCallback callback) {
        @SuppressWarnings("unchecked")
        List<String> chuyenKhoaList = (List<String>) conversationContext.getData("chuyenKhoaList");
        
        if (chuyenKhoaList == null) {
            // Fallback nếu không có data
            handleBookingIntent(callback);
            return;
        }
        
        String selectedChuyenKhoa = null;
        
        // Kiểm tra xem user có chọn "Xem tất cả chuyên khoa" không
        if (userMessage.contains("Xem tất cả") || userMessage.contains("xem thêm")) {
            ChatResponse response = new ChatResponse(
                " TẤT CẢ CHUYÊN KHOA\n\n" +
                "Danh sách đầy đủ các chuyên khoa:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            for (String chuyenKhoa : chuyenKhoaList) {
                quickReplies.add(chuyenKhoa);
                if (quickReplies.size() >= 6) break; // Giới hạn 6 để không quá dài
            }
            
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
            return;
        }
        
        // Tìm chuyên khoa được chọn
        for (String chuyenKhoa : chuyenKhoaList) {
            if (userMessage.toLowerCase().contains(chuyenKhoa.toLowerCase()) ||
                chuyenKhoa.toLowerCase().contains(userMessage.toLowerCase())) {
                selectedChuyenKhoa = chuyenKhoa;
                break;
            }
        }
        
        if (selectedChuyenKhoa == null) {
            ChatResponse response = new ChatResponse(
                "🤔 Tôi không tìm thấy chuyên khoa \"" + userMessage + "\".\n\n" +
                "Vui lòng chọn một trong các chuyên khoa sau:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            for (int i = 0; i < Math.min(chuyenKhoaList.size(), 4); i++) {
                quickReplies.add(chuyenKhoaList.get(i));
            }
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
            return;
        }
        
        // Lưu chuyên khoa đã chọn và chuyển sang chọn ngày
        conversationContext.setData("selectedChuyenKhoa", selectedChuyenKhoa);
        conversationContext.setState(ConversationContext.ConversationState.WAITING_DATE);
        
        ChatResponse response = new ChatResponse(
            "✅ Đã chọn chuyên khoa: " + selectedChuyenKhoa + "\n\n" +
            "📅 Bạn muốn khám vào ngày nào?\n\n" +
            "💡 *Lưu ý: Chỉ hiển thị các ngày có bác sĩ làm việc*",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        // Tạo danh sách ngày gợi ý (7 ngày tới)
        List<String> quickReplies = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        
        // Hôm nay
        quickReplies.add("Hôm nay (" + dateFormat.format(cal.getTime()) + ")");
        
        // 6 ngày tiếp theo
        for (int i = 1; i <= 6; i++) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            String dayName = "";
            switch (cal.get(Calendar.DAY_OF_WEEK)) {
                case Calendar.MONDAY: dayName = "T2"; break;
                case Calendar.TUESDAY: dayName = "T3"; break;
                case Calendar.WEDNESDAY: dayName = "T4"; break;
                case Calendar.THURSDAY: dayName = "T5"; break;
                case Calendar.FRIDAY: dayName = "T6"; break;
                case Calendar.SATURDAY: dayName = "T7"; break;
                case Calendar.SUNDAY: dayName = "CN"; break;
            }
            quickReplies.add(dayName + " (" + dateFormat.format(cal.getTime()) + ")");
        }
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    private void handleDateInput(String userMessage, ChatCallback callback) {
        // Parse ngày từ user input
        Date selectedDate = parseDateFromMessage(userMessage);
        
        if (selectedDate == null) {
            ChatResponse response = new ChatResponse(
                "🤔 Tôi không hiểu ngày \"" + userMessage + "\".\n\n" +
                "Vui lòng chọn một trong các ngày sau:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            // Tạo lại danh sách ngày
            List<String> quickReplies = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Calendar cal = Calendar.getInstance();
            
            quickReplies.add("Hôm nay (" + dateFormat.format(cal.getTime()) + ")");
            for (int i = 1; i <= 3; i++) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                quickReplies.add(dateFormat.format(cal.getTime()));
            }
            
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
            return;
        }
        
        // Lưu ngày đã chọn
        conversationContext.setData("selectedDate", selectedDate);
        
        // Lấy chuyên khoa đã chọn
        String selectedChuyenKhoa = (String) conversationContext.getData("selectedChuyenKhoa");
        
        // Tìm bác sĩ có lịch làm việc trong ngày đã chọn
        findAvailableDoctors(selectedDate, selectedChuyenKhoa, callback);
    }
    
    private Date parseDateFromMessage(String message) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            // Kiểm tra các pattern khác nhau
            if (message.toLowerCase().contains("hôm nay")) {
                return new Date();
            } else if (message.toLowerCase().contains("ngày mai")) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 1);
                return cal.getTime();
            } else {
                // Tìm pattern dd/MM/yyyy trong message
                String[] parts = message.split("\\s+");
                for (String part : parts) {
                    if (part.matches("\\d{2}/\\d{2}/\\d{4}")) {
                        return dateFormat.parse(part);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("ChatbotEngine", "Error parsing date: " + message, e);
        }
        return null;
    }
    
    private void findAvailableDoctors(Date selectedDate, String chuyenKhoa, ChatCallback callback) {
        // Tạo date range cho ngày được chọn
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(selectedDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startDate = startCal.getTime();

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(selectedDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endDate = endCal.getTime();
        
        // Tìm lịch làm việc trong ngày đã chọn
        repo.getCollection("LichLamViec")
                .whereGreaterThanOrEqualTo("ngayLamViec", new com.google.firebase.Timestamp(startDate))
                .whereLessThanOrEqualTo("ngayLamViec", new com.google.firebase.Timestamp(endDate))
                .get()
                .addOnSuccessListener(querySnapshot -> {
                List<String> availableDoctorIds = new ArrayList<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.LichLamViec lichLamViec = 
                        doc.toObject(com.example.doannt118.model.LichLamViec.class);
                    if (lichLamViec != null && lichLamViec.getMaBacSi() != null) {
                        availableDoctorIds.add(lichLamViec.getMaBacSi());
                    }
                }
                
                if (availableDoctorIds.isEmpty()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    ChatResponse response = new ChatResponse(
                        "😔 Không có bác sĩ làm việc\n\n" +
                        "Ngày " + dateFormat.format(selectedDate) + " không có bác sĩ " + 
                        chuyenKhoa + " làm việc.\n\n" +
                        "Vui lòng chọn ngày khác:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    // Gợi ý các ngày khác
                    List<String> quickReplies = new ArrayList<>();
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    
                    for (int i = 0; i < 3; i++) {
                        quickReplies.add(dateFormat.format(cal.getTime()));
                        cal.add(Calendar.DAY_OF_MONTH, 1);
                    }
                    
                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }
                
                // Lấy thông tin bác sĩ theo chuyên khoa
                repo.getByField("BacSi", "chuyenKhoa", chuyenKhoa,
                    bacSiSnapshot -> {
                        List<com.example.doannt118.model.BacSi> availableDoctors = new ArrayList<>();
                        
                        for (var bacSiDoc : bacSiSnapshot.getDocuments()) {
                            com.example.doannt118.model.BacSi bacSi = 
                                bacSiDoc.toObject(com.example.doannt118.model.BacSi.class);
                            if (bacSi != null && 
                                availableDoctorIds.contains(bacSi.getMaBacSi()) &&
                                "Đã xác thực".equals(bacSi.getTrangThaiXacThuc())) {
                                availableDoctors.add(bacSi);
                            }
                        }
                        
                        if (availableDoctors.isEmpty()) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                            ChatResponse response = new ChatResponse(
                                "😔 Không có bác sĩ " + chuyenKhoa + "\n\n" +
                                "Ngày " + dateFormat.format(selectedDate) + " không có bác sĩ chuyên khoa " + 
                                chuyenKhoa + " làm việc.\n\n" +
                                "Bạn có muốn:",
                                ChatResponse.ResponseType.QUICK_REPLY
                            );
                            
                            List<String> quickReplies = new ArrayList<>();
                            quickReplies.add("📅 Chọn ngày khác");
                            quickReplies.add("🩺 Chọn chuyên khoa khác");
                            quickReplies.add("📞 Liên hệ lễ tân");
                            
                            response.setQuickReplies(quickReplies);
                            callback.onResponse(response);
                            return;
                        }
                        
                        // Có bác sĩ available - chuyển sang chọn bác sĩ
                        conversationContext.setState(ConversationContext.ConversationState.WAITING_DOCTOR_SELECTION);
                        conversationContext.setData("availableDoctors", availableDoctors);
                        
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        ChatResponse response = new ChatResponse(
                            "✅ Ngày " + dateFormat.format(selectedDate) + "\n\n" +
                            "👨‍⚕️ Có " + availableDoctors.size() + " bác sĩ " + chuyenKhoa + " làm việc.\n\n" +
                            "Vui lòng chọn bác sĩ:",
                            ChatResponse.ResponseType.QUICK_REPLY
                        );
                        
                        List<String> quickReplies = new ArrayList<>();
                        for (com.example.doannt118.model.BacSi bacSi : availableDoctors) {
                            String doctorInfo = "BS. " + bacSi.getHoTen();
                            if (bacSi.getNamKinhNghiem() > 0) {
                                doctorInfo += " (" + bacSi.getNamKinhNghiem() + " năm KN)";
                            }
                            quickReplies.add(doctorInfo);
                            
                            if (quickReplies.size() >= 4) break; // Giới hạn 4 bác sĩ
                        }
                        
                        if (availableDoctors.size() > 4) {
                            quickReplies.add("👨‍⚕️ Xem tất cả bác sĩ");
                        }
                        
                        response.setQuickReplies(quickReplies);
                        callback.onResponse(response);
                        
                    }, e -> {
                        ChatResponse response = new ChatResponse(
                            "❌ Lỗi khi tìm bác sĩ. Vui lòng thử lại!",
                            ChatResponse.ResponseType.TEXT
                        );
                        callback.onResponse(response);
                    });
                })
                .addOnFailureListener(e -> {
                    ChatResponse response = new ChatResponse(
                        "❌ Lỗi khi kiểm tra lịch làm việc. Vui lòng thử lại!",
                        ChatResponse.ResponseType.TEXT
                    );
                    callback.onResponse(response);
                });
    }
    
    private void handleDoctorSelection(String userMessage, ChatCallback callback) {
        @SuppressWarnings("unchecked")
        List<com.example.doannt118.model.BacSi> availableDoctors = 
            (List<com.example.doannt118.model.BacSi>) conversationContext.getData("availableDoctors");
        
        if (availableDoctors == null) {
            // Fallback
            conversationContext.setState(ConversationContext.ConversationState.WAITING_DATE);
            ChatResponse response = new ChatResponse(
                "❌ Lỗi dữ liệu. Vui lòng chọn lại ngày khám.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        // Kiểm tra xem user có chọn "Xem tất cả bác sĩ" không
        if (userMessage.contains("Xem tất cả") || userMessage.contains("xem tất cả")) {
            ChatResponse response = new ChatResponse(
                "👨‍⚕️ TẤT CẢ BÁC SĨ\n\n" +
                "Danh sách đầy đủ:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            for (com.example.doannt118.model.BacSi bacSi : availableDoctors) {
                String doctorInfo = "BS. " + bacSi.getHoTen();
                if (bacSi.getNamKinhNghiem() > 0) {
                    doctorInfo += " (" + bacSi.getNamKinhNghiem() + " năm KN)";
                }
                quickReplies.add(doctorInfo);
                
                if (quickReplies.size() >= 6) break; // Giới hạn 6
            }
            
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
            return;
        }
        
        // Tìm bác sĩ được chọn
        com.example.doannt118.model.BacSi selectedDoctor = null;
        for (com.example.doannt118.model.BacSi bacSi : availableDoctors) {
            if (userMessage.toLowerCase().contains(bacSi.getHoTen().toLowerCase()) ||
                bacSi.getHoTen().toLowerCase().contains(userMessage.toLowerCase().replace("bs.", "").trim())) {
                selectedDoctor = bacSi;
                break;
            }
        }
        
        if (selectedDoctor == null) {
            ChatResponse response = new ChatResponse(
                "🤔 Tôi không tìm thấy bác sĩ \"" + userMessage + "\".\n\n" +
                "Vui lòng chọn một trong các bác sĩ sau:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            for (int i = 0; i < Math.min(availableDoctors.size(), 4); i++) {
                com.example.doannt118.model.BacSi bacSi = availableDoctors.get(i);
                quickReplies.add("BS. " + bacSi.getHoTen());
            }
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
            return;
        }
        
        // Lưu bác sĩ đã chọn và tìm khung giờ trống
        conversationContext.setData("selectedDoctor", selectedDoctor);
        
        Date selectedDate = (Date) conversationContext.getData("selectedDate");
        findAvailableTimeSlots(selectedDoctor.getMaBacSi(), selectedDate, callback);
    }
    
    private void findAvailableTimeSlots(String maBacSi, Date selectedDate, ChatCallback callback) {
        // Tạo date range cho ngày được chọn
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(selectedDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        Date startDate = startCal.getTime();

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(selectedDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        Date endDate = endCal.getTime();
        
        // Lấy lịch làm việc của bác sĩ trong ngày
        repo.getByFieldAndDateRange("LichLamViec", "maBacSi", maBacSi, "ngayLamViec", startDate, endDate,
            querySnapshot -> {
                List<String> timeSlots = new ArrayList<>();
                
                // Tạo time slots từ lịch làm việc (giống logic trong ChiTietBacSiActivity)
                for (var doc : querySnapshot.getDocuments()) {
                    com.example.doannt118.model.LichLamViec lichLamViec = 
                        doc.toObject(com.example.doannt118.model.LichLamViec.class);
                    if (lichLamViec != null) {
                        List<String> slots = generateTimeSlotsFromWorkSchedule(lichLamViec.getCaLamViec());
                        timeSlots.addAll(slots);
                    }
                }
                
                if (timeSlots.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "😔 Không có khung giờ trống\n\n" +
                        "Bác sĩ này không có khung giờ trống trong ngày đã chọn.\n\n" +
                        "Bạn có muốn:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("👨‍⚕️ Chọn bác sĩ khác");
                    quickReplies.add("📅 Chọn ngày khác");
                    quickReplies.add("🔄 Bắt đầu lại");
                    
                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }
                
                // Kiểm tra các slot đã được đặt
                checkBookedSlotsForTimeSelection(maBacSi, selectedDate, timeSlots, callback);
                
            }, e -> {
                ChatResponse response = new ChatResponse(
                    "❌ Lỗi khi tải lịch làm việc. Vui lòng thử lại!",
                    ChatResponse.ResponseType.TEXT
                );
                callback.onResponse(response);
            });
    }
    
    private List<String> generateTimeSlotsFromWorkSchedule(String caLamViec) {
        List<String> slots = new ArrayList<>();
        
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String[] caParts = caLamViec.split("-");
            if (caParts.length != 2) return slots;
            
            Date startTime = timeFormat.parse(caParts[0].trim());
            Date endTime = timeFormat.parse(caParts[1].trim());
            
            if (startTime == null || endTime == null) return slots;
            
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startTime);
            
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endTime);
            
            // Tạo các slot 30 phút
            Calendar currentSlot = Calendar.getInstance();
            currentSlot.set(Calendar.HOUR_OF_DAY, startCal.get(Calendar.HOUR_OF_DAY));
            currentSlot.set(Calendar.MINUTE, startCal.get(Calendar.MINUTE));
            
            while (currentSlot.get(Calendar.HOUR_OF_DAY) < endCal.get(Calendar.HOUR_OF_DAY) || 
                   (currentSlot.get(Calendar.HOUR_OF_DAY) == endCal.get(Calendar.HOUR_OF_DAY) && 
                    currentSlot.get(Calendar.MINUTE) < endCal.get(Calendar.MINUTE))) {
                
                Calendar slotEnd = (Calendar) currentSlot.clone();
                slotEnd.add(Calendar.MINUTE, 30);
                
                // Đảm bảo không vượt quá thời gian kết thúc
                if (slotEnd.get(Calendar.HOUR_OF_DAY) > endCal.get(Calendar.HOUR_OF_DAY) || 
                    (slotEnd.get(Calendar.HOUR_OF_DAY) == endCal.get(Calendar.HOUR_OF_DAY) && 
                     slotEnd.get(Calendar.MINUTE) > endCal.get(Calendar.MINUTE))) {
                    break;
                }
                
                String gioStart = String.format(Locale.getDefault(), "%02d:%02d", 
                    currentSlot.get(Calendar.HOUR_OF_DAY), currentSlot.get(Calendar.MINUTE));
                String gioEnd = String.format(Locale.getDefault(), "%02d:%02d", 
                    slotEnd.get(Calendar.HOUR_OF_DAY), slotEnd.get(Calendar.MINUTE));
                String khungGio = gioStart + "-" + gioEnd;
                
                slots.add(khungGio);
                currentSlot.add(Calendar.MINUTE, 30);
            }
            
        } catch (Exception e) {
            Log.e("ChatbotEngine", "Error generating time slots: " + caLamViec, e);
        }
        
        return slots;
    }
    
    private void checkBookedSlotsForTimeSelection(String maBacSi, Date selectedDate, 
                                                  List<String> allTimeSlots, ChatCallback callback) {
        
        // Tạo date range
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(selectedDate);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(selectedDate);
        endCal.set(Calendar.HOUR_OF_DAY, 23);
        endCal.set(Calendar.MINUTE, 59);
        endCal.set(Calendar.SECOND, 59);
        endCal.set(Calendar.MILLISECOND, 999);
        
        // Query lịch khám đã đặt
        repo.getCollection("LichKham")
                .whereEqualTo("maBacSi", maBacSi)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> bookedSlots = new ArrayList<>();
                    
                    for (var doc : querySnapshot.getDocuments()) {
                        com.example.doannt118.model.LichKham lichKham = 
                            doc.toObject(com.example.doannt118.model.LichKham.class);
                        if (lichKham == null) continue;
                        
                        // Kiểm tra ngày và trạng thái
                        if (lichKham.getNgayKham() != null && 
                            !"HUY".equals(lichKham.getTrangThai())) {
                            
                            Date ngayKham = lichKham.getNgayKham().toDate();
                            if (ngayKham.getTime() >= startCal.getTimeInMillis() && 
                                ngayKham.getTime() <= endCal.getTimeInMillis()) {
                                
                                if (lichKham.getGioKham() != null) {
                                    bookedSlots.add(lichKham.getGioKham().trim());
                                }
                            }
                        }
                    }
                    
                    // Loại bỏ các slot đã được đặt
                    List<String> availableSlots = new ArrayList<>();
                    for (String slot : allTimeSlots) {
                        if (!bookedSlots.contains(slot)) {
                            availableSlots.add(slot);
                        }
                    }
                    
                    if (availableSlots.isEmpty()) {
                        ChatResponse response = new ChatResponse(
                            "😔 Hết khung giờ trống\n\n" +
                            "Tất cả khung giờ của bác sĩ này đã được đặt.\n\n" +
                            "Bạn có muốn:",
                            ChatResponse.ResponseType.QUICK_REPLY
                        );
                        
                        List<String> quickReplies = new ArrayList<>();
                        quickReplies.add("👨‍⚕️ Chọn bác sĩ khác");
                        quickReplies.add("📅 Chọn ngày khác");
                        quickReplies.add("🔄 Bắt đầu lại");
                        
                        response.setQuickReplies(quickReplies);
                        callback.onResponse(response);
                        return;
                    }
                    
                    // Có slot trống - chuyển sang chọn giờ
                    conversationContext.setState(ConversationContext.ConversationState.WAITING_TIME_SELECTION);
                    conversationContext.setData("availableTimeSlots", availableSlots);
                    
                    com.example.doannt118.model.BacSi selectedDoctor = 
                        (com.example.doannt118.model.BacSi) conversationContext.getData("selectedDoctor");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    ChatResponse response = new ChatResponse(
                        "✅ BS. " + selectedDoctor.getHoTen() + "\n" +
                        "📅 Ngày " + dateFormat.format(selectedDate) + "\n\n" +
                        "⏰ Có " + availableSlots.size() + " khung giờ trống.\n\n" +
                        "Vui lòng chọn giờ khám:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    List<String> quickReplies = new ArrayList<>();
                    for (int i = 0; i < Math.min(availableSlots.size(), 6); i++) {
                        quickReplies.add(availableSlots.get(i));
                    }
                    
                    if (availableSlots.size() > 6) {
                        quickReplies.add("⏰ Xem tất cả giờ");
                    }
                    
                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    
                })
                .addOnFailureListener(e -> {
                    ChatResponse response = new ChatResponse(
                        "❌ Lỗi khi kiểm tra lịch đã đặt. Vui lòng thử lại!",
                        ChatResponse.ResponseType.TEXT
                    );
                    callback.onResponse(response);
                });
    }
    
    private void handleTimeSelection(String userMessage, ChatCallback callback) {
        @SuppressWarnings("unchecked")
        List<String> availableTimeSlots = (List<String>) conversationContext.getData("availableTimeSlots");
        
        if (availableTimeSlots == null) {
            ChatResponse response = new ChatResponse(
                "❌ Lỗi dữ liệu. Vui lòng bắt đầu lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        // Kiểm tra xem user có chọn "Xem tất cả giờ" không
        if (userMessage.contains("Xem tất cả") || userMessage.contains("xem tất cả")) {
            ChatResponse response = new ChatResponse(
                "⏰ TẤT CẢ KHUNG GIỜ TRỐNG\n\n" +
                "Danh sách đầy đủ:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            for (String slot : availableTimeSlots) {
                quickReplies.add(slot);
                if (quickReplies.size() >= 8) break; // Giới hạn 8
            }
            
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
            return;
        }
        
        // Tìm khung giờ được chọn
        String selectedTimeSlot = null;
        for (String slot : availableTimeSlots) {
            if (userMessage.trim().equals(slot) || 
                userMessage.contains(slot) || 
                slot.contains(userMessage.trim())) {
                selectedTimeSlot = slot;
                break;
            }
        }
        
        if (selectedTimeSlot == null) {
            ChatResponse response = new ChatResponse(
                "🤔 Tôi không tìm thấy khung giờ \"" + userMessage + "\".\n\n" +
                "Vui lòng chọn một trong các khung giờ sau:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            for (int i = 0; i < Math.min(availableTimeSlots.size(), 4); i++) {
                quickReplies.add(availableTimeSlots.get(i));
            }
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
            return;
        }
        
        // Lưu khung giờ đã chọn và hiển thị xác nhận
        conversationContext.setData("selectedTimeSlot", selectedTimeSlot);
        conversationContext.setState(ConversationContext.ConversationState.WAITING_CONFIRMATION);
        
        // Lấy thông tin đã chọn để hiển thị
        com.example.doannt118.model.BacSi selectedDoctor = 
            (com.example.doannt118.model.BacSi) conversationContext.getData("selectedDoctor");
        Date selectedDate = (Date) conversationContext.getData("selectedDate");
        String selectedChuyenKhoa = (String) conversationContext.getData("selectedChuyenKhoa");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
        
        ChatResponse response = new ChatResponse(
            "✅ XÁC NHẬN THÔNG TIN ĐẶT LỊCH\n\n" +
            "🩺 Chuyên khoa: " + selectedChuyenKhoa + "\n" +
            "👨‍⚕️ Bác sĩ: " + selectedDoctor.getHoTen() + "\n" +
            "📅 Ngày khám: " + dayFormat.format(selectedDate) + ", " + dateFormat.format(selectedDate) + "\n" +
            "⏰ Giờ khám: " + selectedTimeSlot + "\n" +
            "💰 Phí khám: 200,000 VNĐ\n" +
            "🏥 Địa điểm: " + (selectedDoctor.getDiaChi() != null ? selectedDoctor.getDiaChi() : "Phòng khám") + "\n\n" +
            "❓ Bạn có xác nhận đặt lịch này không?",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("✅ Xác nhận đặt lịch");
        quickReplies.add("✏️ Chỉnh sửa thông tin");
        quickReplies.add("❌ Hủy bỏ");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    private void handleConfirmation(String userMessage, ChatCallback callback) {
        String message = userMessage.toLowerCase().trim();
        
        if (message.contains("xác nhận") || message.contains("đồng ý") || message.contains("ok")) {
            // Thực hiện đặt lịch thực tế vào Firestore
            performActualBooking(callback);
            
        } else if (message.contains("chỉnh sửa") || message.contains("sửa")) {
            // Quay lại bước chọn thông tin
            conversationContext.setState(ConversationContext.ConversationState.WAITING_SPECIALTY_SELECTION);
            
            ChatResponse response = new ChatResponse(
                "✏️ CHỈNH SỬA THÔNG TIN\n\n" +
                "Bạn muốn chỉnh sửa thông tin nào?",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("🩺 Chuyên khoa");
            quickReplies.add("📅 Ngày khám");
            quickReplies.add("👨‍⚕️ Bác sĩ");
            quickReplies.add("⏰ Giờ khám");
            
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
            
        } else {
            // Hủy đặt lịch
            conversationContext.reset();
            
            ChatResponse response = new ChatResponse(
                "❌ Đã hủy đặt lịch khám\n\n" +
                "Bạn có muốn thực hiện hành động khác không?",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("📅 Đặt lịch lại");
            quickReplies.add("👨‍⚕️ Tìm bác sĩ");
            quickReplies.add("🏥 Thông tin bệnh viện");
            quickReplies.add("💬 Trò chuyện khác");
            
            response.setQuickReplies(quickReplies);
            callback.onResponse(response);
        }
    }
    
    private void performActualBooking(ChatCallback callback) {
        // Lấy thông tin từ conversation context
        com.example.doannt118.model.BacSi selectedDoctor = 
            (com.example.doannt118.model.BacSi) conversationContext.getData("selectedDoctor");
        Date selectedDate = (Date) conversationContext.getData("selectedDate");
        String selectedTimeSlot = (String) conversationContext.getData("selectedTimeSlot");
        String selectedChuyenKhoa = (String) conversationContext.getData("selectedChuyenKhoa");
        
        if (selectedDoctor == null || selectedDate == null || selectedTimeSlot == null || maBenhNhan == null) {
            ChatResponse response = new ChatResponse(
                "❌ Lỗi dữ liệu\n\n" +
                "Thiếu thông tin cần thiết. Vui lòng bắt đầu lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        // Tạo lịch khám mới (giống logic trong ChiTietBacSiActivity)
        String maLichKham = java.util.UUID.randomUUID().toString();
        com.example.doannt118.model.LichKham lichKham = new com.example.doannt118.model.LichKham();
        lichKham.setMaLichKham(maLichKham);
        lichKham.setMaBenhNhan(maBenhNhan);
        lichKham.setMaBacSi(selectedDoctor.getMaBacSi());
        lichKham.setNgayKham(new com.google.firebase.Timestamp(selectedDate));
        lichKham.setGioKham(selectedTimeSlot);
        lichKham.setTrangThai("CHO"); // Chờ xác nhận
        
        // Lưu vào Firestore
        repo.addDocument("LichKham", maLichKham, lichKham,
            aVoid -> {
                // Thành công
                conversationContext.setState(ConversationContext.ConversationState.COMPLETED);
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("vi", "VN"));
                
                ChatResponse response = new ChatResponse(
                    "🎉 ĐẶT LỊCH THÀNH CÔNG!\n\n" +
                    " THÔNG TIN LỊCH KHÁM:\n" +
                    "• Mã lịch khám: " + maLichKham.substring(0, 8).toUpperCase() + "\n" +
                    "• Chuyên khoa: " + selectedChuyenKhoa + "\n" +
                    "• Bác sĩ: " + selectedDoctor.getHoTen() + "\n" +
                    "• Thời gian: " + dayFormat.format(selectedDate) + ", " + dateFormat.format(selectedDate) + " lúc " + selectedTimeSlot + "\n" +
                    "• Trạng thái: Chờ xác nhận\n" +
                    "• Phí khám: 200,000 VNĐ\n\n" +
                    "📱 LƯU Ý QUAN TRỌNG:\n" +
                    "• Vui lòng có mặt trước 15 phút\n" +
                    "• Mang theo CMND và thẻ BHYT (nếu có)\n" +
                    "• Bác sĩ sẽ xác nhận lịch khám trong 24h\n" +
                    "• Liên hệ hotline 1900-1234 nếu cần hỗ trợ\n\n" +
                    "Bạn còn cần hỗ trợ gì khác không?",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("📅 Xem lịch khám của tôi");
                quickReplies.add("📅 Đặt thêm lịch khác");
                quickReplies.add("💬 Chat với bác sĩ");
                quickReplies.add("🏥 Thông tin bệnh viện");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
                // Reset conversation context
                conversationContext.reset();
                
            }, e -> {
                // Lỗi
                Log.e("ChatbotEngine", "Error booking appointment: ", e);
                
                ChatResponse response = new ChatResponse(
                    "❌ ĐẶT LỊCH THẤT BẠI\n\n" +
                    "Có lỗi xảy ra khi đặt lịch khám. Có thể:\n" +
                    "• Khung giờ vừa được người khác đặt\n" +
                    "• Lỗi kết nối mạng\n" +
                    "• Lỗi hệ thống\n\n" +
                    "Vui lòng thử lại hoặc liên hệ lễ tân: 1900-1234",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("🔄 Thử lại");
                quickReplies.add("📞 Liên hệ lễ tân");
                quickReplies.add("📅 Chọn giờ khác");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    private void handleCancelSelection(String userMessage, ChatCallback callback) {
        // Placeholder for cancel selection
        ChatResponse response = new ChatResponse(
            "Chức năng hủy lịch đang được phát triển. Vui lòng sử dụng ứng dụng chính.",
            ChatResponse.ResponseType.TEXT
        );
        callback.onResponse(response);
    }
    
    private void handleScheduleActionSelection(String userMessage, ChatCallback callback) {
        // Placeholder for schedule action
        ChatResponse response = new ChatResponse(
            "Chức năng cập nhật lịch đang được phát triển. Vui lòng sử dụng ứng dụng chính.",
            ChatResponse.ResponseType.TEXT
        );
        callback.onResponse(response);
    }
    
    // ============================================
    // COMMON HANDLERS
    // ============================================
    
    private void handleDoctorQuery(String userMessage, ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "👨‍⚕️ TÌM BÁC SĨ\n\n" +
            "Để tìm bác sĩ theo chuyên khoa, vui lòng sử dụng ứng dụng chính.\n\n" +
            "Hoặc bạn có thể đặt lịch khám trực tiếp:",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("📅 Đặt lịch khám");
        quickReplies.add("🏥 Thông tin bệnh viện");
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    private void handleHospitalInfo(String userMessage, ChatCallback callback) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("giờ") || message.contains("thời gian")) {
            ChatResponse response = new ChatResponse(
                "🕐 GIỜ LÀM VIỆC\n\n" +
                "📅 Thứ 2 - Thứ 6:\n" +
                "• Sáng: 7:00 - 11:30\n" +
                "• Chiều: 13:30 - 17:00\n\n" +
                "📅 Thứ 7: 7:00 - 11:30\n" +
                "📅 Chủ nhật: Nghỉ\n\n" +
                "🚨 Cấp cứu: 24/7",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("💰 Bảng giá");
            quickReplies.add("📍 Địa chỉ");
            quickReplies.add("📞 Liên hệ");
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
        } else if (message.contains("giá") || message.contains("phí")) {
            ChatResponse response = new ChatResponse(
                "💰 BẢNG GIÁ\n\n" +
                "🩺 Khám bệnh:\n" +
                "• Khám tổng quát: 150,000 VNĐ\n" +
                "• Khám chuyên khoa: 200,000 VNĐ\n" +
                "• Tái khám: 100,000 VNĐ\n\n" +
                "🔬 Xét nghiệm:\n" +
                "• Máu cơ bản: 200,000 VNĐ\n" +
                "• X-quang: 300,000 VNĐ",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("🕐 Giờ làm việc");
            quickReplies.add("📍 Địa chỉ");
            quickReplies.add("📞 Liên hệ");
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
        } else {
            ChatResponse response = new ChatResponse(
                "🏥 THÔNG TIN BỆNH VIỆN\n\n" +
                "Chào mừng bạn đến với Bệnh viện ABC!\n\n" +
                "🩺 Chuyên khoa: Nội, Ngoại, Tim mạch, Da liễu...\n" +
                "⭐ Dịch vụ: Khám bệnh, Xét nghiệm, Siêu âm, Cấp cứu 24/7\n\n" +
                "Bạn muốn biết thêm gì?",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = new ArrayList<>();
            quickReplies.add("🕐 Giờ làm việc");
            quickReplies.add("💰 Bảng giá");
            quickReplies.add("📍 Địa chỉ");
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
        }
    }
    
    private void handleGreeting(ChatCallback callback) {
        String currentUserType = (String) conversationContext.getData("userType");
        
        if (currentUserType == null) {
            handleRoleSelection(callback);
            return;
        }
        
        if ("bacsi".equals(currentUserType)) {
            // Lấy thông tin thực tế cho bác sĩ
            if (maBacSi != null && !maBacSi.isEmpty()) {
                loadDoctorGreetingData(callback);
            } else {
                showDefaultDoctorGreeting(callback);
            }
        } else {
            // Lấy thông tin thực tế cho bệnh nhân
            if (maBenhNhan != null && !maBenhNhan.isEmpty()) {
                loadPatientGreetingData(callback);
            } else {
                showDefaultPatientGreeting(callback);
            }
        }
    }
    
    private void loadDoctorGreetingData(ChatCallback callback) {
        // Lấy thông tin bác sĩ và số liệu hôm nay
        repo.getCollection("BacSi").document(maBacSi).get()
            .addOnSuccessListener(documentSnapshot -> {
                String tenBacSi = "Bác sĩ";
                
                if (documentSnapshot.exists()) {
                    com.example.doannt118.model.BacSi bacSi = 
                        documentSnapshot.toObject(com.example.doannt118.model.BacSi.class);
                    if (bacSi != null && bacSi.getHoTen() != null) {
                        tenBacSi = bacSi.getHoTen();
                    }
                }
                
                final String finalTenBacSi = tenBacSi;
                
                // Đếm số bệnh nhân hôm nay
                Calendar calStart = Calendar.getInstance();
                calStart.set(Calendar.HOUR_OF_DAY, 0);
                calStart.set(Calendar.MINUTE, 0);
                calStart.set(Calendar.SECOND, 0);
                calStart.set(Calendar.MILLISECOND, 0);
                Date startOfDay = calStart.getTime();

                Calendar calEnd = Calendar.getInstance();
                calEnd.set(Calendar.HOUR_OF_DAY, 23);
                calEnd.set(Calendar.MINUTE, 59);
                calEnd.set(Calendar.SECOND, 59);
                calEnd.set(Calendar.MILLISECOND, 999);
                Date endOfDay = calEnd.getTime();
                
                repo.getByField("LichKham", "maBacSi", maBacSi,
                    querySnapshot -> {
                        int benhNhanHomNay = 0;
                        int choXacNhan = 0;
                        
                        for (var doc : querySnapshot.getDocuments()) {
                            String trangThai = doc.getString("trangThai");
                            com.google.firebase.Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                            
                            if ("CHO".equals(trangThai)) {
                                choXacNhan++;
                            }
                            
                            if (ngayKhamTs != null && "XAC_NHAN".equals(trangThai)) {
                                Date ngayKham = ngayKhamTs.toDate();
                                if (ngayKham.getTime() >= startOfDay.getTime() && 
                                    ngayKham.getTime() <= endOfDay.getTime()) {
                                    benhNhanHomNay++;
                                }
                            }
                        }
                        
                        StringBuilder responseText = new StringBuilder();
                        responseText.append("👨‍⚕️ Xin chào BS. ").append(finalTenBacSi).append("!\n\n");
                        responseText.append("Chúc bạn một ngày làm việc hiệu quả! 🌟\n\n");
                        
                        responseText.append("📊 TỔNG QUAN HÔM NAY:\n");
                        responseText.append("• 👥 Bệnh nhân hôm nay: ").append(benhNhanHomNay).append("\n");
                        responseText.append("• ⏳ Chờ xác nhận: ").append(choXacNhan).append("\n\n");
                        
                        if (choXacNhan > 0) {
                            responseText.append("🔔 Bạn có ").append(choXacNhan).append(" lịch khám cần xác nhận!\n\n");
                        }
                        
                        responseText.append("Bạn cần hỗ trợ gì hôm nay?");
                        
                        ChatResponse response = new ChatResponse(
                            responseText.toString(),
                            ChatResponse.ResponseType.QUICK_REPLY
                        );
                        
                        List<String> quickReplies = new ArrayList<>();
                        if (choXacNhan > 0) {
                            quickReplies.add("✅ Xác nhận lịch khám (" + choXacNhan + ")");
                        } else {
                            quickReplies.add("✅ Xác nhận lịch khám");
                        }
                        quickReplies.add("👥 Bệnh nhân hôm nay");
                        quickReplies.add("📅 Lịch làm việc");
                        quickReplies.add("📊 Thống kê");
                        response.setQuickReplies(quickReplies);
                        
                        callback.onResponse(response);
                        
                    }, e -> {
                        showDefaultDoctorGreeting(callback);
                    });
                    
            })
            .addOnFailureListener(e -> {
                showDefaultDoctorGreeting(callback);
            });
    }
    
    private void showDefaultDoctorGreeting(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "👨‍⚕️ Xin chào Bác sĩ!\n\n" +
            "Chúc bạn một ngày làm việc hiệu quả! 🌟\n\n" +
            "Bạn cần hỗ trợ gì hôm nay?",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("📅 Lịch làm việc hôm nay");
        quickReplies.add("👥 Bệnh nhân hôm nay");
        quickReplies.add("✅ Xác nhận lịch khám");
        quickReplies.add("📊 Thống kê");
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    private void loadPatientGreetingData(ChatCallback callback) {
        // Lấy thông tin bệnh nhân và số liệu
        repo.getCollection("BenhNhan").document(maBenhNhan).get()
            .addOnSuccessListener(documentSnapshot -> {
                String tenBenhNhan = "bạn";
                
                if (documentSnapshot.exists()) {
                    String hoTen = documentSnapshot.getString("hoTen");
                    if (hoTen != null && !hoTen.isEmpty()) {
                        tenBenhNhan = hoTen;
                    }
                }
                
                final String finalTenBenhNhan = tenBenhNhan;
                
                // Đếm số lịch khám sắp tới
                repo.getByField("LichKham", "maBenhNhan", maBenhNhan,
                    querySnapshot -> {
                        int lichSapToi = 0;
                        int donThuocDangDung = 0;
                        
                        Calendar today = Calendar.getInstance();
                        today.set(Calendar.HOUR_OF_DAY, 0);
                        today.set(Calendar.MINUTE, 0);
                        today.set(Calendar.SECOND, 0);
                        today.set(Calendar.MILLISECOND, 0);
                        Date startOfDay = today.getTime();
                        
                        for (var doc : querySnapshot.getDocuments()) {
                            String trangThai = doc.getString("trangThai");
                            com.google.firebase.Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                            
                            if (ngayKhamTs != null && 
                                ("CHO".equals(trangThai) || "XAC_NHAN".equals(trangThai))) {
                                Date ngayKham = ngayKhamTs.toDate();
                                if (ngayKham.getTime() >= startOfDay.getTime()) {
                                    lichSapToi++;
                                }
                            }
                        }
                        
                        StringBuilder responseText = new StringBuilder();
                        responseText.append("👋 Xin chào ").append(finalTenBenhNhan).append("!\n\n");
                        responseText.append("Tôi là MediBot - trợ lý ảo của bệnh viện! 🏥\n\n");
                        
                        if (lichSapToi > 0) {
                            responseText.append("📅 Bạn có ").append(lichSapToi).append(" lịch khám sắp tới.\n\n");
                        }
                        
                        responseText.append("Bạn cần hỗ trợ gì hôm nay?");
                        
                        ChatResponse response = new ChatResponse(
                            responseText.toString(),
                            ChatResponse.ResponseType.QUICK_REPLY
                        );
                        
                        List<String> quickReplies = new ArrayList<>();
                        quickReplies.add("📅 Đặt lịch khám");
                        if (lichSapToi > 0) {
                            quickReplies.add("👀 Xem lịch khám (" + lichSapToi + ")");
                        } else {
                            quickReplies.add("👀 Xem lịch khám");
                        }
                        quickReplies.add("👨‍⚕️ Tìm bác sĩ");
                        quickReplies.add("🏥 Thông tin bệnh viện");
                        response.setQuickReplies(quickReplies);
                        
                        callback.onResponse(response);
                        
                    }, e -> {
                        showDefaultPatientGreeting(callback);
                    });
                    
            })
            .addOnFailureListener(e -> {
                showDefaultPatientGreeting(callback);
            });
    }
    
    private void showDefaultPatientGreeting(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "👋 Xin chào!\n\n" +
            "Tôi là MediBot - trợ lý ảo của bệnh viện! 🏥\n\n" +
            "Bạn cần hỗ trợ gì hôm nay?",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("📅 Đặt lịch khám");
        quickReplies.add("👀 Xem lịch khám");
        quickReplies.add("👨‍⚕️ Tìm bác sĩ");
        quickReplies.add("🏥 Thông tin bệnh viện");
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    private void handleThanks(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "😊 Rất vui được giúp đỡ bạn!\n\n" +
            "Nếu bạn cần hỗ trợ thêm, đừng ngần ngại nhắn tin cho tôi nhé!\n\n" +
            "Chúc bạn sức khỏe! 🌟",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("📅 Đặt lịch khám");
        quickReplies.add("🏥 Thông tin bệnh viện");
        response.setQuickReplies(quickReplies);
        
        callback.onResponse(response);
    }
    
    private void handleUnknownIntent(String userMessage, ChatCallback callback) {
        // Enhanced unknown intent handling with better AI integration
        if (geminiAssistant != null) {
            handleWithEnhancedGemini(userMessage, callback);
        } else {
            // Improved fallback response
            String userType = (String) conversationContext.getData("userType");
            ChatResponse response = new ChatResponse(
                "🤔 Tôi chưa hiểu rõ câu hỏi \"" + userMessage + "\"\n\n" +
                "💡 Tôi có thể giúp bạn với:",
                ChatResponse.ResponseType.QUICK_REPLY
            );
            
            List<String> quickReplies = getContextualSuggestions(userMessage, userType);
            response.setQuickReplies(quickReplies);
            
            callback.onResponse(response);
        }
    }
    
    private void handleWithEnhancedGemini(String userMessage, ChatCallback callback) {
        String userType = (String) conversationContext.getData("userType");
        String userContext = buildEnhancedUserContext();
        
        // Add conversation context for better continuity
        conversationContext.addToHistory(userMessage, true);
        
        // Detect if this is a medical question for better handling
        boolean isMedicalQuestion = geminiAssistant.isMedicalAdvice(userMessage);
        
        if (isMedicalQuestion) {
            // Use specialized medical context
            String medicalContext = geminiAssistant.buildMedicalContext(userType, userMessage, "");
            geminiAssistant.ask(userMessage, medicalContext, userType, new GeminiAssistant.GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    handleGeminiSuccess(response, userMessage, userType, callback);
                }
                
                @Override
                public void onError(String error) {
                    handleGeminiError(error, userMessage, userType, callback);
                }
            });
        } else {
            // Use general context
            geminiAssistant.ask(userMessage, userContext, userType, new GeminiAssistant.GeminiCallback() {
                @Override
                public void onSuccess(String response) {
                    handleGeminiSuccess(response, userMessage, userType, callback);
                }
                
                @Override
                public void onError(String error) {
                    handleGeminiError(error, userMessage, userType, callback);
                }
            });
        }
    }
    
    private void handleGeminiSuccess(String response, String userMessage, String userType, ChatCallback callback) {
        // Enhanced response processing
        response = processGeminiResponse(response, userMessage, userType);
        
        // Add to conversation history
        conversationContext.addToHistory(response, false);
        
        // Create enhanced response with contextual suggestions
        ChatResponse chatResponse = new ChatResponse(response, ChatResponse.ResponseType.QUICK_REPLY);
        
        // Add contextual quick replies
        List<String> suggestions = getContextualSuggestions(userMessage, userType);
        if (!suggestions.isEmpty()) {
            chatResponse.setQuickReplies(suggestions);
        }
        
        callback.onResponse(chatResponse);
    }
    
    private void handleGeminiError(String error, String userMessage, String userType, ChatCallback callback) {
        // Enhanced error handling with fallback responses
        String fallbackResponse = generateFallbackResponse(userMessage, userType);
        
        ChatResponse response = new ChatResponse(fallbackResponse, ChatResponse.ResponseType.QUICK_REPLY);
        List<String> suggestions = conversationContext.getContextualSuggestions();
        if (!suggestions.isEmpty()) {
            response.setQuickReplies(suggestions);
        }
        
        callback.onResponse(response);
    }
    
    /**
     * Build enhanced user context for better AI responses
     */
    private String buildEnhancedUserContext() {
        StringBuilder context = new StringBuilder();
        
        String userType = (String) conversationContext.getData("userType");
        context.append("Người dùng: ").append(userType).append("\n");
        
        // Add conversation history
        String conversationSummary = conversationContext.getConversationSummary();
        if (!conversationSummary.isEmpty()) {
            context.append("Ngữ cảnh cuộc trò chuyện:\n").append(conversationSummary).append("\n");
        }
        
        // Add current time context
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm", new java.util.Locale("vi", "VN"));
        context.append("Thời gian hiện tại: ").append(sdf.format(new java.util.Date())).append("\n");
        
        // Add user-specific context
        if ("benhnhan".equals(userType) && maBenhNhan != null) {
            context.append("Mã bệnh nhân: ").append(maBenhNhan).append("\n");
        } else if ("bacsi".equals(userType) && maBacSi != null) {
            context.append("Mã bác sĩ: ").append(maBacSi).append("\n");
        }
        
        return context.toString();
    }
    
    /**
     * Process Gemini response for better formatting
     */
    private String processGeminiResponse(String response, String userMessage, String userType) {
        if (response == null || response.trim().isEmpty()) {
            return generateFallbackResponse(userMessage, userType);
        }
        
        // Clean up response
        response = response.trim();
        
        // Add medical disclaimer if needed
        if (geminiAssistant.isMedicalAdvice(userMessage) && !response.contains("bác sĩ")) {
            response += "\n\n⚠️ *Lưu ý: Đây chỉ là tư vấn sơ bộ. Vui lòng gặp bác sĩ để được chẩn đoán chính xác.*";
        }
        
        return response;
    }
    
    /**
     * Generate contextual suggestions based on user message and type
     */
    private List<String> getContextualSuggestions(String userMessage, String userType) {
        List<String> suggestions = new ArrayList<>();
        
        if ("bacsi".equals(userType)) {
            // Doctor suggestions
            if (userMessage.toLowerCase().contains("bệnh nhân")) {
                suggestions.add("👥 Xem bệnh nhân hôm nay");
                suggestions.add("📋 Quản lý bệnh án");
            } else if (userMessage.toLowerCase().contains("thuốc")) {
                suggestions.add("💊 Quản lý đơn thuốc");
                suggestions.add("🔍 Tra cứu thuốc");
            } else {
                suggestions.add("👥 Bệnh nhân hôm nay");
                suggestions.add("📅 Lịch làm việc");
                suggestions.add("🤖 AI Assistant");
            }
        } else {
            // Patient suggestions
            if (userMessage.toLowerCase().contains("đau") || userMessage.toLowerCase().contains("bệnh")) {
                suggestions.add("👨‍⚕️ Tìm bác sĩ");
                suggestions.add("📅 Đặt lịch khám");
                suggestions.add("💬 Chat với bác sĩ");
            } else if (userMessage.toLowerCase().contains("thuốc")) {
                suggestions.add("💊 Xem đơn thuốc");
                suggestions.add("⏰ Quản lý uống thuốc");
            } else {
                suggestions.add("📅 Đặt lịch khám");
                suggestions.add("👨‍⚕️ Tìm bác sĩ");
                suggestions.add("🏥 Thông tin bệnh viện");
            }
        }
        
        return suggestions;
    }
    
    /**
     * Generate fallback response when AI fails
     */
    private String generateFallbackResponse(String userMessage, String userType) {
        if (geminiAssistant.isMedicalAdvice(userMessage)) {
            return "🩺 Đây là câu hỏi y tế quan trọng.\n\n" +
                   "💡 Tôi khuyên bạn nên:\n" +
                   "• 👨‍⚕️ Tư vấn trực tiếp với bác sĩ\n" +
                   "• 📅 Đặt lịch khám để được thăm khám\n" +
                   "• 🚨 Nếu cấp cứu, gọi 115 ngay\n\n" +
                   "Sức khỏe của bạn rất quan trọng! 💙";
        }
        
        return "😅 Xin lỗi, tôi gặp chút khó khăn với câu hỏi này.\n\n" +
               "💡 Bạn có thể:\n" +
               "• Thử hỏi lại bằng cách khác\n" +
               "• Sử dụng menu chức năng bên dưới\n" +
               "• Liên hệ hỗ trợ nếu cần thiết\n\n" +
               "Tôi luôn sẵn sàng giúp đỡ bạn! 😊";
    }
    
    private String buildUserContext() {
        return buildEnhancedUserContext();
    }
    
    private boolean isMedicalAdvice(String message) {
        return geminiAssistant.isMedicalAdvice(message);
    }
}