package com.audioarch.dto;

/**
 * DTO para deserializar un track de la respuesta JSON de Deezer.
 */
public class DeezerTrackDto {
    private long id;
    private String title;
    private String title_short;
    private int duration;          // en segundos
    private String preview;        // URL a preview MP3 de 30s
    private DeezerArtistDto artist;
    private DeezerAlbumDto album;

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getTitleShort() { return title_short; }
    public int getDuration() { return duration; }
    public String getPreview() { return preview; }
    public DeezerArtistDto getArtist() { return artist; }
    public DeezerAlbumDto getAlbum() { return album; }

    @Override
    public String toString() {
        return "DeezerTrack{" + "id=" + id + ", title='" + title + '\'' +
                ", duration=" + duration + "s, artist=" +
                (artist != null ? artist.getName() : "null") + '}';
    }
}
