package com.psycho1807.guestdock.model;

import java.util.List;
import java.util.UUID;

/**
 * Represents a temporary guest access session.
 * Contains the allowed entities, expiry time, and access token.
 */
public class GuestSession {

    public enum Profile {
        BEDROOM,    // Bedroom lights + temperature
        LIVING,     // Living room lights + TV
        PARTY,      // All lights + music
        BABYSITTER, // Limited: lights only, no locks
        CUSTOM
    }

    private String id;
    private String name;           // e.g. "Weekend guests"
    private String token;          // Random token for the guest URL
    private Profile profile;
    private List<String> allowedEntities; // HA entity IDs
    private long createdAt;
    private long expiresAt;        // Unix timestamp ms
    private boolean active;
    private int accessCount;       // How many times the guest opened the UI

    public GuestSession() {
        this.id = UUID.randomUUID().toString();
        this.token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        this.createdAt = System.currentTimeMillis();
        this.active = true;
        this.accessCount = 0;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Profile getProfile() { return profile; }
    public void setProfile(Profile profile) { this.profile = profile; }

    public List<String> getAllowedEntities() { return allowedEntities; }
    public void setAllowedEntities(List<String> allowedEntities) { this.allowedEntities = allowedEntities; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getAccessCount() { return accessCount; }
    public void setAccessCount(int accessCount) { this.accessCount = accessCount; }

    // ── Helper methods ───────────────────────────────────────────────────────

    public boolean isExpired() {
        return System.currentTimeMillis() > expiresAt;
    }

    public boolean isValid() {
        return active && !isExpired();
    }

    public void incrementAccessCount() {
        this.accessCount++;
    }

    /** Duration options in milliseconds */
    public static long duration(int hours) {
        return (long) hours * 60 * 60 * 1000;
    }
}
