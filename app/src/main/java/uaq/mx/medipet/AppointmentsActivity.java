package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AppointmentsActivity extends AppCompatActivity {

    private TextView tvReasonAppointment, tvDate, tvStatus, tvVet, tvBranch;
    private Button btnPrescription, btnCancelAppointment;

    private int currentAppointmentId = -1;
    private String currentAppointmentStatus = "";
    private int petId;  // ID de la mascota que se recibe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appoinments);

        tvReasonAppointment = findViewById(R.id.tvReasonAppointment);
        tvDate = findViewById(R.id.tvDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvVet = findViewById(R.id.tvVet);
        tvBranch = findViewById(R.id.tvBranch);
        btnPrescription = findViewById(R.id.btnPrescription);
        btnCancelAppointment = findViewById(R.id.btnCancelAppointment);

        btnCancelAppointment.setVisibility(View.GONE); // Oculto por defecto

        // Recibir el `pet_id` enviado desde `PetActivity`
        petId = getIntent().getIntExtra("pet_id", -1);
        if (petId == -1) {
            Toast.makeText(this, "Error: No se recibió el ID de la mascota", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchAppointments();
    }

    private void fetchAppointments() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Token no encontrado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        // Modificar la URL para filtrar citas por `pet_id`
        String url = "http://192.168.100.6:8000/api/appointments?pet_id=" + petId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray appointments = response.getJSONArray("appointments");

                        if (appointments.length() > 0) {
                            JSONObject appointment = appointments.getJSONObject(0);

                            currentAppointmentId = appointment.optInt("id", -1);
                            currentAppointmentStatus = appointment.optString("status", "");

                            String reason = appointment.optString("reason", "Sin motivo");
                            String date = appointment.optString("appointment_date", "Fecha no disponible");

                            // Obtener nombre veterinario y sucursal desde JSON anidado
                            String vetName = appointment.has("vet") ? appointment.getJSONObject("vet").optString("name", "Veterinario no disponible") : "Veterinario no disponible";
                            String branchName = appointment.has("branch") ? appointment.getJSONObject("branch").optString("name", "Sucursal no disponible") : "Sucursal no disponible";

                            tvReasonAppointment.setText(reason);
                            tvDate.setText("Fecha: " + date);
                            tvStatus.setText("Estado: " + currentAppointmentStatus);
                            tvVet.setText("Veterinario: " + vetName);
                            tvBranch.setText("Sucursal: " + branchName);

                            // Mostrar botón cancelar solo si está en estado scheduled
                            if ("scheduled".equalsIgnoreCase(currentAppointmentStatus)) {
                                btnCancelAppointment.setVisibility(View.VISIBLE);
                            } else {
                                btnCancelAppointment.setVisibility(View.GONE);
                            }

                            btnPrescription.setOnClickListener(v -> {
                                if (currentAppointmentId != -1) {
                                    Intent intent = new Intent(AppointmentsActivity.this, PrescriptionActivity.class);
                                    intent.putExtra("appointment_id", currentAppointmentId);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(this, "ID de cita no disponible", Toast.LENGTH_SHORT).show();
                                }
                            });

                        } else {
                            Toast.makeText(this, "No hay citas para esta mascota", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
