package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private Button buttonLogin;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_main);

        editEmail = findViewById(R.id.email_input);
        editPassword = findViewById(R.id.password_input);
        buttonLogin = findViewById(R.id.login_button);

        requestQueue = Volley.newRequestQueue(this);

        buttonLogin.setOnClickListener(v -> loginUser());

        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> back());

        TextView forgotPasswordLink = findViewById(R.id.forgot_password_link);
        forgotPasswordLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public void back() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        JSONObject loginData = new JSONObject();
        try {
            loginData.put("email", email);
            loginData.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "http://148.220.215.137:8000/api/login",
                loginData,
                response -> {
                    try {
                        String token = response.getString("access_token");
                        JSONObject user = response.getJSONObject("user");
                        String userId = user.getString("id");

                        // Guardar token y user_id
                        SharedPreferences prefs = getSharedPreferences("auth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("token", token);
                        editor.putString("user_id", userId);
                        editor.apply();

                        Toast.makeText(this, "Login correcto", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, HomeActivity.class));

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error procesando respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show();
                    Log.e("LOGIN", "Error: " + error.toString());
                }
        );

        requestQueue.add(request);
    }
}
