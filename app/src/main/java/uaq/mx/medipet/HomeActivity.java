package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout petContainer;
    private String token;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_main);

        petContainer = findViewById(R.id.petContainer);

        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (token == null) {
            redirectToWelcome();
            return;
        }

        setupButtons();
        loadUserPets();
    }

    private void setupButtons() {
        ImageView signOutIcon = findViewById(R.id.icon_folder);
        signOutIcon.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("token");
            editor.apply();
            redirectToWelcome();
        });

        ImageView profileIcon = findViewById(R.id.icon_profile);
        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        ImageButton addPetFab = findViewById(R.id.add_pet_fab);
        addPetFab.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, RegisterPetActivity.class));
        });

        findViewById(R.id.menu_home).setOnClickListener(v -> {
            // Ya estamos en HomeActivity, no hacemos nada
        });

        findViewById(R.id.menu_products).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProductsActivity.class));
        });

        findViewById(R.id.menu_appointments).setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, AppointmentsActivity.class));
        });
    }

    private void redirectToWelcome() {
        Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadUserPets() {
        String url = "http://192.168.100.6:8000/api/pets";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray pets = response.getJSONArray("pets");
                        petContainer.removeAllViews();
                        for (int i = 0; i < pets.length(); i++) {
                            JSONObject pet = pets.getJSONObject(i);
                            int id = pet.getInt("id");
                            String name = pet.getString("name");
                            addPetButton(id, name);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar mascotas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    private void addPetButton(int id, String petName) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        int widthInPx = dpToPx(160);
        int heightInPx = dpToPx(180);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(widthInPx, heightInPx);
        int margin = dpToPx(8);
        containerParams.setMargins(margin, margin, margin, margin);
        container.setLayoutParams(containerParams);
        container.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageButton petButton = new ImageButton(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(140)
        );
        petButton.setLayoutParams(imgParams);
        petButton.setImageResource(R.drawable.image_fingerprint);
        petButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        petButton.setBackground(ContextCompat.getDrawable(this, R.drawable.rounded_pet));
        petButton.setContentDescription(petName);

        petButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PetActivity.class);
            intent.putExtra("pet_id", id);
            startActivity(intent);
        });

        TextView nameText = new TextView(this);
        nameText.setText(petName);
        nameText.setTextColor(ContextCompat.getColor(this, android.R.color.black));
        nameText.setTextSize(16);
        nameText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameText.setLayoutParams(textParams);

        container.addView(petButton);
        container.addView(nameText);

        petContainer.addView(container);
    }

    private int dpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}

