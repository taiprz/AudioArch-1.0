package com.audioarch.dto;

/**
 * DTO para deserializar un artista de la respuesta JSON de Deezer.
 * Ejemplo: GET https://api.deezer.com/search/artist?q=bladee
 */
public class DeezerArtistDto {
    private long id;
    private String name;
    private String picture_medium;  // 250x250
    private String picture_big;     // 500x500
    private int nb_album;
    private int nb_fan;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPictureMedium() { return picture_medium; }
    public void setPictureMedium(String picture_medium) { this.picture_medium = picture_medium; }

    public String getPictureBig() { return picture_big; }
    public void setPictureBig(String picture_big) { this.picture_big = picture_big; }

    public int getNbAlbum() { return nb_album; }
    public void setNbAlbum(int nb_album) { this.nb_album = nb_album; }

    public int getNbFan() { return nb_fan; }
    public void setNbFan(int nb_fan) { this.nb_fan = nb_fan; }

    @Override
    public String toString() {
        return "DeezerArtist{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
