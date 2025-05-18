package org.axolotlj.RemoteHealth.validations;

public enum ValidationErrorType {
    NULL_OR_EMPTY("El valor no puede estar vacío."),
    INVALID_FORMAT("El formato es inválido."),
    OUT_OF_RANGE("El valor está fuera del rango permitido."),
    FILE_NOT_FOUND("No se encontró el archivo especificado."),
    NOT_A_FILE("La ruta no corresponde a un archivo válido."),
    DIRECTORY_NOT_ALLOWED("No se permite seleccionar un directorio."),
    INVALID_PORT("El puerto especificado no es válido."),
    NEGATIVE_NUMBER("El valor no puede ser negativo."),
    ZERO_NOT_ALLOWED("El valor no puede ser cero."),
    UNKNOWN_ERROR("Se ha producido un error desconocido.");

    private final String mensaje;

    ValidationErrorType(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getDesc() {
        return mensaje;
    }
}

