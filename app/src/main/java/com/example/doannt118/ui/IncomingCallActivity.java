package com.example.doannt118.ui;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.doannt118.R;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import de.hdodenhof.circleimageview.CircleImageView;

public class IncomingCallActivity extends AppCompatActivity {
    private static final String TAG = "IncomingCallActivity";
    
    private TextView tvCallerName, tvCallType;
    private CircleImageView ivCallerAvatar;
    private ImageButton btnAnswer, btnReject;
    
    private String callerName;
    private String callerId;
    private String receiverId;
    private String callId;
    private boolean isVideoCall;
    
    private Ringtone ringtone;
    private Vibrator vibrator;
    private Handler timeoutHandler;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        
        initViews();
        getDataFromIntent();
        setupListeners();
        startRinging();
        
        // Auto reject after 30 seconds if no answer
        timeoutHandler = new Handler();
        timeoutHandler.postDelayed(() -> {
            Log.d(TAG, "Call timeout - auto rejecting");
            rejectCall();
        }, 30000);
    }
    
    private void initViews() {
        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallType = findViewById(R.id.tvCallType);
        ivCallerAvatar = findViewById(R.id.ivCallerAvatar);
        btnAnswer = findViewById(R.id.btnAnswer);
        btnReject = findViewById(R.id.btnReject);
    }
    
    private void getDataFromIntent() {
        Intent intent = getIntent();
        callerName = intent.getStringExtra("CALLER_NAME");
        callerId = intent.getStringExtra("CALLER_ID");
        receiverId = intent.getStringExtra("RECEIVER_ID");
        callId = intent.getStringExtra("CALL_ID");
        isVideoCall = intent.getBooleanExtra("IS_VIDEO_CALL", false);
        
        // Update UI
        if (callerName != null) {
            tvCallerName.setText(callerName);
        }
        
        tvCallType.setText(isVideoCall ? "Cuộc gọi video đến..." : "Cuộc gọi đến...");
        
        // Set avatar (default doctor icon)
        ivCallerAvatar.setImageResource(R.drawable.ic_doctor);
    }
    
    private void setupListeners() {
        btnAnswer.setOnClickListener(v -> answerCall());
        btnReject.setOnClickListener(v -> rejectCall());
    }
    
    private void startRinging() {
        try {
            // Start ringtone
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) {
                ringtone.play();
            }
            
            // Start vibration
            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null && vibrator.hasVibrator()) {
                long[] pattern = {0, 1000, 500, 1000, 500, 1000};
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createWaveform(pattern, 0));
                } else {
                    vibrator.vibrate(pattern, 0);
                }
            }
            
            Log.d(TAG, "✅ Started ringing and vibration");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error starting ringtone/vibration: " + e.getMessage());
        }
    }
    
    private void stopRinging() {
        try {
            // Stop ringtone
            if (ringtone != null && ringtone.isPlaying()) {
                ringtone.stop();
            }
            
            // Stop vibration
            if (vibrator != null) {
                vibrator.cancel();
            }
            
            // Cancel timeout
            if (timeoutHandler != null) {
                timeoutHandler.removeCallbacksAndMessages(null);
            }
            
            Log.d(TAG, "✅ Stopped ringing and vibration");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error stopping ringtone/vibration: " + e.getMessage());
        }
    }
    
    private void answerCall() {
        Log.d(TAG, "🟢 User answered the call");
        stopRinging();
        
        if (isVideoCall) {
            // Get video call and answer it
            StringeeCall2 videoCall = com.example.doannt118.MyApplication.getIncomingVideoCall(callId);
            if (videoCall != null) {
                videoCall.answer(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Video call answered successfully");
                        // Open VideoCallActivity
                        Intent intent = new Intent(IncomingCallActivity.this, VideoCallActivity.class);
                        intent.putExtra("CALLER_NAME", callerName);
                        intent.putExtra("CALLER_ID", callerId);
                        intent.putExtra("RECEIVER_ID", receiverId);
                        intent.putExtra("IS_INCOMING_CALL", true);
                        intent.putExtra("CALL_ID", callId);
                        startActivity(intent);
                        finish();
                    }
                    
                    @Override
                    public void onError(StringeeError error) {
                        Log.e(TAG, "❌ Error answering video call: " + error.getMessage());
                        finish();
                    }
                });
            } else {
                Log.e(TAG, "❌ Video call not found");
                finish();
            }
        } else {
            // Get voice call and answer it
            StringeeCall voiceCall = com.example.doannt118.MyApplication.getIncomingCall(callId);
            if (voiceCall != null) {
                voiceCall.answer(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Voice call answered successfully");
                        // Open VoiceCallActivity
                        Intent intent = new Intent(IncomingCallActivity.this, VoiceCallActivity.class);
                        intent.putExtra("CALLER_NAME", callerName);
                        intent.putExtra("CALLER_ID", callerId);
                        intent.putExtra("RECEIVER_ID", receiverId);
                        intent.putExtra("IS_INCOMING_CALL", true);
                        intent.putExtra("CALL_ID", callId);
                        startActivity(intent);
                        finish();
                    }
                    
                    @Override
                    public void onError(StringeeError error) {
                        Log.e(TAG, "❌ Error answering voice call: " + error.getMessage());
                        finish();
                    }
                });
            } else {
                Log.e(TAG, "❌ Voice call not found");
                finish();
            }
        }
    }
    
    private void rejectCall() {
        Log.d(TAG, "🔴 User rejected the call");
        stopRinging();
        
        if (isVideoCall) {
            // Get video call and reject it
            StringeeCall2 videoCall = com.example.doannt118.MyApplication.getIncomingVideoCall(callId);
            if (videoCall != null) {
                videoCall.reject(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Video call rejected successfully");
                    }
                    
                    @Override
                    public void onError(StringeeError error) {
                        Log.e(TAG, "❌ Error rejecting video call: " + error.getMessage());
                    }
                });
                
                // Clean up
                com.example.doannt118.MyApplication.removeIncomingVideoCall(callId);
            }
        } else {
            // Get voice call and reject it
            StringeeCall voiceCall = com.example.doannt118.MyApplication.getIncomingCall(callId);
            if (voiceCall != null) {
                voiceCall.reject(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "✅ Voice call rejected successfully");
                    }
                    
                    @Override
                    public void onError(StringeeError error) {
                        Log.e(TAG, "❌ Error rejecting voice call: " + error.getMessage());
                    }
                });
                
                // Clean up
                com.example.doannt118.MyApplication.removeIncomingCall(callId);
            }
        }
        
        finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRinging();
        Log.d(TAG, "✅ IncomingCallActivity destroyed");
    }
    
    @Override
    public void onBackPressed() {
        // Prevent back button during incoming call
        // User must answer or reject
    }
}