package com.audioarch.service.external;

import com.audioarch.api.DeezerClient;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.dto.DeezerArtistDto;
import com.audioarch.dto.DeezerTrackDto;

import java.util.List;

/**
 * Servicio que conecta la API de Deezer con la lógica de la aplicación.
 * Realiza búsquedas combinadas (tracks + álbumes) y devuelve los DTOs directamente
 * para que el DashboardController los muestre.
 */
public class DeezerService {

    /**
     * Busca tracks en Deezer API. Limita a los primeros 50 resultados.
     */
    public static List<DeezerTrackDto> buscarTracks(String query) {
        List<DeezerTrackDto> tracks = DeezerClient.buscarTracks(query);
        return tracks.size() > 50 ? tracks.subList(0, 50) : tracks;
    }

    /**
     * Busca álbumes en Deezer API. Limita a los primeros 25 resultados.
     */
    public static List<DeezerAlbumDto> buscarAlbumes(String query) {
        List<DeezerAlbumDto> albums = DeezerClient.buscarAlbumes(query);
        return albums.size() > 25 ? albums.subList(0, 25) : albums;
    }

    /**
     * Busca artistas en Deezer API. Limita a los primeros 25 resultados.
     */
    public static List<DeezerArtistDto> buscarArtistas(String query) {
        List<DeezerArtistDto> artists = DeezerClient.buscarArtistas(query);
        return artists.size() > 25 ? artists.subList(0, 25) : artists;
    }

    // ===================== TRENDING / CHARTS =====================

    /**
     * Obtiene los tracks trending de Deezer (chart global). Limita a 50.
     */
    public static List<DeezerTrackDto> obtenerTrending() {
        List<DeezerTrackDto> tracks = DeezerClient.obtenerChartTracks();
        return tracks.size() > 50 ? tracks.subList(0, 50) : tracks;
    }

    /**
     * Obtiene los álbumes trending de Deezer (chart global). Limita a 50.
     */
    public static List<DeezerAlbumDto> obtenerAlbumesTrending() {
        List<DeezerAlbumDto> albums = DeezerClient.obtenerChartAlbums();
        return albums.size() > 50 ? albums.subList(0, 50) : albums;
    }

    /**
     * Obtiene los artistas trending de Deezer (chart global). Limita a 50.
     */
    public static List<DeezerArtistDto> obtenerArtistasTrending() {
        List<DeezerArtistDto> artists = DeezerClient.obtenerChartArtists();
        return artists.size() > 50 ? artists.subList(0, 50) : artists;
    }
}
