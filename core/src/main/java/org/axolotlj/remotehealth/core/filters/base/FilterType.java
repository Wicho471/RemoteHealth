package org.axolotlj.remotehealth.core.filters.base;

/**
 * Tipos de filtros digitales disponibles.
 */
public enum FilterType {
    LOWPASS("Pasa bajas"),
    HIGHPASS("Pasa altas"),
    BANDPASS("Pasa bandas"),
    BANDSTOP("Rechaza bandas");

    private final String displayName;

    FilterType(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Obtiene un nombre legible para mostrar en UI u otras representaciones.
     *
     * @return Nombre descriptivo del tipo de filtro.
     */
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
