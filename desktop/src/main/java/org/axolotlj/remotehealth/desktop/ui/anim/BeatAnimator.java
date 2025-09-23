package org.axolotlj.remotehealth.desktop.ui.anim;

import static org.axolotlj.remotehealth.desktop.javafx.current.FxThreadUtils.runOnUIThread;

import org.axolotlj.remotehealth.desktop.ui.assets.Sounds.Sound;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Utilidad para animar el latido y reproducir sonidos.
 */
public class BeatAnimator {
    private final ImageView target;
    private final Image beatImage;
    private final Image defaultImage;
    private final int durationMs;
    private final Sound beep;

    /**
     * @param target ImageView donde se hará la animación
     * @param beatImage Imagen que se mostrará al hacer el "latido"
     * @param defaultImage Imagen a la que se regresa después
     * @param durationMs Duración en milisegundos del efecto
     */
    public BeatAnimator(ImageView target, Image beatImage, Image defaultImage, int durationMs, Sound beep) {
        this.target = target;
        this.beatImage = beatImage;
        this.defaultImage = defaultImage;
        this.durationMs = durationMs;
        this.beep = beep;
    }

    /**
     * Ejecuta el efecto de latido (cambia imagen y reproduce sonido).
     */
    public void playBeat() {
    	runOnUIThread(() -> {
    		beep.play();
            target.setImage(beatImage);

            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(durationMs), e -> target.setImage(defaultImage))
            );
            timeline.setCycleCount(1);
            timeline.play();
        });
    }
}
