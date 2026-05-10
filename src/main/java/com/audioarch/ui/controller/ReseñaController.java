package com.audioarch.ui.controller;

import javafx.fxml.FXML;
import javafx.animation.ScaleTransition;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.audioarch.dto.DeezerAlbumDto;
import com.audioarch.dto.DeezerTrackDto;
import com.audioarch.repository.AlbumDao;
import com.audioarch.repository.ArtistaDao;
import com.audioarch.repository.CalificacionDao;
import com.audioarch.repository.CancionDao;
import com.audioarch.domain.model.*;
import java.util.stream.Collectors;
import com.audioarch.ui.service.CustomAlertService;
import com.audioarch.ui.controller.CustomAlertController;

public class ReseñaController {
    @FXML private ImageView imgPortada;
    @FXML private Label lblTipo, lblTitulo, lblDetalle, lblInfoExtra;
    @FXML private TextArea txtReview;
    @FXML private HBox containerEstrellas;

    private Object itemActual;
    private Usuario usuarioSesion;
    private Calificacion calificacionExistente; // Para saber si editamos
    private int notaSeleccionada = 0;
    private final Glow glowEffect = new Glow(0.8);

    // carga el objeto y usuario actual
    public void initData(Object item, Usuario usuario) {
        this.itemActual = item;
        this.usuarioSesion = usuario;
        configurarEstrellas();
        aplicarEfectoVinilo();

        if (item instanceof Cancion c) {
            lblTipo.setText("CANCIÓN");
            lblTitulo.setText(c.getTitulo());
            String artistas = c.getArtistas().stream().map(Artista::getNombre).collect(Collectors.joining(", "));
            lblDetalle.setText(artistas);
            String anio = c.getFechaPublicacion() != null ? String.valueOf(c.getFechaPublicacion().getYear()) : "N/A";
            lblInfoExtra.setText("Duración: " + (c.getDuracion()/60) + ":" + String.format("%02d", (c.getDuracion()%60)) + " | " + anio);
            cargarImagen(c.getPortada());
        } else if (item instanceof Album a) {
            lblTipo.setText("ÁLBUM");
            lblTitulo.setText(a.getNombre());
            lblDetalle.setText("Varios Artistas");
            String anio = a.getFechaPublicacion() != null ? String.valueOf(a.getFechaPublicacion().getYear()) : "N/A";
            lblInfoExtra.setText("Duración: " + (a.getDuracion()/60) + ":" + String.format("%02d", (a.getDuracion()%60)) + " | " + anio);
            cargarImagen(a.getFoto());
        } else if (item instanceof DeezerTrackDto track) {
            lblTipo.setText("CANCIÓN");
            lblTitulo.setText(track.getTitle());
            String artistName = track.getArtist() != null ? track.getArtist().getName() : "Artista Desconocido";
            lblDetalle.setText(artistName);
            int min = track.getDuration() / 60;
            int seg = track.getDuration() % 60;
            lblInfoExtra.setText("Duración: " + min + ":" + String.format("%02d", seg));
            if (track.getAlbum() != null && track.getAlbum().getCoverMedium() != null) {
                cargarImagen(track.getAlbum().getCoverMedium());
            }
        } else if (item instanceof DeezerAlbumDto deezerAlbum) {
            lblTipo.setText("ÁLBUM");
            lblTitulo.setText(deezerAlbum.getTitle());
            String artistName = deezerAlbum.getArtist() != null ? deezerAlbum.getArtist().getName() : "Artista Desconocido";
            lblDetalle.setText(artistName);
            lblInfoExtra.setText(deezerAlbum.getNbTracks() + " tracks");
            if (deezerAlbum.getCoverMedium() != null) {
                cargarImagen(deezerAlbum.getCoverMedium());
            }
        }
    }

    private void aplicarEfectoVinilo() {
        // Crear un clip circular para la imagen
        Circle clip = new Circle(65, 65, 65);
        imgPortada.setClip(clip);

        // Opcional: añadir un borde sutil de neón circular
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web("#ff80ab", 0.6));
        ds.setRadius(20);
        ds.setSpread(0.2);
        imgPortada.setEffect(ds);
    }

    // metodo para cargar datos cuando venimos del botón EDITAR
    public void cargarDatosEdicion(Calificacion c, Usuario u) {
        this.calificacionExistente = c;
        Object item = (c.getCancion() != null) ? c.getCancion() : c.getAlbum();
        initData(item, u);

        this.notaSeleccionada = c.getNota();
        this.txtReview.setText(c.getReview());
        resaltarEstrellas(notaSeleccionada / 2);
    }

    // metodos para las estrellas
    private void configurarEstrellas() {
        for (int i = 0; i < containerEstrellas.getChildren().size(); i++) {
            int index = i + 1;
            Label estrella = (Label) containerEstrellas.getChildren().get(i);
            estrella.setOnMouseClicked(e -> {
                this.notaSeleccionada = index * 2;
                resaltarEstrellas(index);
            });
            estrella.setOnMouseEntered(e -> {
                resaltarEstrellas(index);
                hacerZoom(estrella, true);
            });
            estrella.setOnMouseExited(e -> {
                resaltarEstrellas(notaSeleccionada / 2);
                hacerZoom(estrella, false);
            });
        }
    }

    private void hacerZoom(Label label, boolean zoomIn) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), label);
        st.setToX(zoomIn ? 1.25 : 1.0);
        st.setToY(zoomIn ? 1.25 : 1.0);
        st.play();
    }


    private void resaltarEstrellas(int estrellasActivas) {
        for (int i = 0; i < containerEstrellas.getChildren().size(); i++) {
            Label estrella = (Label) containerEstrellas.getChildren().get(i);
            if (i < estrellasActivas) {
                estrella.setStyle("-fx-text-fill: #ff80ab; -fx-font-size: 35; -fx-cursor: hand;");
                estrella.setEffect(glowEffect);
            } else {
                estrella.setStyle("-fx-text-fill: #333; -fx-font-size: 35; -fx-cursor: hand;");
                estrella.setEffect(null);
            }
        }
    }

    /**
     * Guarda la reseña. Si el item es de Deezer, primero lo persiste en la BD local
     * (crea artista + canción/álbum) y después crea la calificación normalmente.
     */
    @FXML
    private void guardar() {
        if (notaSeleccionada == 0) {
            CustomAlertService.show(
                "Falta tu valoración",
                "Selecciona al menos una estrella antes de guardar tu reseña.",
                CustomAlertController.AlertType.INFO
            );
            return;
        }

        String reviewText = txtReview.getText().trim();
        if (!reviewText.isEmpty() && reviewText.length() < 5) {
            CustomAlertService.show(
                "Reseña muy corta",
                "Si escribes algo, debe tener al menos 5 caracteres.",
                CustomAlertController.AlertType.INFO
            );
            return;
        }
        if (reviewText.length() > 1000) {
            CustomAlertService.show(
                "Reseña muy larga",
                "La reseña no puede superar los 1000 caracteres.",
                CustomAlertController.AlertType.INFO
            );
            return;
        }

        try {
            if (calificacionExistente == null) {
                Object itemParaCalificar = itemActual;

                if (itemActual instanceof DeezerTrackDto track) {
                    itemParaCalificar = persistirDeezerTrack(track);
                    if (itemParaCalificar == null) throw new RuntimeException("No se pudo persistir la canción en la base de datos.");
                } else if (itemActual instanceof DeezerAlbumDto deezerAlbum) {
                    itemParaCalificar = persistirDeezerAlbum(deezerAlbum);
                    if (itemParaCalificar == null) throw new RuntimeException("No se pudo persistir el álbum en la base de datos.");
                }

                Calificacion nueva = new Calificacion();
                nueva.setUsuario(usuarioSesion);
                nueva.setNota(notaSeleccionada);
                nueva.setReview(txtReview.getText());
                if (itemParaCalificar instanceof Cancion c) nueva.setCancion(c);
                else if (itemParaCalificar instanceof Album a) nueva.setAlbum(a);
                CalificacionDao.insertarCalificacion(nueva);
            } else {
                calificacionExistente.setNota(notaSeleccionada);
                calificacionExistente.setReview(txtReview.getText());
                CalificacionDao.actualizarCalificacion(calificacionExistente);
            }

            CustomAlertService.show(
                "¡Reseña guardada!",
                "Tu opinión ya está en la comunidad. ¡Gracias por compartirla!",
                CustomAlertController.AlertType.SUCCESS
            );
            cerrar();

        } catch (Exception e) {
            CustomAlertService.show(
                "No hemos podido guardarlo",
                "Algo ha fallado al guardar tu reseña. Inténtalo de nuevo en unos instantes.",
                CustomAlertController.AlertType.ERROR
            );
        }
    }

    /**
     * Persiste un track de Deezer en la BD local.
     * Crea el artista si no existe, luego crea la canción.
     * Retorna la entidad Cancion de la BD.
     */
    private Cancion persistirDeezerTrack(DeezerTrackDto track) {
        // 1. Obtener o crear el artista
        Artista artista = obtenerOCrearArtista(track.getArtist());

        // 2. Buscar si la canción ya existe en BD
        Cancion existente = buscarCancionExistente(track.getTitle(), artista.getNombre());
        if (existente != null) return existente;

        // 3. Crear la canción nueva
        Cancion cancion = new Cancion();
        cancion.setTitulo(track.getTitle());
        cancion.setDuracion(track.getDuration());

        // Usar la portada del álbum de Deezer como portada
        if (track.getAlbum() != null && track.getAlbum().getCoverBig() != null) {
            cancion.setPortada(track.getAlbum().getCoverBig());
        }

        cancion.getArtistas().add(artista);
        CancionDao.insertarCancion(cancion);

        // Refetch para obtener el ID generado
        return buscarCancionExistente(track.getTitle(), artista.getNombre());
    }

    /**
     * Persiste un álbum de Deezer en la BD local.
     * Crea el artista si no existe, luego crea el álbum.
     * Retorna la entidad Album de la BD.
     */
    private Album persistirDeezerAlbum(DeezerAlbumDto deezerAlbum) {
        // 1. Obtener o crear el artista
        Artista artista = obtenerOCrearArtista(deezerAlbum.getArtist());

        // 2. Buscar si el álbum ya existe en BD
        Album existente = AlbumDao.obtenerAlbumPorNombre(deezerAlbum.getTitle());
        if (existente != null) return existente;

        // 3. Crear el álbum nuevo
        Album album = new Album();
        album.setNombre(deezerAlbum.getTitle());

        // Usar la portada de Deezer
        if (deezerAlbum.getCoverBig() != null) {
            album.setFoto(deezerAlbum.getCoverBig());
        }

        album.getArtistas().add(artista);
        AlbumDao.insertarAlbum(album);

        // Refetch
        return AlbumDao.obtenerAlbumPorNombre(deezerAlbum.getTitle());
    }

    /**
     * Obtiene un artista existente de la BD o lo crea a partir del DTO de Deezer.
     */
    private Artista obtenerOCrearArtista(com.audioarch.dto.DeezerArtistDto deezerArtist) {
        if (deezerArtist == null) {
            // Fallback: artista desconocido
            Artista desconocido = ArtistaDao.obtenerArtistaPorNombre("Artista Desconocido");
            if (desconocido == null) {
                desconocido = new Artista("Artista Desconocido", "");
                ArtistaDao.insertarArtista(desconocido);
                desconocido = ArtistaDao.obtenerArtistaPorNombre("Artista Desconocido");
            }
            return desconocido;
        }

        // Buscar artista exacto en BD
        Artista existente = ArtistaDao.obtenerArtistaPorNombre(deezerArtist.getName());
        if (existente != null && existente.getNombre().equalsIgnoreCase(deezerArtist.getName())) {
            return existente;
        }

        // Crear artista nuevo con foto de Deezer
        Artista nuevo = new Artista(deezerArtist.getName(),
                deezerArtist.getPictureMedium() != null ? deezerArtist.getPictureMedium() : "");
        ArtistaDao.insertarArtista(nuevo);
        return ArtistaDao.obtenerArtistaPorNombre(deezerArtist.getName());
    }

    /**
     * Busca una canción existente por título y artista.
     */
    private Cancion buscarCancionExistente(String titulo, String nombreArtista) {
        var resultados = CancionDao.obtenerCancionPorTitulo(titulo);
        for (Cancion c : resultados) {
            if (c.getArtistas() != null) {
                for (Artista a : c.getArtistas()) {
                    if (a.getNombre().equalsIgnoreCase(nombreArtista)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    // metodo para cerrar
    @FXML private void cerrar() {
        ((Stage) lblTitulo.getScene().getWindow()).close();
    }

    /** Método único de carga: soporta data:image (Base64), http, file: y classpath. */
    private void cargarImagen(String ruta) {
        if (ruta == null || ruta.isBlank()) return;
        try {
            if (ruta.startsWith("data:image")) {
                String base64 = ruta.substring(ruta.indexOf(",") + 1);
                byte[] bytes = java.util.Base64.getDecoder().decode(base64);
                imgPortada.setImage(new Image(new java.io.ByteArrayInputStream(bytes)));
            } else if (ruta.startsWith("http") || ruta.startsWith("file:")) {
                imgPortada.setImage(new Image(ruta, true));
            } else {
                String path = ruta.startsWith("/") ? ruta : "/" + ruta;
                var stream = getClass().getResourceAsStream(path);
                if (stream != null) {
                    imgPortada.setImage(new Image(stream));
                } else {
                    imgPortada.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
                }
            }
        } catch (Exception e) {
            try {
                imgPortada.setImage(new Image(getClass().getResourceAsStream("/imagenes/audio arch - icono.png")));
            } catch(Exception ex) {}
        }
    }
}