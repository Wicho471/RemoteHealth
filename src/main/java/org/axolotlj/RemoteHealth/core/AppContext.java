package org.axolotlj.RemoteHealth.core;

import org.axolotlj.RemoteHealth.app.SceneManager;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.DataProcessor;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketManager;
import org.axolotlj.RemoteHealth.service.websocket.WebSocketServerSimu;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Contenedor singleton del contexto de la aplicación.
 */
public class AppContext {

    private static AppContext instance;
    
    private final SceneManager sceneManager;
    private final WebSocketServerSimu simulator;
    private final LinkedBlockingQueue<String> messageQueue;
    private final LinkedBlockingQueue<StructureData> processedQueue;
    
    private final WebSocketManager wsManager;
    private DataLogger dataLogger;
    private DataProcessor dataProcessor;
    
    private AppContext(SceneManager sceneManager, DataLogger dataLogger) {
        this.messageQueue = new LinkedBlockingQueue<>();
        this.processedQueue = new LinkedBlockingQueue<>();
        this.simulator = new WebSocketServerSimu();
        this.sceneManager = sceneManager;
        this.dataLogger = dataLogger;
        this.wsManager = new WebSocketManager(messageQueue, dataLogger);
    }

    public static void initialize(SceneManager sceneManager, DataLogger dataLogger) {
        if (instance == null) {
            instance = new AppContext(sceneManager, dataLogger);
        }
    }

    public static AppContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AppContext no ha sido inicializado.");
        }
        return instance;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public WebSocketManager getWsManager() {
        return wsManager;
    }

    public WebSocketServerSimu getSimulator() {
        return simulator;
    }

    public LinkedBlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public LinkedBlockingQueue<StructureData> getProcessedQueue() {
        return processedQueue;
    }
    
    public DataLogger getDataLogger() {
		return dataLogger;
	}
    
    public DataProcessor getDataProcessor() {
		return dataProcessor;
	}
    
    public void setDataProcessor(DataProcessor dataProcessor) {
		this.dataProcessor = dataProcessor;
	}
    /**
     * Interfaz que define el acceso al contexto de la aplicación.
     */
    public interface ContextAware {
        void setAppContext(AppContext context);
    }
    
    public void finalize() {
		if (simulator != null) {
			try {
				simulator.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (dataLogger != null) {
			dataLogger.close();
		}
		if (dataProcessor != null) {
			dataProcessor.stop();
		}
		if (wsManager != null) {
			wsManager.disconnect();
		}
	}
}
