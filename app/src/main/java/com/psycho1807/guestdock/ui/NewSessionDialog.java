package com.psycho1807.guestdock.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.psycho1807.guestdock.R;
import com.psycho1807.guestdock.model.GuestSession;

/**
 * Dialog to create a new guest session: name, profile, duration.
 */
public class NewSessionDialog extends androidx.fragment.app.DialogFragment {

    public interface Callback { void onCreated(GuestSession session); }

    private Callback callback;

    public static void show(FragmentActivity host, Callback callback) {
        NewSessionDialog d = new NewSessionDialog();
        d.callback = callback;
        d.show(host.getSupportFragmentManager(), "new_session");
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context ctx = requireContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_new_session, null);

        EditText etName = view.findViewById(R.id.et_guest_name);
        Spinner  spProfile  = view.findViewById(R.id.sp_profile);
        Spinner  spDuration = view.findViewById(R.id.sp_duration);

        // Profile spinner
        String[] profiles = {"Chambre", "Salon", "Fête", "Baby-sitter", "Personnalisé"};
        spProfile.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, profiles));

        // Duration spinner
        String[] durations = {"2 h", "4 h", "8 h", "24 h", "48 h"};
        int[]    hours     = {2, 4, 8, 24, 48};
        spDuration.setAdapter(new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_dropdown_item, durations));
        spDuration.setSelection(2); // default 8 h

        return new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.new_session_title)
                .setView(view)
                .setPositiveButton(R.string.create, (d, w) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) name = "Invité";

                    GuestSession.Profile profile = GuestSession.Profile.values()[spProfile.getSelectedItemPosition()];
                    int h = hours[spDuration.getSelectedItemPosition()];

                    GuestSession session = new GuestSession(name, profile, GuestSession.duration(h));
                    if (callback != null) callback.onCreated(session);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
