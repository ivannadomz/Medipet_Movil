package uaq.mx.medipet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductsActivity extends AppCompatActivity {
    public String url_products = "http://192.168.100.6:8000/api/products";

    private LinearLayout productContainer;
    private String token;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        //Contenedor de productos
        productContainer = findViewById(R.id.productsContainer);

        //Obtener token
        prefs = getSharedPreferences("auth", MODE_PRIVATE);
        token = prefs.getString("token", null);
        if (token == null) {
            redirectToWelcome();
            return;
        }

        setupButtons();
        loadProducts();
    }

    //Enviar a welcome si no hay token
    private void redirectToWelcome() {
        Intent intent = new Intent(ProductsActivity.this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Configuracion de botones logout, profile, add pet, etc.
    private void setupButtons() {
        //SignOut
        ImageView signOutIcon = findViewById(R.id.icon_folder);
        signOutIcon.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("token");
            editor.apply();
            redirectToWelcome();
        });

        //Profile
        ImageView profileIcon = findViewById(R.id.icon_profile);
        profileIcon.setOnClickListener(v -> {
            startActivity(new Intent(ProductsActivity.this, ProfileActivity.class));
        });

        //Menu
        findViewById(R.id.menu_home).setOnClickListener(v -> {
            startActivity(new Intent(ProductsActivity.this, HomeActivity.class));
        });

        //Products
        findViewById(R.id.menu_products).setOnClickListener(v -> {
            recreate();
        });

        //Appointments
        findViewById(R.id.menu_appointments).setOnClickListener(v -> {
            startActivity(new Intent(ProductsActivity.this, AppointmentsActivity.class));
        });
    }

    //Cargar todos los productos
    private void loadProducts() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url_products, null,
                response -> {
                    try {
                        JSONArray products = response.getJSONArray("products");
                        productContainer.removeAllViews();
                        for (int i = 0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);
                            int id = product.getInt("id");
                            String productName = product.getString("name");
                            String price = product.getString("price");
                            String description = product.getString("description");
                            String category = product.getString("category");
                            addProduct(productName, price, description, category);

                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error al procesar productos", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show();
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

    //Crear contenedor de producto
    private void  addProduct(String productName, String price, String description, String category) {
        // Contenedor padre vertical
        LinearLayout parentContainer = new LinearLayout(this);
        parentContainer.setOrientation(LinearLayout.VERTICAL);
        parentContainer.setBackgroundResource(R.drawable.rounded_light_grayish_blue_border);
        LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, // ancho
                LinearLayout.LayoutParams.WRAP_CONTENT // alto
        );
        parentParams.setMargins(dpToPx(0), dpToPx(8), dpToPx(0), dpToPx(8));
        parentContainer.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        parentContainer.setLayoutParams(parentParams);

        // Subcontenedor para los 3 TextViews (producto, categoría, descripción)
        LinearLayout textContainer = new LinearLayout(this);
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textContainer.setLayoutParams(textContainerParams);

        // TextView: Nombre del producto
        TextView tvProduct = new TextView(this);
        tvProduct.setText(productName);
        tvProduct.setTextSize(18);
        tvProduct.setTypeface(null, Typeface.BOLD);
        LinearLayout.LayoutParams tvProductParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvProductParams.setMargins(0, 0, 0, dpToPx(5));
        tvProduct.setLayoutParams(tvProductParams);

        // TextView: Categoría del producto
        TextView tvProductCat = new TextView(this);
        tvProductCat.setText(category);
        tvProductCat.setTextSize(12);

        // TextView: Descripción del producto
        TextView tvProductDes = new TextView(this);
        tvProductDes.setText(description);
        tvProductDes.setTextSize(12);

        // TextView: Precio del producto (fuera del contenedor interno)
        TextView tvProductPrice = new TextView(this);
        tvProductPrice.setText(price);
        tvProductPrice.setTextSize(16);
        LinearLayout.LayoutParams priceParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.setMargins(dpToPx(5), 0, dpToPx(5), 0);
        tvProductPrice.setLayoutParams(priceParams);

        // Armado de la estructura
        textContainer.addView(tvProduct);
        textContainer.addView(tvProductCat);
        textContainer.addView(tvProductDes);

        parentContainer.addView(textContainer);
        parentContainer.addView(tvProductPrice);

        // Agregamos al contenedor padre (de tu layout XML)
        productContainer.addView(parentContainer);
    }

    private int dpToPx(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
