package com.psycho1807.guestdock.ha;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.psycho1807.guestdock.model.HaConfig;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Handles all communication with the Home Assistant REST API.
 * The guest never talks directly to HA — only GuestDock does via this client.
 */
public class HaClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http;
    private final HaConfig config;

    public HaClient(HaConfig config) {
        this.config = config;
        this.http = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    // ── Entity state ─────────────────────────────────────────────────────────

    /** Get the current state of an entity */
    public JsonObject getState(String entityId) throws IOException {
        Request req = buildGet("/api/states/" + entityId);
        try (Response resp = http.newCall(req).execute()) {
            return JsonParser.parseString(resp.body().string()).getAsJsonObject();
        }
    }

    /** Get states of all entities */
    public JsonArray getAllStates() throws IOException {
        Request req = buildGet("/api/states");
        try (Response resp = http.newCall(req).execute()) {
            return JsonParser.parseString(resp.body().string()).getAsJsonArray();
        }
    }

    // ── Service calls ─────────────────────────────────────────────────────────

    /** Call a HA service (e.g. light.turn_on) */
    public boolean callService(String domain, String service, String entityId, JsonObject extra)
            throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("entity_id", entityId);
        if (extra != null) {
            for (String key : extra.keySet()) {
                body.add(key, extra.get(key));
            }
        }
        Request req = buildPost("/api/services/" + domain + "/" + service, body.toString());
        try (Response resp = http.newCall(req).execute()) {
            return resp.isSuccessful();
        }
    }

    // ── Convenience wrappers ──────────────────────────────────────────────────

    public boolean turnOn(String entityId) throws IOException {
        String domain = entityId.split("\\.")[0];
        return callService(domain, "turn_on", entityId, null);
    }

    public boolean turnOff(String entityId) throws IOException {
        String domain = entityId.split("\\.")[0];
        return callService(domain, "turn_off", entityId, null);
    }

    public boolean setBrightness(String entityId, int brightness) throws IOException {
        JsonObject extra = new JsonObject();
        extra.addProperty("brightness", brightness);
        return callService("light", "turn_on", entityId, extra);
    }

    public boolean setTemperature(String entityId, float temp) throws IOException {
        JsonObject extra = new JsonObject();
        extra.addProperty("temperature", temp);
        return callService("climate", "set_temperature", entityId, extra);
    }

    // ── Connectivity check ────────────────────────────────────────────────────

    public boolean isReachable() {
        try {
            Request req = buildGet("/api/");
            try (Response resp = http.newCall(req).execute()) {
                return resp.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Request buildGet(String path) {
        return new Request.Builder()
                .url(config.getLocalUrl() + path)
                .header("Authorization", "Bearer " + config.getToken())
                .header("Content-Type", "application/json")
                .get()
                .build();
    }

    private Request buildPost(String path, String json) {
        return new Request.Builder()
                .url(config.getLocalUrl() + path)
                .header("Authorization", "Bearer " + config.getToken())
                .header("Content-Type", "application/json")
                .post(RequestBody.create(json, JSON))
                .build();
    }
}
