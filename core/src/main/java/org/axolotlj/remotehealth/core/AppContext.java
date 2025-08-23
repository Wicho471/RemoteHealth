package org.axolotlj.remotehealth.core;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.axolotlj.remotehealth.core.config.files.GeneralConfig;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;
import org.axolotlj.remotehealth.core.service.DataProcessor;
import org.axolotlj.remotehealth.core.service.websocket.IWebSocketManager;
import org.axolotlj.remotehealth.core.service.websocket.IWebSocketServerSimulator;

/**
 * Contenedor singleton del contexto de la aplicación.
 */
public class AppContext {

	private static AppContext instance;

	private final LinkedBlockingQueue<String> messageQueue;
	private final LinkedBlockingQueue<DataPoint> processedQueue;

	private final IWebSocketServerSimulator simulator;
	private final IWebSocketManager wsManager;
	
	private DataProcessor dataProcessor;
	private GeneralConfig generalConfig;

	private AppContext(IWebSocketServerSimulator simulator, Function<LinkedBlockingQueue<String>, IWebSocketManager> wsFactory) {
		this.messageQueue = new LinkedBlockingQueue<>();
		this.processedQueue = new LinkedBlockingQueue<>();
		this.simulator = simulator;
		this.wsManager = wsFactory.apply(this.messageQueue); 
		this.generalConfig = new GeneralConfig();
	}

	public static void initialize(IWebSocketServerSimulator simulator,
			Function<LinkedBlockingQueue<String>, IWebSocketManager> wsFactory) {
		Log.get().logDebug("Inicializando AppContext");
		if (instance == null) {
			instance = new AppContext(simulator,wsFactory);
		} else {
			Log.get().logWarn("Ya existe una instancia de AppContext");
		}
	}

	public static AppContext getInstance() {
		if (instance == null) {
			throw new IllegalStateException("AppContext no ha sido inicializado.");
		}
		return instance;
	}

	public IWebSocketManager getWsManager() {
		return wsManager;
	}

	public IWebSocketServerSimulator getSimulator() {
		return simulator;
	}

	public LinkedBlockingQueue<String> getMessageQueue() {
		return messageQueue;
	}

	public LinkedBlockingQueue<DataPoint> getProcessedQueue() {
		return processedQueue;
	}

	public DataProcessor getDataProcessor() {
		return dataProcessor;
	}

	public void setDataProcessor(DataProcessor dataProcessor) {
		this.dataProcessor = dataProcessor;
	}

	public GeneralConfig getGeneralConfig() {
		return generalConfig;
	}

	/**
	 * Interfaz que define el acceso al contexto de la aplicación.
	 */
	public interface ContextAware {
		void setAppContext(AppContext context);
	}

	public interface DisposableController {
		void dispose();
	}

	public void finalize() {
		if (simulator != null) {
			try {
				simulator.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (dataProcessor != null) {
			dataProcessor.stop();
		}
		if (wsManager != null) {
			wsManager.disconnect();
		}
		Log.get().close();
	}

	public static void main(String[] args) {

	}
}
