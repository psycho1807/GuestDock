package com.psycho1807.guestdock.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.psycho1807.guestdock.model.HaConfig;

/**
 * Thin wrapper around SharedPreferences for HA configuration.
 */
public class Prefs {

    private static final String PREFS_NAME = "guestdock_prefs";
    private static final String KEY_HA_URL   = "ha_local_url";
    private static final String KEY_HA_TOKEN = "ha_token";
    private static final String KEY_HA_NAME  = "ha_friendly_name";

    private static SharedPreferences get(Context ctx) {
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static HaConfig getHaConfig(Context ctx) {
        SharedPreferences p = get(ctx);
        return new HaConfig(
                p.getString(KEY_HA_URL, ""),
                p.getString(KEY_HA_TOKEN, ""),
                p.getString(KEY_HA_NAME, "Home Assistant")
        );
    }

    public static void saveHaConfig(Context ctx, HaConfig config) {
        get(ctx).edit()
                .putString(KEY_HA_URL, config.getLocalUrl())
                .putString(KEY_HA_TOKEN, config.getToken())
                .putString(KEY_HA_NAME, config.getFriendlyName())
                .apply();
    }

    public static boolean isConfigured(Context ctx) {
        return getHaConfig(ctx).isConfigured();
    }
}
