package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.Calificacion;
import com.audioarch.core.database.JpaUtil;

import java.util.ArrayList;

public class CalificacionDao {
// METODOS DAO de las calificaciones

    public static void insertarCalificacion(Calificacion c) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al insertar la calificación.", ex);
        } finally {
            em.close();
        }
    }

    public static void actualizarCalificacion(Calificacion c) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar la calificación.", ex);
        } finally {
            em.close();
        }
    }

    public static Calificacion getCalificacionPorId(int id) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            return em.find(Calificacion.class, id);
        }
    }

    public static void eliminarCalificacion(int id) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            // Buscamos la entidad dentro del contexto actual antes de borrar
            Calificacion c = em.find(Calificacion.class, id);
            if (c != null) {
                em.remove(c);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar la calificación.", ex);
        } finally {
            em.close();
        }
    }

    // JPQL: Java Persistence Query Language

    // obtener calificaciones por canción
    public static ArrayList<Calificacion> obtenerCalificacionPorCancion(String titulo) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Calificacion> query = em.createQuery(
                    "SELECT c FROM Calificacion c JOIN c.cancion ca WHERE ca.titulo LIKE :titulo", Calificacion.class
            );
            query.setParameter("titulo", "%" + titulo + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // obtener calificaciones por álbum
    public static ArrayList<Calificacion> obtenerCalificacionPorAlbum(String titulo) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Calificacion> query = em.createQuery(
                    "SELECT c FROM Calificacion c JOIN c.album a WHERE a.nombre LIKE :titulo", Calificacion.class
            );
            query.setParameter("titulo", "%" + titulo + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // obtener calificaciones por artista
    public static ArrayList<Calificacion> obtenerCalificacionPorArtista(String nombreArtista) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Calificacion> query = em.createQuery(
                    "SELECT c FROM Calificacion c " +
                            "JOIN c.cancion ca " +
                            "JOIN ca.artistas a " +
                            "WHERE a.nombre LIKE :nombre", Calificacion.class
            );
            query.setParameter("nombre", "%" + nombreArtista + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // verificar si ya existe calificación de canción
    public static boolean existeCalificacionCancion(int idUser, int idCancion) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(c) FROM Calificacion c " +
                            "JOIN c.usuario u " +
                            "JOIN c.cancion ca " +
                            "WHERE u.id = :userId AND ca.id = :cancionId", Long.class
            );
            query.setParameter("userId", idUser);
            query.setParameter("cancionId", idCancion);
            return query.getSingleResult() > 0;
        }
    }

    // verificar si ya existe calificación de album
    public static boolean existeCalificacionAlbum(int idUser, int idAlbum) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(c) FROM Calificacion c " +
                            "JOIN c.usuario u " +
                            "JOIN c.album a " +
                            "WHERE u.id = :userId AND a.id = :albumId", Long.class
            );
            query.setParameter("userId", idUser);
            query.setParameter("albumId", idAlbum);
            return query.getSingleResult() > 0;
        }
    }

    // obtener todas las calificaciones de un usuario
    public static ArrayList<Calificacion> obtenerCalificaciones(String usuario) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Calificacion> query = em.createQuery(
                    "SELECT c FROM Calificacion c JOIN c.usuario u WHERE u.user = :usuario", Calificacion.class
            );
            query.setParameter("usuario", usuario);
            return new ArrayList<>(query.getResultList());
        }
    }

    // obtener calificaciones de los usuarios que el usuario actual sigue (Feed Principal)
    public static ArrayList<Calificacion> obtenerFeedDeSeguidos(int usuarioId) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Calificacion> query = em.createQuery(
                    "SELECT c FROM Calificacion c " +
                    "JOIN Seguimiento s ON c.usuario.id = s.seguido.id " +
                    "WHERE s.seguidor.id = :usuarioId " +
                    "ORDER BY c.nota DESC", Calificacion.class
            );
            query.setParameter("usuarioId", usuarioId);
            return new ArrayList<>(query.getResultList());
        }
    }
}
