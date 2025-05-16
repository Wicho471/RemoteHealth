package org.axolotlj.RemoteHealth.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.io.File;
import java.nio.file.Path;

/**
 * Representa la información asociada a un archivo CSV con datos de sesión.
 */
public class CsvFileInfo {

    private final File file;
    private final Path path;
    private final StringProperty device;
    private final StringProperty patient;
    private final StringProperty date;
    private final StringProperty time;
    private final StringProperty duration;
    private final StringProperty dataSummary;

    /**
     * Crea una instancia con la información relevante del archivo CSV.
     *
     * @param file archivo original
     * @param device identificador del dispositivo
     * @param patient nombre o código del paciente
     * @param date fecha de la sesión
     * @param time hora de la sesión
     * @param duration duración total
     * @param dataSummary resumen de datos o cantidad de registros
     * @param path ruta absoluta del archivo
     */
    public CsvFileInfo(File file, String device, String patient, String date, String time, String duration, String dataSummary, Path path) {
        this.file = file;
        this.device = new SimpleStringProperty(device);
        this.patient = new SimpleStringProperty(patient);
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.duration = new SimpleStringProperty(duration);
        this.dataSummary = new SimpleStringProperty(dataSummary);
        this.path = path;
    }

    /**
     * Devuelve el archivo CSV original.
     *
     * @return archivo CSV
     */
    public File getFile() {
        return file;
    }

    /**
     * Devuelve la ruta absoluta del archivo.
     *
     * @return ruta del archivo
     */
    public Path getPath() {
        return path;
    }
    
    public String getDevice() {
		return device.toString();
	}
    
    public String getPatient() {
		return patient.toString();
	}

    /**
     * Devuelve la propiedad del dispositivo.
     *
     * @return propiedad de dispositivo
     */
    public StringProperty deviceProperty() {
        return device;
    }

    /**
     * Devuelve la propiedad del paciente.
     *
     * @return propiedad de paciente
     */
    public StringProperty patientProperty() {
        return patient;
    }

    /**
     * Devuelve la propiedad de la fecha.
     *
     * @return propiedad de fecha
     */
    public StringProperty dateProperty() {
        return date;
    }

    /**
     * Devuelve la propiedad de la hora.
     *
     * @return propiedad de hora
     */
    public StringProperty timeProperty() {
        return time;
    }

    /**
     * Devuelve la propiedad de la duración.
     *
     * @return propiedad de duración
     */
    public StringProperty durationProperty() {
        return duration;
    }

    /**
     * Devuelve la propiedad del resumen de datos.
     *
     * @return propiedad de resumen de datos
     */
    public StringProperty dataSummaryProperty() {
        return dataSummary;
    }
}
