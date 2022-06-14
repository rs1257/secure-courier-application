package com.gpig.a.utils;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.gpig.a.LoginActivity;
import com.gpig.a.R;

import java.util.List;

public final class NotificationUtils {

    public static final String CHANNEL_ID = "gpig.a.notifications";
    public static boolean isAppOpen = true;

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "GPIG Notifications";
            String description = "Notifications for the silver greyhound";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    public static boolean isAppRunning(final Context context) {
        String packageName = "com.gpig.a";
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null)
        {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void notify(Context context, String title, String text) {
        if(isAppOpen){
            Toast.makeText(context, title, Toast.LENGTH_LONG).show(); //TODO use snackbar
            return;
        }
        int notificationID = 5;
        Intent intent = new Intent(context, LoginActivity.class);
        intent.putExtra("NotificationID", notificationID);
// use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
        nBuilder.setContentTitle(title);
        nBuilder.setContentText(text);
        nBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        nBuilder.setContentIntent(pIntent);
        nBuilder.setSmallIcon(R.mipmap.ic_launcher);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationID, nBuilder.build());
    }

    public static void clearNotifications(Context context) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    public static void clearNotification(Context context, int id) {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(id);
    }
}
