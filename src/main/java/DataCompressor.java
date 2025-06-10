import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.Offset;
import dto.OneString;
import org.apache.commons.lang3.ArrayUtils;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import util.DataUtils;
import util.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataCompressor {
    private int firstOffset = 0x5c310;

    public void compressTexts(File dat, File xls, String outFilename) throws IOException {
        byte[] exe = FileUtils.readAllBytes(dat);
        List<OneString> stringsDb = new ArrayList<>();
        List<OneString> stringsPrintf = new ArrayList<>();
        HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(xls.toPath()));
        HSSFSheet sheet = wb.getSheetAt(0);
        Gson gson = new Gson();

        Type listType = new TypeToken<ArrayList<Offset>>() {
        }.getType();

        for (int a = 1; a <= sheet.getLastRowNum(); a++) {
            HSSFRow row = sheet.getRow(a);
            OneString string = new OneString();
            try {
                if (!row.getCell(4).getStringCellValue().equals(row.getCell(3).getStringCellValue()) ||
                        Boolean.valueOf(row.getCell(5).getStringCellValue())) {
                    string.setText(row.getCell(4).getStringCellValue().trim());
                    string.setNeedRewrite(Boolean.valueOf(row.getCell(5).getStringCellValue()));
                    string.setOldtext(row.getCell(3).getStringCellValue());
                    string.setGlobalPosition(Integer.parseInt(row.getCell(0).getStringCellValue()));
                    string.setOffsets(gson.fromJson(row.getCell(2).getStringCellValue(), listType));
                    if (string.isDB()) {
                        stringsDb.add(string);
                    } else {
                        stringsPrintf.add(string);
                    }
                }
            } catch (Exception ex) {
                System.out.println(String.valueOf(a));
            }

        }
        Integer curOffset = firstOffset;
        processDb(exe, stringsDb, curOffset);
        processPrintf(exe, stringsPrintf);
        List<Byte> datList = IntStream.range(0, exe.length).mapToObj(i -> exe[i]).collect(Collectors.toList());
        FileOutputStream fos = new FileOutputStream(new File(outFilename));
        Byte[] bytes = datList.toArray(new Byte[datList.size()]);
        fos.write(ArrayUtils.toPrimitive(bytes));
        fos.flush();
        fos.close();

        System.out.println("Updated " + stringsDb.size() + " DB strings");
        System.out.println("Updated " + stringsPrintf.size() + " Printf strings");
    }

    private void processDb(byte[] exe, List<OneString> dbStrings, Integer curOffset) throws UnsupportedEncodingException {
        for (OneString str : dbStrings) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            str.setText(str.getText());
            DataUtils.string2bytes(str.getText(), baos);
            str.setText(str.getText());
            byte[] bytes = baos.toByteArray();
            if (str.getOldtext().length() >= str.getText().length() &&
                    str.getOldtext().equals(str.getText())) {
                //можно перезаписать старую строку
                overwrite(exe, bytes, str.getGlobalPosition());
            } else {
                //надо дописать в конец
                if (str.getNeedRewrite()) {
                    overwrite(exe, bytes, curOffset);
                    byte[] pointer = DataUtils.calcDbPointer(curOffset);
                    for (Offset offs : str.getOffsets()) {
                        overwrite(exe, pointer, offs.getOffset());
                    }
                    curOffset += bytes.length;

                } else {
                    System.out.println(
                            String.format("русская DB строка длинее чем оригинал. И указано, что ее нельзя переносить. Смещение указателя - %d; Длина - %d; Оригинал -  %s",
                                    str.getOffsets().get(0).getOffset(), str.getOldtext().length(), str.getOldtext()));
                }
            }
        }
    }

    private void processPrintf(byte[] exe, List<OneString> printfStrings) throws UnsupportedEncodingException {
        for (OneString str : printfStrings) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            str.setText(str.getText());
            DataUtils.string2bytes(str.getText(), baos);
            str.setText(str.getText());

            byte[] bytes = baos.toByteArray();

            if (str.getOldtext().length() >= str.getText().length()) {
                //можно перезаписать старую строку
                overwrite(exe, bytes, str.getGlobalPosition());
            } else {
                System.out.println(
                        String.format("русская Printf строка длинее чем оригинал. Смещение указателя - %d; Длина - %d; Оригинал -  %s",
                                str.getOffsets().get(0).getOffset(), str.getOldtext().length(), str.getOldtext()));
            }
        }
    }

    private void overwrite(byte[] exe, byte[] data, int offset) {
        for (int a = 0; a < data.length; a++) {
            exe[a + offset] = data[a];
        }
    }

    private void overwrite(byte[] exe, List<Byte> data, int offset) {
        for (int a = 0; a < data.size(); a++) {
            exe[a + offset] = data.get(a);
        }
    }

}
