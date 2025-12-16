package util;

import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

public class RawExtractor {
    public void extract(File bmpFilename, File rawFilename, File palletFilename) throws IOException {
        byte[] pallet = FileUtils.readAllBytes(palletFilename);
        byte[] raw = FileUtils.readAllBytes(rawFilename);
        BufferedImage img = new BufferedImage(320, 200, TYPE_INT_RGB);
        int index = 0;
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 320; x++) {
                int color = rgbToInt(
                        pallet[(raw[index] & 0xFF) * 3 + 0] << 2,
                        pallet[(raw[index] & 0xFF) * 3 + 1] << 2,
                        pallet[(raw[index] & 0xFF) * 3 + 2] << 2
                );
                img.setRGB(x, y, color);
                index++;
            }
        }
        ImageIO.write(img, "bmp", bmpFilename);
    }

    public void compress(File bmpFilename, File rawFilename, File palletFilename) throws IOException {
        byte[] raw = new byte[64000];
        byte[] pallet = FileUtils.readAllBytes(palletFilename);
        BufferedImage img = ImageIO.read(bmpFilename);
        int index = 0;
        for (int y = 0; y < 200; y++) {
            for (int x = 0; x < 320; x++) {

                int rgb = img.getRGB(x, y);
                int b = rgb & 0xff;
                int g = (rgb & 0xff00) >> 8;
                int r = (rgb & 0xff0000) >> 16;
                raw[index] = (byte) findIndex(pallet, r, g, b);
                index++;
            }
        }
        FileOutputStream fos = new FileOutputStream(rawFilename);
        fos.write(raw);
        fos.close();
    }

    public static int findIndex(byte[] pallet, int r, int g, int b) {
        r = r >> 2;
        g = g >> 2;
        b = b >> 2;
        for (int a = 0; a < 255; a++) {
            if (pallet[a * 3 + 0] == r &&
                    pallet[a * 3 + 1] == g &&
                    pallet[a * 3 + 2] == b) {
                return a;
            }
        }
        return 0;
    }

    public static int rgbToInt(int r, int g, int b) {
        return (0xFF << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
