package org.axolotlj.RemoteHealth.app.ui;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class TableUtils {

	public static void adjustColumns(TableView<?> tableView, boolean isEditable) {
		tableView.widthProperty().addListener((obs, oldWidth, newWidth) -> {
			double totalWidth = newWidth.doubleValue();
			int columnCount = tableView.getColumns().size();
			double colWidth = totalWidth / columnCount;
			for (TableColumn<?, ?> column : tableView.getColumns()) {
				column.setPrefWidth(colWidth);
			}
		});
		tableView.setEditable(isEditable);
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
