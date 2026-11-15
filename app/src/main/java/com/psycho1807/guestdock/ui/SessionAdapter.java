package com.psycho1807.guestdock.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.psycho1807.guestdock.R;
import com.psycho1807.guestdock.model.GuestSession;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {

    public interface OnClickListener { void onClick(GuestSession session); }

    private List<GuestSession> data;
    private final OnClickListener listener;

    public SessionAdapter(List<GuestSession> data, OnClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    public void updateData(List<GuestSession> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_session, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus, tvExpiry;

        VH(View v) {
            super(v);
            tvName   = v.findViewById(R.id.tv_name);
            tvStatus = v.findViewById(R.id.tv_status);
            tvExpiry = v.findViewById(R.id.tv_expiry);
        }

        void bind(GuestSession s) {
            tvName.setText(s.getName());
            boolean valid = s.isValid();
            tvStatus.setText(valid ? R.string.status_active : R.string.status_expired);
            tvStatus.setTextColor(itemView.getContext().getColor(valid ? R.color.green : R.color.red));
            tvExpiry.setText(java.text.DateFormat.getDateTimeInstance(
                    java.text.DateFormat.SHORT, java.text.DateFormat.SHORT)
                    .format(new java.util.Date(s.getExpiresAt())));
            itemView.setOnClickListener(v -> listener.onClick(s));
        }
    }
}
