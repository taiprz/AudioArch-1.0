package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.Album;
import com.audioarch.core.database.JpaUtil;

import java.util.ArrayList;

// Metodos del albumDAO
public class AlbumDao {


    public static void insertarAlbum(Album a) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al insertar álbum.", ex);
        } finally {
            em.close();
        }
    }

    public static void actualizarAlbum(Album a) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(a);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar álbum.", ex);
        } finally {
            em.close();
        }
    }

    public static Album getAlbumPorId(int id) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            return em.find(Album.class, id);
        }
    }

    public static void eliminarAlbum(Album a) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            Album album = em.find(Album.class, a.getId());
            if (album != null) {
                em.remove(album);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar álbum.", e);
        } finally {
            em.close();
        }
    }

    // JPQL: Java Persistence Query Language

    // devuelve el primer album que coincida con el nombre
    public static Album obtenerAlbumPorNombre(String nombre) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Album> query = em.createQuery(
                    "SELECT a FROM Album a WHERE a.nombre LIKE :nombre", Album.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getResultList().stream().findFirst().orElse(null);
        }
    }

    // comprueba si existe algun album con el nombre dado
    public static boolean existeAlbum(String nombre) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(a) FROM Album a WHERE a.nombre LIKE :nombre", Long.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getSingleResult() > 0;
        }
    }

    // devuelve todos los albumes o filtrando por nombre si se proporciona
    public static ArrayList<Album> obtenerAlbums(String nombre) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Album> query;
            if (nombre == null || nombre.isBlank()) {
                query = em.createQuery("SELECT a FROM Album a", Album.class);
            } else {
                query = em.createQuery(
                        "SELECT a FROM Album a WHERE a.nombre LIKE :nombre", Album.class
                );
                query.setParameter("nombre", "%" + nombre + "%");
            }
            return new ArrayList<>(query.getResultList());
        }
    }
}
