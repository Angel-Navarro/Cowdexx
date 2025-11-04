package com.example.cowdex.ui.bdd.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.cowdex.ui.bdd.dao.CategoriaDao;
import com.example.cowdex.ui.bdd.database.AppDatabase;
import com.example.cowdex.ui.bdd.entities.Categoria;

import java.util.List;

public class CategoriaRepository {

    private CategoriaDao categoriaDao;
    private LiveData<List<Categoria>> allCategorias;

    public CategoriaRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        categoriaDao = database.categoriaDao();
        allCategorias = categoriaDao.getAllCategorias();
    }

    public LiveData<List<Categoria>> getAllCategorias() {
        return allCategorias;
    }

    public void insert(Categoria categoria) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoriaDao.insert(categoria);
        });
    }

    public void update(Categoria categoria) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoriaDao.update(categoria);
        });
    }

    public void delete(Categoria categoria) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            categoriaDao.delete(categoria);
        });
    }
}