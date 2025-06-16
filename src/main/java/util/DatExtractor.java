package util;

import dto.Interval;
import dto.Offset;
import dto.OffsetType;
import dto.OneString;
import mapper.OneStringMapper;
import org.apache.poi.hssf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

//старт для перезаписуемых printf 324364 to финиш для перезаписуемых printf 329827
public class DatExtractor {
    private final int blockStart = 0x4C290;
    private final int[] blockStarts = new int[]{0x4f30c, 0x515cc, 0x51e46, 0x54ff8};
    private final int[] blockEnds = new int[]{0x50ea9, 0x51cab, 0x54aab, 0x55106};

    public void extractText(File fil, String newFilename) throws IOException {
        byte[] exe = FileUtils.readAllBytes(fil);
        List<OneString> result = new ArrayList<>();

        List<OneString> mobs = findMobs(exe);

        /*ByteArrayOutputStream baoss = new ByteArrayOutputStream();
        for (int aa = 0x5340e; aa <= 0x5349b; aa++) {
            baoss.write(exe[aa]);
        }

        List<String> str = new ArrayList<>();
        CCTextConverter.bytes2string(str, baoss.toByteArray());*/

        int stringPosition;
        for (int a = 0; a < blockStarts.length; ++a) {
            int position = blockStarts[a];
            int endPosition = blockEnds[a];
            stringPosition = position;


            for (ByteArrayOutputStream baos = new ByteArrayOutputStream(); position < endPosition; ++position) {
                baos.write(exe[position]);
                if (exe[position] != 0 && exe[position - 1] == 0) {
                    stringPosition = position;
                }

                if (exe[position] == 0) {
                    List<String> temp = new ArrayList<>();
                    CCTextConverter.bytes2string(temp, baos.toByteArray());
                    Boolean fn = isFileName(temp.get(0));
                    if (!temp.isEmpty() && !fn) {

                        List<Offset> offsets = OffsetUtils.getOffsets(exe, stringPosition - blockStart);
                        for (Offset ofs : offsets) {
                            OneString oneString = new OneString();
                            oneString.setText(temp.get(0));
                            oneString.setGlobalPosition(stringPosition);
                            oneString.setLocalPosition(stringPosition - blockStart);
                            oneString.setOffsets(List.of(ofs));
                            oneString.setNeedRewrite(false);
                            result.add(oneString);
                        }
                    }
                    baos = new ByteArrayOutputStream();
                }
            }
        }
        List<OneString> resultsFiltered = result.stream().filter(r -> r.getOffsets().size() > 0).collect(Collectors.toList());
        resultsFiltered = result.stream()
                .sorted(Comparator.comparing(s -> s.getOffsets().get(0).getType()))
                .collect(Collectors.toList());
        List<String> dbs = resultsFiltered.stream()
                .filter(s -> s.getOffsets().get(0).getType() == OffsetType.DB).map(OneString::getText).collect(Collectors.toList());
        findBiggerInterval(resultsFiltered.stream()
                .filter(s -> s.getOffsets().get(0).getType() == OffsetType.DB).collect(Collectors.toList()));
        resultsFiltered = resultsFiltered.stream()
                .peek(s -> {
                    s.setNeedRewrite(false);
                    if (s.getOffsets().get(0).getType() == OffsetType.PRINTF && dbs.contains(s.getText())) {
                        s.setNeedRewrite(true);
                    }
                    if (s.getOffsets().get(0).getType() == OffsetType.DB) {
                        s.setNeedRewrite(true);
                    }
                }).collect(Collectors.toList());

        resultsFiltered.addAll(mobs);
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFCellStyle style = wb.createCellStyle();
        style.setWrapText(true);
        HSSFSheet sheet = wb.createSheet(fil.getName());

        createHeader(style, sheet);
        for (stringPosition = 0; stringPosition < resultsFiltered.size(); ++stringPosition) {
            OneStringMapper.OneStringToRow(sheet, style, resultsFiltered.get(stringPosition));
        }
        FileOutputStream fos = new FileOutputStream(new File(newFilename));
        wb.write(fos);
        fos.close();
        System.out.println("Extracted " + result.size() + " strings");
    }

    private List<OneString> findMobs(byte[] exe) throws UnsupportedEncodingException {
        int begin = 0x44e00;
        int size = begin + 0x6d70;
        List<OneString> ret = new ArrayList<>();
        for (int a = begin; a < size; a += 60) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (int b = 0; b < 16; b++) {
                baos.write(exe[a + b]);
            }
            List<String> temp = new ArrayList<>();
            CCTextConverter.bytes2string(temp, baos.toByteArray());
            if (!temp.isEmpty() && !temp.get(0).isEmpty()) {
                OneString str = new OneString();
                str.setText(temp.get(0));
                str.setOldtext(temp.get(0));
                str.setGlobalPosition(a);
                Offset offs = new Offset();
                offs.setType(OffsetType.MOB);
                str.setOffsets(List.of(offs));
                ret.add(str);
            }
        }
        return ret;
    }

    private void findBiggerInterval(List<OneString> list) {
        List<Interval> intervals = new ArrayList<>();
        if (list.isEmpty()) {
            return;
        }
        list = list.stream().sorted((s1, s2) -> s1.getGlobalPosition().compareTo(s2.getGlobalPosition())).collect(Collectors.toList());
        Interval curInterval = new Interval();
        curInterval.setStart(list.get(0).getGlobalPosition());
        OneString prevstr = null;
        for (OneString str : list) {
            if (prevstr == null) {
                prevstr = str;
            } else {
                if (str.getGlobalPosition() > prevstr.getGlobalPosition() + prevstr.getText().length() + 1) {
                    curInterval.setFinish(prevstr.getGlobalPosition());
                    intervals.add(curInterval);
                    System.out.println("Interval found from:" + curInterval.getStart() + " to " + curInterval.getFinish() + ". Size=" + (curInterval.getFinish() - curInterval.getStart()));
                    curInterval = new Interval();
                    curInterval.setStart(str.getGlobalPosition());

                }
                prevstr = str;
            }
        }
        int a = 1;
    }

    private Boolean isFileName(String str) {
        String[] ends = {".txt", ".obj", ".txt", ".obj", ".voc", ".vga", ".int",
                ".pal", ".raw", ".sav", ".sky", ".xen", ".exe", ".icn",
                ".bin", ".twn", ".cur", ".nam", ".chr", ".m",
                ".mon", ".att", ".pty", ".zom", ".brd", ".mm4", ".cc", ".buf"};
        for (String end : ends) {
            if (str.endsWith(end)) {
                return true;
            }
        }
        return false;
    }

    private void createHeader(HSSFCellStyle style, HSSFSheet sheet) {
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString("Global position"));
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString("Local position"));
        cell.setCellStyle(style);

        cell = row.createCell(2);
        cell.setCellValue(new HSSFRichTextString("Code offsets"));
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue(new HSSFRichTextString("Eng. text"));
        cell.setCellStyle(style);

        cell = row.createCell(4);
        cell.setCellValue(new HSSFRichTextString("Rus. text"));
        cell.setCellStyle(style);
        cell = row.createCell(5);
        cell.setCellValue(new HSSFRichTextString("Must be rewritten"));
        cell.setCellStyle(style);
        sheet.setColumnWidth(0, 2560);
        sheet.setColumnWidth(1, 2560);
        sheet.setColumnWidth(2, 15360);
        sheet.setColumnWidth(3, 15360);
        sheet.setColumnWidth(4, 15360);
        sheet.setColumnWidth(5, 2560);
    }
}