// ref: https://www.youtube.com/watch?v=M7z2MFoI6MQ
package com.febro.fcmpushnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("LOGG_remoteMessage", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("LOGG_payload", "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                // scheduleJob();
            } else {
                // Handle message within 10 seconds
                // handleNow();
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("LOGG_notification", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        // sendInAppNotification(remoteMessage.getFrom(), remoteMessage.getNotification().getBody());
        // background message - when user is not in the app
        sendNotification(remoteMessage);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void sendNotification(RemoteMessage remoteData) {
        Map<String, String> map = remoteData.getData();
//        String notifId = map.get("id");
        String msgType = map.get("type");
        String msgDesc = map.get("msgDesc");
        String imgUrl = map.get("imgUrl");
        String showLockscreen = map.get("showOnLockscreen");
        int randomId = Integer.parseInt(new SimpleDateFormat("ddhhmmss",  Locale.US).format(new Date()));
        Log.d("TAG_randomId", "sendNotification: " + randomId);

        Intent notifIntent = new Intent(this, NotificationScreen.class);
        notifIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle b = new Bundle();
        b.putString("messageBody", msgDesc);
        b.putString("imgUrl", imgUrl);
        notifIntent.putExtras(b);
        // FLAG_ACTIVITY_CLEAR_TOP =  If set, and the activity being launched is already running in the current task, then instead of launching a new instance of that activity, all of the other activities on top of it will be closed and this Intent will be delivered to the (now on top) old activity as a new Intent.
        PendingIntent pendingIntent = PendingIntent.getActivity(this, randomId /* Request code */, notifIntent,
                PendingIntent.FLAG_MUTABLE);

        Bitmap bmp = null;
        try {
            InputStream in = new URL(String.valueOf(imgUrl)).openStream();
            bmp = BitmapFactory.decodeStream(in);
//            bmp = getResizedBitmap(bmp, 120);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String channelId = "fcm_default_channel";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        assert showLockscreen != null;
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_stat_notification)
                        .setContentTitle("Nova Mensagem")
                        .setContentText("Deslize para baixo para visualizar") // if image - "deslize para baixo para visualizar" || else - messageBody
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_MAX) // n1
                        .setLargeIcon(bmp)
                        .setVisibility(showLockscreen.equals("true") ? NotificationCompat.VISIBILITY_PUBLIC : NotificationCompat.VISIBILITY_SECRET)
                        .setVibrate(new long[0])
                        .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bmp).bigLargeIcon(null).setBigContentTitle("Nova mensagem").setSummaryText(msgDesc));
                        //.setVibrate();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        Log.d("TAG_buildversion", String.valueOf(Build.VERSION.SDK_INT));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(randomId, notificationBuilder.build());
    }

    private void sendInAppNotification(String from, String body) {
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(MyFirebaseMessagingService.this.getApplicationContext(), String.format("%s -> %s", from, body), Toast.LENGTH_LONG).show());
    }

    public Bitmap getResizedBitmap(Bitmap bm, int width ) {
        float aspectRatio = bm.getWidth() /
                (float) bm.getHeight();
        int height = Math.round(width / aspectRatio);

        return Bitmap.createScaledBitmap(
                bm, width, height, false);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("LOGG_newToken", "onNewToken: " + token);
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("FCM_TOKEN", token).apply();
        //all the logic of the old FirebaseInstanceIdService.onTokenRefresh() here
        //usually, to send to the app server the instance ID token
        // sendTokenToTheAppServer(token)
    }

    public static String getToken(Context context) {
        return context.getSharedPreferences("_", MODE_PRIVATE).getString("FCM_TOKEN", null);
    }

    public static void subscribeUser(Context context) {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }

                        Log.d("TAG_subscribe_statsus", msg);
                    }
                });
    }
}

/* NOTES
N1 -
You should do this. Other answers seem outdated.

NotificationCompat.Builder mBuilder =
            (NotificationCompat.Builder) new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.some_small_icon)
            .setContentTitle("Title")
            .setContentText("This is a test notification with MAX priority")
            .setPriority(Notification.PRIORITY_MAX);
setPriority(Notification.PRIORITY_MAX) is important. It can also be replaced with any of the following as per requirement.

Different Priority Levels Info:

PRIORITY_MAX -- Use for critical and urgent notifications that alert the user to a condition that is time-critical or needs to be resolved before they can continue with a particular task.

PRIORITY_HIGH -- Use primarily for important communication, such as message or chat events with content that is particularly interesting for the user. High-priority notifications trigger the heads-up notification display.

PRIORITY_DEFAULT -- Use for all notifications that don't fall into any of the other priorities described here.

PRIORITY_LOW -- Use for notifications that you want the user to be informed about, but that are less urgent. Low-priority notifications tend to show up at the bottom of the list, which makes them a good choice for things like public or undirected social updates: The user has asked to be notified about them, but these notifications should never take precedence over urgent or direct communication.

PRIORITY_MIN -- Use for contextual or background information such as weather information or contextual location information. Minimum-priority notifications do not appear in the status bar. The user discovers them on expanding the notification shade.

 */

/*

// ref: https://stackoverflow.com/questions/58767733/the-asynctask-api-is-deprecated-in-android-11-what-are-the-alternatives
        Handler handler = new Handler(Looper.getMainLooper());

        Executors.newSingleThreadExecutor().execute(() -> {


            handler.post(() -> {
                // UI Thread work here
            });
        });

 */