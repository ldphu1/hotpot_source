package com.fourspicehotpot.backend.integration.qrcode.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class QrCodeUtil {

    public Image generateQr(String text) throws Exception {
        BitMatrix matrix = new MultiFormatWriter()
                .encode(text, BarcodeFormat.QR_CODE, 150, 150);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        return new Image(ImageDataFactory.create(out.toByteArray()));
    }
}