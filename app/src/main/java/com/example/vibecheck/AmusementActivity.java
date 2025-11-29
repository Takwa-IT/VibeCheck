package com.example.vibecheck;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.vibecheck.databinding.ActivityAmusementBinding;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.Arrays;
import java.util.List;

public class AmusementActivity extends AppCompatActivity {
    private ActivityAmusementBinding binding;
    private List<String> games;
    private LinearLayout dotsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAmusementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Liste des jeux améliorée
        games = Arrays.asList(
                "Bubble Pop - Libère le stress 😌",
                "Quiz Fun : Quel animal es-tu ? 🐶",
                "Respiration Guidée 🌬️",
                "Jeu de Mémoire 🧠"
        );

        dotsContainer = binding.dotsContainer;
        ViewPager2 pager = binding.viewPagerGames;
        pager.setAdapter(new GamePagerAdapter(games, this));

        // Configuration de l'indicateur personnalisé
        setupDotsIndicator(pager);

        // Configuration du ViewPager pour le glissement entre jeux
        setupViewPager(pager);

        // Bouton retour
        setupBackButton();
    }

    private void setupViewPager(ViewPager2 viewPager) {
        // Activer le glissement entre les pages
        viewPager.setUserInputEnabled(true);

        // Nombre de pages à précharger
        viewPager.setOffscreenPageLimit(2);

        // Animation entre les pages
        viewPager.setPageTransformer(new DepthPageTransformer());

        // Écouter les changements de page pour mettre à jour les indicateurs
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateDots(position);
                updateGameTitle(position);
            }
        });
    }

    private void setupDotsIndicator(ViewPager2 viewPager) {
        dotsContainer.removeAllViews();

        // Créer les points indicateurs
        for (int i = 0; i < games.size(); i++) {
            View dot = new View(this);
            int dotSize = getResources().getDimensionPixelSize(R.dimen.dot_size);
            int dotMargin = getResources().getDimensionPixelSize(R.dimen.dot_margin);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dotSize, dotSize);
            params.setMargins(dotMargin, 0, dotMargin, 0);
            dot.setLayoutParams(params);

            // Style du point
            ShapeAppearanceModel shapeAppearanceModel = new ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, dotSize / 2f)
                    .build();

            MaterialShapeDrawable dotDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
            if (i == 0) {
                dotDrawable.setFillColor(getColorStateList(R.color.soft_purple));
            } else {
                dotDrawable.setFillColor(getColorStateList(R.color.text_secondary_dark));
            }
            dot.setBackground(dotDrawable);

            final int position = i;
            dot.setOnClickListener(v -> viewPager.setCurrentItem(position, true));

            dotsContainer.addView(dot);
        }
    }

    private void updateDots(int currentPosition) {
        for (int i = 0; i < dotsContainer.getChildCount(); i++) {
            View dot = dotsContainer.getChildAt(i);
            MaterialShapeDrawable dotDrawable = (MaterialShapeDrawable) dot.getBackground();

            if (i == currentPosition) {
                dotDrawable.setFillColor(getColorStateList(R.color.soft_purple));
                dot.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).start();
            } else {
                dotDrawable.setFillColor(getColorStateList(R.color.text_secondary_dark));
                dot.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
            }
        }
    }

    private void updateGameTitle(int position) {
        String[] gameTitles = {
                "Bubble Pop 🫧",
                "Quiz Animal 🐾",
                "Respiration 🌬️",
                "Mémoire 🧠"
        };

        if (position < gameTitles.length) {
            binding.tvMainTitle.setText(gameTitles[position]);
        }

        // Message de glissement déjà dans les fragments
    }
    private void setupBackButton() {
        binding.btnBack.setOnClickListener(v -> {
            // Retour à l'activité précédente (analyse)
            finish();
            // Animation de transition
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    // Animation de transition entre les pages
    private static class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                view.setAlpha(0f);
            } else if (position <= 0) { // [-1,0]
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) { // (0,1]
                view.setAlpha(1 - position);
                view.setTranslationX(pageWidth * -position);
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                view.setAlpha(0f);
            }
        }
    }
}