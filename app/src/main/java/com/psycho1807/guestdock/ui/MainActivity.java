package com.psycho1807.guestdock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.psycho1807.guestdock.R;
import com.psycho1807.guestdock.model.GuestSession;
import com.psycho1807.guestdock.server.GuestServerService;
import com.psycho1807.guestdock.utils.Prefs;
import com.psycho1807.guestdock.utils.SessionStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Home screen: lists all guest sessions and lets the user create new ones.
 */
public class MainActivity extends AppCompatActivity {

    private SessionStore store;
    private SessionAdapter adapter;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private final ActivityResultLauncher<Intent> setupLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), r -> {
                if (r.getResultCode() == RESULT_OK) startServer();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        store       = new SessionStore(this);
        recyclerView = findViewById(R.id.recycler_sessions);
        tvEmpty     = findViewById(R.id.tv_empty);

        adapter = new SessionAdapter(new ArrayList<>(), session -> openSession(session));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_new_session);
        fab.setOnClickListener(v -> showNewSessionDialog());

        // Go to setup if not configured
        if (!Prefs.isConfigured(this)) {
            setupLauncher.launch(new Intent(this, SetupActivity.class));
        } else {
            startServer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        store.purgeExpired();
        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            setupLauncher.launch(new Intent(this, SetupActivity.class));
            return true;
        } else if (id == R.id.action_revoke_all) {
            confirmRevokeAll();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshList() {
        List<GuestSession> sessions = store.getAll();
        adapter.updateData(sessions);
        tvEmpty.setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void openSession(GuestSession session) {
        Intent intent = new Intent(this, GuestSessionActivity.class);
        intent.putExtra(GuestSessionActivity.EXTRA_SESSION_ID, session.getId());
        startActivity(intent);
    }

    private void showNewSessionDialog() {
        NewSessionDialog.show(this, session -> {
            store.save(session);
            openSession(session);
        });
    }

    private void confirmRevokeAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.revoke_all_title)
                .setMessage(R.string.revoke_all_message)
                .setPositiveButton(R.string.revoke_all, (d, w) -> {
                    store.revokeAll();
                    refreshList();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void startServer() {
        Intent service = new Intent(this, GuestServerService.class);
        startForegroundService(service);
    }
}
