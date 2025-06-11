package uaq.mx.medipet;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public String url = "http://192.168.1.96:8000/api/owners";

    /*
    {
  "name": "Beto Naranjo",
  "email": "beto@email.com",
  "password": "password",
  "phone": "555123456379",
  "address": "direccion"
}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        login();
    }

    public void login () {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("name", "Pepe");
            jsonBody.put("email", "pepe11@gmail.com");
            jsonBody.put("phone", "10293847");
            jsonBody.put("password", "password");
            jsonBody.put("address", "direccion");
            System.out.println("post>>>"+jsonBody.toString());
        }catch (JSONException je){
            je.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    System.out.println("response>>>"+response.toString());
                    try {
                        System.out.println(response.getString("status"));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                }, error -> {
            System.out.println("Error" + error.toString());
        }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }

        };//end requ

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);
    }
}