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

public class QuizFragment extends Fragment {

    private TextView tvQuestion, tvResult;
    private Button btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private String[] questions;
    private String[][] answers;
    private int[] correctAnswers;
    private int currentQuestion = 0;
    private int score = 0;
    private Random random = new Random();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz, container, false);

        initializeQuizData();
        findViews(view);
        setupClickListeners();
        loadQuestion();

        return view;
    }

    private void initializeQuizData() {
        questions = new String[]{
                "Quel animal représente le mieux votre personnalité ?",
                "Comment vous détendez-vous ?",
                "Quel est votre environnement préféré ?",
                "Quelle qualité vous décrit le mieux ?"
        };

        answers = new String[][]{
                {"Lion 🦁 - Leader naturel", "Dauphin 🐬 - Social et joyeux", "Aigle 🦅 - Visionnaire", "Ours 🐻 - Calme et réfléchi"},
                {"Meditation 🧘", "Sport 🏃", "Lecture 📚", "Musique 🎵"},
                {"Forêt 🌳", "Mer 🌊", "Montagne ⛰️", "Ville 🌆"},
                {"Patience 🕰️", "Courage 💪", "Créativité 🎨", "Empathie ❤️"}
        };

        correctAnswers = new int[]{0, 1, 2, 3}; // Toutes les réponses sont "correctes" pour un quiz de personnalité
    }

    private void findViews(View view) {
        tvQuestion = view.findViewById(R.id.tvQuestion);
        tvResult = view.findViewById(R.id.tvResult);
        btnAnswer1 = view.findViewById(R.id.btnAnswer1);
        btnAnswer2 = view.findViewById(R.id.btnAnswer2);
        btnAnswer3 = view.findViewById(R.id.btnAnswer3);
        btnAnswer4 = view.findViewById(R.id.btnAnswer4);
    }

    private void setupClickListeners() {
        btnAnswer1.setOnClickListener(v -> checkAnswer(0));
        btnAnswer2.setOnClickListener(v -> checkAnswer(1));
        btnAnswer3.setOnClickListener(v -> checkAnswer(2));
        btnAnswer4.setOnClickListener(v -> checkAnswer(3));
    }

    private void loadQuestion() {
        if (currentQuestion < questions.length) {
            tvQuestion.setText(questions[currentQuestion]);
            btnAnswer1.setText(answers[currentQuestion][0]);
            btnAnswer2.setText(answers[currentQuestion][1]);
            btnAnswer3.setText(answers[currentQuestion][2]);
            btnAnswer4.setText(answers[currentQuestion][3]);
            tvResult.setText("Question " + (currentQuestion + 1) + "/" + questions.length);
        } else {
            showFinalResult();
        }
    }

    private void checkAnswer(int selectedAnswer) {
        score += (selectedAnswer == correctAnswers[currentQuestion]) ? 1 : 0;
        currentQuestion++;

        if (currentQuestion < questions.length) {
            loadQuestion();
        } else {
            showFinalResult();
        }
    }

    private void showFinalResult() {
        String[] results = {
                "🦁 Lion - Leader né ! Vous inspirez les autres",
                "🐬 Dauphin - Social et énergique !",
                "🦅 Aigle - Visionnaire et indépendant !",
                "🐻 Ours - Sage et réfléchi !"
        };

        int resultIndex = Math.min(score, results.length - 1);
        tvQuestion.setText("Quiz Terminé !");
        tvResult.setText(results[resultIndex]);

        btnAnswer1.setText("Recommencer");
        btnAnswer2.setVisibility(View.GONE);
        btnAnswer3.setVisibility(View.GONE);
        btnAnswer4.setVisibility(View.GONE);

        btnAnswer1.setOnClickListener(v -> restartQuiz());
    }

    private void restartQuiz() {
        currentQuestion = 0;
        score = 0;
        btnAnswer2.setVisibility(View.VISIBLE);
        btnAnswer3.setVisibility(View.VISIBLE);
        btnAnswer4.setVisibility(View.VISIBLE);
        setupClickListeners();
        loadQuestion();
    }
}