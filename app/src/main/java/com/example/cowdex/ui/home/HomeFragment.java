package com.example.cowdex.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.cowdex.databinding.FragmentHomeBinding;
import com.example.cowdex.ui.bdd.entities.Categoria;

//Analytics
import android.content.SharedPreferences;
import android.content.Context;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CategoriaAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        //Analytics
        SharedPreferences prefs = requireContext().getSharedPreferences("AppAnalytics", Context.MODE_PRIVATE);
        prefs.edit().putInt("home", prefs.getInt("home", 0) + 1).apply();

        View root = binding.getRoot();

        // Configurar RecyclerView
        binding.recyclerViewCategorias.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCategorias.setHasFixedSize(true);

        adapter = new CategoriaAdapter();
        binding.recyclerViewCategorias.setAdapter(adapter);

        // Observar cambios en las categorías
        homeViewModel.getAllCategorias().observe(getViewLifecycleOwner(), categorias -> {
            adapter.setCategorias(categorias);
        });

        // Botón para agregar categoría
        binding.buttonAddCategoria.setOnClickListener(v -> {
            String nombreCategoria = binding.editTextCategoria.getText().toString().trim();

            if (nombreCategoria.isEmpty()) {
                Toast.makeText(getContext(), "Ingrese un nombre de categoría", Toast.LENGTH_SHORT).show();
                return;
            }

            Categoria categoria = new Categoria(nombreCategoria);
            homeViewModel.insert(categoria);
            binding.editTextCategoria.setText("");
            Toast.makeText(getContext(), "Categoría agregada", Toast.LENGTH_SHORT).show();
        });

        // Listeners del adapter
        adapter.setOnCategoriaClickListener(new CategoriaAdapter.OnCategoriaClickListener() {
            @Override
            public void onEditClick(Categoria categoria) {
                showEditDialog(categoria);
            }

            @Override
            public void onDeleteClick(Categoria categoria) {
                showDeleteDialog(categoria);
            }
        });

        return root;
    }

    private void showEditDialog(Categoria categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Editar Categoría");

        final EditText input = new EditText(getContext());
        input.setText(categoria.getNombre());
        builder.setView(input);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = input.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                categoria.setNombre(nuevoNombre);
                homeViewModel.update(categoria);
                Toast.makeText(getContext(), "Categoría actualizada", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteDialog(Categoria categoria) {
        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Categoría")
                .setMessage("¿Está seguro de eliminar esta categoría? Se eliminarán también todos los animales asociados.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    homeViewModel.delete(categoria);
                    Toast.makeText(getContext(), "Categoría eliminada", Toast.LENGTH_SHORT).show();
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