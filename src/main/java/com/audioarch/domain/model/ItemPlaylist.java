package com.audioarch.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name="ItemPlaylist")
public class ItemPlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String titulo;
    private String artista;
    @Column(columnDefinition = "TEXT")
    private String fotoUrl;
    
    @Column(name="previewUrl", columnDefinition = "TEXT")
    private String previewUrl;
    
    @Column(name="duracion")
    private int duracion;

    @ManyToOne
    @JoinColumn(name="playlist_id")
    private Playlist playlist;

    public ItemPlaylist() {}

    public ItemPlaylist(String titulo, String artista, String fotoUrl, String previewUrl, int duracion) {
        this.titulo = titulo;
        this.artista = artista;
        this.fotoUrl = fotoUrl;
        this.previewUrl = previewUrl;
        this.duracion = duracion;
    }

    public int getId() { return id; }
    
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getArtista() { return artista; }
    public void setArtista(String artista) { this.artista = artista; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }
}
