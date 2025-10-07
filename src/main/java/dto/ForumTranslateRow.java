package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForumTranslateRow {
    private String rowNum;
    private String original;
    private String translated;

    public ForumTranslateRow(String[] parts, int rowNum) {

        original = "";
        translated = "";

            this.rowNum = String.valueOf(rowNum);

        if (parts.length > 1) {
            original = parts[1];
        }
        if (parts.length > 2) {
            translated = parts[2];
        }

    }

    public ForumTranslateRow(String original, String translated, int rowNum) {
        this.rowNum = String.valueOf(rowNum);
        this.original = original;
        this.translated = translated;

    }
}
