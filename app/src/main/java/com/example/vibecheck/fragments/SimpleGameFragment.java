package com.example.vibecheck.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.vibecheck.R;
import java.util.Random;

public class SimpleGameFragment extends Fragment {
    private TextView tvTitle, tvMessage;
    private Button btnAction;
    private Random random = new Random();
    private int clickCount = 0;

    private String[] funMessages = {
            "Tu es génial aujourd'hui ! 🌟",
            "Prends une grande respiration ! 🌬️",
            "Souris, ça va bien se passer ! 😊",
            "Tu mérites une pause ! ☕",
            "Lâche prise et profite ! 🎈"
    };

    public SimpleGameFragment(String gameTitle) {
        Bundle args = new Bundle();
        args.putString("TITLE", gameTitle);
        setArguments(args);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_game, container, false);

        tvTitle = view.findViewById(R.id.tvTitle);
        tvMessage = view.findViewById(R.id.tvMessage);
        btnAction = view.findViewById(R.id.btnAction);

        String title = getArguments() != null ? getArguments().getString("TITLE") : "Jeu Détente";
        tvTitle.setText(title);

        setupGame();

        return view;
    }

    private void setupGame() {
        btnAction.setOnClickListener(v -> {
            clickCount++;
            String message = funMessages[random.nextInt(funMessages.length)];
            tvMessage.setText(message + " (Click #" + clickCount + ")");

            // Animation amusante
            btnAction.animate()
                    .scaleX(0.9f).scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> btnAction.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(100));

            // Changer la couleur du bouton aléatoirement
            int[] colors = {
                    getResources().getColor(R.color.soft_purple),
                    getResources().getColor(R.color.soft_pink),
                    getResources().getColor(R.color.accent_green)
            };
            btnAction.setBackgroundColor(colors[random.nextInt(colors.length)]);
        });
    }
}