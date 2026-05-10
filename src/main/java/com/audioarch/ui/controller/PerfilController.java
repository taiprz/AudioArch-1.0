package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.audioarch.repository.CalificacionDao;
import com.audioarch.repository.UsuarioDao;
import com.audioarch.domain.model.Calificacion;
import com.audioarch.domain.model.Usuario;

import java.util.List;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class PerfilController {

    @FXML private ImageView imgBanner;
    @FXML private ImageView imgFotoPerfil;
    @FXML private javafx.scene.layout.Region skeletonAvatar;
    @FXML private Label lblUsername;
    @FXML private Label lblSeguidores;
    @FXML private Label lblSiguiendo;
    @FXML private Button btnAccionSigue;
    @FXML private Label lblBio;
    @FXML private VBox vboxReseñas;

    @FXML private Button btnTabResenas;
    @FXML private Button btnTabPlaylists;
    @FXML private VBox vboxPlaylists;

    private Usuario usuarioPerfil;
    private Usuario usuarioSesion;
    private DashboardController parentDashboard;

    public void setDatos(Usuario usuarioPerfil, Usuario usuarioSesion, DashboardController parent) {
        this.parentDashboard = parent;
        setDatos(usuarioPerfil, usuarioSesion);
    }

    public void setDatos(Usuario usuarioPerfil, Usuario usuarioSesion) {
        this.usuarioSesion = usuarioSesion;
        
        lblUsername.setText("Cargando...");
        lblBio.setText("");
        lblSeguidores.setText("");
        lblSiguiendo.setText("");
        vboxReseñas.getChildren().clear();
        vboxPlaylists.getChildren().clear();
        
        // Skeleton de carga mientras esperamos los datos async
        java.util.List<javafx.scene.Node> skeletonRows = crearSkeletonResenas(4);
        vboxReseñas.getChildren().addAll(skeletonRows);

        javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<>() {
            @Override
            protected Void call() throws Exception {
                // Fetch data async
                Usuario userFull = UsuarioDao.getUsuarioConRelaciones(usuarioPerfil.getId());
                java.util.List<com.audioarch.domain.model.SeguimientoArtista> artistasSeguidos = com.audioarch.repository.SeguimientoArtistaDao.obtenerArtistasSeguidos(userFull);
                java.util.List<Calificacion> resenas = CalificacionDao.obtenerCalificaciones(userFull.getUser());
                java.util.List<com.audioarch.domain.model.Playlist> playlists = com.audioarch.repository.PlaylistDao.obtenerPorUsuario(userFull.getId());
                
                javafx.application.Platform.runLater(() -> {
                    PerfilController.this.usuarioPerfil = userFull;
                    lblUsername.setText(userFull.getUser());
                    lblBio.setText((userFull.getBiografia() != null && !userFull.getBiografia().isBlank()) ? 
                                    userFull.getBiografia() : "Este usuario aún no tiene biografía.");
                    
                    lblSeguidores.setText(userFull.getSeguidores().size() + " Seguidores");
                    lblSeguidores.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14; -fx-cursor: hand;");
                    lblSeguidores.setOnMouseClicked(e -> abrirListaUsuarios(
                        userFull.getSeguidores().stream().map(com.audioarch.domain.model.Seguimiento::getSeguidor).toList(),
                        "Seguidores"
                    ));

                    int totalSiguiendo = userFull.getSiguiendo().size() + artistasSeguidos.size();
                    lblSiguiendo.setText(totalSiguiendo + " Siguiendo");
                    lblSiguiendo.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14; -fx-cursor: hand;");
                    lblSiguiendo.setOnMouseClicked(e -> {
                        java.util.List<Object> mixta = new java.util.ArrayList<>();
                        mixta.addAll(userFull.getSiguiendo().stream().map(com.audioarch.domain.model.Seguimiento::getSeguido).toList());
                        mixta.addAll(artistasSeguidos);
                        abrirListaUsuarios(mixta, "Siguiendo");
                    });

                    // Ocultar skeleton avatar cuando la imagen esté lista
                    animarSkeleton(skeletonAvatar, 0);
                    imgFotoPerfil.setOpacity(0);
                    
                    Runnable hideSkeleton = () -> {
                        skeletonAvatar.setVisible(false);
                        skeletonAvatar.setManaged(false);
                        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(
                            javafx.util.Duration.millis(300), imgFotoPerfil);
                        ft.setToValue(1.0);
                        ft.play();
                    };

                    imgFotoPerfil.imageProperty().addListener((obs, o, n) -> {
                        if (n != null) {
                            if (n.getProgress() == 1.0) {
                                hideSkeleton.run();
                            } else {
                                n.progressProperty().addListener((pObs, pO, pN) -> {
                                    if (pN.doubleValue() == 1.0) hideSkeleton.run();
                                });
                            }
                        }
                    });
                    
                    cargarImagen(imgFotoPerfil, userFull.getFotoPerfil());
                    
                    if (imgFotoPerfil.getImage() != null && imgFotoPerfil.getImage().getProgress() == 1.0) {
                        hideSkeleton.run();
                    }
                    if (userFull.getBanner() != null && !userFull.getBanner().isBlank()) {
                        cargarImagen(imgBanner, userFull.getBanner());
                    }
                    if (imgBanner.getParent() instanceof javafx.scene.layout.Region) {
                        imgBanner.fitWidthProperty().bind(((javafx.scene.layout.Region)imgBanner.getParent()).widthProperty());
                    }
                    
                    configurarBotonAccion();
                    
                    vboxReseñas.getChildren().clear();
                    renderizarResenas(resenas);
                    renderizarPlaylists(playlists);
                });
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void configurarBotonAccion() {
        if (usuarioSesion != null && usuarioSesion.getId() == usuarioPerfil.getId()) {
            btnAccionSigue.setText("Editar Perfil");
            btnAccionSigue.setStyle("-fx-background-color: transparent; -fx-border-color: #888; -fx-text-fill: #aaa; -fx-border-radius: 20; -fx-padding: 5 20; -fx-font-weight: bold; -fx-cursor: hand;");
            btnAccionSigue.setOnAction(e -> {
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/editar_perfil.fxml"));
                    javafx.scene.Parent root = loader.load();
                    EditarPerfilController controller = loader.getController();
                    controller.setDatos(usuarioPerfil, this);

                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
                    javafx.scene.Scene scene = new javafx.scene.Scene(root);
                    scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    stage.setScene(scene);
                    stage.show();
                } catch (Exception ex) {
                    System.err.println("Error al cargar playlists o calificaciones en perfil: " + ex.getMessage());
                }
            });
        } else {
            actualizarBotonSeguir();
            btnAccionSigue.setOnAction(e -> {
                boolean estaSiguiendo = com.audioarch.repository.SeguimientoDao.estaSiguiendo(usuarioSesion.getId(), usuarioPerfil.getId());
                if (estaSiguiendo) {
                    com.audioarch.repository.SeguimientoDao.dejarDeSeguir(usuarioSesion, usuarioPerfil);
                } else {
                    com.audioarch.repository.SeguimientoDao.seguir(usuarioSesion, usuarioPerfil);
                }
                actualizarBotonSeguir();
                // Actualizar contadores
                this.usuarioPerfil = UsuarioDao.getUsuarioConRelaciones(usuarioPerfil.getId()); // recargar
                lblSeguidores.setText(this.usuarioPerfil.getSeguidores().size() + " Seguidores");
            });

        }
    }

    private void actualizarBotonSeguir() {
        boolean estaSiguiendo = com.audioarch.repository.SeguimientoDao.estaSiguiendo(usuarioSesion.getId(), usuarioPerfil.getId());
        if (estaSiguiendo) {
            btnAccionSigue.setText("Dejar de Seguir");
            btnAccionSigue.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 20; -fx-padding: 5 20; -fx-font-weight: bold; -fx-cursor: hand;");
        } else {
            btnAccionSigue.setText("Seguir");
            btnAccionSigue.setStyle("-fx-background-color: transparent; -fx-border-color: #ff80ab; -fx-text-fill: #ff80ab; -fx-border-radius: 20; -fx-padding: 5 20; -fx-font-weight: bold; -fx-cursor: hand;");
        }
    }

    private void cargarImagen(ImageView imageView, String ruta) {
        if (ruta != null && !ruta.isBlank()) {
            try {
                if (ruta.startsWith("data:image")) {
                    // Imagen embebida como Base64 (subida desde archivo local)
                    String base64 = ruta.substring(ruta.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    imageView.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (ruta.startsWith("http") || ruta.startsWith("file:")) {
                    imageView.setImage(new Image(ruta, true));
                } else {
                    String pathFormateado = ruta.startsWith("/") ? ruta : "/" + ruta;
                    var resource = getClass().getResourceAsStream(pathFormateado);
                    if (resource != null) {
                        imageView.setImage(new Image(resource));
                    } else {
                        imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                    }
                }
            } catch (Exception e) {
                try {
                    imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                } catch(Exception ex) {}
            }
        } else {
            try {
                imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
            } catch(Exception e) {}
        }
    }

    private void renderizarResenas(List<Calificacion> resenas) {
        if (resenas.isEmpty()) {
            Label noReviews = new Label("Este usuario aún no ha escrito reseñas.");
            noReviews.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
            vboxReseñas.getChildren().add(noReviews);
            return;
        }

        javafx.scene.layout.FlowPane grid = new javafx.scene.layout.FlowPane(15, 15);
        grid.getStyleClass().add("results-grid"); // Reutilizamos para el padding de las sombras
        vboxReseñas.getChildren().add(grid);

        for (Calificacion resena : resenas) {
            VBox card = new VBox(8);
            card.setAlignment(Pos.CENTER);
            card.getStyleClass().addAll("review-card", "review-card-compact");
            card.setPrefWidth(180);

            // Imagen de portada
            javafx.scene.image.ImageView imgCover = new javafx.scene.image.ImageView();
            imgCover.setFitWidth(200);
            imgCover.setFitHeight(140);
            imgCover.setPreserveRatio(false);
            imgCover.setSmooth(true);
            
            // Clip para bordes redondeados
            javafx.scene.shape.Rectangle clipR = new javafx.scene.shape.Rectangle(200, 140);
            clipR.setArcWidth(44); // Emparejado con el radio de la review-card
            clipR.setArcHeight(44);
            imgCover.setClip(clipR);

            String portadaUrl = null;
            if (resena.getCancion() != null && resena.getCancion().getPortada() != null) {
                portadaUrl = resena.getCancion().getPortada();
            } else if (resena.getAlbum() != null && resena.getAlbum().getFoto() != null) {
                portadaUrl = resena.getAlbum().getFoto();
            }

            if (portadaUrl != null && !portadaUrl.isBlank()) {
                try {
                    if (portadaUrl.startsWith("http") || portadaUrl.startsWith("file:")) {
                        imgCover.setImage(new javafx.scene.image.Image(portadaUrl, true));
                    } else {
                        String path = portadaUrl.startsWith("/") ? portadaUrl : "/" + portadaUrl;
                        var res = getClass().getResourceAsStream(path);
                        if (res != null) imgCover.setImage(new javafx.scene.image.Image(res));
                    }
                } catch (Exception ignored) {}
            }

            if (imgCover.getImage() == null) {
                try {
                    imgCover.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                } catch (Exception ignored) {}
            }

            // Sección de información
            VBox infoBox = new VBox(5);
            infoBox.setAlignment(Pos.CENTER_LEFT);
            infoBox.getStyleClass().add("review-card-info");

            String nombreItem = (resena.getCancion() != null) ? resena.getCancion().getTitulo() : resena.getAlbum().getNombre();
            Label lblTitulo = new Label(nombreItem);
            lblTitulo.getStyleClass().add("card-title");
            lblTitulo.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
            lblTitulo.setMaxWidth(170);

            Label lblNota = new Label("★ " + resena.getNota() + "/10");
            lblNota.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");

            infoBox.getChildren().addAll(lblTitulo, lblNota);

            if (resena.getReview() != null && !resena.getReview().isBlank()) {
                Label lblReview = new Label(resena.getReview());
                lblReview.setWrapText(true);
                lblReview.setMaxWidth(170);
                lblReview.setMaxHeight(40);
                lblReview.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-font-size: 11;");
                infoBox.getChildren().add(lblReview);
            }

            // Añadir imagen e info y aplicar animación
            card.getChildren().addAll(imgCover, infoBox);

            boolean esPropio = usuarioSesion != null && usuarioSesion.getId() == usuarioPerfil.getId();

            if (esPropio) {
                HBox acciones = new HBox(10);
                acciones.setAlignment(Pos.CENTER);
                acciones.setStyle("-fx-padding: 0 15 12 15;");

                Button btnEd = new Button("EDITAR");
                btnEd.getStyleClass().add("btn-secondary-solid");
                btnEd.setOnAction(e -> abrirEdicionResena(resena));
                
                Button btnEliminar = new Button("ELIMINAR");
                btnEliminar.getStyleClass().add("btn-danger-outline");
                btnEliminar.setOnAction(e -> {
                    CalificacionDao.eliminarCalificacion(resena.getId());
                    cargarReseñas();
                });
                
                acciones.getChildren().addAll(btnEd, btnEliminar);
                card.getChildren().add(acciones);
            }

            aplicarAnimacionEntrada(card);
            grid.getChildren().add(card);
        }
    }

    private void renderizarPlaylists(java.util.List<com.audioarch.domain.model.Playlist> playlists) {
        if (playlists.isEmpty()) {
            Label l = new Label("Este usuario no tiene playlists públicas.");
            l.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
            vboxPlaylists.getChildren().add(l);
            return;
        }

        javafx.scene.layout.FlowPane grid = new javafx.scene.layout.FlowPane(20, 20);
        vboxPlaylists.getChildren().add(grid);

        for (com.audioarch.domain.model.Playlist p : playlists) {
            javafx.scene.layout.StackPane card = new javafx.scene.layout.StackPane();
            card.getStyleClass().add("song-card-v2");
            card.setPrefWidth(200);
            card.setPrefHeight(280);
            card.setOnMouseClicked(e -> {
                if (parentDashboard != null) parentDashboard.abrirPlaylistOverlay(p);
            });

            // Portada de la Playlist como fondo
            ImageView iv = new ImageView();
            iv.setFitWidth(200);
            iv.setFitHeight(280);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            cargarImagen(iv, p.getPortadaUrl());

            // Capa de gradiente oscuro
            javafx.scene.layout.Region gradient = new javafx.scene.layout.Region();
            gradient.getStyleClass().add("card-gradient-overlay");

            // Información y Botones sobre el gradiente
            VBox overlayContent = new VBox(5);
            overlayContent.setAlignment(Pos.BOTTOM_LEFT);
            overlayContent.setStyle("-fx-padding: 15;");
            overlayContent.setPickOnBounds(false);

            Label lblT = new Label(p.getNombre());
            lblT.getStyleClass().add("card-title");
            
            String des = (p.getDescripcion() != null && !p.getDescripcion().isBlank()) ? p.getDescripcion() : p.getItems().size() + " pistas";
            Label lblSub = new Label(des);
            lblSub.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic; -fx-font-size: 11;");

            overlayContent.getChildren().addAll(lblT, lblSub);

            boolean esPropio = usuarioSesion != null && usuarioSesion.getId() == usuarioPerfil.getId();
            
            if (esPropio) {
                HBox acciones = new HBox(10);
                acciones.setAlignment(Pos.CENTER_LEFT);
                acciones.setStyle("-fx-padding: 10 0 0 0;");

                Button btnEd = new Button("EDITAR");
                btnEd.getStyleClass().add("btn-secondary-solid");
                btnEd.setOnAction(e -> {
                     e.consume(); // Previene abrir playlist
                     abrirEdicionPlaylist(p);
                });

                Button btnEl = new Button("ELIMINAR");
                btnEl.getStyleClass().add("btn-eliminar-white");
                btnEl.setOnAction(e -> {
                     e.consume(); // Previene abrir playlist
                     com.audioarch.repository.PlaylistDao.eliminar(p.getId());
                     cargarPlaylists();
                });
                
                acciones.getChildren().addAll(btnEd, btnEl);
                overlayContent.getChildren().add(acciones);
            }

            card.getChildren().addAll(iv, gradient, overlayContent);
            aplicarAnimacionEntrada(card);
            grid.getChildren().add(card);
        }
    }

    private void abrirEdicionPlaylist(com.audioarch.domain.model.Playlist p) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/editar_playlist.fxml"));
            javafx.scene.Parent root = loader.load();
            EditarPlaylistController controller = loader.getController();
            // Necesitamos un "refrescador" para cuando termine de editar. 
            // EditarPlaylistController.initData espera (Playlist, PlaylistViewController) o similar? 
            // Mirando EditarPlaylistController...
            controller.initData(p, null); // Por ahora pasamos null como refrescador o implementamos uno simple
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            
            cargarPlaylists();
        } catch (Exception e) { 
            CustomAlertService.show(
                "Error",
                "No hemos podido cargar los seguidores.",
                CustomAlertController.AlertType.ERROR
            );
        }
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
        } else {
            Stage stage = (Stage) btnAccionSigue.getScene().getWindow();
            stage.close();
        }
    }

    private void abrirListaUsuarios(List<?> usuarios, String titulo) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/lista_usuarios.fxml"));
            javafx.scene.Parent root = loader.load();
            ListaUsuariosController controller = loader.getController();
            controller.setItems(usuarios, titulo, usuarioSesion, parentDashboard);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);

            // Cerrar al hacer click fuera del diálogo
            stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) stage.close();
            });

            stage.show();
        } catch (Exception e) {
            CustomAlertService.show(
                "Error",
                "No hemos podido cargar la ventana de edición de perfil.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }

    private void abrirEdicionResena(Calificacion c) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/reseña_view.fxml"));
            javafx.scene.Parent root = loader.load();
            ReseñaController controller = loader.getController();
            controller.cargarDatosEdicion(c, usuarioSesion);

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            stage.setScene(scene);
            stage.showAndWait();

            cargarReseñas();
        } catch (java.io.IOException e) {
            CustomAlertService.show(
                "Error",
                "No hemos podido crear la nueva playlist.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }

    private void cargarReseñas() {
        new Thread(() -> {
            java.util.List<com.audioarch.domain.model.Calificacion> resenas = com.audioarch.repository.CalificacionDao.obtenerCalificaciones(usuarioPerfil.getUser());
            javafx.application.Platform.runLater(() -> {
                vboxReseñas.getChildren().clear();
                renderizarResenas(resenas);
            });
        }).start();
    }

    private void cargarPlaylists() {
        new Thread(() -> {
            java.util.List<com.audioarch.domain.model.Playlist> playlists = com.audioarch.repository.PlaylistDao.obtenerPorUsuario(usuarioPerfil.getId());
            javafx.application.Platform.runLater(() -> {
                vboxPlaylists.getChildren().clear();
                renderizarPlaylists(playlists);
            });
        }).start();
    }

    private void aplicarAnimacionEntrada(javafx.scene.Node node) {
        node.setOpacity(0);
        node.setTranslateY(20);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(400), node);
        fade.setToValue(1);
        
        javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(400), node);
        slide.setToY(0);
        
        new javafx.animation.ParallelTransition(fade, slide).play();
    }

    // ===================== SKELETON LOADING =====================

    private java.util.List<javafx.scene.Node> crearSkeletonResenas(int count) {
        java.util.List<javafx.scene.Node> rows = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            javafx.scene.Node row = crearSkeletonRow();
            animarSkeleton(row, i * 80);
            rows.add(row);
        }
        return rows;
    }

    private javafx.scene.layout.HBox crearSkeletonRow() {
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(14);
        row.setPadding(new javafx.geometry.Insets(14, 18, 14, 18));
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.getStyleClass().add("skeleton-row");
        row.setMaxWidth(Double.MAX_VALUE);

        // Avatar / portada
        javafx.scene.layout.Region avatar = new javafx.scene.layout.Region();
        avatar.setPrefSize(52, 52);
        avatar.setMaxSize(52, 52);
        avatar.getStyleClass().add("skeleton-image");

        // Bloque de texto
        javafx.scene.layout.VBox lines = new javafx.scene.layout.VBox(8);
        lines.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.HBox.setHgrow(lines, javafx.scene.layout.Priority.ALWAYS);

        javafx.scene.layout.Region line1 = new javafx.scene.layout.Region();
        line1.setPrefHeight(13);
        line1.setMaxWidth(Double.MAX_VALUE);
        line1.getStyleClass().add("skeleton-line");

        javafx.scene.layout.Region line2 = new javafx.scene.layout.Region();
        line2.setPrefSize(130, 10);
        line2.setMaxSize(130, 10);
        line2.getStyleClass().add("skeleton-line-short");

        javafx.scene.layout.Region line3 = new javafx.scene.layout.Region();
        line3.setPrefSize(80, 9);
        line3.setMaxSize(80, 9);
        line3.getStyleClass().add("skeleton-line-short");

        lines.getChildren().addAll(line1, line2, line3);
        row.getChildren().addAll(avatar, lines);
        return row;
    }

    private void animarSkeleton(javafx.scene.Node root, long delayMs) {
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(delayMs),
                new javafx.animation.KeyValue(root.opacityProperty(), 0.35)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(delayMs + 850),
                new javafx.animation.KeyValue(root.opacityProperty(), 1.0)),
            new javafx.animation.KeyFrame(javafx.util.Duration.millis(delayMs + 1700),
                new javafx.animation.KeyValue(root.opacityProperty(), 0.35))
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
        root.sceneProperty().addListener((obs, o, n) -> { if (n == null) timeline.stop(); });
    }
}
