package com.audioarch.core.database;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * Utilidad JPA.
 * Gestiona el EntityManagerFactory para la conexión con la base de datos PostgreSQL (Neon.tech).
 */
public final class JpaUtil {

    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("Persistencia");

    public static EntityManagerFactory getEmf() { return emf; }

    /**
     * Cierra el EntityManagerFactory.
     * Llamar al cerrar la aplicación.
     */
    public static void emfClose() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("✓ Conexión JPA cerrada correctamente.");
        }
    }
}
