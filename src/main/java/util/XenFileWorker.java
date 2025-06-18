package util;

import org.apache.poi.hssf.usermodel.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class XenFileWorker {
    public void extractTexts(File fil, String newFilename) throws IOException {
        byte[] file = FileUtils.readAllBytes(fil);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        HSSFSheet sheet = wb.createSheet(fil.getName());
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        HSSFCell cell = row.createCell(0);
        sheet.setColumnWidth(0, 15360);
        sheet.setColumnWidth(1, 15360);
        cell.setCellValue(new HSSFRichTextString("Original"));
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("translated"));
        cell.setCellStyle(style);

        for (int a = 0; a < file.length; a++) {

            baos.write(file[a]);
            if ((file[a] & 0xffL) < 0x20) {

                String temp = renderString(baos.toByteArray());
                if (!temp.isEmpty()) {
                    addRow(sheet, style, temp);
                }
                baos = new ByteArrayOutputStream();
            }
        }
        FileOutputStream fos = new FileOutputStream(newFilename);
        wb.write(fos);
        wb.close();
    }

    public void compressTexts(File xls, String outFilename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(xls.toPath()));
        HSSFSheet sheet = wb.getSheetAt(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int a = 1; a <= sheet.getLastRowNum(); a++) {
            HSSFRow row = sheet.getRow(a);
            String oldDat = row.getCell(0).getStringCellValue();
            String dat = row.getCell(1).getStringCellValue();
            if (oldDat.contains("0x00") && !dat.contains("0x00")) {
                dat = dat + " 0x00";
            }
            List<Integer> arr = strToByteArray(dat);
            for (int b = 0; b < arr.size(); b++) {
                baos.write((byte) (arr.get(b) & 0xff));
            }
        }
        Files.write(Path.of(outFilename), baos.toByteArray());
    }

    public static void addRow(HSSFSheet sheet, HSSFCellStyle style, String str) {
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString(str));
        cell.setCellStyle(style);
        HSSFCell cell2 = row.createCell(1);
        cell2.setCellValue(new HSSFRichTextString(str));
        cell2.setCellStyle(style);
    }

    private String renderString(byte[] data) {
        String newStr = new String();

        for (int a = 0; a < data.length; a++) {
            int value = (int) (data[a] & 0xffL);
            if (value >= 32) {
                byte[] tmp = new byte[]{data[a]};
                newStr = newStr + new String(tmp, Charset.forName("CP866"));
            } else {
                newStr = String.format("%s0x%02X", newStr, value);
            }
        }
        return newStr;
    }

    private static List<Integer> strToByteArray(String data) {
        List<Integer> ret = new ArrayList<>();
        char[] symbols = data.toCharArray();
        byte[] symbols2 = data.getBytes(Charset.forName("CP866"));
        for (int a = 0; a < symbols2.length; a++) {
            if (a + 3 < symbols2.length) {
                if (symbols2[a] == '0' && symbols2[a + 1] == 'x') {
                    char[] number = {symbols[a], symbols[a + 1], symbols[a + 2], symbols[a + 3]};
                    String hexStr = String.valueOf(number);
                    Integer symbol = Integer.decode(hexStr);
                    ret.add(symbol);
                    a += 3;
                } else {
                    ret.add(Integer.valueOf(symbols2[a]) & 0xff);
                }
            } else {
                ret.add(Integer.valueOf(symbols2[a]) & 0xff);
            }
        }
        return ret;
    }
}
