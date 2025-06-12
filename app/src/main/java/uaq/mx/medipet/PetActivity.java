package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PetActivity extends AppCompatActivity {

    private TextView petNameTextView, petBirthdayTextView, petWeightTextView,
            petAllergiesTextView, petSpecieTextView, petRaceTextView, petGenderTextView;

    private int petId;  // ID de la mascota que se recibe

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
        petGenderTextView = findViewById(R.id.pet_gender);

        // Obtener el pet_id enviado desde HomeActivity
        petId = getIntent().getIntExtra("pet_id", -1);
        if (petId == -1) {
            Log.e("PetActivity", "No se recibió el ID de la mascota");
            finish(); // Termina actividad si no hay ID válido
            return;
        }

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PetActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Configurar el botón para ir a citas y enviar el `pet_id`
        Button buttonAppointments = findViewById(R.id.button_go_to_appointments);
        buttonAppointments.setOnClickListener(v -> {
            Intent intent = new Intent(PetActivity.this, AppointmentsActivity.class);
            intent.putExtra("pet_id", petId);  // Se pasa el ID de la mascota
            startActivity(intent);
        });

        cargarDatosMascota();
    }

    private void cargarDatosMascota() {
        String url = "http://148.220.215.137:8000/api/pets/" + petId;

        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONObject pet = response.getJSONObject("pet");

                        String name = pet.getString("name");
                        String birthdate = pet.getString("birthdate");
                        String weight = pet.getString("weight");
                        String allergies = pet.getString("allergies");
                        String gender = pet.optString("gender", "No especificado");

                        JSONObject specie = pet.optJSONObject("specie");
                        String specieName = (specie != null) ? specie.optString("specie", "Sin especie") : "Sin especie";

                        JSONObject race = pet.optJSONObject("race");
                        String raceName = (race != null) ? race.optString("name", "Sin raza") : "Sin raza";

                        petNameTextView.setText(name);
                        petBirthdayTextView.setText(birthdate);
                        petWeightTextView.setText(weight + " kg");
                        petAllergiesTextView.setText(allergies);
                        petGenderTextView.setText(gender);
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
