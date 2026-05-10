package com.audioarch.domain.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="Usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "clave")
    private String clave;

    @Column(name = "username")
    private String user;

    // nota: el orphanremoval se pone aqui para que al quitarla de la lista se borre
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Calificacion> calificaciones = new ArrayList<>();

    // --- CAMPOS SOCIALES ---
    @Column(name = "biografia", columnDefinition = "TEXT")
    private String biografia;

    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil;

    @Column(name = "banner", columnDefinition = "TEXT")
    private String banner;

    // Lista de usuarios a los que yo sigo
    @OneToMany(mappedBy = "seguidor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seguimiento> siguiendo = new ArrayList<>();

    // Lista de usuarios que me siguen
    @OneToMany(mappedBy = "seguido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seguimiento> seguidores = new ArrayList<>();


    // Constructores
    // IMPORTANTE: Hibernate SIEMPRE necesita un constructor vacio
    public Usuario() {}

    public Usuario(String email, String clave, String user) {
        this.email = email;
        this.clave = clave;
        this.user = user;
    }

    // getters y setters
    public int getId() { return id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public List<Calificacion> getCalificaciones() { return calificaciones; }
    public void setCalificaciones(List<Calificacion> calificaciones) { this.calificaciones = calificaciones; }

    public String getBiografia() { return biografia; }
    public void setBiografia(String biografia) { this.biografia = biografia; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getBanner() { return banner; }
    public void setBanner(String banner) { this.banner = banner; }

    public List<Seguimiento> getSiguiendo() { return siguiendo; }
    public void setSiguiendo(List<Seguimiento> siguiendo) { this.siguiendo = siguiendo; }

    public List<Seguimiento> getSeguidores() { return seguidores; }
    public void setSeguidores(List<Seguimiento> seguidores) { this.seguidores = seguidores; }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
