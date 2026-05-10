package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.dto.DeezerTrackDto;
import com.audioarch.repository.PlaylistDao;
import com.audioarch.domain.model.Album;
import com.audioarch.domain.model.Cancion;
import com.audioarch.domain.model.ItemPlaylist;
import com.audioarch.domain.model.Playlist;
import com.audioarch.domain.model.Usuario;
import com.audioarch.ui.service.CustomAlertService;

import java.util.List;
import java.util.stream.Collectors;

public class AddToPlaylistController {
    @FXML private VBox vboxPlaylists;
    @FXML private TextField txtNuevaPlaylist;

    private Object itemActual;
    private Usuario usuarioSesion;

    public void initData(Object item, Usuario usuario) {
        this.itemActual = item;
        this.usuarioSesion = usuario;
        cargarPlaylists();
    }

    private void cargarPlaylists() {
        vboxPlaylists.getChildren().clear();
        List<Playlist> pList = PlaylistDao.obtenerPorUsuario(usuarioSesion.getId());
        
        if (pList.isEmpty()) {
            Label noData = new Label("No tienes playlists aún.");
            noData.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-font-size: 13;");
            vboxPlaylists.getChildren().add(noData);
        } else {
            for (Playlist p : pList) {
                Button btn = new Button(p.getNombre() + " (" + p.getItems().size() + ")");
                btn.getStyleClass().add("playlist-item-btn");
                btn.setMaxWidth(Double.MAX_VALUE);
                btn.setOnAction(e -> agregarItemAPlaylist(p));
                vboxPlaylists.getChildren().add(btn);
            }
        }
    }

    @FXML
    private void crearPlaylist() {
        String nombre = txtNuevaPlaylist.getText().trim();
        if (nombre == null || nombre.isBlank()) {
            CustomAlertService.show("Campo vacío",
                "Escribe un nombre para la playlist antes de crearla.",
                CustomAlertController.AlertType.ERROR);
            return;
        }
        if (nombre.length() > 60) {
            CustomAlertService.show("Nombre demasiado largo",
                "El nombre de la playlist no puede superar 60 caracteres.",
                CustomAlertController.AlertType.ERROR);
            return;
        }

        // Comprobar duplicado (mismo nombre, mismo usuario, case-insensitive)
        List<Playlist> existentes = PlaylistDao.obtenerPorUsuario(usuarioSesion.getId());
        boolean yaExiste = existentes.stream()
            .anyMatch(p -> p.getNombre().equalsIgnoreCase(nombre));
        if (yaExiste) {
            CustomAlertService.show("Playlist duplicada",
                "Ya tienes una playlist con ese nombre.",
                CustomAlertController.AlertType.ERROR);
            return;
        }

        Playlist p = new Playlist(nombre, "", usuarioSesion);
        PlaylistDao.guardar(p);
        
        CustomAlertService.show(
            "Éxito", 
            "Playlist '" + nombre + "' creada correctamente.", 
            CustomAlertController.AlertType.SUCCESS
        );
        
        txtNuevaPlaylist.clear();
        cargarPlaylists();
    }

    private void agregarItemAPlaylist(Playlist p) {
        String titulo = "Unknown";
        String artista = "Unknown";
        String cover = "";
        String previewUrl = "";
        int duracion = 0;

        if (itemActual instanceof Cancion) {
            Cancion c = (Cancion) itemActual;
            titulo = c.getTitulo();
            cover = c.getPortada();
            duracion = c.getDuracion();
            if (c.getArtistas() != null && !c.getArtistas().isEmpty()) {
                artista = c.getArtistas().stream().map(a->a.getNombre()).collect(Collectors.joining(", "));
            }
        } else if (itemActual instanceof Album) {
            Album a = (Album) itemActual;
            titulo = "Álbum: " + a.getNombre();
            cover = a.getFoto();
            if (a.getArtistas() != null && !a.getArtistas().isEmpty()) {
                artista = a.getArtistas().stream().map(ar->ar.getNombre()).collect(Collectors.joining(", "));
            }
        } else if (itemActual instanceof DeezerTrackDto) {
            DeezerTrackDto t = (DeezerTrackDto) itemActual;
            titulo = t.getTitle();
            duracion = t.getDuration();
            previewUrl = t.getPreview();
            if (t.getArtist() != null) artista = t.getArtist().getName();
            if (t.getAlbum() != null && t.getAlbum().getCoverMedium() != null) cover = t.getAlbum().getCoverMedium();
        } else if (itemActual instanceof DeezerAlbumDto) {
            DeezerAlbumDto a = (DeezerAlbumDto) itemActual;
            titulo = "Álbum: " + a.getTitle();
            if (a.getArtist() != null) artista = a.getArtist().getName();
            if (a.getCoverMedium() != null) cover = a.getCoverMedium();
        }

        // --- Verificación de Duplicados ---
        for (ItemPlaylist item : p.getItems()) {
            if (item.getTitulo().equalsIgnoreCase(titulo) && item.getArtista().equalsIgnoreCase(artista)) {
                CustomAlertService.show(
                    "Duplicado", 
                    "Esta canción ya existe dentro de esta playlist.", 
                    CustomAlertController.AlertType.ERROR
                );
                return; // Previene cerrar e insertar
            }
        }

        ItemPlaylist ip = new ItemPlaylist(titulo, artista, cover, previewUrl, duracion);
        p.addItem(ip);
        PlaylistDao.actualizar(p);

        CustomAlertService.show(
            "Éxito", 
            "Canción añadida correctamente a la playlist.", 
            CustomAlertController.AlertType.SUCCESS
        );

        cerrar();
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) vboxPlaylists.getScene().getWindow();
        stage.close();
    }
}
