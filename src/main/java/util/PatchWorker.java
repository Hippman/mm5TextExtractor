package util;

import dto.PatchLine;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PatchWorker {
    private final int thresholdSize = 8;

    public void makePatch(File file1, File file2, int offsetFrom, int offsetTo) throws IOException {
        List<PatchLine> lines = new ArrayList<>();
        byte[] file1Data = FileUtils.readAllBytes(file1);
        byte[] file2Data = FileUtils.readAllBytes(file2);
        PatchLine bufLine = null;
        Boolean flag = false;
        for (int a = offsetFrom; a < offsetTo; a++) {
            if (file1Data[a] != file2Data[a]) {
                if (!flag) {
                    bufLine = new PatchLine();
                    bufLine.setOffset(a);
                    addPrefix(bufLine, file1Data, file2Data, a);
                    bufLine.getOldData().add(file1Data[a]);
                    bufLine.getNewData().add(file2Data[a]);
                    lines.add(bufLine);
                    flag = true;
                } else {
                    bufLine.getOldData().add(file1Data[a]);
                    bufLine.getNewData().add(file2Data[a]);
                }
            } else if (file1Data[a] == file2Data[a] && flag) {
                addPrefix(bufLine, file1Data, file2Data, a + thresholdSize);
                flag = false;
            }
        }
        writeResults(lines, "patch.xls");
    }

    public void applyPatch(File filIn, File filOut, File patchPath) throws IOException {
        byte[] fileIn = FileUtils.readAllBytes(filIn);
        byte[] fileOut = FileUtils.readAllBytes(filIn);
        HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(patchPath.toPath()));
        HSSFSheet sheet = wb.getSheetAt(0);
        for (int a = 1; a <= sheet.getLastRowNum(); a++) {
            HSSFRow row = sheet.getRow(a);
            if (!row.getCell(3).getStringCellValue().equals("true")) {
                continue;
            }
            List<Integer> origData = strToByteArray(row.getCell(1).getStringCellValue().replaceAll(" ", ""));
            List<Integer> newData = strToByteArray(row.getCell(2).getStringCellValue().replaceAll(" ", ""));
            int dataIterator = 0;
            for (int b = 0; b < fileIn.length; b++) {
                Integer value = Integer.valueOf(fileIn[b]) & 0xff;
                if (value.equals(origData.get(dataIterator))) {
                    if (dataIterator + 1 == origData.size()) {
                        //Применяем патч
                        int offset = b - origData.size();
                        for (int c = 0; c < origData.size(); c++) {
                            value = newData.get(c);
                            byte[] bytes = ByteBuffer.allocate(4).putInt(value).array();
                            fileOut[offset + c] = bytes[3];
                        }
                        b = fileIn.length;
                    } else {
                        dataIterator++;
                    }
                } else {
                    dataIterator = 0;
                }
            }
        }
        Files.write(filOut.toPath(), fileOut);
    }

    private void addPrefix(PatchLine line, byte[] file1Data, byte[] file2Data, int offset) {
        for (int a = offset - thresholdSize; a < offset; a++) {
            line.getOldData().add(file1Data[a]);
            line.getNewData().add(file2Data[a]);
        }
    }

    private void writeResults(List<PatchLine> lines, String outFilename) throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        HSSFSheet sheet = wb.createSheet("Patch");
        createHeader(style, sheet);
        lines.forEach(l -> PatchLineToRow(sheet, style, l));
        FileOutputStream fos = new FileOutputStream(new File(outFilename));
        wb.write(fos);
        fos.close();
    }

    private void createHeader(HSSFCellStyle style, HSSFSheet sheet) {
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString("Global position"));
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("original data"));
        cell.setCellStyle(style);

        cell = row.createCell(2);
        cell.setCellValue(new HSSFRichTextString("new data"));
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue(new HSSFRichTextString("enabled"));
        cell.setCellStyle(style);

        sheet.setColumnWidth(0, 2560);
        sheet.setColumnWidth(1, 15360);
        sheet.setColumnWidth(2, 15360);
        sheet.setColumnWidth(3, 2560);
    }

    public static void PatchLineToRow(HSSFSheet sheet, HSSFCellStyle style, PatchLine str) {
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(str.getOffset())));
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString(
                String.valueOf(renderString(ArrayUtils.toPrimitive((Byte[]) str.getOldData().toArray(new Byte[str.getOldData().size()]))))));
        cell.setCellStyle(style);

        cell = row.createCell(2);
        cell.setCellValue(new HSSFRichTextString(
                String.valueOf(renderString(ArrayUtils.toPrimitive((Byte[]) str.getNewData().toArray(new Byte[str.getNewData().size()]))))));
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue(new HSSFRichTextString(
                String.valueOf(str.getEnabled())));
        cell.setCellStyle(style);
    }

    public static String renderString(byte[] data) {
        String newStr = new String();

        for (int a = 0; a < data.length; a++) {
            int value = (int) (data[a] & 0xffL);
            newStr = String.format("%s 0x%02X", newStr, value);
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
