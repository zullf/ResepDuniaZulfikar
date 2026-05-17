package com.example.resepduniazulfikar;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.resepduniazulfikar.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ArrayList<Meal> meals = new ArrayList<>();
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Konfigurasi Toolbar (Material 3)
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Katalog Dessert");
        }

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            loadDataFromApi();
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    binding.swipeRefreshLayout.setRefreshing(false), 1500);
        });

        setupRecyclerView();

        loadDataFromApi();
    }

    private void setupRecyclerView() {
        // Deteksi orientasi untuk menentukan jumlah kolom (Grid)
        int spanCount = (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) ? 2 : 4;

        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        binding.recyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new RecyclerViewAdapter(this, meals);
        binding.recyclerView.setAdapter(mAdapter);
    }

    public void loadDataFromApi() {
        binding.progressBar.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://www.themealdb.com/api/json/v1/1/filter.php?c=Dessert";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("meals");
                        meals.clear();

                        if (jsonArray.length() != 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject data = jsonArray.getJSONObject(i);
                                String id = data.getString("idMeal").trim();
                                String mealName = data.getString("strMeal").trim();
                                String photo = data.getString("strMealThumb").trim();

                                meals.add(new Meal(id, mealName, photo));
                            }
                            // Beritahu adapter bahwa data telah berubah
                            mAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    } finally {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Gagal memuat data. Periksa koneksi!", Toast.LENGTH_SHORT).show();
                }
        );
        queue.add(jsObjRequest);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Update jumlah kolom ketika layar diputar
        if (binding.recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            int spanCount = (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) ? 2 : 4;
            ((GridLayoutManager) binding.recyclerView.getLayoutManager()).setSpanCount(spanCount);
        }
    }
}