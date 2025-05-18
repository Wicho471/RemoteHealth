package org.axolotlj.RemoteHealth.sensor.handle;

import org.axolotlj.RemoteHealth.sensor.data.SensorValue;

/**
 * Clase utilitaria para parsear valores de sensores desde sus representaciones en cadena.
 * Maneja valores numéricos codificados en hexadecimal, así como valores especiales como "NR" o "ERR".
 */
public class SensorValueParser {

    /**
     * Parsea un valor hexadecimal de tipo short.
     *
     * @param value el valor en formato hexadecimal o texto especial.
     * @return instancia de SensorValue<Short> válida, no lista, errónea o malformada.
     */
    public static SensorValue<Short> parseShort(String value) {
        if (isNotReady(value)) return SensorValue.notReady();
        if (isError(value)) return SensorValue.error();

        try {
            return SensorValue.valid(Short.parseShort(value, 16));
        } catch (NumberFormatException e) {
            return SensorValue.malformed();
        }
    }

    /**
     * Parsea un valor hexadecimal de tipo int.
     *
     * @param value el valor en formato hexadecimal o texto especial.
     * @return instancia de SensorValue<Integer> válida, no lista, errónea o malformada.
     */
    public static SensorValue<Integer> parseInt(String value) {
        if (isNotReady(value)) return SensorValue.notReady();
        if (isError(value)) return SensorValue.error();

        try {
            return SensorValue.valid(Integer.parseInt(value, 16));
        } catch (NumberFormatException e) {
            return SensorValue.malformed();
        }
    }

    /**
     * Parsea un valor decimal flotante.
     *
     * @param value el valor en formato decimal o texto especial.
     * @return instancia de SensorValue<Float> válida, no lista, errónea o malformada.
     */
    public static SensorValue<Float> parseFloat(String value) {
        if (isNotReady(value)) return SensorValue.notReady();
        if (isError(value)) return SensorValue.error();

        try {
            return SensorValue.valid(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return SensorValue.malformed();
        }
    }

    private static boolean isNotReady(String value) {
        return "NR".equalsIgnoreCase(value);
    }

    private static boolean isError(String value) {
        return "ERR".equalsIgnoreCase(value);
    }
}
