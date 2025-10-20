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
    private static final String[] binFileNames = {"award.bin",
            "notes.bin",
            "qnotes.bin",
            "quest.bin",
            "special.bin",
            "spldesc.bin",
            "tavern.bin",
            "viewtext.bin"};

    public static void processForumTranslate2(String translateFile, String outFoldPath, int type) throws IOException {

        List<ForumTranslateBlock> translate = readTranslates2(translateFile);
        for (int num = 0; num < translate.size(); num++) {
            String filename = getFilename(num, type);
            if (filename == null) {
                continue;
            }
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
                Files.delete(Path.of(outFoldPath + "/" + filename));
            } catch (Exception ex) {

            }
            bytes.forEach(b -> {
                try {
                    Files.write(Path.of(outFoldPath + "/" + filename), b, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        int a = 10;
    }

    private static List<ForumTranslateBlock> readTranslates2(String filename) throws IOException {
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

    private static String getFilename(int blockNumber, int type) {
        if (type == 0) {
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
                //return "darkmirr.txt";
                return null;
            }
            if (blockNumber >= 230 && blockNumber <= 259) {
                return String.format("darkx%04d.txt", blockNumber - 129);
            }
            if (blockNumber >= 260 && blockNumber <= 344) {
                return String.format("xeen%04d.txt", blockNumber - 259);
            }
            if (blockNumber == 345) {
                //return "xeenmirr.txt";
                return null;
            }
            return "unknown.txt";
        } else {
            if (binFileNames.length < blockNumber - 1) {
                return "unknown.txt";
            }
            return binFileNames[blockNumber];
        }
    }
}
