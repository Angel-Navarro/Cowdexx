package com.example.cowdex.ui.bdd.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cowdex.ui.bdd.entities.Animal;

import java.util.List;

@Dao
public interface AnimalDao {

    @Insert
    void insert(Animal animal);

    @Update
    void update(Animal animal);

    @Delete
    void delete(Animal animal);

    @Query("SELECT * FROM animales ORDER BY nombre ASC")
    LiveData<List<Animal>> getAllAnimales();

    @Query("SELECT * FROM animales WHERE categoriaId = :categoriaId ORDER BY nombre ASC")
    LiveData<List<Animal>> getAnimalesByCategoria(int categoriaId);

    @Query("SELECT * FROM animales WHERE idAnimal LIKE :searchQuery OR nombre LIKE :searchQuery ORDER BY nombre ASC")
    LiveData<List<Animal>> searchAnimales(String searchQuery);

    @Query("SELECT * FROM animales WHERE id = :animalId")
    LiveData<Animal> getAnimalById(int animalId);

    // AGREGAR ESTOS MÉTODOS SÍNCRONOS PARA RESPALDOS
    @Query("SELECT * FROM animales ORDER BY nombre ASC")
    List<Animal> getAllAnimalsSync();

    @Query("DELETE FROM animales")
    void deleteAll();
}