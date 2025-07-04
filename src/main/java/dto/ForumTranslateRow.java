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

    public ForumTranslateRow(String[] parts) {
        if (parts.length == 3) {
            rowNum = parts[0];
            original = parts[1];
            translated = parts[2];
        }
    }
}
