package org.axolotlj.RemoteHealth.app.ui;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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
}
