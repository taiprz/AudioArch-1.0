package com.audioarch.ui.controller;

import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.dto.DeezerTrackDto;
import com.audioarch.repository.CalificacionDao;
import com.audioarch.repository.UsuarioDao;
import com.audioarch.domain.model.Calificacion;
import com.audioarch.domain.model.Usuario;
import com.audioarch.service.external.DeezerService;

import java.io.IOException;
import java.util.List;

public class DashboardController {
    @FXML private TextField txtBuscador;
    @FXML private ComboBox<String> cmbFiltro;
    @FXML private VBox panelResultados;
    @FXML private Label lblBienvenidaNombre;
    @FXML private StackPane pnlCentral;
    @FXML private VBox vistaPrincipal;
    @FXML private VBox sideBar;
    @FXML private Button btnToggle;

    // --- Mini Player FXML ---
    @FXML private VBox miniPlayer;
    @FXML private ImageView mpCover;
    @FXML private Label mpTitle;
    @FXML private Label mpArtist;
    @FXML private Button mpPlayPause;
    @FXML private ProgressBar mpProgress;

    private Usuario usuarioSesion;
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private boolean sidebarVisible = true;

    // --- Media Player ---
    private MediaPlayer mediaPlayer;
    private static DashboardController instance;

    public static DashboardController getInstance() { return instance; }

    public void actualizarSesion(Usuario u) {
        this.usuarioSesion = u;
        if (lblBienvenidaNombre != null) lblBienvenidaNombre.setText(u.getUser() + "!");
    }

    public void setUsuario(Usuario u) {
        instance = this;
        this.usuarioSesion = u;
        ItemController.setUsuarioSesion(u);
        ItemController.setDashboardController(this);
        if (lblBienvenidaNombre != null) lblBienvenidaNombre.setText(u.getUser() + "!");

        txtBuscador.textProperty().addListener((observable, oldValue, newValue) -> {
            debounce.setOnFinished(event -> cargarResultados(newValue));
            debounce.playFromStart();
        });
        
        // Configurar ComboBox
        cmbFiltro.getItems().addAll("Canción", "Álbum", "Artistas", "Usuarios");
        cmbFiltro.setValue("Canción");
        cmbFiltro.setOnAction(e -> cargarResultados(txtBuscador.getText()));
        
        cargarFeed();
    }

    // ===================== FEED PRINCIPAL =====================

    @FXML
    private void cargarFeed() {
        debounce.stop();
        panelResultados.getChildren().clear();
        limpiarOverlays();

        String filtroActivo = cmbFiltro.getValue();
        boolean mostrarUsuarios = "Usuarios".equals(filtroActivo);
        boolean mostrarCanciones = "Canción".equals(filtroActivo) || filtroActivo == null;
        boolean mostrarAlbumes = "Álbum".equals(filtroActivo) || filtroActivo == null;
        boolean mostrarArtistas = "Artistas".equals(filtroActivo) || filtroActivo == null;
        // Si el filtro no se seleccionó (null), o no es un filtro estricto de exclusión, mostramos lo habitual

        if (mostrarUsuarios) {
            Task<List<Usuario>> userTask = new Task<>() {
                @Override protected List<Usuario> call() {
                    return UsuarioDao.obtenerUsuarios();
                }
            };
            userTask.setOnSucceeded(e -> {
                List<Usuario> todosUsuarios = userTask.getValue();
                if (!todosUsuarios.isEmpty()) {
                    crearSeccionFeed("Comunidad", todosUsuarios, true);
                }
            });
            Thread tu = new Thread(userTask);
            tu.setDaemon(true);
            tu.start();
        } else if (filtroActivo == null) {
            // --- Sección 1: "Tu Círculo" ---
            Task<List<Calificacion>> feedTask = new Task<>() {
                @Override protected List<Calificacion> call() {
                    return CalificacionDao.obtenerFeedDeSeguidos(usuarioSesion.getId());
                }
            };
            feedTask.setOnSucceeded(e -> {
                List<Calificacion> feed = feedTask.getValue();
                crearSeccionFeed("Tu Círculo", feed, true);
            });
            Thread tf = new Thread(feedTask);
            tf.setDaemon(true);
            tf.start();
        }

        if (mostrarCanciones || mostrarAlbumes || mostrarArtistas) {
            VBox skeletonContainer = crearSkeletonFeed(8);
            panelResultados.getChildren().add(skeletonContainer);

            Task<Void> trendingTask = new Task<>() {
                @Override
                protected Void call() {
                    List<DeezerTrackDto> tracks = mostrarCanciones ? DeezerService.obtenerTrending() : java.util.Collections.emptyList();
                    List<DeezerAlbumDto> albums = mostrarAlbumes ? DeezerService.obtenerAlbumesTrending() : java.util.Collections.emptyList();
                    List<com.audioarch.dto.DeezerArtistDto> artists = mostrarArtistas ? DeezerService.obtenerArtistasTrending() : java.util.Collections.emptyList();

                    Platform.runLater(() -> {
                        panelResultados.getChildren().remove(skeletonContainer);
                        if (!tracks.isEmpty()) crearSeccionFeed("¿Ya has escuchado estas canciones?", tracks, false);
                        if (!albums.isEmpty()) crearSeccionFeed("¿Ya has escuchado estos álbumes?", albums, false);
                        if (!artists.isEmpty()) crearSeccionFeed("¿Ya has escuchado a...?", artists, false);
                        
                        if (tracks.isEmpty() && albums.isEmpty() && artists.isEmpty()) {
                            Label noData = new Label("No hay recomendaciones o revisa tu conexión.");
                            noData.setStyle("-fx-text-fill: #666; -fx-font-size: 13;");
                            panelResultados.getChildren().add(noData);
                        }
                    });
                    return null;
                }
            };
            Thread thread = new Thread(trendingTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    private void ajustarGrid(FlowPane grid, List<?> items, double w) {
        if (w <= 0) return;
        double cardW = 200;
        double minGap = 20;
        int cols = Math.max(1, (int)((w + minGap) / (cardW + minGap)));
        
        if (cols > 1) {
            // Subtracting 1.0 prevents floating point rounding errors that cause items to wrap early
            double dynamicGap = Math.floor((w - (cols * cardW)) / (cols - 1)) - 1.0;
            grid.setHgap(Math.max(minGap, dynamicGap));
        }
        
        int current = grid.getChildren().size();
        int remainder = current % cols;
        if (remainder != 0 && current < items.size()) {
            int needed = cols - remainder;
            int canAdd = Math.min(needed, items.size() - current);
            for (int i = 0; i < canAdd; i++) {
                grid.getChildren().add(crearNodoGen(items.get(current + i)));
            }
        }
    }

    private void crearSeccionFeed(String tituloStr, List<?> items, boolean esTituloPrincipal) {
        if (items == null || items.isEmpty()) return;
        
        VBox sectionBox = new VBox(15);
        if (esTituloPrincipal) {
            sectionBox.getChildren().add(crearTituloSeccion(tituloStr));
        } else {
            sectionBox.getChildren().add(crearSubtituloSeccion(tituloStr));
        }
        
        FlowPane grid = new FlowPane();
        grid.setHgap(20);
        grid.setVgap(25);
        
        // Initial chunk depends on available width, but we can just use 14 initially and let listener fix it
        int CHUNK_SIZE = 14;
        int limite = Math.min(items.size(), CHUNK_SIZE);
        for (int i = 0; i < limite; i++) {
            grid.getChildren().add(crearNodoGen(items.get(i)));
        }
        
        // Listener dinámico: ajusta Hgap Y auto-rellena filas incompletas
        grid.widthProperty().addListener((obs, oldV, newV) -> {
            ajustarGrid(grid, items, newV.doubleValue());
        });
        
        grid.prefWidthProperty().bind(sectionBox.widthProperty());
        sectionBox.getChildren().add(grid);
        
        Button btnVerMas = new Button("Ver más");
        btnVerMas.getStyleClass().add("btn-ver-mas-feed");
        HBox bottomBox = new HBox(btnVerMas);
        bottomBox.setAlignment(Pos.BOTTOM_LEFT);
        
        if (items.size() > grid.getChildren().size()) {
            sectionBox.getChildren().add(bottomBox);
        }
        
        btnVerMas.setOnAction(e -> {
            int start = grid.getChildren().size();
            int limitItems = Math.min(items.size(), start + CHUNK_SIZE);
            for (int i = start; i < limitItems; i++) {
                grid.getChildren().add(crearNodoGen(items.get(i)));
            }
            
            ajustarGrid(grid, items, grid.getWidth());
            
            if (grid.getChildren().size() >= items.size()) {
                sectionBox.getChildren().remove(bottomBox);
            }
        });
        
        panelResultados.getChildren().add(sectionBox);
        
        // Fix initial state missing ver mas button logic by binding to a one-time delayed check
        javafx.application.Platform.runLater(() -> {
            if (grid.getChildren().size() >= items.size()) {
                sectionBox.getChildren().remove(bottomBox);
            } else if (!sectionBox.getChildren().contains(bottomBox)) {
                sectionBox.getChildren().add(bottomBox);
            }
        });
    }
    
    private Node crearNodoGen(Object obj) {
        if (obj instanceof Calificacion) return crearCardResena((Calificacion) obj);
        if (obj instanceof Usuario) return crearNodoUsuario((Usuario) obj);
        return crearNodoItem(obj);
    }

    // ===================== FEED UI HELPERS =====================

    private Label crearTituloSeccion(String texto) {
        Label titulo = new Label(texto);
        titulo.getStyleClass().add("feed-section-title");
        titulo.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(titulo, new Insets(10, 0, 5, 0));
        return titulo;
    }

    private Label crearSubtituloSeccion(String texto) {
        Label sub = new Label(texto);
        sub.getStyleClass().add("feed-section-subtitle");
        sub.setMaxWidth(Double.MAX_VALUE);
        VBox.setMargin(sub, new Insets(8, 0, 2, 0));
        return sub;
    }

    private VBox crearCardResena(Calificacion resena) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("review-card", "review-card-feed");

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label lblUser = new Label("👤 " + resena.getUsuario().getUser());
        lblUser.getStyleClass().add("label-usuario-link");
        lblUser.setStyle("-fx-text-fill: #ff80ab; -fx-font-weight: bold; -fx-cursor: hand;");
        lblUser.setOnMouseClicked(e -> abrirPerfilUsuario(resena.getUsuario()));
        header.getChildren().add(lblUser);

        String nombre = (resena.getCancion() != null) ? resena.getCancion().getTitulo() : resena.getAlbum().getNombre();
        Label lblNom = new Label("🎵 " + nombre);
        lblNom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

        Label lblNota = new Label("★ " + resena.getNota() + "/10");
        lblNota.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");

        Label lblCom = new Label(resena.getReview());
        lblCom.setWrapText(true);
        lblCom.setStyle("-fx-text-fill: #ccc; -fx-font-style: italic; -fx-font-size: 12;");
        lblCom.setMaxWidth(220);
        lblCom.setMinHeight(30);

        card.getChildren().addAll(header, lblNom, lblNota, lblCom);
        
        aplicarAnimacionEntrada(card);
        
        return card;
    }

    // ===================== BÚSQUEDA =====================

    @FXML
    private void manejarBusqueda() {
        debounce.stop();
        cargarResultados(txtBuscador.getText());
    }

    @FXML
    private void abrirMiPerfil() {
        abrirPerfilUsuario(usuarioSesion);
    }

    /**
     * Carga resultados: si el campo está vacío, muestra el feed.
     * Si hay filtro, busca SOLO en Deezer API (de forma asíncrona) para canciones/álbumes/artistas.
     * Para usuarios, busca en la BD local.
     */
    private void cargarResultados(String filtro) {
        // Si el campo está vacío, volver al feed
        if (filtro == null || filtro.isBlank()) {
            cargarFeed();
            return;
        }
        if (filtro.length() < 2) return;

        panelResultados.getChildren().clear();
        limpiarOverlays();

        String tipoBusqueda = cmbFiltro.getValue();

        if ("Usuarios".equals(tipoBusqueda)) {
            // ----- Búsqueda de Usuarios (BD local) -----
            List<Usuario> usuarios = UsuarioDao.buscarUsuarios(filtro);
            if (usuarios.isEmpty()) {
                Label noResults = new Label("No se encontraron usuarios.");
                noResults.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                panelResultados.getChildren().add(noResults);
            } else {
                FlowPane gridBusqueda = new FlowPane();
                gridBusqueda.setHgap(20);
                gridBusqueda.setVgap(20);
                for (Usuario u : usuarios) {
                    gridBusqueda.getChildren().add(crearNodoUsuario(u));
                }
                panelResultados.getChildren().add(gridBusqueda);
            }

        } else {
            // ----- Búsqueda de Música (Solo Deezer API) -----
            VBox skeletonBusqueda = crearSkeletonFeed(6);
            panelResultados.getChildren().add(skeletonBusqueda);

            String finalFiltro = filtro;
            Task<Void> deezerTask = new Task<>() {
                @Override
                protected Void call() {
                    List<DeezerTrackDto> deezerTracks = java.util.Collections.emptyList();
                    List<DeezerAlbumDto> deezerAlbums = java.util.Collections.emptyList();
                    List<com.audioarch.dto.DeezerArtistDto> deezerArtists = java.util.Collections.emptyList();

                    if ("Canción".equals(tipoBusqueda)) {
                        deezerTracks = DeezerService.buscarTracks(finalFiltro);
                    } else if ("Álbum".equals(tipoBusqueda)) {
                        deezerAlbums = DeezerService.buscarAlbumes(finalFiltro);
                    } else if ("Artistas".equals(tipoBusqueda)) {
                        deezerArtists = DeezerService.buscarArtistas(finalFiltro);
                    }

                    List<DeezerTrackDto> finalTracks = deezerTracks;
                    List<DeezerAlbumDto> finalAlbums = deezerAlbums;
                    List<com.audioarch.dto.DeezerArtistDto> finalArtists = deezerArtists;

                    Platform.runLater(() -> {
                        panelResultados.getChildren().remove(skeletonBusqueda);
                        
                        if (finalTracks.isEmpty() && finalAlbums.isEmpty() && finalArtists.isEmpty()) {
                            Label noResults = new Label("No se encontraron resultados para \"" + finalFiltro + "\"");
                            noResults.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
                            panelResultados.getChildren().add(noResults);
                        }
                        
                        FlowPane gridBusquedaM = new FlowPane();
                        gridBusquedaM.setHgap(20);
                        gridBusquedaM.setVgap(20);
                        for (DeezerTrackDto t : finalTracks) gridBusquedaM.getChildren().add(crearNodoItem(t));
                        for (DeezerAlbumDto a : finalAlbums) gridBusquedaM.getChildren().add(crearNodoItem(a));
                        for (com.audioarch.dto.DeezerArtistDto ar : finalArtists) gridBusquedaM.getChildren().add(crearNodoItem(ar));
                        panelResultados.getChildren().add(gridBusquedaM);
                    });
                    return null;
                }
            };
            Thread thread = new Thread(deezerTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    // ===================== NAVEGACIÓN =====================

    public void limpiarOverlays() {
        if (pnlCentral != null) {
            // Eliminar cualquier nodo que no sea vistaPrincipal ni btnToggle
            pnlCentral.getChildren().removeIf(node -> 
                node != vistaPrincipal && node != btnToggle
            );
        }
        vistaPrincipal.setVisible(true);
        vistaPrincipal.setManaged(true);
    }

    public void abrirPerfilUsuario(Usuario u) {
        try {
            limpiarOverlays(); // Fix: limpiar overlays previos para evitar duplicación
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/perfil.fxml"));
            Parent root = loader.load();
            PerfilController controller = loader.getController();
            
            controller.setDatos(u, usuarioSesion, this);
            pnlCentral.getChildren().add(root);
            StackPane.setAlignment(root, Pos.TOP_LEFT);
            vistaPrincipal.setVisible(false);
            vistaPrincipal.setManaged(false);
            btnToggle.toFront(); // Fix: asegurar que el toggle siempre sea visible
        } catch (IOException e) {
            CustomAlertService.show("Algo ha ido mal", "No hemos podido cargar el perfil. Inténtalo de nuevo.", CustomAlertController.AlertType.ERROR);
        }
    }

    public void abrirPlaylistOverlay(com.audioarch.domain.model.Playlist p) {
        try {
            limpiarOverlays(); // Fix: limpiar overlays previos
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/playlist_view.fxml"));
            Parent root = loader.load();
            com.audioarch.ui.controller.PlaylistViewController controller = loader.getController();
            
            boolean isOwner = p.getUsuario().getId() == usuarioSesion.getId();
            controller.initData(p, isOwner, this);
            pnlCentral.getChildren().add(root);
            StackPane.setAlignment(root, Pos.TOP_LEFT);
            vistaPrincipal.setVisible(false);
            vistaPrincipal.setManaged(false);
            btnToggle.toFront(); // Fix: asegurar que el toggle siempre sea visible
        } catch (IOException e) {
            CustomAlertService.show("Algo ha ido mal", "No hemos podido abrir la playlist. Inténtalo de nuevo.", CustomAlertController.AlertType.ERROR);
        }
    }

    public void abrirAlbumView(DeezerAlbumDto album) {
        try {
            limpiarOverlays();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/album_view.fxml"));
            Parent root = loader.load();
            AlbumViewController controller = loader.getController();
            controller.initData(album, this);
            pnlCentral.getChildren().add(root);
            StackPane.setAlignment(root, Pos.TOP_LEFT);
            vistaPrincipal.setVisible(false);
            vistaPrincipal.setManaged(false);
            btnToggle.toFront();
        } catch (IOException e) {
            CustomAlertService.show("Algo ha ido mal", "No hemos podido cargar el álbum. Inténtalo de nuevo.", CustomAlertController.AlertType.ERROR);
        }
    }

    public void abrirPerfilArtista(com.audioarch.dto.DeezerArtistDto artista) {
        try {
            limpiarOverlays();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/perfil_artista.fxml"));
            Parent root = loader.load();
            PerfilArtistaController controller = loader.getController();
            
            controller.setDatos(artista, this);
            pnlCentral.getChildren().add(root);
            StackPane.setAlignment(root, Pos.TOP_LEFT);
            vistaPrincipal.setVisible(false);
            vistaPrincipal.setManaged(false);
            btnToggle.toFront();
        } catch (IOException e) {
            CustomAlertService.show("Algo ha ido mal", "No hemos podido cargar el perfil del artista. Inténtalo de nuevo.", CustomAlertController.AlertType.ERROR);
        }
    }

    public void cerrarUltimoOverlay() {
        if (pnlCentral.getChildren().size() > 2) {
            // Encontrar el último nodo que sea un overlay (ni vistaPrincipal ni btnToggle)
            for (int i = pnlCentral.getChildren().size() - 1; i >= 0; i--) {
                javafx.scene.Node node = pnlCentral.getChildren().get(i);
                if (node != vistaPrincipal && node != btnToggle) {
                    pnlCentral.getChildren().remove(i);
                    break;
                }
            }
        }
        
        // Si ya no quedan overlays, volver a mostrar la vista principal
        boolean hasOverlays = pnlCentral.getChildren().stream()
                .anyMatch(node -> node != vistaPrincipal && node != btnToggle);
                
        if (!hasOverlays) {
            vistaPrincipal.setVisible(true);
            vistaPrincipal.setManaged(true);
        }
    }

    @FXML
    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        
        javafx.animation.TranslateTransition transition = new javafx.animation.TranslateTransition(Duration.millis(300), sideBar);
        
        if (!sidebarVisible) {
            transition.setToX(-250);
            transition.setOnFinished(e -> {
                sideBar.setManaged(false);
                sideBar.setVisible(false);
            });
            btnToggle.setText("▶");
        } else {
            sideBar.setVisible(true);
            sideBar.setManaged(true);
            transition.setToX(0);
            btnToggle.setText("☰");
        }
        transition.play();
    }

    public void cerrarPerfilOverlay() {
        cerrarUltimoOverlay();
    }
    
    public void cerrarPlaylistOverlay() {
        cerrarUltimoOverlay();
    }

    // ===================== PREVIEW PLAYER =====================

    /**
     * Reproduce un preview MP3 de 30 segundos desde Deezer.
     * Muestra el mini player en la sidebar con controles de reproducción.
     */
    public void playPreview(String previewUrl, String title, String artist, String coverUrl) {
        if (previewUrl == null || previewUrl.isBlank()) return;
        
        // Si es la misma canción, alternar play/pause en lugar de reiniciar
        if (mediaPlayer != null && mediaPlayer.getMedia() != null && previewUrl.equals(mediaPlayer.getMedia().getSource())) {
            togglePlayPause();
            return;
        }

        // Detener reproducción anterior
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(previewUrl);
            mediaPlayer = new MediaPlayer(media);

            // Actualizar UI del mini player
            if (mpTitle != null) mpTitle.setText(title != null ? title : "");
            if (mpArtist != null) mpArtist.setText(artist != null ? artist : "");
            if (mpPlayPause != null) mpPlayPause.setText("⏸");

            // Cargar cover
            if (mpCover != null && coverUrl != null && !coverUrl.isBlank()) {
                try {
                    mpCover.setImage(new Image(coverUrl, true));
                } catch (Exception e) { /* fallback silencioso */ }
            }

            // Mostrar mini player
            if (miniPlayer != null) {
                miniPlayer.setVisible(true);
                miniPlayer.setManaged(true);
            }

            // Progress bar listener
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (mpProgress != null && mediaPlayer.getTotalDuration() != null) {
                    double progress = newTime.toMillis() / mediaPlayer.getTotalDuration().toMillis();
                    mpProgress.setProgress(progress);
                }
            });

            // Al terminar, resetear
            mediaPlayer.setOnEndOfMedia(() -> {
                if (mpPlayPause != null) mpPlayPause.setText("▶");
                if (mpProgress != null) mpProgress.setProgress(0);
            });

            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Error al reproducir preview: " + e.getMessage());
        }
    }

    @FXML
    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            if (mpPlayPause != null) mpPlayPause.setText("▶");
        } else {
            mediaPlayer.play();
            if (mpPlayPause != null) mpPlayPause.setText("⏸");
        }
    }

    // ===================== NODOS UI =====================

    Node crearNodoItem(Object item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/item_cancion.fxml"));
            Node card = loader.load();
            ((ItemController)loader.getController()).setData(item);
            
            if (item instanceof com.audioarch.dto.DeezerArtistDto) {
                card.setOnMouseClicked(e -> {
                    abrirPerfilArtista((com.audioarch.dto.DeezerArtistDto) item);
                });
            } else if (item instanceof DeezerAlbumDto) {
                card.setOnMouseClicked(e -> {
                    abrirAlbumView((DeezerAlbumDto) item);
                });
            }
            
            aplicarAnimacionEntrada(card);
            
            return card;
        } catch (IOException e) { return new Label("Error"); }
    }

    private Node crearNodoUsuario(Usuario u) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/item_usuario.fxml"));
            VBox card = loader.load();
            ((ItemUsuarioController)loader.getController()).setData(u, this.usuarioSesion, this);
            
            aplicarAnimacionEntrada(card);
            
            return card;
        } catch (IOException e) { return new Label("Error"); }
    }

    private void aplicarAnimacionEntrada(Node node) {
        node.setOpacity(0);
        node.setTranslateY(20);
        
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.millis(400), node);
        fade.setToValue(1);
        
        javafx.animation.TranslateTransition slide = new javafx.animation.TranslateTransition(Duration.millis(400), node);
        slide.setToY(0);
        
        new javafx.animation.ParallelTransition(fade, slide).play();
    }

    // ===================== SKELETON LOADING =====================

    private VBox crearSkeletonFeed(int count) {
        VBox container = new VBox(20);
        container.setPadding(new Insets(0, 0, 10, 0));

        // Skeleton de titulo de sección
        javafx.scene.layout.Region titleSk = new javafx.scene.layout.Region();
        titleSk.setPrefSize(200, 20);
        titleSk.setMaxSize(200, 20);
        titleSk.getStyleClass().add("skeleton-line");
        container.getChildren().add(titleSk);

        javafx.scene.layout.FlowPane grid = new javafx.scene.layout.FlowPane();
        grid.setHgap(20);
        grid.setVgap(25);
        for (int i = 0; i < count; i++) {
            grid.getChildren().add(crearSkeletonCard());
        }
        container.getChildren().add(grid);

        animarSkeleton(container);
        return container;
    }

    private VBox crearSkeletonCard() {
        VBox card = new VBox(10);
        card.setPrefSize(200, 265);
        card.setMaxSize(200, 265);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("skeleton-card");

        javafx.scene.layout.Region img = new javafx.scene.layout.Region();
        img.setPrefSize(172, 150);
        img.setMaxSize(172, 150);
        img.getStyleClass().add("skeleton-image");

        javafx.scene.layout.Region line1 = new javafx.scene.layout.Region();
        line1.setPrefHeight(14);
        line1.setMaxWidth(Double.MAX_VALUE);
        line1.getStyleClass().add("skeleton-line");
        VBox.setVgrow(line1, javafx.scene.layout.Priority.NEVER);

        javafx.scene.layout.Region line2 = new javafx.scene.layout.Region();
        line2.setPrefSize(120, 11);
        line2.setMaxSize(120, 11);
        line2.getStyleClass().add("skeleton-line-short");

        javafx.scene.layout.Region line3 = new javafx.scene.layout.Region();
        line3.setPrefSize(70, 10);
        line3.setMaxSize(70, 10);
        line3.getStyleClass().add("skeleton-line-short");

        card.getChildren().addAll(img, line1, line2, line3);
        return card;
    }

    private void animarSkeleton(javafx.scene.Node root) {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,           new KeyValue(root.opacityProperty(), 0.35)),
            new KeyFrame(Duration.millis(850),    new KeyValue(root.opacityProperty(), 1.0)),
            new KeyFrame(Duration.millis(1700),   new KeyValue(root.opacityProperty(), 0.35))
        );
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
        root.sceneProperty().addListener((obs, o, n) -> { if (n == null) timeline.stop(); });
    }

    @FXML
    private void cerrarSesion() {
        // Detener reproductor si está activo
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) txtBuscador.getScene().getWindow();
            SceneTransitionUtil.cambiarVistaConBlur(stage, root);
        } catch (IOException e) {
            CustomAlertService.show(
                "Error",
                "No se pudo cerrar sesión correctamente.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }
}
