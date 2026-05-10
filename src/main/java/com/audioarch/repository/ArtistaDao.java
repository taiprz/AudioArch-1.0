package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.Artista;
import com.audioarch.core.database.JpaUtil;

import java.util.ArrayList;

public class ArtistaDao {

    public static void insertarArtista(Artista a) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(a);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al insertar artista.", ex);
        } finally {
            em.close();
        }
    }

    public static void actualizarArtista(Artista a) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(a);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar artista.", ex);
        } finally {
            em.close();
        }
    }

    public static Artista getArtistaPorId(int id) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            return em.find(Artista.class, id);
        }
    }

    public static void eliminarArtista(Artista a) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            Artista artista = em.find(Artista.class, a.getId());
            if (artista != null) {
                em.remove(artista);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar artista.", e);
        } finally {
            em.close();
        }
    }


    // JPQL: Java Persistence Query Language

    // devuelve el primer artista que coincida con el nombre
    public static Artista obtenerArtistaPorNombre(String nombre) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Artista> query = em.createQuery(
                    "SELECT a FROM Artista a WHERE a.nombre LIKE :nombre", Artista.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getResultList().stream().findFirst().orElse(null);
        }
    }

    // comprueba si existe algun artista con ese nombre
    public static boolean existeArtista(String nombre) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(a) FROM Artista a WHERE a.nombre LIKE :nombre", Long.class
            );
            query.setParameter("nombre", "%" + nombre + "%");
            return query.getSingleResult() > 0;
        }
    }

    // Devuelve todos los artistas o filtrando por nombre
    public static ArrayList<Artista> obtenerArtistas(String nombre) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Artista> query;
            if (nombre == null || nombre.isBlank()) {
                query = em.createQuery("SELECT a FROM Artista a", Artista.class);
            } else {
                query = em.createQuery("SELECT a FROM Artista a WHERE a.nombre LIKE :nombre", Artista.class);
                query.setParameter("nombre", "%" + nombre + "%");
            }
            return new ArrayList<>(query.getResultList());
        }
    }
}
