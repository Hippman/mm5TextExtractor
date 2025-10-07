package util;

import dto.ForumTranslateBlock;
import dto.ForumTranslateRow;
import org.apache.poi.hssf.usermodel.*;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextFixesUtil {


    public static void processForumTranslate2(String translateFile, String outFoldPath) throws IOException {

        List<ForumTranslateBlock> translate = readTranslates2(translateFile);
        for (int num = 0; num < translate.size(); num++) {
            String filename = getFilename(num);
            List<byte[]> bytes = new ArrayList<>();

            translate.get(num).getRows().forEach(row -> {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    DataUtils.string2bytes(row.getTranslated(), baos);
                    byte[] localBytes = baos.toByteArray();
                    bytes.add(localBytes);

                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                Files.delete(Path.of(filename));
            } catch (Exception ex) {

            }
            bytes.forEach(b -> {
                try {
                    Files.write(Path.of(filename), b, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        int a = 10;
    }

    private static List<ForumTranslateBlock> readTranslates2(String filename) throws IOException {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "WINDOWS-1251"));

        List<ForumTranslateBlock> ret = new ArrayList<>();

        String str = "";
        ForumTranslateBlock curBlock = new ForumTranslateBlock();
        str = br.readLine();
        int rowNum = 0;
        while (str != null) {
            try {
                str = br.readLine();
            } catch (IOException e) {
                return ret;
            }
            System.out.println(str);
            if (str == null || str.isEmpty()) {
                continue;
            }
            String[] parts = str.split(Pattern.quote("|"));

            if (str.contains("====")) {
                str = br.readLine();
                ret.add(curBlock);
                curBlock = new ForumTranslateBlock();
                rowNum = 0;
            } else {

                ForumTranslateRow row = new ForumTranslateRow(parts, rowNum);
                curBlock.getRows().add(row);
                rowNum++;
            }
        }
        return ret;
    }

    private static String getFilename(int blockNumber) {
        if (blockNumber >= 0 && blockNumber <= 98) {
            return String.format("aaze%04d.txt", blockNumber + 1);
        }
        if (blockNumber == 99) {
            return "aaze2121.txt";
        }
        if (blockNumber >= 100 && blockNumber <= 129) {
            return String.format("aazex%04d.txt", blockNumber);
        }
        if (blockNumber >= 130 && blockNumber <= 228) {
            return String.format("dark%04d.txt", blockNumber - 129);
        }
        if (blockNumber == 229) {
            return "darkmirr.txt";
        }
        if (blockNumber >= 230 && blockNumber <= 259) {
            return String.format("darkx%04d.txt", blockNumber - 129);
        }
        if (blockNumber >= 260 && blockNumber <= 344) {
            return String.format("xeen%04d.txt", blockNumber - 259);
        }
        if (blockNumber == 345) {
            return "xeenmirr.txt";
        }
        return "unknown.txt";
    }

    private static void processFile(HSSFWorkbook wb, List<ForumTranslateBlock> translate, HSSFWorkbook ruWb, String filename) {
        HSSFSheet sheet = wb.getSheetAt(0);
        String firstData = sheet.getRow(1).getCell(0).getStringCellValue();
        ForumTranslateBlock block = translate.stream()
                .filter(b -> !b.getRows().isEmpty() && firstData.contains(b.getRows().get(0).getOriginal()))
                .filter(b -> b.getRows().size() <= sheet.getLastRowNum() + 10)
                .findFirst().orElse(null);
        if (block != null) {
            System.out.println("▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒");
            System.out.println(String.format("filename %s block found", filename));
            for (int a = 1; a <= sheet.getLastRowNum(); a++) {
                HSSFRow row = sheet.getRow(a);
                ForumTranslateRow tRow = block.getRows().stream()
                        .filter(r -> row.getCell(0).getStringCellValue().contains(r.getOriginal()))
                        .filter(r -> {
                            double len = r.getOriginal().replaceAll("0x00", "").length();
                            double rowLen = row.getCell(0).getStringCellValue().length();
                            double dif = Math.abs(len - rowLen);
                            double avg = (len + rowLen) / 2.0;
                            return (dif / avg * 100) < 40.0;
                        })
                        .findFirst().orElse(null);
                if (tRow != null) {
                    String translated = row.getCell(0).getStringCellValue().replaceAll(Pattern.quote(tRow.getOriginal()), tRow.getTranslated());
                    row.getCell(1).setCellValue(new HSSFRichTextString(translated));
                } else {
                    if (ruWb != null && ruWb.getSheetAt(0) != null && ruWb.getSheetAt(0).getRow(a) != null) {
                        if (!row.getCell(0).getStringCellValue().equals("0x00")) {
                            System.out.println(String.format("filename %s row %s FROM RUFILE", filename, row.getCell(0).getStringCellValue()));
                        }
                        HSSFRow ruRow = ruWb.getSheetAt(0).getRow(a);
                        row.getCell(1).setCellValue(new HSSFRichTextString(ruRow.getCell(1).getStringCellValue()));
                    } else {
                        System.out.println(String.format("filename %s row %s ORIGINAL", filename, row.getCell(0).getStringCellValue()));
                    }
                }
            }
        } else {
            System.out.println("▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒");
            System.out.println(String.format("filename %s block not found", filename));
            for (int a = 1; a <= sheet.getLastRowNum(); a++) {
                HSSFRow row = sheet.getRow(a);
                if (ruWb != null && ruWb.getSheetAt(0) != null && ruWb.getSheetAt(0).getRow(a) != null) {
                    HSSFRow ruRow = ruWb.getSheetAt(0).getRow(a);
                    row.getCell(1).setCellValue(new HSSFRichTextString(ruRow.getCell(1).getStringCellValue()));
                } else {
                    System.out.println(String.format("filename %s row %s ORIGINAL", filename, row.getCell(0).getStringCellValue()));
                }
            }
        }

    }


    public static HSSFWorkbook extractTexts(File fil) throws IOException {
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
            if ((file[a] & 0xffL) == 0x00) {
                String temp = XenFileWorker.renderString(baos.toByteArray());
                if (!temp.isEmpty()) {
                    XenFileWorker.addRow(sheet, style, temp);
                }
                baos = new ByteArrayOutputStream();
            }
        }
        return wb;
    }
}
