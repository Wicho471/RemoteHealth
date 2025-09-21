package org.axolotlj.remotehealth.desktop.controller.window;

import static org.axolotlj.remotehealth.desktop.javafx.current.AsyncExecutor.runAsyncTask;
import static org.axolotlj.remotehealth.desktop.javafx.current.FxThreadUtils.runOnUIThread;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.axolotlj.remotehealth.core.config.ConfigFileHelper;
import org.axolotlj.remotehealth.core.io.FileCompressor;
import org.axolotlj.remotehealth.core.model.LogEntry;
import org.axolotlj.remotehealth.desktop.ui.AlertUtil;
import org.axolotlj.remotehealth.desktop.ui.ButtonUtils;
import org.axolotlj.remotehealth.desktop.ui.ImageViewUtils;
import org.axolotlj.remotehealth.desktop.ui.assets.Images;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class LogsController {

	@FXML
	private TableView<LogEntry> logsTable;
	@FXML
	private TableColumn<LogEntry, String> dateColumn;
	@FXML
	private TableColumn<LogEntry, Void> actionColumn;
	@FXML
	private TableColumn<LogEntry, String> sizeColumn;
	@FXML
	private TableColumn<LogEntry, Long> lineCountColumn;

	@FXML
	private ListView<String> logListView; // reemplaza a TextFlow
	@FXML
	private VBox charging;
	@FXML
	private StackPane stackPane;

	private final ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();
	private final ObservableList<String> logLines = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		showChargeScreen(false);
		setupTable();
		setupLogList();
		loadLogFiles();
	}

	@FXML
	private void clearAllHandle() {
		Optional<ButtonType> option = AlertUtil.showConfirmationAlert("Alerta",
				"¿Está seguro de borrar todos los registros?", "Estos cambios serán permanentes");

		if (option.isPresent() && option.get() == ButtonType.OK) {
			Path logsDir = ConfigFileHelper.getDLogsDir();
			try {
				if (Files.exists(logsDir) && Files.isDirectory(logsDir)) {
					Files.list(logsDir).forEach(path -> {
						try {
							Files.deleteIfExists(path);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				}
			} catch (IOException e) {
				e.printStackTrace(); // Manejo de error general
			}
			setupTable();
			setupLogList();
			loadLogFiles();
		}
	}

	private void showChargeScreen(boolean show) {
		runOnUIThread(() -> {
			charging.setVisible(show);
			charging.setManaged(show);
		});
	}

	private void setupTable() {
		dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
		sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));
		lineCountColumn.setCellValueFactory(new PropertyValueFactory<>("lineCount"));

	    actionColumn.setUserData("IGNORE_ADJUST");

	    actionColumn.setPrefWidth(80);
	    actionColumn.setStyle("-fx-alignment: CENTER;");
		
		actionColumn.setCellFactory(param -> new TableCell<>() {
			private final Button btn = new Button();

			{
				ImageView icon = new ImageView();
				ImageViewUtils.setImage(icon, Images.IMG_BUTTONS_FLECHA_DERECHA, 25, 25);
				ButtonUtils.setGraphicImage(btn, icon);
				btn.setOnAction(event -> {
					LogEntry entry = getTableView().getItems().get(getIndex());
					showContent(entry.getFile());
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
		logsTable.setItems(logEntries);
	}

	private void setupLogList() {
		logListView.setItems(logLines);

		logListView.setCellFactory(list -> new ListCell<>() {

			
			private final Label label = new Label();

			{
				label.setWrapText(true);
				label.setMaxWidth(Double.MAX_VALUE);
				setPrefWidth(0);
			}
			

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
					setStyle("-fx-background-color: black;");
				} else {
					label.setText(item);
					// Colorear según nivel de log (estilo Minecraft)
					if (item.contains("ERROR")) {
						label.setTextFill(Color.web("#FF5555")); // Rojo
					} else if (item.contains("WARN")) {
						label.setTextFill(Color.web("#FFFF55")); // Amarillo
					} else if (item.contains("INFO")) {
						label.setTextFill(Color.web("#55FF55")); // Verde
					} else if (item.contains("DEBUG")) {
						label.setTextFill(Color.web("#55FFFF")); // Cyan
					} else {
						label.setTextFill(Color.WHITE); // Blanco por defecto
					}
					label.setStyle("-fx-background-color: black;");
					setGraphic(label);
				}
			}
		});
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

		runAsyncTask("Log-Metadata", () -> {
			List<LogEntry> entries = new ArrayList<>();
			for (File file : files) {
				String size = humanReadableByteCountBin(file.length());
				long lineCount = countLines(file);
				entries.add(new LogEntry(file.getName(), file, size, lineCount));
			}
			entries.sort(Comparator.comparing(f -> f.getFile().lastModified(), Comparator.reverseOrder()));
			return entries;
		}, result -> {
			logEntries.setAll(result);
		}, error -> {
			System.err.println("Error cargando metadatos de logs: " + error.getMessage());
		});
	}

	private void showContent(File file) {
		logLines.clear();
		showChargeScreen(true);

		runAsyncTask("Log-Reader", () -> {
			List<String> lines = new ArrayList<>();
			try (BufferedReader reader = file.getName().endsWith(".gz")
					? new BufferedReader(new InputStreamReader(FileCompressor.decompressToStream(file)))
					: Files.newBufferedReader(file.toPath())) {

				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isBlank())
						continue;
					lines.add(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return lines;
		}, result -> {
			logLines.setAll(result);
			logListView.scrollTo(logLines.size() - 1); // bajar al finals
			showChargeScreen(false);
		}, error -> {
			logLines.setAll(List.of("Error al leer archivo: " + error.getMessage()));
			showChargeScreen(false);
		});
	}

	private static String humanReadableByteCountBin(long bytes) {
		long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		if (absB < 1024) {
			return bytes + " B";
		}
		long value = absB;
		CharacterIterator ci = new java.text.StringCharacterIterator("KMGTPE");
		for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
			value >>= 10;
			ci.next();
		}
		value *= Long.signum(bytes);
		return String.format("%.1f %ciB", value / 1024.0, ci.current());
	}

	private static long countLines(File file) {
		try (BufferedReader reader = file.getName().endsWith(".gz")
				? new BufferedReader(new InputStreamReader(FileCompressor.decompressToStream(file)))
				: Files.newBufferedReader(file.toPath())) {
			return reader.lines().count();
		} catch (IOException e) {
			return -1;
		}
	}

}
