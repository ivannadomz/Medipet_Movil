package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
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

public class PrescriptionActivity extends AppCompatActivity {
    public String url = "http://192.168.100.6:8000/api/prescriptions?appointment_id=";

    private String token;
    private SharedPreferences prefs;

    // ID de la cita que se recibe para buscar prescripción
    private int appointmentId;

    private TextView reasonTextView, diagnosisTextView, dateTextView, treatmentTextView, specificationsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prescription);

        // Obtener token
        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);
        if (token == null) {
            redirectToWelcome();
            return;
        }

        // Encontrar los elementos de la vista
        reasonTextView = findViewById(R.id.tvReason);
        diagnosisTextView = findViewById(R.id.tvDiagnosis);
        dateTextView = findViewById(R.id.tvDatePrescription);
        treatmentTextView = findViewById(R.id.tvTreatment);
        specificationsTextView = findViewById(R.id.tvSpecifications);

        // Obtener el appointment_id enviado desde AppointmentsActivity
        appointmentId = getIntent().getIntExtra("appointment_id", -1);
        if (appointmentId == -1) {
            Log.e("PrescriptionActivity", "No se recibió el ID de la cita");
            finish(); // Termina actividad si no hay ID válido
            return;
        }

        // Botón de regresar
        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(PrescriptionActivity.this, AppointmentsActivity.class);
            startActivity(intent);
            finish();
        });

        cargarDatosPrescripcion();
    }

    private void redirectToWelcome() {
        Intent intent = new Intent(PrescriptionActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void cargarDatosPrescripcion() {
        String fullUrl = url + appointmentId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                fullUrl,
                null,
                response -> {
                    Log.d("PrescriptionActivity", "Respuesta JSON completa: " + response.toString());

                    try {
                        if (!response.has("prescriptions")) {
                            Log.e("PrescriptionActivity", "No existe 'prescriptions' en la respuesta");
                            return;
                        }

                        // Obtener arreglo de prescripciones
                        JSONArray prescriptionsArray = response.getJSONArray("prescriptions");

                        if (prescriptionsArray.length() == 0) {
                            Log.e("PrescriptionActivity", "El arreglo 'prescriptions' está vacío");
                            return;
                        }

                        // Obtener la primera prescripción
                        JSONObject prescription = prescriptionsArray.getJSONObject(0);

                        String reason = prescription.getString("reason");
                        String diagnosis = prescription.getString("diagnosis");
                        String date = prescription.getString("date");
                        String treatment = prescription.getString("treatment");
                        String specifications = prescription.getString("specifications");

                        reasonTextView.setText(reason);
                        diagnosisTextView.setText(diagnosis);
                        dateTextView.setText(date);
                        treatmentTextView.setText(treatment);
                        specificationsTextView.setText(specifications);

                    } catch (JSONException e) {
                        Log.e("PrescriptionActivity", "Error JSON: " + e.getMessage());
                    }
                },
                error -> Log.e("PrescriptionActivity", "Error en la solicitud: " + error.toString())
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
