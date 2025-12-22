package com.example.doannt118.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.doannt118.utils.MedicineReminderManager;

public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, rescheduling medicine reminders");
            
            // Lấy maBenhNhan từ SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String maBenhNhan = prefs.getString("maBenhNhan", null);
            
            if (maBenhNhan != null && !maBenhNhan.isEmpty()) {
                MedicineReminderManager.rescheduleReminders(context, maBenhNhan);
            }
        }
    }
}
