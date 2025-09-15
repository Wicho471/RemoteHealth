package org.axolotlj.remotehealth.desktop.service.bluetooth.so;

import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.service.bluetooth.BluetoothDeviceInfo;
import org.axolotlj.remotehealth.core.service.bluetooth.BluetoothService;

import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementación de BluetoothService para Windows y macOS usando BlueCove.
 */
public class WindowsMacBluetoothService implements BluetoothService {

	private final DataLogger dataLogger = Log.get();
	
    private StreamConnection connection;
    private OutputStream outputStream;
    private Thread readerThread;
    private Consumer<String> dataListener;
    private volatile boolean running;

    @Override
    public List<BluetoothDeviceInfo> getDevices() {
        List<BluetoothDeviceInfo> devices = new ArrayList<>();
        try {
            LocalDevice localDevice = LocalDevice.getLocalDevice();
            DiscoveryAgent agent = localDevice.getDiscoveryAgent();

            final Object inquiryCompletedEvent = new Object();

            agent.startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
                @Override
                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    try {
                        String name = btDevice.getFriendlyName(false);
                        String address = btDevice.getBluetoothAddress();
                        devices.add(new BluetoothDeviceInfo(name, address));
                    } catch (IOException ignored) {
                        devices.add(new BluetoothDeviceInfo("Unknown", btDevice.getBluetoothAddress()));
                    }
                }

                @Override
                public void inquiryCompleted(int discType) {
                    synchronized (inquiryCompletedEvent) {
                        inquiryCompletedEvent.notifyAll();
                    }
                }

                @Override
                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}

                @Override
                public void serviceSearchCompleted(int transID, int respCode) {}
            });

            synchronized (inquiryCompletedEvent) {
                inquiryCompletedEvent.wait();
            }

        } catch (Exception e) {
            System.out.println("Error al escanear dispositivos: " + e.getMessage());
        }
        return devices;
    }

    @Override
    public void establishConnection(BluetoothDeviceInfo device, Runnable onSuccess, Runnable onFailure) {
        try {
            String url = "btspp://" + device.getAddress() + ":1;authenticate=false;encrypt=false;master=false";
            connection = (StreamConnection) Connector.open(url);
            outputStream = connection.openOutputStream();
            running = true;

            // Lanzar hilo lector en segundo plano
            readerThread = new Thread(this::readLoop);
            readerThread.setDaemon(true);
            readerThread.start();

            if (onSuccess != null) onSuccess.run();
        } catch (IOException e) {
            System.out.println("Error al conectar con " + device + ": " + e.getMessage());
            if (onFailure != null) onFailure.run();
        }
    }

    @Override
    public void sendCommand(String command) {
//        try {
//            if (outputStream != null) {
//                outputStream.write(command.getBytes());
//                outputStream.flush();
//                return "Comando enviado: " + command;
//            } else {
//                return "No hay conexión activa.";
//            }
//        } catch (IOException e) {
//            return "Error al enviar comando: " + e.getMessage();
//        }
    }

    @Override
    public void onDataReceived(Consumer<String> dataReceived) {
        this.dataListener = dataReceived;
    }

    @Override
    public void closeConnection() {
        running = false;
        try {
            if (outputStream != null) outputStream.close();
            if (connection != null) connection.close();
        } catch (IOException e) {
            System.out.println("Error al cerrar conexión: " + e.getMessage());
        }
    }

    private void readLoop() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.openInputStream()))) {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (dataListener != null) {
                    dataListener.accept(line);
                }
            }
        } catch (IOException e) {
            if (dataListener != null) {
                dataListener.accept("Error de lectura: " + e.getMessage());
            }
        }
    }

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void onSignalLost(Runnable handleDisconection) {
		dataLogger.logWarn("No se a terminado el metodo, ignorando llamada a funcion");		
	}

	@Override
	public void dispatch(String json) {
		dataLogger.logWarn("No se a terminado el metodo, ignorando ->"+json);
		
	}
}
