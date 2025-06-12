package uaq.mx.medipet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppointmentsActivity extends AppCompatActivity {

    private LinearLayout appointmentsContainer;
    private List<JSONObject> appointmentList = new ArrayList<>();
    private int petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appoinments);

        appointmentsContainer = findViewById(R.id.appointmentsContainer);

        petId = getIntent().getIntExtra("pet_id", -1);
        if (petId == -1) {
            Toast.makeText(this, "Error: No se recibió el ID de la mascota", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchAppointments();

        ImageView iconProfile = findViewById(R.id.icon_profile);
        iconProfile.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            String token = prefs.getString("token", null);
            if (token == null) {
                Toast.makeText(this, "Token no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = prefs.getInt("user_id", -1);
            if (userId == -1) {
                Toast.makeText(this, "ID de usuario no encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Obtener el ownerId del backend
            String url = "http://192.168.100.6:8000/api/owners/user/" + userId;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    response -> {
                        try {
                            int ownerId = response.getInt("id");
                            Intent intent = new Intent(AppointmentsActivity.this, ProfileActivity.class);
                            intent.putExtra("ownerId", ownerId);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Toast.makeText(this, "Error al procesar respuesta del servidor", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(this, "Error al obtener datos del propietario", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(request);
        });

        ImageView iconFolder = findViewById(R.id.icon_folder);
        iconFolder.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(AppointmentsActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        ImageButton menuProducts = findViewById(R.id.menu_products);
        menuProducts.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentsActivity.this, ProductsActivity.class);
            startActivity(intent);
        });

        ImageButton menuAppointments = findViewById(R.id.menu_appointments);
        menuAppointments.setOnClickListener(v -> {
            Toast.makeText(this, "Ya estás en la sección de citas", Toast.LENGTH_SHORT).show();
        });

        ImageButton menuHome = findViewById(R.id.menu_home);
        menuHome.setOnClickListener(v -> {
            Intent intent = new Intent(AppointmentsActivity.this, HomeActivity.class);
            startActivity(intent);
        });
    }

    private void fetchAppointments() {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "Token no encontrado. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            return;
        }

        String url = "http://192.168.100.6:8000/api/appointments/pet/" + petId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray appointments = response.optJSONArray("appointments");
                        appointmentList.clear();
                        appointmentsContainer.removeAllViews();

                        if (appointments != null && appointments.length() > 0) {
                            for (int i = 0; i < appointments.length(); i++) {
                                JSONObject appointment = appointments.getJSONObject(i);
                                appointmentList.add(appointment);
                                addAppointmentCard(appointment);
                            }
                        } else {
                            Toast.makeText(this, "No hay citas para esta mascota", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar los datos", Toast.LENGTH_LONG).show();
                    }
                },
                error -> Toast.makeText(this, "Error al conectar con el servidor", Toast.LENGTH_LONG).show()
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

    private void addAppointmentCard(JSONObject appointment) {
        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            View cardView = inflater.inflate(R.layout.item_appointment, appointmentsContainer, false);

            TextView tvReason = cardView.findViewById(R.id.tvReason);
            TextView tvDate = cardView.findViewById(R.id.tvDate);
            TextView tvStatus = cardView.findViewById(R.id.tvStatus);
            TextView tvVet = cardView.findViewById(R.id.tvVet);
            TextView tvBranch = cardView.findViewById(R.id.tvBranch);
            Button btnPrescription = cardView.findViewById(R.id.btnPrescription);
            Button btnCancel = cardView.findViewById(R.id.btnCancel);

            int appointmentId = appointment.getInt("id");
            String reason = appointment.optString("reason", "Sin motivo");
            String date = appointment.optString("appointment_date", "Fecha no disponible");
            String status = appointment.optString("status", "Estado no disponible");

            String vetName = "Veterinario no disponible";
            if (appointment.has("vet") && appointment.getJSONObject("vet").has("user")) {
                vetName = appointment.getJSONObject("vet").getJSONObject("user").optString("name", vetName);
            }

            String branchName = appointment.has("branch") ? appointment.getJSONObject("branch").optString("name", "Sucursal no disponible") : "Sucursal no disponible";

            tvReason.setText(reason);
            tvDate.setText("Fecha: " + date);
            tvStatus.setText("Estado: " + status);
            tvVet.setText("Veterinario: " + vetName);
            tvBranch.setText("Sucursal: " + branchName);

            btnPrescription.setOnClickListener(v -> {
                Intent i = new Intent(this, PrescriptionActivity.class);
                i.putExtra("appointment_id", appointmentId);
                startActivity(i);
            });

            if ("scheduled".equalsIgnoreCase(status)) {
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> cancelAppointment(appointmentId, tvStatus, btnCancel));
            } else {
                btnCancel.setVisibility(View.GONE);
            }

            appointmentsContainer.addView(cardView);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void cancelAppointment(int appointmentId, TextView tvStatus, Button btnCancel) {
        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null || appointmentId == -1) {
            Toast.makeText(this, "Error: No se puede cancelar la cita", Toast.LENGTH_LONG).show();
            return;
        }

        String url = "http://192.168.100.6:8000/api/appointments/" + appointmentId + "/cancel";

        StringRequest request = new StringRequest(Request.Method.PUT, url,
                response -> {
                    Toast.makeText(this, "Cita cancelada", Toast.LENGTH_SHORT).show();
                    tvStatus.setText("Estado: canceled");
                    btnCancel.setVisibility(View.GONE);
                },
                error -> Toast.makeText(this, "Error al cancelar la cita", Toast.LENGTH_LONG).show()
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
