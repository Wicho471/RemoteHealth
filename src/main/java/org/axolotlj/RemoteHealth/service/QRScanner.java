package org.axolotlj.RemoteHealth.service;

import com.github.sarxos.webcam.Webcam;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.LuminanceSource;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

public class QRScanner {
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
            System.err.println("No hay cámara disponible");
            return;
        }
        
        webcam.open();

        new Thread(() -> {
            while (running) {
                BufferedImage image = webcam.getImage();
                if (image == null) continue;
                WritableImage fx = SwingFXUtils.toFXImage(image, null);
                Platform.runLater(() -> targetView.setImage(fx));
                String qr = decodeQRCode(image);
                if (qr != null) {
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

    private String decodeQRCode(BufferedImage image) {
        try {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String decodeQRCode(String filePath) throws IOException, NotFoundException {
        BufferedImage bufferedImage = ImageIO.read(new File(filePath));
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(bitmap);
        System.out.println("Qr data -> "+result.getText());
        return result.getText();
    }

    
    public void stop() {
        running = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}
