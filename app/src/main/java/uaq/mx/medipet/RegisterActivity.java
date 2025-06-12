package uaq.mx.medipet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import android.content.Intent;

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

public class RegisterActivity extends AppCompatActivity {
    public String url = "http://148.220.215.137:8000/api/owners";

    EditText nameEdit, lastNameEdit, motherLastNameEdit,  emailEdit, phoneEdit, addressEdit, passEdit, confirmPassEdit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in_main);

        //Obtener datos de la ventana signin
        nameEdit = findViewById(R.id.name);
        lastNameEdit = findViewById(R.id.last_name);
        motherLastNameEdit = findViewById(R.id.mother_last_name);
        emailEdit = findViewById(R.id.email);
        phoneEdit = findViewById(R.id.phone);
        addressEdit = findViewById(R.id.address);
        passEdit = findViewById(R.id.pass);
        confirmPassEdit = findViewById(R.id.confirmPass);

        //Obtener boton para registrar
        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> registerUser());

        //Obtener boton para regresar
        ImageButton backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> back());
    }

    //metodo para regresar a la anterior ventana
    public void back() {
        startActivity(new Intent(this, MainActivity.class));
    }

    //metodo para registar usuario
    public void registerUser() {
        //Convertir a strings los datos tomados
        String name = nameEdit.getText().toString().trim();
        String lastName = lastNameEdit.getText().toString().trim();
        String motherLastName = motherLastNameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String phone = phoneEdit.getText().toString().trim();
        String address = addressEdit.getText().toString().trim();
        String password = passEdit.getText().toString().trim();
        String confirmPassword = confirmPassEdit.getText().toString().trim();

        //Comprobar que no esten vacios
        if (name.isEmpty() || lastName.isEmpty() || motherLastName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        //Juntar el nombre y apellidos
        name = name + " " + lastName + " " + motherLastName;

        //Comprobar que las contraseñas coincidan
        if (!password.equals(confirmPassword)){
            Toast.makeText(this, "Contreseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        //Insert json
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", name);
            jsonBody.put("email", email);
            jsonBody.put("phone", phone);
            jsonBody.put("address", address);
            jsonBody.put("password", password);
        } catch (JSONException je) {
            je.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    startActivity(new Intent(this, WelcomeActivity.class));
                    Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show();
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