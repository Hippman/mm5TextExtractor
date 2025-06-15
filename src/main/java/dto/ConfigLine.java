package dto;

import enums.ConfigLineType;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ConfigLine {
    Map<ConfigLineType, String> data = new HashMap<>();
}
