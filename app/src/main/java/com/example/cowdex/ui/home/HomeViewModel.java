package com.example.cowdex.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.cowdex.ui.bdd.entities.Categoria;
import com.example.cowdex.ui.bdd.repository.CategoriaRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {

    private final CategoriaRepository repository;
    private final LiveData<List<Categoria>> allCategorias;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        repository = new CategoriaRepository(application);
        allCategorias = repository.getAllCategorias();
    }

    // 👇 ESTE MÉTODO es el que falta y soluciona tu error
    public LiveData<List<Categoria>> getAllCategorias() {
        return allCategorias;
    }

    public void insert(Categoria categoria) {
        repository.insert(categoria);
    }

    public void update(Categoria categoria) {
        repository.update(categoria);
    }

    public void delete(Categoria categoria) {
        repository.delete(categoria);
    }
}
