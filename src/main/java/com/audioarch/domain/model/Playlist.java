package com.audioarch.domain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Playlists")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable=false)
    private String nombre;

    private String descripcion;

    @Column(name="portada_url", columnDefinition = "TEXT")
    private String portadaUrl;

    @Column(name="fecha_creacion")
    private LocalDate fechaCreacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="usuario_id")
    private Usuario usuario;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemPlaylist> items = new ArrayList<>();

    public Playlist() {}

    public Playlist(String nombre, String descripcion, Usuario usuario) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.fechaCreacion = LocalDate.now();
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getPortadaUrl() { return portadaUrl; }
    public void setPortadaUrl(String portadaUrl) { this.portadaUrl = portadaUrl; }

    public int getDuracionTotal() {
        return items == null ? 0 : items.stream().mapToInt(ItemPlaylist::getDuracion).sum();
    }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<ItemPlaylist> getItems() { return items; }
    public void setItems(List<ItemPlaylist> items) { this.items = items; }

    public void addItem(ItemPlaylist item) {
        this.items.add(item);
        item.setPlaylist(this);
    }

    public void removeItem(ItemPlaylist item) {
        this.items.remove(item);
        item.setPlaylist(null);
    }
}
