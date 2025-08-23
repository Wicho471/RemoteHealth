package org.axolotlj.remotehealth.mobile.service.websocket;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.service.websocket.IWebSocketServerSimulator;
import org.axolotlj.remotehealth.core.simulation.GenerationMode;

import javafx.beans.property.BooleanProperty;

public class WebSocketServerSimulator implements IWebSocketServerSimulator {

	private DataLogger dataLogger = Log.get();

	@Override
	public void start() {
		dataLogger.logWarn("Metodos no implementados");

	}

	@Override
	public void stop() {
		dataLogger.logWarn("Metodos no implementados");

	}

	@Override
	public void restart() {
		dataLogger.logWarn("Metodos no implementados");

	}

	@Override
	public boolean isActive() {
		dataLogger.logWarn("Metodos no implementados");

		return false;
	}

	@Override
	public BooleanProperty activeProperty() {
		dataLogger.logWarn("Metodos no implementados");

		return null;
	}

	@Override
	public void setGenerationMode(GenerationMode generationMode) {
		dataLogger.logWarn("Metodos no implementados");

	}

	@Override
	public ConnectionData getConnection() {
		dataLogger.logWarn("Metodos no implementados");

		return null;
	}

}
