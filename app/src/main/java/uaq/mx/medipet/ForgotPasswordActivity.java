package uaq.mx.medipet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_pass_email);

        emailInput = findViewById(R.id.email_input);

        TextView forgotPasswordButton = findViewById(R.id.btnRecover);
        forgotPasswordButton.setOnClickListener(v -> forgotPassword());

        TextView loginButton = findViewById(R.id.btnBackLogin);
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void forgotPassword() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Por favor, introduce un correo válido.", Toast.LENGTH_SHORT).show();
            return;
        }

        //Insert json
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
        } catch (JSONException je) {
            je.printStackTrace();
        }

        String url = "http://192.168.100.6:8000/api/forgot-password"; // ⚙️ Asegúrate de que la ruta sea /api/forgot-password

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(this, "Correo de recuperación enviado correctamente.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String message = "Error de conexión";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String body = new String(error.networkResponse.data, "UTF-8");
                            message = new JSONObject(body).optString("message");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                }
        );

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}