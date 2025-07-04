package util;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import dto.ForumTranslateBlock;
import dto.ForumTranslateRow;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;


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
                HSSFWorkbook wb = xenFileWorker.extractTexts(f);
                File ruFile = new File(ruFoldPath + "\\" + f.getName());
                HSSFWorkbook ruWb = null;
                if (ruFile.exists()) {
                    ruWb = xenFileWorker.extractTexts(ruFile);
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
                .filter(b -> b.getRows().stream().anyMatch(r -> firstData.contains(r.getOriginal())))
                .filter(b -> b.getRows().size() <= sheet.getLastRowNum() + 10)
                .findFirst().orElse(null);
        if (block != null) {
            System.out.println("▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒");
            System.out.println(String.format("filename %s block found", filename));
            for (int a = 1; a <= sheet.getLastRowNum(); a++) {
                HSSFRow row = sheet.getRow(a);
                ForumTranslateRow tRow = block.getRows().stream()
                        .filter(r -> row.getCell(0).getStringCellValue().contains(r.getOriginal())).findFirst().orElse(null);
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
}
