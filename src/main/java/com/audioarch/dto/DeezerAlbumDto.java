package com.audioarch.dto;

/**
 * DTO para deserializar un álbum de la respuesta JSON de Deezer.
 */
public class DeezerAlbumDto {
    private long id;
    private String title;
    private String cover_medium;   // 250x250
    private String cover_big;      // 500x500
    private int nb_tracks;
    private String record_type;    // album, single, ep
    private DeezerArtistDto artist;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getCoverMedium() { return cover_medium; }
    public String getCoverBig() { return cover_big; }
    public int getNbTracks() { return nb_tracks; }
    public String getRecordType() { return record_type; }
    public DeezerArtistDto getArtist() { return artist; }

    @Override
    public String toString() {
        return "DeezerAlbum{" + "id=" + id + ", title='" + title + '\'' +
                ", artist=" + (artist != null ? artist.getName() : "null") + '}';
    }
}
