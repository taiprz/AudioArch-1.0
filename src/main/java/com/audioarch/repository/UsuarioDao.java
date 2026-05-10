package com.audioarch.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import com.audioarch.domain.model.Usuario;
import com.audioarch.core.database.JpaUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;

public class UsuarioDao {

    public static void InsertarUsuario(Usuario u) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();

        try {
            // Hash password before saving
            if (u.getClave() != null) {
                u.setClave(BCrypt.hashpw(u.getClave(), BCrypt.gensalt()));
            }

            em.getTransaction().begin();
            em.persist(u);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al registrar en base de datos: " + ex.getMessage(), ex);
        } finally {
            em.close();
        }
    }

    public static void actualizarUsuario(Usuario u) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(u);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al actualizar el usuario.", ex);
        } finally {
            em.close();
        }
    }

    public static Usuario getUsuarioPorId(int id) {
        try(EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            return em.find(Usuario.class, id);
        }
    }

    public static Usuario getUsuarioConRelaciones(int id) {
        try(EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            Usuario u = em.find(Usuario.class, id);
            if (u != null) {
                u.getSeguidores().size(); // Fuerza la inicialización
                u.getSiguiendo().size();  // Fuerza la inicialización
            }
            return u;
        }
    }

    public static void eliminarUsuario(Usuario u) {
        EntityManager em = JpaUtil.getEmf().createEntityManager();

        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, u.getId());
            if (usuario != null) {
                em.remove(usuario);
                em.getTransaction().commit();
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar usuario.", e);
        } finally {
            em.close();
        }
    }

    // JPQL: Java Persistence Query Language

    public static Usuario obtenerUsuarioPorUser(String user) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE LOWER(u.user) = LOWER(:user)", Usuario.class
            );
            query.setParameter("user", user.trim());
            return query.getResultList().stream().findFirst().orElse(null);
        }
    }

    public static boolean existeUsuario(String user) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(u) FROM Usuario u WHERE LOWER(u.user) = LOWER(:user)", Long.class
            );
            query.setParameter("user", user.trim());
            return query.getSingleResult() > 0;
        }
    }

    public static boolean existeEmail(String email) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT count(u) FROM Usuario u WHERE LOWER(u.email) = LOWER(:email)", Long.class
            );
            query.setParameter("email", email.trim());
            return query.getSingleResult() > 0;
        }
    }

    // Devuelve todos los usuarios ordenados por su última reseña (el más activo primero)
    public static ArrayList<Usuario> obtenerUsuarios() {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u LEFT JOIN Calificacion c ON c.usuario = u " +
                "GROUP BY u ORDER BY COALESCE(MAX(c.id), 0) DESC",
                Usuario.class
            );
            return new ArrayList<>(query.getResultList());
        }
    }

    // metodo para buscar varios usuarios por su user (para el buscador global)
    public static ArrayList<Usuario> buscarUsuarios(String filtro) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u LEFT JOIN Calificacion c ON c.usuario = u WHERE u.user LIKE :filtro GROUP BY u ORDER BY COALESCE(MAX(c.id), 0) DESC",
                    Usuario.class
            );
            query.setParameter("filtro", "%" + filtro + "%");
            return new ArrayList<>(query.getResultList());
        }
    }

    // metodo para iniciar sesion
    public static Usuario login(String email, String clave) {
        try (EntityManager em = JpaUtil.getEmf().createEntityManager()) {

            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email",
                    Usuario.class
            );

            query.setParameter("email", email);

            Usuario u = query.getResultList().stream().findFirst().orElse(null);

            if (u != null) {
                try {
                    // Verificar password con BCrypt
                    if (BCrypt.checkpw(clave, u.getClave())) {
                        return u;
                    }
                } catch (IllegalArgumentException e) {
                    // Fallback para contraseñas antiguas
                    if (clave.equals(u.getClave())) {
                        return u;
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            throw new RuntimeException("Error al conectar con la base de datos durante el login: " + ex.getMessage(), ex);
        }
    }

}
