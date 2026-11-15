package com.psycho1807.guestdock.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.psycho1807.guestdock.R;
import com.psycho1807.guestdock.ha.HaClient;
import com.psycho1807.guestdock.model.HaConfig;
import com.psycho1807.guestdock.utils.Prefs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * First-run setup: enter the local HA URL and a long-lived token.
 */
public class SetupActivity extends AppCompatActivity {

    private EditText etUrl, etToken, etName;
    private Button btnTest, btnSave;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        etUrl   = findViewById(R.id.et_ha_url);
        etToken = findViewById(R.id.et_ha_token);
        etName  = findViewById(R.id.et_ha_name);
        btnTest = findViewById(R.id.btn_test);
        btnSave = findViewById(R.id.btn_save);

        // Pre-fill with existing config
        HaConfig existing = Prefs.getHaConfig(this);
        etUrl.setText(existing.getLocalUrl());
        etToken.setText(existing.getToken());
        etName.setText(existing.getFriendlyName());

        btnTest.setOnClickListener(v -> testConnection());
        btnSave.setOnClickListener(v -> saveAndFinish());
    }

    private void testConnection() {
        HaConfig config = buildConfig();
        if (!validate(config)) return;

        btnTest.setEnabled(false);
        btnTest.setText(R.string.testing);

        executor.execute(() -> {
            HaClient client = new HaClient(config);
            boolean ok = client.isReachable();
            runOnUiThread(() -> {
                btnTest.setEnabled(true);
                btnTest.setText(R.string.test_connection);
                if (ok) {
                    Toast.makeText(this, R.string.connection_ok, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void saveAndFinish() {
        HaConfig config = buildConfig();
        if (!validate(config)) return;
        Prefs.saveHaConfig(this, config);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private HaConfig buildConfig() {
        String url   = etUrl.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        String name  = etName.getText().toString().trim();
        if (name.isEmpty()) name = "Home Assistant";
        // Remove trailing slash
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        return new HaConfig(url, token, name);
    }

    private boolean validate(HaConfig config) {
        if (TextUtils.isEmpty(config.getLocalUrl())) {
            etUrl.setError(getString(R.string.required));
            return false;
        }
        if (TextUtils.isEmpty(config.getToken())) {
            etToken.setError(getString(R.string.required));
            return false;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
