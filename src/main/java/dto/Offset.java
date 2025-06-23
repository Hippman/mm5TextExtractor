package dto;

import enums.OffsetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Offset {
    private OffsetType type;
    private int offset;
}
