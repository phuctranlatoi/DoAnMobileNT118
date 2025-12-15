package com.example.doannt118.stringee;

import android.content.Context;
import android.util.Log;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;
import com.stringee.listener.StatusListener;
import com.stringee.common.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class StringeeManager {
    private static final String TAG = "StringeeManager";
    
    // Stringee credentials - CHÍNH XÁC từ user
    private static final String STRINGEE_SID_KEY = "SK.0.uHNIGYBHHRcU5J0hjrSSky4nzdXvAbso";
    private static final String STRINGEE_SECRET_KEY = "TnNsaE1UZWJRRXJxUDZnMWdMMTYxaUdRdEszbnpwYkY=";
    
    private static StringeeManager instance;
    private StringeeClient stringeeClient;
    private Context context;
    private boolean isConnected = false;
    
    // Callbacks
    public interface StringeeConnectionCallback {
        void onConnected();
        void onDisconnected();
        void onConnectionError(String error);
    }
    
    public interface StringeeCallCallback {
        void onCallInitiated(StringeeCall call);
        void onCallConnected(StringeeCall call);
        void onCallEnded(StringeeCall call);
        void onCallError(StringeeCall call, int code, String description);
    }
    
    public interface StringeeCall2Callback {
        void onCall2Initiated(StringeeCall2 call);
        void onCall2Connected(StringeeCall2 call);
        void onCall2Ended(StringeeCall2 call);
        void onCall2Error(StringeeCall2 call, int code, String description);
    }
    
    private StringeeConnectionCallback connectionCallback;
    private StringeeCallCallback callCallback;
    private StringeeCall2Callback call2Callback;
    
    private StringeeManager(Context context) {
        this.context = context.getApplicationContext();
        initStringeeClient();
        // Tự động kết nối khi khởi tạo
        connectCurrentUser();
    }
    
    public static synchronized StringeeManager getInstance(Context context) {
        if (instance == null) {
            instance = new StringeeManager(context);
        }
        return instance;
    }
    
    private void initStringeeClient() {
        stringeeClient = new StringeeClient(context);
        
        // 🔥 CRITICAL: Set Stringee host servers (this was missing!)
        List<SocketAddress> socketAddressList = new ArrayList<>();
        socketAddressList.add(new SocketAddress("v1.stringee.com", 9879));
        socketAddressList.add(new SocketAddress("v2.stringee.com", 9879));
        stringeeClient.setHost(socketAddressList);
        
        Log.d(TAG, "🌐 Configured Stringee hosts: v1.stringee.com, v2.stringee.com");
        
        stringeeClient.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient stringeeClient, boolean isReconnecting) {
                Log.d(TAG, "🎉 Stringee connected successfully! isReconnecting: " + isReconnecting);
                Log.d(TAG, "User ID: " + stringeeClient.getUserId());
                isConnected = true;
                if (connectionCallback != null) {
                    connectionCallback.onConnected();
                }
            }
            
            @Override
            public void onConnectionDisconnected(StringeeClient stringeeClient, boolean isReconnecting) {
                Log.d(TAG, "Stringee disconnected, isReconnecting: " + isReconnecting);
                isConnected = false;
                if (connectionCallback != null) {
                    connectionCallback.onDisconnected();
                }
            }
            
            @Override
            public void onIncomingCall(StringeeCall stringeeCall) {
                Log.d(TAG, "Incoming call from: " + stringeeCall.getFrom());
                if (callCallback != null) {
                    callCallback.onCallInitiated(stringeeCall);
                }
            }
            
            @Override
            public void onIncomingCall2(StringeeCall2 stringeeCall2) {
                Log.d(TAG, "Incoming video call from: " + stringeeCall2.getFrom());
                if (call2Callback != null) {
                    call2Callback.onCall2Initiated(stringeeCall2);
                }
            }
            
            @Override
            public void onConnectionError(StringeeClient stringeeClient, StringeeError stringeeError) {
                Log.e(TAG, "❌ Connection error: " + stringeeError.getMessage());
                Log.e(TAG, "❌ Error code: " + stringeeError.getCode());
                
                // Provide more specific error messages
                String errorMessage = stringeeError.getMessage();
                if (stringeeError.getCode() == -1) {
                    errorMessage = "Authentication failed - Kiểm tra lại API keys";
                } else if (stringeeError.getCode() == -2) {
                    errorMessage = "Network error - Kiểm tra kết nối internet";
                } else if (stringeeError.getCode() == -3) {
                    errorMessage = "Invalid token - Token không hợp lệ";
                }
                
                isConnected = false;
                if (connectionCallback != null) {
                    connectionCallback.onConnectionError(errorMessage);
                }
            }
            
            @Override
            public void onRequestNewToken(StringeeClient stringeeClient) {
                Log.d(TAG, "Request new token");
                // Generate access token using your server
                generateAccessToken();
            }
            
            @Override
            public void onTopicMessage(String from, JSONObject msg) {
                Log.d(TAG, "Topic message from: " + from + ", message: " + msg.toString());
            }
            
            @Override
            public void onCustomMessage(String from, JSONObject msg) {
                Log.d(TAG, "Custom message from: " + from + ", message: " + msg.toString());
            }
        });
    }
    
    private void generateAccessToken() {
        try {
            Log.d(TAG, "🚀 === BẮT ĐẦU TẠO ACCESS TOKEN ===");
            
            // Validate API keys first
            if (!validateApiKeys()) {
                Log.e(TAG, "❌ Invalid API keys");
                if (connectionCallback != null) {
                    connectionCallback.onConnectionError("Invalid API keys configuration");
                }
                return;
            }
            
            // Generate access token using local implementation
            String userId = getCurrentUserId();
            Log.d(TAG, "🔑 Generating token for userId: " + userId);
            Log.d(TAG, "🔑 SID Key: " + STRINGEE_SID_KEY);
            Log.d(TAG, "🔑 Secret Key: " + STRINGEE_SECRET_KEY);
            Log.d(TAG, "🔑 Secret Key length: " + STRINGEE_SECRET_KEY.length());
            
            if (userId != null && !userId.isEmpty()) {
                // Thử tạo token với debug chi tiết
                String accessToken = StringeeTokenGenerator.generateAccessToken(userId);
                
                if (accessToken != null && !accessToken.isEmpty()) {
                    Log.d(TAG, "✅ Generated access token successfully!");
                    Log.d(TAG, "📏 Token length: " + accessToken.length());
                    Log.d(TAG, "🔗 Full token: " + accessToken);
                    
                    // Validate token format
                    String[] tokenParts = accessToken.split("\\.");
                    Log.d(TAG, "🔍 Token parts count: " + tokenParts.length);
                    if (tokenParts.length == 3) {
                        Log.d(TAG, "🔍 Header: " + tokenParts[0]);
                        Log.d(TAG, "🔍 Payload: " + tokenParts[1]);
                        Log.d(TAG, "🔍 Signature: " + tokenParts[2]);
                    }
                    
                    Log.d(TAG, "🌐 Connecting to Stringee with token...");
                    stringeeClient.connect(accessToken);
                } else {
                    Log.e(TAG, "❌ Failed to generate access token - token is null or empty");
                    if (connectionCallback != null) {
                        connectionCallback.onConnectionError("Failed to generate access token");
                    }
                }
            } else {
                Log.e(TAG, "❌ User ID is null or empty");
                if (connectionCallback != null) {
                    connectionCallback.onConnectionError("User ID is required");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 Error generating access token: " + e.getMessage());
            e.printStackTrace();
            if (connectionCallback != null) {
                connectionCallback.onConnectionError("Error generating token: " + e.getMessage());
            }
        }
    }
    
    private boolean validateApiKeys() {
        if (STRINGEE_SID_KEY == null || STRINGEE_SID_KEY.isEmpty()) {
            Log.e(TAG, "❌ SID Key is null or empty");
            return false;
        }
        
        if (!STRINGEE_SID_KEY.startsWith("SK.")) {
            Log.e(TAG, "❌ SID Key format invalid - should start with 'SK.'");
            return false;
        }
        
        if (STRINGEE_SECRET_KEY == null || STRINGEE_SECRET_KEY.isEmpty()) {
            Log.e(TAG, "❌ Secret Key is null or empty");
            return false;
        }
        
        // Try to decode secret key to validate base64 format
        try {
            android.util.Base64.decode(STRINGEE_SECRET_KEY, android.util.Base64.DEFAULT);
            Log.d(TAG, "✅ API keys validation passed");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "❌ Secret Key is not valid base64: " + e.getMessage());
            return false;
        }
    }
    
    private String getCurrentUserId() {
        // First check if we have a stored user ID from connect()
        android.content.SharedPreferences stringeePrefs = context.getSharedPreferences("stringee_info", Context.MODE_PRIVATE);
        String storedUserId = stringeePrefs.getString("current_user_id", "");
        if (!storedUserId.isEmpty()) {
            Log.d(TAG, "🆔 Using stored userId: " + storedUserId);
            return storedUserId;
        }
        
        // Get current user ID from SharedPreferences or other storage
        android.content.SharedPreferences prefs = context.getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String maBenhNhan = prefs.getString("maBenhNhan", "");
        String maBacSi = prefs.getString("maBacSi", "");
        
        Log.d(TAG, "🆔 From SharedPreferences - maBenhNhan: " + maBenhNhan + ", maBacSi: " + maBacSi);
        
        // Return the appropriate user ID
        String userId;
        if (!maBenhNhan.isEmpty()) {
            userId = "patient_" + maBenhNhan;
        } else if (!maBacSi.isEmpty()) {
            userId = "doctor_" + maBacSi;
        } else {
            // Fallback to a default user ID for testing
            userId = "user_" + System.currentTimeMillis();
            Log.w(TAG, "⚠️ Using fallback userId: " + userId);
        }
        
        Log.d(TAG, "🆔 Generated userId: " + userId);
        return userId;
    }
    
    public void connect(String userId) {
        if (stringeeClient != null && !isConnected) {
            Log.d(TAG, "Connecting to Stringee with userId: " + userId);
            // Store userId for token generation
            android.content.SharedPreferences prefs = context.getSharedPreferences("stringee_info", Context.MODE_PRIVATE);
            prefs.edit().putString("current_user_id", userId).apply();
            generateAccessToken();
        }
    }
    
    public void connectCurrentUser() {
        if (stringeeClient != null) {
            Log.d(TAG, "Connecting current user to Stringee");
            generateAccessToken();
        }
    }
    
    /**
     * Test method để thử kết nối với token đơn giản
     */
    public void testConnection() {
        Log.d(TAG, "🧪 === TEST CONNECTION ===");
        
        // Thử tạo một token đơn giản để test
        String testUserId = "test_user_" + System.currentTimeMillis();
        Log.d(TAG, "🧪 Test userId: " + testUserId);
        
        try {
            String testToken = StringeeTokenGenerator.generateAccessToken(testUserId);
            if (testToken != null && !testToken.isEmpty()) {
                Log.d(TAG, "🧪 Test token created: " + testToken);
                Log.d(TAG, "🧪 Attempting connection...");
                stringeeClient.connect(testToken);
            } else {
                Log.e(TAG, "🧪 ❌ Failed to create test token");
            }
        } catch (Exception e) {
            Log.e(TAG, "🧪 ❌ Exception in test connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test với hardcoded token từ Stringee Dashboard (để debug)
     */
    public void testWithHardcodedToken() {
        Log.d(TAG, "🧪 === TEST WITH HARDCODED TOKEN ===");
        
        // TODO: Thay thế bằng token từ Stringee Dashboard -> Tools -> Generate Access Token
        // String hardcodedToken = "YOUR_HARDCODED_TOKEN_HERE";
        
        Log.d(TAG, "🧪 Để test nhanh, hãy:");
        Log.d(TAG, "🧪 1. Vào Stringee Dashboard -> Tools -> Generate Access Token");
        Log.d(TAG, "🧪 2. Tạo token với userId: test_user");
        Log.d(TAG, "🧪 3. Copy token và thay vào method này");
        Log.d(TAG, "🧪 4. Uncomment dòng stringeeClient.connect(hardcodedToken)");
        
        // stringeeClient.connect(hardcodedToken);
    }
    
    public void forceReconnect() {
        if (stringeeClient != null) {
            Log.d(TAG, "Force reconnecting to Stringee");
            isConnected = false;
            generateAccessToken();
        }
    }
    
    public void disconnect() {
        if (stringeeClient != null && isConnected) {
            Log.d(TAG, "Disconnecting from Stringee");
            stringeeClient.disconnect();
        }
    }
    
    public boolean isConnected() {
        return isConnected;
    }
    
    /**
     * Tạo StringeeCall object cho voice call
     * Lưu ý: Method này KHÔNG gọi makeCall(), Activity sẽ tự gọi
     */
    @Deprecated
    public StringeeCall makeVoiceCall(String fromUserId, String toUserId) {
        if (!isConnected) {
            Log.e(TAG, "❌ Stringee not connected");
            return null;
        }
        
        try {
            Log.d(TAG, "🎯 Creating voice call from: " + fromUserId + " to: " + toUserId);
            
            // Tạo StringeeCall theo API thực tế: StringeeCall(client, from, to)
            StringeeCall call = new StringeeCall(stringeeClient, fromUserId, toUserId);
            
            Log.d(TAG, "🎯 Voice call object created (not yet initiated)");
            return call;
            
        } catch (Exception e) {
            Log.e(TAG, "💥 Error creating voice call: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo StringeeCall2 object cho video call
     * Lưu ý: Method này KHÔNG gọi makeCall(), Activity sẽ tự gọi
     */
    @Deprecated
    public StringeeCall2 makeVideoCall(String fromUserId, String toUserId) {
        if (!isConnected) {
            Log.e(TAG, "❌ Stringee not connected");
            return null;
        }
        
        try {
            Log.d(TAG, "🎯 Creating video call from: " + fromUserId + " to: " + toUserId);
            
            // Tạo StringeeCall2 theo API thực tế: StringeeCall2(client, from, to)
            StringeeCall2 call = new StringeeCall2(stringeeClient, fromUserId, toUserId);
            
            // Set video call
            call.setVideoCall(true);
            
            Log.d(TAG, "🎯 Video call object created (not yet initiated)");
            return call;
            
        } catch (Exception e) {
            Log.e(TAG, "💥 Error creating video call: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Setters for callbacks
    public void setConnectionCallback(StringeeConnectionCallback callback) {
        this.connectionCallback = callback;
    }
    
    public void setCallCallback(StringeeCallCallback callback) {
        this.callCallback = callback;
    }
    
    public void setCall2Callback(StringeeCall2Callback callback) {
        this.call2Callback = callback;
    }
    
    public StringeeClient getStringeeClient() {
        return stringeeClient;
    }
    

}