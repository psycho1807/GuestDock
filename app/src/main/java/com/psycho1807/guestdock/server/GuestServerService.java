package com.psycho1807.guestdock.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.psycho1807.guestdock.R;
import com.psycho1807.guestdock.ha.HaClient;
import com.psycho1807.guestdock.ui.MainActivity;
import com.psycho1807.guestdock.utils.Prefs;
import com.psycho1807.guestdock.utils.SessionStore;

import java.io.IOException;

/**
 * Foreground service that keeps the GuestServer running in the background.
 */
public class GuestServerService extends Service {

    private static final String CHANNEL_ID = "guestdock_server";
    private static final int NOTIF_ID = 1;

    private GuestServer server;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIF_ID, buildNotification());

        if (server == null || !server.isAlive()) {
            HaClient haClient = new HaClient(Prefs.getHaConfig(this));
            SessionStore sessionStore = new SessionStore(this);
            server = new GuestServer(this, haClient, sessionStore);
            try {
                server.start();
            } catch (IOException e) {
                stopSelf();
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (server != null) server.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    // ── Notification ──────────────────────────────────────────────────────────

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "GuestDock Server", NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("Keeps guest access running");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GuestDock")
                .setContentText("Guest server running on port 8765")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pi)
                .setOngoing(true)
                .build();
    }
}
