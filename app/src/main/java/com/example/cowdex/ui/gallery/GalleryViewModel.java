package com.example.cowdex.ui.gallery;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.cowdex.ui.bdd.entities.Animal;
import com.example.cowdex.ui.bdd.entities.Categoria;
import com.example.cowdex.ui.bdd.repository.AnimalRepository;
import com.example.cowdex.ui.bdd.repository.CategoriaRepository;
import java.util.List;

public class GalleryViewModel extends AndroidViewModel {

    private final AnimalRepository animalRepository;
    private final CategoriaRepository categoriaRepository;

    public GalleryViewModel(@NonNull Application application) {
        super(application);
        animalRepository = new AnimalRepository(application);
        categoriaRepository = new CategoriaRepository(application);
    }

    public LiveData<List<Categoria>> getAllCategorias() {
        return categoriaRepository.getAllCategorias();
    }

    public void insertAnimal(Animal animal) {
        animalRepository.insert(animal);
    }
}
