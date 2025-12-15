package com.example.doannt118.stringee;

import android.util.Base64;
import android.util.Log;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class StringeeTokenGenerator {
    private static final String TAG = "StringeeTokenGenerator";
    
    // Stringee credentials - API Keys từ user
    // SID Key format: SK.0.xxxxx
    // Secret Key: Base64 encoded string
    private static final String STRINGEE_SID_KEY = "SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso";
    private static final String STRINGEE_SECRET_KEY = "TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY=";
    
    // 🔥 HARDCODED TOKEN FOR TESTING - Tạo từ Stringee Dashboard
    // Vào: https://developer.stringee.com/project -> Tools -> Generate Access Token
    // Nhập userId và copy token vào đây để test
    private static final String HARDCODED_TEST_TOKEN = null; // Set token here to test
    
    /**
     * Generate JWT access token for Stringee
     * @param userId User ID for the token
     * @return JWT token string
     */
    public static String generateAccessToken(String userId) {
        try {
            Log.d(TAG, "🚀 === BẮT ĐẦU TẠO JWT TOKEN ===");
            Log.d(TAG, "🔑 UserId: " + userId);
            
            // 🔥 NẾU CÓ HARDCODED TOKEN, SỬ DỤNG NÓ ĐỂ TEST
            if (HARDCODED_TEST_TOKEN != null && !HARDCODED_TEST_TOKEN.isEmpty()) {
                Log.d(TAG, "🔥 Using HARDCODED_TEST_TOKEN for testing");
                return HARDCODED_TEST_TOKEN;
            }
            
            Log.d(TAG, "🔑 SID Key: " + STRINGEE_SID_KEY);
            Log.d(TAG, "🔑 Secret Key: " + STRINGEE_SECRET_KEY);
            
            // Kiểm tra API keys
            if (STRINGEE_SID_KEY == null || STRINGEE_SID_KEY.isEmpty()) {
                Log.e(TAG, "❌ SID Key is null or empty!");
                return null;
            }
            
            if (STRINGEE_SECRET_KEY == null || STRINGEE_SECRET_KEY.isEmpty()) {
                Log.e(TAG, "❌ Secret Key is null or empty!");
                return null;
            }
            
            // JWT Header - ĐÚNG FORMAT STRINGEE
            JSONObject header = new JSONObject();
            header.put("typ", "JWT");
            header.put("alg", "HS256");
            header.put("cty", "stringee-api;v=1");
            
            // JWT Payload - ĐÚNG FORMAT STRINGEE
            long currentTime = System.currentTimeMillis() / 1000;
            long expTime = currentTime + 3600; // Token expires in 1 hour
            
            JSONObject payload = new JSONObject();
            payload.put("jti", STRINGEE_SID_KEY + "-" + currentTime);
            payload.put("iss", STRINGEE_SID_KEY);
            payload.put("exp", expTime);
            payload.put("userId", userId);
            
            Log.d(TAG, "📋 JWT Header: " + header.toString());
            Log.d(TAG, "📋 JWT Payload: " + payload.toString());
            
            // Encode header and payload
            String encodedHeader = base64UrlEncode(header.toString());
            String encodedPayload = base64UrlEncode(payload.toString());
            
            Log.d(TAG, "🔤 Encoded Header: " + encodedHeader);
            Log.d(TAG, "🔤 Encoded Payload: " + encodedPayload);
            
            // Create signature
            String data = encodedHeader + "." + encodedPayload;
            Log.d(TAG, "📝 Data to sign: " + data);
            
            String signature = createSignature(data, STRINGEE_SECRET_KEY);
            
            if (signature == null || signature.isEmpty()) {
                Log.e(TAG, "❌ Failed to create signature - signature is null or empty");
                return null;
            }
            
            Log.d(TAG, "✍️ Signature: " + signature);
            
            // Combine to create JWT
            String jwt = data + "." + signature;
            
            Log.d(TAG, "🎯 === HOÀN THÀNH TẠO JWT TOKEN ===");
            Log.d(TAG, "✅ JWT Token: " + jwt);
            Log.d(TAG, "📏 Token length: " + jwt.length());
            
            // Validate JWT format
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                Log.e(TAG, "❌ Invalid JWT format - should have 3 parts, got: " + parts.length);
                return null;
            }
            
            return jwt;
            
        } catch (Exception e) {
            Log.e(TAG, "💥 EXCEPTION in generateAccessToken: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static String base64UrlEncode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
        return encoded;
    }
    
    private static String createSignature(String data, String secret) {
        try {
            Log.d(TAG, "🔐 === BẮT ĐẦU TẠO SIGNATURE ===");
            Log.d(TAG, "🔐 Data to sign: " + data);
            Log.d(TAG, "🔐 Secret key: " + secret);
            Log.d(TAG, "🔐 Secret length: " + secret.length());
            
            // THỬ CÁCH 1: Sử dụng secret key trực tiếp (raw string)
            byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
            Log.d(TAG, "🔐 Method 1: Using raw secret string, bytes length: " + secretBytes.length);
            
            String signature1 = createHmacSignature(data, secretBytes);
            if (signature1 != null && !signature1.isEmpty()) {
                Log.d(TAG, "🔐 ✅ Signature created with raw secret: " + signature1);
                return signature1;
            }
            
            // THỬ CÁCH 2: Decode secret từ base64
            Log.d(TAG, "🔐 Method 2: Trying base64 decoded secret...");
            try {
                byte[] decodedSecretBytes = Base64.decode(secret, Base64.DEFAULT);
                Log.d(TAG, "🔐 Base64 decoded secret bytes length: " + decodedSecretBytes.length);
                
                String signature2 = createHmacSignature(data, decodedSecretBytes);
                if (signature2 != null && !signature2.isEmpty()) {
                    Log.d(TAG, "🔐 ✅ Signature created with base64 decoded secret: " + signature2);
                    return signature2;
                }
            } catch (Exception e) {
                Log.e(TAG, "🔐 Base64 decode failed: " + e.getMessage());
            }
            
            Log.e(TAG, "❌ All signature methods failed");
            return "";
            
        } catch (Exception e) {
            Log.e(TAG, "💥 EXCEPTION in createSignature: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    private static String createHmacSignature(String data, byte[] secretBytes) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Encode signature to base64 URL safe
            String signature = Base64.encodeToString(signatureBytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            
            return signature;
        } catch (Exception e) {
            Log.e(TAG, "❌ createHmacSignature failed: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Validate if a token is still valid (not expired)
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public static boolean isTokenValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            
            // Decode payload
            String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
            JSONObject payload = new JSONObject(payloadJson);
            
            long expTime = payload.getLong("exp");
            long currentTime = System.currentTimeMillis() / 1000;
            
            return currentTime < expTime;
            
        } catch (Exception e) {
            Log.e(TAG, "Error validating token: " + e.getMessage());
            return false;
        }
    }
}