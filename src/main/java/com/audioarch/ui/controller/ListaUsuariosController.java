package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import com.audioarch.domain.model.Usuario;
import com.audioarch.domain.model.SeguimientoArtista;
import com.audioarch.dto.DeezerArtistDto;

import java.util.List;
import com.audioarch.ui.controller.DashboardController;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class ListaUsuariosController {

    @FXML private Label lblTitulo;
    @FXML private VBox vboxUsuariosLista;

    private Usuario usuarioSesion;
    private DashboardController parentDashboard;

    public void setItems(List<?> items, String titulo, Usuario usuarioSesion, DashboardController parent) {
        this.usuarioSesion = usuarioSesion;
        this.parentDashboard = parent;
        lblTitulo.setText(titulo);
        vboxUsuariosLista.getChildren().clear();

        if (items.isEmpty()) {
            Label noItems = new Label("No hay elementos para mostrar.");
            noItems.setStyle("-fx-text-fill: #666; -fx-font-size: 14;");
            vboxUsuariosLista.getChildren().add(noItems);
            return;
        }

        for (Object item : items) {
            if (item instanceof Usuario usuario) {
                vboxUsuariosLista.getChildren().add(crearFilaUsuario(usuario));
            } else if (item instanceof SeguimientoArtista sa) {
                vboxUsuariosLista.getChildren().add(crearFilaArtista(sa));
            }
        }
    }

    private HBox crearFilaUsuario(Usuario usuario) {
        HBox row = crearFilaBase();

        // Avatar
        ImageView avatar = crearAvatar();
        cargarImagenPerfil(avatar, usuario.getFotoPerfil());

        // Neon ring
        javafx.scene.layout.StackPane avatarRing = new javafx.scene.layout.StackPane(avatar);
        avatarRing.setStyle("-fx-border-color: rgba(254, 127, 170, 0.4); -fx-border-width: 2; -fx-border-radius: 50; -fx-padding: 3;");

        // Info
        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nombre = new Label(usuario.getUser());
        nombre.setStyle("-fx-text-fill: #fec7e8; -fx-font-weight: 800; -fx-font-size: 15;");

        String bioText = "Usuario";
        if (usuario.getBiografia() != null && !usuario.getBiografia().isBlank()) {
            bioText = usuario.getBiografia().length() > 45
                    ? usuario.getBiografia().substring(0, 42) + "..."
                    : usuario.getBiografia();
        }
        Label bio = new Label(bioText);
        bio.setStyle("-fx-text-fill: #6a6a6a; -fx-font-size: 12;");

        info.getChildren().addAll(nombre, bio);

        // Arrow indicator
        Label arrow = new Label("›");
        arrow.setStyle("-fx-text-fill: #555; -fx-font-size: 22;");

        row.getChildren().addAll(avatarRing, info, arrow);

        // Click to navigate
        row.setOnMouseClicked(e -> {
            cerrarLista();
            if (parentDashboard != null) {
                parentDashboard.limpiarOverlays();
                parentDashboard.abrirPerfilUsuario(usuario);
            }
        });

        return row;
    }

    private HBox crearFilaArtista(SeguimientoArtista sa) {
        HBox row = crearFilaBase();

        // Avatar
        ImageView avatar = crearAvatar();
        try {
            avatar.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
        } catch (Exception ignored) {}

        javafx.concurrent.Task<String> imgTask = new javafx.concurrent.Task<String>() {
            @Override
            protected String call() throws Exception {
                com.audioarch.dto.DeezerArtistDto artistDto = com.audioarch.api.DeezerClient.obtenerArtistaPorId(sa.getArtistId());
                return artistDto != null ? artistDto.getPictureMedium() : null;
            }
        };
        imgTask.setOnSucceeded(ev -> {
            String url = imgTask.getValue();
            if (url != null && !url.isEmpty()) {
                avatar.setImage(new Image(url, true));
            }
        });
        Thread th = new Thread(imgTask);
        th.setDaemon(true);
        th.start();

        // Neon ring
        javafx.scene.layout.StackPane avatarRing = new javafx.scene.layout.StackPane(avatar);
        avatarRing.setStyle("-fx-border-color: rgba(254, 127, 170, 0.4); -fx-border-width: 2; -fx-border-radius: 50; -fx-padding: 3;");

        // Info
        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nombre = new Label(sa.getArtistName());
        nombre.setStyle("-fx-text-fill: #fec7e8; -fx-font-weight: 800; -fx-font-size: 15;");

        Label tipo = new Label("Artista verificado");
        tipo.setStyle("-fx-text-fill: #6a6a6a; -fx-font-size: 12;");

        info.getChildren().addAll(nombre, tipo);

        // Verified badge
        Label badge = new Label("✓");
        badge.setStyle("-fx-text-fill: #fe7faa; -fx-font-size: 13; -fx-font-weight: bold; " +
                "-fx-background-color: rgba(254, 127, 170, 0.15); -fx-background-radius: 50; " +
                "-fx-min-width: 24; -fx-min-height: 24; -fx-alignment: center;");

        // Arrow indicator
        Label arrow = new Label("›");
        arrow.setStyle("-fx-text-fill: #555; -fx-font-size: 22;");

        row.getChildren().addAll(avatarRing, info, badge, arrow);

        // Click to navigate
        row.setOnMouseClicked(e -> {
            cerrarLista();
            if (parentDashboard != null) {
                parentDashboard.limpiarOverlays();
                try {
                    DeezerArtistDto artistDto = com.audioarch.api.DeezerClient.obtenerArtistaPorId(sa.getArtistId());
                    if (artistDto != null) {
                        parentDashboard.abrirPerfilArtista(artistDto);
                    }
                } catch (Exception ex) {
                    CustomAlertService.show(
                        "Error",
                        "No hemos podido cargar el perfil del usuario.",
                        CustomAlertController.AlertType.ERROR
                    );
                }
            }
        });

        return row;
    }

    /**
     * Crea la fila base con estilos y hover effects.
     */
    private HBox crearFilaBase() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 18, 12, 18));
        String baseStyle = "-fx-background-color: rgba(30, 30, 30, 0.5); -fx-background-radius: 18; " +
                "-fx-border-color: rgba(254, 127, 170, 0.1); -fx-border-radius: 18; -fx-border-width: 1; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: rgba(254, 127, 170, 0.08); -fx-background-radius: 18; " +
                "-fx-border-color: rgba(254, 127, 170, 0.35); -fx-border-radius: 18; -fx-border-width: 1; -fx-cursor: hand;";
        row.setStyle(baseStyle);
        row.setOnMouseEntered(e -> row.setStyle(hoverStyle));
        row.setOnMouseExited(e -> row.setStyle(baseStyle));
        return row;
    }

    /**
     * Crea un avatar circular de 44x44.
     */
    private ImageView crearAvatar() {
        ImageView avatar = new ImageView();
        avatar.setFitWidth(44);
        avatar.setFitHeight(44);
        avatar.setPreserveRatio(false);
        avatar.setSmooth(true);
        avatar.setClip(new Circle(22, 22, 22));
        return avatar;
    }

    private void cargarImagenPerfil(ImageView imageView, String ruta) {
        if (ruta != null && !ruta.isEmpty()) {
            try {
                if (ruta.startsWith("data:image")) {
                    String base64 = ruta.substring(ruta.indexOf(",") + 1);
                    byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                    imageView.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
                } else if (ruta.startsWith("http")) {
                    imageView.setImage(new Image(ruta, true));
                } else {
                    String path = ruta.startsWith("/") ? ruta : "/" + ruta;
                    var resource = getClass().getResourceAsStream(path);
                    if (resource != null) imageView.setImage(new Image(resource));
                }
                return;
            } catch (Exception ignored) {}
        }
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
        } catch (Exception ignored) {}
    }

    @FXML
    private void cerrarLista() {
        Stage stage = (Stage) lblTitulo.getScene().getWindow();
        stage.close();
    }
}
