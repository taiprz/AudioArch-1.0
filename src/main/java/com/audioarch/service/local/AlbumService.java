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

public class AlbumService {

    public static boolean crearAlbum(List<String> titulosCanciones, List<String> artistasCanciones, int duracion, String portada, String titulo, List<String> artistasColaboradores, LocalDate fechaPublicacion) {

        ArrayList<Artista> artistas = new ArrayList<>();

        if (titulosCanciones == null || titulosCanciones.isEmpty()) return false;
        // crregido: la validación debe ser sobre la lista que recibes por parámetro
        if (artistasColaboradores == null || artistasColaboradores.isEmpty()) return false;

        ArrayList<Cancion> canciones = new ArrayList<>();
        for (int i = 0; i < titulosCanciones.size(); i++) {
            String titu = titulosCanciones.get(i);
            String artistaCancion = (i < artistasCanciones.size()) ? artistasCanciones.get(i) : null;

            Cancion c = CancionService.obtenerCancionPorTituloYArtista(titu, artistaCancion);
            if (c == null) {
                return false;
            }
            canciones.add(c);
        }
        for (String nombre : artistasColaboradores) {
            Artista a = ArtistaDao.obtenerArtistaPorNombre(nombre);
            if (a == null) {
                return false;
            }
            artistas.add(a);
        }

        if (titulo == null) return false;
        if (duracion == 0) return false;
        if (fechaPublicacion == null) return false;
        if (portada == null) return false;

        Album a = new Album();
        a.setArtistas(artistas);
        a.setDuracion(duracion);
        a.setFoto(portada);
        a.setNombre(titulo);
        a.setCancionList(canciones);

        // se añade la llamada al DAO para que el álbum se guarde realmente
        AlbumDao.insertarAlbum(a);
        return true;
    }

    public static boolean eliminarAlbum(int idAlbum) {

        Album album = AlbumDao.getAlbumPorId(idAlbum);
        if (album == null) return false;

        // quitar relación con canciones
        for (Cancion c : album.getCancionList()) {
            c.setAlbum(null);
            CancionDao.actualizarCancion(c);
        }

        // quitar relación con artistas
        for (Artista art : album.getArtistas()) {
            art.getAlbumesList().remove(album);
            ArtistaDao.actualizarArtista(art);
        }

        AlbumDao.eliminarAlbum(album);
        return true;
    }

    public static boolean actualizarAlbum(
            int idAlbum,
            String nuevoTitulo,
            Integer nuevaDuracion,
            String nuevaPortada,
            LocalDate nuevaFechaPublicacion,
            List<String> nuevosTitulosCanciones,
            List<String> nuevosArtistasCanciones,
            List<String> nuevosArtistasColaboradores
    ) {

        Album album = AlbumDao.getAlbumPorId(idAlbum);
        if (album == null) return false;

        // campos simples
        if (nuevoTitulo != null && !nuevoTitulo.isBlank()) {
            album.setNombre(nuevoTitulo);
        }

        if (nuevaDuracion != null && nuevaDuracion > 0) {
            album.setDuracion(nuevaDuracion);
        }

        if (nuevaPortada != null && !nuevaPortada.isBlank()) {
            album.setFoto(nuevaPortada);
        }

        if (nuevaFechaPublicacion != null) {
            album.setFechaPublicacion(nuevaFechaPublicacion);
        }

        // actualizar canciones
        if (nuevosTitulosCanciones != null && !nuevosTitulosCanciones.isEmpty()) {

            // quitar canciones antiguas
            for (Cancion c : album.getCancionList()) {
                c.setAlbum(null);
                CancionDao.actualizarCancion(c);
            }

            ArrayList<Cancion> nuevasCanciones = new ArrayList<>();
            for (int i = 0; i < nuevosTitulosCanciones.size(); i++) {
                String titulo = nuevosTitulosCanciones.get(i);
                String artista = (i < nuevosArtistasCanciones.size())
                        ? nuevosArtistasCanciones.get(i)
                        : null;

                Cancion c = CancionService.obtenerCancionPorTituloYArtista(titulo, artista);
                if (c == null) return false;

                c.setAlbum(album);
                nuevasCanciones.add(c);
            }

            album.setCancionList(nuevasCanciones);
        }

        // actualizar artistas
        if (nuevosArtistasColaboradores != null && !nuevosArtistasColaboradores.isEmpty()) {

            // quitar artistas antiguos
            for (Artista art : album.getArtistas()) {
                art.getAlbumesList().remove(album);
                ArtistaDao.actualizarArtista(art);
            }

            ArrayList<Artista> nuevosArtistas = new ArrayList<>();
            for (String nombre : nuevosArtistasColaboradores) {
                Artista a = ArtistaDao.obtenerArtistaPorNombre(nombre);
                if (a == null) return false;

                a.getAlbumesList().add(album);
                nuevosArtistas.add(a);
            }

            album.setArtistas(nuevosArtistas);
        }

        AlbumDao.actualizarAlbum(album);
        return true;
    }

    // NUEVO MÉTODO PARA EL BUSCADOR
    public static List<Album> buscarPorNombre(String texto) {
        return AlbumDao.obtenerAlbums(texto);
    }

}