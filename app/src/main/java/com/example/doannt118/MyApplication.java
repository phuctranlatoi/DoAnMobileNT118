package com.example.doannt118;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import com.example.doannt118.stringee.StringeeManager;
import com.example.doannt118.ui.VoiceCallActivity;
import com.example.doannt118.ui.VideoCallActivity;
import com.example.doannt118.ui.IncomingCallActivity;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import java.util.HashMap;
import java.util.Map;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    
    // Store incoming calls để Activity có thể lấy
    private static Map<String, StringeeCall> incomingCalls = new HashMap<>();
    private static Map<String, StringeeCall2> incomingVideoCalls = new HashMap<>();
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application created");
        
        // Initialize Stringee
        initializeStringee();
    }
    
    private void initializeStringee() {
        try {
            // Initialize StringeeManager
            StringeeManager stringeeManager = StringeeManager.getInstance(this);
            
            // Set connection callback
            stringeeManager.setConnectionCallback(new StringeeManager.StringeeConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "✅ Stringee connected successfully");
                }
                
                @Override
                public void onDisconnected() {
                    Log.d(TAG, "⚠️ Stringee disconnected");
                }
                
                @Override
                public void onConnectionError(String error) {
                    Log.e(TAG, "❌ Stringee connection error: " + error);
                }
            });
            
            // Set call callback để xử lý incoming voice call
            stringeeManager.setCallCallback(new StringeeManager.StringeeCallCallback() {
                @Override
                public void onCallInitiated(StringeeCall call) {
                    Log.d(TAG, "📞 Incoming voice call from: " + call.getFrom());
                    handleIncomingVoiceCall(call);
                }
                
                @Override
                public void onCallConnected(StringeeCall call) {
                    Log.d(TAG, "📞 Voice call connected");
                }
                
                @Override
                public void onCallEnded(StringeeCall call) {
                    Log.d(TAG, "📞 Voice call ended");
                    incomingCalls.remove(call.getCallId());
                }
                
                @Override
                public void onCallError(StringeeCall call, int code, String description) {
                    Log.e(TAG, "❌ Voice call error: " + code + " - " + description);
                }
            });
            
            // Set call2 callback để xử lý incoming video call
            stringeeManager.setCall2Callback(new StringeeManager.StringeeCall2Callback() {
                @Override
                public void onCall2Initiated(StringeeCall2 call) {
                    Log.d(TAG, "📹 Incoming video call from: " + call.getFrom());
                    handleIncomingVideoCall(call);
                }
                
                @Override
                public void onCall2Connected(StringeeCall2 call) {
                    Log.d(TAG, "📹 Video call connected");
                }
                
                @Override
                public void onCall2Ended(StringeeCall2 call) {
                    Log.d(TAG, "📹 Video call ended");
                    incomingVideoCalls.remove(call.getCallId());
                }
                
                @Override
                public void onCall2Error(StringeeCall2 call, int code, String description) {
                    Log.e(TAG, "❌ Video call error: " + code + " - " + description);
                }
            });
            
            Log.d(TAG, "✅ Stringee initialized with call callbacks");
            
        } catch (Exception e) {
            Log.e(TAG, "💥 Error initializing Stringee: " + e.getMessage());
        }
    }
    
    private void handleIncomingVoiceCall(StringeeCall call) {
        Log.d(TAG, "🔔 === HANDLING INCOMING VOICE CALL ===");
        Log.d(TAG, "🔔 Call ID: " + call.getCallId());
        Log.d(TAG, "🔔 From: " + call.getFrom());
        Log.d(TAG, "🔔 To: " + call.getTo());
        
        // Store call để Activity có thể lấy
        incomingCalls.put(call.getCallId(), call);
        Log.d(TAG, "🔔 Stored incoming voice call with ID: " + call.getCallId());
        
        // Không cần notification vì đã có IncomingCallActivity
        
        // Mở IncomingCallActivity để hiển thị màn hình đổ chuông
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("CALLER_NAME", call.getFrom());
        intent.putExtra("CALLER_ID", call.getFrom());
        intent.putExtra("RECEIVER_ID", call.getTo());
        intent.putExtra("IS_VIDEO_CALL", false);
        intent.putExtra("CALL_ID", call.getCallId());
        
        Log.d(TAG, "🔔 Starting IncomingCallActivity for voice call...");
        startActivity(intent);
        Log.d(TAG, "🔔 IncomingCallActivity started successfully");
    }
    
    private void handleIncomingVideoCall(StringeeCall2 call) {
        Log.d(TAG, "🔔 === HANDLING INCOMING VIDEO CALL ===");
        Log.d(TAG, "🔔 Call ID: " + call.getCallId());
        Log.d(TAG, "🔔 From: " + call.getFrom());
        Log.d(TAG, "🔔 To: " + call.getTo());
        Log.d(TAG, "🔔 Is video call: " + call.isVideoCall());
        
        // Store call để Activity có thể lấy
        incomingVideoCalls.put(call.getCallId(), call);
        Log.d(TAG, "🔔 Stored incoming video call with ID: " + call.getCallId());
        
        // Không cần notification vì đã có IncomingCallActivity
        
        // Mở IncomingCallActivity để hiển thị màn hình đổ chuông
        Intent intent = new Intent(this, IncomingCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("CALLER_NAME", call.getFrom());
        intent.putExtra("CALLER_ID", call.getFrom());
        intent.putExtra("RECEIVER_ID", call.getTo());
        intent.putExtra("IS_VIDEO_CALL", true);
        intent.putExtra("CALL_ID", call.getCallId());
        
        Log.d(TAG, "🔔 Starting IncomingCallActivity for video call...");
        startActivity(intent);
        Log.d(TAG, "🔔 IncomingCallActivity started successfully");
    }
    
    // Static methods để Activity có thể lấy call object
    public static StringeeCall getIncomingCall(String callId) {
        return incomingCalls.get(callId);
    }
    
    public static StringeeCall2 getIncomingVideoCall(String callId) {
        return incomingVideoCalls.get(callId);
    }
    
    public static void removeIncomingCall(String callId) {
        incomingCalls.remove(callId);
    }
    
    public static void removeIncomingVideoCall(String callId) {
        incomingVideoCalls.remove(callId);
    }
    
    /**
     * Method đơn giản để đảm bảo kết nối Stringee khi cần
     */
    public static void ensureStringeeConnection(android.content.Context context) {
        try {
            StringeeManager stringeeManager = StringeeManager.getInstance(context);
            if (!stringeeManager.isConnected()) {
                Log.d("MyApplication", "🔄 Ensuring Stringee connection...");
                stringeeManager.connectCurrentUser();
            } else {
                Log.d("MyApplication", "✅ Stringee already connected");
            }
        } catch (Exception e) {
            Log.e("MyApplication", "❌ Error ensuring connection: " + e.getMessage());
        }
    }

}