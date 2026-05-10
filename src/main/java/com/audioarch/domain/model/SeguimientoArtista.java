package com.audioarch.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name="Seguimiento_Artista")
public class SeguimientoArtista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "artist_id", nullable = false)
    private long artistId;

    @Column(name = "artist_name")
    private String artistName;

    public SeguimientoArtista() {}

    public SeguimientoArtista(Usuario usuario, long artistId, String artistName) {
        this.usuario = usuario;
        this.artistId = artistId;
        this.artistName = artistName;
    }

    public int getId() { return id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public long getArtistId() { return artistId; }
    public void setArtistId(long artistId) { this.artistId = artistId; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }
}
