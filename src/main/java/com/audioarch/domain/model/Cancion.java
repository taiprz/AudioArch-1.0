package com.audioarch.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name="Canciones")
public class Cancion {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name="Titulo")
    private String titulo;

    // se añade CascadeType.MERGE para que al insertar/actualizar canción se vinculen los artistas correctamente
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "cancion_artista",
            joinColumns = @JoinColumn(name="cancion_id"),
            inverseJoinColumns = @JoinColumn(name = "artista_id")
    )
    private List<Artista> artistas = new ArrayList<>();


    @Column(name="FechaPublicacion")
    private LocalDate fechaPublicacion;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;


    @Column(name="duracion")
    private int duracion;

    @Column(name="Portada", columnDefinition = "TEXT")
    private String portada;

    @OneToMany(mappedBy = "cancion", cascade = CascadeType.ALL)
    private List<Calificacion> calificaciones = new ArrayList<>();

    // Constructores

    public Cancion() { }

    public Cancion(String titulo, LocalDate fechaPublicacion,int duracion, String portada) {
        this.titulo = titulo;
        this.fechaPublicacion = fechaPublicacion;
        this.duracion = duracion;
        this.portada = portada;
    }


    // setters y getters

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public List<Artista> getArtistas() {
        return artistas;
    }

    public void setArtistas(List<Artista> artistas) {
        this.artistas = artistas;
    }

    public LocalDate getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDate fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getPortada() {
        return portada;
    }

    public void setPortada(String portada) {
        this.portada = portada;
    }

    public List<Calificacion> getCalificaciones() {
        return calificaciones;
    }

    public void setCalificaciones(List<Calificacion> calificaciones) {
        this.calificaciones = calificaciones;
    }

    @Override
    public String toString() {
        int minutos = duracion / 60;
        int segundos = duracion % 60;

        String artistasStr = artistas.isEmpty() ? "Sin artistas" :
                artistas.stream().map(Artista::getNombre).reduce((a,b) -> a + ", " + b).orElse("Sin artistas");

        return String.format(
                "[%s] %s - Artistas: %s - Duración: %d:%02d - Fecha: %s",
                portada != null ? portada : "sin portada",
                titulo,
                artistasStr,
                minutos,
                segundos,
                fechaPublicacion != null ? fechaPublicacion : "sin fecha"
        );
    }

    // Métodos necesarios para que el Service pueda comparar e intersectar listas correctamente
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cancion cancion = (Cancion) o;
        return id == cancion.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}