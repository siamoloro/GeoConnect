package com.example.geoconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.Holder> {

    public interface OnRevokeListener {
        void onRevoke(Session session);
    }

    private final List<Session> sessions;
    private final OnRevokeListener listener;
    private final String currentSessionId;

    public SessionAdapter(List<Session> sessions, String currentSessionId, OnRevokeListener listener) {
        this.sessions = sessions;
        this.currentSessionId = currentSessionId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Session s = sessions.get(position);
        boolean isCurrent = s.getId().equals(currentSessionId);
        h.name.setText(isCurrent ? s.getDeviceName() + " (this device)" : s.getDeviceName());
        h.info.setText(s.getLocationInfo());
        h.revoke.setVisibility(isCurrent ? View.GONE : View.VISIBLE);
        h.revoke.setOnClickListener(v -> listener.onRevoke(s));
    }

    @Override
    public int getItemCount() { return sessions.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView name, info;
        MaterialButton revoke;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvDeviceName);
            info = itemView.findViewById(R.id.tvLocationInfo);
            revoke = itemView.findViewById(R.id.btnRevoke);
        }
    }
}