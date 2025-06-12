package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private String token;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtener token
        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);
        if (token != null) {
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            redirectToWelcome();
        }

    }

    //Redirigir a la pagina de bienvinida si no
    private void redirectToWelcome() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}