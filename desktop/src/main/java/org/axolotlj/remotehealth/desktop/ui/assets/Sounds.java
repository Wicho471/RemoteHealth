package org.axolotlj.remotehealth.desktop.ui.assets;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.desktop.paths.DesktopPaths;

/**
 * Carga y gestiona en memoria todos los recursos de audio del proyecto. Permite
 * reproducir sonidos a partir de rutas declaradas en {@link DesktopPaths}.
 * <p>
 * Los sonidos se precargan en memoria para una reproducción eficiente y repetida.
 * <p>
 * Ejemplo de uso: {@code Sounds.SND_HEARTBEAT.play();}
 */
public class Sounds {
    private static final DataLogger logger = Log.get();

    /**
     * Clase interna que representa un sonido precargado en memoria.
     */
    public static class Sound {
        private Clip clip; // El objeto Clip que contiene el audio en memoria.

        /**
         * Carga el recurso de sonido en memoria al momento de la creación.
         * @param resourcePath La ruta interna al recurso de sonido.
         */
        private Sound(String resourcePath) {
            if (resourcePath == null) {
                logger.logFatal("La ruta del recurso de sonido es null.");
                // Se lanza una excepción para detener la ejecución si la configuración es incorrecta.
                throw new IllegalArgumentException("La ruta del recurso no puede ser null.");
            }

            try {
                URL url = Sounds.class.getResource(resourcePath);
                if (url == null) {
                    logger.logWarn("No se encontró el recurso de sonido: " + resourcePath);
                    return; // El clip permanecerá null y play() no hará nada.
                }

                // Usamos try-with-resources para asegurar que el AudioInputStream se cierre.
                try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(url)) {
                    this.clip = AudioSystem.getClip();
                    this.clip.open(audioIn); // Carga los datos del stream al Clip.
                }
            } catch (UnsupportedAudioFileException e) {
                logger.logException("Formato de audio no soportado: " + resourcePath, e);
            } catch (IOException e) {
                logger.logException("Error de E/S al cargar sonido: " + resourcePath, e);
            } catch (LineUnavailableException e) {
                logger.logException("Línea de audio no disponible para: " + resourcePath, e);
            }
        }

        /**
         * Reproduce el sonido que ya está cargado en memoria.
         * Si el sonido ya se está reproduciendo, lo detiene y lo reinicia desde el principio.
         */
        public void play() {
            if (clip != null) {
                clip.stop(); // Detiene cualquier reproducción anterior.
                clip.setFramePosition(0); // Vuelve al inicio del audio.
                clip.start(); // Reproduce el sonido.
            }
        }
    }

    // ========================= SND/heart =========================
    // La carga en memoria ocurre aquí, una sola vez, cuando la clase Sounds es inicializada.
    public static final Sound HEARTBEAT = new Sound(DesktopPaths.SOUND_BEAT);
}