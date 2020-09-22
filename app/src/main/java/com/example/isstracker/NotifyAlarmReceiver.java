package com.example.isstracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class NotifyAlarmReceiver extends BroadcastReceiver {

    MediaPlayer mp;

    @Override
    public void onReceive(Context context, Intent intent) {

        try {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "ALARM NOTIFICATION ID")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    //example for large icon
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))
                    .setContentTitle("ISS is near by")
                    .setContentText("The visibility of ISS is based on time and climate conditions")
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            99,
                            notificationIntent,
                            PendingIntent.FLAG_ONE_SHOT
                    );
            // example for blinking LED
            builder.setLights(0xFFb71c1c, 1000, 2000);
            //builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            builder.setContentIntent(pendingIntent);
            manager.notify(12345, builder.build());

            //start activity
            Intent i = new Intent();
            i.setClassName("com.example.isstracker", "com.example.isstracker.AlarmActivity");
            i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
            context.startActivity(i);

            Bundle bundle = intent.getExtras();
            String message = null;
            if (bundle != null) {
                message = bundle.getString("alarm_message");
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            CountDownTimer alarmStop = new CountDownTimer(1000*10,1000*10) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    mp.stop();
                }
            };

            Uri alarmUri  = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri != null)
            {
                mp = MediaPlayer.create(context, alarmUri);
                mp.start();
                alarmStop.start();
            }

        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d("alarmErrorr",e.getMessage());
            e.printStackTrace();
        }
    }
}
