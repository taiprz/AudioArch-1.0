package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.Cancion;
import com.audioarch.core.database.JpaUtil;

import java.util.ArrayList;

public class CancionDao {


    public static void insertarCancion(Cancion c) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c); // Cambiado a merge para que funcione con el Service y la Carga Inicial
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al insertar canción.", ex);
        } finally {
            em.close();
        }
    }

    public static void actualizarCancion(Cancion c) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar canción.", ex);
        } finally {
            em.close();
        }
    }

    public static Cancion getCancionPorId(int id) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            return em.find(Cancion.class, id);
        }
    }

    public static void eliminarCancion(Cancion c) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            Cancion cancion = em.find(Cancion.class, c.getId());
            if (cancion != null) {
                em.remove(cancion);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar canción.", e);
        } finally {
            em.close();
        }
    }

    // JPQL: Java Persistence Query Language

    // obtener canciones por titulo
    public static ArrayList<Cancion> obtenerCancionPorTitulo(String titulo) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            // añadido DISTINCT y FETCH para que el Service pueda intersectar sin errores
            TypedQuery<Cancion> query = em.createQuery(
                    "SELECT DISTINCT c FROM Cancion c LEFT JOIN FETCH c.artistas WHERE c.titulo LIKE :titulo", Cancion.class
            );
            query.setParameter("titulo", "%" + titulo + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // obtener canciones por artista
    public static ArrayList<Cancion> obtenerCancionPorArtista(String nombreArtista) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            // añadido JOIN FETCH para que la lista de artistas no venga vacía
            TypedQuery<Cancion> query = em.createQuery(
                    "SELECT DISTINCT c FROM Cancion c " +
                            "JOIN FETCH c.artistas a " +
                            "WHERE a.nombre LIKE :nombre", Cancion.class
            );
            query.setParameter("nombre", "%" + nombreArtista + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // obtener cancion por album
    public static ArrayList<Cancion> obtenerCancionPorAlbum(String tituloAlbum) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            // añadido FETCH para que el buscador muestre los artistas correctamente
            TypedQuery<Cancion> query = em.createQuery(
                    "SELECT DISTINCT c FROM Cancion c " +
                            "LEFT JOIN FETCH c.artistas " +
                            "JOIN c.album a " +
                            "WHERE a.nombre LIKE :titulo", Cancion.class
            );
            query.setParameter("titulo", "%" + tituloAlbum + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // ver si existe la cancion
    public static boolean existeCancion(String titulo) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(c) FROM Cancion c WHERE c.titulo LIKE :titulo", Long.class
            );
            query.setParameter("titulo", "%" + titulo + "%");
            return query.getSingleResult() > 0;
        }
    }

    // metodo para que las canciones se muestren aleatoriamente (ajuste visual)
    public static ArrayList<Cancion> obtenerTodasLasCancionesCompletas() {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Cancion> query = em.createQuery(
                    "SELECT DISTINCT c FROM Cancion c LEFT JOIN FETCH c.artistas ORDER BY FUNCTION('RAND')",
                    Cancion.class
            );
            return new ArrayList<>(query.getResultList());
        }
    }

    // metodo extra para el buscador "híbrido" (albumes y canciones)
    public static ArrayList<Cancion> buscarGeneral(String filtro) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Cancion> query = em.createQuery(
                    "SELECT DISTINCT c FROM Cancion c LEFT JOIN FETCH c.artistas a LEFT JOIN FETCH c.album alb " +
                            "WHERE c.titulo LIKE :p OR a.nombre LIKE :p OR alb.nombre LIKE :p", Cancion.class
            );
            query.setParameter("p", "%" + filtro + "%");
            return new ArrayList<>(query.getResultList());
        }
    }
}