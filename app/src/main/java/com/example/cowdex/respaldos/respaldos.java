    package com.example.cowdex.respaldos;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.annotation.NonNull;
    import androidx.annotation.Nullable;
    import androidx.core.content.ContextCompat;
    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;

    import android.Manifest;
    import android.app.AlertDialog;
    import android.content.ContentValues;
    import android.content.pm.PackageManager;
    import android.net.Uri;
    import android.os.Build;
    import android.os.Bundle;
    import android.os.Environment;
    import android.provider.MediaStore;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.Button;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.example.cowdex.R;
    import com.example.cowdex.ui.bdd.database.AppDatabase;  // ← CORRECTO
    import com.example.cowdex.ui.bdd.entities.Animal;
    import com.example.cowdex.ui.bdd.entities.Categoria;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.FirebaseFirestore;

    import org.apache.poi.ss.usermodel.Cell;
    import org.apache.poi.ss.usermodel.Row;
    import org.apache.poi.ss.usermodel.Sheet;
    import org.apache.poi.ss.usermodel.Workbook;
    import org.apache.poi.xssf.usermodel.XSSFWorkbook;

    import java.io.OutputStream;
    import java.text.SimpleDateFormat;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Locale;
    import java.util.Map;
    import java.util.concurrent.Executors;

    public class respaldos extends Fragment {

        private RespaldosViewModel mViewModel;
        private FirebaseAuth mAuth;
        private FirebaseFirestore db;
        private AppDatabase roomDb;

        private Button btnRespaldar, btnRestaurar, btnExportar;
        private ProgressBar progressBar;
        private TextView tvEstado, tvUltimoRespaldo;

        private ActivityResultLauncher<String> requestPermissionLauncher;

        public static respaldos newInstance() {
            return new respaldos();
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_respaldos, container, false);

            // Inicializar Firebase y Room
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();
            roomDb = AppDatabase.getDatabase(requireContext());  // ← CORRECTO

            // Inicializar vistas
            btnRespaldar = view.findViewById(R.id.btnRespaldar);
            btnRestaurar = view.findViewById(R.id.btnRestaurar);
            btnExportar = view.findViewById(R.id.btnExportar);
            progressBar = view.findViewById(R.id.progressBar);
            tvEstado = view.findViewById(R.id.tvEstado);
            tvUltimoRespaldo = view.findViewById(R.id.tvUltimoRespaldo);

            // Configurar launcher de permisos
            requestPermissionLauncher = registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            exportarAExcel();
                        } else {
                            Toast.makeText(requireContext(), "Permiso denegado", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Cargar último respaldo
            cargarUltimoRespaldo();

            // Listeners
            btnRespaldar.setOnClickListener(v -> respaldarDatos());
            btnRestaurar.setOnClickListener(v -> mostrarDialogRestaurar());
            btnExportar.setOnClickListener(v -> verificarPermisosYExportar());

            return view;
        }

        private void cargarUltimoRespaldo() {
            String email = mAuth.getCurrentUser().getEmail();
            db.collection("users").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists() && documentSnapshot.contains("createdAt")) {
                            long timestamp = documentSnapshot.getLong("createdAt");
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            tvUltimoRespaldo.setText("Último respaldo: " + sdf.format(new Date(timestamp)));
                        }
                    });
        }

        private void respaldarDatos() {
            btnRespaldar.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            tvEstado.setText("Respaldando...");

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    // Obtener datos de Room (MÉTODOS SÍNCRONOS)
                    List<Animal> animales = roomDb.animalDao().getAllAnimalsSync();  // ← CAMBIO
                    List<Categoria> categorias = roomDb.categoriaDao().getAllCategoriasSync();  // ← CAMBIO

                    // Preparar datos para Firestore
                    Map<String, Object> backup = new HashMap<>();
                    backup.put("email", mAuth.getCurrentUser().getEmail());
                    backup.put("createdAt", System.currentTimeMillis());
                    backup.put("totalAnimales", animales.size());
                    backup.put("totalCategorias", categorias.size());

                    // Convertir animales a Map
                    for (int i = 0; i < animales.size(); i++) {
                        Animal animal = animales.get(i);
                        Map<String, Object> animalMap = new HashMap<>();
                        animalMap.put("idAnimal", animal.getIdAnimal());
                        animalMap.put("nombre", animal.getNombre());
                        animalMap.put("apodo", animal.getApodo());
                        animalMap.put("raza", animal.getRaza());
                        animalMap.put("sexo", animal.getSexo());
                        animalMap.put("fechaNacimiento", animal.getFechaNacimiento());
                        animalMap.put("categoriaId", animal.getCategoriaId());
                        animalMap.put("vacunas", animal.getVacunas());
                        animalMap.put("observaciones", animal.getObservaciones());

                        backup.put("animal_" + i, animalMap);
                    }

                    // Convertir categorías a Map
                    for (int i = 0; i < categorias.size(); i++) {
                        Categoria categoria = categorias.get(i);
                        Map<String, Object> categoriaMap = new HashMap<>();
                        categoriaMap.put("id", categoria.getId());
                        categoriaMap.put("nombre", categoria.getNombre());

                        backup.put("categoria_" + i, categoriaMap);
                    }

                    // Subir a Firestore
                    String email = mAuth.getCurrentUser().getEmail();
                    db.collection("users").document(email)
                            .set(backup)
                            .addOnSuccessListener(aVoid -> {
                                requireActivity().runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    tvEstado.setText("✓ Respaldo completado");
                                    btnRespaldar.setEnabled(true);
                                    cargarUltimoRespaldo();
                                    Toast.makeText(requireContext(), "Respaldo exitoso", Toast.LENGTH_SHORT).show();
                                });
                            })
                            .addOnFailureListener(e -> {
                                requireActivity().runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    tvEstado.setText("✗ Error al respaldar");
                                    btnRespaldar.setEnabled(true);
                                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEstado.setText("✗ Error");
                        btnRespaldar.setEnabled(true);
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }

        private void mostrarDialogRestaurar() {
            new AlertDialog.Builder(requireContext())
                    .setTitle("⚠️ Restaurar Datos")
                    .setMessage("Esto eliminará todos los datos actuales y los reemplazará con el respaldo. ¿Continuar?")
                    .setPositiveButton("Restaurar", (dialog, which) -> restaurarDatos())
                    .setNegativeButton("Cancelar", null)
                    .show();
        }

        private void restaurarDatos() {
            btnRestaurar.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            tvEstado.setText("Restaurando...");

            String email = mAuth.getCurrentUser().getEmail();
            db.collection("users").document(email).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) {
                            Toast.makeText(requireContext(), "No hay respaldo disponible", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                            btnRestaurar.setEnabled(true);
                            return;
                        }

                        Executors.newSingleThreadExecutor().execute(() -> {
                            try {
                                // Limpiar base de datos local
                                roomDb.animalDao().deleteAll();
                                roomDb.categoriaDao().deleteAll();

                                // Restaurar categorías
                                int totalCategorias = documentSnapshot.getLong("totalCategorias").intValue();
                                for (int i = 0; i < totalCategorias; i++) {
                                    Map<String, Object> categoriaMap = (Map<String, Object>) documentSnapshot.get("categoria_" + i);
                                    if (categoriaMap != null) {
                                        Categoria categoria = new Categoria();
                                        categoria.setId(((Long) categoriaMap.get("id")).intValue());
                                        categoria.setNombre((String) categoriaMap.get("nombre"));
                                        roomDb.categoriaDao().insert(categoria);
                                    }
                                }

                                // Restaurar animales
                                int totalAnimales = documentSnapshot.getLong("totalAnimales").intValue();
                                for (int i = 0; i < totalAnimales; i++) {
                                    Map<String, Object> animalMap = (Map<String, Object>) documentSnapshot.get("animal_" + i);
                                    if (animalMap != null) {
                                        Animal animal = new Animal();
                                        animal.setIdAnimal((String) animalMap.get("idAnimal"));
                                        animal.setNombre((String) animalMap.get("nombre"));
                                        animal.setApodo((String) animalMap.get("apodo"));
                                        animal.setRaza((String) animalMap.get("raza"));
                                        animal.setSexo((String) animalMap.get("sexo"));
                                        animal.setFechaNacimiento((Long) animalMap.get("fechaNacimiento"));
                                        animal.setCategoriaId(((Long) animalMap.get("categoriaId")).intValue());
                                        animal.setVacunas((String) animalMap.get("vacunas"));
                                        animal.setObservaciones((String) animalMap.get("observaciones"));
                                        roomDb.animalDao().insert(animal);
                                    }
                                }

                                requireActivity().runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    tvEstado.setText("✓ Restauración completada");
                                    btnRestaurar.setEnabled(true);
                                    Toast.makeText(requireContext(), "Datos restaurados exitosamente", Toast.LENGTH_LONG).show();
                                });

                            } catch (Exception e) {
                                requireActivity().runOnUiThread(() -> {
                                    progressBar.setVisibility(View.GONE);
                                    tvEstado.setText("✗ Error al restaurar");
                                    btnRestaurar.setEnabled(true);
                                    Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        tvEstado.setText("✗ Error");
                        btnRestaurar.setEnabled(true);
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }

        private void verificarPermisosYExportar() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                exportarAExcel();
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    exportarAExcel();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        }

        private void exportarAExcel() {
            btnExportar.setEnabled(false);
            progressBar.setVisibility(View.VISIBLE);
            tvEstado.setText("Exportando...");

            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    List<Animal> animales = roomDb.animalDao().getAllAnimalsSync();

                    StringBuilder csv = new StringBuilder();
                    csv.append("ID Animal,Nombre,Apodo,Raza,Sexo,FechaNacimiento,CategoriaID,Vacunas,Observaciones\n");

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

                    for (Animal a : animales) {
                        csv.append(a.getIdAnimal()).append(",");
                        csv.append(a.getNombre()).append(",");
                        csv.append(a.getApodo()).append(",");
                        csv.append(a.getRaza()).append(",");
                        csv.append(a.getSexo()).append(",");
                        csv.append(a.getFechaNacimiento() > 0 ? sdf.format(new Date(a.getFechaNacimiento())) : "").append(",");
                        csv.append(a.getCategoriaId()).append(",");
                        csv.append(a.getVacunas()).append(",");
                        csv.append(a.getObservaciones()).append("\n");
                    }

                    String fileName = "Animales_" +
                            new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
                            ".csv";

                    Uri uri;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        uri = requireContext().getContentResolver()
                                .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                    } else {
                        java.io.File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        java.io.File file = new java.io.File(downloads, fileName);
                        uri = Uri.fromFile(file);
                    }

                    OutputStream os = requireContext().getContentResolver().openOutputStream(uri);
                    os.write(csv.toString().getBytes());
                    os.close();

                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEstado.setText("✓ Archivo exportado");
                        btnExportar.setEnabled(true);
                        Toast.makeText(requireContext(),
                                "Exportado a Descargas: " + fileName,
                                Toast.LENGTH_LONG).show();
                    });

                } catch (Exception e) {
                    requireActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        tvEstado.setText("✗ Error al exportar");
                        btnExportar.setEnabled(true);
                        Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }


        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            mViewModel = new ViewModelProvider(this).get(RespaldosViewModel.class);
        }
    }