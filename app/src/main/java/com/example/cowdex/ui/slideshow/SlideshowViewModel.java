package com.example.cowdex.ui.slideshow;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.cowdex.ui.bdd.entities.Animal;
import com.example.cowdex.ui.bdd.entities.Categoria;
import com.example.cowdex.ui.bdd.repository.AnimalRepository;
import com.example.cowdex.ui.bdd.repository.CategoriaRepository;

import java.util.List;

public class SlideshowViewModel extends AndroidViewModel {

    private AnimalRepository animalRepository;
    private CategoriaRepository categoriaRepository;
    private LiveData<List<Animal>> allAnimales;
    private LiveData<List<Categoria>> allCategorias;

    // Para filtros
    private MutableLiveData<Integer> categoriaSeleccionada = new MutableLiveData<>(-1);
    private MutableLiveData<String> busquedaQuery = new MutableLiveData<>("");

    private LiveData<List<Animal>> animalesFiltrados;

    public SlideshowViewModel(@NonNull Application application) {
        super(application);
        animalRepository = new AnimalRepository(application);
        categoriaRepository = new CategoriaRepository(application);
        allAnimales = animalRepository.getAllAnimales();
        allCategorias = categoriaRepository.getAllCategorias();

        // Configurar filtros combinados
        animalesFiltrados = Transformations.switchMap(categoriaSeleccionada, categoria -> {
            if (categoria != null && categoria > 0) {
                return animalRepository.getAnimalesByCategoria(categoria);
            } else {
                return allAnimales;
            }
        });

        // Aplicar búsqueda sobre animales filtrados
        animalesFiltrados = Transformations.switchMap(busquedaQuery, query -> {
            if (query != null && !query.trim().isEmpty()) {
                return animalRepository.searchAnimales(query);
            } else {
                int cat = categoriaSeleccionada.getValue() != null ? categoriaSeleccionada.getValue() : -1;
                if (cat > 0) {
                    return animalRepository.getAnimalesByCategoria(cat);
                } else {
                    return allAnimales;
                }
            }
        });
    }

    public LiveData<List<Animal>> getAnimalesFiltrados() {
        return animalesFiltrados;
    }

    public LiveData<List<Categoria>> getAllCategorias() {
        return allCategorias;
    }

    public void filtrarPorCategoria(int categoriaId) {
        categoriaSeleccionada.setValue(categoriaId);
    }

    public void buscarAnimales(String query) {
        busquedaQuery.setValue(query);
    }

    public void updateAnimal(Animal animal) {
        animalRepository.update(animal);
    }

    public void deleteAnimal(Animal animal) {
        animalRepository.delete(animal);
    }
}