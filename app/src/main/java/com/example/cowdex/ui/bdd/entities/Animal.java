package com.example.cowdex.ui.bdd.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "animales",
        foreignKeys = @ForeignKey(
                entity = Categoria.class,
                parentColumns = "id",
                childColumns = "categoriaId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("categoriaId")})
public class Animal {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String idAnimal; // ID personalizado del animal
    private String nombre;
    private String apodo;
    private String raza;
    private String sexo; // Macho, Hembra
    private String imagenUri; // Ruta de la imagen
    private String vacunas; // Registro de vacunas
    private String observaciones;
    private long fechaNacimiento; // timestamp
    private int categoriaId; // FK a categorias

    public Animal(String idAnimal, String nombre, String apodo, String raza,
                  String sexo, String imagenUri, String vacunas, String observaciones,
                  long fechaNacimiento, int categoriaId) {
        this.idAnimal = idAnimal;
        this.nombre = nombre;
        this.apodo = apodo;
        this.raza = raza;
        this.sexo = sexo;
        this.imagenUri = imagenUri;
        this.vacunas = vacunas;
        this.observaciones = observaciones;
        this.fechaNacimiento = fechaNacimiento;
        this.categoriaId = categoriaId;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(String idAnimal) {
        this.idAnimal = idAnimal;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApodo() {
        return apodo;
    }

    public void setApodo(String apodo) {
        this.apodo = apodo;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getImagenUri() {
        return imagenUri;
    }

    public void setImagenUri(String imagenUri) {
        this.imagenUri = imagenUri;
    }

    public String getVacunas() {
        return vacunas;
    }

    public void setVacunas(String vacunas) {
        this.vacunas = vacunas;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public long getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(long fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(int categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Animal() {
    }

}