package org.axolotlj.RemoteHealth.validations;

import java.io.File;

public class ValidationUtil {

	/**
	 * Valida que el número de puerto esté en el rango permitido (1–65535).
	 *
	 * @param port Número de puerto
	 * @return true si es válido
	 */
	public static boolean validatePort(int port) {
		return port >= 1 && port <= 65535;
	}

	/**
	 * Verifica que una cadena no esté vacía ni sea nula.
	 *
	 * @param value Cadena a validar
	 * @return true si la cadena contiene texto
	 */
	public static boolean isNotNullOrEmpty(String value) {
		return value != null && !value.trim().isEmpty();
	}

	/**
	 * Verifica que un archivo exista y no sea un directorio.
	 *
	 * @param file Archivo a validar
	 * @return true si el archivo existe
	 */
	public static boolean isFileValid(File file) {
		return file != null && file.exists() && file.isFile();
	}

	/**
	 * Valida si una cadena puede convertirse en un entero positivo.
	 *
	 * @param value Cadena a evaluar
	 * @return true si es entero positivo
	 */
	public static boolean isPositiveInteger(String value) {
		try {
			return Integer.parseInt(value) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Valida si una cadena puede convertirse en un número decimal positivo.
	 *
	 * @param value Cadena a evaluar
	 * @return true si es decimal positivo
	 */
	public static boolean isPositiveDecimal(String value) {
		try {
			return Double.parseDouble(value) > 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Verifica que un valor esté dentro de un rango inclusivo.
	 *
	 * @param value Valor a verificar
	 * @param min   Mínimo permitido
	 * @param max   Máximo permitido
	 * @return true si está dentro del rango
	 */
	public static boolean isWithinRange(int value, int min, int max) {
		return value >= min && value <= max;
	}

	public static boolean isValidName(String name) {
		return name != null && name.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñ0-9 ]+$");
	}

	public static boolean isValidIPv4(String ip) {
		return ip != null && ip.matches("^(localhost|((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(?!$)|$)){4})$");
	}

	public static boolean isValidIPv6(String ip) {
		return ip != null && (ip.equals("localhost") || ip.matches("^(?:[a-fA-F0-9]{1,4}:){7}[a-fA-F0-9]{1,4}$"));
	}

	public static ValidationResult validateName(String name) {
		if (!isNotNullOrEmpty(name)) {
			return ValidationResult.error("El nombre no puede estar vacío.", ValidationErrorType.NULL_OR_EMPTY);
		}
		if (!isValidName(name)) {
			return ValidationResult.error("El nombre contiene caracteres no permitidos.",
					ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateIPv4(String ip) {
		if (!isNotNullOrEmpty(ip)) {
			return ValidationResult.error("La dirección IP no puede estar vacía.", ValidationErrorType.NULL_OR_EMPTY);
		}
		if (!isValidIPv4(ip)) {
			return ValidationResult.error("La dirección IPv4 no es válida. Debe ser localhost o una IPv4 válida.",
					ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateIPv6(String ip) {
		if (!isNotNullOrEmpty(ip)) {
			return ValidationResult.error("La dirección IP no puede estar vacía.", ValidationErrorType.NULL_OR_EMPTY);
		}
		if (!isValidIPv6(ip)) {
			return ValidationResult.error("La dirección IPv6 no es válida. Debe ser localhost o una IPv6 válida.",
					ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validatePort(String port) {
		if (!isNotNullOrEmpty(port)) {
			return ValidationResult.error("El puerto no puede estar vacío.", ValidationErrorType.NULL_OR_EMPTY);
		}
		try {
			int value = Integer.parseInt(port);
			if (value <= 0) {
				return ValidationResult.error("El puerto debe ser mayor que cero.",
						ValidationErrorType.ZERO_NOT_ALLOWED);
			}
			if (value < 1 || value > 65535) {
				return ValidationResult.error("El puerto debe estar entre 1 y 65535.",
						ValidationErrorType.OUT_OF_RANGE);
			}
		} catch (NumberFormatException e) {
			return ValidationResult.error("El puerto debe ser un número válido.", ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}
	
	public static ValidationResult validateFrequency(String value) {
		if (!isPositiveDecimal(value)) {
			return ValidationResult.error("La frecuencia debe ser un número decimal positivo", ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateOrder(String value) {
		if (!isPositiveInteger(value)) {
			return ValidationResult.error("El orden debe ser un entero positivo", ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateInferiorLessThanSuperior(String inferior, String superior) {
		try {
			double inf = Double.parseDouble(inferior);
			double sup = Double.parseDouble(superior);
			if (inf >= sup) {
				return ValidationResult.error("El valor inferior debe ser menor al superior", ValidationErrorType.OUT_OF_RANGE);
			}
			return ValidationResult.ok();
		} catch (NumberFormatException e) {
			return ValidationResult.error("Valores inválidos para comparar frecuencias", ValidationErrorType.INVALID_FORMAT);
		}
	}

	public static ValidationResult validateWaveletLevel(String value) {
		if (!isPositiveInteger(value)) {
			return ValidationResult.error("El nivel debe ser un entero positivo", ValidationErrorType.INVALID_FORMAT);
		}
		int level = Integer.parseInt(value);
		if (level < 1) {
			return ValidationResult.error("El nivel mínimo permitido es 1", ValidationErrorType.OUT_OF_RANGE);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateThreshold(String value) {
		try {
			double val = Double.parseDouble(value);
			if (val < 0) {
				return ValidationResult.error("El umbral debe ser mayor o igual a 0", ValidationErrorType.OUT_OF_RANGE);
			}
			return ValidationResult.ok();
		} catch (NumberFormatException e) {
			return ValidationResult.error("El umbral debe ser un número decimal", ValidationErrorType.INVALID_FORMAT);
		}
	}

	public static ValidationResult validateWindowSize(String value) {
		if (!isPositiveInteger(value)) {
			return ValidationResult.error("La ventana debe ser un número entero positivo", ValidationErrorType.INVALID_FORMAT);
		}
		int v = Integer.parseInt(value);
		if (v <= 2 || v % 2 == 0) {
			return ValidationResult.error("La ventana debe ser un número impar mayor a 2", ValidationErrorType.OUT_OF_RANGE);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validatePolynomial(String polyValue, String windowValue) {
		if (!isPositiveInteger(polyValue)) {
			return ValidationResult.error("El polígono debe ser un entero positivo", ValidationErrorType.INVALID_FORMAT);
		}
		if (!isPositiveInteger(windowValue)) {
			return ValidationResult.error("La ventana debe ser un entero positivo", ValidationErrorType.INVALID_FORMAT);
		}
		int poly = Integer.parseInt(polyValue);
		int window = Integer.parseInt(windowValue);
		if (poly >= window) {
			return ValidationResult.error("El polígono debe ser menor que la ventana", ValidationErrorType.OUT_OF_RANGE);
		}
		return ValidationResult.ok();
	}
}
