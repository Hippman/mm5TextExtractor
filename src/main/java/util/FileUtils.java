package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtils {
    public static byte[] readAllBytes(File fil) throws IOException {
        FileInputStream fis = new FileInputStream(fil);
        byte[] exe = fis.readAllBytes();
        fis.close();
        return exe;
    }
}
