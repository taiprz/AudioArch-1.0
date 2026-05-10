package com.audioarch.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name="Calificacion")
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name="Usuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name ="Cancion")
    private Cancion cancion;

    @ManyToOne
    @JoinColumn(name ="Album")
    private Album album;

    @Column(name="Review")
    private String review;

    @Column(name = "Nota")
    private int nota;

    // Constructores
    // IMPORTANTE: Hibernate SIEMPRE necesita un constructor vacío
    public Calificacion() { }

    // Constructor para calificación de canción
    public Calificacion(Usuario usuario, Cancion cancion, int nota, String review) {
        this.usuario = usuario;
        this.cancion = cancion;
        this.album = null;
        this.nota = nota;
        this.review = review;
    }

    // Constructor para calificación de álbum
    public Calificacion(Usuario usuario, Album album, int nota, String review) {
        this.usuario = usuario;
        this.album = album;
        this.cancion = null;
        this.nota = nota;
        this.review = review;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Cancion getCancion() {
        return cancion;
    }

    public void setCancion(Cancion cancion) {
        this.cancion = cancion;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    @Override
    public String toString() {
        return "Calificacion{" +
                "id=" + id +
                ", nota=" + nota +
                ", review='" + review + '\'' +
                ", usuarioId=" + (usuario != null ? usuario.getId() : null) +
                ", albumId=" + (album != null ? album.getId() : null) +
                ", cancionId=" + (cancion != null ? cancion.getId() : null) +
                '}';
    }
}
