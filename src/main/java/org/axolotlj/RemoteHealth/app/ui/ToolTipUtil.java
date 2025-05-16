package org.axolotlj.RemoteHealth.app.ui;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

/**
 * Utilidad para agregar tooltips a elementos interactivos de la interfaz.
 */
public class ToolTipUtil {

    private static final int TOOLTIP_SHOW_DELAY_MS = 300;
    private static final int TOOLTIP_HIDE_DELAY_MS = 100;

    /**
     * Asocia un tooltip con el texto proporcionado a un control.
     *
     * @param control Control al que se agregará el tooltip.
     * @param text Texto del tooltip.
     */
    public static void applyTooltip(Control control, String text) {
        Tooltip tooltip = createTooltip(text);
        Tooltip.install(control, tooltip);
        control.setTooltip(tooltip);
    }
    
    
    public static void applyTooltip(Node node, String text) {
        Tooltip tooltip = createTooltip(text);
        Tooltip.install(node, tooltip);
        if (node instanceof Control control) {
            control.setTooltip(tooltip); 
        }
    }

    /**
     * Crea una instancia de Tooltip con configuración predeterminada.
     *
     * @param text Texto del tooltip.
     * @return Tooltip configurado.
     */
    public static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(TOOLTIP_SHOW_DELAY_MS));
        tooltip.setHideDelay(Duration.millis(TOOLTIP_HIDE_DELAY_MS));
        return tooltip;
    }


}
