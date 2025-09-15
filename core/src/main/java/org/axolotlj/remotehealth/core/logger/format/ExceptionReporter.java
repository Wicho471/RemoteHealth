package org.axolotlj.remotehealth.core.logger.format;

/**
 * Utilidad para generar un reporte detallado de una excepción,
 * filtrando el stack trace a solo clases relevantes del proyecto.
 */
public class ExceptionReporter {

    private static final String PROJECT_PACKAGE_PREFIX = "org.axolotlj.remotehealth";

    /**
     * Genera un informe filtrado de una excepción.
     *
     * @param context Contexto donde ocurrió la excepción.
     * @param exception Instancia de la excepción capturada.
     * @return Texto del reporte de error.
     */
    public static String generateReport(String context, Exception exception) {
        return generateReport(context, (Throwable) exception);
    }

    /**
     * Genera un informe filtrado de un error o excepción.
     *
     * @param context Contexto donde ocurrió el error.
     * @param throwable Instancia del error o excepción.
     * @return Texto del reporte de error.
     */
    public static String generateReport(String context, Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        sb.append("[EXCEPTION REPORT]").append(System.lineSeparator());
        sb.append("Contexto      : ").append(context).append(System.lineSeparator());
        sb.append("Excepción     : ").append(throwable.getClass().getName()).append(System.lineSeparator());
        sb.append("Mensaje       : ").append(throwable.getMessage()).append(System.lineSeparator());

        sb.append("Ubicación     :").append(System.lineSeparator());
        appendFilteredStackTrace(sb, throwable);

        Throwable cause = throwable.getCause();
        if (cause != null) {
            sb.append("Causa raíz    : ")
              .append(cause.getClass().getName())
              .append(" - ").append(cause.getMessage())
              .append(System.lineSeparator());

            sb.append("Ubicación raíz:").append(System.lineSeparator());
            appendFilteredStackTrace(sb, cause);
        }

        return sb.toString();
    }

    private static void appendFilteredStackTrace(StringBuilder sb, Throwable throwable) {
        for (StackTraceElement element : throwable.getStackTrace()) {
            if (element.getClassName().startsWith(PROJECT_PACKAGE_PREFIX)) {
                sb.append("    at ")
                  .append(element.getClassName()).append(".")
                  .append(element.getMethodName()).append("(")
                  .append(element.getFileName()).append(":")
                  .append(element.getLineNumber()).append(")")
                  .append(System.lineSeparator());
            }
        }
    }
}
