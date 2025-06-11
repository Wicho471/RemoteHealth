package org.axolotlj.RemoteHealth.app.ui;

import org.axolotlj.RemoteHealth.util.TxtUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class DialogPanelUtils {
	public static void showTextDialog(String title, String header, String path, double width, double height,
			boolean isAbout, String font, int size) {
		String content = isAbout ? TxtUtils.loadAboutText(path) : TxtUtils.loadText(path);

		Alert alert = AlertUtil.showInformationAlert(title, header, content, false);

		Text text = new Text(content);
		text.setFont(Font.font(font, size));

		TextFlow textFlow = new TextFlow(text);
		textFlow.setPrefWidth(width);

		ScrollPane scrollPane = new ScrollPane(textFlow);
		scrollPane.setFitToWidth(true);
		scrollPane.setPrefHeight(height);

		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.setContent(scrollPane);

		alert.showAndWait();
	}

}
