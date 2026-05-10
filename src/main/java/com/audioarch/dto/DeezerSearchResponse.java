package com.audioarch.dto;

import java.util.List;

/**
 * Wrapper genérico para la respuesta de búsqueda de Deezer.
 * Deezer siempre devuelve: { "data": [...], "total": N }
 */
public class DeezerSearchResponse<T> {
    private List<T> data;
    private int total;

    public List<T> getData() { return data; }
    public int getTotal() { return total; }
}
