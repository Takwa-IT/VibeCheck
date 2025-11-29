package com.example.vibecheck.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.vibecheck.R;

public class BreathingFragment extends Fragment {
    private TextView tvInstruction, tvTimer;
    private Button btnStart;
    private CountDownTimer breathingTimer;
    private boolean isBreathingActive = false;
    private int currentPhase = 0;
    private int cycleCount = 0;
    private final int MAX_CYCLES = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_breathing, container, false);

        tvInstruction = view.findViewById(R.id.tvInstruction);
        tvTimer = view.findViewById(R.id.tvTimer);
        btnStart = view.findViewById(R.id.btnStart);

        btnStart.setOnClickListener(v -> toggleBreathing());

        return view;
    }

    private void toggleBreathing() {
        if (!isBreathingActive) {
            startBreathingExercise();
        } else {
            stopBreathingExercise();
        }
    }

    private void startBreathingExercise() {
        isBreathingActive = true;
        cycleCount = 0;
        btnStart.setText("Arrêter");
        btnStart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.soft_pink));
        startBreathingCycle();
    }

    private void stopBreathingExercise() {
        isBreathingActive = false;
        btnStart.setText("Commencer");
        btnStart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.soft_purple));

        if (breathingTimer != null) {
            breathingTimer.cancel();
        }

        tvInstruction.setText("Prêt pour une séance de respiration ?");
        tvTimer.setText("Clique sur Commencer");
    }

    private void completeBreathingExercise() {
        isBreathingActive = false;
        btnStart.setText("Recommencer");
        btnStart.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.soft_green));
        tvInstruction.setText("Séance terminée ! 🌟");
        tvTimer.setText("Excellent travail");
    }

    private void startBreathingCycle() {
        if (cycleCount >= MAX_CYCLES) {
            completeBreathingExercise();
            return;
        }

        currentPhase = 0;
        startPhaseTimer();
    }

    private void startPhaseTimer() {
        if (!isBreathingActive) return;

        int duration;
        String instruction;
        String emoji;

        switch (currentPhase) {
            case 0: // Inspiration
                duration = 4000;
                instruction = "Inspirez profondément";
                emoji = "⬆️";
                break;
            case 1: // Rétention
                duration = 4000;
                instruction = "Retenez votre souffle";
                emoji = "⏸️";
                break;
            case 2: // Expiration
                duration = 6000;
                instruction = "Expirez lentement";
                emoji = "⬇️";
                break;
            default:
                duration = 4000;
                instruction = "Inspirez profondément";
                emoji = "⬆️";
        }

        tvInstruction.setText(instruction + " " + emoji);

        breathingTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000) + 1;
                tvTimer.setText(String.valueOf(secondsRemaining));
            }

            public void onFinish() {
                currentPhase++;
                if (currentPhase >= 3) {
                    // Cycle complet terminé
                    cycleCount++;
                    if (cycleCount < MAX_CYCLES) {
                        tvInstruction.setText("Cycle " + cycleCount + "/" + MAX_CYCLES + " terminé");
                        getView().postDelayed(() -> {
                            currentPhase = 0;
                            startBreathingCycle();
                        }, 2000);
                    } else {
                        completeBreathingExercise();
                    }
                } else {
                    startPhaseTimer();
                }
            }
        }.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (breathingTimer != null) {
            breathingTimer.cancel();
        }
    }
}