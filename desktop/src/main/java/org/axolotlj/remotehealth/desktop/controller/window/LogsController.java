package org.axolotlj.remotehealth.desktop.controller.window;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.io.FileCompressor;
import org.axolotlj.remotehealth.core.javafx.util.ImageViewUtils;
import org.axolotlj.remotehealth.desktop.ui.ButtonUtils;
import org.axolotlj.remotehealth.desktop.ui.TableUtils;
import org.axolotlj.remotehealth.desktop.utils.Fonts;
import org.axolotlj.remotehealth.desktop.utils.Images;
import org.axolotlj.remotehealth.core.model.LogEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LogsController {

	@FXML
	private TextFlow logTextFlow;
	@FXML
	private ScrollPane logScrollPane;
	@FXML
	private TableView<LogEntry> logsTable;
	@FXML
	private TableColumn<LogEntry, String> dateColumn;
	@FXML
	private TableColumn<LogEntry, Void> actionColumn;

	private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		setupTable();
		loadLogFiles();
	}

	private void setupTable() {
	    dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
	    actionColumn.setCellFactory(param -> new TableCell<>() {
	        private final Button btn = new Button();

	        {
	            ImageView icon = new ImageView();
	            ImageViewUtils.setImage(icon, Images.IMG_BUTTONS_FLECHA_DERECHA, 25, 25);
	            ButtonUtils.setGraphicImage(btn, icon);
	            btn.setOnAction(event -> {
	                LogEntry entry = getTableView().getItems().get(getIndex());
	                mostrarContenido(entry.getFile());
	            });
	        }

	        @Override
	        protected void updateItem(Void item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	                setGraphic(null);
	            } else {
	                HBox container = new HBox(btn);
	                container.setStyle("-fx-alignment: CENTER;");
	                setGraphic(container);
	            }
	        }
	    });
		TableUtils.adjustColumns(logsTable, false);

	    logsTable.setItems(logEntries);   
	}


	private void loadLogFiles() {
		File logsDir = ConfigFileHelper.getDLogsDir().toFile();
		if (!logsDir.exists() || !logsDir.isDirectory())
			return;

		File[] files = logsDir.listFiles(file -> file.getName().endsWith(".log") || file.getName().endsWith(".gz"));

		if (files == null) {
			System.out.println("No se encontraron archivos");
			return;			
		}

		List<LogEntry> entries = new ArrayList<>();

		for (File file : files) {
			entries.add(new LogEntry(file.getName(), file));
		}

		// Ordenar por fecha de modificación, descendente
		entries.sort(Comparator.comparing(f -> f.getFile().lastModified(), Comparator.reverseOrder()));
		logEntries.setAll(entries);
	}

	private void mostrarContenido(File file) {
	    logTextFlow.getChildren().clear(); // limpiar contenido anterior

	    try (BufferedReader reader = file.getName().endsWith(".gz")
	            ? new BufferedReader(new InputStreamReader(FileCompressor.decompressToStream(file)))
	            : Files.newBufferedReader(file.toPath())) {

	        String line;
	        while ((line = reader.readLine()) != null) {
	            printColoredLogLine(line);
	        }

	        // Scroll automático al final
	        logScrollPane.layout();
	        logScrollPane.setVvalue(1.0);

	    } catch (Exception e) {
	        logTextFlow.getChildren().setAll(new Text("Error al leer el archivo: " + e.getMessage()));
	    }
	}

	private void printColoredLogLine(String line) {
	    Text text = new Text(line + "\n");

	    if (line.contains("ERROR")) {
	        text.setFill(Color.web("#FF5555")); // Red brillante (Minecraft §c)
	    } else if (line.contains("WARN")) {
	        text.setFill(Color.web("#FFFF55")); // Amarillo (Minecraft §e)
	    } else if (line.contains("INFO")) {
	        text.setFill(Color.web("#55FF55")); // Verde (Minecraft §a)
	    } else if (line.contains("DEBUG")) {
	        text.setFill(Color.web("#55FFFF")); // Aqua (Minecraft §b)
	    } else {
	        text.setFill(Color.web("#FFFFFF")); // Blanco (Minecraft §f)
	    }

	    //text.setFont(Fonts.load(Paths.FONTS_UBUNTU_REGULAR_PATH, 12));  // Estilo consola
	    text.setFont(Fonts.UBUNTU_MONO_REGULAR);
	    logTextFlow.getChildren().add(text);
	}
}
