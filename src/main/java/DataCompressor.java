import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.Offset;
import dto.OffsetType;
import dto.OneString;
import org.apache.commons.lang3.ArrayUtils;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import util.FileUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DataCompressor {
    private int firstOffset = 0x5c310;

    public void compressTexts(File dat, File xls, String outFilename) throws IOException {
        byte[] exe = FileUtils.readAllBytes(dat);
        List<OneString> strings = new ArrayList<>();
        HSSFWorkbook wb = new HSSFWorkbook(Files.newInputStream(xls.toPath()));
        HSSFSheet sheet = wb.getSheetAt(0);
        Gson gson = new Gson();

        Type listType = new TypeToken<ArrayList<Offset>>() {
        }.getType();

        for (int a = 1; a <= sheet.getLastRowNum(); a++) {
            HSSFRow row = sheet.getRow(a);
            OneString string = new OneString();
            if (!row.getCell(4).getStringCellValue().equals(row.getCell(3).getStringCellValue()) ||
                    Boolean.valueOf(row.getCell(5).getStringCellValue())) {
                string.setText(row.getCell(4).getStringCellValue().trim());
                string.setNeedRewrite(Boolean.valueOf(row.getCell(5).getStringCellValue()));
                string.setOldtext(row.getCell(3).getStringCellValue());
                string.setGlobalPosition(Integer.parseInt(row.getCell(0).getStringCellValue()));
                string.setOffsets(gson.fromJson(row.getCell(2).getStringCellValue(), listType));
                strings.add(string);
            }
        }

        int curOffset = firstOffset;
        for (OneString str : strings) {
            //str.setGlobalPosition(curOffset - blockStart);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            string2bytes(str.getText(), baos);
            byte[] bytes = baos.toByteArray();
            byte[] bytesX = new byte[bytes.length];
            for (int a = 0; a < bytesX.length; a++) {
                bytesX[a] = 'X';
            }
            /*if (!isDB(str)) {
                overwrite(exe, bytesX, str.getGlobalPosition());
            } else {
                int a=1;
            }*/
            if (str.getOldtext().length() >= str.getText().length()) {
                //можно перезаписать старую строку
                overwrite(exe, bytes, str.getGlobalPosition());


            } else {
                //надо дописать в конец
                if (isDB(str)) {
                    overwrite(exe, bytesX, str.getGlobalPosition());
                    overwrite(exe, bytes, curOffset);
                    byte[] pointer = calcPointer(curOffset);
                    for (Offset offs : str.getOffsets()) {
                        overwrite(exe, pointer, offs.getOffset());
                    }

                } else {
                    System.out.println(
                            String.format("русская Printf строка длинее чем оригинал. Смещение указателя - %d; Длина - %d; Оригинал -  %s",
                                    str.getOffsets().get(0).getOffset(), str.getOldtext().length(), str.getOldtext()));
                }
                curOffset += bytes.length;

            }
        }
        //overwrite(exe,extraData,firstOffset);
        List<Byte> datList = IntStream.range(0, exe.length).mapToObj(i -> exe[i]).collect(Collectors.toList());
/*
        datList.addAll(zeros);
        datList.addAll(extraData);

        updateHeader(datList);*/
        FileOutputStream fos = new FileOutputStream(new File(outFilename));
        Byte[] bytes = datList.toArray(new Byte[datList.size()]);
        fos.write(ArrayUtils.toPrimitive(bytes));
        fos.flush();
        fos.close();

        System.out.println("Updated " + strings.size() + " strings");
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

    private Boolean isDB(OneString str) {
        return str.getOffsets().get(0).getType() == OffsetType.DB;
    }

    private void updateHeader(List<Byte> datList) {
        int pages = datList.size() / 512;
        int lastPageSize = datList.size() - pages * 512;
        byte[] pagesBytes = ByteBuffer.allocate(4).putInt(pages).array();
        byte[] pageSizeBytes = ByteBuffer.allocate(4).putInt(lastPageSize).array();
        datList.set(0x02, pageSizeBytes[3]);
        datList.set(0x03, pageSizeBytes[2]);
        datList.set(0x04, pagesBytes[3]);
        datList.set(0x05, pagesBytes[2]);
    }

    public static void string2bytes(String str, ByteArrayOutputStream baos) throws UnsupportedEncodingException {
        byte[] bytes = str.getBytes(Charset.forName("cp866"));
        int j = 0;

        for (byte cbrace = "{".getBytes()[0]; j < bytes.length; ++j) {
            if (bytes[j] == cbrace) {
                int nextByte = Integer.parseInt(new String(bytes, j + 1, 1), 16);
                switch (nextByte) {
                    case 1:
                    case 2:
                    case 5:
                    case 6:
                    case 10:
                    case 13:
                    case 14:
                        baos.write(nextByte);
                        j += 2;
                        break;
                    case 3:
                    case 8:
                        baos.write(nextByte);
                        baos.write(bytes[j + 3]);
                        j += 4;
                        break;
                    case 4:
                    case 7:
                    case 9:
                    case 11:
                        baos.write(nextByte);
                        baos.write(bytes, j + 3, 3);
                        j += 6;
                        break;
                    case 12:
                        baos.write(nextByte);
                        if (bytes[j + 3] == 100) {
                            baos.write(bytes, j + 3, 1);
                            j += 4;
                        } else {
                            baos.write(bytes, j + 3, 2);
                            j += 5;
                        }
                        break;
                    default:
                        j += 2;
                }
            } else {
                baos.write(bytes[j]);
            }
        }

        baos.write(0);
    }

    byte[] calcPointer(int offset) {
        byte[] ret = new byte[4];
        int tmpOffset = offset - 0x5470;
        int smallOffset = tmpOffset - ((tmpOffset / 0x10) * 0x10);
        int rightPart = tmpOffset / 0x10;
        byte[] bytes = ByteBuffer.allocate(4).putInt(smallOffset).array();
        ret[0] = bytes[3];
        ret[1] = 0;
        bytes = ByteBuffer.allocate(4).putInt(rightPart).array();
        ret[2] = bytes[3];
        ret[3] = bytes[2];
        return ret;
    }
    /*
    private static int getWord(byte[] b, int index) {
      return getByte(b, index) + (getByte(b, index + 1) << 8);
   }
     */
}
