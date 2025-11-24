package com.example.cowdex.ui.bdd.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cowdex.ui.bdd.entities.Categoria;

import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    void insert(Categoria categoria);

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);

    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    LiveData<List<Categoria>> getAllCategorias();

    @Query("SELECT * FROM categorias WHERE id = :categoriaId")
    LiveData<Categoria> getCategoriaById(int categoriaId);

    // AGREGAR ESTOS MÉTODOS SÍNCRONOS PARA RESPALDOS
    @Query("SELECT * FROM categorias ORDER BY nombre ASC")
    List<Categoria> getAllCategoriasSync();

    @Query("DELETE FROM categorias")
    void deleteAll();
}