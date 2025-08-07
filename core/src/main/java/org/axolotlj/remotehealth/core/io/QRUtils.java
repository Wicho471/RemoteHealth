package org.axolotlj.remotehealth.core.io;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;

public class QRUtils {
    public static String tryDecodeQR(BinaryBitmap bitmap) {
        try {
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (Exception e) {
            return null;
        }
    }
}
