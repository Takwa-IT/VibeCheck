package com.example.vibecheck;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.vibecheck.databinding.ActivityPsychologistListBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Header;

public class PsychologistListActivity extends AppCompatActivity {
    private static final String TAG = "NominatimAPI";
    private ActivityPsychologistListBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private final List<Psychologist> psychologists = new ArrayList<>();
    private PsychologistAdapter adapter;

    // Interface Retrofit pour Nominatim API
    public interface NominatimApi {
        @GET("search")
        Call<List<NominatimPlace>> searchPlaces(
                @Query("q") String query,
                @Query("format") String format,
                @Query("limit") int limit,
                @Query("addressdetails") int addressDetails,
                @Header("User-Agent") String userAgent
        );
    }

    // Modèle de données pour Nominatim
    public static class NominatimPlace {
        @SerializedName("place_id")
        public long placeId;

        @SerializedName("lat")
        public String lat;

        @SerializedName("lon")
        public String lon;

        @SerializedName("display_name")
        public String displayName;

        @SerializedName("type")
        public String type;

        @SerializedName("address")
        public Address address;
    }

    public static class Address {
        @SerializedName("road")
        public String road;

        @SerializedName("city")
        public String city;

        @SerializedName("postcode")
        public String postcode;
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    getCurrentLocation();
                } else {
                    Toast.makeText(this, "Permission requise pour trouver des psychologues", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPsychologistListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupRecyclerView();

        binding.tvLocationPrompt.setText("Active ta localisation pour trouver des psychologues 🌍");
        binding.btnShareLocation.setOnClickListener(v -> requestPermissions());
    }

    private void requestPermissions() {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        binding.tvLocationPrompt.setText("📍 Recherche en cours...");
        binding.btnShareLocation.setEnabled(false);

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                binding.tvLocationPrompt.setText("📍 Localisation activée");
                searchNearbyPsychologists(location.getLatitude(), location.getLongitude());
            } else {
                binding.tvLocationPrompt.setText("⚠️ Localisation introuvable. Réessaie.");
                binding.btnShareLocation.setEnabled(true);
            }
        });
    }

    private void searchNearbyPsychologists(double lat, double lng) {
        // Créer Retrofit avec logging
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nominatim.openstreetmap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        NominatimApi api = retrofit.create(NominatimApi.class);

        // Recherche 1: Psychologues
        searchAndAdd(api, "psychologue near " + lat + "," + lng, lat, lng, () -> {
            // Recherche 2: Psychiatres (après 1 seconde - limite Nominatim)
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                searchAndAdd(api, "psychiatre near " + lat + "," + lng, lat, lng, () -> {
                    // Recherche 3: Thérapeutes (après encore 1 seconde)
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        searchAndAdd(api, "thérapeute near " + lat + "," + lng, lat, lng, () -> {
                            finalizeSearch(lat, lng);
                        });
                    }, 1100);
                });
            }, 1100);
        });
    }

    private void searchAndAdd(NominatimApi api, String query, double userLat, double userLng, Runnable onComplete) {
        // User-Agent obligatoire pour Nominatim (politique d'usage)
        String userAgent = "VibeCheck/1.0 (Android Mental Health App)";

        api.searchPlaces(query, "json", 10, 1, userAgent).enqueue(new Callback<List<NominatimPlace>>() {
            @Override
            public void onResponse(@NonNull Call<List<NominatimPlace>> call, @NonNull Response<List<NominatimPlace>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (NominatimPlace place : response.body()) {
                        try {
                            double placeLat = Double.parseDouble(place.lat);
                            double placeLng = Double.parseDouble(place.lon);
                            double distance = calculateDistance(userLat, userLng, placeLat, placeLng);

                            String name = extractName(place.displayName);
                            String address = place.displayName;
                            String specialty = determineSpecialty(query);

                            // Éviter les doublons
                            if (!isDuplicate(name)) {
                                psychologists.add(new Psychologist(
                                        name,
                                        specialty + " • " + address,
                                        String.format("%.1f km", distance),
                                        0.0,
                                        null, // Nominatim ne fournit pas de téléphone
                                        "https://www.google.com/maps/search/?api=1&query=" + placeLat + "," + placeLng
                                ));
                            }
                        } catch (NumberFormatException e) {
                            Log.e(TAG, "Erreur parsing coordonnées", e);
                        }
                    }
                    updateRecyclerView();
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(@NonNull Call<List<NominatimPlace>> call, @NonNull Throwable t) {
                Log.e(TAG, "Erreur API: " + t.getMessage());
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void finalizeSearch(double lat, double lng) {
        binding.btnShareLocation.setEnabled(true);
        if (psychologists.isEmpty()) {
            binding.tvLocationPrompt.setText("Aucun psychologue trouvé. Ouvre Google Maps ?");
            openGoogleMapsSearch(lat, lng);
        } else {
            Toast.makeText(this, psychologists.size() + " professionnel(s) trouvé(s)", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractName(String displayName) {
        // Extraire le nom avant la première virgule
        if (displayName.contains(",")) {
            return displayName.split(",")[0].trim();
        }
        return displayName;
    }

    private String determineSpecialty(String query) {
        if (query.contains("psychiatre")) return "Psychiatre";
        if (query.contains("thérapeute")) return "Thérapeute";
        return "Psychologue";
    }

    private boolean isDuplicate(String name) {
        for (Psychologist psy : psychologists) {
            if (psy.name.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, results);
        return results[0] / 1000; // km
    }

    private void openGoogleMapsSearch(double lat, double lng) {
        String mapsUrl = "https://www.google.com/maps/search/?api=1&query=psychologue+psychiatre&center=" + lat + "," + lng;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl));
        startActivity(intent);
    }

    private void setupRecyclerView() {
        adapter = new PsychologistAdapter(psychologists, this);
        binding.rvPsychologists.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPsychologists.setAdapter(adapter);
    }

    private void updateRecyclerView() {
        adapter.notifyDataSetChanged();
    }

    public static class Psychologist {
        String name, specialty, distance, phoneUri, websiteUrl;
        double rating;

        public Psychologist(String name, String specialty, String distance, double rating, String phoneUri, String websiteUrl) {
            this.name = name;
            this.specialty = specialty;
            this.distance = distance;
            this.rating = rating;
            this.phoneUri = phoneUri;
            this.websiteUrl = websiteUrl;
        }
    }
}