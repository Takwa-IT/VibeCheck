package com.example.vibecheck;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.vibecheck.databinding.ActivityEmotionDashboardBinding;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class EmotionDashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardÉmotions";
    private ActivityEmotionDashboardBinding binding;

    // Liste pour les émotions sélectionnées (multiple)
    private final List<String> selectedEmotions = new ArrayList<>();

    // Données pour les cartes
    private final String[] emotions = {"Stressé", "Fatigué", "Joyeux", "Anxieux", "Motivé", "Triste"};
    private final int[] iconResources = {
            R.drawable.ic_stress,
            R.drawable.ic_fatigue,
            R.drawable.ic_joy,
            R.drawable.ic_anxious,
            R.drawable.ic_motivated,
            R.drawable.ic_triste
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmotionDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.d(TAG, "Activité démarrée - Configuration des cartes d'émotions");
        setupEmotionCards();

        // BOUTON VALIDER – CORRIGÉ ET AMÉLIORÉ
        binding.btnValidate.setOnClickListener(v -> {
            Log.d(TAG, "Bouton Valider cliqué");

            String customEmotion = binding.etCustomEmotion.getText().toString().trim();

            // NOUVELLE LOGIQUE : on accepte si au moins UNE émotion est choisie OU écrite
            if (selectedEmotions.isEmpty() && customEmotion.isEmpty()) {
                Toast.makeText(this, "Dis-moi ce que tu ressens, même en quelques mots", Toast.LENGTH_LONG).show();
                return;
            }

            // Construire le texte final pour l'IA
            String fullEmotion;

            if (selectedEmotions.isEmpty()) {
                fullEmotion = customEmotion; // seulement ce qu'il a écrit
            } else if (customEmotion.isEmpty()) {
                fullEmotion = String.join(", ", selectedEmotions); // seulement les icônes
            } else {
                fullEmotion = String.join(", ", selectedEmotions) + " + " + customEmotion; // les deux
            }

            Log.d(TAG, "Envoi vers l'IA : " + fullEmotion);

            Intent intent = new Intent(this, AIInterpretationActivity.class);
            intent.putExtra("EMOTION", fullEmotion);
            startActivity(intent);
            finish();
        });
    }

    private void setupEmotionCards() {
        LinearLayout container = binding.emotionContainer;
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int row = 0; row < 3; row++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            rowLayout.setPadding(0, 0, 0, 24);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowLayout.setLayoutParams(rowParams);

            for (int col = 0; col < 2; col++) {
                int index = row * 2 + col;
                if (index >= emotions.length) break;

                View cardWrapper = inflater.inflate(R.layout.item_emotion_card, rowLayout, false);
                ImageView ivIcon = cardWrapper.findViewById(R.id.ivIcon);
                TextView tvEmotion = cardWrapper.findViewById(R.id.tvEmotion);
                MaterialCardView cardContent = cardWrapper.findViewById(R.id.card_content);

                ivIcon.setImageResource(iconResources[index]);
                tvEmotion.setText(emotions[index]);
                cardContent.setTag(emotions[index]);

                cardContent.setOnClickListener(view -> {
                    MaterialCardView card = (MaterialCardView) view;
                    String emotion = (String) view.getTag();

                    if (card.getStrokeWidth() > 0) {
                        // Désélection
                        card.setStrokeColor(Color.TRANSPARENT);
                        card.setStrokeWidth(0);
                        selectedEmotions.remove(emotion);
                    } else {
                        // Sélection
                        card.setStrokeColor(ContextCompat.getColor(this, R.color.selected_pink));
                        card.setStrokeWidth(4);
                        selectedEmotions.add(emotion);
                    }

                    // Animation douce
                    view.animate().scaleX(1.08f).scaleY(1.08f).setDuration(120)
                            .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100))
                            .start();

                    // Feedback discret
                    Toast.makeText(this, selectedEmotions.size() + " émotion(s) sélectionnée(s)", Toast.LENGTH_SHORT).show();
                });

                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
                );
                if (col == 0) {
                    cardParams.setMargins(0, 0, 24, 0);
                }
                cardWrapper.setLayoutParams(cardParams);
                rowLayout.addView(cardWrapper);
            }
            container.addView(rowLayout);
        }

        Log.d(TAG, "Cartes d'émotions configurées avec succès");
    }
}