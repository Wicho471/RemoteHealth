package org.axolotlj.RemoteHealth.service.serial;

import com.fazecast.jSerialComm.SerialPort;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Servicio que monitorea puertos seriales en búsqueda de un ESP32 conectado por
 * USB.
 */
public class SerialMonitorService {

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private final Set<String> knownPorts = ConcurrentHashMap.newKeySet();

	private final ComboBox<SerialPort> deviceSelector;
	private final Button flashBtn;
	private final ImageView imgEsp32;
	private final TextArea outputSerialTextArea;
	private final SerialOutputBuffer serialBuffer;

	private SerialPort connectedPort;

	public SerialMonitorService(ComboBox<SerialPort> deviceSelector, Button flashBtn, ImageView imgEsp32,
			TextArea outputSerialTextArea) {
		this.deviceSelector = deviceSelector;
		this.flashBtn = flashBtn;
		this.imgEsp32 = imgEsp32;
		this.outputSerialTextArea = outputSerialTextArea;
	    this.serialBuffer = new SerialOutputBuffer(outputSerialTextArea);
	    serialBuffer.start();
	}

	public void startMonitoring() {
		executor.scheduleAtFixedRate(this::scanPorts, 0, 1, TimeUnit.SECONDS);
	}

	private void scanPorts() {
		Set<String> currentPorts = new HashSet<>();
		SerialPort[] ports = SerialPort.getCommPorts();

		for (SerialPort port : ports) {
			currentPorts.add(port.getSystemPortName());

			if (!knownPorts.contains(port.getSystemPortName())) {
				knownPorts.add(port.getSystemPortName());

				Platform.runLater(() -> {
					if (!deviceSelector.getItems().contains(port)) {
						deviceSelector.getItems().add(port);
					}

					// Selección automática si no hay nada seleccionado
					if (deviceSelector.getSelectionModel().isEmpty()) {
						deviceSelector.getSelectionModel().select(port);
						connectToPort(port); // abrimos y leemos desde este
					}
				});
			}
		}

		Platform.runLater(() -> {
			// Eliminar puertos desconectados
			deviceSelector.getItems().removeIf(port -> !currentPorts.contains(port.getSystemPortName()));

			// Actualizar knownPorts
			knownPorts.retainAll(currentPorts);

			// Si el puerto seleccionado ya no existe, seleccionar otro
			SerialPort selected = deviceSelector.getSelectionModel().getSelectedItem();
			if (selected != null && !currentPorts.contains(selected.getSystemPortName())) {
				System.out.println("Puerto desconectado: " + selected.getSystemPortName());

				if (connectedPort != null && connectedPort.isOpen()) {
					connectedPort.closePort();
				}

				if (!deviceSelector.getItems().isEmpty()) {
					SerialPort fallbackPort = deviceSelector.getItems().get(0);
					deviceSelector.getSelectionModel().select(fallbackPort);
					connectToPort(fallbackPort);
				} else {
					deviceSelector.getSelectionModel().clearSelection();
					flashBtn.setDisable(true);
					imgEsp32.setOpacity(0.25);
				}
			}
		});
	}

	private void handleNoneDevice() {
		Platform.runLater(() -> {
			deviceSelector.getItems().clear();
			deviceSelector.getItems().add(null); // O puedes usar un SerialPort simulado
			deviceSelector.setPromptText("No hay dispositivos detectados");
			deviceSelector.setDisable(true);

			flashBtn.setDisable(true);
			imgEsp32.setOpacity(0.25);
		});
	}

	private void handleDeviceDetection(SerialPort port) {
		Platform.runLater(() -> {
			if (deviceSelector.isDisabled()) {
				deviceSelector.setDisable(false);
				deviceSelector.getItems().clear();
			}

			if (!deviceSelector.getItems().contains(port)) {
				deviceSelector.getItems().add(port);
			}

			deviceSelector.getSelectionModel().select(port);
			flashBtn.setDisable(false);
			imgEsp32.setOpacity(1.0);

			if (!port.isOpen()) {
				if (port.openPort()) {
					port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

					Thread readerThread = new Thread(() -> {
						try (var in = port.getInputStream()) {
							byte[] buffer = new byte[1024];
							int len;
							while ((len = in.read(buffer)) > -1) {
								String data = new String(buffer, 0, len);
								serialBuffer.appendData(data);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					});

					readerThread.setDaemon(true);
					readerThread.start();
				} else {
					System.err.println("No se pudo abrir el puerto: " + port.getSystemPortName());
				}
			}
		});
	}

	public void connectToPort(SerialPort port) {
	    if (connectedPort != null && connectedPort.getSystemPortName().equals(port.getSystemPortName()) && connectedPort.isOpen()) {
	        return; // Ya conectado
	    }

	    if (connectedPort != null && connectedPort.isOpen()) {
	        connectedPort.closePort();
	    }

	    if (port.openPort()) {
	        connectedPort = port;
	        port.setComPortParameters(115200, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
	        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 10000, 0);

	        Thread readerThread = new Thread(() -> {
	            try (var in = port.getInputStream()) {
	                byte[] buffer = new byte[1024];
	                int len;
	                while ((len = in.read(buffer)) > -1) {
	                    String data = new String(buffer, 0, len);
	                    serialBuffer.appendData(data);
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        });
	        readerThread.setDaemon(true);
	        readerThread.start();
	    } else {
	        System.err.println("No se pudo abrir el puerto: " + port.getSystemPortName());
	    }
	}


	/**
	 * Finaliza el monitoreo de puertos seriales y cierra la conexión activa si
	 * existe.
	 */
	public void stopMonitoring() {
		// Detener el hilo de escaneo periódico
		executor.shutdownNow();

		// Cerrar puerto conectado si está abierto
		if (connectedPort != null && connectedPort.isOpen()) {
			try {
				connectedPort.closePort();
				System.out.println("Puerto cerrado: " + connectedPort.getSystemPortName());
			} catch (Exception e) {
				System.err.println("Error cerrando el puerto: " + e.getMessage());
			}
		}

		// Limpiar el ComboBox y estado visual (opcional, si quieres resetear todo)
		Platform.runLater(() -> {
			deviceSelector.getItems().clear();
			flashBtn.setDisable(true);
			imgEsp32.setOpacity(0.25);
		});
		serialBuffer.stop();
		knownPorts.clear();
		connectedPort = null;
	}

}
