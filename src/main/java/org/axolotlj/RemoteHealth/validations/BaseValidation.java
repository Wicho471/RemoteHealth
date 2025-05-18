package org.axolotlj.RemoteHealth.validations;

import java.io.File;

import org.axolotlj.RemoteHealth.app.ui.AlertUtil;

/**
 * Contiene validaciones básicas y reutilizables para diversos tipos de datos.
 */
public abstract class BaseValidation {

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
	 * Valida que el nombre proporcionado solo contenga letras (incluyendo acentuadas),
	 * números y espacios. No se permiten caracteres especiales ni símbolos.
	 *
	 * @param name El nombre a validar.
	 * @return true si el nombre es válido; false en caso contrario.
	 */
	public static boolean isValidName(String name) {
	    return name != null && name.matches("^[a-zA-ZÁÉÍÓÚáéíóúÑñ0-9 ]+$");
	}

	/**
	 * Valida que una cadena represente una dirección IPv4 válida o sea la palabra "localhost".
	 * Una IPv4 válida tiene el formato nnn.nnn.nnn.nnn con valores entre 0 y 255.
	 *
	 * @param ip Dirección IP a validar.
	 * @return true si es una IPv4 válida o "localhost"; false en caso contrario.
	 */
	public static boolean isValidIPv4(String ip) {
	    return ip != null && ip.matches("^(localhost|((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(?!$)|$)){4})$");
	}

	/**
	 * Valida que una cadena represente una dirección IPv6 válida o sea la palabra "localhost".
	 * Se considera válida una cadena que tenga 8 grupos de 4 dígitos hexadecimales separados por ':'.
	 *
	 * @param ip Dirección IP a validar.
	 * @return true si es una IPv6 válida o "localhost"; false en caso contrario.
	 */
	public static boolean isValidIPv6(String ip) {
	    return ip != null && (ip.equals("localhost") || ip.matches("^(?:[a-fA-F0-9]{1,4}:){7}[a-fA-F0-9]{1,4}$"));
	}
	
	/**
	 * Maneja el resultado de una validación mostrando una alerta si el valor es inválido.
	 * 
	 * Si el resultado no es válido, se muestra una alerta de advertencia con el tipo y mensaje de error
	 * proporcionado en el objeto {@link ValidationResult}.
	 *
	 * @param result El resultado de la validación a evaluar.
	 * @return true si la validación falló y se mostró una alerta; false si la validación fue exitosa.
	 */
	public static boolean handleValidation(ValidationResult result) {
	    if (!result.isValid()) {
	        AlertUtil.showWarningAlert("Campo invalido", result.getErrorType().getDesc(), result.getMessage());
	        return true;
	    }
	    return false;
	}

}
