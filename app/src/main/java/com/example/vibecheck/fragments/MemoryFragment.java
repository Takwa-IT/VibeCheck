package com.example.vibecheck.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.vibecheck.MemoryCard;
import com.example.vibecheck.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryFragment extends Fragment {

    private GridLayout gridLayout;
    private TextView tvPairsFound;
    private Button btnNewGame;
    private int pairsFound = 0;
    private final int TOTAL_PAIRS = 6;

    private List<MemoryCard> cards;
    private MemoryCard firstSelected = null;
    private MemoryCard secondSelected = null;
    private boolean canClick = true;

    private final String[] animalEmojis = {
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊",
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_memory, container, false);

        gridLayout = view.findViewById(R.id.gridLayout);
        tvPairsFound = view.findViewById(R.id.tvPairsFound);
        btnNewGame = view.findViewById(R.id.btnNewGame);

        setupGame();

        btnNewGame.setOnClickListener(v -> setupGame());

        return view;
    }

    private void setupGame() {
        pairsFound = 0;
        updateScore();
        gridLayout.removeAllViews();
        cards = new ArrayList<>();
        firstSelected = null;
        secondSelected = null;
        canClick = true;

        // Créer et mélanger les cartes
        List<String> animals = new ArrayList<>();
        for (int i = 0; i < TOTAL_PAIRS; i++) {
            animals.add(animalEmojis[i]);
            animals.add(animalEmojis[i]);
        }
        Collections.shuffle(animals);

        // Configurer la grille
        gridLayout.setColumnCount(4); // 4 colonnes pour plus de compacité
        gridLayout.setRowCount(3);    // 3 rangées

        // Créer les cartes
        for (int i = 0; i < animals.size(); i++) {
            MemoryCard card = new MemoryCard(getContext(), animals.get(i));
            card.setId(View.generateViewId());

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = getResources().getDimensionPixelSize(R.dimen.memory_card_size_small);
            params.columnSpec = GridLayout.spec(i % 4, 1f);
            params.rowSpec = GridLayout.spec(i / 4, 1f);
            params.setMargins(2, 2, 2, 2); // Marges réduites
            card.setLayoutParams(params);

            final int position = i;
            card.setOnClickListener(v -> {
                if (canClick && !card.isRevealed() && !card.isMatched()) {
                    handleCardClick(card);
                }
            });

            cards.add(card);
            gridLayout.addView(card);
        }
    }

    private void handleCardClick(MemoryCard card) {
        card.reveal();

        if (firstSelected == null) {
            firstSelected = card;
        } else {
            secondSelected = card;
            canClick = false;

            checkForMatch();
        }
    }

    private void checkForMatch() {
        if (firstSelected != null && secondSelected != null) {
            if (firstSelected.getAnimalEmoji().equals(secondSelected.getAnimalEmoji())) {
                // Match trouvé !
                firstSelected.setMatched(true);
                secondSelected.setMatched(true);
                pairsFound++;
                updateScore();

                if (pairsFound == TOTAL_PAIRS) {
                    tvPairsFound.setText("🎉 Bravo ! Jeu terminé !");
                }

                resetSelection();
            } else {
                // Pas de match, retourner les cartes après un délai
                new android.os.Handler().postDelayed(() -> {
                    firstSelected.hide();
                    secondSelected.hide();
                    resetSelection();
                }, 1000);
            }
        }
    }

    private void resetSelection() {
        firstSelected = null;
        secondSelected = null;
        canClick = true;
    }

    private void updateScore() {
        tvPairsFound.setText("Paires: " + pairsFound + "/" + TOTAL_PAIRS);
    }
}