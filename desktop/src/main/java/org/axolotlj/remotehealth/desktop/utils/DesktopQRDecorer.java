package org.axolotlj.remotehealth.desktop.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.axolotlj.remotehealth.core.io.QRUtils;
import org.axolotlj.remotehealth.core.logger.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

public class DesktopQRDecorer {
	public static String decodeQRCode(BufferedImage BufferedImage) {
		BinaryBitmap bitmap = toBinaryBitmap(BufferedImage);
		return QRUtils.tryDecodeQR(bitmap);
	}

	public static String decodeQRCode(String filePath) {
		BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(new File(filePath));
			BinaryBitmap bitmap = toBinaryBitmap(bufferedImage);
			return QRUtils.tryDecodeQR(bitmap);
		} catch (IOException e) {
			Log.get().logException("Ocurrio un error al intentar obtener archivo de la ruta '" + filePath + "'", e);
		} catch (Exception e) {
			Log.get().logException(
					"Ocurrio un error al inesperado al intentar obtener archivo de la ruta '" + filePath + "'", e);
		}
		return null;
	}

	private static BinaryBitmap toBinaryBitmap(BufferedImage bufferedImage) {
		LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
		return new BinaryBitmap(new HybridBinarizer(source));
	}
}
