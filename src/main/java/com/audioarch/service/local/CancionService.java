package com.audioarch.service.local;

import com.audioarch.repository.AlbumDao;
import com.audioarch.repository.ArtistaDao;
import com.audioarch.repository.CancionDao;
import com.audioarch.domain.model.Album;
import com.audioarch.domain.model.Artista;
import com.audioarch.domain.model.Cancion;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CancionService {

    public static boolean crearCancion(List<String> nombresArtistas, String titulo, int duracion, LocalDate fechaPublicacion, String portada) {
        if (nombresArtistas == null || nombresArtistas.isEmpty()) return false;

        ArrayList<Artista> artistas = new ArrayList<>();
        for (String nombre : nombresArtistas) {
            Artista a = ArtistaDao.obtenerArtistaPorNombre(nombre);
            if (a == null) return false;
            artistas.add(a);
        }

        if (titulo == null || duracion <= 0 || fechaPublicacion == null || portada == null) return false;

        Cancion c = new Cancion();
        c.setArtistas(artistas);
        c.setTitulo(titulo);
        c.setDuracion(duracion);
        c.setFechaPublicacion(fechaPublicacion);
        c.setPortada(portada);

        CancionDao.insertarCancion(c);
        return true;
    }

    /**
     * 1. Si los campos coinciden (Searchbar único), usa buscarGeneral.
     * 2. Si son específicos, usa la intersección.
     */

    public static List<Cancion> buscarCanciones(String titulo, String nombreArtista, String nombreAlbum) {
        // CASO ESPECIAL: si los tres filtros son el mismo texto, es una búsqueda global desde el SearchBar
        if (titulo != null && titulo.equals(nombreArtista) && titulo.equals(nombreAlbum)) {
            return CancionDao.buscarGeneral(titulo);
        }

        // búsquedas individuales simples
        if (titulo != null && !titulo.isBlank() && nombreArtista == null && nombreAlbum == null) {
            return CancionDao.obtenerCancionPorTitulo(titulo);
        }

        if (nombreArtista != null && !nombreArtista.isBlank() && titulo == null && nombreAlbum == null) {
            return CancionDao.obtenerCancionPorArtista(nombreArtista);
        }

        if (nombreAlbum != null && !nombreAlbum.isBlank() && titulo == null && nombreArtista == null) {
            return CancionDao.obtenerCancionPorAlbum(nombreAlbum);
        }

        // caso complejo: Intersección (Filtros combinados)
        List<Cancion> porTitulo = (titulo != null && !titulo.isBlank()) ? CancionDao.obtenerCancionPorTitulo(titulo) : null;
        List<Cancion> porArtista = (nombreArtista != null && !nombreArtista.isBlank()) ? CancionDao.obtenerCancionPorArtista(nombreArtista) : null;
        List<Cancion> porAlbum = (nombreAlbum != null && !nombreAlbum.isBlank()) ? CancionDao.obtenerCancionPorAlbum(nombreAlbum) : null;

        return intersectarListas(porTitulo, porArtista, porAlbum);
    }

    public static List<Cancion> intersectarListas(List<Cancion>... listas) {
        List<Cancion> interseccion = new ArrayList<>();
        boolean primera = true;

        for (List<Cancion> lista : listas) {
            if (lista == null) continue;

            if (primera) {
                interseccion.addAll(lista);
                primera = false;
            } else {
                // Para que retainAll funcione bien, Cancion debe tener implementado equals/hashCode por ID
                interseccion.retainAll(lista);
            }
        }
        return interseccion;
    }

    public static Cancion obtenerCancionPorTituloYArtista(String titulo, String nombreArtista) {
        List<Cancion> porTitulo = CancionDao.obtenerCancionPorTitulo(titulo);
        List<Cancion> porArtista = CancionDao.obtenerCancionPorArtista(nombreArtista);

        for (Cancion c : porTitulo) {
            // contain utiliza el equals() de la clase Cancion
            if (porArtista.contains(c)) {
                return c;
            }
        }
        return null;
    }

    public static boolean eliminarCancion(int id) {
        Cancion c = CancionDao.getCancionPorId(id);
        if (c == null) return false;
        CancionDao.eliminarCancion(c);
        return true;
    }

    public static boolean actualizarCancion(int id, List<String> nuevoNombresArtistas, String nuevoTitulo, int nuevaDuracion, LocalDate nuevaFechapublicacion, String nuevaPortada, String nuevoAlbum) {
        Cancion c = CancionDao.getCancionPorId(id);
        if (c == null) return false;

        if (nuevoTitulo != null && !nuevoTitulo.isBlank()) c.setTitulo(nuevoTitulo);
        if (nuevaDuracion > 0) c.setDuracion(nuevaDuracion);
        if (nuevaFechapublicacion != null) c.setFechaPublicacion(nuevaFechapublicacion);
        if (nuevaPortada != null && !nuevaPortada.isBlank()) c.setPortada(nuevaPortada);

        // actualizar Álbum
        if (nuevoAlbum != null && !nuevoAlbum.isBlank()) {
            Album a = AlbumDao.obtenerAlbumPorNombre(nuevoAlbum);
            if (a != null) {
                if (c.getAlbum() != null) {
                    c.getAlbum().getCancionList().remove(c);
                }
                c.setAlbum(a);
                a.getCancionList().add(c);
            }
        }

        // actualizar Artistas
        if (nuevoNombresArtistas != null && !nuevoNombresArtistas.isEmpty()) {
            // limpiar referencias antiguas
            if (c.getArtistas() != null) {
                c.getArtistas().forEach(art -> art.getCancionList().remove(c));
            }

            List<Artista> listaArtistas = new ArrayList<>();
            for (String nombre : nuevoNombresArtistas) {
                Artista art = ArtistaDao.obtenerArtistaPorNombre(nombre);
                if (art != null) {
                    listaArtistas.add(art);
                    art.getCancionList().add(c);
                }
            }
            // IMPORTANTE: asignar nueva instancia de lista para Hibernate
            c.setArtistas(new ArrayList<>(listaArtistas));
        }

        CancionDao.actualizarCancion(c);
        return true;
    }
}