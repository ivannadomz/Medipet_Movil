package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class PetActivity extends AppCompatActivity {

    private TextView petName, petBirthday, petWeight, petAllergies, petSpecie, petRace;
    private ImageView petImage, backButton;
    private LinearLayout infoContainer;
    private SharedPreferences prefs;
    private String token;
    private int petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_profile_main);

        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (token == null) {
            redirectToWelcome();
            return;
        }

        petId = getIntent().getIntExtra("pet_id", -1);
        if (petId == -1) {
            Toast.makeText(this, "Error al obtener la mascota", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        backButton = findViewById(R.id.back_button);
        petImage = findViewById(R.id.profile_image);
        petName = findViewById(R.id.pet_name);
        petBirthday = findViewById(R.id.pet_birthday);
        petWeight = findViewById(R.id.pet_weight);
        petAllergies = findViewById(R.id.pet_allergies);
        petSpecie = findViewById(R.id.pet_specie);
        petRace = findViewById(R.id.pet_race);
        infoContainer = findViewById(R.id.info_container);

        backButton.setOnClickListener(view -> finish());

        loadPetData();
    }

    private void redirectToWelcome() {
        Intent intent = new Intent(PetActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadPetData() {
        String url = "http://192.168.100.6:8000/api/pets/" + petId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject pet = response.getJSONObject("pet");
                        petName.setText(pet.getString("name"));
                        petBirthday.setText(pet.getString("birthday"));
                        petWeight.setText(pet.getString("weight") + " kg");
                        petAllergies.setText(pet.getString("allergies"));
                        petSpecie.setText(pet.getString("specie"));
                        petRace.setText(pet.getString("race"));

                        // Validar si existe una URL de imagen antes de iniciar la descarga
                        String imageUrl = pet.optString("image_url", "");
                        if (!imageUrl.isEmpty()) {
                            new LoadImageTask(petImage).execute(imageUrl);
                        }

                        infoContainer.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar datos de la mascota", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al cargar datos de la mascota", Toast.LENGTH_SHORT).show();
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

    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                Log.e("LoadImageTask", "Error cargando imagen", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}
