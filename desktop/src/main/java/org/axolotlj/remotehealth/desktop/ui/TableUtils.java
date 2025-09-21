package org.axolotlj.remotehealth.desktop.ui;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

/**
 * Utilidades para manejo de tablas en JavaFX.
 */
public class TableUtils {

	/**
	 * Modos de ajuste de columnas.
	 */
	public enum ColumnAdjustMode {
		DISTRIBUTE, // Reparte el ancho de forma equitativa
		CONTENT, // Ajusta al contenido
		MIXED // Ajusta al contenido, pero reparte espacio sobrante
	}

	/**
	 * Ajusta las columnas de una tabla según el modo indicado.
	 *
	 * @param tableView  Tabla objetivo
	 * @param isEditable Define si la tabla será editable
	 * @param mode       Modo de ajuste de columnas
	 */
	public static void adjustColumns(TableView<?> tableView, boolean isEditable, ColumnAdjustMode mode) {
		if (mode == ColumnAdjustMode.DISTRIBUTE) {
			tableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
				double totalWidth = newWidth.doubleValue();
				int columnCount = tableView.getColumns().size();
				double colWidth = totalWidth / columnCount;
				for (TableColumn<?, ?> column : tableView.getColumns()) {
					column.setPrefWidth(colWidth);
				}
			});
		} else if (mode == ColumnAdjustMode.CONTENT) {
			adjustColumnsToContent(tableView);
		} else if (mode == ColumnAdjustMode.MIXED) {
			adjustColumnsMixed(tableView);
		}
		tableView.setEditable(isEditable);
	}

	private static void adjustColumnsToContent(TableView<?> tableView) { // TODO No funciona correctamente
		ObservableList<?> items = tableView.getItems();
		if (items == null || items.isEmpty()) {
			distributeEvenly(tableView);
			return;
		}

		for (TableColumn<?, ?> column : tableView.getColumns()) {
			if ("IGNORE_ADJUST".equals(column.getUserData())) {
				continue;
			}

			double maxWidth = calculateMaxColumnWidth(tableView, column);
			double adjustedWidth = (maxWidth * 1.15) + 25.0;
			column.setPrefWidth(adjustedWidth);
		}
	}

	private static void adjustColumnsMixed(TableView<?> tableView) {
		ObservableList<?> items = tableView.getItems();
		if (items == null || items.isEmpty()) {
			distributeEvenly(tableView);
			return;
		}

		double totalWidth = tableView.getWidth();
		double usedWidth = 0;

		for (TableColumn<?, ?> column : tableView.getColumns()) {
			double maxWidth = calculateMaxColumnWidth(tableView, column);
			column.setPrefWidth(maxWidth + 20);
			usedWidth += column.getPrefWidth();
		}

		if (usedWidth < totalWidth) {
			double extra = (totalWidth - usedWidth) / tableView.getColumns().size();
			for (TableColumn<?, ?> column : tableView.getColumns()) {
				column.setPrefWidth(column.getPrefWidth() + extra);
			}
		}
	}

	private static void distributeEvenly(TableView<?> tableView) {
		double totalWidth = tableView.getWidth();
		int columnCount = tableView.getColumns().size();
		double colWidth = totalWidth / columnCount;
		for (TableColumn<?, ?> column : tableView.getColumns()) {
			column.setPrefWidth(colWidth);
		}
	}

	private static double calculateMaxColumnWidth(TableView<?> tableView, TableColumn<?, ?> column) {
		Text headerText = new Text(column.getText());
		double maxWidth = headerText.getLayoutBounds().getWidth();

		int rowCount = tableView.getItems().size();
		for (int row = 0; row < rowCount; row++) {
			Object cellData = column.getCellData(row);
			if (cellData != null) {
				Text cellText = new Text(cellData.toString());
				double cellWidth = cellText.getLayoutBounds().getWidth();
				if (cellWidth > maxWidth) {
					maxWidth = cellWidth;
				}
			}
		}
		return maxWidth;
	}

	/**
	 * Asocia un campo de búsqueda a una tabla con lógica de filtrado personalizada.
	 *
	 * @param searchField  Campo de texto usado para búsqueda
	 * @param tableView    Tabla a actualizar
	 * @param dataSupplier Proveedor de todos los datos (sin filtrar)
	 * @param filterLogic  Lógica de filtrado con base en el texto ingresado
	 * @param <T>          Tipo de los elementos en la tabla
	 */
	public static <T> void bindSearch(TextField searchField, TableView<T> tableView, Supplier<List<T>> dataSupplier,
			FilterFactory<T> filterLogic) {

		searchField.textProperty()
				.addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
					String query = newValue == null ? "" : newValue.trim().toLowerCase();
					List<T> allItems = dataSupplier.get();
					if (query.isBlank()) {
						tableView.getItems().setAll(allItems);
						return;
					}
					List<T> filtered = allItems.stream().filter(filterLogic.createPredicate(query)).toList();
					tableView.getItems().setAll(filtered);
				});
	}

	@FunctionalInterface
	public interface FilterFactory<T> {
		Predicate<T> createPredicate(String query);
	}
}
