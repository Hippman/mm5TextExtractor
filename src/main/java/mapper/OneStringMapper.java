package mapper;

import com.google.gson.Gson;
import dto.OneString;
import org.apache.poi.hssf.usermodel.*;

public class OneStringMapper {
    public static void OneStringToRow(HSSFSheet sheet, HSSFCellStyle style, OneString str){
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        HSSFCell cell = row.createCell(0);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(str.getGlobalPosition())));
        cell.setCellStyle(style);

        cell = row.createCell(1);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(str.getLocalPosition())));
        cell.setCellStyle(style);

        Gson gson=new Gson();
        cell = row.createCell(2);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(gson.toJson(str.getOffsets()))));
        cell.setCellStyle(style);

        cell = row.createCell(3);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(str.getText())));
        cell.setCellStyle(style);

        cell = row.createCell(4);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(str.getText())));
        cell.setCellStyle(style);

        cell = row.createCell(5);
        cell.setCellValue(new HSSFRichTextString(String.valueOf(str.getNeedRewrite())));
        cell.setCellStyle(style);
    }
}
