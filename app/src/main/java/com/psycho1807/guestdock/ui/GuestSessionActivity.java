package com.psycho1807.guestdock.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.psycho1807.guestdock.R;
import com.psycho1807.guestdock.model.GuestSession;
import com.psycho1807.guestdock.utils.QrGenerator;
import com.psycho1807.guestdock.utils.SessionStore;
import com.psycho1807.guestdock.server.GuestServer;

/**
 * Shows QR code and details for a single guest session.
 */
public class GuestSessionActivity extends AppCompatActivity {

    public static final String EXTRA_SESSION_ID = "session_id";

    private SessionStore store;
    private GuestSession session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_session);

        store = new SessionStore(this);
        String id = getIntent().getStringExtra(EXTRA_SESSION_ID);
        session = store.findById(id);

        if (session == null) { finish(); return; }

        bindViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh in case status changed
        if (session != null) {
            session = store.findById(session.getId());
            if (session != null) bindViews();
        }
    }

    private void bindViews() {
        TextView tvName    = findViewById(R.id.tv_session_name);
        TextView tvUrl     = findViewById(R.id.tv_guest_url);
        TextView tvExpiry  = findViewById(R.id.tv_expiry);
        TextView tvCount   = findViewById(R.id.tv_access_count);
        TextView tvStatus  = findViewById(R.id.tv_status);
        ImageView ivQr     = findViewById(R.id.iv_qr);
        Button btnRevoke   = findViewById(R.id.btn_revoke);
        Button btnShare    = findViewById(R.id.btn_share);

        String guestUrl = buildGuestUrl();

        tvName.setText(session.getName());
        tvUrl.setText(guestUrl);
        java.text.DateFormat df = java.text.DateFormat.getDateTimeInstance(
                java.text.DateFormat.SHORT, java.text.DateFormat.SHORT);
        tvExpiry.setText(getString(R.string.expires_at, df.format(new java.util.Date(session.getExpiresAt()))));
        tvCount.setText(getString(R.string.access_count, session.getAccessCount()));

        boolean valid = session.isValid();
        tvStatus.setText(valid ? R.string.status_active : R.string.status_expired);
        tvStatus.setTextColor(getColor(valid ? R.color.green : R.color.red));

        // QR code
        Bitmap qr = QrGenerator.generate(guestUrl);
        if (qr != null) ivQr.setImageBitmap(qr);

        btnRevoke.setOnClickListener(v -> confirmRevoke());
        btnShare.setOnClickListener(v -> shareUrl(guestUrl));

        // Hide revoke if already expired/inactive
        btnRevoke.setVisibility(valid ? View.VISIBLE : View.GONE);
    }

    private String buildGuestUrl() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        @SuppressWarnings("deprecation")
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return "http://" + ip + ":" + GuestServer.PORT + "/guest/" + session.getToken();
    }

    private void confirmRevoke() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.revoke_title)
                .setMessage(R.string.revoke_message)
                .setPositiveButton(R.string.revoke, (d, w) -> {
                    session.setActive(false);
                    store.save(session);
                    bindViews();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void shareUrl(String url) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message, session.getName(), url));
        startActivity(Intent.createChooser(share, getString(R.string.share_title)));
    }
}
