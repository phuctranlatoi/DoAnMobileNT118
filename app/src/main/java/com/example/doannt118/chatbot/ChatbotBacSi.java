package com.example.doannt118.chatbot;

import android.content.Context;
import android.util.Log;
import com.example.doannt118.model.BenhAn;
import com.example.doannt118.model.LichKham;
import com.example.doannt118.model.LichLamViec;
import com.example.doannt118.repository.FirestoreRepository;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * CHATBOT DÀNH CHO BÁC SĨ
 * Tích hợp dữ liệu thực từ Firestore dựa trên MainBacSiActivity
 */
public class ChatbotBacSi {
    
    private Context context;
    private FirestoreRepository repo;
    private String maBacSi;
    private IntentDetector intentDetector;
    
    public interface ChatCallback {
        void onResponse(ChatResponse response);
        void onError(String error);
    }
    
    public ChatbotBacSi(Context context, String maBacSi) {
        this.context = context;
        this.maBacSi = maBacSi;
        this.repo = new FirestoreRepository();
        this.intentDetector = new IntentDetector();
    }
    
    public void processMessage(String userMessage, ChatCallback callback) {
        IntentDetector.Intent intent = intentDetector.detect(userMessage);
        
        switch (intent) {
            case XEM_LICH_LAM_VIEC:
                handleDoctorSchedule(callback);
                break;
                
            case XEM_BENH_NHAN_NGAY:
                handleViewTodayPatients(callback);
                break;
                
            case XEM_BENH_AN:
                handleViewMedicalRecords(callback);
                break;
                
            case THONG_KE_BAC_SI:
                handleDoctorStatistics(callback);
                break;
                
            case XAC_NHAN_LICH_KHAM:
                handleConfirmAppointments(callback);
                break;
                
            case QUAN_LY_BENH_AN:
                handleManageMedicalRecords(callback);
                break;
                
            case QUAN_LY_DON_THUOC_BS:
                handleManagePrescriptions(callback);
                break;
                
            case NHAP_MA_KHAM:
                handleEnterPatientCode(callback);
                break;
                
            case AI_ASSISTANT:
                handleAIAssistant(callback);
                break;
                
            case CHAT_VOI_BENH_NHAN:
                handleChatWithPatients(callback);
                break;
                
            case CHAO_HOI:
                handleGreeting(callback);
                break;
                
            case CAM_ON:
                handleThanks(callback);
                break;
                
            default:
                // Thêm debug cho các câu hỏi không hiểu
                if (userMessage.toLowerCase().contains("tất cả lịch khám") || 
                    userMessage.toLowerCase().contains("debug") ||
                    userMessage.toLowerCase().contains("kiểm tra")) {
                    handleDebugAllAppointments(callback);
                } else {
                    handleUnknown(userMessage, callback);
                }
        }
    }
    
    // LỊCH LÀM VIỆC HÔM NAY - Dựa trên MainBacSiActivity
    private void handleDoctorSchedule(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        // Lấy lịch làm việc hôm nay
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
                        "LỊCH LÀM VIỆC HÔM NAY\n\nHôm nay bạn không có lịch làm việc.\n\nBạn có muốn:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("Xem lịch tuần này");
                    quickReplies.add("Quản lý lịch làm việc");
                    quickReplies.add("Bệnh nhân hôm nay");
                    quickReplies.add("Thống kê");
                    
                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }
                
                StringBuilder responseText = new StringBuilder();
                responseText.append("LỊCH LÀM VIỆC HÔM NAY\n\n");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                responseText.append("Ngày: ").append(dateFormat.format(new Date())).append("\n\n");
                
                for (var doc : querySnapshot.getDocuments()) {
                    LichLamViec lichLamViec = doc.toObject(LichLamViec.class);
                    if (lichLamViec != null) {
                        responseText.append("Ca làm việc: ").append(lichLamViec.getCaLamViec()).append("\n");
                        if (lichLamViec.getLoaiHinh() != null) {
                            responseText.append("Loại hình: ").append(lichLamViec.getLoaiHinh()).append("\n");
                        }
                        if (lichLamViec.getGhiChu() != null && !lichLamViec.getGhiChu().isEmpty()) {
                            responseText.append("Ghi chú: ").append(lichLamViec.getGhiChu()).append("\n");
                        }
                        responseText.append("\n");
                    }
                }
                
                responseText.append("Để quản lý lịch chi tiết, vui lòng sử dụng ứng dụng chính.");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Xác nhận lịch khám");
                quickReplies.add("Thống kê");
                quickReplies.add("Quản lý lịch làm việc");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            }, e -> {
                Log.e("ChatbotBacSi", "Error loading doctor schedule: ", e);
                
                ChatResponse response = new ChatResponse(
                    "Lỗi tải lịch làm việc\n\nKhông thể tải lịch làm việc. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Thử lại");
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Liên hệ hỗ trợ");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    // BỆNH NHÂN HÔM NAY - Dựa trên MainBacSiActivity.loadLichHenHomNay()
    private void handleViewTodayPatients(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        Log.d("ChatbotBacSi", "Loading today patients for maBacSi: " + maBacSi);
        
        // Lấy ngày hôm nay (từ 00:00:00 đến 23:59:59) - giống MainBacSiActivity
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
        
        Log.d("ChatbotBacSi", "Date range: " + startOfDay + " to " + endOfDay);
        
        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                List<LichKham> lichHenList = new ArrayList<>();
                
                Log.d("ChatbotBacSi", "Total LichKham documents found: " + querySnapshot.size());
                
                for (var doc : querySnapshot.getDocuments()) {
                    try {
                        String trangThai = doc.getString("trangThai");
                        Log.d("ChatbotBacSi", "LichKham ID: " + doc.getId() + ", trangThai: " + trangThai);
                        
                        if (!"XAC_NHAN".equals(trangThai)) continue;

                        // Kiểm tra ngày khám
                        Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                        if (ngayKhamTs == null) {
                            Log.d("ChatbotBacSi", "ngayKham is null for doc: " + doc.getId());
                            continue;
                        }
                        
                        Date ngayKham = ngayKhamTs.toDate();
                        Log.d("ChatbotBacSi", "ngayKham: " + ngayKham + ", startOfDay: " + startOfDay + ", endOfDay: " + endOfDay);
                        
                        if (ngayKham.before(startOfDay) || ngayKham.after(endOfDay)) {
                            Log.d("ChatbotBacSi", "Date not in range for doc: " + doc.getId());
                            continue;
                        }

                        LichKham lichKham = doc.toObject(LichKham.class);
                        if (lichKham != null) {
                            lichHenList.add(lichKham);
                            Log.d("ChatbotBacSi", "Added LichKham: " + lichKham.getMaLichKham());
                        }
                    } catch (Exception e) {
                        Log.e("ChatbotBacSi", "Error parsing LichKham", e);
                    }
                }
                
                Log.d("ChatbotBacSi", "Final lichHenList size: " + lichHenList.size());
                
                if (lichHenList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "BỆNH NHÂN HÔM NAY\n\nHôm nay bạn không có bệnh nhân nào đã xác nhận.\n\nCó thể do:\n• Chưa có lịch khám nào được đặt\n• Lịch khám chưa được xác nhận\n• Lịch khám không phải hôm nay\n\nBạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("Lịch làm việc");
                    quickReplies.add("Xác nhận lịch khám");
                    quickReplies.add("Thống kê");
                    quickReplies.add("Quản lý lịch");
                    
                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }
                
                // Sắp xếp theo giờ khám - giống MainBacSiActivity
                Collections.sort(lichHenList, (a, b) -> {
                    String gioA = a.getGioKham() != null ? a.getGioKham() : "";
                    String gioB = b.getGioKham() != null ? b.getGioKham() : "";
                    return gioA.compareTo(gioB);
                });
                
                StringBuilder responseText = new StringBuilder();
                responseText.append("BỆNH NHÂN HÔM NAY\n\n");
                responseText.append("Tổng cộng: ").append(lichHenList.size()).append(" bệnh nhân\n\n");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                responseText.append("Ngày: ").append(dateFormat.format(new Date())).append("\n\n");
                
                int count = 0;
                for (LichKham lichKham : lichHenList) {
                    if (count >= 5) break; // Chỉ hiển thị 5 bệnh nhân đầu tiên
                    
                    responseText.append("Giờ khám: ").append(lichKham.getGioKham()).append("\n");
                    responseText.append("Mã BN: ").append(lichKham.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("Mã LK: ").append(lichKham.getMaLichKham().substring(0, 8).toUpperCase()).append("\n");
                    responseText.append("Trạng thái: Đã xác nhận\n\n");
                    
                    count++;
                }
                
                if (lichHenList.size() > 5) {
                    responseText.append("Và ").append(lichHenList.size() - 5).append(" bệnh nhân khác...\n\n");
                }
                
                responseText.append("Để xem chi tiết và quản lý, vui lòng sử dụng ứng dụng chính.");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Lịch làm việc");
                quickReplies.add("Xác nhận lịch khám");
                quickReplies.add("Quản lý bệnh án");
                quickReplies.add("Quản lý đơn thuốc");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            }, e -> {
                Log.e("ChatbotBacSi", "Error loading today patients: ", e);
                
                ChatResponse response = new ChatResponse(
                    "Lỗi tải danh sách bệnh nhân\n\nKhông thể tải danh sách bệnh nhân hôm nay. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Thử lại");
                quickReplies.add("Lịch làm việc");
                quickReplies.add("Liên hệ hỗ trợ");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    // XEM BỆNH ÁN - Dành cho bác sĩ xem bệnh án của bệnh nhân
    private void handleViewMedicalRecords(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        Log.d("ChatbotBacSi", "Loading medical records for doctor: " + maBacSi);
        
        // Lấy bệnh án mà bác sĩ đã khám (có thể theo maBacSi hoặc tất cả)
        repo.getByField("BenhAn", "maBacSi", maBacSi,
            querySnapshot -> {
                List<BenhAn> benhAnList = new ArrayList<>();
                
                for (var doc : querySnapshot.getDocuments()) {
                    BenhAn benhAn = doc.toObject(BenhAn.class);
                    if (benhAn != null) {
                        benhAnList.add(benhAn);
                    }
                }
                
                if (benhAnList.isEmpty()) {
                    ChatResponse response = new ChatResponse(
                        "BỆNH ÁN CỦA BẠN\n\nBạn chưa có bệnh án nào.\n\nBạn có thể:",
                        ChatResponse.ResponseType.QUICK_REPLY
                    );
                    
                    List<String> quickReplies = new ArrayList<>();
                    quickReplies.add("Bệnh nhân hôm nay");
                    quickReplies.add("Lịch làm việc");
                    quickReplies.add("Quản lý bệnh án");
                    quickReplies.add("Thống kê");
                    
                    response.setQuickReplies(quickReplies);
                    callback.onResponse(response);
                    return;
                }
                
                // Sắp xếp theo ngày khám (mới nhất trước)
                benhAnList.sort((a, b) -> {
                    Timestamp tsA = a.getNgayKhamAsTimestamp();
                    Timestamp tsB = b.getNgayKhamAsTimestamp();
                    if (tsA == null) return 1;
                    if (tsB == null) return -1;
                    return tsB.compareTo(tsA);
                });
                
                StringBuilder responseText = new StringBuilder();
                responseText.append("BỆNH ÁN ĐÃ KHÁM\n\n");
                responseText.append("Tổng cộng: ").append(benhAnList.size()).append(" bệnh án\n\n");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                
                int count = 0;
                for (BenhAn benhAn : benhAnList) {
                    if (count >= 5) break; // Chỉ hiển thị 5 bệnh án gần nhất
                    
                    responseText.append("BỆNH ÁN ").append(count + 1).append("\n");
                    responseText.append("Mã BA: ").append(benhAn.getMaBenhAn().substring(0, 8).toUpperCase()).append("\n");
                    
                    Timestamp ngayKham = benhAn.getNgayKhamAsTimestamp();
                    if (ngayKham != null) {
                        responseText.append("Ngày khám: ").append(dateFormat.format(ngayKham.toDate())).append("\n");
                    }
                    
                    if (benhAn.getMaBenhNhan() != null) {
                        responseText.append("Mã BN: ").append(benhAn.getMaBenhNhan().substring(0, 8).toUpperCase()).append("\n");
                    }
                    
                    if (benhAn.getChanDoan() != null && !benhAn.getChanDoan().isEmpty()) {
                        String chanDoan = benhAn.getChanDoan();
                        if (chanDoan.length() > 50) {
                            chanDoan = chanDoan.substring(0, 50) + "...";
                        }
                        responseText.append("Chẩn đoán: ").append(chanDoan).append("\n");
                    }
                    
                    if (benhAn.getLoaiKham() != null && !benhAn.getLoaiKham().isEmpty()) {
                        responseText.append("Loại khám: ").append(benhAn.getLoaiKham()).append("\n");
                    }
                    
                    if (benhAn.getPhiKham() > 0) {
                        responseText.append("Phí khám: ").append(String.format("%,d", benhAn.getPhiKham())).append(" VNĐ\n");
                    }
                    
                    responseText.append("\n");
                    count++;
                }
                
                if (benhAnList.size() > 5) {
                    responseText.append("Và ").append(benhAnList.size() - 5).append(" bệnh án khác...\n\n");
                }
                
                responseText.append("Để quản lý chi tiết, vui lòng sử dụng ứng dụng chính.");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Quản lý bệnh án");
                quickReplies.add("Quản lý đơn thuốc");
                quickReplies.add("Thống kê");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            }, e -> {
                Log.e("ChatbotBacSi", "Error loading medical records: ", e);
                
                ChatResponse response = new ChatResponse(
                    "Lỗi tải bệnh án\n\nKhông thể tải danh sách bệnh án. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Thử lại");
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Liên hệ hỗ trợ");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    // THỐNG KÊ BÁC SĨ
    private void handleDoctorStatistics(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
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
                    Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
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
                responseText.append("THỐNG KÊ CỦA BẠN\n\n");
                
                responseText.append("TỔNG QUAN:\n");
                responseText.append("• Tổng lịch khám: ").append(totalAppointments).append("\n");
                responseText.append("• Đã xác nhận: ").append(confirmedAppointments).append("\n");
                responseText.append("• Hoàn thành: ").append(completedAppointments).append("\n");
                responseText.append("• Đã hủy: ").append(cancelledAppointments).append("\n\n");
                
                responseText.append("HÔM NAY:\n");
                responseText.append("• Bệnh nhân hôm nay: ").append(todayAppointments).append("\n\n");
                
                // Tính tỷ lệ hoàn thành
                if (totalAppointments > 0) {
                    double completionRate = (double) completedAppointments / totalAppointments * 100;
                    responseText.append("Tỷ lệ hoàn thành: ").append(String.format("%.1f%%", completionRate)).append("\n\n");
                }
                
                responseText.append("Để xem báo cáo chi tiết, vui lòng sử dụng ứng dụng chính.");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Lịch làm việc");
                quickReplies.add("Xác nhận lịch khám");
                quickReplies.add("Quản lý bệnh án");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            }, e -> {
                Log.e("ChatbotBacSi", "Error loading statistics: ", e);
                
                ChatResponse response = new ChatResponse(
                    "Lỗi tải thống kê\n\nKhông thể tải thống kê. Vui lòng thử lại sau.",
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Thử lại");
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Liên hệ hỗ trợ");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
    
    // XÁC NHẬN LỊCH KHÁM
    private void handleConfirmAppointments(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "XÁC NHẬN LỊCH KHÁM\n\nĐể xác nhận lịch khám chi tiết, vui lòng sử dụng ứng dụng chính.\n\nCác chức năng bao gồm:\n• Xem lịch khám chờ xác nhận\n• Xác nhận hoặc từ chối\n• Ghi chú cho bệnh nhân\n• Thông báo tự động",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Lịch làm việc");
        quickReplies.add("Quản lý bệnh án");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // QUẢN LÝ BỆNH ÁN
    private void handleManageMedicalRecords(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "QUẢN LÝ BỆNH ÁN\n\nĐể quản lý bệnh án chi tiết, vui lòng sử dụng ứng dụng chính.\n\nCác chức năng bao gồm:\n• Tạo bệnh án mới\n• Cập nhật bệnh án\n• Tra cứu bệnh án\n• Xem lịch sử khám\n• Ghi nhận kết quả xét nghiệm",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Xác nhận lịch khám");
        quickReplies.add("Quản lý đơn thuốc");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // QUẢN LÝ ĐỚN THUỐC
    private void handleManagePrescriptions(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "QUẢN LÝ ĐỚN THUỐC\n\nĐể quản lý đơn thuốc chi tiết, vui lòng sử dụng ứng dụng chính.\n\nCác chức năng bao gồm:\n• Kê đơn thuốc mới\n• Chỉnh sửa đơn thuốc\n• Tra cứu đơn thuốc\n• Xem lịch sử kê đơn\n• Kiểm tra tương tác thuốc",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Quản lý bệnh án");
        quickReplies.add("Xác nhận lịch khám");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // NHẬP MÃ KHÁM
    private void handleEnterPatientCode(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "NHẬP MÃ KHÁM\n\nĐể nhập mã khám và tra cứu thông tin bệnh nhân, vui lòng sử dụng ứng dụng chính.\n\nChức năng bao gồm:\n• Nhập mã khám bệnh nhân\n• Xem thông tin bệnh nhân\n• Ghi nhận kết quả khám\n• Kê đơn thuốc\n• Cập nhật bệnh án",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Quản lý bệnh án");
        quickReplies.add("Quản lý đơn thuốc");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // AI ASSISTANT
    private void handleAIAssistant(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "AI ASSISTANT CHO BÁC SĨ\n\nTôi có thể hỗ trợ bạn với:\n\nHỗ trợ chẩn đoán:\n• Phân tích triệu chứng\n• Gợi ý chẩn đoán phân biệt\n• Tư vấn xét nghiệm cần thiết\n\nHỗ trợ điều trị:\n• Gợi ý phác đồ điều trị\n• Kiểm tra tương tác thuốc\n• Tính liều dùng thuốc\n\nTra cứu y khoa:\n• Thông tin bệnh lý\n• Hướng dẫn điều trị\n• Cập nhật y khoa mới\n\nBạn muốn hỏi gì?",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Hỗ trợ chẩn đoán");
        quickReplies.add("Tư vấn điều trị");
        quickReplies.add("Tra cứu y khoa");
        quickReplies.add("Bệnh nhân hôm nay");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // CHAT VỚI BỆNH NHÂN
    private void handleChatWithPatients(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "CHAT VỚI BỆNH NHÂN\n\nĐể chat trực tiếp với bệnh nhân, vui lòng sử dụng ứng dụng chính.\n\nCác tính năng bao gồm:\n• Nhắn tin trực tiếp\n• Gửi hình ảnh\n• Thông báo tức thì\n• Xem lịch sử chat\n• Quản lý cuộc trò chuyện",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Quản lý bệnh án");
        quickReplies.add("Xác nhận lịch khám");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // CHÀO HỎI
    private void handleGreeting(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "Xin chào Bác sĩ!\n\nChúc bạn một ngày làm việc hiệu quả!\n\nTôi có thể hỗ trợ bạn:\n• Xem lịch làm việc hôm nay\n• Danh sách bệnh nhân hôm nay\n• Xem bệnh án đã khám\n• Xác nhận lịch khám\n• Quản lý bệnh án\n• Quản lý đơn thuốc\n• Nhập mã khám\n• Thống kê và báo cáo\n• AI Assistant\n• Chat với bệnh nhân\n\nBạn cần hỗ trợ gì hô,
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Lịch làm việc hôm nay");
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Xem bệnh án");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // CẢM ƠN
    private void handleThanks(ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "Rất vui được hỗ trợ công việc của bạn!\n\nNếu bạn cần hỗ trợ thêm, đừng ngần ngại nhắn tin cho tôi nhé!\n\nChúc bạn làm việc hiệu quả!",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Lịch làm việc hôm nay");
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Thống kê");
        quickReplies.add("AI Assistant");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // KHÔNG HIỂU
    private void handleUnknown(String userMessage, ChatCallback callback) {
        ChatResponse response = new ChatResponse(
            "Xin lỗi, tôi chưa hiểu câu hỏi của bạn.\n\nBạn có thể thử hỏi về:",
            ChatResponse.ResponseType.QUICK_REPLY
        );
        
        List<String> quickReplies = new ArrayList<>();
        quickReplies.add("Lịch làm việc hôm nay");
        quickReplies.add("Bệnh nhân hôm nay");
        quickReplies.add("Xem bệnh án");
        quickReplies.add("Thống kê");
        
        response.setQuickReplies(quickReplies);
        callback.onResponse(response);
    }
    
    // DEBUG - Kiểm tra tất cả lịch khám của bác sĩ
    private void handleDebugAllAppointments(ChatCallback callback) {
        if (maBacSi == null || maBacSi.isEmpty()) {
            ChatResponse response = new ChatResponse(
                "Không tìm thấy thông tin bác sĩ. Vui lòng đăng nhập lại.",
                ChatResponse.ResponseType.TEXT
            );
            callback.onResponse(response);
            return;
        }
        
        Log.d("ChatbotBacSi", "DEBUG: Loading all appointments for maBacSi: " + maBacSi);
        
        repo.getByField("LichKham", "maBacSi", maBacSi,
            querySnapshot -> {
                StringBuilder responseText = new StringBuilder();
                responseText.append("DEBUG - TẤT CẢ LỊCH KHÁM\n\n");
                responseText.append("Mã bác sĩ: ").append(maBacSi).append("\n");
                responseText.append("Tổng số documents: ").append(querySnapshot.size()).append("\n\n");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                
                int count = 0;
                for (var doc : querySnapshot.getDocuments()) {
                    if (count >= 10) break; // Chỉ hiển thị 10 lịch đầu tiên
                    
                    String trangThai = doc.getString("trangThai");
                    Timestamp ngayKhamTs = doc.getTimestamp("ngayKham");
                    String gioKham = doc.getString("gioKham");
                    String maBenhNhan = doc.getString("maBenhNhan");
                    
                    responseText.append("Lịch ").append(count + 1).append(":\n");
                    responseText.append("• Trạng thái: ").append(trangThai != null ? trangThai : "null").append("\n");
                    responseText.append("• Ngày: ").append(ngayKhamTs != null ? dateFormat.format(ngayKhamTs.toDate()) : "null").append("\n");
                    responseText.append("• Giờ: ").append(gioKham != null ? gioKham : "null").append("\n");
                    responseText.append("• Mã BN: ").append(maBenhNhan != null ? maBenhNhan.substring(0, Math.min(8, maBenhNhan.length())) : "null").append("\n\n");
                    
                    count++;
                }
                
                if (querySnapshot.size() > 10) {
                    responseText.append("Và ").append(querySnapshot.size() - 10).append(" lịch khác...\n\n");
                }
                
                responseText.append("Để xem bệnh nhân hôm nay, chỉ lịch có trạng thái XAC_NHAN và ngày hôm nay sẽ được hiển thị.");
                
                ChatResponse response = new ChatResponse(
                    responseText.toString(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Lịch làm việc");
                quickReplies.add("Thống kê");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
                
            }, e -> {
                Log.e("ChatbotBacSi", "Error loading debug appointments: ", e);
                
                ChatResponse response = new ChatResponse(
                    "Lỗi tải debug\n\nKhông thể tải danh sách lịch khám. Lỗi: " + e.getMessage(),
                    ChatResponse.ResponseType.QUICK_REPLY
                );
                
                List<String> quickReplies = new ArrayList<>();
                quickReplies.add("Thử lại");
                quickReplies.add("Bệnh nhân hôm nay");
                quickReplies.add("Liên hệ hỗ trợ");
                
                response.setQuickReplies(quickReplies);
                callback.onResponse(response);
            });
    }
}