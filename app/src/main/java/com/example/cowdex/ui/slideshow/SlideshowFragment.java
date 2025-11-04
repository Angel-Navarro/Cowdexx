package com.example.cowdex.ui.slideshow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cowdex.R;
import com.example.cowdex.databinding.FragmentSlideshowBinding;
import com.example.cowdex.ui.bdd.entities.Animal;
import com.example.cowdex.ui.bdd.entities.Categoria;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SlideshowFragment extends Fragment implements AnimalAdapter.OnAnimalClickListener {

    private FragmentSlideshowBinding binding;
    private SlideshowViewModel viewModel;
    private AnimalAdapter adapter;
    private List<Categoria> listaCategorias = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
        binding = FragmentSlideshowBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Configurar RecyclerView
        adapter = new AnimalAdapter(this);
        binding.recyclerViewAnimales.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewAnimales.setAdapter(adapter);

        // Observar animales filtrados
        viewModel.getAnimalesFiltrados().observe(getViewLifecycleOwner(), animales -> {
            adapter.setAnimales(animales);
            if (animales == null || animales.isEmpty()) {
                binding.recyclerViewAnimales.setVisibility(View.GONE);
                binding.textViewNoAnimales.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewAnimales.setVisibility(View.VISIBLE);
                binding.textViewNoAnimales.setVisibility(View.GONE);
            }
        });

        // Cargar categorías para el filtro
        viewModel.getAllCategorias().observe(getViewLifecycleOwner(), categorias -> {
            listaCategorias = categorias;
            List<String> nombresCategorias = new ArrayList<>();
            nombresCategorias.add("Todas las categorías");
            for (Categoria categoria : categorias) {
                nombresCategorias.add(categoria.getNombre());
            }

            ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    nombresCategorias
            );
            categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerFiltroCategoria.setAdapter(categoriaAdapter);
        });

        // Listener para filtro de categoría
        binding.spinnerFiltroCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.filtrarPorCategoria(-1); // Todas las categorías
                } else {
                    int categoriaId = listaCategorias.get(position - 1).getId();
                    viewModel.filtrarPorCategoria(categoriaId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Listener para búsqueda
        binding.editTextBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.buscarAnimales(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return root;
    }

    @Override
    public void onEditClick(Animal animal) {
        mostrarDialogEditar(animal);
    }

    @Override
    public void onDeleteClick(Animal animal) {
        mostrarDialogEliminar(animal);
    }

    private void mostrarDialogEditar(Animal animal) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_editar_animal, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Referencias a los campos del diálogo
        EditText editIdAnimal = dialogView.findViewById(R.id.editTextIdAnimalDialog);
        EditText editNombre = dialogView.findViewById(R.id.editTextNombreDialog);
        EditText editRaza = dialogView.findViewById(R.id.editTextRazaDialog);
        Spinner spinnerSexo = dialogView.findViewById(R.id.spinnerSexoDialog);
        EditText editFecha = dialogView.findViewById(R.id.editTextFechaNacimientoDialog);
        Spinner spinnerCategoria = dialogView.findViewById(R.id.spinnerCategoriaDialog);
        EditText editVacunas = dialogView.findViewById(R.id.editTextVacunasDialog);
        EditText editObservaciones = dialogView.findViewById(R.id.editTextObservacionesDialog);
        Button buttonCancelar = dialogView.findViewById(R.id.buttonCancelarDialog);
        Button buttonGuardar = dialogView.findViewById(R.id.buttonGuardarDialog);

        // Configurar spinner de sexo
        ArrayAdapter<String> sexoAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"Macho", "Hembra"}
        );
        sexoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSexo.setAdapter(sexoAdapter);

        // Configurar spinner de categorías
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listaCategorias.stream().map(Categoria::getNombre).toArray(String[]::new)
        );
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        // Rellenar campos con datos del animal
        editIdAnimal.setText(animal.getIdAnimal());
        editNombre.setText(animal.getNombre());
        editRaza.setText(animal.getRaza());
        spinnerSexo.setSelection(animal.getSexo().equals("Macho") ? 0 : 1);
        editVacunas.setText(animal.getVacunas());
        editObservaciones.setText(animal.getObservaciones());

        if (animal.getFechaNacimiento() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editFecha.setText(sdf.format(new Date(animal.getFechaNacimiento())));
        }

        // Seleccionar categoría actual
        for (int i = 0; i < listaCategorias.size(); i++) {
            if (listaCategorias.get(i).getId() == animal.getCategoriaId()) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }

        // DatePicker para fecha
        editFecha.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (animal.getFechaNacimiento() > 0) {
                calendar.setTimeInMillis(animal.getFechaNacimiento());
            }

            DatePickerDialog datePicker = new DatePickerDialog(
                    getContext(),
                    (view, year, month, day) -> {
                        String fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                                day, month + 1, year);
                        editFecha.setText(fecha);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Botón cancelar
        buttonCancelar.setOnClickListener(v -> dialog.dismiss());

        // Botón guardar
        buttonGuardar.setOnClickListener(v -> {
            String nombre = editNombre.getText().toString().trim();
            if (nombre.isEmpty()) {
                Toast.makeText(getContext(), "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
                return;
            }

            animal.setIdAnimal(editIdAnimal.getText().toString().trim());
            animal.setNombre(nombre);
            animal.setApodo(nombre);
            animal.setRaza(editRaza.getText().toString().trim());
            animal.setSexo(spinnerSexo.getSelectedItem().toString());
            animal.setVacunas(editVacunas.getText().toString().trim());
            animal.setObservaciones(editObservaciones.getText().toString().trim());

            String fechaStr = editFecha.getText().toString().trim();
            if (!fechaStr.isEmpty()) {
                try {
                    Date date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaStr);
                    animal.setFechaNacimiento(date != null ? date.getTime() : 0);
                } catch (ParseException e) {
                    Toast.makeText(getContext(), "Formato de fecha inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            animal.setCategoriaId(listaCategorias.get(spinnerCategoria.getSelectedItemPosition()).getId());

            viewModel.updateAnimal(animal);
            Toast.makeText(getContext(), "Animal actualizado correctamente", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void mostrarDialogEliminar(Animal animal) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Animal")
                .setMessage("¿Estás seguro de que deseas eliminar a " + animal.getNombre() + "?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    viewModel.deleteAnimal(animal);
                    Toast.makeText(getContext(), "Animal eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}