package org.axolotlj.RemoteHealth.validations;

/**
 * Resultado de una validación con mensaje y tipo de error opcional.
 */
public class ValidationResult {

	private final boolean valid;
	private final String message;
	private final ValidationErrorType errorType;

	public ValidationResult(boolean valid, String message, ValidationErrorType errorType) {
		this.valid = valid;
		this.message = message;
		this.errorType = errorType;
	}

	public static ValidationResult ok() {
		return new ValidationResult(true, null, null);
	}

	public static ValidationResult error(String message, ValidationErrorType errorType) {
		return new ValidationResult(false, message, errorType);
	}

	public boolean isValid() {
		return valid;
	}

	public String getMessage() {
		return message;
	}

	public ValidationErrorType getErrorType() {
		return errorType;
	}
}