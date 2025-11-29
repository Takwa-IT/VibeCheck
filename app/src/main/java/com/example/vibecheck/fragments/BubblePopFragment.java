package com.example.vibecheck.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.vibecheck.R;
import java.util.Random;

public class BubblePopFragment extends Fragment {

    private ViewGroup bubbleContainer;
    private TextView tvScore;
    private int score = 0;
    private Handler handler = new Handler();
    private Random random = new Random();
    private boolean gameActive = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bubble_pop, container, false);

        bubbleContainer = view.findViewById(R.id.bubbleContainer);
        tvScore = view.findViewById(R.id.tvScore);

        startBubbleGame();

        return view;
    }

    private void startBubbleGame() {
        gameActive = true;
        score = 0;
        updateScore();

        // Créer des bulles toutes les 800ms
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gameActive) {
                    createBubble();
                    handler.postDelayed(this, 800);
                }
            }
        }, 800);
    }

    private void createBubble() {
        if (getContext() == null) return;

        View bubble = new View(getContext());
        int size = random.nextInt(100) + 50; // Taille entre 50-150dp
        int color = getRandomColor();

        // Style de la bulle
        bubble.setBackgroundResource(R.drawable.bubble_shape);
        bubble.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

        // Animation de base
        bubble.setAlpha(0f);
        bubble.animate().alpha(1f).setDuration(500).start();

        // Position aléatoire
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(size, size);
        params.leftMargin = random.nextInt(bubbleContainer.getWidth() - size);
        params.topMargin = random.nextInt(bubbleContainer.getHeight() / 2);
        bubble.setLayoutParams(params);

        // Click listener pour éclater la bulle
        bubble.setOnClickListener(v -> {
            if (gameActive) {
                popBubble(v);
            }
        });

        bubbleContainer.addView(bubble);

        // Supprimer la bulle après un certain temps
        new Handler().postDelayed(() -> {
            if (bubble.getParent() != null) {
                bubbleContainer.removeView(bubble);
            }
        }, 3000);
    }

    private void popBubble(View bubble) {
        score += 10;
        updateScore();

        // Animation d'éclatement
        bubble.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .alpha(0f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (bubble.getParent() != null) {
                        bubbleContainer.removeView(bubble);
                    }
                })
                .start();
    }

    private int getRandomColor() {
        int[] colors = {
                getResources().getColor(R.color.soft_blue),
                getResources().getColor(R.color.soft_purple),
                getResources().getColor(R.color.soft_pink),
                getResources().getColor(R.color.soft_green)
        };
        return colors[random.nextInt(colors.length)];
    }

    private void updateScore() {
        tvScore.setText("Score: " + score);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gameActive = false;
        handler.removeCallbacksAndMessages(null);
    }
}