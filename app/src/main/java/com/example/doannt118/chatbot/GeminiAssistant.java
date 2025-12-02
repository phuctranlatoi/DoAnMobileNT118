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
 * GEMINI ASSISTANT - CHỈ DÙNG KHI CẦN
 * 
 * Fallback khi rule-based không handle được
 * Giảm thiểu API calls để tiết kiệm quota
 */
public class GeminiAssistant {
    
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";
    private static final String API_KEY = "AIzaSyDV_NQJ6TdqhPVnSKWsDCzEcjl6MQd8Uk4";
    
    private ExecutorService executor;
    private Handler mainHandler;
    
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public GeminiAssistant(Context context) {
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Gọi Gemini API - CHỈ DÙNG KHI THỰC SỰ CẦN
     */
    public void ask(String question, String context, GeminiCallback callback) {
        executor.execute(() -> {
            try {
                // Build full prompt (system + user question)
                String systemPrompt = buildSystemPrompt(context);
                String fullPrompt = systemPrompt + "\n\nCâu hỏi: " + question;
                
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
                
                // Make API call
                URL url = new URL(API_URL + "?key=" + API_KEY);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                
                // Send request
                OutputStream os = conn.getOutputStream();
                os.write(requestBody.toString().getBytes());
                os.flush();
                os.close();
                
                // Read response
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    br.close();
                    
                    // Parse response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String text = jsonResponse
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");
                    
                    // Return on main thread
                    mainHandler.post(() -> callback.onSuccess(text));
                    
                } else {
                    mainHandler.post(() -> callback.onError("API Error: " + responseCode));
                }
                
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    /**
     * Build system prompt cho Gemini
     */
    private String buildSystemPrompt(String userContext) {
        return "Bạn là trợ lý y tế AI của phòng khám.\n\n" +
               "CONTEXT:\n" + userContext + "\n\n" +
               "QUY TẮC:\n" +
               "- Trả lời NGẮN GỌN (2-3 câu)\n" +
               "- Lịch sự, thân thiện\n" +
               "- Dùng emoji phù hợp\n" +
               "- KHÔNG chẩn đoán bệnh\n" +
               "- Khuyên gặp bác sĩ nếu nghiêm trọng\n\n" +
               "Trả lời câu hỏi sau:";
    }
}
