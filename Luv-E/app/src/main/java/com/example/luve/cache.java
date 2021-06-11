package com.example.luve;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;


public class cache extends Application {
    public static final String CHANNEL_ID = "Message";
    private Socket socket;
    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {


            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;


            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            channel.enableLights(true);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }
        super.onCreate();
    }
    private Socket getSocket(){
        try {
            socket = IO.socket("http://192.168.0.104:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return socket;
    }
}
