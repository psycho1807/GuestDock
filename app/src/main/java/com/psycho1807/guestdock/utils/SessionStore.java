package com.psycho1807.guestdock.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.psycho1807.guestdock.model.GuestSession;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists GuestSession objects to SharedPreferences.
 */
public class SessionStore {

    private static final String PREFS_NAME = "guestdock_sessions";
    private static final String KEY_SESSIONS = "sessions";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public SessionStore(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<GuestSession> getAll() {
        String json = prefs.getString(KEY_SESSIONS, "[]");
        Type type = new TypeToken<List<GuestSession>>() {}.getType();
        List<GuestSession> sessions = gson.fromJson(json, type);
        return sessions != null ? sessions : new ArrayList<>();
    }

    public GuestSession findByToken(String token) {
        for (GuestSession s : getAll()) {
            if (s.getToken().equals(token)) return s;
        }
        return null;
    }

    public GuestSession findById(String id) {
        for (GuestSession s : getAll()) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    public void save(GuestSession session) {
        List<GuestSession> all = getAll();
        // Replace if exists, otherwise add
        boolean found = false;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(session.getId())) {
                all.set(i, session);
                found = true;
                break;
            }
        }
        if (!found) all.add(session);
        persist(all);
    }

    public void delete(String sessionId) {
        List<GuestSession> all = getAll();
        all.removeIf(s -> s.getId().equals(sessionId));
        persist(all);
    }

    public void revokeAll() {
        List<GuestSession> all = getAll();
        for (GuestSession s : all) s.setActive(false);
        persist(all);
    }

    /** Remove expired sessions to keep storage clean */
    public void purgeExpired() {
        List<GuestSession> all = getAll();
        all.removeIf(GuestSession::isExpired);
        persist(all);
    }

    private void persist(List<GuestSession> sessions) {
        prefs.edit().putString(KEY_SESSIONS, gson.toJson(sessions)).apply();
    }
}
