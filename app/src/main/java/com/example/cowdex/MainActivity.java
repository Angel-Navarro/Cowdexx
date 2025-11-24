package com.example.cowdex;

import android.os.Bundle;
import android.view.Menu;

import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cowdex.databinding.ActivityMainBinding;

//--
import android.content.Intent;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.analytics.FirebaseAnalytics;  // ← AGREGAR ESTA LÍNEA
import com.example.cowdex.ui.EmailCheckActivity.EmailCheckActivity;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseAnalytics mFirebaseAnalytics;  // ← Analytics

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);  // ← Inicializar Analytics

        // Verificar si el usuario está autenticado
        if (mAuth.getCurrentUser() == null) {
            // No hay sesión activa, redirigir al login
            Intent intent = new Intent(MainActivity.this, EmailCheckActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        } else {
            // Usuario autenticado - registrar evento en Analytics
            Bundle bundle = new Bundle();
            bundle.putString("user_email", mAuth.getCurrentUser().getEmail());
            bundle.putString("user_id", mAuth.getCurrentUser().getUid());
            mFirebaseAnalytics.logEvent("app_opened_authenticated", bundle);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationView navView = findViewById(R.id.nav_view);

        // Configuración de los destinos de nivel superior
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.home, R.id.nav_gallery, R.id.nav_slideshow, R.id.ubicacion2)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_backup) {
            performBackup();
            return true;
        } else if (id == R.id.action_restore) {
            performRestore();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        // Registrar evento de logout en Analytics
        Bundle bundle = new Bundle();
        bundle.putString("user_id", mAuth.getCurrentUser().getUid());
        mFirebaseAnalytics.logEvent("user_logout", bundle);

        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, EmailCheckActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void performBackup() {
        // Registrar evento de backup en Analytics
        Bundle bundle = new Bundle();
        bundle.putString("action", "backup_initiated");
        mFirebaseAnalytics.logEvent("backup_action", bundle);

        // Implementar lógica de respaldo aquí
    }

    private void performRestore() {
        // Registrar evento de restore en Analytics
        Bundle bundle = new Bundle();
        bundle.putString("action", "restore_initiated");
        mFirebaseAnalytics.logEvent("restore_action", bundle);

        // Implementar lógica de restauración aquí
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.home, R.id.nav_gallery, R.id.nav_slideshow, R.id.ubicacion2, R.id.analytics)
                .setOpenableLayout(drawer)
                .build();
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}