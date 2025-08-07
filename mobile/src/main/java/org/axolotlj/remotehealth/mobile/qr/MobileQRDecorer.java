package org.axolotlj.remotehealth.mobile.qr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.axolotlj.remotehealth.core.io.QRUtils;
import org.axolotlj.remotehealth.core.logger.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

/**
 * Utilidad para decodificar códigos QR desde imágenes en dispositivos móviles.
 */
public class MobileQRDecorer {

    /**
     * Intenta decodificar un código QR desde una imagen de JavaFX.
     *
     * @param image imagen cargada desde la galería del dispositivo
     * @return contenido del código QR o null si no fue posible decodificar
     */
    public static String decodeQRCode(Image image) {
        if (image == null) {
            Log.get().logWarn("Imagen nula para decodificación QR.");
            return null;
        }

        try {
            BinaryBitmap bitmap = fromImage(image);
            return QRUtils.tryDecodeQR(bitmap);
        } catch (Exception e) {
            Log.get().logException("Error al intentar decodificar QR desde imagen", e);
            return null;
        }
    }
    
    /**
     * Intenta decodificar un código QR desde un archivo de imagen.
     *
     * @param file archivo de imagen compatible (ej. PNG, JPG)
     * @return contenido del código QR o null si no fue posible decodificar
     */
    public static String decodeQRCode(File file) {
        if (file == null || !file.exists()) {
            Log.get().logWarn("Archivo nulo o inexistente para decodificación QR.");
            return null;
        }

        try (FileInputStream input = new FileInputStream(file)) {
            Image image = new Image(input);
            return decodeQRCode(image);
        } catch (IOException e) {
            Log.get().logException("Error al leer archivo para decodificación QR: " + file.getAbsolutePath(), e);
        } catch (Exception e) {
            Log.get().logException("Error inesperado al decodificar archivo QR: " + file.getAbsolutePath(), e);
        }

        return null;
    }
    
	/**
	 * Convierte una imagen de JavaFX a una instancia de BinaryBitmap.
	 *
	 * @param image Imagen en JavaFX
	 * @return BinaryBitmap que puede ser usado por ZXing
	 */
	private static BinaryBitmap fromImage(Image image) {
		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		int[] pixels = new int[width * height];

		PixelReader reader = image.getPixelReader();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Color color = reader.getColor(x, y);
				int r = (int) (color.getRed() * 255);
				int g = (int) (color.getGreen() * 255);
				int b = (int) (color.getBlue() * 255);
				int argb = (0xFF << 24) | (r << 16) | (g << 8) | b;
				pixels[y * width + x] = argb;
			}
		}

		LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
		return new BinaryBitmap(new HybridBinarizer(source));
	}
}
