package com.psycho1807.guestdock.server;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.psycho1807.guestdock.ha.HaClient;
import com.psycho1807.guestdock.model.GuestSession;
import com.psycho1807.guestdock.utils.SessionStore;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Embedded HTTP server that:
 *  - Serves the guest PWA (HTML/CSS/JS from assets)
 *  - Proxies allowed HA API calls for a valid session token
 *
 * Guest URL format: http://<tablet-ip>:8765/guest/<token>
 */
public class GuestServer extends NanoHTTPD {

    private static final int PORT = 8765;

    private final Context context;
    private final HaClient haClient;
    private final SessionStore sessionStore;
    private final Gson gson = new Gson();

    public GuestServer(Context context, HaClient haClient, SessionStore sessionStore) {
        super(PORT);
        this.context = context;
        this.haClient = haClient;
        this.sessionStore = sessionStore;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();

        // ── Guest UI ─────────────────────────────────────────────────────────
        if (uri.startsWith("/guest/")) {
            String token = uri.substring("/guest/".length()).split("/")[0];
            GuestSession gs = sessionStore.findByToken(token);

            if (gs == null || !gs.isValid()) {
                return serveExpired();
            }

            gs.incrementAccessCount();
            sessionStore.save(gs);
            return serveGuestUI(token);
        }

        // ── Proxy API ────────────────────────────────────────────────────────
        if (uri.startsWith("/api/guest/")) {
            return handleGuestApi(session, uri);
        }

        // ── Static assets ─────────────────────────────────────────────────────
        if (uri.startsWith("/static/")) {
            return serveAsset(uri.substring(1));
        }

        return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not found");
    }

    // ── Guest UI ──────────────────────────────────────────────────────────────

    private Response serveGuestUI(String token) {
        try {
            InputStream is = context.getAssets().open("guest_ui/index.html");
            String html = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // Inject the session token so the JS knows which session it is
            html = html.replace("__GUEST_TOKEN__", token);
            return Response.newFixedLengthResponse(Status.OK, "text/html", html);
        } catch (IOException e) {
            return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "UI error");
        }
    }

    private Response serveExpired() {
        String html = "<html><body style='font-family:sans-serif;text-align:center;padding:60px'>"
                + "<h2>🏠 Access expired</h2>"
                + "<p>This guest session has ended. Thank you for your visit!</p>"
                + "</body></html>";
        return Response.newFixedLengthResponse(Status.OK, "text/html", html);
    }

    // ── Proxy API ──────────────────────────────────────────────────────────────

    private Response handleGuestApi(IHTTPSession session, String uri) {
        // Extract token from header
        String token = session.getHeaders().get("x-guest-token");
        if (token == null) {
            return jsonError(Status.UNAUTHORIZED, "Missing token");
        }

        GuestSession gs = sessionStore.findByToken(token);
        if (gs == null || !gs.isValid()) {
            return jsonError(Status.UNAUTHORIZED, "Session expired");
        }

        // GET /api/guest/states — returns only allowed entities
        if (uri.equals("/api/guest/states") && session.getMethod().name().equals("GET")) {
            return handleGetStates(gs);
        }

        // POST /api/guest/service — call a service on an allowed entity
        if (uri.equals("/api/guest/service") && session.getMethod().name().equals("POST")) {
            return handleCallService(session, gs);
        }

        return jsonError(Status.NOT_FOUND, "Unknown endpoint");
    }

    private Response handleGetStates(GuestSession gs) {
        try {
            var allStates = haClient.getAllStates();
            var allowed = new com.google.gson.JsonArray();
            for (var el : allStates) {
                var obj = el.getAsJsonObject();
                String entityId = obj.get("entity_id").getAsString();
                if (gs.getAllowedEntities().contains(entityId)) {
                    allowed.add(obj);
                }
            }
            return jsonOk(allowed.toString());
        } catch (IOException e) {
            return jsonError(Status.INTERNAL_ERROR, "HA unreachable");
        }
    }

    private Response handleCallService(IHTTPSession session, GuestSession gs) {
        try {
            Map<String, String> body = new java.util.HashMap<>();
            session.parseBody(body);
            String json = body.get("postData");
            if (json == null) return jsonError(Status.BAD_REQUEST, "Empty body");

            JsonObject req = gson.fromJson(json, JsonObject.class);
            String entityId = req.get("entity_id").getAsString();
            String service  = req.get("service").getAsString();

            // Security: only allowed entities
            if (!gs.getAllowedEntities().contains(entityId)) {
                return jsonError(Status.FORBIDDEN, "Entity not allowed");
            }

            String domain = entityId.split("\\.")[0];
            JsonObject extra = req.has("data") ? req.getAsJsonObject("data") : null;
            boolean ok = haClient.callService(domain, service, entityId, extra);
            return jsonOk("{\"success\":" + ok + "}");

        } catch (Exception e) {
            return jsonError(Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    // ── Static assets ──────────────────────────────────────────────────────────

    private Response serveAsset(String path) {
        try {
            InputStream is = context.getAssets().open(path);
            String mime = path.endsWith(".js") ? "application/javascript"
                    : path.endsWith(".css") ? "text/css"
                    : path.endsWith(".png") ? "image/png"
                    : "text/plain";
            return Response.newChunkedResponse(Status.OK, mime, is);
        } catch (IOException e) {
            return Response.newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Asset not found");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Response jsonOk(String json) {
        return Response.newFixedLengthResponse(Status.OK, "application/json", json);
    }

    private Response jsonError(Status status, String message) {
        return Response.newFixedLengthResponse(status, "application/json",
                "{\"error\":\"" + message + "\"}");
    }

    public int getPort() { return PORT; }
}
