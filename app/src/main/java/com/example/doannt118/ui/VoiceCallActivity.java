package com.example.doannt118.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.doannt118.R;
import com.example.doannt118.stringee.StringeeManager;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.common.StringeeAudioManager;
import de.hdodenhof.circleimageview.CircleImageView;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VoiceCallActivity extends AppCompatActivity {
    private static final String TAG = "VoiceCallActivity";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private TextView tvCallerName, tvCallStatus, tvCallDuration;
    private CircleImageView ivCallerAvatar;
    private ImageButton btnEndCall, btnMute, btnSpeaker;
    
    private StringeeCall stringeeCall;
    private StringeeManager stringeeManager;
    private StringeeAudioManager audioManager;
    private boolean isMuted = false;
    private boolean isSpeakerOn = false;
    private boolean isIncomingCall = false;
    private boolean isMediaConnected = false;
    
    private Handler callDurationHandler;
    private Runnable callDurationRunnable;
    private long callStartTime = 0;
    
    // Intent extras
    private String callerName;
    private String callerId;
    private String receiverId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        
        initViews();
        getDataFromIntent();
        setupStringee();
        setupListeners();
        
        // Setup back button handling
        setupBackPressedHandler();
        
        // Check permissions before making/receiving call
        if (checkAndRequestPermissions()) {
            startCall();
        }
    }
    
    private void startCall() {
        if (isIncomingCall) {
            // Incoming call đã được answer từ IncomingCallActivity
            // Chỉ cần setup call
            handleIncomingCall();
        } else {
            // Outgoing call
            makeOutgoingCall();
        }
    }
    
    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        
        // Check RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        
        // Check BLUETOOTH_CONNECT for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }
        
        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                    permissionsNeeded.toArray(new String[0]), 
                    PERMISSION_REQUEST_CODE);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                startCall();
            } else {
                Toast.makeText(this, "Cần cấp quyền microphone để thực hiện cuộc gọi", 
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void initViews() {
        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        tvCallDuration = findViewById(R.id.tvCallDuration);
        ivCallerAvatar = findViewById(R.id.ivCallerAvatar);
        btnEndCall = findViewById(R.id.btnEndCall);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        
        // Setup call duration handler
        callDurationHandler = new Handler();
        callDurationRunnable = new Runnable() {
            @Override
            public void run() {
                updateCallDuration();
                callDurationHandler.postDelayed(this, 1000);
            }
        };
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        callerName = intent.getStringExtra("CALLER_NAME");
        callerId = intent.getStringExtra("CALLER_ID");
        receiverId = intent.getStringExtra("RECEIVER_ID");
        isIncomingCall = intent.getBooleanExtra("IS_INCOMING_CALL", false);
        
        if (callerName != null) {
            tvCallerName.setText(callerName);
        }
    }
    
    private void setupStringee() {
        stringeeManager = StringeeManager.getInstance(this);
        
        // Initialize StringeeAudioManager theo tài liệu chính thức
        audioManager = StringeeAudioManager.create(this);
        audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice selectedAudioDevice, 
                    Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
                Log.d(TAG, "Audio device changed: " + selectedAudioDevice);
            }
        });
        
        // Voice call setup audio
        audioManager.setSpeakerphoneOn(false); // Mặc định dùng earpiece
        
        // Ensure audio focus
        android.media.AudioManager systemAudioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
        if (systemAudioManager != null) {
            systemAudioManager.setMode(android.media.AudioManager.MODE_IN_COMMUNICATION);
            systemAudioManager.requestAudioFocus(null, android.media.AudioManager.STREAM_VOICE_CALL, 
                android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        
        Log.d(TAG, "✅ Audio setup completed for voice call");
    }
    
    private void setupListeners() {
        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());
    }
    
    private void makeOutgoingCall() {
        if (!stringeeManager.isConnected()) {
            Log.e(TAG, "❌ Stringee not ready for calls, ensuring connection...");
            tvCallStatus.setText("Đang kết nối server...");
            
            stringeeManager.setConnectionCallback(new StringeeManager.StringeeConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "✅ Stringee connected, making call...");
                    runOnUiThread(() -> {
                        tvCallStatus.setText("Đang gọi...");
                        initiateVoiceCall();
                    });
                }
                
                @Override
                public void onDisconnected() {
                    Log.e(TAG, "❌ Stringee disconnected");
                    runOnUiThread(() -> {
                        tvCallStatus.setText("Mất kết nối server");
                        Toast.makeText(VoiceCallActivity.this, "Mất kết nối server", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
                
                @Override
                public void onConnectionError(String error) {
                    Log.e(TAG, "❌ Connection error: " + error);
                    runOnUiThread(() -> {
                        tvCallStatus.setText("Lỗi kết nối server");
                        Toast.makeText(VoiceCallActivity.this, "Chưa kết nối được server, thử lại sau", Toast.LENGTH_LONG).show();
                        finish();
                    });
                }
            });
            
            // Try to reconnect
            stringeeManager.connectCurrentUser();
            return;
        }
        
        // Connection is ready, make the call
        initiateVoiceCall();
    }
    
    private void initiateVoiceCall() {
        tvCallStatus.setText("Đang gọi...");
        
        // Get caller ID from current user
        String callerId = getCurrentUserId();
        Log.d(TAG, "🎯 Making voice call from: " + callerId + " to: " + receiverId);
        
        StringeeClient client = stringeeManager.getStringeeClient();
        if (client == null) {
            Toast.makeText(this, "Lỗi: Client chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Tạo StringeeCall theo tài liệu chính thức
        stringeeCall = new StringeeCall(client, callerId, receiverId);
        
        // Set custom data cho app-to-app call
        try {
            JSONObject custom = new JSONObject();
            custom.put("type", "app-to-app");
            stringeeCall.setCustom(custom.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom data: " + e.getMessage());
        }
        
        setupCallListener();
        
        // Make the call
        stringeeCall.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Voice call initiated successfully");
            }
            
            @Override
            public void onError(StringeeError error) {
                Log.e(TAG, "❌ Error making voice call: " + error.getMessage());
                runOnUiThread(() -> {
                    Toast.makeText(VoiceCallActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }
    
    private void handleIncomingCall() {
        tvCallStatus.setText("Cuộc gọi đến...");
        
        // Lấy call từ MyApplication
        String callId = getIntent().getStringExtra("CALL_ID");
        if (callId != null) {
            stringeeCall = com.example.doannt118.MyApplication.getIncomingCall(callId);
        }
        
        if (stringeeCall == null) {
            Log.e(TAG, "❌ Incoming call not found");
            Toast.makeText(this, "Lỗi: Không tìm thấy cuộc gọi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupCallListener();
        
        // Gửi tín hiệu ringing
        stringeeCall.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Ringing signal sent");
            }
            
            @Override
            public void onError(StringeeError error) {
                Log.e(TAG, "❌ Error sending ringing: " + error.getMessage());
            }
        });
        
        // Hiển thị nút trả lời và từ chối
        showIncomingCallUI();
    }
    
    private void showIncomingCallUI() {
        // TODO: Thêm UI cho incoming call với nút Answer và Reject
        // Tạm thời tự động trả lời sau 2 giây (để test)
        new Handler().postDelayed(() -> {
            if (stringeeCall != null) {
                answerCall();
            }
        }, 2000);
    }
    
    private void answerCall() {
        if (stringeeCall != null) {
            stringeeCall.answer(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Call answered");
                    runOnUiThread(() -> tvCallStatus.setText("Đã trả lời"));
                }
                
                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "❌ Error answering call: " + error.getMessage());
                }
            });
        }
    }
    
    private void rejectCall() {
        if (stringeeCall != null) {
            stringeeCall.reject(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Call rejected");
                }
                
                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "❌ Error rejecting call: " + error.getMessage());
                }
            });
            
            // Clean up
            String callId = getIntent().getStringExtra("CALL_ID");
            if (callId != null) {
                com.example.doannt118.MyApplication.removeIncomingCall(callId);
            }
            
            finish();
        }
    }
    
    private void setupCallListener() {
        if (stringeeCall == null) {
            Log.e(TAG, "❌ stringeeCall is null");
            return;
        }
        
        Log.d(TAG, "🎯 Setting up StringeeCall listener...");
        
        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall call, StringeeCall.SignalingState signalingState, 
                    String reason, int sipCode, String sipReason) {
                Log.d(TAG, "📞 Signaling state: " + signalingState + ", reason: " + reason);
                
                runOnUiThread(() -> {
                    switch (signalingState) {
                        case CALLING:
                            tvCallStatus.setText("Đang gọi...");
                            break;
                        case RINGING:
                            tvCallStatus.setText("Đang đổ chuông...");
                            break;
                        case ANSWERED:
                            tvCallStatus.setText("Đã trả lời");
                            break;
                        case BUSY:
                            tvCallStatus.setText("Máy bận");
                            endCallAfterDelay();
                            break;
                        case ENDED:
                            tvCallStatus.setText("Cuộc gọi kết thúc");
                            endCallAfterDelay();
                            break;
                    }
                });
            }
            
            @Override
            public void onError(StringeeCall call, int code, String description) {
                Log.e(TAG, "❌ Call error: " + code + " - " + description);
                runOnUiThread(() -> {
                    tvCallStatus.setText("Lỗi: " + description);
                    Toast.makeText(VoiceCallActivity.this, "Lỗi cuộc gọi: " + description, Toast.LENGTH_SHORT).show();
                    endCallAfterDelay();
                });
            }
            
            @Override
            public void onHandledOnAnotherDevice(StringeeCall call, StringeeCall.SignalingState signalingState, 
                    String description) {
                Log.d(TAG, "📱 Call handled on another device: " + description);
                runOnUiThread(() -> {
                    tvCallStatus.setText("Đã xử lý trên thiết bị khác");
                    endCallAfterDelay();
                });
            }
            
            @Override
            public void onMediaStateChange(StringeeCall call, StringeeCall.MediaState mediaState) {
                Log.d(TAG, "🎬 Media state: " + mediaState);
                
                runOnUiThread(() -> {
                    if (mediaState == StringeeCall.MediaState.CONNECTED) {
                        isMediaConnected = true;
                        tvCallStatus.setText("Đã kết nối");
                        startCallDurationTimer();
                    } else {
                        isMediaConnected = false;
                        tvCallStatus.setText("Đang kết nối media...");
                    }
                });
            }
            
            @Override
            public void onLocalStream(StringeeCall call) {
                Log.d(TAG, "🎤 Local stream available");
            }
            
            @Override
            public void onRemoteStream(StringeeCall call) {
                Log.d(TAG, "🔊 Remote stream available");
            }
            
            @Override
            public void onCallInfo(StringeeCall call, JSONObject callInfo) {
                Log.d(TAG, "📋 Call info: " + callInfo.toString());
            }
        });
    }
    
    private void startCallDurationTimer() {
        callStartTime = System.currentTimeMillis();
        tvCallDuration.setVisibility(View.VISIBLE);
        callDurationHandler.post(callDurationRunnable);
    }
    
    private void updateCallDuration() {
        if (callStartTime > 0) {
            long duration = (System.currentTimeMillis() - callStartTime) / 1000;
            long minutes = duration / 60;
            long seconds = duration % 60;
            tvCallDuration.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }
    
    private void toggleMute() {
        if (stringeeCall != null) {
            isMuted = !isMuted;
            stringeeCall.mute(isMuted);
            btnMute.setSelected(isMuted);
            
            if (isMuted) {
                btnMute.setColorFilter(getResources().getColor(R.color.danger));
            } else {
                btnMute.clearColorFilter();
            }
        }
    }
    
    private void toggleSpeaker() {
        if (audioManager != null) {
            isSpeakerOn = !isSpeakerOn;
            audioManager.setSpeakerphoneOn(isSpeakerOn);
            btnSpeaker.setSelected(isSpeakerOn);
            
            if (isSpeakerOn) {
                btnSpeaker.setColorFilter(getResources().getColor(R.color.primary));
            } else {
                btnSpeaker.clearColorFilter();
            }
            
            Log.d(TAG, "Speaker toggled: " + (isSpeakerOn ? "ON" : "OFF"));
        }
    }
    
    private void endCall() {
        if (stringeeCall != null) {
            stringeeCall.hangup(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Call ended successfully");
                }
                
                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "Error ending call: " + error.getMessage());
                }
            });
        }
        finish();
    }
    
    private void endCallAfterDelay() {
        new Handler().postDelayed(this::finish, 2000);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callDurationHandler != null && callDurationRunnable != null) {
            callDurationHandler.removeCallbacks(callDurationRunnable);
        }
        
        // Stop audio manager and release audio focus
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        
        // Release audio focus
        android.media.AudioManager systemAudioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
        if (systemAudioManager != null) {
            systemAudioManager.setMode(android.media.AudioManager.MODE_NORMAL);
            systemAudioManager.abandonAudioFocus(null);
        }
        
        // Properly cleanup StringeeCall
        if (stringeeCall != null) {
            try {
                // Hangup the call if still active
                stringeeCall.hangup(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Voice call ended in onDestroy");
                    }

                    @Override
                    public void onError(StringeeError error) {
                        Log.e(TAG, "❌ Error ending voice call in onDestroy: " + error.getMessage());
                    }
                });
                
                // Clear the reference
                stringeeCall = null;
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error during call cleanup: " + e.getMessage());
            }
        }
        
        // Clean up incoming call from MyApplication if needed
        String callId = getIntent().getStringExtra("CALL_ID");
        if (callId != null && isIncomingCall) {
            com.example.doannt118.MyApplication.removeIncomingCall(callId);
        }
        
        // CRITICAL FIX: Force reconnect StringeeManager for subsequent calls
        if (stringeeManager != null) {
            Log.d(TAG, "🔄 Force reconnecting StringeeManager for future calls...");
            // Always force reconnect after call ends to ensure fresh connection
            new android.os.Handler().postDelayed(() -> {
                Log.d(TAG, "🔄 Forcing StringeeManager reconnection...");
                stringeeManager.forceReconnect();
            }, 500);
        }
        
        Log.d(TAG, "✅ VoiceCallActivity cleanup completed");
    }
    
    private String getCurrentUserId() {
        // Get current user ID from SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String maBenhNhan = prefs.getString("maBenhNhan", "");
        String maBacSi = prefs.getString("maBacSi", "");
        
        if (!maBenhNhan.isEmpty()) {
            return "patient_" + maBenhNhan;
        } else if (!maBacSi.isEmpty()) {
            return "doctor_" + maBacSi;
        }
        
        // Fallback
        return "user_" + System.currentTimeMillis();
    }
    
    private void setupBackPressedHandler() {
        // Use modern OnBackPressedDispatcher instead of deprecated onBackPressed
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Prevent back button during call
                // User must use end call button
                // Do nothing to prevent back navigation
            }
        });
    }
}