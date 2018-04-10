package com.jackz314.todo;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import static com.jackz314.todo.MainActivity.REMINDER_NOTIFICATION;
import static com.jackz314.todo.MainActivity.REMINDER_NOTIFICATION_ID;

public class ReminderBroadcastReceiver extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn;
        if (pm != null) {
            isScreenOn = pm.isInteractive();
            if(!isScreenOn){
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"ToDoReminderWakeLock");
                wl.acquire(5000);//wake screen for 5 seconds.
                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification reminderNotification = intent.getParcelableExtra(REMINDER_NOTIFICATION);
                int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);
                if (notificationManager != null) {
                    notificationManager.notify(id,reminderNotification);
                }
            }
        }

    }
}
