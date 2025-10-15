package util;

import dto.Interval;
import dto.Offset;
import dto.OneString;
import enums.OffsetType;
import mapper.OneStringMapper;
import org.apache.poi.hssf.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class ExeDatExtractor {
    private final int dataSegmentStart = 0x1c6d0;
    private final int[] blockStarts = new int[]{0x1c6d0};
    private final int[] blockEnds = new int[]{0x20430};


    public void extractText(File fil, String newFilename) throws IOException {
        byte[] exe = FileUtils.readAllBytes(fil);
        List<OneString> result = new ArrayList<>();



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
                    try {
                        CCTextConverter.bytes2string(temp, baos.toByteArray());
                    } catch (Exception ex){
                        ex.printStackTrace();
                        continue;
                    }
                    Boolean fn = isFileName(temp.get(0));
                    if (!temp.isEmpty() && !fn) {

                        List<Offset> offsets = OffsetUtils.getOffsets(exe, stringPosition - dataSegmentStart);
                        if(offsets.size()>0){
                            OneString oneString = new OneString();
                            oneString.setText(temp.get(0));
                            oneString.setGlobalPosition(stringPosition);
                            oneString.setLocalPosition(stringPosition - dataSegmentStart);
                            oneString.setOffsets(offsets);
                            oneString.setNeedRewrite(false);
                            result.add(oneString);
                        }
                        //}
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