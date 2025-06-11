package org.axolotlj.RemoteHealth.validations;

/**
 * Validaciones específicas para configuraciones de filtros digitales.
 */
public class FilterValidation extends BaseValidation {

	public static ValidationResult validateFrequency(String value) {
		if(value == null) return ValidationResult.error("Error interno", ValidationErrorType.NULL_OR_EMPTY);
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
