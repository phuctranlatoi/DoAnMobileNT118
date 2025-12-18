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

        setupBackPressedHandler();

        if (checkAndRequestPermissions()) {
            startCall();
        }
    }

    private void startCall() {
        if (isIncomingCall) {
            handleIncomingCall();
        } else {
            makeOutgoingCall(); // Sửa chính ở đây
        }
    }

    private boolean checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

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

        audioManager = StringeeAudioManager.create(this);
        audioManager.start(new StringeeAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StringeeAudioManager.AudioDevice selectedAudioDevice,
                                             Set<StringeeAudioManager.AudioDevice> availableAudioDevices) {
                Log.d(TAG, "Audio device changed: " + selectedAudioDevice);
            }
        });

        audioManager.setSpeakerphoneOn(false);

        AudioManager systemAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (systemAudioManager != null) {
            systemAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            systemAudioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }

        Log.d(TAG, "✅ Audio setup completed for voice call");
    }

    private void setupListeners() {
        btnEndCall.setOnClickListener(v -> endCall());
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());
    }

    // ==================== SỬA CHÍNH: OUTGOING CALL ====================
    private void makeOutgoingCall() {
        // Bấm gọi là hiện ngay "Đang gọi..." - trải nghiệm mượt
        tvCallStatus.setText("Đang gọi...");

        // Debug user info và test connection trước
        Log.d(TAG, "🔍 Debugging user info before call...");
        stringeeManager.debugUserInfo();
        
        Log.d(TAG, "🧪 Testing Stringee connection before call...");
        stringeeManager.testSimpleConnection();

        // Đảm bảo kết nối bền vững
        stringeeManager.ensurePersistentConnection();

        // Đợi một chút để connection hoàn tất
        new Handler().postDelayed(() -> {
            initiateVoiceCallWithRetry();
        }, 1500);
    }

    private void initiateVoiceCallWithRetry() {
        // 🔥 FIX: Sử dụng callerId từ Intent thay vì getCurrentUserId()
        String currentCallerId = callerId; // Đã được set từ Intent trong getDataFromIntent()
        
        Log.d(TAG, "🔍 DEBUG CALL LOGIC:");
        Log.d(TAG, "🔍 - callerId from Intent: " + callerId);
        Log.d(TAG, "🔍 - receiverId from Intent: " + receiverId);
        Log.d(TAG, "🔍 - isIncomingCall: " + isIncomingCall);
        Log.d(TAG, "🔍 - callerName: " + callerName);
        
        StringeeClient client = stringeeManager.getStringeeClient();

        // Nếu chưa có client hoặc chưa connect → hiển thị đang kết nối và retry
        if (client == null || !stringeeManager.isConnected()) {
            tvCallStatus.setText("Đang kết nối...");

            // Sử dụng persistent connection
            stringeeManager.ensurePersistentConnection();

            new Handler().postDelayed(() -> {
                if (!isFinishing() && stringeeCall == null) {
                    initiateVoiceCallWithRetry();
                }
            }, 2000);

            return;
        }

        // Đã connect → quay lại trạng thái gọi bình thường
        runOnUiThread(() -> tvCallStatus.setText("Đang gọi..."));

        Log.d(TAG, "🎯 Making voice call from: " + currentCallerId + " to: " + receiverId);

        stringeeCall = new StringeeCall(client, currentCallerId, receiverId);

        try {
            JSONObject custom = new JSONObject();
            custom.put("type", "app-to-app");
            stringeeCall.setCustom(custom.toString());
        } catch (Exception e) {
            Log.e(TAG, "Error setting custom data: " + e.getMessage());
        }

        setupCallListener();

        stringeeCall.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ makeCall() success");
            }

            @Override
            public void onError(StringeeError error) {
                Log.e(TAG, "❌ makeCall error: " + error.getMessage());
                runOnUiThread(() -> {
                    if (error.getMessage() != null && 
                        (error.getMessage().contains("not connected") || 
                         error.getMessage().contains("chưa kết nối") ||
                         error.getMessage().contains("server"))) {
                        
                        tvCallStatus.setText("Đang kết nối lại...");
                        // Sử dụng soft reconnect thay vì force reconnect
                        stringeeManager.softReconnect();
                        
                        new Handler().postDelayed(() -> {
                            if (!isFinishing()) initiateVoiceCallWithRetry();
                        }, 3000);
                    } else {
                        Toast.makeText(VoiceCallActivity.this, "Lỗi gọi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        endCallAfterDelay();
                    }
                });
            }
        });
    }
    // ==================================================================

    private void handleIncomingCall() {
        tvCallStatus.setText("Cuộc gọi đến...");

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

        stringeeCall.ringing(new StatusListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "✅ Ringing signal sent");
            }
        });

        showIncomingCallUI();
    }

    private void showIncomingCallUI() {
        // Tự động trả lời sau 2s để test (bạn có thể thay bằng nút Answer)
        new Handler().postDelayed(() -> {
            if (stringeeCall != null && !isFinishing()) {
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
            });
        }
    }

    private void setupCallListener() {
        if (stringeeCall == null) return;

        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall call, StringeeCall.SignalingState state,
                                               String reason, int sipCode, String sipReason) {
                runOnUiThread(() -> {
                    switch (state) {
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
                runOnUiThread(() -> {
                    tvCallStatus.setText("Lỗi cuộc gọi");
                    Toast.makeText(VoiceCallActivity.this, "Lỗi: " + description, Toast.LENGTH_SHORT).show();
                    endCallAfterDelay();
                });
            }

            @Override
            public void onMediaStateChange(StringeeCall call, StringeeCall.MediaState mediaState) {
                runOnUiThread(() -> {
                    if (mediaState == StringeeCall.MediaState.CONNECTED) {
                        isMediaConnected = true;
                        tvCallStatus.setText("Đang nói chuyện");
                        startCallDurationTimer();
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall call) {}
            @Override
            public void onRemoteStream(StringeeCall call) {}
            @Override
            public void onCallInfo(StringeeCall call, JSONObject callInfo) {}
            @Override
            public void onHandledOnAnotherDevice(StringeeCall call, StringeeCall.SignalingState signalingState, String description) {
                runOnUiThread(() -> {
                    tvCallStatus.setText("Đã xử lý trên thiết bị khác");
                    endCallAfterDelay();
                });
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
            btnMute.setColorFilter(isMuted ? getResources().getColor(R.color.danger) : 0);
        }
    }

    private void toggleSpeaker() {
        if (audioManager != null) {
            isSpeakerOn = !isSpeakerOn;
            audioManager.setSpeakerphoneOn(isSpeakerOn);
            btnSpeaker.setSelected(isSpeakerOn);
            btnSpeaker.setColorFilter(isSpeakerOn ? getResources().getColor(R.color.primary) : 0);
        }
    }

    private void endCall() {
        if (stringeeCall != null) {
            stringeeCall.hangup(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Cuộc gọi đã kết thúc thành công");
                }

                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "❌ Lỗi khi kết thúc cuộc gọi: " + error.getMessage());
                }
            });
            stringeeCall = null; // Đặt null để tránh gọi lại
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

        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }

        AudioManager systemAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (systemAudioManager != null) {
            systemAudioManager.setMode(AudioManager.MODE_NORMAL);
            systemAudioManager.abandonAudioFocus(null);
        }

        if (stringeeCall != null) {
            stringeeCall.hangup(new StatusListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "✅ Call hung up successfully in onDestroy");
                }

                @Override
                public void onError(StringeeError error) {
                    Log.e(TAG, "❌ Error hanging up call in onDestroy: " + error.getMessage());
                }
            });
            stringeeCall = null;
        }

        String callId = getIntent().getStringExtra("CALL_ID");
        if (callId != null && isIncomingCall) {
            com.example.doannt118.MyApplication.removeIncomingCall(callId);
        }

        // SOFT RECONNECT: Duy trì kết nối bền vững thay vì reset hoàn toàn
        if (stringeeManager != null) {
            Log.d(TAG, "🔄 Ensuring persistent connection for future calls...");
            new Handler().postDelayed(() -> {
                Log.d(TAG, "🔄 Soft reconnect to maintain connection...");
                stringeeManager.softReconnect();
            }, 1000);
        }

        Log.d(TAG, "✅ VoiceCallActivity destroyed cleanly");
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
        return "user_" + System.currentTimeMillis();
    }

    private void setupBackPressedHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Không cho back trong lúc gọi
            }
        });
    }
}