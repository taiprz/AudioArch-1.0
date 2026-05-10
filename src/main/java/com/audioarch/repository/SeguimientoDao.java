package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.Seguimiento;
import com.audioarch.domain.model.Usuario;
import com.audioarch.core.database.JpaUtil;

public class SeguimientoDao {

    public static void seguir(Usuario seguidor, Usuario seguido) {
        if (estaSiguiendo(seguidor.getId(), seguido.getId())) return; // Ya lo sigue
        
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            // Necesitamos asegurarnos que seguidor y seguido están atachados
            Usuario s1 = em.find(Usuario.class, seguidor.getId());
            Usuario s2 = em.find(Usuario.class, seguido.getId());
            
            Seguimiento nuevoSeguimiento = new Seguimiento(s1, s2);
            em.persist(nuevoSeguimiento);
            
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al crear seguimiento.", ex);
        } finally {
            em.close();
        }
    }

    public static void dejarDeSeguir(Usuario seguidor, Usuario seguido) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            TypedQuery<Seguimiento> query = em.createQuery(
                "SELECT s FROM Seguimiento s WHERE s.seguidor.id = :seguidorId AND s.seguido.id = :seguidoId", 
                Seguimiento.class
            );
            query.setParameter("seguidorId", seguidor.getId());
            query.setParameter("seguidoId", seguido.getId());
            
            Seguimiento s = query.getResultList().stream().findFirst().orElse(null);
            if (s != null) {
                em.remove(s);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar seguimiento.", ex);
        } finally {
            em.close();
        }
    }

    public static boolean estaSiguiendo(int idSeguidor, int idSeguido) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                "SELECT count(s) FROM Seguimiento s WHERE s.seguidor.id = :seguidorId AND s.seguido.id = :seguidoId", 
                Long.class
            );
            query.setParameter("seguidorId", idSeguidor);
            query.setParameter("seguidoId", idSeguido);
            return query.getSingleResult() > 0;
        }
    }
}
