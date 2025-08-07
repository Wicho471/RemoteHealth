package org.axolotlj.remotehealth.core.logger;

/**
 * Utilidad para aplicar colores ANSI a texto para consola.
 */
public class AnsiConsoleColor {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String CYAN = "\u001B[36m";

    public static String colorForLevel(LogLevel level, String message) {
        switch (level) {
            case ERROR:
            case FATAL:
                return RED + message + RESET;
            case WARN:
                return YELLOW + message + RESET;
            case INFO:
                return GREEN + message + RESET;
            case DEBUG:
                return CYAN + message + RESET;
            default:
                return message;
        }
    }
}
