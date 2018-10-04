package com.jackz314.todo;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.PowerManager;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;
import static com.jackz314.todo.DatabaseManager.IMPORTANCE;
import static com.jackz314.todo.MainActivity.REMINDER_NOTIFICATION_ID;
import static com.jackz314.todo.MainActivity.REMINDER_NOTIFICATION_SET_TIME;
import static com.jackz314.todo.MainActivity.generateReminderNotification;
import static com.jackz314.todo.MainActivity.getCurrentTime;
import static com.jackz314.todo.MainActivity.scheduleAllReminder;

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
            //(re)schedule all reminders after reboot as all of the previous set ones have been destroyed
            scheduleAllReminder(context);
        }else {
            if(ACTION_START_REMINDER.equals(intent.getAction())){
                PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                if (pm != null && !pm.isInteractive()) {//screen is off
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"com.jackz314.ToDo:ReminderWakeLock");
                    wl.acquire(5000);//wake screen for 5 seconds.
                }
                int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);
                System.out.println("START REMINDER INTENT RECEIVED: " + id);
                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                Notification reminderNotification;
                DatabaseManager todoSql = new DatabaseManager(context);
                if(intent.getBooleanExtra(SNOOZED_REMINDER,false)){
                    long previouslySnoozedTime = intent.getLongExtra(SNOOZE_TIME, 3600);
                    reminderNotification = generateReminderNotification(context,todoSql.getOneDataInTODO(id), previouslySnoozedTime);
                }else {
                    reminderNotification = generateReminderNotification(context,todoSql.getOneDataInTODO(id));
                }
                if (notificationManager != null && reminderNotification != null) {
                    System.out.println("REMINDER NOTIFICATION FIRED");
                    notificationManager.notify(id,reminderNotification);//fire notification
                }else if(reminderNotification == null){//unlikely to happen but will still notify user of such events happening as the matters needed to be reminded might be crucial to user
                    Notification.Builder notificationBuilder = new Notification.Builder(context);
                    notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
                    notificationBuilder.setContentTitle(context.getString(R.string.failed_to_create_reminder));
                    notificationBuilder.setContentText(context.getString(R.string.failed_to_create_reminder_detail));
                    if (notificationManager != null) {
                        notificationManager.notify(id, notificationBuilder.build());
                    }else {
                        Toast.makeText(context,"FAILED TO CREATE NotificationManager",Toast.LENGTH_LONG).show();
                    }
                }

                //WEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE I hate coding, jk.

                //schedule overdue alarms
                SharedPreferences sharedPreferences = context.getSharedPreferences("settings_data",MODE_PRIVATE);
                AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                if(alarmManager != null && sharedPreferences.getBoolean(context.getString(R.string.main_overdue_switch),true)){//if main overdue reminder switch is turned on, then set overdue reminder AlarmManagers here
                    Cursor cursor = todoSql.getOneDataInTODO(id);
                    cursor.moveToFirst();
                    int overdueCount = intent.getIntExtra(OVERDUE_REMINDER, 0);
                    Intent notificationOverdueIntent = new Intent(context, ReminderBroadcastReceiver.class);
                    notificationOverdueIntent.putExtra(REMINDER_NOTIFICATION_ID, id);
                    notificationOverdueIntent.putExtra(OVERDUE_REMINDER, overdueCount + 1);//count overdue times, stop notifying after a certain number of notifications
                    notificationOverdueIntent.setAction(ACTION_START_REMINDER);
                    PendingIntent overduePendingIntent = PendingIntent.getBroadcast(context, id, notificationOverdueIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    long remindTime = intent.getLongExtra(REMINDER_NOTIFICATION_SET_TIME, getCurrentTime().getTime());
                    if(sharedPreferences.getBoolean(context.getString(R.string.normal_overdue_switch),true)){//if main overdue reminder switch is turned on, then set overdue reminder AlarmManagers here
                        if (overdueCount < 5){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, remindTime + 3600 * 1000, overduePendingIntent);//overdue reminder set at 1 hour later
                            }else {
                                alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime + 3600 * 1000, overduePendingIntent);//overdue reminder set at 1 hour later
                            }
                        }
                    }else if (cursor.getInt(cursor.getColumnIndex(IMPORTANCE)) > 0){
                        if(cursor.getInt(cursor.getColumnIndex(IMPORTANCE)) < 3){//normal level overdue remind
                            if(overdueCount < 8){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, remindTime + 3600 * 1000, overduePendingIntent);//overdue reminder set at 1 hour later
                                }else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime + 3600 * 1000, overduePendingIntent);//overdue reminder set at 1 hour later
                                }
                            }
                        }else {//high level overdue remind
                            if (overdueCount < 16){
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, remindTime + 1800 * 1000, overduePendingIntent);//overdue reminder set at 0.5 hour later
                                }else {
                                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, remindTime + 1800 * 1000, overduePendingIntent);//overdue reminder set at 0.5 hour later
                                }
                            }
                        }
                    }
                }
            }else if(ACTION_SNOOZE.equals(intent.getAction())){
                //todo pop up snooze time chooser
            }else if(ACTION_FINISH.equals(intent.getAction())){//todo not working
                int id = intent.getIntExtra(REMINDER_NOTIFICATION_ID, -1);
                //update data to finish the reminder's previous remind times (to eliminate overdue situations)
                DatabaseManager todoSql = new DatabaseManager(context);
                todoSql.finishReminder(id);

                //dismiss notification
                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(id);
                }

                //cancel previous alarms
                PendingIntent cancelAlarmPendingIntent = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager cancelAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                if (cancelAlarmManager != null) {
                    System.out.println("Previous AlarmManager canceled! Finished reminder: " + id);
                    cancelAlarmManager.cancel(cancelAlarmPendingIntent);
                    cancelAlarmPendingIntent.cancel();
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
                    cancelAlarmPendingIntent.cancel();
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
                NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(id);
                }
                Toast.makeText(context, context.getString(R.string.reminder_snoozed), Toast.LENGTH_SHORT).show();
            }

        }

    }
}
