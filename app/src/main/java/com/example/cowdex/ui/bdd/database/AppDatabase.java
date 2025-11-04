package com.example.cowdex.ui.bdd.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.cowdex.ui.bdd.dao.AnimalDao;
import com.example.cowdex.ui.bdd.dao.CategoriaDao;
import com.example.cowdex.ui.bdd.entities.Animal;
import com.example.cowdex.ui.bdd.entities.Categoria;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Categoria.class, Animal.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CategoriaDao categoriaDao();
    public abstract AnimalDao animalDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "cowdex_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}