package uaq.mx.medipet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private EditText editName, editEmail, editPhone, editAddress;
    private TextView editButton;
    private LinearLayout buttonsContainer;
    private Button btnCancel, btnUpdate;
    private ImageView backArrow;

    private SharedPreferences prefs;
    private String token;
    private String ownerId;

    private String originalName = "";
    private String originalEmail = "";
    private String originalPhone = "";
    private String originalAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_main);

        // Inicialización de vistas
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        editPhone = findViewById(R.id.edit_phone);
        editAddress = findViewById(R.id.edit_address);
        editButton = findViewById(R.id.edit_button);
        buttonsContainer = findViewById(R.id.buttons_container);
        btnCancel = findViewById(R.id.btn_cancel);
        btnUpdate = findViewById(R.id.btn_update);
        backArrow = findViewById(R.id.back_arrow);

        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);

        if (token == null) {
            Toast.makeText(this, "No se encontró token. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        fetchCurrentUser();

        editButton.setOnClickListener(v -> {
            setEditable(true);
            buttonsContainer.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        });

        btnCancel.setOnClickListener(v -> {
            restoreOriginalValues();
            setEditable(false);
            buttonsContainer.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        });

        btnUpdate.setOnClickListener(v -> updateUserProfile());

        backArrow.setOnClickListener(v -> finish());
    }

    private void setEditable(boolean editable) {
        editName.setEnabled(editable);
        editEmail.setEnabled(editable);
        editPhone.setEnabled(editable);
        editAddress.setEnabled(editable);
    }

    private void restoreOriginalValues() {
        editName.setText(originalName);
        editEmail.setText(originalEmail);
        editPhone.setText(originalPhone);
        editAddress.setText(originalAddress);
    }

    private void fetchCurrentUser() {
        String url = "http://192.168.100.6:8000/api/user";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
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

    private void fetchOwnerIdByUser(String userId) {
        String url = "http://192.168.100.6:8000/api/owners/by-user/" + userId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject ownerObj = response.getJSONObject("owner");
                        ownerId = ownerObj.getString("id");
                        loadUserProfile();
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

    private void loadUserProfile() {
        String url = "http://192.168.100.6:8000/api/owners/" + ownerId;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Obtenemos el objeto "owner"
                        JSONObject ownerObj = response.getJSONObject("owner");

                        // Del owner sacamos phone y address
                        originalPhone = ownerObj.optString("phone", "");
                        originalAddress = ownerObj.optString("address", "");

                        // Del objeto user dentro de owner sacamos name y email
                        JSONObject userObj = ownerObj.getJSONObject("user");
                        originalName = userObj.optString("name", "");
                        originalEmail = userObj.optString("email", "");

                        restoreOriginalValues();
                        setEditable(false);
                        buttonsContainer.setVisibility(View.GONE);
                        editButton.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error procesando perfil", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error cargando perfil", Toast.LENGTH_SHORT).show()) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        queue.add(request);
    }


    private void updateUserProfile() {
        String url = "http://192.168.100.6:8000/api/owners/" + ownerId;

        JSONObject body = new JSONObject();
        JSONObject userObj = new JSONObject();

        try {
            String phone = editPhone.getText().toString().trim();
            if (phone.isEmpty()) phone = originalPhone;
            body.put("phone", phone);

            String address = editAddress.getText().toString().trim();
            if (address.isEmpty()) address = originalAddress;
            body.put("address", address);

            String name = editName.getText().toString().trim();
            if (name.isEmpty()) name = originalName;
            userObj.put("name", name);

            String email = editEmail.getText().toString().trim();
            if (email.isEmpty()) email = originalEmail;
            userObj.put("email", email);

            body.put("user", userObj);

        } catch (JSONException e) {
            Toast.makeText(this, "Error preparando datos", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Body JSON PUT: " + body.toString());

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest putRequest = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();

                    originalName = editName.getText().toString();
                    originalEmail = editEmail.getText().toString();
                    originalPhone = editPhone.getText().toString();
                    originalAddress = editAddress.getText().toString();

                    setEditable(false);
                    buttonsContainer.setVisibility(View.GONE);
                    editButton.setVisibility(View.VISIBLE);
                },
                error -> {
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        String errorBody = new String(error.networkResponse.data);
                        Log.e(TAG, "Error body: " + errorBody);
                        Toast.makeText(this, "Error: " + errorBody, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Error actualizando perfil", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(putRequest);
    }

}


