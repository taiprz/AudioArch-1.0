package com.audioarch.api;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.audioarch.dto.*;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * Cliente HTTP para consumir la API pública de Deezer.
 * No requiere autenticación para búsquedas públicas.
 *
 * Endpoints utilizados:
 *   - GET https://api.deezer.com/search/artist?q={query}
 *   - GET https://api.deezer.com/search/album?q={query}
 *   - GET https://api.deezer.com/search/track?q={query}
 *   - GET https://api.deezer.com/artist/{id}/albums
 *   - GET https://api.deezer.com/album/{id}/tracks
 */
public class DeezerClient {

    private static final String BASE_URL = "https://api.deezer.com";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .build();
    private static final Gson gson = new Gson();

    // ===================== BÚSQUEDAS =====================

    /**
     * Busca artistas por nombre.
     */
    public static List<DeezerArtistDto> buscarArtistas(String query) {
        String url = BASE_URL + "/search/artist?q=" + encode(query);
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerArtistDto>>(){}.getType();
        DeezerSearchResponse<DeezerArtistDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Busca álbumes por nombre.
     */
    public static List<DeezerAlbumDto> buscarAlbumes(String query) {
        String url = BASE_URL + "/search/album?q=" + encode(query);
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerAlbumDto>>(){}.getType();
        DeezerSearchResponse<DeezerAlbumDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Busca canciones/tracks por nombre.
     */
    public static List<DeezerTrackDto> buscarTracks(String query) {
        String url = BASE_URL + "/search/track?q=" + encode(query);
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerTrackDto>>(){}.getType();
        DeezerSearchResponse<DeezerTrackDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    // ===================== DETALLES =====================

    /**
     * Obtiene los álbumes de un artista por su ID de Deezer.
     */
    public static List<DeezerAlbumDto> obtenerAlbumesDeArtista(long artistId) {
        String url = BASE_URL + "/artist/" + artistId + "/albums";
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerAlbumDto>>(){}.getType();
        DeezerSearchResponse<DeezerAlbumDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Obtiene los tracks de un álbum por su ID de Deezer.
     */
    public static List<DeezerTrackDto> obtenerTracksDeAlbum(long albumId) {
        String url = BASE_URL + "/album/" + albumId + "/tracks";
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerTrackDto>>(){}.getType();
        DeezerSearchResponse<DeezerTrackDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Obtiene los top tracks de un artista por su ID de Deezer.
     */
    public static List<DeezerTrackDto> obtenerTopTracksDeArtista(long artistId) {
        String url = BASE_URL + "/artist/" + artistId + "/top?limit=10";
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerTrackDto>>(){}.getType();
        DeezerSearchResponse<DeezerTrackDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Obtiene un artista por su ID de Deezer.
     */
    public static DeezerArtistDto obtenerArtistaPorId(long artistId) {
        String url = BASE_URL + "/artist/" + artistId;
        String json = hacerGetRequest(url);
        if (json == null) return null;
        return gson.fromJson(json, DeezerArtistDto.class);
    }

    // ===================== CHARTS / TRENDING =====================

    /**
     * Obtiene las canciones trending (chart global) de Deezer.
     * Endpoint público, no requiere autenticación.
     */
    public static List<DeezerTrackDto> obtenerChartTracks() {
        String url = BASE_URL + "/chart/0/tracks?limit=50";
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerTrackDto>>(){}.getType();
        DeezerSearchResponse<DeezerTrackDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Obtiene los álbumes trending (chart global) de Deezer.
     * Endpoint público, no requiere autenticación.
     */
    public static List<DeezerAlbumDto> obtenerChartAlbums() {
        String url = BASE_URL + "/chart/0/albums?limit=50";
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerAlbumDto>>(){}.getType();
        DeezerSearchResponse<DeezerAlbumDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    /**
     * Obtiene los artistas trending (chart global) de Deezer.
     * Endpoint público, no requiere autenticación.
     */
    public static List<DeezerArtistDto> obtenerChartArtists() {
        String url = BASE_URL + "/chart/0/artists?limit=50";
        String json = hacerGetRequest(url);
        if (json == null) return Collections.emptyList();

        Type type = new TypeToken<DeezerSearchResponse<DeezerArtistDto>>(){}.getType();
        DeezerSearchResponse<DeezerArtistDto> response = gson.fromJson(json, type);
        return response != null && response.getData() != null ? response.getData() : Collections.emptyList();
    }

    // ===================== UTILIDADES =====================

    private static String hacerGetRequest(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.err.println("Deezer API error: HTTP " + response.statusCode());
                return null;
            }
        } catch (java.net.http.HttpTimeoutException e) {
            mostrarAlertaError("Tiempo de espera agotado", "La conexión con Deezer ha tardado demasiado. Comprueba tu conexión a internet.");
            return null;
        } catch (IOException | InterruptedException e) {
            mostrarAlertaError("Error de conexión", "No hemos podido conectar con los servidores de Deezer en este momento.");
            return null;
        }
    }

    private static void mostrarAlertaError(String titulo, String mensaje) {
        javafx.application.Platform.runLater(() -> {
            CustomAlertService.show(
                titulo, 
                mensaje, 
                CustomAlertController.AlertType.ERROR
            );
        });
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
