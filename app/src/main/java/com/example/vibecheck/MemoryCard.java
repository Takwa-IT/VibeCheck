package com.example.vibecheck;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class MemoryCard extends CardView {
    private String animalEmoji;
    private boolean isRevealed = false;
    private boolean isMatched = false;
    private TextView textView;

    public MemoryCard(Context context, String animalEmoji) {
        super(context);
        this.animalEmoji = animalEmoji;
        setupView();
    }

    private void setupView() {
        // Configuration de la carte - taille réduite
        setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.soft_purple));
        setCardElevation(4); // Élévation réduite
        setRadius(8); // Coins moins arrondis

        // TextView pour l'émoji - police plus petite
        textView = new TextView(getContext());
        textView.setText("?");
        textView.setTextSize(16); // Taille réduite de 24 à 16
        textView.setTextColor(Color.WHITE);
        textView.setGravity(android.view.Gravity.CENTER);

        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        addView(textView, params);
    }

    public void reveal() {
        isRevealed = true;
        textView.setText(animalEmoji);
        setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.soft_green));
    }

    public void hide() {
        isRevealed = false;
        textView.setText("?");
        setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.soft_purple));
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        isMatched = matched;
        if (matched) {
            setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.soft_blue));
        }
    }

    public String getAnimalEmoji() {
        return animalEmoji;
    }
}