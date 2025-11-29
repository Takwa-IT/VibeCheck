package com.example.vibecheck;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View; // Importation ajoutée
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.vibecheck.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "Login";
    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private boolean isRegisterMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "LoginActivity démarrée.");

        // Supprimé : Toute la configuration de Google Sign-In a été enlevée
        // GoogleSignInOptions gso = ...
        // mGoogleSignInClient = ...

        binding.btnLogin.setOnClickListener(v -> handleEmailAuth());
        // Supprimé : Le listener pour le bouton Google a été enlevé
        // binding.btnGoogle.setOnClickListener(...)
        binding.tvRegister.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        if (isRegisterMode) {
            binding.tvTitle.setText("Créer un compte");
            binding.btnLogin.setText("S'Inscrire");
            binding.tvRegister.setText("Déjà un compte ? Se connecter");
            // Affiche les champs Prénom et Nom
            binding.tilFirstName.setVisibility(View.VISIBLE);
            binding.tilLastName.setVisibility(View.VISIBLE);
        } else {
            binding.tvTitle.setText("Bienvenue 😊");
            binding.btnLogin.setText("Se Connecter");
            binding.tvRegister.setText("Pas de compte ? S'inscrire");
            // Cache les champs Prénom et Nom
            binding.tilFirstName.setVisibility(View.GONE);
            binding.tilLastName.setVisibility(View.GONE);
        }
        Log.d(TAG, "Mode basculé vers : " + (isRegisterMode ? "Inscription" : "Connexion"));
    }

    private void handleEmailAuth() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();

        if (isRegisterMode) {
            // Validation pour l'inscription
            if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Veuillez remplir tous les champs.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            // Validation pour la connexion
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Veuillez remplir l'e-mail et le mot de passe.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        if (isRegisterMode) {
            // Logique d'inscription
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Utilisateur créé avec succès.");
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Met à jour le profil de l'utilisateur avec son nom
                            updateUserProfile(user, firstName, lastName);
                        } else {
                            handleAuthFailure(task);
                        }
                    });
        } else {
            // Logique de connexion
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Utilisateur connecté avec succès.");
                            // Le nom est déjà dans le profil, on passe null
                            navigateToWelcome(null);
                        } else {
                            handleAuthFailure(task);
                        }
                    });
        }
    }

    // NOUVELLE MÉTHODE : Met à jour le nom de l'utilisateur dans son profil Firebase
    private void updateUserProfile(FirebaseUser user, String firstName, String lastName) {
        if (user == null) return;

        String fullName = firstName + " " + lastName;
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Profil utilisateur mis à jour avec le nom : " + fullName);
                    } else {
                        Log.w(TAG, "Échec de la mise à jour du profil.");
                    }
                    // Navigue vers l'écran de bienvenue, que la mise à jour du profil réussisse ou non
                    navigateToWelcome(fullName);
                });
    }

    // Supprimé : Les méthodes signInWithGoogle et firebaseAuthWithGoogle ont été enlevées

    private void navigateToWelcome(String defaultName) {
        runOnUiThread(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Log.e(TAG, "Erreur critique : utilisateur nul après une authentification réussie.");
                Toast.makeText(this, "Erreur d'authentification.", Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = defaultName;
            if (userName == null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    userName = user.getDisplayName();
                } else {
                    userName = user.getEmail().split("@")[0];
                }
            }

            Log.d(TAG, "Navigation vers WelcomeActivity avec le nom : " + userName);
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.putExtra("USER_NAME", userName);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // La méthode handleAuthFailure reste la même, elle est toujours utile
    private void handleAuthFailure(@NonNull Task<?> task) {
        String errorMessage = "L'authentification a échoué. Veuillez réessayer."; // Message par défaut
        Exception exception = task.getException();

        if (exception == null) {
            Log.e(TAG, "Échec de l'authentification sans exception retournée.");
            String finalErrorMessage1 = errorMessage;
            runOnUiThread(() -> Toast.makeText(LoginActivity.this, finalErrorMessage1, Toast.LENGTH_LONG).show());
            return;
        }

        Throwable cause = exception;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }

        if (cause instanceof FirebaseAuthException) {
            FirebaseAuthException firebaseAuthException = (FirebaseAuthException) cause;
            String errorCode = firebaseAuthException.getErrorCode();
            errorMessage = getFriendlyErrorMessage(errorCode);
            Log.w(TAG, "Échec de l'authentification Firebase. Code: " + errorCode, firebaseAuthException);
        } else {
            errorMessage = "Une erreur est survenue. Vérifiez votre connexion internet.";
            Log.e(TAG, "Échec avec une exception non-Firebase: " + exception.getClass().getName(), exception);
        }

        String finalErrorMessage = errorMessage;
        runOnUiThread(() -> Toast.makeText(LoginActivity.this, finalErrorMessage, Toast.LENGTH_LONG).show());
    }

    private String getFriendlyErrorMessage(String errorCode) {
        switch (errorCode) {
            case "auth/invalid-credential":
            case "auth/user-not-found":
            case "auth/wrong-password":
                return "E-mail ou mot de passe incorrect.";
            case "auth/invalid-email":
                return "Le format de l'e-mail est invalide.";
            case "auth/weak-password":
                return "Le mot de passe est trop faible (6 caractères minimum).";
            case "auth/email-already-in-use":
                return "Cet e-mail est déjà associé à un compte.";
            case "auth/network-request-failed":
                return "Problème de réseau. Vérifiez votre connexion Internet.";
            default:
                return "L'authentification a échoué. Code: " + errorCode;
        }
    }
}
