package com.psycho1807.guestdock.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.psycho1807.guestdock.server.GuestServerService;

/**
 * Restarts the GuestServer service after device reboot.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Only start if at least one active session exists
            SessionStore store = new SessionStore(context);
            boolean hasActive = store.getAll().stream().anyMatch(s -> s.isActive() && !s.isExpired());
            if (hasActive) {
                Intent service = new Intent(context, GuestServerService.class);
                context.startForegroundService(service);
            }
        }
    }
}
