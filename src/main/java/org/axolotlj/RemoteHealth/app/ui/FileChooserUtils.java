package org.axolotlj.RemoteHealth.app.ui;

import java.io.File;
import java.util.Optional;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileChooserUtils {
    /**
     * Muestra un diálogo para seleccionar un archivo con una extensión específica.
     *
     * @param stage     Ventana padre
     * @param title     Título del diálogo
     * @param desc      Descripción de la extensión (ej. "Imágenes PNG")
     * @param extension Extensión permitida (ej. "*.png")
     * @return Archivo seleccionado, envuelto en Optional
     */
    public static Optional<File> chooseFile(Stage stage, String title, String desc, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, extension));
        File file = fileChooser.showOpenDialog(stage);
        return Optional.ofNullable(file);
    }
    
    /**
     * Muestra un diálogo para seleccionar una ubicación para guardar un archivo.
     *
     * @param stage     Ventana padre
     * @param title     Título del diálogo
     * @param desc      Descripción de la extensión (ej. "Documento de texto")
     * @param extension Extensión sugerida (ej. "*.txt")
     * @return Ruta del archivo a guardar, envuelta en Optional
     */
    public static Optional<File> chooseSaveLocation(Stage stage, String title, String desc, String extension, String name) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(desc, extension));
        fileChooser.setInitialFileName(name);
        File file = fileChooser.showSaveDialog(stage);
        return Optional.ofNullable(file);
    }
}
