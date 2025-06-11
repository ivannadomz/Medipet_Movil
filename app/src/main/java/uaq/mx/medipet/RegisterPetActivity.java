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

import com.android.volley.AuthFailureError;
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

    public String url_pets = "http://192.168.1.96:8000/api/pets";
    public String url_races = "http://192.168.1.96:8000/api/races";
    public String url_species = "http://192.168.1.96:8000/api/species";
    public String url_user = "http://192.168.1.96:8000/api/user";
    public String url_owner = "http://192.168.1.96:8000/api/owners/by-user/";

    EditText petNameEdit, petBirthdateEdit, petWeightEdit, petAllergiesEdit;
    Spinner petSpeciesSpinner, petBreedsSpinner, petGenSpinner;

    private String token;
    private SharedPreferences prefs;
    private String ownerId;

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

        //Obtener token
        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);
        if (token == null) {
            redirectToWelcome();
            return;
        }

        fetchCurrentUser();

        //Obtener boton para registrar
        Button registerButton = findViewById(R.id.register_pet_button);
        registerButton.setOnClickListener(v -> registerPet());

        //Obtener boton para regresar
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> back());
    }

    //Redirigir a la pagina de bienvinida si no
    private void redirectToWelcome() {
        Intent intent = new Intent(RegisterPetActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //metodo para regresar a la anterior ventana
    public void back() {
        startActivity(new Intent(this, MainActivity.class));
    }

    //Metodo para cargar el perfil del usuario
    private void fetchCurrentUser() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_user, null,
                response -> {
                    try {
                        String userId = response.getString("id");
                        fetchOwnerIdByUser(userId);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error procesando usuario", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(this, "No se pudo obtener usuario", Toast.LENGTH_SHORT).show();
                    finish();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    //Metodo para obtener el id del dueño
    private void fetchOwnerIdByUser(String userId) {
        url_owner = url_owner + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_owner, null,
                response -> {
                    try {
                        JSONObject ownerObj = response.getJSONObject("owner");
                        ownerId = ownerObj.getString("id");
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error procesando dueño", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(this, "Dueño no encontrado", Toast.LENGTH_SHORT).show();
                    finish();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }

    //Metodo para registar mascota
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

        //Validar que no seleccionen la opción por defecto || species.equals("Selecciona especie") || breeds.equals("Selecciona raza")
        if (gender.equals("Selecciona género") ) {
            Toast.makeText(this, "Por favor selecciona un género, raza y epecie.", Toast.LENGTH_SHORT).show();
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        //Insert json
        JSONObject petData = new JSONObject();
        try {
            petData.put("name", name);
            petData.put("birthdate", birthdate);
            petData.put("gender", gender);
            petData.put("weight", weight);
            petData.put("allergies", allergies);
            petData.put("species_id", 1);
            petData.put("race_id", 1);
            petData.put("owner_id", ownerId);
        } catch (JSONException je) {
            je.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url_pets,
                petData,
                response -> {
                    startActivity(new Intent(this, HomeActivity.class));
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
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(jsonObjectRequest);
    }
}
