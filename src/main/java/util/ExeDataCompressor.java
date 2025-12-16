package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.Offset;
import dto.OneString;
import dto.TextInterval;
import enums.OffsetType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.poi.ss.usermodel.CellType.STRING;

public class ExeDataCompressor {
    private int firstOffset = 0x217b1;

    public void compressTexts(File dat, File xls, String outFilename) throws Exception {
        byte[] exe = FileUtils.readAllBytes(dat);
        List<OneString> stringsDb = new ArrayList<>();
        List<OneString> stringsPrintf = new ArrayList<>();
        List<OneString> stringMob = new ArrayList<>();
        HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(xls.toPath()));
        HSSFSheet sheet = wb.getSheetAt(0);
        Gson gson = new Gson();

        Type listType = new TypeToken<ArrayList<Offset>>() {
        }.getType();

        for (int a = 1; a <= sheet.getLastRowNum(); a++) {
            HSSFRow row = sheet.getRow(a);
            OneString string = new OneString();
            if (row == null) {
                continue;
            }
            try {

                if (!row.getCell(4).getStringCellValue().equals(row.getCell(3).getStringCellValue()) ||
                        Boolean.valueOf(row.getCell(5).getStringCellValue())) {
                    string.setText(row.getCell(4).getStringCellValue().trim());
                    if (string.getText().isEmpty()) {
                        string.setText(" ");
                    }
                    string.setNeedRewrite(Boolean.valueOf(row.getCell(5).getStringCellValue()));
                    string.setOldtext(row.getCell(3).getStringCellValue());
                    if (row.getCell(0).getCellType() == STRING) {
                        string.setGlobalPosition(Integer.parseInt(row.getCell(0).getStringCellValue()));
                    } else {
                        string.setGlobalPosition(Double.valueOf(row.getCell(0).getNumericCellValue()).intValue());
                    }
                    string.setOffsets(gson.fromJson(row.getCell(2).getStringCellValue(), listType));
                    if (string.isDB()) {
                        stringsDb.add(string);
                    } else {
                        if (string.getOffsets() != null
                                && (string.getOffsets().get(0).getType() == OffsetType.PRINTF
                                || string.getOffsets().get(0).getType() == OffsetType.PRINTFB8
                                || string.getOffsets().get(0).getType() == OffsetType.DBPRINTF
                                || string.getOffsets().get(0).getType() == OffsetType.PRINTFNOB
                        )) {
                            stringsPrintf.add(string);
                        }
                        if (string.getOffsets() != null
                                && (string.getOffsets().get(0).getType() == OffsetType.MOB)) {
                            stringMob.add(string);
                        }
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
            DataUtils.string2bytes(str.getText(), baos);
            str.setNewBytes(strToByte(str.getText()));
            if (str.getOldtext().length() >= str.getText().length()) {
                //можно перезаписать старую строку
                overwrite(exe, str.getNewBytes(), str.getGlobalPosition());
            } else {
                //надо дописать в конец
                if (str.getNeedRewrite()) {
                    overwrite(exe, str.getNewBytes(), curOffset);
                    byte[] pointer = DataUtils.calcDbPointer(curOffset);
                    for (Offset offs : str.getOffsets()) {
                        overwrite(exe, pointer, offs.getOffset());
                    }
                    System.out.println(String.format("DB [%s] передвинута", str.getText()));

                    curOffset += str.getNewBytes().length;

                } else {
                    System.out.println(
                            String.format("русская DB строка длинее чем оригинал. И указано, что ее нельзя переносить. Смещение указателя - %d; Длина - %d; Длина оригинала %d; Оригинал -  %s",
                                    str.getOffsets().get(0).getOffset(), str.getText().length(), str.getOldtext().length(), str.getOldtext()));
                }
            }
        }
    }

    private void processPrintf(byte[] exe, List<OneString> printfStrings) throws Exception {
        printfStrings.stream().filter(OneString::getNeedRewrite).forEach(str -> {
            if (str.getText().length() <= str.getOldtext().length()) {
                try {
                    str.setNewBytes(strToByte(str.getText()));
                    //System.out.println(String.format("Строка [%s] перезаписана", str.getText()));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                overwrite(exe, str.getNewBytes(), str.getGlobalPosition());
            } else {
                System.out.printf("!!! Не могу перезаписать строку, она длинее оригинала. Строка оригинал %s", str.getOldtext());
            }
        });

        List<TextInterval> intervalsNdb = new ArrayList<>();
        printfStrings = printfStrings.stream().filter(s -> !s.getNeedRewrite()).collect(Collectors.toList());
        List<OneString> strsNDB = printfStrings.stream().filter(s -> s.getOffsets().get(0).getType() != OffsetType.DBPRINTF).sorted(Comparator.comparing(OneString::getNewSize).reversed()).collect(Collectors.toList());
        //Соберем список свободных интервалов
        TextInterval lastInterval = new TextInterval(firstOffset,5*1024);
        intervalsNdb.add(lastInterval);
        for (OneString str : strsNDB) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataUtils.string2bytes(str.getText(), baos);
            str.setOldBytes(strToByte(str.getOldtext()));
            str.setNewBytes(strToByte(str.getText()));

            TextInterval interval = new TextInterval(str.getGlobalPosition(), str.getOldBytes().length);
            if (intervalsNdb.isEmpty()) {
                intervalsNdb.add(interval);
            } else {
                Boolean status = intervalsNdb.stream().map(i -> i.unityIntervals(interval)).filter(s -> s).findAny().orElse(false);
                if (!status) {
                    intervalsNdb.add(interval);
                }
            }
        }

        //отсортируем строки по уменьшению размера нового текста

        if (!intervalsNdb.isEmpty()) {
            printfStrings = new ArrayList<>();
            printfStrings.addAll(strsNDB);
            intervalsNdb = sortIntervals(intervalsNdb);
            subprocessPrintfs(strsNDB, exe, intervalsNdb);
        }
    }

    private void subprocessPrintfs(List<OneString> printfStrings, byte[] exe, List<TextInterval> intervals) throws Exception {
        printfStrings = sortStrings(printfStrings);
        for (int a = 0; a < printfStrings.size(); a++) {
            OneString str = printfStrings.get(a);
            if (!str.checkPercents()) {
                System.out.printf("!! В строке неверное количество символов %%. Строка оригинал %s%n", str.getOldtext());
                continue;
            }
            if (str.getProcessed() || str.getNeedRewrite()) {
                continue;
            }
            byte[] pointer = DataUtils.calcExePrintfPointer(str.getGlobalPosition());
            TextInterval interval = intervals.stream().filter(i -> i.getSize() >= str.getNewSize()).findFirst().orElse(null);
            if (interval == null) {
                System.out.println(
                        String.format("!! русская Printf строка не влезает ни в один из интервалов. Смещение указателя - %d; Длина - %d; Оригинал -  %s",
                                str.getOffsets().get(0).getOffset(), str.getOldtext().length(), str.getOldtext()));
                throw (new Exception("!! Не хватает места"));
            }

            str.setGlobalPosition(interval.getStart());
            interval.shrinkFromStart(str.getNewSize());
            intervals = sortIntervals(intervals);
            switch (str.getOffsets().get(0).getType()) {
                case PRINTF: {
                    pointer = DataUtils.calcPrintfPointer(str.getGlobalPosition());
                    break;
                }
                case PRINTFB8: {
                    pointer = DataUtils.calcPrintfB8Pointer(str.getGlobalPosition());
                    break;
                }
                case PRINTFNOB: {
                    pointer = DataUtils.calcPrintfNOBPointer(str.getGlobalPosition());
                    break;
                }
                case DBPRINTF: {
                    pointer = DataUtils.calcDbPrintfPointer(str.getGlobalPosition());
                    break;
                }
            }
            for (Offset offs : str.getOffsets()) {
                overwrite(exe, pointer, offs.getOffset());
            }
            overwrite(exe, str.getNewBytes(), str.getGlobalPosition());
            str.setProcessed(true);
        }

    }

    List<TextInterval> sortIntervals(List<TextInterval> intervals) {
        return intervals.stream().filter(i -> i.getSize() > 0).sorted(Comparator.comparing(TextInterval::getSize)).collect(Collectors.toList());
    }

    List<OneString> sortStrings(List<OneString> strings) {
        return strings.stream().filter(i -> i.getNewSize() > 0).sorted((f1, f2) -> Long.compare(f2.getNewSize(), f1.getNewSize())).collect(Collectors.toList());
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

    private byte[] strToByte(String str) throws UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataUtils.string2bytes(str, baos);
        return baos.toByteArray();
    }
}
