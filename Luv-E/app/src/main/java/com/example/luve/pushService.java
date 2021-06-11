package com.example.luve;

import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class pushService extends FirebaseMessagingService {
    public static final String CHANNEL_ID = "Message";

    public pushService() {
    }

    @Override
    public void onDeletedMessages() {

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            if (!TextUtils.isEmpty(remoteMessage.getData().get("User_id"))) {
                String user_id = remoteMessage.getData().get("User_id");
                String user_name = remoteMessage.getData().get("User_name");
                String profile_pic = remoteMessage.getData().get("Profile_pic");


                Intent result_intent = new Intent(this, chat.class);
                Intent back_intent = new Intent(this, MainActivity.class);
                back_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                result_intent.putExtra("User", user_id);
                result_intent.putExtra("User_name", user_name);
                result_intent.putExtra("User_profile", profile_pic);


                PendingIntent pendingIntent = PendingIntent.getActivities(
                        this, 0,
                        new Intent[]{back_intent, result_intent}, PendingIntent.FLAG_ONE_SHOT);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(body))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(1, builder.build());
            } else {

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("Noti", 1);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

                PendingIntent pendingIntent = PendingIntent.getActivities(
                        this, 0,
                        new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);


                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(2, builder.build());
            }
        }
    }
}