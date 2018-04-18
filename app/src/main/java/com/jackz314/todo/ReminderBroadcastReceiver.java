package com.jackz314.todo;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import static com.jackz314.todo.MainActivity.REMINDER_NOTIFICATION_ID;
import static com.jackz314.todo.MainActivity.generateReminderNotification;

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
            }
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);
            DatabaseManager todoSql = new DatabaseManager(context);
            Notification reminderNotification = generateReminderNotification(context,todoSql.getOneDataInTODO(id));
            if (notificationManager != null && reminderNotification != null) {
                notificationManager.notify(id,reminderNotification);
            }else if(reminderNotification == null){
                Notification.Builder notificationBuilder = new Notification.Builder(context);
                notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                notificationBuilder.setContentTitle(context.getString(R.string.failed_to_create_reminder));
                notificationBuilder.setContentText(context.getString(R.string.failed_to_create_reminder_detail));
                if (notificationManager != null) {
                    notificationManager.notify(id,notificationBuilder.build());
                }else {
                    Toast.makeText(context,"FAILED TO CREATE NotificationManager",Toast.LENGTH_LONG).show();
                }
            }
        }

    }
}
