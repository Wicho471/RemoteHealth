package org.axolotlj.remotehealth.core.service;

import java.util.concurrent.BlockingQueue;

import org.axolotlj.remotehealth.core.cmd.CommandCommunicator;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.core.logger.api.DataLogger;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;
import org.axolotlj.remotehealth.core.sensor.handle.DataParser;
import org.axolotlj.remotehealth.core.service.datawriter.CsvDataWriter;
import org.axolotlj.remotehealth.core.service.datawriter.FileCsvDataWriter;

public class DataProcessor {
	
	private final DataLogger dataLogger = Log.get();

	private volatile boolean active;

	private BlockingQueue<String> messageQueue;
	private BlockingQueue<DataPoint> processedQueue;
	private CommandCommunicator communicator;
	private Thread processorThread;
	
	private CsvDataWriter csvDataWriter;
	private volatile boolean isCsvEnabled;
	
	private static final String INVALID_DATA = ",NR,NR,NR,NR,NR";

	public DataProcessor(BlockingQueue<String> messageQueue, BlockingQueue<DataPoint> processedQueue, CommandCommunicator communicator) {
		this.active = true;
		this.messageQueue = messageQueue;
		this.processedQueue = processedQueue;
		this.communicator = communicator;
	}

	public void startProcessing() {

		processorThread = new Thread(() -> {
			while (active) {
				try {
					String data = messageQueue.take();
					//Puede que haya mas de una linea de mensajes
					if ("STOP".equals(data))
						break;
					if (data.contains("{")) {
						dataLogger.logDebug("Comando detectado -> "+ data);
						communicator.dispatch(data);
						continue;
					}
					if (data.contains(INVALID_DATA)) continue;
					DataPoint processedData = DataParser.process(data);
					if (processedData == null)
						continue;
					if (isCsvEnabled && csvDataWriter != null) {
						csvDataWriter.writeData(data);
					}
					processedQueue.put(processedData);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}, "DataProcessorThread");
		processorThread.start();
	}

	public boolean recordData(ConnectionData connectionData, String patientName) {
		if (isCsvEnabled || csvDataWriter != null)
			return false;
		try {
			csvDataWriter = new FileCsvDataWriter(connectionData, patientName);
			isCsvEnabled = true;
			return true;
		} catch (Exception e) {
			System.err.println("Error en recordData: " + e.getMessage());
			return false;
		}
	}

	public boolean stopRecordingData() {
		if (!isCsvEnabled)
			return false;
		if (csvDataWriter == null)
			return false;
		csvDataWriter.close();
		csvDataWriter = null;
		this.isCsvEnabled = false;
		return true;

	}

	public void stop() {
		stopRecordingData();
		this.active = false;
		if (messageQueue != null) {
			messageQueue.offer("STOP"); 
		}
		if (processorThread != null && processorThread.isAlive()) {
			processorThread.interrupt(); 
			try {
				processorThread.join(2000); 
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			processorThread = null;
		}
	}

	public CommandCommunicator getCommunicator() {
		return communicator;
	}
}
