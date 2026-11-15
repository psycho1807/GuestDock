package com.psycho1807.guestdock.model;

/**
 * Home Assistant connection configuration.
 */
public class HaConfig {
    private String localUrl;    // e.g. http://192.168.1.100:8123
    private String token;       // Long-lived access token
    private String friendlyName; // e.g. "My Home"

    public HaConfig() {}

    public HaConfig(String localUrl, String token, String friendlyName) {
        this.localUrl = localUrl;
        this.token = token;
        this.friendlyName = friendlyName;
    }

    public String getLocalUrl() { return localUrl; }
    public void setLocalUrl(String localUrl) { this.localUrl = localUrl; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getFriendlyName() { return friendlyName; }
    public void setFriendlyName(String friendlyName) { this.friendlyName = friendlyName; }

    public boolean isConfigured() {
        return localUrl != null && !localUrl.isEmpty()
            && token != null && !token.isEmpty();
    }
}
