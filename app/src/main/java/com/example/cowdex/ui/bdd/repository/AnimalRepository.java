package com.example.cowdex.ui.bdd.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.cowdex.ui.bdd.dao.AnimalDao;
import com.example.cowdex.ui.bdd.database.AppDatabase;
import com.example.cowdex.ui.bdd.entities.Animal;

import java.util.List;

public class AnimalRepository {

    private AnimalDao animalDao;
    private LiveData<List<Animal>> allAnimales;

    public AnimalRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        animalDao = database.animalDao();
        allAnimales = animalDao.getAllAnimales();
    }

    public LiveData<List<Animal>> getAllAnimales() {
        return allAnimales;
    }

    public LiveData<List<Animal>> getAnimalesByCategoria(int categoriaId) {
        return animalDao.getAnimalesByCategoria(categoriaId);
    }

    public LiveData<List<Animal>> searchAnimales(String query) {
        return animalDao.searchAnimales("%" + query + "%");
    }

    public void insert(Animal animal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            animalDao.insert(animal);
        });
    }

    public void update(Animal animal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            animalDao.update(animal);
        });
    }

    public void delete(Animal animal) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            animalDao.delete(animal);
        });
    }
}