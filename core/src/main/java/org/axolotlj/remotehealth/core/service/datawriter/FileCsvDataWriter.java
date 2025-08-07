package org.axolotlj.remotehealth.core.service.datawriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.io.FileCompressor;
import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.model.ConnectionData;
import org.axolotlj.remotehealth.core.sensor.data.DataPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementación que escribe datos CSV en archivos físicos de forma asíncrona y con formato estructurado.
 */
public class FileCsvDataWriter extends CsvDataWriter {

    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final File dataFile;
    private final CSVPrinter csvPrinter;
    public final BlockingQueue<String[]> writeQueue;
    private final Thread writerThread;
    private volatile boolean running = true;
    private DataLogger dataLogger;

    @SuppressWarnings("deprecation")
	public FileCsvDataWriter(ConnectionData connectionData, String patientName, DataLogger dataLogger) throws IOException {
    	this.dataLogger = dataLogger;
    	
        Path dataDir = ConfigFileHelper.getDataDir();
        Files.createDirectories(dataDir);

        String fileName = "["+(connectionData != null ? connectionData.getName() : "Unkown")+"]["+(patientName != null ? patientName : "Unknown")+"]"+LocalDateTime.now().format(FILE_FORMATTER) + ".csv";
        this.dataFile = dataDir.resolve(fileName).toFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile, true));

        this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("timestamp", "ecg", "acc", "temp", "ir", "red"));

        this.writeQueue = new LinkedBlockingQueue<>();

        this.writerThread = new Thread(() -> {
            while (running || !writeQueue.isEmpty()) {
                try {
                    String[] values = writeQueue.take();
                    csvPrinter.printRecord((Object[]) values);
                    csvPrinter.flush();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    dataLogger.logException("Error al escribir en archivo CSV", e);
                }
            }
        }, "CsvDataWriter-WriterThread");

        writerThread.setDaemon(true);
        writerThread.start();
    }

    @Override
    public void writeData(String rawLine) {
        if (!running || rawLine == null || rawLine.isBlank() || rawLine.endsWith(",NR,NR,NR,NR,NR") || rawLine.contains(",NR,NR,NR,NR,NR")) return;

        String[] values = rawLine.split(",");
        if (values.length != 6) {
            dataLogger.logWarn("Formato inválido de línea CSV: " + rawLine);
            return;
        }
        writeQueue.offer(values);
    }

    @Override
    public void close() {
        running = false;
        writeQueue.offer(new String[0]);
        try {
            writerThread.join();
            while (!writeQueue.isEmpty()) {
                String[] data = writeQueue.poll();
                if (data != null && data.length > 0) {
                    csvPrinter.printRecord((Object[]) data);
                }
            }
            csvPrinter.close();
            try {
                FileCompressor.compress(dataFile);
                Files.deleteIfExists(dataFile.toPath());
            } catch (IOException e) {
                dataLogger.logException("CsvDataWriter - error al comprimir archivo CSV: " , e);
            }
            dataLogger.logDebug("FileCsvDataWriter cerrado correctamente después de vaciar la cola.");
        } catch (IOException | InterruptedException e) {
            dataLogger.logException("Error al cerrar FileCsvDataWriter: " , e);
        }
    }

    @Override
    public String getDataFilePath() {
        return dataFile.getAbsolutePath();
    }

	@Override
	public void writeData(DataPoint data) {
	    writeData(data.toCsvLine());
	}
	
	
}
