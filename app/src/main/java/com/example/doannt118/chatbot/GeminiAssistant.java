package com.example.doannt118.chatbot;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * GEMINI ASSISTANT - ENHANCED VERSION
 * 
 * Improved AI integration with better context awareness
 * Enhanced Vietnamese language processing
 * Smart fallback system with conversation memory
 */
public class GeminiAssistant {
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final String API_KEY = "AIzaSyDV_NQJ6TdqhPVnSKWsDCzEcjl6MQd8Uk4";
    
    private ExecutorService executor;
    private Handler mainHandler;
    private Context context;
    
    // Enhanced conversation memory
    private StringBuilder conversationHistory;
    private int maxHistoryLength = 2000;
    
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public GeminiAssistant(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.conversationHistory = new StringBuilder();
    }
    
    /**
     * Enhanced ask method with conversation memory and better context
     */
    public void ask(String question, String context, GeminiCallback callback) {
        ask(question, context, null, callback);
    }
    
    /**
     * Ask with user type for better personalization
     */
    public void ask(String question, String context, String userType, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                // Build enhanced prompt with conversation history
                String systemPrompt = buildEnhancedSystemPrompt(context, userType);
                String fullPrompt = systemPrompt + "\n\n" + getRecentHistory() + "\nCâu hỏi mới: " + question;
                
                // Add to conversation history
                addToHistory("User: " + question);
                
                // Build request (Gemini format)
                JSONObject requestBody = new JSONObject();
                JSONArray contents = new JSONArray();
                
                // Single user message with full prompt
                JSONObject userMessage = new JSONObject();
                JSONArray userParts = new JSONArray();
                userParts.put(new JSONObject().put("text", fullPrompt));
                userMessage.put("parts", userParts);
                contents.put(userMessage);
                
                requestBody.put("contents", contents);
                
                // Enhanced generation config for better Vietnamese responses
                JSONObject generationConfig = new JSONObject();
                generationConfig.put("temperature", 0.7);
                generationConfig.put("topK", 40);
                generationConfig.put("topP", 0.95);
                generationConfig.put("maxOutputTokens", 1024);
                requestBody.put("generationConfig", generationConfig);
                
                // Safety settings for medical content
                JSONArray safetySettings = new JSONArray();
                JSONObject medicalSafety = new JSONObject();
                medicalSafety.put("category", "HARM_CATEGORY_DANGEROUS_CONTENT");
                medicalSafety.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
                safetySettings.put(medicalSafety);
                requestBody.put("safetySettings", safetySettings);
                
                // Make API call with enhanced error handling
                URL url = new URL(API_URL + "?key=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("User-Agent", "MediBot/2.0");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                // Send request
                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                
                // Read response
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    
                    // Parse response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String rawText = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
                    
                    // Post-process response for better Vietnamese
                    final String text = enhanceVietnameseResponse(rawText);
                    
                    // Add to conversation history
                    addToHistory("Assistant: " + text);
                    
                    // Return on main thread
                    mainHandler.post(() -> callback.onSuccess(text));
                    
                } else {
                    // Enhanced error handling
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    errorReader.close();
                    
                    String defaultErrorMessage = "API Error " + responseCode;
                    if (errorResponse.length() > 0) {
                        try {
                            JSONObject errorJson = new JSONObject(errorResponse.toString());
                            if (errorJson.has("error")) {
                                defaultErrorMessage = errorJson.getJSONObject("error").getString("message");
                            }
                        } catch (Exception e) {
                            // Use default error message
                        }
                    }
                    
                    final String errorMessage = defaultErrorMessage;
                    mainHandler.post(() -> callback.onError(errorMessage));
                }
                
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Lỗi kết nối: " + e.getMessage()));
            }
        });
    }
    
    /**
     * ENHANCED: Ask with medical context for better health advice
     */
    public void askMedical(String question, String symptoms, String medicalHistory, String userType, GeminiCallback callback) {
        String medicalContext = buildMedicalContext(userType, symptoms, medicalHistory);
        ask(question, medicalContext, userType, callback);
    }
    
    /**
     * ENHANCED: Quick health check with symptom analysis
     */
    public void analyzeSymptoms(String symptoms, String userType, GeminiCallback callback) {
        String question = "Phân tích triệu chứng: " + symptoms + ". Đưa ra lời khuyên sơ bộ và khi nào cần gặp bác sĩ.";
        String context = "PHÂN TÍCH TRIỆU CHỨNG - Cần tư vấn y tế cẩn thận và khuyên gặp bác sĩ khi cần thiết.";
        ask(question, context, userType, callback);
    }
    
    /**
     * Build enhanced system prompt with better Vietnamese context
     */
    private String buildEnhancedSystemPrompt(String userContext, String userType) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Bạn là MediBot - trợ lý y tế AI thông minh của phòng khám.\n\n");
        
        // Role-specific instructions
        if ("bacsi".equals(userType)) {
            prompt.append("NGƯỜI DÙNG: Bác sĩ\n");
            prompt.append("VAI TRÒ: Hỗ trợ chuyên môn y khoa\n");
            prompt.append("CHỨC NĂNG:\n");
            prompt.append("- Hỗ trợ chẩn đoán và phân tích triệu chứng\n");
            prompt.append("- Tư vấn phác đồ điều trị cập nhật\n");
            prompt.append("- Kiểm tra tương tác thuốc và liều lượng\n");
            prompt.append("- Tra cứu thông tin y khoa chuyên sâu\n");
            prompt.append("- Gợi ý xét nghiệm và chẩn đoán phân biệt\n");
            prompt.append("- Cập nhật guideline điều trị mới nhất\n\n");
        } else {
            prompt.append("NGƯỜI DÙNG: Bệnh nhân\n");
            prompt.append("VAI TRÒ: Tư vấn sức khỏe cơ bản\n");
            prompt.append("CHỨC NĂNG:\n");
            prompt.append("- Tư vấn sức khỏe tổng quát và phòng bệnh\n");
            prompt.append("- Hướng dẫn chăm sóc sức khỏe hàng ngày\n");
            prompt.append("- Giải thích về bệnh lý phổ biến dễ hiểu\n");
            prompt.append("- Khuyến cáo khi nào cần gặp bác sĩ khẩn cấp\n");
            prompt.append("- Hướng dẫn sơ cứu cơ bản\n");
            prompt.append("- Tư vấn dinh dưỡng và lối sống lành mạnh\n\n");
        }
        
        prompt.append("CONTEXT:\n").append(userContext).append("\n\n");
        
        prompt.append("QUY TẮC QUAN TRỌNG:\n");
        prompt.append("1. 🇻🇳 Trả lời bằng tiếng Việt tự nhiên, thân thiện\n");
        prompt.append("2. 😊 Sử dụng emoji phù hợp để tạo cảm xúc tích cực\n");
        prompt.append("3. 📝 Trả lời NGẮN GỌN (2-4 câu), dễ hiểu, có cấu trúc\n");
        prompt.append("4. ⚠️ KHÔNG chẩn đoán bệnh chính xác - chỉ tư vấn sơ bộ\n");
        prompt.append("5. 👨‍⚕️ Luôn khuyên gặp bác sĩ khi cần thiết\n");
        prompt.append("6. 📚 Sử dụng thuật ngữ y khoa đơn giản, giải thích rõ ràng\n");
        prompt.append("7. ❤️ Thể hiện sự quan tâm và đồng cảm\n");
        prompt.append("8. 🤔 Nếu không chắc chắn, thành thật nói 'Tôi không chắc'\n");
        prompt.append("9. 🚨 Với triệu chứng nghiêm trọng, khuyên đi khám ngay\n");
        prompt.append("10. 💡 Đưa ra lời khuyên thực tế và có thể thực hiện\n\n");
        
        prompt.append("PHONG CÁCH:\n");
        prompt.append("- Lịch sự, chuyên nghiệp nhưng gần gũi như bạn bè\n");
        prompt.append("- Tích cực, động viên tinh thần, truyền cảm hứng\n");
        prompt.append("- Cung cấp thông tin hữu ích và thực tế\n");
        prompt.append("- Tránh gây lo lắng không cần thiết\n");
        prompt.append("- Khuyến khích lối sống lành mạnh\n\n");
        
        prompt.append("CẤU TRÚC PHẢN HỒI:\n");
        prompt.append("- Bắt đầu với emoji phù hợp\n");
        prompt.append("- Tóm tắt vấn đề (1 câu)\n");
        prompt.append("- Đưa ra lời khuyên chính (2-3 câu)\n");
        prompt.append("- Kết thúc với lời động viên hoặc khuyến cáo\n\n");
        
        return prompt.toString();
    }
    
    /**
     * Enhance Vietnamese response quality
     */
    private String enhanceVietnameseResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "Xin lỗi, tôi không thể trả lời câu hỏi này. Vui lòng thử lại! 😊";
        }
        
        // Clean up common formatting issues
        response = response.trim();
        
        // Ensure proper Vietnamese punctuation
        response = response.replaceAll("\\s+", " ");
        response = response.replaceAll("\\s+([,.!?])", "$1");
        
        // Add friendly closing if response is too clinical
        if (!response.contains("😊") && !response.contains("🙂") && 
            !response.contains("💡") && !response.contains("❤️")) {
            if (response.length() > 100) {
                response += " 😊";
            }
        }
        
        return response;
    }
    
    /**
     * Conversation history management
     */
    private void addToHistory(String message) {
        conversationHistory.append(message).append("\n");
        
        // Trim history if too long
        if (conversationHistory.length() > maxHistoryLength) {
            String history = conversationHistory.toString();
            int cutPoint = history.indexOf("\n", history.length() - maxHistoryLength);
            if (cutPoint > 0) {
                conversationHistory = new StringBuilder(history.substring(cutPoint + 1));
            }
        }
    }
    
    private String getRecentHistory() {
        String history = conversationHistory.toString().trim();
        if (history.isEmpty()) {
            return "";
        }
        return "Lịch sử trò chuyện gần đây:\n" + history + "\n";
    }
    
    /**
     * Clear conversation history
     */
    public void clearHistory() {
        conversationHistory.setLength(0);
    }
    
    /**
     * Medical advice detection with Vietnamese patterns
     */
    public boolean isMedicalAdvice(String message) {
        String lower = message.toLowerCase();
        return lower.contains("bị") || 
               lower.contains("đau") || 
               lower.contains("sốt") ||
               lower.contains("nên làm gì") ||
               lower.contains("triệu chứng") ||
               lower.contains("bệnh") ||
               lower.contains("thuốc") ||
               lower.contains("điều trị") ||
               lower.contains("khám") ||
               lower.contains("chữa") ||
               lower.contains("uống") ||
               lower.contains("dùng") ||
               lower.contains("liều") ||
               lower.contains("tác dụng phụ");
    }
    
    /**
     * Enhanced medical context builder
     */
    public String buildMedicalContext(String userType, String symptoms, String medicalHistory) {
        StringBuilder context = new StringBuilder();
        context.append("THÔNG TIN Y TẾ:\n");
        context.append("- Loại người dùng: ").append(userType).append("\n");
        
        if (symptoms != null && !symptoms.trim().isEmpty()) {
            context.append("- Triệu chứng: ").append(symptoms).append("\n");
        }
        
        if (medicalHistory != null && !medicalHistory.trim().isEmpty()) {
            context.append("- Tiền sử bệnh: ").append(medicalHistory).append("\n");
        }
        
        context.append("- Thời gian: ").append(new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(new java.util.Date())).append("\n");
        
        return context.toString();
    }
}
