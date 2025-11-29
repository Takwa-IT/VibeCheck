package com.example.vibecheck;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    private TextView tvUserName;
    private MaterialButton btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Lier les vues du layout
        tvUserName = findViewById(R.id.tvUserName);
        btnStart = findViewById(R.id.btnStart);

        // Récupérer le nom de l'utilisateur passé par LoginActivity
        String userName = getIntent().getStringExtra("USER_NAME");

        // Si le nom est vide ou nul, essayons de le récupérer depuis le profil Firebase
        if (TextUtils.isEmpty(userName)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && !TextUtils.isEmpty(user.getDisplayName())) {
                userName = user.getDisplayName();
            } else {
                // Fallback si tout échoue
                userName = "Utilisateur";
                Log.w(TAG, "Le nom d'utilisateur n'a pas pu être récupéré. Utilisation d'un nom par défaut.");
            }
        }

        // Afficher le nom de l'utilisateur
        tvUserName.setText(userName + " !");

        // Mettre en place le listener pour le bouton "Commencer"
        btnStart.setOnClickListener(view -> {
            Log.d(TAG, "Le bouton 'Commencer' a été cliqué. Navigation vers l'activité principale.");
            navigateToNextPage();
        });
    }

    private void navigateToNextPage() {
        // Remplacez 'EmotionDashboardActivity.class' par le nom de votre prochaine activité
        Intent intent = new Intent(WelcomeActivity.this, EmotionDashboardActivity.class);
        startActivity(intent);
        // On termine l'activité WelcomeActivity pour qu'on ne puisse pas y revenir avec le bouton "Retour"
        finish();
    }
}
