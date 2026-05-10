package com.audioarch.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Album")
public class Album {
    @Id
    @Column(name ="id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "album")
    private List<Cancion> cancionList = new ArrayList<>();

    // updatable = false ya que impide que cualquier UPDATE posterior borre el valor del constructor
    @Column(name = "duracion", updatable = false)
    private int duracion = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "album_artista",
            joinColumns = @JoinColumn(name = "album_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    private List<Artista> artistas = new ArrayList<>();

    @Column(name = "FechaPublicacion")
    private LocalDate fechaPublicacion;

    @Column(name="foto", columnDefinition = "TEXT")
    private String foto;

    @Column(name="nombre")
    private String nombre;

    public Album(){}

    public Album(String nombre, String foto, LocalDate fechaPublicacion, int duracion) {
        this.nombre = nombre;
        this.foto = foto;
        this.fechaPublicacion = fechaPublicacion;
        this.duracion = duracion;
    }

    // --- MÉTODOS DE CONVENIENCIA ---

    public void addCancion(Cancion cancion) {
        this.cancionList.add(cancion);
        cancion.setAlbum(this);
    }

    public void addArtista(Artista a) {
        this.artistas.add(a);
    }

    // --- GETTERS Y SETTERS ---

    public int getId() { return id; }

    public int getDuracion() { return duracion; }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public List<Cancion> getCancionList() { return cancionList; }

    public void setCancionList(List<Cancion> cancionList) {
        this.cancionList = cancionList;
    }

    public List<Artista> getArtistas() { return artistas; }
    public void setArtistas(List<Artista> artistas) { this.artistas = artistas; }
    public LocalDate getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(LocalDate fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Album{" + "id=" + id + ", nombre='" + nombre + '\'' +
                ", duracion=" + duracion + ", fechaPublicacion=" + fechaPublicacion + '}';
    }
}