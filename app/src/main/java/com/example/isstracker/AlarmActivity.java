package com.example.isstracker;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends AppCompatActivity {

    Button alarm_btn;
    //private PowerManager.WakeLock wl;

    @Override
    protected void onResume() {
        super.onResume();
        //wl.acquire(10*60*1000L /*10 minutes*/);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //wl.release();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "isstracker:alarm_activity");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);
        Log.d("wakeLockk", "wakeLockk running");

        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("isstracker:alarm_activity");
        keyguardLock.disableKeyguard();

        /*KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
        keyguardLock.disableKeyguard();*/

        alarm_btn = findViewById(R.id.alarm_btn);

        alarm_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setClassName("com.example.isstracker", "com.example.isstracker.MainActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.context.startActivity(i);
                finish();
            }
        });
        wakeLock.release();
    }
}
