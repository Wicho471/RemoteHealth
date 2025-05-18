package org.axolotlj.RemoteHealth.validations;


/**
 * Validaciones generales.
 */
public class GeneralValidation extends BaseValidation {

	public static ValidationResult validateName(String name) {
		if (!isNotNullOrEmpty(name)) {
			return ValidationResult.error("El nombre no puede estar vacío.", ValidationErrorType.NULL_OR_EMPTY);
		}
		if (!isValidName(name)) {
			return ValidationResult.error("El nombre contiene caracteres no permitidos.", ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateIPv4(String ip) {
		if (!isNotNullOrEmpty(ip)) {
			return ValidationResult.error("La dirección IP no puede estar vacía.", ValidationErrorType.NULL_OR_EMPTY);
		}
		if (!isValidIPv4(ip)) {
			return ValidationResult.error("La dirección IPv4 no es válida. Debe ser localhost o una IPv4 válida.", ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}

	public static ValidationResult validateIPv6(String ip) {
		if (!isNotNullOrEmpty(ip)) {
			return ValidationResult.error("La dirección IP no puede estar vacía.", ValidationErrorType.NULL_OR_EMPTY);
		}
		if (!isValidIPv6(ip)) {
			return ValidationResult.error("La dirección IPv6 no es válida. Debe ser localhost o una IPv6 válida.", ValidationErrorType.INVALID_FORMAT);
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
				return ValidationResult.error("El puerto debe ser mayor que cero.", ValidationErrorType.ZERO_NOT_ALLOWED);
			}
			if (value < 1 || value > 65535) {
				return ValidationResult.error("El puerto debe estar entre 1 y 65535.", ValidationErrorType.OUT_OF_RANGE);
			}
		} catch (NumberFormatException e) {
			return ValidationResult.error("El puerto debe ser un número válido.", ValidationErrorType.INVALID_FORMAT);
		}
		return ValidationResult.ok();
	}
}
