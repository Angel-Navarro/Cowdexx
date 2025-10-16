package com.example.cowdex;

import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.example.cowdex.ui.EmailCheckActivity.EmailCheckActivity;
//import com.tuapp.activities.EmailCheckActivity;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Verificar si el usuario está autenticado
        if (mAuth.getCurrentUser() == null) {
            // No hay sesión activa, redirigir al login
            Intent intent = new Intent(MainActivity.this, EmailCheckActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavigationView navView = findViewById(R.id.nav_view);

        // Configuración de los destinos de nivel superior
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
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
            // Aquí puedes llamar al método de respaldo
            performBackup();
            return true;
        } else if (id == R.id.action_restore) {
            // Aquí puedes llamar al método de restauración
            performRestore();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(MainActivity.this, EmailCheckActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void performBackup() {
        // Implementar lógica de respaldo aquí
        // Ejemplo usando FirestoreHelper:
        /*
        FirestoreHelper helper = new FirestoreHelper();
        File dbFile = getDatabasePath("tu_base_datos.db");

        helper.uploadBackupFile(dbFile, new FirestoreHelper.OnUploadListener() {
            @Override
            public void onSuccess(String downloadUrl) {
                Toast.makeText(MainActivity.this, "Respaldo completado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int progress) {
                // Actualizar barra de progreso
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    private void performRestore() {
        // Implementar lógica de restauración aquí
        // Ejemplo usando FirestoreHelper:
        /*
        FirestoreHelper helper = new FirestoreHelper();
        File destinationFile = new File(getFilesDir(), "restored_backup.db");

        helper.downloadBackupFile(destinationFile, new FirestoreHelper.OnDownloadListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Restauración completada", Toast.LENGTH_SHORT).show();
                // Aquí deberías reemplazar la base de datos actual con el respaldo
            }

            @Override
            public void onProgress(int progress) {
                // Actualizar barra de progreso
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        */
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

}