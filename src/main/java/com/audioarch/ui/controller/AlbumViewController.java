package com.audioarch.ui.controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import com.audioarch.api.DeezerClient;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.dto.DeezerTrackDto;

import java.util.List;

/**
 * Controlador para la vista de álbum.
 * Reutiliza la misma lógica de renderizado de tracks que PlaylistViewController,
 * pero obtiene los datos desde la API de Deezer en lugar de la BD local.
 */
public class AlbumViewController {
    @FXML private ImageView imgPortada;
    @FXML private Label lblNombre, lblArtista, lblMetadata;
    @FXML private VBox vboxTracks;

    private DeezerAlbumDto album;
    private DashboardController parentDashboard;

    public void initData(DeezerAlbumDto album, DashboardController parent) {
        this.album = album;
        this.parentDashboard = parent;

        lblNombre.setText(album.getTitle());
        String artistName = album.getArtist() != null ? album.getArtist().getName() : "Artista Desconocido";
        lblArtista.setText(artistName);
        lblMetadata.setText(album.getNbTracks() + " canciones");

        cargarImagen(imgPortada, album.getCoverBig() != null ? album.getCoverBig() : album.getCoverMedium(), 190);

        cargarTracks();
    }

    private void cargarTracks() {
        vboxTracks.getChildren().clear();
        Label lblCargando = new Label("Cargando canciones del álbum...");
        lblCargando.setStyle("-fx-text-fill: #888; -fx-font-size: 14; -fx-font-style: italic;");
        vboxTracks.getChildren().add(lblCargando);

        Task<List<DeezerTrackDto>> task = new Task<>() {
            @Override
            protected List<DeezerTrackDto> call() {
                return DeezerClient.obtenerTracksDeAlbum(album.getId());
            }
        };

        task.setOnSucceeded(e -> {
            vboxTracks.getChildren().clear();
            List<DeezerTrackDto> tracks = task.getValue();
            if (tracks == null || tracks.isEmpty()) {
                Label noData = new Label("No hay canciones disponibles.");
                noData.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                vboxTracks.getChildren().add(noData);
                lblMetadata.setText("0 canciones");
                return;
            }

            int totalDuration = 0;
            int index = 1;
            for (DeezerTrackDto track : tracks) {
                totalDuration += track.getDuration();
                HBox row = crearFilaTrack(track, index++);
                vboxTracks.getChildren().add(row);
            }

            int mins = totalDuration / 60;
            int secs = totalDuration % 60;
            lblMetadata.setText(tracks.size() + " canciones • " + mins + " min " + secs + " s");
        });

        task.setOnFailed(e -> {
            vboxTracks.getChildren().clear();
            Label error = new Label("Error al cargar las canciones.");
            error.setStyle("-fx-text-fill: #ff5555; -fx-font-size: 14;");
            vboxTracks.getChildren().add(error);
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    /**
     * Crea una fila de track reutilizando el mismo patrón visual de PlaylistViewController.
     */
    private HBox crearFilaTrack(DeezerTrackDto track, int idx) {
        HBox row = new HBox(20);
        row.setStyle("-fx-padding: 10 25; -fx-alignment: CENTER_LEFT; -fx-background-radius: 12;");

        Label lblIndex = new Label(String.valueOf(idx));
        lblIndex.setStyle("-fx-text-fill: #fe7faa; -fx-min-width: 30; -fx-font-size: 14; -fx-font-weight: bold;");

        // Contenedor de la foto con overlay de play
        StackPane imgContainer = new StackPane();

        ImageView iv = new ImageView();
        iv.setFitWidth(50);
        iv.setFitHeight(50);
        Rectangle clip = new Rectangle(50, 50);
        clip.setArcWidth(15);
        clip.setArcHeight(15);
        iv.setClip(clip);

        // Usar la portada del álbum para todas las canciones
        String coverUrl = album.getCoverMedium() != null ? album.getCoverMedium() : album.getCoverBig();
        cargarImagen(iv, coverUrl, 50);

        Label lblPlay = new Label("▶");
        lblPlay.setStyle("-fx-text-fill: #fec7e8; -fx-font-size: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
        lblPlay.setVisible(false);

        Region overlay = new Region();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 8;");
        overlay.setVisible(false);

        imgContainer.getChildren().addAll(iv, overlay, lblPlay);

        boolean hasPreview = track.getPreview() != null && !track.getPreview().isBlank();

        // Hover animations
        row.setOnMouseEntered(e -> {
            row.setStyle("-fx-padding: 10 25; -fx-alignment: CENTER_LEFT; -fx-background-color: rgba(254, 127, 170, 0.1); -fx-background-radius: 12; -fx-cursor: hand;");
            lblIndex.setVisible(false);
            if (hasPreview) {
                lblPlay.setVisible(true);
                overlay.setVisible(true);
            }
        });
        row.setOnMouseExited(e -> {
            row.setStyle("-fx-padding: 10 25; -fx-alignment: CENTER_LEFT; -fx-background-color: transparent; -fx-background-radius: 12;");
            lblIndex.setVisible(true);
            lblPlay.setVisible(false);
            overlay.setVisible(false);
        });

        // Click to play preview
        row.setOnMouseClicked(e -> {
            if (parentDashboard != null && hasPreview) {
                String artistName = album.getArtist() != null ? album.getArtist().getName() : "Desconocido";
                parentDashboard.playPreview(track.getPreview(), track.getTitle(), artistName, coverUrl);
            }
        });

        VBox vbText = new VBox(4);
        vbText.setAlignment(Pos.CENTER_LEFT);
        Label lTitle = new Label(track.getTitle());
        lTitle.setStyle("-fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: 800;");
        Label lArt = new Label(album.getArtist() != null ? album.getArtist().getName() : "");
        lArt.setStyle("-fx-text-fill: #aaa; -fx-font-size: 13; -fx-font-weight: 600;");
        vbText.getChildren().addAll(lTitle, lArt);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        int durSecs = track.getDuration();
        Label lTime = new Label(String.format("%d:%02d", durSecs / 60, durSecs % 60));
        lTime.setStyle("-fx-text-fill: #aaa; -fx-min-width: 50; -fx-font-size: 14; -fx-font-weight: 600;");

        row.getChildren().addAll(lblIndex, imgContainer, vbText, spacer, lTime);
        return row;
    }

    private void cargarImagen(ImageView iv, String url, int fallBackSize) {
        try {
            if (url != null && !url.isBlank()) {
                if (url.startsWith("data:image")) {
                    String base64 = url.substring(url.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    iv.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (url.startsWith("http") || url.startsWith("file:")) {
                    iv.setImage(new Image(url, true));
                } else {
                    String path = url.startsWith("/") ? url : "/" + url;
                    java.io.InputStream stream = getClass().getResourceAsStream(path);
                    if (stream != null) iv.setImage(new Image(stream));
                    else setDefault(iv);
                }
            } else {
                setDefault(iv);
            }
        } catch (Exception e) {
            setDefault(iv);
        }
    }

    private void setDefault(ImageView iv) {
        try {
            iv.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
        } catch (Exception e) {}
    }

    @FXML
    private void cerrar() {
        if (parentDashboard != null) {
            parentDashboard.cerrarPlaylistOverlay();
        }
    }
}
