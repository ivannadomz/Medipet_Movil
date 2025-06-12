package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PetActivity extends AppCompatActivity {

    private TextView petNameTextView, petBirthdayTextView, petWeightTextView,
            petAllergiesTextView, petSpecieTextView, petRaceTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_profile_main);

        petNameTextView = findViewById(R.id.pet_name);
        petBirthdayTextView = findViewById(R.id.pet_birthday);
        petWeightTextView = findViewById(R.id.pet_weight);
        petAllergiesTextView = findViewById(R.id.pet_allergies);
        petSpecieTextView = findViewById(R.id.pet_specie);
        petRaceTextView = findViewById(R.id.pet_race);

        // Botón back
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Aquí regresa a HomeActivity
            Intent intent = new Intent(PetActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // cerrar esta actividad para no regresar aquí con back
        });

        cargarDatosMascota();
    }

    private void cargarDatosMascota() {
        String url = "http://192.168.100.6:8000/api/pets";

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray petsArray = response.getJSONArray("pets");
                        if (petsArray.length() == 0) {
                            Log.d("PetActivity", "No hay mascotas registradas.");
                            return;
                        }

                        JSONObject pet = petsArray.getJSONObject(0);

                        String name = pet.getString("name");
                        String birthdate = pet.getString("birthdate");
                        String weight = pet.getString("weight");
                        String allergies = pet.getString("allergies");

                        JSONObject specie = pet.optJSONObject("specie");
                        String specieName = (specie != null) ? specie.getString("specie") : "Sin especie";

                        JSONObject race = pet.optJSONObject("race");
                        String raceName = (race != null) ? race.getString("name") : "Sin raza";

                        petNameTextView.setText(name);
                        petBirthdayTextView.setText(birthdate);
                        petWeightTextView.setText(weight + " kg");
                        petAllergiesTextView.setText(allergies);
                        petSpecieTextView.setText(specieName);
                        petRaceTextView.setText(raceName);

                    } catch (JSONException e) {
                        Log.e("PetActivity", "Error JSON: " + e.getMessage());
                    }
                },
                error -> Log.e("PetActivity", "Error en la solicitud: " + error.toString())
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
