package com.audioarch.ui.controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.dto.DeezerTrackDto;
import com.audioarch.domain.model.Cancion;
import com.audioarch.domain.model.Album;
import com.audioarch.domain.model.Artista;
import com.audioarch.domain.model.Usuario;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

import java.io.IOException;
import java.util.stream.Collectors;

public class ItemController {
    @FXML private ImageView imgPortada;
    @FXML private Label lblTitulo, lblArtista;
    @FXML private StackPane playOverlay;
    @FXML private javafx.scene.control.Button btnAddPlaylist, btnCalificar;

    private Object itemActual; // soporta Cancion, Album, DeezerTrackDto, DeezerAlbumDto
    private static Usuario usuarioSesion;
    private static DashboardController dashboardController;

    // Datos para preview
    private String previewUrl;
    private String coverUrl;

    public static void setUsuarioSesion(Usuario u) {
        usuarioSesion = u;
    }

    public static Usuario getUsuarioSesion() {
        return usuarioSesion;
    }

    public static void setDashboardController(DashboardController dc) {
        dashboardController = dc;
    }

    public void setData(Object item) {
        this.itemActual = item;
        this.previewUrl = null;
        this.coverUrl = null;
        
        if (btnAddPlaylist != null) {
            btnAddPlaylist.setVisible(true);
            btnAddPlaylist.setManaged(true);
        }

        if (item instanceof Cancion cancion) {
            // --- Canción de la BD local ---
            lblTitulo.setText(cancion.getTitulo());

            String artistas = "Artista Desconocido";
            if (cancion.getArtistas() != null && !cancion.getArtistas().isEmpty()) {
                artistas = cancion.getArtistas().stream()
                        .map(Artista::getNombre)
                        .collect(Collectors.joining(", "));
            } else if (cancion.getAlbum() != null && cancion.getAlbum().getArtistas() != null) {
                artistas = cancion.getAlbum().getArtistas().stream()
                        .map(Artista::getNombre)
                        .collect(Collectors.joining(", "));
            }
            lblArtista.setText(artistas);
            cargarImagen(cancion.getPortada());

        } else if (item instanceof Album album) {
            // --- Álbum de la BD local ---
            lblTitulo.setText(album.getNombre());

            String artistas = "Varios Artistas";
            if (album.getArtistas() != null && !album.getArtistas().isEmpty()) {
                artistas = album.getArtistas().stream()
                        .map(Artista::getNombre)
                        .collect(Collectors.joining(", "));
            }
            lblArtista.setText("Álbum • " + artistas);
            cargarImagen(album.getFoto());
            if (btnAddPlaylist != null) { btnAddPlaylist.setVisible(false); btnAddPlaylist.setManaged(false); }

        } else if (item instanceof DeezerTrackDto track) {
            // --- Track de Deezer API ---
            lblTitulo.setText(track.getTitle());
            String artistName = track.getArtist() != null ? track.getArtist().getName() : "Artista Desconocido";
            lblArtista.setText("🌐 " + artistName);

            // Cargar portada del álbum desde Deezer CDN
            if (track.getAlbum() != null && track.getAlbum().getCoverMedium() != null) {
                cargarImagen(track.getAlbum().getCoverMedium());
                this.coverUrl = track.getAlbum().getCoverMedium();
            }

            // Preview URL para el reproductor
            this.previewUrl = track.getPreview();

        } else if (item instanceof DeezerAlbumDto deezerAlbum) {
            // --- Álbum de Deezer API ---
            lblTitulo.setText(deezerAlbum.getTitle());
            String artistName = deezerAlbum.getArtist() != null ? deezerAlbum.getArtist().getName() : "Artista Desconocido";
            lblArtista.setText("🌐 Álbum • " + artistName);

            // Cargar portada desde Deezer CDN
            if (deezerAlbum.getCoverMedium() != null) {
                cargarImagen(deezerAlbum.getCoverMedium());
                this.coverUrl = deezerAlbum.getCoverMedium();
            }
            if (btnAddPlaylist != null) { btnAddPlaylist.setVisible(false); btnAddPlaylist.setManaged(false); }
        } else if (item instanceof com.audioarch.dto.DeezerArtistDto deezerArtist) {
            // --- Artista de Deezer API ---
            lblTitulo.setText(deezerArtist.getName());
            lblArtista.setText("🌐 Artista");
            if (deezerArtist.getPictureMedium() != null) {
                cargarImagen(deezerArtist.getPictureMedium());
            }
            if (btnAddPlaylist != null) { btnAddPlaylist.setVisible(false); btnAddPlaylist.setManaged(false); }
            if (btnCalificar != null) {
                actualizarBotonSeguirArtista();
                btnCalificar.setOnAction(e -> {
                    if (usuarioSesion != null) {
                        if (com.audioarch.repository.SeguimientoArtistaDao.isSiguiendo(usuarioSesion, deezerArtist.getId())) {
                            com.audioarch.repository.SeguimientoArtistaDao.dejarDeSeguir(usuarioSesion, deezerArtist.getId());
                        } else {
                            com.audioarch.repository.SeguimientoArtistaDao.seguirArtista(usuarioSesion, deezerArtist.getId(), deezerArtist.getName());
                        }
                        actualizarBotonSeguirArtista();
                    }
                });
            }
        }

        // Configurar hover play overlay
        configurarPlayOverlay();
    }

    /**
     * Configura el overlay de play con hover (fade in/out).
     * Solo se muestra si hay preview disponible.
     */
    private void configurarPlayOverlay() {
        if (playOverlay == null) return;
        
        boolean tienePreview = (previewUrl != null && !previewUrl.isBlank());

        if (!tienePreview) {
            playOverlay.setVisible(false);
            playOverlay.setManaged(false);
            return;
        }

        playOverlay.setOpacity(0);
        playOverlay.setVisible(true);

        // Obtener la card raíz (el StackPane padre)
        StackPane cardRoot = (StackPane) playOverlay.getParent();
        
        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), playOverlay);
        fadeIn.setToValue(1);
        
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), playOverlay);
        fadeOut.setToValue(0);

        cardRoot.setOnMouseEntered(e -> fadeIn.playFromStart());
        cardRoot.setOnMouseExited(e -> fadeOut.playFromStart());

        // Click en el overlay reproduce el preview
        playOverlay.setOnMouseClicked(e -> {
            e.consume(); // evitar que se propague a los botones debajo
            reproducirPreview();
        });
    }

    /**
     * Envía el preview al DashboardController para reproducirlo en el mini player.
     */
    private void reproducirPreview() {
        if (dashboardController == null || previewUrl == null) return;

        String title = lblTitulo.getText();
        String artist = lblArtista.getText();

        dashboardController.playPreview(previewUrl, title, artist, coverUrl);
    }


    private void cargarImagen(String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                if (ruta.startsWith("data:image")) {
                    String base64 = ruta.substring(ruta.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    imgPortada.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (ruta.startsWith("http") || ruta.startsWith("file:")) {
                    imgPortada.setImage(new Image(ruta, true));
                } else {
                    String pathFormateado = ruta.startsWith("/") ? ruta : "/" + ruta;
                    var resource = getClass().getResourceAsStream(pathFormateado);
                    if (resource != null) {
                        imgPortada.setImage(new Image(resource));
                    } else {
                        imgPortada.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                    }
                }

                // Aplicar recorte redondeado para evitar que la imagen se salga de los bordes de la card
                Rectangle clip = new Rectangle(imgPortada.getFitWidth(), imgPortada.getFitHeight());
                clip.setArcWidth(48);
                clip.setArcHeight(48);
                imgPortada.setClip(clip);

            } catch (Exception e) {
                try {
                    imgPortada.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                } catch(Exception ex) {}
            }
        } else {
            try {
                imgPortada.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
            } catch(Exception ex) {}
        }
    }

    // metodo para crear las reseñas abriendo el pop-up
    @FXML
    private void abrirResena() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reseña_view.fxml"));
            Parent root = loader.load();

            ReseñaController controller = loader.getController();
            // pasamos el itemActual (Object: Cancion, Album, DeezerTrackDto o DeezerAlbumDto) y el usuario
            controller.initData(this.itemActual, usuarioSesion);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            CustomAlertService.show(
                "Error",
                "No hemos podido cargar la ventana de reseñas.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }

    @FXML
    private void añadirAPlaylist() {
        if (usuarioSesion == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_to_playlist.fxml"));
            Parent root = loader.load();
            AddToPlaylistController controller = loader.getController();
            controller.initData(this.itemActual, usuarioSesion);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            CustomAlertService.show(
                "Error",
                "No hemos podido abrir la ventana de playlists.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }
    private void actualizarBotonSeguirArtista() {
        if (btnCalificar == null || !(itemActual instanceof com.audioarch.dto.DeezerArtistDto artist)) return;
        boolean isFollowing = com.audioarch.repository.SeguimientoArtistaDao.isSiguiendo(usuarioSesion, artist.getId());
        btnCalificar.setText(isFollowing ? "Siguiendo" : "Seguir");
    }
}