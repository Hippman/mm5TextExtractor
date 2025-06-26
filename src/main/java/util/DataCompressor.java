package util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.Offset;
import enums.OffsetType;
import dto.OneString;
import dto.TextInterval;
import org.apache.commons.lang3.ArrayUtils;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.poi.ss.usermodel.CellType.STRING;

public class DataCompressor {
    private int firstOffset = 0x5c310;

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
        processMob(exe, stringMob);
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

    private void processMob(byte[] exe, List<OneString> mobStrings) throws UnsupportedEncodingException {
        for (OneString str : mobStrings) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataUtils.string2bytes(str.getText(), baos);
            str.setNewBytes(strToByte(str.getText()));
            if (str.getText().length() <= 16) {
                overwrite(exe, str.getNewBytes(), str.getGlobalPosition());
            } else {
                String.format("русская MOB строка длинее чем 16 символов. Строка -  %s", str.getText());
            }
        }
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
        List<TextInterval> intervals = new ArrayList<>();
        printfStrings = printfStrings.stream().filter(s -> !s.getNeedRewrite()).collect(Collectors.toList());
        //Соберем список свободных интервалов
        for (OneString str : printfStrings) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataUtils.string2bytes(str.getText(), baos);
            str.setOldBytes(strToByte(str.getOldtext()));
            str.setNewBytes(strToByte(str.getText()));

            TextInterval interval = new TextInterval(str.getGlobalPosition(), str.getOldBytes().length);
            if (intervals.isEmpty()) {
                intervals.add(interval);
            } else {
                Boolean status = intervals.stream().map(i -> i.unityIntervals(interval)).filter(s -> s).findAny().orElse(false);
                if (!status) {
                    intervals.add(interval);
                }
            }
        }
        //Отсортируем интервалы по убыванию объема
        intervals = sortIntervals(intervals);

        //отсортируем строки по уменьшению размера нового текста
        /*
        тут мы отделяем DBPRINTF от остальных и выдаем им отдеольный инервал, который ближе всего к их началу и точно вместит изменения
        это сейчас самый большой
         */
        List<OneString> strsDB = printfStrings.stream().filter(s -> s.getOffsets().get(0).getType() == OffsetType.DBPRINTF).sorted(Comparator.comparing(OneString::getNewSize).reversed()).collect(Collectors.toList());
        List<OneString> strsNDB = printfStrings.stream().filter(s -> s.getOffsets().get(0).getType() != OffsetType.DBPRINTF).sorted(Comparator.comparing(OneString::getNewSize).reversed()).collect(Collectors.toList());
        if (!intervals.isEmpty()) {
            TextInterval outinterval = intervals.get(intervals.size() - 1);
            printfStrings = new ArrayList<>();
            printfStrings.addAll(strsNDB);
            printfStrings.addAll(strsDB);
            subprocessPrintfs(strsDB, exe, List.of(outinterval));
            subprocessPrintfs(strsNDB, exe, intervals);
        }
    }

    private void subprocessPrintfs(List<OneString> printfStrings, byte[] exe, List<TextInterval> intervals) throws Exception {
        for (OneString str : printfStrings) {
            if (!str.checkPercents()) {
                System.out.printf("!! В строке неверное количество символов %%. Строка оригинал %s%n", str.getOldtext());
                continue;
            }
            if (str.getProcessed() || str.getNeedRewrite()) {
                continue;
            }
            byte[] pointer = DataUtils.calcPrintfPointer(str.getGlobalPosition());
            TextInterval interval = intervals.stream().filter(i -> i.getSize() >= str.getNewSize()).findFirst().orElse(null);
            if (interval == null) {
                System.out.println(
                        String.format("!! русская Printf строка не влезает ни в один из интервалов. Смещение указателя - %d; Длина - %d; Оригинал -  %s",
                                str.getOffsets().get(0).getOffset(), str.getOldtext().length(), str.getOldtext()));
                throw (new Exception("!! Не хватает места"));
            }
            if (str.getGlobalPosition() != interval.getStart()) {
                //System.out.println(String.format("Строка [%s] передвинута", str.getText()));
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
                case DBPRINTF: {
                    pointer = DataUtils.calcDbPrintfPointer(str.getGlobalPosition());
                    break;
                }
            }


            List<OneString> sameStrings = printfStrings.stream().filter(s -> s.getGlobalPosition().equals(str.getGlobalPosition())
                    && s.getOffsets().get(0).getOffset() != str.getOffsets().get(0).getOffset()).collect(Collectors.toList());
            if (!sameStrings.isEmpty()) {
                for (OneString str2 : sameStrings) {
                    for (Offset offs : str2.getOffsets()) {
                        overwrite(exe, pointer, offs.getOffset());
                    }
                    str2.setProcessed(true);
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
