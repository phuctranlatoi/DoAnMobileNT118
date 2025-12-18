package com.example.doannt118.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
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
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.common.StringeeAudioManager;
import com.stringee.video.StringeeVideoTrack;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class VideoCallActivity extends AppCompatActivity {
    private static final String TAG = "VideoCallActivity";
    private static final int PERMISSION_REQUEST_CODE = 1002;
    
    // Video views
    private FrameLayout localViewContainer, remoteViewContainer;
    private TextView tvCallerName, tvCallStatus, tvCallDuration;
    private ImageButton btnEndCall, btnMute, btnCamera, btnSwitchCamera;
    private ImageButton btnAnswerCall, btnRejectCall;
    private View layoutCallInfo, layoutIncomingCallButtons, layoutCallControls;
    
    private StringeeCall2 stringeeCall2;
    private StringeeManager stringeeManager;
    private StringeeAudioManager audioManager;
    private boolean isMuted = false;
    private boolean isCameraOn = true;
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
        setContentView(R.layout.activity_video_call);
        
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
            // Chỉ cần setup call và hiển thị control buttons
            layoutIncomingCallButtons.setVisibility(View.GONE);
            layoutCallControls.setVisibility(View.VISIBLE);
            handleIncomingCall();
        } else {
            // Outgoing call - hiển thị control buttons ngay
            layoutIncomingCallButtons.setVisibility(View.GONE);
            layoutCallControls.setVisibility(View.VISIBLE);
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
        
        // Check CAMERA permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.CAMERA);
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
                Toast.makeText(this, "Cần cấp quyền microphone và camera để thực hiện cuộc gọi video", 
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    
    private void initViews() {
        // Video containers - sử dụng FrameLayout theo tài liệu Stringee
        localViewContainer = findViewById(R.id.localVideoView);
        remoteViewContainer = findViewById(R.id.remoteVideoView);
        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        tvCallDuration = findViewById(R.id.tvCallDuration);
        btnEndCall = findViewById(R.id.btnEndCall);
        btnMute = findViewById(R.id.btnMute);
        btnCamera = findViewById(R.id.btnCamera);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnAnswerCall = findViewById(R.id.btnAnswerCall);
        btnRejectCall = findViewById(R.id.btnRejectCall);
        layoutCallInfo = findViewById(R.id.layoutCallInfo);
        layoutIncomingCallButtons = findViewById(R.id.layoutIncomingCallButtons);
        layoutCallControls = findViewById(R.id.layoutCallControls);
        
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
        
        // Video call setup audio
        audioManager.setSpeakerphoneOn(true);
        
        // Ensure audio focus
        android.media.AudioManager systemAudioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
        if (systemAudioManager != null) {
            systemAudioManager.setMode(android.media.AudioManager.MODE_IN_COMMUNICATION);
            systemAudioManager.requestAudioFocus(null, android.media.AudioManager.STREAM_VOICE_CALL, 
                android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        
        Log.d(TAG, "✅ Audio setup completed for video call");
    }
    
    private void setupListeners() {
        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> toggleMute());
        btnCamera.setOnClickListener(v -> toggleCamera());
        btnSwitchCamera.setOnClickListener(v -> switchCamera());
        btnAnswerCall.setOnClickListener(v -> answerCall());
        btnRejectCall.setOnClickListener(v -> rejectCall());
        
        // Toggle call info visibility khi tap vào màn hình
        if (remoteViewContainer != null) {
            remoteViewContainer.setOnClickListener(v -> toggleCallInfoVisibility());
        }
    }
    
    private void makeOutgoingCall() {
        // Đảm bảo kết nối bền vững
        stringeeManager.ensurePersistentConnection();
        
        if (!stringeeManager.isConnected()) {
            Log.e(TAG, "❌ Stringee not connected, trying to connect...");
            tvCallStatus.setText("Đang kết nối...");
            
            stringeeManager.setConnectionCallback(new StringeeManager.StringeeConnectionCallback() {
                @Override
                public void onConnected() {
                    Log.d(TAG, "✅ Stringee connected, making call...");
                    runOnUiThread(() -> initiateVideoCall());
                }
                
                @Override
                public void onDisconnected() {
                    Log.e(TAG, "❌ Stringee disconnected");
                    runOnUiThread(() -> {
                        Toast.makeText(VideoCallActivity.this, "Mất kết nối", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
                
                @Override
                public void onConnectionError(String error) {
                    Log.e(TAG, "❌ Connection error: " + error);
                    runOnUiThread(() -> {
                        Toast.makeText(VideoCallActivity.this, "Lỗi kết nối: " + error, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
            
            // Sử dụng persistent connection
            stringeeManager.ensurePersistentConnection();
            return;
        }
        
        initiateVideoCall();
    }
    
    private void initiateVideoCall() {
        tvCallStatus.setText("Đang gọi video...");
        
        // 🔥 FIX: Sử dụng callerId từ Intent thay vì getCurrentUserId()
        // callerId đã được set từ Intent trong getDataFromIntent()
        
        Log.d(TAG, "🔍 DEBUG VIDEO CALL LOGIC:");
        Log.d(TAG, "🔍 - callerId from Intent: " + callerId);
        Log.d(TAG, "🔍 - receiverId from Intent: " + receiverId);
        Log.d(TAG, "🔍 - isIncomingCall: " + isIncomingCall);
        Log.d(TAG, "🔍 - callerName: " + callerName);
        
        Log.d(TAG, "🎯 Making video call from: " + callerId + " to: " + receiverId);
        
        StringeeClient client = stringeeManager.getStringeeClient();
        if (client == null) {
            Toast.makeText(this, "Lỗi: Client chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Tạo StringeeCall2 theo tài liệu chính thức
        stringeeCall2 = new StringeeCall2(client, callerId, receiverId);
        stringeeCall2.setVideoCall(true);
        
        // Set custom data cho app-to-app call
        try {
            JSONObject custom = new JSONObject();
            custom.put("type", "app-to-app");
            stringeeCall2.setCustom(custom.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom data: " + e.getMessage());
        }
        
        setupCallListener();
        
        // Make the call
        stringeeCall2.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Video call initiated successfully");
            }
            
            @Override
            public void onError(StringeeError error) {
                Log.e(TAG, "❌ Error making video call: " + error.getMessage());
                runOnUiThread(() -> {
                    if (error.getMessage() != null && 
                        (error.getMessage().contains("not connected") || 
                         error.getMessage().contains("chưa kết nối") ||
                         error.getMessage().contains("server"))) {
                        
                        tvCallStatus.setText("Đang kết nối lại...");
                        // Sử dụng soft reconnect thay vì force reconnect
                        stringeeManager.softReconnect();
                        
                        new Handler().postDelayed(() -> {
                            if (!isFinishing()) {
                                makeOutgoingCall();
                            }
                        }, 3000);
                    } else {
                        Toast.makeText(VideoCallActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }
    
    private void handleIncomingCall() {
        tvCallStatus.setText("Cuộc gọi video đến...");
        
        // Lấy call từ MyApplication
        String callId = getIntent().getStringExtra("CALL_ID");
        if (callId != null) {
            stringeeCall2 = com.example.doannt118.MyApplication.getIncomingVideoCall(callId);
        }
        
        if (stringeeCall2 == null) {
            Log.e(TAG, "❌ Incoming video call not found");
            Toast.makeText(this, "Lỗi: Không tìm thấy cuộc gọi video", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        setupCallListener();
        
        // Gửi tín hiệu ringing
        stringeeCall2.ringing(new StatusListener() {
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
        // Hiển thị UI cho incoming call với nút Answer và Reject
        runOnUiThread(() -> {
            // Ẩn control buttons thông thường
            layoutCallControls.setVisibility(View.GONE);
            
            // Hiển thị answer/reject buttons
            layoutIncomingCallButtons.setVisibility(View.VISIBLE);
            
            // Cập nhật status
            tvCallStatus.setText("Cuộc gọi video đến...");
            
            Log.d(TAG, "✅ Incoming call UI displayed - waiting for user action");
        });
    }
    
    private void answerCall() {
        if (stringeeCall2 != null) {
            stringeeCall2.answer(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Video call answered");
                    runOnUiThread(() -> {
                        tvCallStatus.setText("Đã trả lời");
                        
                        // Chuyển từ incoming UI sang call UI
                        layoutIncomingCallButtons.setVisibility(View.GONE);
                        layoutCallControls.setVisibility(View.VISIBLE);
                        
                        // Bắt đầu timer
                        startCallDurationTimer();
                    });
                }

                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "❌ Error answering video call: " + error.getMessage());
                    runOnUiThread(() -> {
                        Toast.makeText(VideoCallActivity.this, "Lỗi trả lời cuộc gọi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        }
    }
    
    private void rejectCall() {
        if (stringeeCall2 != null) {
            stringeeCall2.reject(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Video call rejected");
                }

                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "❌ Error rejecting video call: " + error.getMessage());
                }
            });

            // Clean up
            String callId = getIntent().getStringExtra("CALL_ID");
            if (callId != null) {
                com.example.doannt118.MyApplication.removeIncomingVideoCall(callId);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Đã từ chối cuộc gọi video", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }
    
    private void setupCallListener() {
        if (stringeeCall2 == null) {
            Log.e(TAG, "❌ stringeeCall2 is null");
            return;
        }

        Log.d(TAG, "🎯 Using no-listener approach to avoid SDK compatibility issues");
        
        // Set basic call status
        tvCallStatus.setText("Cuộc gọi đang kết nối...");
        
        // Use polling to check call state and render video manually
        startCallStatePolling();
        
        Log.d(TAG, "✅ No-listener setup completed");
    }
    
    private void startCallStatePolling() {
        Handler pollHandler = new Handler();
        Runnable pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (stringeeCall2 != null) {
                    try {
                        // Try to render video streams manually
                        renderVideoStreamsManually();
                        
                        // Continue polling every 2 seconds
                        pollHandler.postDelayed(this, 2000);
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Error in polling: " + e.getMessage());
                    }
                }
            }
        };
        
        // Start polling after 3 seconds
        pollHandler.postDelayed(pollRunnable, 3000);
    }
    
    private void renderVideoStreamsManually() {
        try {
            // Try to get and render local video
            if (stringeeCall2.isVideoCall() && localViewContainer != null) {
                View localView = stringeeCall2.getLocalView();
                if (localView != null && localView.getParent() == null) {
                    runOnUiThread(() -> {
                        localViewContainer.removeAllViews();
                        localViewContainer.addView(localView);
                        stringeeCall2.renderLocalView(true);
                        Log.d(TAG, "✅ Local video rendered manually");
                    });
                }
            }
            
            // Try to get and render remote video
            if (stringeeCall2.isVideoCall() && remoteViewContainer != null) {
                View remoteView = stringeeCall2.getRemoteView();
                if (remoteView != null && remoteView.getParent() == null) {
                    runOnUiThread(() -> {
                        remoteViewContainer.removeAllViews();
                        remoteViewContainer.addView(remoteView);
                        stringeeCall2.renderRemoteView(false);
                        Log.d(TAG, "✅ Remote video rendered manually");
                        
                        // Start call duration timer when remote video is available
                        if (!isMediaConnected) {
                            isMediaConnected = true;
                            tvCallStatus.setText("Đã kết nối video");
                            startCallDurationTimer();
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error rendering video manually: " + e.getMessage());
        }
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
        if (stringeeCall2 != null) {
            isMuted = !isMuted;
            stringeeCall2.mute(isMuted);
            btnMute.setSelected(isMuted);
            
            if (isMuted) {
                btnMute.setColorFilter(getResources().getColor(R.color.danger));
            } else {
                btnMute.clearColorFilter();
            }
        }
    }
    
    private void toggleCamera() {
        if (stringeeCall2 != null) {
            isCameraOn = !isCameraOn;
            stringeeCall2.enableVideo(isCameraOn);
            btnCamera.setSelected(!isCameraOn);
            
            if (!isCameraOn) {
                btnCamera.setColorFilter(getResources().getColor(R.color.danger));
                if (localViewContainer != null) {
                    localViewContainer.setVisibility(View.GONE);
                }
            } else {
                btnCamera.clearColorFilter();
                if (localViewContainer != null) {
                    localViewContainer.setVisibility(View.VISIBLE);
                }
            }
            
            Log.d(TAG, "Camera toggled: " + (isCameraOn ? "ON" : "OFF"));
        }
    }
    
    private void switchCamera() {
        if (stringeeCall2 != null && isCameraOn) {
            stringeeCall2.switchCamera(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Camera switched successfully");
                }
                
                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "Error switching camera: " + error.getMessage());
                }
            });
        }
    }
    
    private void toggleCallInfoVisibility() {
        if (layoutCallInfo.getVisibility() == View.VISIBLE) {
            layoutCallInfo.setVisibility(View.GONE);
        } else {
            layoutCallInfo.setVisibility(View.VISIBLE);
            // Auto hide after 5 seconds
            new Handler().postDelayed(() -> {
                if (layoutCallInfo != null) {
                    layoutCallInfo.setVisibility(View.GONE);
                }
            }, 5000);
        }
    }
    
    private void endCall() {
        if (stringeeCall2 != null) {
            stringeeCall2.hangup(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Video call ended successfully");
                }
                
                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "Error ending video call: " + error.getMessage());
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
        
        Log.d(TAG, "🧹 Cleaning up VideoCallActivity...");
        
        // Stop call duration timer
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

        // Properly cleanup StringeeCall2
        if (stringeeCall2 != null) {
            try {
                // Hangup the call
                stringeeCall2.hangup(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Video call ended in onDestroy");
                    }

                    @Override
                    public void onError(StringeeError error) {
                        Log.e(TAG, "❌ Error ending video call in onDestroy: " + error.getMessage());
                    }
                });
                
                // Clear the reference
                stringeeCall2 = null;
                
            } catch (Exception e) {
                Log.e(TAG, "❌ Error during call cleanup: " + e.getMessage());
            }
        }
        
        // Clean up incoming call from MyApplication if needed
        String callId = getIntent().getStringExtra("CALL_ID");
        if (callId != null && isIncomingCall) {
            com.example.doannt118.MyApplication.removeIncomingVideoCall(callId);
        }
        
        // SOFT RECONNECT: Duy trì kết nối bền vững thay vì reset hoàn toàn
        if (stringeeManager != null) {
            Log.d(TAG, "🔄 Ensuring persistent connection for future calls...");
            new android.os.Handler().postDelayed(() -> {
                Log.d(TAG, "🔄 Soft reconnect to maintain connection...");
                stringeeManager.softReconnect();
            }, 1000);
        }
        
        Log.d(TAG, "✅ VideoCallActivity cleanup completed");
    }
    
    private String getCurrentUserId() {
        // 🔥 FIX: Sử dụng SessionManager
        try {
            com.example.doannt118.utils.SessionManager sessionManager = new com.example.doannt118.utils.SessionManager(this);
            
            String maTaiKhoan = sessionManager.getMaTaiKhoan();
            String vaiTro = sessionManager.getVaiTro();
            
            if (maTaiKhoan != null && !maTaiKhoan.isEmpty() && vaiTro != null && !vaiTro.isEmpty()) {
                if ("BenhNhan".equalsIgnoreCase(vaiTro) || "patient".equalsIgnoreCase(vaiTro)) {
                    return "patient_" + maTaiKhoan;
                } else if ("BacSi".equalsIgnoreCase(vaiTro) || "doctor".equalsIgnoreCase(vaiTro)) {
                    return "doctor_" + maTaiKhoan;
                } else {
                    return vaiTro.toLowerCase() + "_" + maTaiKhoan;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user info from SessionManager: " + e.getMessage());
        }
        
        // Fallback
        android.content.SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String maBenhNhan = prefs.getString("maBenhNhan", "");
        String maBacSi = prefs.getString("maBacSi", "");
        
        if (!maBenhNhan.isEmpty()) {
            return "patient_" + maBenhNhan;
        } else if (!maBacSi.isEmpty()) {
            return "doctor_" + maBacSi;
        }
        
        // Final fallback
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
    
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "🔄 Activity paused - maintaining connection...");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 Activity resumed - checking connection...");
        
        // Ensure persistent connection when returning to activity
        if (stringeeManager != null && !stringeeManager.isConnected()) {
            Log.d(TAG, "🔄 Connection lost during pause, ensuring persistent connection...");
            stringeeManager.ensurePersistentConnection();
        }
    }
}