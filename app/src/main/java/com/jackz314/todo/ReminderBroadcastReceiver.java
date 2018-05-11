package com.jackz314.todo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import static com.jackz314.todo.MainActivity.REMINDER_NOTIFICATION_ID;
import static com.jackz314.todo.MainActivity.generateReminderNotification;

public class ReminderBroadcastReceiver extends BroadcastReceiver{

    public static String ACTION_START_REMINDER = "action_start_reminder";
    public static String OVERDUE_REMINDER = "overdue_reminder";//used as boolean tag
    public static String SNOOZED_REMINDER = "snoozed_reminder";//used as boolean tag
    public static String ACTION_SNOOZE = "action_snooze";
    public static String ACTION_FINISH = "action_finish";
    public static String ACTION_SNOOZE_TIME = "action_snooze_time";
    public static String SNOOZE_TIME = "snooze_time_value";
    public static String ANDROID_BOOT_COMPLETE_INTENT = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("BROADCAST RECEIVED!");
        if (ANDROID_BOOT_COMPLETE_INTENT.equals(intent.getAction())) {
            //todo Re-set alarms after device reboot

        }
        if(ACTION_START_REMINDER.equals(intent.getAction())){
            System.out.println("START REMINDER INTENT RECEIVED " + intent);
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isInteractive()) {//screen is off
                PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"ToDoReminderWakeLock");
                wl.acquire(5000);//wake screen for 5 seconds.
            }
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);
            Notification reminderNotification;
            DatabaseManager todoSql = new DatabaseManager(context);
            if(intent.getBooleanExtra(SNOOZED_REMINDER,false)){
                long previouslySnoozedTime = intent.getLongExtra(SNOOZE_TIME, 3600);
                reminderNotification = generateReminderNotification(context,todoSql.getOneDataInTODO(id), previouslySnoozedTime);
            }else {
                reminderNotification = generateReminderNotification(context,todoSql.getOneDataInTODO(id));
            }
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
        }else if(ACTION_SNOOZE.equals(intent.getAction())){
            //todo pop up snooze time chooser
        }else if(ACTION_FINISH.equals(intent.getAction())){
            int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);

            //update data to finish the reminder's previous remind times (to eliminate overdue situations
            DatabaseManager todoSql = new DatabaseManager(context);
            todoSql.finishReminder(id);

            //cancel previous alarms
            PendingIntent cancelAlarmPendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager cancelAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (cancelAlarmManager != null) {
                System.out.println("Previous AlarmManager canceled! Finished reminder!");
                cancelAlarmManager.cancel(cancelAlarmPendingIntent);
            }

        }else if(ACTION_SNOOZE_TIME.equals(intent.getAction())){
            long snoozeToTime = intent.getLongExtra(SNOOZE_TIME, 3600);//default snooze for one hour
            int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);

            //update data to new snoozed(put off) time
            DatabaseManager todoSql = new DatabaseManager(context);
            todoSql.storeSnoozedReminder(id, snoozeToTime);

            //cancel previous alarms
            PendingIntent cancelAlarmPendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager cancelAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (cancelAlarmManager != null) {
                System.out.println("Previous AlarmManager canceled! Proceeding to snooze action!");
                cancelAlarmManager.cancel(cancelAlarmPendingIntent);
            }

            //set snoozed alarm
            Intent notificationIntent = new Intent(context, ReminderBroadcastReceiver.class);
            notificationIntent.putExtra(REMINDER_NOTIFICATION_ID, id);
            notificationIntent.setAction(ACTION_START_REMINDER);
            notificationIntent.putExtra(SNOOZED_REMINDER, true);
            notificationIntent.putExtra(SNOOZE_TIME, snoozeToTime);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if(alarmManager != null){
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, snoozeToTime + System.currentTimeMillis(), pendingIntent);
            }

        }
    }
}
