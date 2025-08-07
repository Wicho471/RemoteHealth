package org.axolotlj.remotehealth.desktop.service;

import java.awt.image.BufferedImage;
import java.util.function.Consumer;

import org.axolotlj.remotehealth.core.logger.DataLogger;
import org.axolotlj.remotehealth.core.logger.Log;
import org.axolotlj.remotehealth.desktop.utils.DesktopQRDecorer;

import com.github.sarxos.webcam.Webcam;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;


public class QRScanner {
	private DataLogger dataLogger = Log.get();
	
    private Webcam webcam;
    private volatile boolean running = true;
    
    private final ImageView targetView;
    private final Consumer<String> onQRDetected;
    
    public QRScanner(ImageView view, Consumer<String> onQRDetected) {
        this.targetView   = view;
        this.onQRDetected = onQRDetected;
    }

    public void start() {
    	webcam = Webcam.getDefault();
        if (webcam == null) {
        	dataLogger.logWarn("No hay cámara disponible");
            return;
        }
        
        webcam.open();
        new Thread(() -> {
        	dataLogger.logInfo("Inciando servicio de camara");
            while (running) {
                BufferedImage image = webcam.getImage();
                if (image == null) continue;
                WritableImage fx = SwingFXUtils.toFXImage(image, null);
                Platform.runLater(() -> targetView.setImage(fx));
                String qr = DesktopQRDecorer.decodeQRCode(image);
                if (qr != null) {
                	dataLogger.logInfo("Codigo QR detectado");
                    running = false;
                    webcam.close();
                    onQRDetected.accept(qr);
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }
    
    public void stop() {
        running = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}
