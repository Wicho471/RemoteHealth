package org.axolotlj.remotehealth.mobile.attach;

import com.gluonhq.attach.display.DisplayService;
import com.gluonhq.attach.localnotifications.Notification;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;

import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Clase de utilidad que facilita el uso de {@link DisplayService}.
 * Permite consultar la resolución de pantalla, tipo de dispositivo y propiedades del notch.
 */
public class Display {

    private final DisplayService service;

    private Display(DisplayService service) {
        this.service = service;
    }

    /**
     * Crea una instancia de {@link Display} si el servicio está disponible.
     * @return Optional de {@link Display}.
     */
    public static Optional<Display> create() {
        Optional<DisplayService> optionalService = DisplayService.create();
        return optionalService.map(Display::new);
    }

    /**
     * Devuelve true si el dispositivo es considerado un teléfono.
     * @return true si es teléfono, false de lo contrario.
     */
    public boolean isPhone() {
        return service.isPhone();
    }

    /**
     * Devuelve true si el dispositivo es considerado una tablet.
     * @return true si es tablet, false de lo contrario.
     */
    public boolean isTablet() {
        return service.isTablet();
    }

    /**
     * Devuelve true si el dispositivo es considerado una computadora de escritorio o laptop.
     * @return true si es desktop, false de lo contrario.
     */
    public boolean isDesktop() {
        return service.isDesktop();
    }

    /**
     * Devuelve la resolución de la pantalla en píxeles.
     * @return Resolución de pantalla como {@link Dimension2D}.
     */
    public Dimension2D getScreenResolution() {
        return service.getScreenResolution();
    }

    /**
     * Devuelve las dimensiones predeterminadas de un dispositivo móvil en dp.
     * @return Dimensiones predeterminadas como {@link Dimension2D}.
     */
    public Dimension2D getDefaultDimensions() {
        return service.getDefaultDimensions();
    }

    /**
     * Devuelve la escala lógica de la pantalla.
     * @return escala de pantalla.
     */
    public float getScreenScale() {
        return service.getScreenScale();
    }

    /**
     * Devuelve true si la pantalla es redonda.
     * @return true si la pantalla es redonda, false en caso contrario.
     */
    public boolean isScreenRound() {
        return service.isScreenRound();
    }

    /**
     * Devuelve true si el dispositivo tiene notch.
     * @return true si hay notch, false de lo contrario.
     */
    public boolean hasNotch() {
        return service.hasNotch();
    }

    /**
     * Devuelve la posición actual del notch.
     * @return {@link DisplayService.Notch} indicando la posición del notch.
     */
    public DisplayService.Notch getNotchPosition() {
        return service.notchProperty().get();
    }

    /**
     * Devuelve la propiedad de solo lectura del notch.
     * @return propiedad del notch.
     */
    public ReadOnlyObjectProperty<DisplayService.Notch> notchProperty() {
        return service.notchProperty();
    }

    /**
     * Registra un listener que se ejecuta cuando cambia la posición del notch.
     * @param listener el listener que recibe el nuevo valor.
     */
    public void onNotchChange(ChangeListener<? super DisplayService.Notch> listener) {
        service.notchProperty().addListener(listener);
    }
    
    private void example() {
        Display.create().ifPresentOrElse(monitor -> {
            System.out.println("Es teléfono? " + monitor.isPhone());
            System.out.println("Es tablet? " + monitor.isTablet());
            System.out.println("Resolución de pantalla: " + monitor.getScreenResolution().getWidth() 
                               + "x" + monitor.getScreenResolution().getHeight());

            monitor.onNotchChange((obs, oldVal, newVal) ->
                    System.out.println("Posición del notch cambiada a: " + newVal));
        }, () -> System.out.println("No se pudo inicializar el monitor de pantalla."));
	}
}
