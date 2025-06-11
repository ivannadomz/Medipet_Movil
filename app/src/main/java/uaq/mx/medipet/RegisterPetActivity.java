package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterPetActivity extends AppCompatActivity {

    public String url = "http://192.168.1.96:8000/api/pets";
    //public String url1 = "http://192.168.1.96:8000/api/races";
    //public String url2 = "http://192.168.1.96:8000/api/species";

    EditText petNameEdit, petBirthdateEdit, petWeightEdit, petAllergiesEdit;
    Spinner petSpeciesSpinner, petBreedsSpinner, petGenSpinner;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_pet_main);

        //Obtener datos de la ventana signin
        petNameEdit = findViewById(R.id.pet_name);
        petBirthdateEdit = findViewById(R.id.pet_birthday);
        petWeightEdit = findViewById(R.id.pet_weight);
        petAllergiesEdit = findViewById(R.id.pet_allergies);

        //Obtener seleccion de species y breeds
        petSpeciesSpinner = findViewById(R.id.pet_species_text);
        petBreedsSpinner = findViewById(R.id.pet_breed_text);
        petGenSpinner = findViewById(R.id.pet_gender);

        // Crear adaptador con opciones "H" y "M"
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Selecciona género", "H", "M"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        petGenSpinner.setAdapter(genderAdapter);

        //Obtener boton para registrar
        Button registerButton = findViewById(R.id.register_pet_button);
        registerButton.setOnClickListener(v -> registerPet());

        //Obtener boton para regresar
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> back());
    }

    //metodo para regresar a la anterior ventana
    public void back() {
        startActivity(new Intent(this, MainActivity.class));
    }

    //metodo para registar mascota
    public void registerPet() {
        //Convertir a strings los datos tomados
        String name = petNameEdit.getText().toString().trim();
        String birthdate = petBirthdateEdit.getText().toString().trim();
        String gender = petGenSpinner.getSelectedItem().toString().trim();
        String weight = petWeightEdit.getText().toString().trim();
        String allergies = petAllergiesEdit.getText().toString().trim();
        //String species = petSpeciesSpinner.getSelectedItem().toString().trim();
        //String breeds = petBreedsSpinner.getSelectedItem().toString().trim();

        //Comprobar que no esten vacios  || species.isEmpty() || breeds.isEmpty()
        if (name.isEmpty() || birthdate.isEmpty() || gender.isEmpty() || weight.isEmpty() || allergies.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        //Validar que no seleccionen la opción por defecto
        if (gender.equals("Selecciona género")) {
            Toast.makeText(this, "Por favor selecciona un género.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Insert json
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("birthdate", birthdate);
            jsonBody.put("gender", gender);
            jsonBody.put("weight", weight);
            jsonBody.put("allergies", allergies);
            jsonBody.put("species_id", 1);
            jsonBody.put("race_id", 1);
            jsonBody.put("owner_id", 31);
        } catch (JSONException je) {
            je.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    startActivity(new Intent(this, MainActivity.class));
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show(); // Delay de 1 segundo
                },
                error -> {
                    if (error.networkResponse != null) {
                        String errorMessage = new String(error.networkResponse.data);
                        Toast.makeText(this, "Error del servidor: " + errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "No se pudo conectar al servidor", Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                headers.put("Accept", "application/json");

                //SharedPreferences preferences = getSharedPreferences("user_data", MODE_PRIVATE);
                //String token = preferences.getString("auth_token", "");

                String token = ("8|TxHaHjnm8eZ3Xhx49krU91hBk7O6mwyq2kPUdqbz125dd2e6");

                if (!token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }

                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}
