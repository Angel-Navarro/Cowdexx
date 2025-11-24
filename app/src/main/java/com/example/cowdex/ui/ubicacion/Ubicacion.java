package com.example.cowdex.ui.ubicacion;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.cowdex.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

//Analytics
import android.content.SharedPreferences;
import android.content.Context;

public class Ubicacion extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "UbicacionFragment";
    private UbicacionViewModel mViewModel;
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    public static Ubicacion newInstance() {
        return new Ubicacion();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView - Inflando layout");

        //Analytics
        SharedPreferences prefs = requireContext().getSharedPreferences("AppAnalytics", Context.MODE_PRIVATE);
        prefs.edit().putInt("ubicacion", prefs.getInt("ubicacion", 0) + 1).apply();

        return inflater.inflate(R.layout.fragment_ubicacion, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated - Inicializando mapa");

        // IMPORTANTE: Inicializar el SupportMapFragment
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "MapFragment encontrado, esperando mapa...");
        } else {
            Log.e(TAG, "ERROR: MapFragment es null");
            Toast.makeText(requireContext(), "Error al cargar el mapa", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UbicacionViewModel.class);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady - Mapa listo para usar");
        mMap = googleMap;

        // Configurar ubicación en Aguascalientes, México
        LatLng aguascalientes = new LatLng(21.8853, -102.2916);

        mMap.addMarker(new MarkerOptions()
                .position(aguascalientes)
                .title("Aguascalientes")
                .snippet("México"));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(aguascalientes, 12));

        // Configurar UI del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);

        // Habilitar ubicación del usuario
        enableMyLocation();

        Toast.makeText(requireContext(), "Mapa cargado correctamente", Toast.LENGTH_SHORT).show();
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (mMap != null) {
                try {
                    mMap.setMyLocationEnabled(true);
                    Log.d(TAG, "Ubicación del usuario habilitada");
                } catch (SecurityException e) {
                    Log.e(TAG, "Error de permisos: " + e.getMessage());
                }
            }
        } else {
            // Solicitar permisos
            Log.d(TAG, "Solicitando permisos de ubicación");
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permisos concedidos");
                enableMyLocation();
            } else {
                Log.d(TAG, "Permisos denegados");
                Toast.makeText(requireContext(),
                        "Permiso de ubicación denegado",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}