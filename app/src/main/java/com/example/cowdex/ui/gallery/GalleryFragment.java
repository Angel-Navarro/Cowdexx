package com.example.cowdex.ui.gallery;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cowdex.databinding.FragmentGalleryBinding;
import com.example.cowdex.ui.bdd.entities.Animal;
import com.example.cowdex.ui.bdd.entities.Categoria;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private GalleryViewModel viewModel;
    private List<Categoria> listaCategorias;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar spinner de sexo
        ArrayAdapter<String> sexoAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Macho", "Hembra"}
        );
        sexoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSexo.setAdapter(sexoAdapter);

        // Cargar categorías
        viewModel.getAllCategorias().observe(getViewLifecycleOwner(), categorias -> {
            listaCategorias = categorias;
            ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    categorias.stream().map(Categoria::getNombre).toArray(String[]::new)
            );
            categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCategoria.setAdapter(categoriaAdapter);
        });

        //Abrir calendario al hacer clic en el campo de fecha
        binding.editTextFechaNacimiento.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        binding.editTextFechaNacimiento.setText(fechaSeleccionada);
                    },
                    year, month, day
            );
            datePicker.show();
        });

        // Registrar animal
        binding.buttonRegistrarAnimal.setOnClickListener(v -> registrarAnimal());

        return root;
    }

    private void registrarAnimal() {
        if (listaCategorias == null || listaCategorias.isEmpty()) {
            Toast.makeText(getContext(), "Primero registre una categoría", Toast.LENGTH_SHORT).show();
            return;
        }

        String idAnimal = binding.editTextIdAnimal.getText().toString().trim();
        String nombre = binding.editTextNombre.getText().toString().trim();
        String raza = binding.editTextRaza.getText().toString().trim();
        String sexo = binding.spinnerSexo.getSelectedItem().toString();
        String vacunas = binding.editTextVacunas.getText().toString().trim();
        String observaciones = binding.editTextObservaciones.getText().toString().trim();
        String fechaStr = binding.editTextFechaNacimiento.getText().toString().trim();
        int categoriaId = listaCategorias.get(binding.spinnerCategoria.getSelectedItemPosition()).getId();

        if (nombre.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese un nombre o apodo", Toast.LENGTH_SHORT).show();
            return;
        }

        long fechaNacimiento = 0;
        if (!fechaStr.isEmpty()) {
            try {
                Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaStr);
                fechaNacimiento = date != null ? date.getTime() : 0;
            } catch (ParseException e) {
                Toast.makeText(getContext(), "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Animal animal = new Animal(
                idAnimal,
                nombre,
                nombre,
                raza,
                sexo,
                null,
                vacunas,
                observaciones,
                fechaNacimiento,
                categoriaId
        );

        viewModel.insertAnimal(animal);
        Toast.makeText(getContext(), "Animal registrado correctamente", Toast.LENGTH_SHORT).show();

        limpiarCampos();
    }

    private void limpiarCampos() {
        binding.editTextIdAnimal.setText("");
        binding.editTextNombre.setText("");
        binding.editTextRaza.setText("");
        binding.editTextVacunas.setText("");
        binding.editTextObservaciones.setText("");
        binding.editTextFechaNacimiento.setText("");
        binding.spinnerSexo.setSelection(0);
        if (binding.spinnerCategoria.getAdapter() != null && binding.spinnerCategoria.getAdapter().getCount() > 0)
            binding.spinnerCategoria.setSelection(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
