<div align="center">

#  AudioArch

*Una plataforma de escritorio multiplataforma donde la música se descubre, se analiza y se comparte.*

---

**Autores:** Jismel Perez Lapaix & Etam Paez Guerrero
**Curso:** 2º DAM · 2025/2026 · Proyecto de Fin de Grado

</div>

---

##  Descripción

AudioArch es una aplicación de escritorio multiplataforma finalizada, desarrollada en **Java + JavaFX**, que permite a los usuarios explorar, catalogar y compartir música de forma social.

El sistema integra en tiempo real el catálogo global de la **API de Deezer** (más de 90 millones de pistas) con una **base de datos PostgreSQL en la nube** (Neon.tech), creando un ecosistema híbrido donde la música se descubre, se puntúa y se comparte entre usuarios.

AudioArch cuenta con un **módulo social completo**: perfiles personalizables (con almacenamiento de imágenes nativas en Base64), sistema de seguimiento entre usuarios, un feed de actividad dinámico que prioriza a los usuarios más activos, gestión de playlists personalizadas y un robusto sistema de reseñas con calificaciones numéricas. Todo ello se presenta con una interfaz visual premium en **Dark Mode** con efectos de glassmorphism y micro-animaciones fluidas.

---

##  Funcionalidades principales

| Módulo | Descripción |
|---|---|
|  **Autenticación** | Registro e inicio de sesión con contraseñas cifradas mediante BCrypt. |
|  **Búsqueda híbrida** | Motor de búsqueda con debounce (500ms) para consultar la API de Deezer y la base de datos local simultáneamente. |
|  **Sistema de reseñas** | Creación, edición y borrado de calificaciones (1–10 estrellas) acompañadas de texto libre. |
|  **Red social** | Sistema de seguimiento de usuarios con ordenamiento inteligente basado en la actividad reciente (últimas reseñas). |
|  **Feed de actividad** | Visualización en tiempo real de las reseñas de la comunidad general y de tu red personal de seguidos. |
|  **Reproductor** | Previsualización nativa de 30 segundos del catálogo oficial. |
|  **Playlists mixtas** | Listas de reproducción con carátulas personalizadas (Base64) e integración de tracks de la API. |
|  **Perfiles Nativos** | Personalización completa (foto, banner y biografía) con almacenamiento universal Base64, garantizando visibilidad en la nube para todos los usuarios. |

---

##  Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| **Java** | 17 / 21 (Liberica JDK) | Lenguaje principal (Backend y Lógica) |
| **JavaFX** | 21.0.1 | Interfaz gráfica (FXML + CSS) |
| **Hibernate ORM** | 6.2.7.Final | Mapeo objeto-relacional (JPA 3.1) |
| **PostgreSQL** | Cloud (Neon.tech) | Base de datos serverless en la nube |
| **Deezer API** | REST pública | Catálogo musical global en tiempo real |
| **Gson** | 2.10.1 | Deserialización de respuestas JSON |
| **jBCrypt** | 0.4 | Cifrado seguro de contraseñas |

---

##  Arquitectura y patrones

El proyecto implementa una **arquitectura MVC** limpia:

```text
src/main/java/com/audioarch/
├── domain/
│   └── model/          # Entidades JPA (Usuario, Cancion, Album, Artista, ...)
├── repository/         # Capa DAO (acceso a datos con Hibernate)
├── service/            # Capa de servicios (lógica híbrida local + API)
├── api/                # Cliente HTTP para la API de Deezer
├── dto/                # Data Transfer Objects de Deezer
└── ui/
    ├── controller/     # Controladores JavaFX
    └── service/        # Servicios de UI (alertas, Base64, navegación)
```

**Patrones aplicados:**
- **MVC** — Separación de modelo, vista (FXML/CSS) y controladores.
- **DAO** — Encapsulación del acceso a datos.
- **Service Layer** — Coordinación de lógica de negocio y llamadas HTTP.
- **Singleton** — Gestión de la fábrica de conexiones (EntityManagerFactory).
- **DTO** — Desacoplamiento del modelo interno respecto a la API externa.

---

##  Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/taiprz/AudioArch-1.0.git
cd AudioArch

# 2. Compilar y lanzar la aplicación
mvn clean javafx:run
```

>  **Sin configuración de base de datos:** Al utilizar persistencia en la nube mediante Neon.tech e Hibernate, la aplicación genera y sincroniza su esquema automáticamente al ejecutarse. No se requiere servidor local.

---

##  Diseño — Identidad "The Sonic Gallery"

El proyecto presenta una estética premium denominada **Dark Electronic Music Editorial**:
- **Paleta de Colores:** Deep Black (#050505), Neon Pink (#FE7FAA), Lavender Glow (#EC91FF).
- **Técnicas Visuales:** Glassmorphism (paneles translúcidos), sombreado de contorno (Neon Glow), recortes de imágenes circulares (Efecto Vinilo) y transiciones fluidas de entrada (FadeTransition).

---

##  Licencia

Proyecto académico finalizado para el **Proyecto de Fin de Grado** · 2º DAM · 2025/2026.
<div align="center">
Hecho con ❤️ y mucha música por <strong>Jismel Perez Lapaix</strong> & <strong>Etam Paez Guerrero</strong>
</div>
