package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import com.audioarch.domain.model.Playlist;
import com.audioarch.core.database.JpaUtil;

import java.util.List;

public class PlaylistDao {

    public static void guardar(Playlist p) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al guardar la playlist.", e);
        } finally {
            em.close();
        }
    }

    public static void actualizar(Playlist p) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar la playlist.", e);
        } finally {
            em.close();
        }
    }

    public static void eliminar(int id) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            Playlist p = em.find(Playlist.class, id);
            if (p != null) {
                em.remove(p);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar la playlist.", e);
        } finally {
            em.close();
        }
    }

    public static List<Playlist> obtenerPorUsuario(int usuarioId) {
        try(EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            return em.createQuery("SELECT p FROM Playlist p WHERE p.usuario.id = :uid", Playlist.class)
                     .setParameter("uid", usuarioId)
                     .getResultList();
        }
    }
}
