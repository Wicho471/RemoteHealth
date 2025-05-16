package org.axolotlj.RemoteHealth.simulation;

/**
 * Generador de cargas útiles de datos simulados.
 */
public interface DataPayloadGenerator {

    /**
     * Genera una nueva carga útil simulada.
     * 
     * @return Cadena representando los datos simulados.
     */
    String generatePayload();
}
