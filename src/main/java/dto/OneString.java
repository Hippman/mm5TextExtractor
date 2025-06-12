package dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class OneString {
    private String text;
    private String oldtext;
    private Integer globalPosition;
    private Integer localPosition;
    private List<Offset> offsets;
    private Boolean needRewrite;
    private Boolean processed = false;

    private byte[] oldBytes;
    private byte[] newBytes;

    public int getNewSize() {
        if (newBytes != null) {
            return newBytes.length;
        }
        return 0;
    }


    public OneString() {
        offsets = new ArrayList<>();
    }

    public Boolean isDB() {
        if (offsets == null || offsets.isEmpty()) {
            return false;
        }
        return offsets.get(0).getType() == OffsetType.DB;
    }

    public Boolean checkPercents() {
        int countText = text.length() - text.replace("%", "").length();
        int countOldText = oldtext.length() - oldtext.replace("%", "").length();
        return countText == countOldText;
    }
}
