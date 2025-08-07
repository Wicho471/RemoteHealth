package org.axolotlj.remotehealth.core.logger;

/**
 * Niveles de severidad para los mensajes de log.
 */
public enum LogLevel {
	TRACE("TRACE"),
	DEBUG("DEBUG"),
	INFO("INFO"),
	WARN("WARN"),
	ERROR("ERROR"),
	FATAL("FATAL");

	private final String label;

	LogLevel(String label) {
		this.label = label;
	}

	/**
	 * Devuelve el nombre asociado al nivel de log.
	 *
	 * @return nombre del nivel de log en formato texto.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Obtiene el nivel de log correspondiente al nombre especificado.
	 *
	 * @param label nombre del nivel
	 * @return nivel de log correspondiente
	 * @throws IllegalArgumentException si no existe un nivel con ese nombre
	 */
	public static LogLevel fromLabel(String label) {
		for (LogLevel level : LogLevel.values()) {
			if (level.label.equalsIgnoreCase(label)) {
				return level;
			}
		}
		throw new IllegalArgumentException("LogLevel desconocido: " + label);
	}
}
