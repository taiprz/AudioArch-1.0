package com.audioarch.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Artistas")
public class Artista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // IMPORTANTE: mappedBy debe ser exactamente el nombre de la variable en la clase Album
    @ManyToMany(mappedBy = "artistas")
    private List<Album> albumesList = new ArrayList<>();

    // IMPORTANTE: mappedBy debe ser exactamente el nombre de la variable en la clase Cancion
    @ManyToMany(mappedBy = "artistas")
    private List<Cancion> cancionList = new ArrayList<>();

    @Column(name="nombre")
    private String nombre;

    @Column(name="foto", columnDefinition = "TEXT")
    private String foto;

    public Artista(){}

    public Artista( String nombre, String foto) {
        this.nombre = nombre;
        this.foto = foto;
    }

    public int getId() {
        return id;
    }


    public List<Album> getAlbumesList() {
        return albumesList;
    }

    public void setAlbumesList(List<Album> albumesList) {
        this.albumesList = albumesList;
    }

    public List<Cancion> getCancionList() {
        return cancionList;
    }

    public void setCancionList(List<Cancion> cancionList) {
        this.cancionList = cancionList;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    @Override
    public String toString() {
        // Mostrar cantidad de canciones y álbumes
        return String.format(
                "[%s] %s - Canciones: %d - Álbumes: %d",
                foto != null ? foto : "sin foto",
                nombre,
                cancionList != null ? cancionList.size() : 0,
                albumesList != null ? albumesList.size() : 0
        );
    }
}
