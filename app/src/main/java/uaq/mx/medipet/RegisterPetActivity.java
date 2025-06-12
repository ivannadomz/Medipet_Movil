package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterPetActivity extends AppCompatActivity {

    public String url_pets = "http://192.168.1.96:8000/api/pets";
    public String url_breeds = "http://192.168.1.96:8000/api/races";
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

        //Obtener datos del usuario
        fetchCurrentUser();

        //Obtener especies y razas
        loadSpecies();

        //Obtener seleccion de species y breeds
        petSpeciesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpeciesModel selectedSpecies = (SpeciesModel) parent.getItemAtPosition(position);
                if (selectedSpecies.getId() != -1) {
                    loadBreeds(selectedSpecies.getId());
                } else {
                    petBreedsSpinner.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

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

    //Metodo para obtener las especies
    private void loadSpecies() {

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_species, null,
                response -> {
                    try {
                        JSONArray species = response.getJSONArray("species");

                        ArrayList<SpeciesModel> speciesList = new ArrayList<>();
                        speciesList.add(new SpeciesModel(-1, "Selecciona especie"));

                        for (int i = 0; i < species.length(); i++) {
                            JSONObject specie = species.getJSONObject(i);
                            int id = specie.getInt("id");
                            String name = specie.getString("specie");
                            speciesList.add(new SpeciesModel(id, name));
                        }

                        ArrayAdapter<SpeciesModel> speciesAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item,
                                speciesList);
                        speciesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        petSpeciesSpinner.setAdapter(speciesAdapter);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar especies", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al cargar especies", Toast.LENGTH_SHORT).show();
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

    //Modelo para las especies
    public class SpeciesModel {
        private int id;
        private String name;

        public SpeciesModel(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }

        @Override
        public String toString() {
            return name;
        }
    }

    //Metodo para obtener las especies
    private void loadBreeds(int speciesId) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = url_breeds + "?species_id=" + speciesId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray breeds = response.getJSONArray("races");

                        ArrayList<BreedsModel> breedsList = new ArrayList<>();
                        breedsList.add(new BreedsModel(-1, "Selecciona raza", 0));

                        for (int i = 0; i < breeds.length(); i++) {
                            JSONObject breed = breeds.getJSONObject(i);
                            int id = breed.getInt("id");
                            String name = breed.getString("name");
                            int species_Id = breed.getInt("species_id");
                            breedsList.add(new BreedsModel(id, name, species_Id));
                        }

                        ArrayAdapter<BreedsModel> breedsAdapter = new ArrayAdapter<>(this,
                                android.R.layout.simple_spinner_item,
                                breedsList);
                        breedsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        petBreedsSpinner.setAdapter(breedsAdapter);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar razas", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al cargar razas", Toast.LENGTH_SHORT).show();
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

    //Modelo para las especies
    public class BreedsModel {
        private int id;
        private String name;
        private int species_id;

        public BreedsModel(int id, String name, int species_id) {
            this.id = id;
            this.name = name;
            this.species_id = species_id;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getSpeciesId() { return species_id; }

        @Override
        public String toString() {
            return name;
        }
    }


    //Metodo para registar mascota
    public void registerPet() {
        //Convertir a strings los datos tomados
        String name = petNameEdit.getText().toString().trim();
        String birthdate = petBirthdateEdit.getText().toString().trim();
        String gender = petGenSpinner.getSelectedItem().toString().trim();
        String weight = petWeightEdit.getText().toString().trim();
        String allergies = petAllergiesEdit.getText().toString().trim();
        SpeciesModel selectedSpecies = (SpeciesModel) petSpeciesSpinner.getSelectedItem();
        BreedsModel selectedBreed = (BreedsModel) petBreedsSpinner.getSelectedItem();

        //Comprobar que no esten vacios  || species.isEmpty() || breeds.isEmpty()
        if (name.isEmpty() || birthdate.isEmpty() || gender.isEmpty() || weight.isEmpty() || allergies.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        //Validar que no seleccionen la opción por defecto
        if (gender.equals("Selecciona género") || selectedSpecies.getId() == -1 || selectedBreed.getId() == -1) {
            Toast.makeText(this, "Por favor selecciona un género, especie y raza.", Toast.LENGTH_SHORT).show();
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
            petData.put("species_id", selectedSpecies.getId());
            petData.put("race_id", selectedBreed.getId());
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
