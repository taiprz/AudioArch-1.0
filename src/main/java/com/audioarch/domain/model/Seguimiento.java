package com.audioarch.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="Seguimientos")
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // El usuario que sigue a alguien
    @ManyToOne
    @JoinColumn(name = "seguidor_id", nullable = false)
    private Usuario seguidor;

    // El usuario que es seguido
    @ManyToOne
    @JoinColumn(name = "seguido_id", nullable = false)
    private Usuario seguido;

    @Column(name = "fecha_seguimiento")
    private LocalDateTime fechaSeguimiento;

    public Seguimiento() {
        this.fechaSeguimiento = LocalDateTime.now();
    }

    public Seguimiento(Usuario seguidor, Usuario seguido) {
        this.seguidor = seguidor;
        this.seguido = seguido;
        this.fechaSeguimiento = LocalDateTime.now();
    }

    public int getId() { return id; }

    public Usuario getSeguidor() { return seguidor; }
    public void setSeguidor(Usuario seguidor) { this.seguidor = seguidor; }

    public Usuario getSeguido() { return seguido; }
    public void setSeguido(Usuario seguido) { this.seguido = seguido; }

    public LocalDateTime getFechaSeguimiento() { return fechaSeguimiento; }
    public void setFechaSeguimiento(LocalDateTime fechaSeguimiento) { this.fechaSeguimiento = fechaSeguimiento; }
}
