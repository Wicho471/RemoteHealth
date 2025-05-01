package org.axolotlj.RemoteHealth.service;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.axolotlj.RemoteHealth.model.ConnectionData;
import org.axolotlj.RemoteHealth.model.StructureData;
import org.axolotlj.RemoteHealth.service.datawriter.CsvDataWriter;
import org.axolotlj.RemoteHealth.service.datawriter.FileCsvDataWriter;
import org.axolotlj.RemoteHealth.util.DataHandler;
import org.axolotlj.RemoteHealth.util.cmd.CommandHandler;
import org.axolotlj.RemoteHealth.util.cmd.CommandHandler.CommandType;
import org.axolotlj.RemoteHealth.util.cmd.CommandResponseListener;

public class DataProcessor {
	
	private volatile boolean active;
	
    private BlockingQueue<String> messageQueue;
    private BlockingQueue<StructureData> processedQueue;
    
    private final List<CommandResponseListener> listeners = new CopyOnWriteArrayList<>();
    
    private CsvDataWriter csvDataWriter;
    private volatile boolean isCsvEnabled;
    
    public void addCommandResponseListener(CommandResponseListener listener) {
        listeners.add(listener);
    }

    public void removeCommandResponseListener(CommandResponseListener listener) {
        listeners.remove(listener);
    }

    private void notifyCommandResponse(String command) {
    	CommandType commandType = CommandType.fromResponse(command);
    	if (commandType==CommandType.UNKNOWN) return;
    	String content = CommandHandler.extractResponseContent(command);
    	if (content.isEmpty() || content.isBlank()) return;
    	ImmutablePair<CommandType, String> cmd = new ImmutablePair<CommandType, String>(commandType, content);
        for (CommandResponseListener listener : listeners) {
            listener.onCommandResponse(cmd);
        }
    }

    public DataProcessor(BlockingQueue<String> messageQueue, BlockingQueue<StructureData> processedQueue) {
        this.active = true;
    	this.messageQueue = messageQueue;
        this.processedQueue = processedQueue;
    }

    public void startProcessing() {

        new Thread(() -> {
            while (active) {
                try {
                	String data = messageQueue.take();
                	if ("STOP".equals(data)) break;
                	if(data.startsWith(CommandHandler.PREFIX)) {
                		System.out.println("Se detecto un comando de respuesta");
                		notifyCommandResponse(data);
                		continue;
                	}
                	if(data.endsWith(",NR,NR,NR,NR,NR") || data.contains(",NR,NR,NR,NR,NR")) continue;
                    StructureData processedData = DataHandler.processData(data);
                    if (processedData == null) continue;
                    if(isCsvEnabled && csvDataWriter != null) {
                    	csvDataWriter.writeData(data);                    	
                    }
                    processedQueue.put(processedData);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "DataProcessorThread").start();
    }

    public boolean recordData(ConnectionData connectionData, String patientName) {
        if (isCsvEnabled || csvDataWriter != null) return false;
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
    	if(!isCsvEnabled) return false;
    	if(csvDataWriter == null) return false;
    	csvDataWriter.close();
    	csvDataWriter = null;
    	this.isCsvEnabled = false;
    	return true;

    }
    
    public void stop() {
    	stopRecordingData();
    	this.active = false;
    	messageQueue.offer("STOP");
	}
}
