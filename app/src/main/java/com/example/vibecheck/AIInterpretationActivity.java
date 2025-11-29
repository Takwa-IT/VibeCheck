package com.example.vibecheck;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.vibecheck.databinding.ActivityAiInterpretationBinding;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AIInterpretationActivity extends AppCompatActivity {

    private static final String TAG = "GROQ_Interpretation";

    // Clé API sécurisée via BuildConfig (de local.properties)
    private static final String API_KEY = BuildConfig.GROK_API_KEY;
    private static final String BASE_URL = "https://api.groq.com/openai/v1/";

    private ActivityAiInterpretationBinding binding;

    // --- MODÈLES DE DONNÉES ET INTERFACE RETROFIT ---
    public interface GrokApi {
        @POST("chat/completions")
        Call<GrokResponse> getInterpretation(@Body GrokRequest request);
    }

    public static class GrokRequest {
        public String model = "llama-3.1-8b-instant";
        public List<Message> messages;
        public double temperature = 0.7;
        public int max_tokens = 200;

        public GrokRequest(String emotion) {
            this.messages = new ArrayList<>();
            String prompt = "Ton rôle est d'être un coach de bien-être digital, amical et rassurant. " +
                    "Analyse l'émotion suivante que l'utilisateur ressent : '" + emotion + "'. " +
                    "Réponds directement à l'utilisateur (utilise 'tu' ou 'vous'). " +
                    "Ta réponse doit être en français, concise (2-3 phrases), empathique et positive. " +
                    "Termine avec une suggestion simple et concrète pour l'aider à se sentir mieux (ex: exercice de respiration, petite pause, écouter une chanson). " +
                    "Utilise 1 ou 2 emojis pertinents pour un ton chaleureux. ✨";
            this.messages.add(new Message("user", prompt));
        }
    }

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static class GrokResponse {
        public List<Choice> choices;
        public Usage usage;

        public static class Choice {
            public Message message;
        }

        public static class Usage {
            public int prompt_tokens;
            public int completion_tokens;
            public int total_tokens;
        }
    }
    // --- FIN DES MODÈLES ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiInterpretationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Récupérer l'émotion depuis l'Intent
        String emotionFromIntent = getIntent().getStringExtra("EMOTION");

        // VÉRIFICATION CLÉ API
        if (API_KEY == null || API_KEY.isEmpty() || API_KEY.equals("no-key-defined") || !API_KEY.startsWith("gsk_")) {
            binding.tvInterpretation.setText("Erreur : clé API Groq manquante.\nVa sur console.groq.com/keys");
            return;
        }

        // Vérifier qu'on a bien une émotion
        if (emotionFromIntent != null && !emotionFromIntent.trim().isEmpty()) {
            binding.tvInterpretation.setText("Analyse en cours... Respire calmement ! 🌸");
            callGrokApi(emotionFromIntent);
        } else {
            binding.tvInterpretation.setText("Aucune émotion reçue. Retourne en arrière et choisis une émotion.");
        }

        // Boutons en bas
        binding.btnPsycho.setOnClickListener(v -> startActivity(new Intent(this, PsychologistListActivity.class)));
        binding.btnAmuse.setOnClickListener(v -> startActivity(new Intent(this, AmusementActivity.class)));

    }

    private void callGrokApi(String emotion) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Intercepteur pour Bearer Token
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request request = original.newBuilder()
                    .header("Authorization", "Bearer " + API_KEY)
                    .method(original.method(), original.body())
                    .build();
            return chain.proceed(request);
        });

        // Logging pour debug
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(logging);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GrokApi api = retrofit.create(GrokApi.class);
        GrokRequest request = new GrokRequest(emotion);

        api.getInterpretation(request).enqueue(new Callback<GrokResponse>() {
            @Override
            public void onResponse(@NonNull Call<GrokResponse> call, @NonNull Response<GrokResponse> response) {
                if (response.isSuccessful() && response.body() != null &&
                        response.body().choices != null && !response.body().choices.isEmpty()) {

                    String interpretation = response.body().choices.get(0).message.content.trim();
                    binding.tvInterpretation.setText(interpretation);
                    Log.d(TAG, "Succès Groq : " + interpretation);
                } else {
                    String errorBody = "Aucun détail";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Erreur lecture erreur", e);
                    }
                    Log.e(TAG, "Échec Groq. Code: " + response.code() + " | Erreur: " + errorBody);
                    showFallback("Réponse Groq invalide (code " + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GrokResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Échec réseau Groq", t);
                showFallback("Pas de connexion ou Groq HS.");
                Toast.makeText(AIInterpretationActivity.this, "Connexion instable. Réessaie !", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showFallback(String reason) {
        String fallback = "Je suis là avec toi. Prends une grande inspiration, tiens 4 secondes, puis expire lentement. Tu n'es pas seul·e. 🌿\n\n(Groq indisponible : " + reason + ")";
        binding.tvInterpretation.setText(fallback);
    }
}