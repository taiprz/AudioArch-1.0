package com.audioarch.ui.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import com.audioarch.dto.DeezerArtistDto;
import com.audioarch.dto.DeezerTrackDto;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.api.DeezerClient;
import com.audioarch.repository.SeguimientoArtistaDao;
import com.audioarch.domain.model.Usuario;

import java.util.List;

public class PerfilArtistaController {

    @FXML private ImageView imgBanner;
    @FXML private ImageView imgFotoPerfil;
    @FXML private Label lblUsername;
    @FXML private Label lblSeguidores;
    @FXML private Label lblSiguiendo;
    @FXML private Button btnAccionSigue;
    @FXML private Label lblBio;
    @FXML private VBox vboxReseñas;
    @FXML private VBox vboxPlaylists;
    @FXML private Button btnTabResenas;
    @FXML private Button btnTabPlaylists;

    private DeezerArtistDto artista;
    private DashboardController parentDashboard;

    public void setDatos(DeezerArtistDto artista, DashboardController parent) {
        this.artista = artista;
        this.parentDashboard = parent;

        lblUsername.setText(artista.getName());
        lblBio.setText("Artista verificado en Deezer");
        
        Usuario uSesion = ItemController.getUsuarioSesion();
        // Carga asíncrona de datos de la base de datos para no bloquear UI
        javafx.concurrent.Task<Void> dbTask = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() {
                int fansAMostrar = SeguimientoArtistaDao.contarSeguidores(artista.getId());
                boolean isFollowing = uSesion != null && SeguimientoArtistaDao.isSiguiendo(uSesion, artista.getId());
                
                javafx.application.Platform.runLater(() -> {
                    lblSeguidores.setText(fansAMostrar + " Fans");
                    lblSeguidores.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14;");
                    
                    if (uSesion != null) {
                        btnAccionSigue.setVisible(true);
                        btnAccionSigue.setManaged(true);
                        
                        if (isFollowing) {
                            btnAccionSigue.setText("Siguiendo");
                            btnAccionSigue.setStyle("-fx-background-color: transparent; -fx-text-fill: #fec7e8; -fx-border-color: #fec7e8; -fx-border-radius: 30; -fx-padding: 8 24; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand;");
                        } else {
                            btnAccionSigue.setText("Seguir");
                            btnAccionSigue.setStyle("-fx-background-color: linear-gradient(to right, #fec7e8, #ec91ff); -fx-text-fill: #1a001a; -fx-background-radius: 30; -fx-padding: 8 24; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand;");
                        }

                        btnAccionSigue.setOnAction(e -> {
                            Usuario u = ItemController.getUsuarioSesion();
                            if (u != null) {
                                // Toggle status
                                boolean currentlyFollowing = btnAccionSigue.getText().equals("Siguiendo");
                                if (currentlyFollowing) {
                                    SeguimientoArtistaDao.dejarDeSeguir(u, artista.getId());
                                } else {
                                    SeguimientoArtistaDao.seguirArtista(u, artista.getId(), artista.getName());
                                }
                                int fansActuales = SeguimientoArtistaDao.contarSeguidores(artista.getId());
                                lblSeguidores.setText(fansActuales + " Fans");
                                actualizarBotonSigue(u); // Sync back
                            }
                        });
                    } else {
                        btnAccionSigue.setVisible(false);
                        btnAccionSigue.setManaged(false);
                    }
                });
                return null;
            }
        };
        Thread t = new Thread(dbTask);
        t.setDaemon(true);
        t.start();

        btnTabResenas.setText("Top Tracks");
        btnTabPlaylists.setText("Álbumes");

        cargarImagen(imgFotoPerfil, artista.getPictureMedium() != null ? artista.getPictureMedium() : artista.getPictureBig());
        // El banner se deja en negro por defecto para que no se estire la foto cuadrada
        
        if (imgBanner.getParent() instanceof javafx.scene.layout.Region) {
            imgBanner.fitWidthProperty().bind(((javafx.scene.layout.Region)imgBanner.getParent()).widthProperty());
        }

        cargarTopTracks();
        cargarAlbumes();
    }

    private void actualizarBotonSigue(Usuario u) {
        if (u != null) {
            boolean isFollowing = SeguimientoArtistaDao.isSiguiendo(u, artista.getId());
            if (isFollowing) {
                btnAccionSigue.setText("Siguiendo");
                btnAccionSigue.setStyle("-fx-background-color: transparent; -fx-text-fill: #fec7e8; -fx-border-color: #fec7e8; -fx-border-radius: 30; -fx-padding: 8 24; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand;");
            } else {
                btnAccionSigue.setText("Seguir");
                btnAccionSigue.setStyle("-fx-background-color: linear-gradient(to right, #fec7e8, #ec91ff); -fx-text-fill: #1a001a; -fx-background-radius: 30; -fx-padding: 8 24; -fx-font-weight: 700; -fx-font-size: 13px; -fx-cursor: hand;");
            }
        }
    }

    private void cargarImagen(ImageView imageView, String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                imageView.setImage(new Image(ruta, true));
            } catch (Exception e) {
                try {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                } catch(Exception ex) {}
            }
        }
    }

    private void cargarTopTracks() {
        vboxReseñas.getChildren().clear();
        javafx.scene.layout.VBox skTop = crearSkeletonCards(5);
        vboxReseñas.getChildren().add(skTop);
        animarSkeleton(skTop);

        Task<List<DeezerTrackDto>> task = new Task<>() {
            @Override
            protected List<DeezerTrackDto> call() {
                return DeezerClient.obtenerTopTracksDeArtista(artista.getId());
            }
        };

        task.setOnSucceeded(e -> {
            vboxReseñas.getChildren().clear();
            List<DeezerTrackDto> tracks = task.getValue();
            if (tracks == null || tracks.isEmpty()) {
                Label noData = new Label("No hay pistas populares disponibles.");
                noData.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                vboxReseñas.getChildren().add(noData);
                return;
            }

            FlowPane grid = new FlowPane(20, 20);
            vboxReseñas.getChildren().add(grid);

            for (DeezerTrackDto track : tracks) {
                Node node = parentDashboard.crearNodoItem(track);
                grid.getChildren().add(node);
            }
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void cargarAlbumes() {
        vboxPlaylists.getChildren().clear();
        javafx.scene.layout.VBox skAlb = crearSkeletonCards(5);
        vboxPlaylists.getChildren().add(skAlb);
        animarSkeleton(skAlb);

        Task<List<DeezerAlbumDto>> task = new Task<>() {
            @Override
            protected List<DeezerAlbumDto> call() {
                return DeezerClient.obtenerAlbumesDeArtista(artista.getId());
            }
        };

        task.setOnSucceeded(e -> {
            vboxPlaylists.getChildren().clear();
            List<DeezerAlbumDto> albums = task.getValue();
            if (albums == null || albums.isEmpty()) {
                Label noData = new Label("No hay álbumes disponibles.");
                noData.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                vboxPlaylists.getChildren().add(noData);
                return;
            }

            FlowPane grid = new FlowPane(20, 20);
            vboxPlaylists.getChildren().add(grid);

            for (DeezerAlbumDto album : albums) {
                Node node = parentDashboard.crearNodoItem(album);
                grid.getChildren().add(node);
            }
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private javafx.scene.layout.VBox crearSkeletonCards(int count) {
        javafx.scene.layout.VBox cont = new javafx.scene.layout.VBox(20);
        javafx.scene.layout.FlowPane fp = new javafx.scene.layout.FlowPane(20, 20);
        for (int i = 0; i < count; i++) {
            javafx.scene.layout.VBox card = new javafx.scene.layout.VBox(10);
            card.setPrefSize(200, 260); card.setMaxSize(200, 260);
            card.setPadding(new javafx.geometry.Insets(14));
            card.getStyleClass().add("skeleton-card");
            javafx.scene.layout.Region img = new javafx.scene.layout.Region();
            img.setPrefSize(172, 150); img.setMaxSize(172, 150);
            img.getStyleClass().add("skeleton-image");
            javafx.scene.layout.Region l1 = new javafx.scene.layout.Region();
            l1.setPrefHeight(13); l1.setMaxWidth(Double.MAX_VALUE);
            l1.getStyleClass().add("skeleton-line");
            javafx.scene.layout.Region l2 = new javafx.scene.layout.Region();
            l2.setPrefSize(110, 10); l2.setMaxSize(110, 10);
            l2.getStyleClass().add("skeleton-line-short");
            card.getChildren().addAll(img, l1, l2);
            fp.getChildren().add(card);
        }
        cont.getChildren().add(fp);
        return cont;
    }

    private void animarSkeleton(javafx.scene.Node root) {
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.ZERO,
                new javafx.animation.KeyValue(root.opacityProperty(), 0.35)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(850),
                new javafx.animation.KeyValue(root.opacityProperty(), 1.0)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(1700),
                new javafx.animation.KeyValue(root.opacityProperty(), 0.35))
        );
        tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tl.play();
        root.sceneProperty().addListener((obs, o, n) -> { if (n == null) tl.stop(); });
    }

    @FXML
    private void mostrarTabResenas() {
        vboxReseñas.setVisible(true); vboxReseñas.setManaged(true);
        vboxPlaylists.setVisible(false); vboxPlaylists.setManaged(false);
        btnTabResenas.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand; -fx-border-color: #ff80ab; -fx-border-width: 0 0 2 0;");
        btnTabPlaylists.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand; -fx-border-width: 0;");
    }

    @FXML
    private void mostrarTabPlaylists() {
        vboxReseñas.setVisible(false); vboxReseñas.setManaged(false);
        vboxPlaylists.setVisible(true); vboxPlaylists.setManaged(true);
        btnTabPlaylists.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand; -fx-border-color: #ff80ab; -fx-border-width: 0 0 2 0;");
        btnTabResenas.setStyle("-fx-background-color: transparent; -fx-text-fill: #aaa; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand; -fx-border-width: 0;");
    }

    @FXML
    private void cerrarPerfil() {
        if (parentDashboard != null) {
            parentDashboard.cerrarPerfilOverlay();
        }
    }
}
