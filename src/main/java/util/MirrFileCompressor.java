package util;

import dto.OneString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import java.util.Arrays;

public class MirrFileCompressor {
    public static void compressMirr(File dat, File xls, File outFilename) throws IOException {
        byte[] exe = FileUtils.readAllBytes(dat);

        HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(xls.toPath()));
        HSSFSheet sheet = wb.getSheetAt(0);

        for (int a = 1; a <= sheet.getLastRowNum(); a++) {
            HSSFRow row = sheet.getRow(a);
            OneString string = new OneString();
            if (row == null) {
                continue;
            }
            try {
                String newDat = row.getCell(1).getStringCellValue();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataUtils.string2bytes(newDat, baos);
                byte[] bs = baos.toByteArray();

                byte[] newBytes=Arrays.copyOf(bs, 28);
                if (newBytes.length <= 28) {
                    overwrite(exe, newBytes, (a-1)*32);
                } else {
                    System.out.println(String.format("русская строка длинее 28 символов. Строка -  %s", newDat));
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        FileOutputStream fos = new FileOutputStream(outFilename);
        fos.write(exe);
        fos.flush();
        fos.close();
    }
    private static void overwrite(byte[] exe, byte[] data, int offset) {
        for (int a = 0; a < data.length; a++) {
            exe[a + offset] = data[a];
        }
    }
}
