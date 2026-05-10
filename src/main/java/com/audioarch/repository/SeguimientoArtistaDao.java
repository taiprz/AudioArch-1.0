package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.SeguimientoArtista;
import com.audioarch.domain.model.Usuario;
import com.audioarch.core.database.JpaUtil;

public class SeguimientoArtistaDao {

    public static boolean isSiguiendo(Usuario usuario, long artistId) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                "SELECT count(s) FROM SeguimientoArtista s WHERE s.usuario = :usuario AND s.artistId = :artistId", Long.class);
            query.setParameter("usuario", usuario);
            query.setParameter("artistId", artistId);
            return query.getSingleResult() > 0;
        }
    }

    public static int contarSeguidores(long artistId) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                "SELECT count(s) FROM SeguimientoArtista s WHERE s.artistId = :artistId", Long.class);
            query.setParameter("artistId", artistId);
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        }
    }

    public static void seguirArtista(Usuario usuario, long artistId, String artistName) {
        if (isSiguiendo(usuario, artistId)) return;

        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            SeguimientoArtista sa = new SeguimientoArtista(usuario, artistId, artistName);
            em.persist(sa);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al seguir al artista.", e);
        } finally {
            em.close();
        }
    }

    public static void dejarDeSeguir(Usuario usuario, long artistId) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<SeguimientoArtista> query = em.createQuery(
                "SELECT s FROM SeguimientoArtista s WHERE s.usuario = :usuario AND s.artistId = :artistId", SeguimientoArtista.class);
            query.setParameter("usuario", usuario);
            query.setParameter("artistId", artistId);
            
            SeguimientoArtista sa = query.getResultList().stream().findFirst().orElse(null);
            if (sa != null) {
                em.remove(sa);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al dejar de seguir al artista.", e);
        } finally {
            em.close();
        }
    }

    public static java.util.List<SeguimientoArtista> obtenerArtistasSeguidos(Usuario usuario) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<SeguimientoArtista> query = em.createQuery(
                "SELECT s FROM SeguimientoArtista s WHERE s.usuario = :usuario", SeguimientoArtista.class);
            query.setParameter("usuario", usuario);
            return query.getResultList();
        }
    }
}
