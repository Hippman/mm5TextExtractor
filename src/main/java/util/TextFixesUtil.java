package util;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import dto.ForumTranslateBlock;
import dto.ForumTranslateRow;
import org.apache.poi.hssf.usermodel.*;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

public class TextFixesUtil {

    public static void processForumTranslate(String translateFile, String foldpath, String ruFoldPath, String outFoldPath) throws IOException {
        XenFileWorker xenFileWorker = new XenFileWorker();
        List<ForumTranslateBlock> translate = readTranslates(translateFile);
        File folder = new File(foldpath);
        File[] files = folder.listFiles();
        Arrays.stream(files).forEach(f -> {
            try {
                HSSFWorkbook wb = extractTexts(f);
                File ruFile = new File(ruFoldPath + "\\" + f.getName());
                HSSFWorkbook ruWb = null;
                if (f.getName().equals("quest.bin")) {
                    int a = 0;
                }
                if (ruFile.exists()) {
                    ruWb = extractTexts(ruFile);
                    //ruWb = xenFileWorker.extractTexts(ruFile);
                }

                processFile(wb, translate, ruWb, f.getName());
                xenFileWorker.compressTexts(wb, outFoldPath + "\\" + f.getName());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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
        int a = 0;
    }

    private static List<ForumTranslateBlock> readTranslates(String filename) throws FileNotFoundException, UnsupportedEncodingException {
        FileReader fr = new FileReader(filename);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "WINDOWS-1251"));

        List<ForumTranslateBlock> ret = new ArrayList<>();

        String str = "";
        ForumTranslateBlock curBlock = new ForumTranslateBlock();
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
            if (parts.length < 3 || parts[1].isEmpty() || parts[1].equals("|")) {
                continue;
            }
            if (parts[1].equals("====")) {
                ret.add(curBlock);
                curBlock = new ForumTranslateBlock();
            } else {
                ForumTranslateRow row = new ForumTranslateRow(parts);
                curBlock.getRows().add(row);
            }
        }
        return ret;
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
