package org.axolotlj.remotehealth.core.cmd;

import java.util.function.Consumer;

public interface CommandCommunicator {
	
	/**
	 * 
	 * @param dataRecived
	 */
	void onDataReceived(Consumer<String> dataRecived); 
	
    /**
     * Envía un comando al dispositivo conectado.
     *
     * @param command comando a enviar
     * @return respuesta del dispositivo
     */
    void sendCommand(String command);
    
    void dispatch(String json);
}
