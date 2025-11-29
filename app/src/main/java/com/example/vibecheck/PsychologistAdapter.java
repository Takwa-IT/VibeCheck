package com.example.vibecheck;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class PsychologistAdapter extends RecyclerView.Adapter<PsychologistAdapter.ViewHolder> {
    private final List<PsychologistListActivity.Psychologist> data;
    private final Context context;

    public PsychologistAdapter(List<PsychologistListActivity.Psychologist> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_psychologist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PsychologistListActivity.Psychologist item = data.get(position);

        // Remplir les données
        holder.tvName.setText(item.name);
        holder.tvSpecialty.setText(item.specialty);
        holder.tvDistance.setText(item.distance);

        // Action du bouton Contact
        holder.btnContact.setOnClickListener(v -> {
            if (item.websiteUrl != null && !item.websiteUrl.isEmpty()) {
                // Ouvrir Google Maps ou site web
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.websiteUrl));
                context.startActivity(browserIntent);
            } else if (item.phoneUri != null) {
                // Appeler le numéro
                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(item.phoneUri));
                context.startActivity(callIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpecialty, tvDistance;
        MaterialButton btnContact;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvSpecialty = itemView.findViewById(R.id.tvSpecialty);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            btnContact = itemView.findViewById(R.id.btnContact);
        }
    }
}