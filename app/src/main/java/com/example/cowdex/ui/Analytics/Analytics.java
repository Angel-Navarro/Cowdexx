package com.example.cowdex.ui.Analytics;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.cowdex.R;
import com.example.cowdex.ui.slideshow.SlideshowViewModel;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Analytics extends Fragment {

    private AnalyticsViewModel mViewModel;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseFirestore db;
    private SlideshowViewModel slideshowViewModel;

    private TextView tvTotalAnimales;
    private TextView tvTotalMachos;
    private TextView tvTotalHembras;
    private TextView tvTotalUsuarios;
    private TextView tvVisitasHome;
    private TextView tvVisitasGallery;
    private TextView tvVisitasSlideshow;
    private TextView tvVisitasUbicacion;

    private SharedPreferences sharedPrefs;

    public static Analytics newInstance() {
        return new Analytics();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        // Inicializar Firebase
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireContext());
        db = FirebaseFirestore.getInstance();

        // Registrar que se abrió Analytics
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, "Analytics Dashboard");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);

        // SharedPreferences para contar visitas localmente
        sharedPrefs = requireContext().getSharedPreferences("AppAnalytics", Context.MODE_PRIVATE);

        // Incrementar contador de vista de Analytics
        incrementarVisita("analytics");

        // Inicializar vistas
        tvTotalAnimales = view.findViewById(R.id.tvTotalAnimales);
        tvTotalMachos = view.findViewById(R.id.tvTotalMachos);
        tvTotalHembras = view.findViewById(R.id.tvTotalHembras);
        tvTotalUsuarios = view.findViewById(R.id.tvTotalUsuarios);
        tvVisitasHome = view.findViewById(R.id.tvVisitasHome);
        tvVisitasGallery = view.findViewById(R.id.tvVisitasGallery);
        tvVisitasSlideshow = view.findViewById(R.id.tvVisitasSlideshow);
        tvVisitasUbicacion = view.findViewById(R.id.tvVisitasUbicacion);

        // Inicializar ViewModels
        slideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);

        // Cargar datos
        cargarEstadisticas();
        cargarUsuarios();
        cargarVisitas();

        return view;
    }

    private void incrementarVisita(String vista) {
        int visitas = sharedPrefs.getInt(vista, 0);
        sharedPrefs.edit().putInt(vista, visitas + 1).apply();
    }

    private void cargarEstadisticas() {
        // Observar animales
        slideshowViewModel.getAnimalesFiltrados().observe(getViewLifecycleOwner(), animales -> {
            if (animales != null) {
                int totalAnimales = animales.size();
                int machos = 0;
                int hembras = 0;

                for (int i = 0; i < animales.size(); i++) {
                    String sexo = animales.get(i).getSexo();
                    if ("Macho".equalsIgnoreCase(sexo)) {
                        machos++;
                    } else if ("Hembra".equalsIgnoreCase(sexo)) {
                        hembras++;
                    }
                }

                tvTotalAnimales.setText(String.valueOf(totalAnimales));
                tvTotalMachos.setText(String.valueOf(machos));
                tvTotalHembras.setText(String.valueOf(hembras));

                // Registrar en Analytics
                Bundle statsBundle = new Bundle();
                statsBundle.putInt("total_animales", totalAnimales);
                statsBundle.putInt("total_machos", machos);
                statsBundle.putInt("total_hembras", hembras);
                mFirebaseAnalytics.logEvent("dashboard_stats_loaded", statsBundle);
            } else {
                tvTotalAnimales.setText("0");
                tvTotalMachos.setText("0");
                tvTotalHembras.setText("0");
            }
        });
    }

    private void cargarUsuarios() {
        // Contar usuarios en Firebase Authentication
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalUsuarios = queryDocumentSnapshots.size();
                    tvTotalUsuarios.setText(String.valueOf(totalUsuarios));

                    // Registrar en Analytics
                    Bundle userBundle = new Bundle();
                    userBundle.putInt("total_users", totalUsuarios);
                    mFirebaseAnalytics.logEvent("total_users_counted", userBundle);
                })
                .addOnFailureListener(e -> {
                    // Si no existe la colección, usar método alternativo
                    tvTotalUsuarios.setText("1+");
                });
    }

    private void cargarVisitas() {
        int visitasHome = sharedPrefs.getInt("home", 0);
        int visitasGallery = sharedPrefs.getInt("gallery", 0);
        int visitasSlideshow = sharedPrefs.getInt("slideshow", 0);
        int visitasUbicacion = sharedPrefs.getInt("ubicacion", 0);

        tvVisitasHome.setText(visitasHome + " visitas");
        tvVisitasGallery.setText(visitasGallery + " visitas");
        tvVisitasSlideshow.setText(visitasSlideshow + " visitas");
        tvVisitasUbicacion.setText(visitasUbicacion + " visitas");

        // Registrar en Analytics
        Bundle visitasBundle = new Bundle();
        visitasBundle.putInt("home_visits", visitasHome);
        visitasBundle.putInt("gallery_visits", visitasGallery);
        visitasBundle.putInt("slideshow_visits", visitasSlideshow);
        visitasBundle.putInt("ubicacion_visits", visitasUbicacion);
        mFirebaseAnalytics.logEvent("screen_visits_summary", visitasBundle);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
    }
}