package com.audioarch.service.local;

import com.audioarch.repository.ArtistaDao;
import com.audioarch.domain.model.Artista;

public class ArtistaService {

    public static boolean registrarArtista(String nombre, String foto) {

        if (nombre == null || nombre.isBlank()) return false;
        if (foto == null || foto.isBlank()) return false;

        if (ArtistaDao.existeArtista(nombre)) {
            return false;
        }

        Artista a = new Artista();
        a.setNombre(nombre);
        a.setFoto(foto);
        ArtistaDao.insertarArtista(a);
        return true;
    }

    public static Artista obtenerPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        return ArtistaDao.obtenerArtistaPorNombre(nombre);
    }

    public static boolean eliminarArtista(int id) {
        Artista a = ArtistaDao.getArtistaPorId(id);
        if (a == null) return false;

        ArtistaDao.eliminarArtista(a);
        return true;
    }

    public static boolean actualizarArtista(int id, String nuevoNombre, String nuevaFoto) {
        Artista a = ArtistaDao.getArtistaPorId(id);
        if (a == null) return false;

        if (nuevoNombre != null && !nuevoNombre.isBlank()) {
            a.setNombre(nuevoNombre);
        }

        if (nuevaFoto != null && !nuevaFoto.isBlank()) {
            a.setFoto(nuevaFoto);
        }

        ArtistaDao.actualizarArtista(a);
        return true;
    }
}
