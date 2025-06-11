package org.axolotlj.RemoteHealth.controller.window;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import org.axolotlj.RemoteHealth.app.ui.AlertUtil;
import org.axolotlj.RemoteHealth.app.ui.ImageViewUtils;
import org.axolotlj.RemoteHealth.app.ui.TableUtils;
import org.axolotlj.RemoteHealth.common.Images;
import org.axolotlj.RemoteHealth.config.ConfigFileHelper;
import org.axolotlj.RemoteHealth.controller.scene.DataAnalysisController;
import org.axolotlj.RemoteHealth.model.CsvFileInfo;
import org.axolotlj.RemoteHealth.sensor.io.CsvDataManager;
import org.axolotlj.RemoteHealth.service.logger.DataLogger;
import org.axolotlj.RemoteHealth.service.logger.Log;
import org.axolotlj.RemoteHealth.util.CsvUtils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class CsvSelectorController {
	private DataLogger dataLogger;
	
	private List<CsvFileInfo> allCsvData = List.of();

	@FXML
	private TableView<CsvFileInfo> csvTable;
	
	@FXML
	private TextField searchTextField;

	private DataAnalysisController parentController;

	public void setParentController(DataAnalysisController parent) {
		this.parentController = parent;
	}

	@FXML
	public void initialize() {
		this.dataLogger = Log.get();
		loadCsvFiles();
		setupSearchField();
	}

	private void setupSearchField() {
		TableUtils.bindSearch(
			searchTextField,
			csvTable,
			() -> allCsvData,
			query -> data ->
				data.getFile().getName().toLowerCase().contains(query) ||
				data.getDevice().toLowerCase().contains(query) ||
				data.getPatient().toLowerCase().contains(query)
		);
	}

	
	private void loadCsvFiles() {
		File folder = ConfigFileHelper.getDataDir().toFile();
		List<File> csvFiles = CsvUtils.getAllCsvFiles(folder);
		List<CsvFileInfo> tempData = FXCollections.observableArrayList();

		for (File file : csvFiles) {
			CsvFileInfo info = parseFileName(file);
			if (info != null) {
				tempData.add(info);
			}
		}

		this.allCsvData = tempData;
		csvTable.setItems(FXCollections.observableArrayList(allCsvData));
		setupTableColumns();
	}


	/**
	 * Parsea el nombre del archivo CSV para extraer información estructurada. Si el
	 * formato es incorrecto, se asignan valores por defecto "Unknown".
	 *
	 * @param file Archivo CSV a procesar
	 * @return CsvFileInfo con los campos extraídos o valores por defecto
	 */
	private CsvFileInfo parseFileName(File file) {
		String fileName = file.getName();
		String dispositivo = "Unknown";
		String paciente = "Unknown";
		String fecha = "Unknown";
		String hora = "Unknown";
		String duracionStr = "Unknown";
		String datosStr = "Unknown";

		try {
			int firstBracket = fileName.indexOf('[');
			int secondBracket = fileName.indexOf(']', firstBracket);
			int thirdBracket = fileName.indexOf('[', secondBracket);
			int fourthBracket = fileName.indexOf(']', thirdBracket);

			if (firstBracket == -1 || secondBracket == -1 || thirdBracket == -1 || fourthBracket == -1) {
				throw new IllegalArgumentException("Formato de nombre inválido: no se encontraron corchetes");
			}

			dispositivo = fileName.substring(firstBracket + 1, secondBracket);
			paciente = fileName.substring(thirdBracket + 1, fourthBracket);

			int extensionIndex = fileName.lastIndexOf('.');
			if (extensionIndex == -1 || extensionIndex <= fourthBracket) {
				throw new IllegalArgumentException("Formato de nombre inválido: extensión o datetime ausente");
			}

			String datetime = fileName.substring(fourthBracket + 1, extensionIndex);
			String[] parts = datetime.split("_");
			if (parts.length != 2) {
				throw new IllegalArgumentException("Formato de fecha y hora inválido");
			}

			fecha = parts[0];
			hora = parts[1].replace("-", ":");

		} catch (Exception e) {
			System.out.println("Error en parseFileName (estructura de nombre): " + e.getMessage());
		}

		try {
			long duracionMs = CsvDataManager.calculateDuration(file.toPath());
			duracionStr = String.format("%.2f s", duracionMs / 1000.0);
		} catch (Exception e) {
			System.out.println("Error al calcular duración: " + e.getMessage());
			duracionStr = "Error";
		}

		try {
			int datos = CsvDataManager.countValidDataRows(file.toPath());
			datosStr = String.valueOf(datos);
		} catch (Exception e) {
			System.out.println("Error al contar datos: " + e.getMessage());
			datosStr = "Error";
		}

		return new CsvFileInfo(file, dispositivo, paciente, fecha, hora, duracionStr, datosStr, file.toPath());
	}

	@SuppressWarnings("unchecked")
	private void setupTableColumns() {
		// Suponiendo que ya tienes columnas declaradas en el FXML por orden.

		TableColumn<CsvFileInfo, Void> verCol = (TableColumn<CsvFileInfo, Void>) csvTable.getColumns().get(0);
		TableColumn<CsvFileInfo, String> dispositivoCol = (TableColumn<CsvFileInfo, String>) csvTable.getColumns()
				.get(1);
		TableColumn<CsvFileInfo, String> pacienteCol = (TableColumn<CsvFileInfo, String>) csvTable.getColumns().get(2);
		TableColumn<CsvFileInfo, String> fechaCol = (TableColumn<CsvFileInfo, String>) csvTable.getColumns().get(3);
		TableColumn<CsvFileInfo, String> horaCol = (TableColumn<CsvFileInfo, String>) csvTable.getColumns().get(4);
		TableColumn<CsvFileInfo, String> duracionCol = (TableColumn<CsvFileInfo, String>) csvTable.getColumns().get(5);
		TableColumn<CsvFileInfo, String> datosCol = (TableColumn<CsvFileInfo, String>) csvTable.getColumns().get(6);
		TableColumn<CsvFileInfo, Void> seleccionarCol = (TableColumn<CsvFileInfo, Void>) csvTable.getColumns().get(7);

		// Columna de eliminar archivo
		TableColumn<CsvFileInfo, Void> eliminarCol = new TableColumn<>("Eliminar");
		eliminarCol.setPrefWidth(50);
		eliminarCol.setCellFactory(col -> new TableCell<>() {
			private final Button btn = new Button();
			private final ImageView iconView = new ImageView();
			private final HBox box = new HBox();

			{
				ImageViewUtils.setImage(iconView, Images.IMG_ICONS_DELETE, 25, 25);
				btn.setGraphic(iconView);
				btn.setOnAction(event -> {
					CsvFileInfo info = getTableView().getItems().get(getIndex());

					// Alerta de confirmación
					var confirm = AlertUtil.showConfirmationAlert(
							"Eliminar archivo",
							"¿Estás seguro que deseas eliminar el archivo?",
							info.getFile().getName() + "\nEsta acción no se puede deshacer."
					);

					if (confirm.isPresent() && confirm.get() == javafx.scene.control.ButtonType.OK) {
						File file = info.getFile();
						if (file.exists() && file.delete()) {
							csvTable.getItems().remove(info);
							AlertUtil.showInformationAlert("Archivo eliminado", null, "El archivo se eliminó correctamente.", false);
						} else {
							AlertUtil.showErrorAlert("Error", null, "No se pudo eliminar el archivo.");
						}
					}
				});

				box.getChildren().add(btn);
				box.setAlignment(Pos.CENTER);
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(box);
				}
			}
		});

		// Insertar columna al inicio
		csvTable.getColumns().add(0, eliminarCol);

		
		dispositivoCol.setCellValueFactory(cellData -> cellData.getValue().deviceProperty());
		pacienteCol.setCellValueFactory(cellData -> cellData.getValue().patientProperty());
		fechaCol.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
		horaCol.setCellValueFactory(cellData -> cellData.getValue().timeProperty());
		duracionCol.setCellValueFactory(cellData -> cellData.getValue().durationProperty());
		datosCol.setCellValueFactory(cellData -> cellData.getValue().dataSummaryProperty());

		// Ver columna (ícono Excel)
		// Ver columna (ícono Excel)
		verCol.setCellFactory(col -> new TableCell<>() {
			private final Button btn = new Button();
			private final ImageView iconView = new ImageView();
			private final HBox box = new HBox();

			{
				ImageViewUtils.setImage(iconView, Images.IMG_MISC_EXCEL, 25, 25);
				btn.setGraphic(iconView);
				btn.setOnAction(event -> {
					CsvFileInfo info = getTableView().getItems().get(getIndex());
					System.out.println("Abrir para ver: " + info.getPath().toAbsolutePath());

					try {
						Path fileToOpen = info.getPath();
						File originalFile = fileToOpen.toFile();

						if (!originalFile.exists()) {
							AlertUtil.showErrorAlert("Error", "Archivo no encontrado",
									"El archivo no existe en la ruta: " + originalFile.getAbsolutePath());
							return;
						}
						Path tempFilePath = java.nio.file.Files.createTempFile("RemoteHealthTemp-", ".csv");
						java.nio.file.Files.copy(fileToOpen, tempFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
						tempFilePath.toFile().deleteOnExit();


						// ✅ Ahora abrir el archivo temporal
						if (Desktop.isDesktopSupported()) {
							Desktop desktop = Desktop.getDesktop();
							if (desktop.isSupported(Desktop.Action.OPEN)) {
								desktop.open(tempFilePath.toFile());
							} else {
								dataLogger.logWarn("La acción OPEN no está soportada en este sistema.");
								AlertUtil.showErrorAlert("Error", "No se puede abrir el archivo",
										"La acción OPEN no está soportada en este sistema.");
							}
						} else {
							dataLogger.logWarn("Desktop no está soportado en este sistema.");
							AlertUtil.showErrorAlert("Error", "No se puede abrir el archivo",
									"Desktop no está soportado en este sistema.");
						}

					} catch (IOException e) {
						dataLogger.logError("No se puede abrir el archivo -> "+e.getMessage());
						AlertUtil.showErrorAlert("Error", "No se puede abrir el archivo", e.getMessage());
					}
				});

				box.getChildren().add(btn);
				box.setAlignment(Pos.CENTER); // Centrar el botón
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(box);
				}
			}
		});

		// Seleccionar columna (botón o ícono)
		seleccionarCol.setCellFactory(col -> new TableCell<>() {
			private final Button btn = new Button();
			private final ImageView iconView = new ImageView();
			private final HBox box = new HBox();

			{
				ImageViewUtils.setImage(iconView, Images.IMG_BUTTONS_ABRIR, 25, 25);
				btn.setGraphic(iconView);
				btn.setOnAction(event -> {
					CsvFileInfo info = getTableView().getItems().get(getIndex());
					dataLogger.logDebug("Seleccionado: " + info.getFile().getName());
					if (parentController != null) {
						parentController.loadFile(info.getFile().getAbsolutePath());
					}
					// Cerrar modal
					Stage stage = (Stage) csvTable.getScene().getWindow();
					stage.close();
				});
				box.getChildren().add(btn);
				box.setAlignment(Pos.CENTER);
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(box);
				}
			}
		});

		TableUtils.adjustColumns(csvTable, false);
	}
}
